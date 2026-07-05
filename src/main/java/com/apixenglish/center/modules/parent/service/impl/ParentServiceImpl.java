package com.apixenglish.center.modules.parent.service.impl;

import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.parent.dto.request.CreateParentRequest;
import com.apixenglish.center.modules.parent.dto.request.UpdateParentRequest;
import com.apixenglish.center.modules.parent.dto.response.ParentResponse;
import com.apixenglish.center.modules.parent.entity.Parent;
import com.apixenglish.center.modules.parent.mapper.ParentMapper;
import com.apixenglish.center.modules.parent.repository.ParentRepository;
import com.apixenglish.center.modules.parent.service.ParentService;
import com.apixenglish.center.modules.user.entity.User;
import com.apixenglish.center.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParentServiceImpl implements ParentService {

    private final ParentRepository parentRepository;
    private final UserRepository userRepository;
    private final ParentMapper parentMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ParentResponse> getParents(String search, Pageable pageable) {
        Page<Parent> parentPage = parentRepository.searchParents(search, pageable);
        Page<ParentResponse> responsePage = parentPage.map(parentMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public ParentResponse getParentById(UUID id) {
        Parent parent = parentRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));
        return parentMapper.toResponse(parent);
    }

    @Override
    @Transactional
    public ParentResponse createParent(CreateParentRequest request) {
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        String parentCode = generateNextParentCode();

        Parent parent = Parent.builder()
                .user(user)
                .parentCode(parentCode)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .jobTitle(request.getJobTitle())
                .note(request.getNote())
                .build();

        Parent savedParent = parentRepository.save(parent);
        return parentMapper.toResponse(savedParent);
    }

    @Override
    @Transactional
    public ParentResponse updateParent(UUID id, UpdateParentRequest request) {
        Parent parent = parentRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));

        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        parent.setUser(user);
        parent.setFullName(request.getFullName());
        parent.setPhone(request.getPhone());
        parent.setEmail(request.getEmail());
        parent.setAddress(request.getAddress());
        parent.setJobTitle(request.getJobTitle());
        parent.setNote(request.getNote());

        Parent updatedParent = parentRepository.save(parent);
        return parentMapper.toResponse(updatedParent);
    }

    @Override
    @Transactional
    public void deleteParent(UUID id) {
        Parent parent = parentRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));

        parent.delete();
        parentRepository.save(parent);
    }

    private synchronized String generateNextParentCode() {
        String maxCode = parentRepository.findMaxParentCode();
        if (maxCode == null) {
            return "PAR000001";
        }
        try {
            int numericPart = Integer.parseInt(maxCode.substring(3));
            return String.format("PAR%06d", numericPart + 1);
        } catch (Exception e) {
            return "PAR" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }
    }
}
