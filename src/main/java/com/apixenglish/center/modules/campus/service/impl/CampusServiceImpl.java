package com.apixenglish.center.modules.campus.service.impl;

import com.apixenglish.center.common.exception.ConflictException;
import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.campus.dto.request.CreateCampusRequest;
import com.apixenglish.center.modules.campus.dto.request.UpdateCampusRequest;
import com.apixenglish.center.modules.campus.dto.response.CampusResponse;
import com.apixenglish.center.modules.campus.entity.Campus;
import com.apixenglish.center.modules.campus.mapper.CampusMapper;
import com.apixenglish.center.modules.campus.repository.CampusRepository;
import com.apixenglish.center.modules.campus.service.CampusService;
import com.apixenglish.center.modules.campus.dto.response.CampusLookupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CampusServiceImpl implements CampusService {

    private final CampusRepository campusRepository;
    private final CampusMapper campusMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CampusResponse> getCampuses(String search, Pageable pageable) {
        Page<Campus> campusPage = campusRepository.searchCampuses(search, pageable);
        Page<CampusResponse> responsePage = campusPage.map(campusMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public CampusResponse getCampusById(UUID id) {
        Campus campus = campusRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Campus not found"));
        return campusMapper.toResponse(campus);
    }

    @Override
    @Transactional
    public CampusResponse createCampus(CreateCampusRequest request) {
        if (campusRepository.existsByCodeAndDeletedAtIsNull(request.getCode())) {
            throw new ConflictException("Campus code already exists");
        }

        Campus campus = Campus.builder()
                .code(request.getCode())
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Campus savedCampus = campusRepository.save(campus);
        return campusMapper.toResponse(savedCampus);
    }

    @Override
    @Transactional
    public CampusResponse updateCampus(UUID id, UpdateCampusRequest request) {
        Campus campus = campusRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Campus not found"));

        if (!campus.getCode().equals(request.getCode())) {
            if (campusRepository.existsByCodeAndDeletedAtIsNull(request.getCode())) {
                throw new ConflictException("Campus code already exists");
            }
        }

        campus.setCode(request.getCode());
        campus.setName(request.getName());
        campus.setAddress(request.getAddress());
        campus.setPhone(request.getPhone());
        campus.setDescription(request.getDescription());
        campus.setIsActive(request.getIsActive());

        Campus updatedCampus = campusRepository.save(campus);
        return campusMapper.toResponse(updatedCampus);
    }

    @Override
    @Transactional
    public void deleteCampus(UUID id) {
        Campus campus = campusRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Campus not found"));

        campus.delete();
        campusRepository.save(campus);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampusLookupResponse> lookupCampuses(String search, Boolean includeInactive) {
        boolean inclInactive = includeInactive != null && includeInactive;
        List<Campus> campuses = campusRepository.lookupCampuses(search, inclInactive);
        return campuses.stream()
                .map(c -> CampusLookupResponse.builder()
                        .id(c.getId())
                        .code(c.getCode())
                        .name(c.getName())
                        .displayName("[" + c.getCode() + "] " + c.getName())
                        .build())
                .toList();
    }
}
