package com.uit.weddingmanagement.modules.system.application.port.in;

import com.uit.weddingmanagement.modules.system.application.model.result.SystemSettingsResult;
import com.uit.weddingmanagement.modules.system.application.model.command.UpdateCancellationRuleCommand;

public interface UpdateCancellationRuleUseCase {

  SystemSettingsResult updateCancellationRule(UpdateCancellationRuleCommand command);
}
