package com.zencube.registry.profile.service.impl;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.profile.dto.response.WorkExperienceResponse;
import com.zencube.registry.profile.mapper.WorkExperienceMapper;
import com.zencube.registry.profile.repository.WorkExperienceRepository;
import com.zencube.registry.profile.service.WorkExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkExperienceServiceImpl implements WorkExperienceService {

    private final WorkExperienceRepository repository;
    private final UserRepository userRepository;
    private final WorkExperienceMapper mapper;

    @Override
    public List<WorkExperienceResponse> getMyExperiences() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        return repository.findByProfileUserIdOrderByStartDateDesc(user.getId())
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<WorkExperienceResponse> getExperiencesByUserId(UUID userId) {
        return repository.findByProfileUserIdOrderByStartDateDesc(userId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
