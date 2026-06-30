package com.uit.weddingmanagement.modules.system.application.usecase;

import com.uit.weddingmanagement.modules.system.application.model.result.SystemSettingsResult;
import com.uit.weddingmanagement.modules.system.application.port.in.GetSystemSettingsUseCase;
import com.uit.weddingmanagement.modules.system.application.port.out.SystemParameterQueryPort;
import com.uit.weddingmanagement.modules.system.domain.model.SystemParameter;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetSystemSettingsService implements GetSystemSettingsUseCase {

  private final SystemParameterQueryPort systemParameterQueryPort;

  public GetSystemSettingsService(SystemParameterQueryPort systemParameterQueryPort) {
    this.systemParameterQueryPort = systemParameterQueryPort;
  }

  @Override
  public SystemSettingsResult getSystemSettings() {
    return SystemSettingsResult.from(loadSystemParameter());
  }

  private SystemParameter loadSystemParameter() {
    return systemParameterQueryPort
        .getSystemParameter()
        .orElseThrow(() -> new EntityNotFoundException("System settings have not been initialized."));
  }
}
