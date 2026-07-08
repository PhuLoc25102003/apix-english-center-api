package com.apixenglish.center.modules.classmanagement.mapper;

import com.apixenglish.center.modules.classmanagement.dto.response.ClassStaffResponse;
import com.apixenglish.center.modules.classmanagement.entity.ClassStaff;
import org.springframework.stereotype.Component;

@Component
public class ClassStaffMapper {

    public ClassStaffResponse toResponse(ClassStaff staff) {
        if (staff == null) return null;
        return ClassStaffResponse.builder()
                .id(staff.getId())
                .classId(staff.getClazz() != null ? staff.getClazz().getId() : null)
                .employeeId(staff.getEmployee() != null ? staff.getEmployee().getId() : null)
                .employeeCode(staff.getEmployee() != null ? staff.getEmployee().getEmployeeCode() : null)
                .employeeName(staff.getEmployee() != null ? staff.getEmployee().getFullName() : null)
                .staffType(staff.getStaffRole())
                .startDate(staff.getStartDate())
                .endDate(staff.getEndDate())
                .isPrimary(staff.getIsPrimary())
                .build();
    }
}
