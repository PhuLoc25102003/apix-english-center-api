package com.apixenglish.center.modules.student.service;

import com.apixenglish.center.modules.student.dto.request.LinkStudentParentRequest;
import com.apixenglish.center.modules.student.dto.response.StudentParentResponse;

import java.util.List;
import java.util.UUID;

public interface StudentParentService {
    StudentParentResponse linkStudentParent(UUID studentId, UUID parentId, LinkStudentParentRequest request);
    List<StudentParentResponse> getStudentParents(UUID studentId);
    void unlinkStudentParent(UUID studentId, UUID parentId);
}
