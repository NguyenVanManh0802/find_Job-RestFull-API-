package vn.manh.findJob.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.manh.findJob.domain.Permission;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.exception.ResourceAlreadyExistsException;
import vn.manh.findJob.exception.ResourceNotFoundException;
import vn.manh.findJob.repository.PermissionRepository;
import vn.manh.findJob.repository.RoleRepository;


@Slf4j
@RequiredArgsConstructor
@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository; // Inject Role repo for delete check

    private Permission findPermissionById(long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));
    }

    public Permission createPermission(Permission permission) {
        log.info("Creating new permission: {}", permission.getName());
        if(permissionRepository.existsByName(permission.getName())) {
            throw new ResourceAlreadyExistsException("Permission with name '" + permission.getName() + "' already exists.");
        }
        if(permissionRepository.existsByApiPathAndMethod(permission.getApiPath(), permission.getMethod())) {
            throw new ResourceAlreadyExistsException("Permission with path '" + permission.getApiPath() + "' and method '" + permission.getMethod() + "' already exists.");
        }

        Permission savedPermission = permissionRepository.save(permission);
        log.info("Permission created successfully, permissionId={}", savedPermission.getId());
        return savedPermission;
    }

    public ResultPaginationDTO getAllPermissions(Specification<Permission> specification, Pageable pageable) {
        Page<Permission> pagePermission = permissionRepository.findAll(specification, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1); // Adjust for 1-based display
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pagePermission.getTotalPages());
        meta.setTotal(pagePermission.getTotalElements());
        rs.setMeta(meta);

        rs.setResult(pagePermission.getContent());
        return rs;
    }

    public Permission getPermissionById(long id) {
        log.info("Fetching permission with id={}", id);
        Permission permission = this.findPermissionById(id);
        return permission;
    }

    public Permission updatePermission(long id, Permission permission) {
        log.info("Updating permission with id={}", id);
        Permission existingPermission = this.findPermissionById(id);

        // Add uniqueness checks if name/apiPath/method are changed
        if (!existingPermission.getName().equals(permission.getName()) && permissionRepository.existsByName(permission.getName())) {
            throw new ResourceAlreadyExistsException("Permission with name '" + permission.getName() + "' already exists.");
        }
        if ((!existingPermission.getApiPath().equals(permission.getApiPath()) || !existingPermission.getMethod().equals(permission.getMethod())) &&
                permissionRepository.existsByApiPathAndMethod(permission.getApiPath(), permission.getMethod())) {
            throw new ResourceAlreadyExistsException("Permission with path '" + permission.getApiPath() + "' and method '" + permission.getMethod() + "' already exists.");
        }

        existingPermission.setName(permission.getName());
        existingPermission.setApiPath(permission.getApiPath());
        existingPermission.setMethod(permission.getMethod());
        existingPermission.setModule(permission.getModule());
        // existingPermission.setModule(permission.getModule()); // Add module if needed

        Permission updatedPermission = permissionRepository.save(existingPermission);
        log.info("Permission updated successfully, permissionId={}", updatedPermission.getId());
        return updatedPermission;
    }

    public void deletePermission(long id) {
        log.info("Deleting permission with id={}", id);
        Permission permission = this.findPermissionById(id);

        // Check if any role is using this permission before deleting
        if (permission.getRoles() != null && !permission.getRoles().isEmpty()) {
            permission.getRoles().forEach(role-> role.getPermissions().remove(permission));
        }

        permissionRepository.delete(permission);
        log.info("Permission deleted successfully, permissionId={}", id);
    }
}