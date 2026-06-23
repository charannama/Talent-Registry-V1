package com.zencube.registry.auth.service.impl;

import com.zencube.registry.auth.dto.role.CreateRoleRequest;
import com.zencube.registry.auth.dto.role.RoleResponse;
import com.zencube.registry.auth.dto.role.UpdateRoleRequest;
import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.auth.repository.RoleRepository;
import com.zencube.registry.auth.service.interfaces.RoleService;
import com.zencube.registry.common.exception.BusinessException;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        if (roleRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new BusinessException("Role already exists with name: '" + request.getName() + "'", org.springframework.http.HttpStatus.CONFLICT);
        }

        Role role = Role.builder()
                .name(request.getName())
                .roleType(request.getRoleType())
                .description(request.getDescription())
                .isSystem(false)
                .build();

        Role savedRole = roleRepository.save(role);
        return mapToResponse(savedRole);
    }

    @Override
    public RoleResponse getRole(UUID id) {
        Role role = roleRepository.findById(id)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        return mapToResponse(role);
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .filter(r -> !r.isDeleted())
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public RoleResponse updateRole(UUID id, UpdateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        role.setName(request.getName());
        role.setDescription(request.getDescription());
        
        Role savedRole = roleRepository.save(role);
        return mapToResponse(savedRole);
    }

    @Override
    @Transactional
    public void deleteRole(UUID id) {
        Role role = roleRepository.findById(id)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        role.softDelete("system");
        roleRepository.save(role);
    }

    private RoleResponse mapToResponse(Role role) {
        if (role == null) {
            return null;
        }
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .roleType(role.getRoleType())
                .description(role.getDescription())
                .isSystem(role.isSystem())
                .build();
    }
}
