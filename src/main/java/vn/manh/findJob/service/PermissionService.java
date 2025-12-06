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
import org.springframework.transaction.annotation.Transactional; // Import Transactional
import vn.manh.findJob.domain.Permission;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.exception.ResourceAlreadyExistsException;
import vn.manh.findJob.exception.ResourceNotFoundException;
import vn.manh.findJob.repository.PermissionRepository;
import vn.manh.findJob.repository.RoleRepository;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional // 1. Giữ Transaction cho toàn bộ class để Hibernate Session luôn mở
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    // --- HÀM HỖ TRỢ (QUAN TRỌNG NHẤT) ---
    // Tạo bản sao sạch sẽ (POJO) để đưa vào Redis
    private Permission sanitizePermission(Permission dbPermission) {
        Permission cleanPermission = new Permission();
        cleanPermission.setId(dbPermission.getId());
        cleanPermission.setName(dbPermission.getName());
        cleanPermission.setApiPath(dbPermission.getApiPath());
        cleanPermission.setMethod(dbPermission.getMethod());
        cleanPermission.setModule(dbPermission.getModule());
        cleanPermission.setCreatedAt(dbPermission.getCreatedAt());
        cleanPermission.setUpdatedAt(dbPermission.getUpdatedAt());
        cleanPermission.setCreatedBy(dbPermission.getCreatedBy());
        cleanPermission.setUpdatedBy(dbPermission.getUpdatedBy());

        // Với Permission, ta thường KHÔNG CẦN cache danh sách Roles đang sử dụng nó
        // (Vì list này rất dài và ít khi cần hiển thị chi tiết trong API get Permission)
        // Set null để cắt đứt mọi dây mơ rễ má với Hibernate
        cleanPermission.setRoles(null);

        return cleanPermission;
    }

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

    @Transactional(readOnly = true)
    public ResultPaginationDTO getAllPermissions(Specification<Permission> specification, Pageable pageable) {
        Page<Permission> pagePermission = permissionRepository.findAll(specification, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pagePermission.getTotalPages());
        meta.setTotal(pagePermission.getTotalElements());
        rs.setMeta(meta);

        rs.setResult(pagePermission.getContent());
        return rs;
    }

    // 2. Cacheable: Lấy từ Redis hoặc DB -> Sanitize -> Lưu Cache
    @Cacheable(value = "permissions", key = "#id")
    public Permission getPermissionById(long id) {
        log.info("Fetching permission with id={}", id);
        Permission dbPermission = this.findPermissionById(id);
        // Trả về object sạch
        return this.sanitizePermission(dbPermission);
    }

    // 3. CachePut: Update xong -> Sanitize -> Cập nhật Cache
    @CachePut(value = "permissions", key = "#id")
    public Permission updatePermission(long id, Permission permission) {
        log.info("Updating permission with id={}", id);
        Permission existingPermission = this.findPermissionById(id);

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

        Permission updatedPermission = permissionRepository.save(existingPermission);
        log.info("Permission updated successfully, permissionId={}", updatedPermission.getId());

        // Trả về object sạch
        return this.sanitizePermission(updatedPermission);
    }

    // 4. CacheEvict: Xóa -> Bay màu khỏi Cache
    @CacheEvict(value = "permissions", key = "#id")
    public void deletePermission(long id) {
        log.info("Deleting permission with id={}", id);
        Permission permission = this.findPermissionById(id);

        if (permission.getRoles() != null && !permission.getRoles().isEmpty()) {
            permission.getRoles().forEach(role-> role.getPermissions().remove(permission));
        }

        permissionRepository.delete(permission);
        log.info("Permission deleted successfully, permissionId={}", id);
    }
}