package com.uit.weddingmanagement.modules.audit.infrastructure.aspect;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uit.weddingmanagement.modules.audit.application.port.in.RecordAuditLogUseCase;
import com.uit.weddingmanagement.modules.audit.domain.model.AuditActorSnapshot;
import com.uit.weddingmanagement.modules.audit.domain.model.AuditResultStatus;
import com.uit.weddingmanagement.modules.auth.application.port.out.CurrentUserPort;
import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Aspect gom logic đọc annotation, đánh giá expression và ghi audit log mà
 * không làm bẩn business flow.
 * Follow AOP aspect-oriented programming để tách biệt rõ ràng giữa logic audit
 * và logic nghiệp vụ chính.
 * Aspect này sẽ intercept các phương thức được đánh dấu bằng @AuditAction,đọc
 * metadata từ annotation, đánh giá các biểu thức SpEL để lấy thông tin chi tiết
 * về
 */
@Aspect
@Component
public class AuditActionAspect {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final Logger log = LoggerFactory.getLogger(AuditActionAspect.class);

    private final RecordAuditLogUseCase recordAuditLogUseCase;
    private final CurrentUserPort currentUserPort;
    private final ObjectMapper objectMapper;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public AuditActionAspect(
            RecordAuditLogUseCase recordAuditLogUseCase,
            CurrentUserPort currentUserPort,
            ObjectMapper objectMapper) {
        this.recordAuditLogUseCase = recordAuditLogUseCase;
        this.currentUserPort = currentUserPort;
        this.objectMapper = objectMapper;
    }

    // Phương thức này sẽ được gọi mỗi khi một phương thức được đánh dấu bằng
    // @AuditAction được gọi
    @Around("@annotation(com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction)")
    public Object aroundAuditAction(ProceedingJoinPoint joinPoint) throws Throwable {
        // Chuẩn bị metadata trước khi gọi method thật
        Method method = resolveMethod(joinPoint);
        AuditAction auditAction = method.getAnnotation(AuditAction.class);
        AuditActorSnapshot actorSnapshot = resolveActorSnapshot();
        OffsetDateTime occurredAt = OffsetDateTime.now();

        try {
            // Gọi method thật và ghi log audit dựa trên kết quả trả về hoặc exception nếu
            // có
            Object result = joinPoint.proceed();
            // Nếu thành công thì ghi log với status SUCCESS
            safeRecordAudit(
                    auditAction,
                    method,
                    joinPoint.getArgs(), // Lấy Arguments gốc của method, sẽ được dùng để đánh giá SpEL
                    actorSnapshot,
                    occurredAt,
                    AuditResultStatus.SUCCESS,
                    result,
                    null);
            return result;
        } catch (Throwable throwable) {
            // Nếu có exception thì ghi log với status FAIL và thông tin lỗi
            safeRecordAudit(
                    auditAction,
                    method,
                    joinPoint.getArgs(),
                    actorSnapshot,
                    occurredAt,
                    AuditResultStatus.FAIL,
                    null,
                    throwable);
            throw throwable;
        }
    }

    private void safeRecordAudit(
            AuditAction auditAction,
            Method method,
            Object[] arguments,
            AuditActorSnapshot actorSnapshot,
            OffsetDateTime occurredAt,
            AuditResultStatus resultStatus,
            Object result,
            Throwable throwable) {
        try {
            recordAudit(auditAction, method, arguments, actorSnapshot, occurredAt, resultStatus, result, throwable);
        } catch (RuntimeException exception) {
            log.warn(
                    "Failed to write audit log for action {} on method {}",
                    auditAction.action(),
                    method.toGenericString(),
                    exception);
        }
    }

