package com.uit.weddingmanagement.modules.system.application.port.in;

import com.uit.weddingmanagement.modules.system.application.model.result.SystemSettingsResult;
import com.uit.weddingmanagement.modules.system.application.model.command.UpdateDepositRuleCommand;

public interface UpdateDepositRuleUseCase {

  SystemSettingsResult updateDepositRule(UpdateDepositRuleCommand command);
}
