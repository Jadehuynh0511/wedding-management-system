package com.uit.weddingmanagement.modules.catalog.application.port.out;

import com.uit.weddingmanagement.modules.catalog.domain.model.Shift;
import java.util.List;
import java.util.Optional;

public interface ShiftQueryPort {

  List<Shift> findAllShifts();

  Optional<Shift> findShiftById(Long shiftId);

  boolean existsShiftByName(String shiftName);

  boolean existsShiftByNameAndIdNot(String shiftName, Long shiftId);
}
