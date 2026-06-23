package com.zencube.registry.profile.service.impl;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.profile.dto.response.ProjectResponse;
import com.zencube.registry.profile.mapper.ProjectMapper;
import com.zencube.registry.profile.repository.StudentProjectRepository;
import com.zencube.registry.profile.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private final StudentProjectRepository repository;
    private final UserRepository userRepository;
    private final ProjectMapper mapper;

    @Override
    public List<ProjectResponse> getMyProjects() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        return repository.findByProfileUserId(user.getId())
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<ProjectResponse> getProjectsByUserId(UUID userId) {
        return repository.findByProfileUserId(userId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
