package vn.manh.findJob.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import vn.manh.findJob.domain.Permission;
import vn.manh.findJob.domain.Role;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.exception.ResourceAlreadyExistsException;
import vn.manh.findJob.exception.ResourceNotFoundException;
import vn.manh.findJob.repository.PermissionRepository;
import vn.manh.findJob.repository.RoleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private PermissionService permissionService;

    // ==========================================
    // 1. TEST createPermission
    // ==========================================
    @Test
    @DisplayName("createPermission: Success")
    void createPermission_Success() {
        // Arrange
        Permission req = new Permission();
        req.setName("CREATE_USER");
        req.setApiPath("/api/users");
        req.setMethod("POST");

        Permission saved = new Permission();
        saved.setId(1L);
        saved.setName("CREATE_USER");

        // Mock Validation passes (không trùng)
        when(permissionRepository.existsByName(req.getName())).thenReturn(false);
        when(permissionRepository.existsByApiPathAndMethod(req.getApiPath(), req.getMethod())).thenReturn(false);
        when(permissionRepository.save(req)).thenReturn(saved);

        // Act
        Permission result = permissionService.createPermission(req);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(permissionRepository).save(req);
    }

    @Test
    @DisplayName("createPermission: Fail if Name exists")
    void createPermission_Fail_NameExists() {
        Permission req = new Permission();
        req.setName("EXIST_NAME");

        when(permissionRepository.existsByName(req.getName())).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> permissionService.createPermission(req));

        verify(permissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("createPermission: Fail if Path & Method exists")
    void createPermission_Fail_PathMethodExists() {
        Permission req = new Permission();
        req.setName("NEW_NAME");
        req.setApiPath("/api/users");
        req.setMethod("GET");

        when(permissionRepository.existsByName(req.getName())).thenReturn(false);
        when(permissionRepository.existsByApiPathAndMethod("/api/users", "GET")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> permissionService.createPermission(req));
    }

    // ==========================================
    // 2. TEST getPermissionById (Sanitize Check)
    // ==========================================
    @Test
    @DisplayName("getPermissionById: Must return sanitized object (Roles = null)")
    void getPermissionById_Success_Sanitize() {
        // Arrange
        long id = 1L;
        Permission dbPermission = new Permission();
        dbPermission.setId(id);
        dbPermission.setName("VIEW_USER");

        // Giả lập DB trả về permission có chứa danh sách Roles
        List<Role> roles = new ArrayList<>();
        roles.add(new Role());
        dbPermission.setRoles(roles);

        when(permissionRepository.findById(id)).thenReturn(Optional.of(dbPermission));

        // Act
        Permission result = permissionService.getPermissionById(id);

        // Assert
        assertNotNull(result);
        assertEquals("VIEW_USER", result.getName());

        // QUAN TRỌNG: Kiểm tra hàm sanitizePermission đã chạy chưa?
        assertNull(result.getRoles(), "Roles list must be null to be safe for Redis");

        // Đảm bảo object trả về không phải là object gốc (vì sanitize tạo object mới)
        assertNotSame(dbPermission, result);
    }

    @Test
    @DisplayName("getPermissionById: Not Found")
    void getPermissionById_NotFound() {
        when(permissionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> permissionService.getPermissionById(99L));
    }

    // ==========================================
    // 3. TEST getAllPermissions
    // ==========================================
    @Test
    @DisplayName("getAllPermissions: Success pagination")
    void getAllPermissions_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Permission> page = new PageImpl<>(List.of(new Permission()), pageable, 1);

        Specification<Permission> spec = Mockito.mock(Specification.class);

        when(permissionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        ResultPaginationDTO result = permissionService.getAllPermissions(spec, pageable);

        assertNotNull(result);
        assertEquals(1, result.getMeta().getTotal());
    }

    // ==========================================
    // 4. TEST updatePermission
    // ==========================================
    @Test
    @DisplayName("updatePermission: Success (No duplicate conflict)")
    void updatePermission_Success() {
        // Arrange
        long id = 1L;
        Permission existing = new Permission();
        existing.setId(id);
        existing.setName("OLD_NAME");
        existing.setApiPath("/old");
        existing.setMethod("GET");

        Permission updateReq = new Permission();
        updateReq.setName("NEW_NAME");
        updateReq.setApiPath("/new");
        updateReq.setMethod("POST");
        updateReq.setModule("USERS");

        when(permissionRepository.findById(id)).thenReturn(Optional.of(existing));
        // Mock check duplicate: Tên mới chưa có, Path mới chưa có
        when(permissionRepository.existsByName("NEW_NAME")).thenReturn(false);
        when(permissionRepository.existsByApiPathAndMethod("/new", "POST")).thenReturn(false);

        // Mock save
        when(permissionRepository.save(existing)).thenReturn(existing);

        // Act
        Permission result = permissionService.updatePermission(id, updateReq);

        // Assert
        assertEquals("NEW_NAME", result.getName());
        assertEquals("USERS", result.getModule());
        assertNull(result.getRoles()); // Sanitize check

        verify(permissionRepository).save(existing);
    }

    @Test
    @DisplayName("updatePermission: Fail if New Name exists")
    void updatePermission_Fail_DuplicateName() {
        long id = 1L;
        Permission existing = new Permission();
        existing.setId(id);
        existing.setName("OLD_NAME");

        Permission updateReq = new Permission();
        updateReq.setName("EXISTING_NAME"); // Trùng tên người khác

        when(permissionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(permissionRepository.existsByName("EXISTING_NAME")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> permissionService.updatePermission(id, updateReq));
    }

    @Test
    @DisplayName("updatePermission: Fail if New Path/Method exists")
    void updatePermission_Fail_DuplicatePathMethod() {
        long id = 1L;
        Permission existing = new Permission();
        existing.setId(id);
        existing.setName("OLD_NAME");
        existing.setApiPath("/old");
        existing.setMethod("GET");

        Permission updateReq = new Permission();
        updateReq.setName("OLD_NAME"); // Tên giữ nguyên (ko check trùng tên)
        updateReq.setApiPath("/duplicate"); // Đổi path -> Trùng
        updateReq.setMethod("GET");

        when(permissionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(permissionRepository.existsByApiPathAndMethod("/duplicate", "GET")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> permissionService.updatePermission(id, updateReq));
    }

    // ==========================================
    // 5. TEST deletePermission (Logic Role relation)
    // ==========================================
    @Test
    @DisplayName("deletePermission: Should remove permission from roles first")
    void deletePermission_Success() {
        // Arrange
        long id = 1L;
        Permission permission = new Permission();
        permission.setId(id);

        // Giả lập permission đang được gán cho 1 Role
        Role role = new Role();
        role.setId(10L);
        // Role chứa list permission (trong đó có permission này)
        List<Permission> rolePerms = new ArrayList<>();
        rolePerms.add(permission);
        role.setPermissions(rolePerms);

        // Permission cũng chứa list Roles (Bidirectional)
        List<Role> permRoles = new ArrayList<>();
        permRoles.add(role);
        permission.setRoles(permRoles);

        when(permissionRepository.findById(id)).thenReturn(Optional.of(permission));

        // Act
        permissionService.deletePermission(id);

        // Assert
        // 1. Kiểm tra xem permission đã bị xóa khỏi list của role chưa?
        // (Đây là logic: permission.getRoles().forEach...)
        assertFalse(role.getPermissions().contains(permission), "Permission must be removed from Role's list");

        // 2. Kiểm tra gọi repo delete
        verify(permissionRepository).delete(permission);
    }

    @Test
    @DisplayName("deletePermission: Not Found")
    void deletePermission_NotFound() {
        when(permissionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> permissionService.deletePermission(99L));

    }
}