package com.uit.weddingmanagement.modules.system.application.port.out;

import com.uit.weddingmanagement.modules.system.domain.model.SystemParameter;

public interface SystemParameterCommandPort {

  SystemParameter saveSystemParameter(SystemParameter systemParameter);
}
