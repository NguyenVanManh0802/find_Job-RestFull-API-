package vn.manh.findJob.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.manh.findJob.domain.Permission;
import vn.manh.findJob.domain.Role;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.exception.ResourceAlreadyExistsException;
import vn.manh.findJob.exception.ResourceNotFoundException;
import vn.manh.findJob.repository.PermissionRepository;
import vn.manh.findJob.repository.RoleRepository;


import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository; // Inject Permission repo

    private Role findRoleById(long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
    }

    public Role createRole(Role role) {
        log.info("Creating new role with name: {}", role.getName());
        if (roleRepository.existsByName(role.getName())) {
            throw new ResourceAlreadyExistsException("Role with name '" + role.getName() + "' already exists.");
        }

        // Fetch managed permissions
        if (role.getPermissions() != null) {
            List<Long> permissionIds = role.getPermissions().stream()
                    .map(Permission::getId)
                    .collect(Collectors.toList());
            List<Permission> dbPermissions = permissionRepository.findByIdIn(permissionIds);

            role.setPermissions(dbPermissions);
        }

        Role savedRole = roleRepository.save(role);
        log.info("Role created successfully, roleId={}", savedRole.getId());
        return savedRole;
    }

    public ResultPaginationDTO getAllRoles(Specification<Role> specification, Pageable pageable) {
        Page<Role> pageRole = roleRepository.findAll(specification, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1); // Adjust for 1-based display
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageRole.getTotalPages());
        meta.setTotal(pageRole.getTotalElements());
        rs.setMeta(meta);


        rs.setResult(pageRole.getContent());
        return rs;
    }

    public Role getRoleById(long id) {
        log.info("Fetching role with id={}", id);
        Role role = this.findRoleById(id);
        return role;
    }

    public Role updateRole(long id, Role role) {
        log.info("Updating role with id={}", id);
        Role existingRole = this.findRoleById(id);

        // Check uniqueness if name is changed and different from original
        if (!existingRole.getName().equals(role.getName()) && roleRepository.existsByName(role.getName())) {
            throw new ResourceAlreadyExistsException("Role with name '" + role.getName() + "' already exists.");
        }

        existingRole.setName(role.getName());
        existingRole.setDescription(role.getDescription());
        existingRole.setActive(role.isActive());

        // Fetch managed permissions for update
        if (role.getPermissions() != null) {
            List<Long> permissionIds = role.getPermissions().stream()
                    .map(Permission::getId)
                    .collect(Collectors.toList());
            List<Permission> dbPermissions = permissionRepository.findByIdIn(permissionIds);
            existingRole.setPermissions(dbPermissions);
        } else {
            existingRole.setPermissions(null); // Clear permissions if null is passed
        }

        Role updatedRole = roleRepository.save(existingRole);
        log.info("Role updated successfully, roleId={}", updatedRole.getId());
        return updatedRole;
    }

    public void deleteRole(long id) {
        log.info("Deleting role with id={}", id);
        Role role = this.findRoleById(id); // Ensure it exists
        // Add business logic checks if needed (e.g., prevent deleting ADMIN role)
        roleRepository.delete(role); // Use delete(entity) for potential cascade/lifecycle handling
        log.info("Role deleted successfully, roleId={}", id);
    }
}