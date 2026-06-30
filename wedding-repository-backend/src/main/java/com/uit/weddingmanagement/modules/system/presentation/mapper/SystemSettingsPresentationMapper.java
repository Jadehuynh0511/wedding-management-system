package com.uit.weddingmanagement.modules.system.presentation.mapper;

import com.uit.weddingmanagement.modules.system.application.model.result.SystemSettingsResult;
import com.uit.weddingmanagement.modules.system.presentation.dto.response.CancellationRuleResponse;
import com.uit.weddingmanagement.modules.system.presentation.dto.response.DepositRuleResponse;
import com.uit.weddingmanagement.modules.system.presentation.dto.response.LatePaymentPenaltyRuleResponse;
import com.uit.weddingmanagement.modules.system.presentation.dto.response.SystemSettingsResponse;

import org.springframework.stereotype.Component;

/**
 * Mapper chuyển đổi SystemSettingsResult sang SystemSettingsResponse.
 * Được tách ra từ SystemSettingsController để tuân thủ SRP.
 */
@Component
public class SystemSettingsPresentationMapper {

    public SystemSettingsResponse toResponse(SystemSettingsResult result) {
        return new SystemSettingsResponse(
                result.id(),
                new DepositRuleResponse(result.depositRule().minimumDepositPercentage()),
                new LatePaymentPenaltyRuleResponse(
                        result.latePaymentPenaltyRule().latePaymentPenaltyEnabled(),
                        result.latePaymentPenaltyRule().latePaymentPenaltyRate()),
                new CancellationRuleResponse(
                        result.cancellationRule().cancellationDeadlineDays(),
                        result.cancellationRule().depositRefundPercentage()));
    }
}
