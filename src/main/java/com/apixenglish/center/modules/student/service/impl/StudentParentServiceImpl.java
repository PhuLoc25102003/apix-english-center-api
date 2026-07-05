package com.apixenglish.center.modules.student.service.impl;

import com.apixenglish.center.common.exception.ConflictException;
import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.modules.parent.dto.response.ParentChildResponse;
import com.apixenglish.center.modules.parent.entity.Parent;
import com.apixenglish.center.modules.parent.repository.ParentRepository;
import com.apixenglish.center.modules.student.dto.request.LinkStudentParentRequest;
import com.apixenglish.center.modules.student.dto.response.StudentParentResponse;
import com.apixenglish.center.modules.student.entity.Student;
import com.apixenglish.center.modules.student.entity.StudentParent;
import com.apixenglish.center.modules.student.mapper.StudentParentMapper;
import com.apixenglish.center.modules.student.repository.StudentParentRepository;
import com.apixenglish.center.modules.student.repository.StudentRepository;
import com.apixenglish.center.modules.student.service.StudentParentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentParentServiceImpl implements StudentParentService {

    private final StudentParentRepository studentParentRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final StudentParentMapper studentParentMapper;

    @Override
    @Transactional
    public StudentParentResponse linkStudentParent(UUID studentId, LinkStudentParentRequest request) {
        UUID parentId = request.getParentId();

        Student student = studentRepository.findById(studentId)
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Parent parent = parentRepository.findById(parentId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));

        boolean isPrimary = Boolean.TRUE.equals(request.getIsPrimaryContact());

        Optional<StudentParent> existingLinkOpt = studentParentRepository.findByStudentIdAndParentId(studentId, parentId);
        if (existingLinkOpt.isPresent()) {
            StudentParent existingLink = existingLinkOpt.get();
            if (existingLink.getDeletedAt() == null) {
                throw new ConflictException("Student and Parent are already linked");
            }
            
            // If it was soft-deleted, restore and update it!
            if (isPrimary) {
                demotePrimaryContacts(studentId);
            }
            existingLink.restore();
            existingLink.setRelationship(request.getRelationship());
            existingLink.setIsPrimaryContact(isPrimary);
            existingLink.setCanReceiveNotification(request.getCanReceiveNotification() != null ? request.getCanReceiveNotification() : true);
            existingLink.setCanReceiveTuition(request.getCanReceiveTuition() != null ? request.getCanReceiveTuition() : true);
            existingLink.setCanPickupStudent(request.getCanPickupStudent() != null ? request.getCanPickupStudent() : false);
            existingLink.setIsEmergencyContact(request.getIsEmergencyContact() != null ? request.getIsEmergencyContact() : false);

            StudentParent savedLink = studentParentRepository.save(existingLink);
            return studentParentMapper.toResponse(savedLink);
        }

        if (isPrimary) {
            demotePrimaryContacts(studentId);
        }

        StudentParent studentParent = StudentParent.builder()
                .student(student)
                .parent(parent)
                .relationship(request.getRelationship())
                .isPrimaryContact(isPrimary)
                .canReceiveNotification(request.getCanReceiveNotification() != null ? request.getCanReceiveNotification() : true)
                .canReceiveTuition(request.getCanReceiveTuition() != null ? request.getCanReceiveTuition() : true)
                .canPickupStudent(request.getCanPickupStudent() != null ? request.getCanPickupStudent() : false)
                .isEmergencyContact(request.getIsEmergencyContact() != null ? request.getIsEmergencyContact() : false)
                .build();

        StudentParent savedLink = studentParentRepository.save(studentParent);
        return studentParentMapper.toResponse(savedLink);
    }

    private void demotePrimaryContacts(UUID studentId) {
        List<StudentParent> currentLinks = studentParentRepository.findByStudentIdAndDeletedAtIsNull(studentId);
        for (StudentParent link : currentLinks) {
            if (Boolean.TRUE.equals(link.getIsPrimaryContact())) {
                link.setIsPrimaryContact(false);
                studentParentRepository.save(link);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentParentResponse> getStudentParents(UUID studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found");
        }

        List<StudentParent> links = studentParentRepository.findByStudentIdAndDeletedAtIsNull(studentId);
        return links.stream()
                .map(studentParentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentChildResponse> getParentChildren(UUID parentId) {
        if (!parentRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("Parent not found");
        }

        List<StudentParent> links = studentParentRepository.findByParentIdAndDeletedAtIsNull(parentId);
        return links.stream()
                .map(studentParentMapper::toParentChildResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void unlinkStudentParent(UUID studentId, UUID parentId) {
        StudentParent link = studentParentRepository.findByStudentIdAndParentIdAndDeletedAtIsNull(studentId, parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student-Parent link not found"));

        link.delete();
        studentParentRepository.save(link);
    }
}
