package com.apixenglish.center.modules.course.service.impl;

import com.apixenglish.center.common.exception.ConflictException;
import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.course.dto.request.CreateCourseRequest;
import com.apixenglish.center.modules.course.dto.request.UpdateCourseRequest;
import com.apixenglish.center.modules.course.dto.response.CourseResponse;
import com.apixenglish.center.modules.course.entity.Course;
import com.apixenglish.center.modules.course.entity.Level;
import com.apixenglish.center.modules.course.mapper.CourseMapper;
import com.apixenglish.center.modules.course.repository.CourseRepository;
import com.apixenglish.center.modules.course.repository.LevelRepository;
import com.apixenglish.center.modules.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final LevelRepository levelRepository;
    private final CourseMapper courseMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> getCourses(String search, Pageable pageable) {
        Page<Course> coursePage = courseRepository.searchCourses(search, pageable);
        Page<CourseResponse> responsePage = coursePage.map(courseMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(UUID id) {
        Course course = courseRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        return courseMapper.toResponse(course);
    }

    @Override
    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request) {
        Level level = levelRepository.findById(request.getLevelId())
                .filter(l -> l.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Level not found"));

        if (courseRepository.existsByCodeAndDeletedAtIsNull(request.getCode())) {
            throw new ConflictException("Course code already exists");
        }

        Course course = Course.builder()
                .level(level)
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .totalLessons(request.getTotalLessons())
                .durationMinutes(request.getDurationMinutes())
                .defaultTuitionFee(request.getDefaultTuitionFee())
                .status(request.getStatus())
                .build();

        Course savedCourse = courseRepository.save(course);
        return courseMapper.toResponse(savedCourse);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(UUID id, UpdateCourseRequest request) {
        Course course = courseRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Level level = levelRepository.findById(request.getLevelId())
                .filter(l -> l.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Level not found"));

        if (!course.getCode().equals(request.getCode())) {
            if (courseRepository.existsByCodeAndDeletedAtIsNull(request.getCode())) {
                throw new ConflictException("Course code already exists");
            }
        }

        course.setLevel(level);
        course.setCode(request.getCode());
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setTotalLessons(request.getTotalLessons());
        course.setDurationMinutes(request.getDurationMinutes());
        course.setDefaultTuitionFee(request.getDefaultTuitionFee());
        course.setStatus(request.getStatus());

        Course updatedCourse = courseRepository.save(course);
        return courseMapper.toResponse(updatedCourse);
    }

    @Override
    @Transactional
    public void deleteCourse(UUID id) {
        Course course = courseRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        course.delete();
        courseRepository.save(course);
    }
}
