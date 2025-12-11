package vn.manh.findJob.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.manh.findJob.domain.Permission;
import vn.manh.findJob.domain.Role;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.exception.ResourceAlreadyExistsException;
import vn.manh.findJob.exception.ResourceNotFoundException;
import vn.manh.findJob.repository.PermissionRepository;
import vn.manh.findJob.repository.RoleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional // Giữ transaction cho toàn bộ class
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    // --- HÀM HỖ TRỢ ĐẶC BIỆT ---
    // Hàm này biến "Hibernate Role" thành "Java Role" sạch sẽ để lưu Cache
    private Role sanitizeRole(Role dbRole) {
        Role cleanRole = new Role();
        cleanRole.setId(dbRole.getId());
        cleanRole.setName(dbRole.getName());
        cleanRole.setDescription(dbRole.getDescription());
        cleanRole.setActive(dbRole.isActive());
        cleanRole.setCreatedAt(dbRole.getCreatedAt());
        cleanRole.setUpdatedAt(dbRole.getUpdatedAt());
        cleanRole.setCreatedBy(dbRole.getCreatedBy());
        cleanRole.setUpdatedBy(dbRole.getUpdatedBy());

        // Quan trọng: Chuyển List Hibernate thành ArrayList Java
        if (dbRole.getPermissions() != null) {
            cleanRole.setPermissions(new ArrayList<>(dbRole.getPermissions()));
        }
        return cleanRole;
    }

    private Role findRoleById(long id) {
        return roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
    }
    public Role handleGetRoleByName(String name) {
        return this.roleRepository.findByName(name);
    }
    public Role createRole(Role role) {
        if (roleRepository.existsByName(role.getName())) {
            throw new ResourceAlreadyExistsException("Role with name '" + role.getName() + "' already exists.");
        }
        if (role.getPermissions() != null) {
            List<Long> permissionIds = role.getPermissions().stream()
                    .map(Permission::getId).collect(Collectors.toList());
            role.setPermissions(permissionRepository.findByIdIn(permissionIds));
        }
        return roleRepository.save(role);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO getAllRoles(Specification<Role> specification, Pageable pageable) {
        Page<Role> pageRole = roleRepository.findAll(specification, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageRole.getTotalPages());
        meta.setTotal(pageRole.getTotalElements());
        rs.setMeta(meta);
        rs.setResult(pageRole.getContent());
        return rs;
    }

    // --- GET CACHE ---
    @Cacheable(value = "roles", key = "#id")
    public Role getRoleById(long id) {
        log.info("Fetching role from Database with id={}", id);
        Role dbRole = this.findRoleById(id);
        // Trả về bản sao sạch sẽ
        return this.sanitizeRole(dbRole);
    }

    // --- UPDATE CACHE ---
    @CachePut(value = "roles", key = "#id")
    public Role updateRole(long id, Role role) {
        log.info("Updating role with id={}", id);
        Role existingRole = this.findRoleById(id);

        if (!existingRole.getName().equals(role.getName()) && roleRepository.existsByName(role.getName())) {
            throw new ResourceAlreadyExistsException("Role with name '" + role.getName() + "' already exists.");
        }

        existingRole.setName(role.getName());
        existingRole.setDescription(role.getDescription());
        existingRole.setActive(role.isActive());

        if (role.getPermissions() != null) {
            List<Long> permissionIds = role.getPermissions().stream()
                    .map(Permission::getId).collect(Collectors.toList());
            existingRole.setPermissions(permissionRepository.findByIdIn(permissionIds));
        } else {
            existingRole.setPermissions(null);
        }

        Role savedRole = roleRepository.save(existingRole);

        // Trả về bản sao sạch sẽ của cái vừa lưu
        return this.sanitizeRole(savedRole);
    }

    @CacheEvict(value = "roles", key = "#id")
    public void deleteRole(long id) {
        Role role = this.findRoleById(id);
        roleRepository.delete(role);
    }
}