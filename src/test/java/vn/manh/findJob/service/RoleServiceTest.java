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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private RoleService roleService;

    // ==========================================
    // 1. TEST createRole
    // ==========================================
    @Test
    @DisplayName("createRole: Success with Permissions mapping")
    void createRole_Success() {
        // Arrange
        Role req = new Role();
        req.setName("ADMIN");
        // Request có chứa list Permission (chỉ có ID)
        Permission pReq = new Permission(); pReq.setId(10L);
        req.setPermissions(List.of(pReq));

        // Mock DB
        Permission dbPerm = new Permission(); dbPerm.setId(10L); dbPerm.setName("FULL_ACCESS");
        Role savedRole = new Role(); savedRole.setId(1L); savedRole.setName("ADMIN");

        when(roleRepository.existsByName("ADMIN")).thenReturn(false);
        when(permissionRepository.findByIdIn(anyList())).thenReturn(List.of(dbPerm));
        when(roleRepository.save(req)).thenReturn(savedRole);

        // Act
        Role result = roleService.createRole(req);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());

        // Kiểm tra xem Permission thật từ DB đã được set vào Role chưa
        assertEquals(dbPerm, req.getPermissions().get(0));

        verify(roleRepository).save(req);
    }

    @Test
    @DisplayName("createRole: Fail if Name exists")
    void createRole_Fail_DuplicateName() {
        Role req = new Role(); req.setName("USER");
        when(roleRepository.existsByName("USER")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> roleService.createRole(req));

        verify(roleRepository, never()).save(any());
    }

    // ==========================================
    // 2. TEST getRoleById (Sanitize & Logic Check)
    // ==========================================
    @Test
    @DisplayName("getRoleById: Success & Must return SANITIZED object")
    void getRoleById_Success() {
        // Arrange
        long id = 1L;
        Role dbRole = new Role();
        dbRole.setId(id);
        dbRole.setName("HR");

        // Giả lập role từ DB có chứa Permission list
        List<Permission> permList = new ArrayList<>();
        permList.add(new Permission());
        dbRole.setPermissions(permList);

        // Lưu ý: Mock hàm findByIdWithDetails (custom query của bạn)
        when(roleRepository.findByIdWithPermissions(id)).thenReturn(Optional.of(dbRole));

        // Act
        Role result = roleService.getRoleById(id);

        // Assert
        assertNotNull(result);
        assertEquals("HR", result.getName());

        // --- SENIOR CHECK: SANITIZE LOGIC ---
        // 1. Object trả về KHÔNG ĐƯỢC là object gốc (vì sanitize tạo new Role())
        assertNotSame(dbRole, result, "Result must be a new instance (sanitized)");

        // 2. List permissions phải được copy sang (không null, size = 1)
        assertNotNull(result.getPermissions());
        assertEquals(1, result.getPermissions().size());

        // 3. List permissions phải là ArrayList (do logic: new ArrayList<>(...))
        assertTrue(result.getPermissions() instanceof ArrayList);
    }

    @Test
    @DisplayName("getRoleById: Not Found")
    void getRoleById_NotFound() {
        when(roleRepository.findByIdWithPermissions(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> roleService.getRoleById(99L));
    }

    // ==========================================
    // 3. TEST getAllRoles
    // ==========================================
    @Test
    @DisplayName("getAllRoles: Success")
    void getAllRoles_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Role> page = new PageImpl<>(List.of(new Role()), pageable, 1);

        Specification<Role> spec = Mockito.mock(Specification.class);
        when(roleRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        ResultPaginationDTO result = roleService.getAllRoles(spec, pageable);

        assertNotNull(result);
        assertEquals(1, result.getMeta().getTotal());
    }

    // ==========================================
    // 4. TEST updateRole
    // ==========================================
    @Test
    @DisplayName("updateRole: Success update fields & permissions")
    void updateRole_Success() {
        // Arrange
        long id = 1L;
        Role existingRole = new Role();
        existingRole.setId(id);
        existingRole.setName("OLD_NAME");
        existingRole.setPermissions(new ArrayList<>()); // List cũ rỗng

        Role updateReq = new Role();
        updateReq.setName("NEW_NAME");
        updateReq.setDescription("New Desc");
        updateReq.setActive(true);

        // Request update permission
        Permission pReq = new Permission(); pReq.setId(99L);
        updateReq.setPermissions(List.of(pReq));

        Permission dbPerm = new Permission(); dbPerm.setId(99L); dbPerm.setName("NEW_PERM");

        // Mock Behaviors
        when(roleRepository.findByIdWithPermissions(id)).thenReturn(Optional.of(existingRole));
        // Mock check name: tên mới khác tên cũ, và tên mới chưa tồn tại -> OK
        when(roleRepository.existsByName("NEW_NAME")).thenReturn(false);

        when(permissionRepository.findByIdIn(anyList())).thenReturn(List.of(dbPerm));
        // Save trả về chính object đã sửa
        when(roleRepository.save(existingRole)).thenReturn(existingRole);

        // Act
        Role result = roleService.updateRole(id, updateReq);

        // Assert
        // 1. Check existingRole đã được update field chưa
        assertEquals("NEW_NAME", existingRole.getName());
        assertEquals("New Desc", existingRole.getDescription());

        // 2. Check Permission đã được fetch từ DB và set vào chưa
        assertEquals(dbPerm, existingRole.getPermissions().get(0));

        // 3. Check kết quả trả về có phải là Sanitized copy không
        assertNotSame(existingRole, result, "Update must return a sanitized copy");
        assertEquals("NEW_NAME", result.getName());
    }

    @Test
    @DisplayName("updateRole: Fail if Name exists")
    void updateRole_Fail_DuplicateName() {
        long id = 1L;
        Role existingRole = new Role(); existingRole.setId(id); existingRole.setName("OLD");

        Role req = new Role(); req.setName("EXISTING"); // Tên mới bị trùng

        when(roleRepository.findByIdWithPermissions(id)).thenReturn(Optional.of(existingRole));
        when(roleRepository.existsByName("EXISTING")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> roleService.updateRole(id, req));

        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateRole: Success remove all permissions (input null)")
    void updateRole_RemovePermissions() {
        long id = 1L;
        Role existing = new Role(); existing.setId(id); existing.setName("A");

        Role req = new Role(); req.setName("A");
        req.setPermissions(null); // Gửi null để xóa hết quyền

        when(roleRepository.findByIdWithPermissions(id)).thenReturn(Optional.of(existing));
        when(roleRepository.save(existing)).thenReturn(existing);

        roleService.updateRole(id, req);

        // Verify setPermissions(null) được gọi
        assertNull(existing.getPermissions());
    }

    // ==========================================
    // 5. TEST deleteRole
    // ==========================================
    @Test
    @DisplayName("deleteRole: Success")
    void deleteRole_Success() {
        Role role = new Role(); role.setId(1L);
        // Code của bạn dùng findRoleById (tức là findByIdWithPermissions)
        when(roleRepository.findByIdWithPermissions(1L)).thenReturn(Optional.of(role));

        roleService.deleteRole(1L);

        verify(roleRepository).delete(role);
    }

    @Test
    @DisplayName("deleteRole: Not Found")
    void deleteRole_NotFound() {
        when(roleRepository.findByIdWithPermissions(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> roleService.deleteRole(1L));
    }
}