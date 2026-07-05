package com.apixenglish.center.modules.classmanagement.service.impl;

import com.apixenglish.center.common.exception.BusinessException;
import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.campus.entity.Campus;
import com.apixenglish.center.modules.campus.repository.CampusRepository;
import com.apixenglish.center.modules.classmanagement.dto.request.CreateClassRequest;
import com.apixenglish.center.modules.classmanagement.dto.request.UpdateClassRequest;
import com.apixenglish.center.modules.classmanagement.dto.response.ClassResponse;
import com.apixenglish.center.modules.classmanagement.entity.Clazz;
import com.apixenglish.center.modules.classmanagement.mapper.ClazzMapper;
import com.apixenglish.center.modules.classmanagement.repository.ClazzRepository;
import com.apixenglish.center.modules.classmanagement.service.ClazzService;
import com.apixenglish.center.modules.course.entity.Course;
import com.apixenglish.center.modules.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClazzServiceImpl implements ClazzService {

    private final ClazzRepository clazzRepository;
    private final CourseRepository courseRepository;
    private final CampusRepository campusRepository;
    private final ClazzMapper clazzMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClassResponse> getClasses(String search, Pageable pageable) {
        Page<Clazz> clazzPage = clazzRepository.searchClasses(search, pageable);
        Page<ClassResponse> responsePage = clazzPage.map(clazzMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public ClassResponse getClassById(UUID id) {
        Clazz clazz = clazzRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
        return clazzMapper.toResponse(clazz);
    }

    @Override
    @Transactional
    public ClassResponse createClass(CreateClassRequest request) {
        if (request.getStartDate().isAfter(request.getExpectedEndDate())) {
            throw new BusinessException("Start date must be less than or equal to expected end date", "INVALID_DATES");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Campus campus = campusRepository.findById(request.getCampusId())
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Campus not found"));

        String classCode = generateNextClassCode();

        Clazz clazz = Clazz.builder()
                .course(course)
                .campus(campus)
                .classCode(classCode)
                .name(request.getName())
                .capacity(request.getCapacity())
                .startDate(request.getStartDate())
                .expectedEndDate(request.getExpectedEndDate())
                .status(request.getStatus())
                .note(request.getNote())
                .build();

        Clazz savedClazz = clazzRepository.save(clazz);
        return clazzMapper.toResponse(savedClazz);
    }

    @Override
    @Transactional
    public ClassResponse updateClass(UUID id, UpdateClassRequest request) {
        if (request.getStartDate().isAfter(request.getExpectedEndDate())) {
            throw new BusinessException("Start date must be less than or equal to expected end date", "INVALID_DATES");
        }

        Clazz clazz = clazzRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Campus campus = campusRepository.findById(request.getCampusId())
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Campus not found"));

        clazz.setCourse(course);
        clazz.setCampus(campus);
        clazz.setName(request.getName());
        clazz.setCapacity(request.getCapacity());
        clazz.setStartDate(request.getStartDate());
        clazz.setExpectedEndDate(request.getExpectedEndDate());
        clazz.setStatus(request.getStatus());
        clazz.setNote(request.getNote());

        Clazz updatedClazz = clazzRepository.save(clazz);
        return clazzMapper.toResponse(updatedClazz);
    }

    @Override
    @Transactional
    public void deleteClass(UUID id) {
        Clazz clazz = clazzRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        clazz.delete();
        clazzRepository.save(clazz);
    }

    private synchronized String generateNextClassCode() {
        String maxCode = clazzRepository.findMaxClassCode();
        if (maxCode == null) {
            return "CLS000001";
        }
        try {
            int numericPart = Integer.parseInt(maxCode.substring(3));
            return String.format("CLS%06d", numericPart + 1);
        } catch (Exception e) {
            return "CLS" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }
    }
}
