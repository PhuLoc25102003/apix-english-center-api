package com.apixenglish.center.modules.classmanagement.service;

import com.apixenglish.center.modules.classmanagement.dto.request.AssignClassStaffRequest;
import com.apixenglish.center.modules.classmanagement.dto.response.ClassStaffResponse;
import com.apixenglish.center.modules.classmanagement.dto.response.ClassResponse;

import java.util.List;
import java.util.UUID;

public interface ClassStaffService {
    ClassStaffResponse assignStaff(UUID classId, AssignClassStaffRequest request);
    List<ClassStaffResponse> listClassStaff(UUID classId);
    void deactivateClassStaff(UUID classId, UUID classStaffId);
    List<ClassResponse> getMyClasses(UUID userId);
}
