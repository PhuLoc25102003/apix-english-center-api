package com.apixenglish.center.modules.student.service;

import com.apixenglish.center.modules.parent.dto.response.ParentChildResponse;
import com.apixenglish.center.modules.student.dto.request.LinkStudentParentRequest;
import com.apixenglish.center.modules.student.dto.response.StudentParentResponse;

import java.util.List;
import java.util.UUID;

public interface StudentParentService {
    StudentParentResponse linkStudentParent(UUID studentId, LinkStudentParentRequest request);
    List<StudentParentResponse> getStudentParents(UUID studentId);
    List<ParentChildResponse> getParentChildren(UUID parentId);
    void unlinkStudentParent(UUID studentId, UUID parentId);
}
