package com.apixenglish.center.modules.curriculum.service.impl;

import com.apixenglish.center.common.exception.BusinessException;
import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.course.entity.Course;
import com.apixenglish.center.modules.course.repository.CourseRepository;
import com.apixenglish.center.modules.curriculum.dto.request.CreateCurriculumRequest;
import com.apixenglish.center.modules.curriculum.dto.request.UpdateCurriculumRequest;
import com.apixenglish.center.modules.curriculum.dto.response.CurriculumLookupResponse;
import com.apixenglish.center.modules.curriculum.dto.response.CurriculumResponse;
import com.apixenglish.center.modules.curriculum.entity.Curriculum;
import com.apixenglish.center.modules.curriculum.mapper.CurriculumMapper;
import com.apixenglish.center.modules.curriculum.repository.CurriculumRepository;
import com.apixenglish.center.modules.curriculum.service.CurriculumService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurriculumServiceImpl implements CurriculumService {

    private final CurriculumRepository curriculumRepository;
    private final CourseRepository courseRepository;
    private final CurriculumMapper curriculumMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CurriculumResponse> listCurriculums(String search, UUID courseId, Boolean isActive, Pageable pageable) {
        Page<Curriculum> page = curriculumRepository.searchCurriculums(search, courseId, isActive, pageable);
        return PageResponse.of(page.map(curriculumMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CurriculumLookupResponse> lookupCurriculums() {
        return curriculumRepository.findAllActive().stream()
                .map(curriculumMapper::toLookupResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CurriculumResponse getCurriculumById(UUID id) {
        Curriculum curriculum = curriculumRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum not found"));
        return curriculumMapper.toResponse(curriculum);
    }

    @Override
    @Transactional
    public CurriculumResponse createCurriculum(CreateCurriculumRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (curriculumRepository.existsByCourseIdAndVersionNameAndDeletedAtIsNull(request.getCourseId(), request.getVersionName())) {
            throw new BusinessException("Curriculum with this course and version already exists", "DUPLICATE_CURRICULUM_VERSION");
        }

        Curriculum curriculum = Curriculum.builder()
                .course(course)
                .name(request.getName())
                .versionName(request.getVersionName())
                .description(request.getDescription())
                .isActive(request.getIsActive())
                .build();

        return curriculumMapper.toResponse(curriculumRepository.save(curriculum));
    }

    @Override
    @Transactional
    public CurriculumResponse updateCurriculum(UUID id, UpdateCurriculumRequest request) {
        Curriculum curriculum = curriculumRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        curriculum.setCourse(course);
        curriculum.setName(request.getName());
        curriculum.setVersionName(request.getVersionName());
        curriculum.setDescription(request.getDescription());
        curriculum.setIsActive(request.getIsActive());

        return curriculumMapper.toResponse(curriculumRepository.save(curriculum));
    }

    @Override
    @Transactional
    public void deleteCurriculum(UUID id) {
        Curriculum curriculum = curriculumRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum not found"));
        curriculum.delete();
        curriculumRepository.save(curriculum);
    }
}
