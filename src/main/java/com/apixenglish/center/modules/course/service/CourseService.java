package com.apixenglish.center.modules.course.service;

import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.course.dto.request.CreateCourseRequest;
import com.apixenglish.center.modules.course.dto.request.UpdateCourseRequest;
import com.apixenglish.center.modules.course.dto.response.CourseResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CourseService {
    PageResponse<CourseResponse> getCourses(String search, Pageable pageable);
    CourseResponse getCourseById(UUID id);
    CourseResponse createCourse(CreateCourseRequest request);
    CourseResponse updateCourse(UUID id, UpdateCourseRequest request);
    void deleteCourse(UUID id);
}
