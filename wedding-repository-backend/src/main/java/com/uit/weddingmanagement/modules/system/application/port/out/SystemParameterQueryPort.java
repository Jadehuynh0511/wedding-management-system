package com.uit.weddingmanagement.modules.system.application.port.out;

import com.uit.weddingmanagement.modules.system.domain.model.SystemParameter;
import java.util.Optional;

public interface SystemParameterQueryPort {

  Optional<SystemParameter> getSystemParameter();
}