    // Hàm này thực sự thực hiện việc ghi một bản ghi audit log mới bằng cách đánh
    // giá các biểu thức SpEL trong annotation để lấy thông tin chi tiết về hành
    // động, đối tượng bị tác động, kết quả của hành động, và các thông tin bổ sung
    // khác, sau đó gọi RecordAuditLogUseCase để lưu trữ bản ghi vào persistence.
    private void recordAudit(
            AuditAction auditAction,
            Method method,
            Object[] arguments,
            AuditActorSnapshot actorSnapshot,
            OffsetDateTime occurredAt,
            AuditResultStatus resultStatus,
            Object result,
            Throwable throwable) {
        EvaluationContext evaluationContext = createEvaluationContext(method, arguments, actorSnapshot, result,
                throwable);
        String description = resolveDescription(auditAction, evaluationContext, resultStatus);

        recordAuditLogUseCase.record(new RecordAuditLogUseCase.RecordAuditLogCommand(
                occurredAt,
                actorSnapshot,
                auditAction.action(),
                auditAction.module(),
                auditAction.targetType(),
                evaluateString(auditAction.targetIdExpression(), evaluationContext),
                evaluateString(auditAction.targetLabelExpression(), evaluationContext),
                resultStatus,
                StringUtils.hasText(description) ? description : buildFallbackDescription(auditAction),
                resultStatus == AuditResultStatus.FAIL ? extractErrorMessage(throwable) : null,
                evaluateDetails(auditAction.detailsExpression(), evaluationContext)));
    }

    private Method resolveMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return AopUtils.getMostSpecificMethod(signature.getMethod(), joinPoint.getTarget().getClass());
    }

    // Giải pháp lấy thông tin người thực hiện hành động audit, nếu có thể. Nếu
    // không lấy được thông tin người dùng hiện tại (ví dụ: do lỗi hoặc vì hành động
    // này được
    private AuditActorSnapshot resolveActorSnapshot() {
        try {
            AuthenticatedUser currentUser = currentUserPort.getCurrentUser();
            return new AuditActorSnapshot(currentUser.id(), currentUser.username(), currentUser.groupName());
        } catch (RuntimeException exception) {
            return AuditActorSnapshot.system();
        }
    }

    // Tạo EvaluationContext để đánh giá các biểu thức SpEL trong annotation
    private EvaluationContext createEvaluationContext(
            Method method,
            Object[] arguments,
            AuditActorSnapshot actorSnapshot,
            Object result,
            Throwable throwable) {
        MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(null, method, arguments,
                parameterNameDiscoverer);
        evaluationContext.setVariable("args", arguments);
        evaluationContext.setVariable("actor", actorSnapshot);
        evaluationContext.setVariable("result", result);
        evaluationContext.setVariable("error", throwable);

        for (int index = 0; index < arguments.length; index++) {
            evaluationContext.setVariable("p" + index, arguments[index]);
            evaluationContext.setVariable("a" + index, arguments[index]);
        }

        return evaluationContext;
    }

    private String resolveDescription(
            AuditAction auditAction,
            EvaluationContext evaluationContext,
            AuditResultStatus resultStatus) {
        String expression = resultStatus == AuditResultStatus.SUCCESS
                ? auditAction.successDescriptionExpression()
                : auditAction.failureDescriptionExpression();

        return evaluateString(expression, evaluationContext);
    }

    private String evaluateString(String expressionText, EvaluationContext evaluationContext) {
        if (!StringUtils.hasText(expressionText)) {
            return null;
        }

        Object value = evaluateExpression(expressionText, evaluationContext);
        if (value == null) {
            return null;
        }

        String stringValue = value.toString().trim();
        return stringValue.isEmpty() ? null : stringValue;
    }

    private Map<String, Object> evaluateDetails(String expressionText, EvaluationContext evaluationContext) {
        if (!StringUtils.hasText(expressionText)) {
            return null;
        }

        Object value = evaluateExpression(expressionText, evaluationContext);
        if (value == null) {
            return null;
        }

        if (value instanceof Map<?, ?> mapValue) {
            Map<String, Object> details = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                if (entry.getKey() instanceof String key) {
                    details.put(key, entry.getValue());
                }
            }
            return details.isEmpty() ? null : details;
        }

        if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean) {
            return Map.of("value", value);
        }

        Map<String, Object> converted = objectMapper.convertValue(value, MAP_TYPE);
        return converted.isEmpty() ? null : converted;
    }

    // Đọc chuỗi SpEL -> parse nó -> dùng context hiện tại để tính giá trị thật
    private Object evaluateExpression(String expressionText, EvaluationContext evaluationContext) {
        Expression expression = expressionParser.parseExpression(expressionText);
        return expression.getValue(evaluationContext);
    }

    private String buildFallbackDescription(AuditAction auditAction) {
        return auditAction.action() + " on " + auditAction.targetType();
    }

    private String extractErrorMessage(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        if (StringUtils.hasText(throwable.getMessage())) {
            return throwable.getMessage().trim();
        }

        return throwable.getClass().getSimpleName();
    }
}
