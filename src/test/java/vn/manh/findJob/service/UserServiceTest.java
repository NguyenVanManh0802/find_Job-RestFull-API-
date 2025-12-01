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
import vn.manh.findJob.domain.Company;
import vn.manh.findJob.domain.Role;
import vn.manh.findJob.domain.User;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.dto.User.UserResponseDTO;
import vn.manh.findJob.exception.ResourceAlreadyExistsException;
import vn.manh.findJob.exception.ResourceNotFoundException;
import vn.manh.findJob.mapper.UserMapper;
import vn.manh.findJob.repository.CompanyRepository;
import vn.manh.findJob.repository.UserRepository;


import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


//@Test: Đánh dấu một phương thức là test case.
//@BeforeEach: Chạy trước mỗi test (khởi tạo dữ liệu).
//@AfterEach: Chạy sau mỗi test (giải phóng tài nguyên).
//@SpringBootTest: Khởi chạy Spring context (cho test tích hợp).
//@Mock: Tạo mock object.
//@InjectMocks: Tự động inject mock vào đối tượng cần test.
//@DisplayName: Đặt tên dễ đọc cho test case.

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private UserService userService;

    @Test
    void saveUser_ShouldReturnDTO_WhenValidRequest() {
        // ==========================================
        // BƯỚC 1: ARRANGE (Given - Chuẩn bị bối cảnh)
        // ==========================================
        // 1.1. Chuẩn bị dữ liệu đầu vào (Input)
//        User request = new User();
//        request.setEmail("test@gmail.com");
//        request.setName("New User");
//
//        // 1.2. Chuẩn bị dữ liệu giả sẽ được trả về từ các Mock
//        User savedEntity = new User();
//        savedEntity.setId(10L); // Giả vờ DB đã sinh ID là 10
//
//        UserResponseDTO expectedResponse = new UserResponseDTO();
//        expectedResponse.setId(10L);
//
//        // 1.3. Dạy cho Mockito cách ứng xử (Stubbing)
        //giả lập hành vi cho repository để mà lưu dl đúng theo nó làm
//        // "Nếu ai hỏi email tồn tại chưa -> Trả về false"
//        Mockito.when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
//        // "Nếu ai gọi hàm save -> Trả về savedEntity"
//        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(savedEntity);
//        // "Nếu ai gọi mapper -> Trả về DTO"
//        Mockito.when(userMapper.toUserResponseDTO(savedEntity)).thenReturn(expectedResponse);
//
//        // ==========================================
//        // BƯỚC 2: ACT (When - Hành động)
//        // ==========================================
//        // Gọi hàm cần test. Lưu kết quả thực tế vào biến 'actualResult'
//        UserResponseDTO actualResult = userService.saveUser(request);
//
//        // ==========================================
//        // BƯỚC 3: ASSERT (Then - Kiểm tra kết quả)
//        // ==========================================
//        // 3.1. Kiểm tra giá trị trả về (State Verification)
//        Assertions.assertNotNull(actualResult); // Không được null
//        Assertions.assertEquals(10L, actualResult.getId()); // ID phải là 10
//
//        // 3.2. Kiểm tra hành vi (Behavior Verification) - Cực quan trọng trong Mocking
//        // "Đảm bảo hàm save của Repository phải được gọi đúng 1 lần"
//        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
    }


    // ==========================================
    // 1. TEST findUserById
    // ==========================================
    @Test
    @DisplayName("findUserById: Success")
    void findUserById_WhenIdExists_ShouldReturnUser() {
        //giả lập hành vi
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        //hành động
        User result = userService.findUserById(1L);
        //then kiểm tra kết quả
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("findUserById: Not Found Exception")
    void findUserById_WhenIdNotExist_ShouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findUserById(999L));
    }

    // ==========================================
    // 2. TEST isEmailExist
    // ==========================================
    @Test
    @DisplayName("isEmailExist: Return True")
    void isEmailExist_WhenExists_ShouldReturnTrue() {
        when(userRepository.findByEmail("exist@mail.com")).thenReturn(new User());
        assertTrue(userService.isEmailExist("exist@mail.com"));
    }

    @Test
    @DisplayName("isEmailExist: Return False")
    void isEmailExist_WhenNotExists_ShouldReturnFalse() {
        when(userRepository.findByEmail("new@mail.com")).thenReturn(null);
        assertFalse(userService.isEmailExist("new@mail.com"));
    }

    // ==========================================
    // 3. TEST saveUser
    // ==========================================
    @Test
    @DisplayName("saveUser: Fail if Email Exists")
    void saveUser_WhenEmailExists_ShouldThrowException() {
        User req = new User();
        req.setEmail("duplicate@mail.com");
        when(userRepository.existsByEmail(req.getEmail())).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> userService.saveUser(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveUser: Success with Company and Role")
    void saveUser_Success_WithCompanyAndRole() {
        // Given
        User req = new User();
        req.setEmail("new@mail.com");

        Company compReq = new Company(); compReq.setId(10L);
        req.setCompany(compReq);

        Role roleReq = new Role(); roleReq.setId(5L);
        req.setRole(roleReq);

        User savedUser = new User(); savedUser.setId(1L);
        UserResponseDTO resDto = new UserResponseDTO(); resDto.setId(1L);

        // Mocking
        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(companyRepository.findById(10L)).thenReturn(Optional.of(new Company()));
        when(roleService.getRoleById(5L)).thenReturn(new Role());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toUserResponseDTO(savedUser)).thenReturn(resDto);

        // When
        UserResponseDTO result = userService.saveUser(req);

        // Then
        assertNotNull(result);
        verify(companyRepository).findById(10L);
        verify(roleService).getRoleById(5L);
        verify(userRepository).save(req);
    }

    @Test
    @DisplayName("saveUser: Success without Company and Role (Null check)")
    void saveUser_Success_NoCompanyNoRole() {
        User req = new User();
        req.setEmail("simple@mail.com");
        // Company null, Role null

        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(req);
        when(userMapper.toUserResponseDTO(any())).thenReturn(new UserResponseDTO());

        userService.saveUser(req);

        verify(companyRepository, never()).findById(anyLong());
        verify(roleService, never()).getRoleById(anyLong());
    }

    // ==========================================
    // 4. TEST updateUser
    // ==========================================
    @Test
    @DisplayName("updateUser: Fail if User Not Found")
    void updateUser_UserNotFound_ShouldThrowException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        User req = new User();

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(99L, req));
    }

    @Test
    @DisplayName("updateUser: Fail if Email Exists")
    void updateUser_EmailExists_ShouldThrowException() {
        // Giả lập tìm thấy user cũ
        User existingUser = new User(); existingUser.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        // Giả lập email mới bị trùng
        User req = new User(); req.setEmail("exist@mail.com");
        when(userRepository.existsByEmail("exist@mail.com")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> userService.updateUser(1L, req));
    }

    @Test
    @DisplayName("updateUser: Success update fields")
    void updateUser_Success() {
        // Given
        long userId = 1L;
        User existingUser = new User(); existingUser.setId(userId);

        User req = new User();
        req.setName("Updated Name");
        req.setEmail("update@mail.com");
        req.setAge(25);
        req.setAddress("Hanoi");

        Company comp = new Company(); comp.setId(2L);
        req.setCompany(comp);

        // Mocking
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(companyRepository.findById(2L)).thenReturn(Optional.of(new Company()));

        // Mock save returning updated entity
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        UserResponseDTO resDto = new UserResponseDTO(); resDto.setName("Updated Name");
        when(userMapper.toUserResponseDTO(existingUser)).thenReturn(resDto);

        // When
        UserResponseDTO result = userService.updateUser(userId, req);

        // Then
        assertEquals("Updated Name", result.getName());
        assertEquals("Hanoi", existingUser.getAddress()); // Check side effect on entity
        verify(userRepository).save(existingUser);
    }

    // ==========================================
    // 5. TEST deleteUser
    // ==========================================
    @Test
    @DisplayName("deleteUser: Success")
    void deleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteUser: Fail if not found")
    void deleteUser_NotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1L));
        verify(userRepository, never()).deleteById(anyLong());
    }

    // ==========================================
    // 6. TEST getUserById (Wrapper)
    // ==========================================
    @Test
    @DisplayName("getUserById: Success mapping")
    void getUserById_Success() {
        User user = new User(); user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDTO(user)).thenReturn(new UserResponseDTO());

        UserResponseDTO res = userService.getUserById(1L);
        assertNotNull(res);
    }

    // ==========================================
    // 7. TEST getUsers (Pagination) - QUAN TRỌNG
    // ==========================================
    @Test
    @DisplayName("getUsers: Pagination and Mapping")
    void getUsers_Pagination_Success() {
        // 1. Setup Data giả lập cho Page
        User user1 = new User(); user1.setName("A");
        User user2 = new User(); user2.setName("B");
        List<User> userList = List.of(user1, user2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> pageResult = new PageImpl<>(userList, pageable, 2);

        // 2. Mocking
        // Lưu ý: Do Specification là interface và generic, ta dùng any()
        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pageResult);
        when(userMapper.toUserResponseDTO(any(User.class))).thenReturn(new UserResponseDTO());

        // 3. Execution
        Specification<User> spec = Mockito.mock(Specification.class);
        ResultPaginationDTO result = userService.getUsers(spec, pageable);

        // 4. Assertions
        assertNotNull(result);
        assertNotNull(result.getMeta());
        assertEquals(1, result.getMeta().getPage()); // Code bạn: getPageNumber() + 1
        assertEquals(2, result.getMeta().getTotal());

    }

    // ==========================================
    // 8. TEST handleGetUserByUserName
    // ==========================================
    @Test
    @DisplayName("handleGetUserByUserName: Success")
    void handleGetUserByUserName_Success() {
        User u = new User(); u.setEmail("a@a.com");
        when(userRepository.findByEmail("a@a.com")).thenReturn(u);
        User res = userService.handleGetUserByUserName("a@a.com");
        assertEquals("a@a.com", res.getEmail());
    }

    @Test
    @DisplayName("handleGetUserByUserName: Not Found Exception")
    void handleGetUserByUserName_NotFound() {
        when(userRepository.findByEmail("ghost@a.com")).thenReturn(null);
        assertThrows(ResourceNotFoundException.class,
                () -> userService.handleGetUserByUserName("ghost@a.com"));
    }

    // ==========================================
    // 9. TEST UpdateUserToken
    // ==========================================
    @Test
    @DisplayName("UpdateUserToken: Update success when user found")
    void updateUserToken_Success() {
        User u = new User(); u.setEmail("a@a.com");
        when(userRepository.findByEmail("a@a.com")).thenReturn(u);

        userService.UpdateUserToken("xyz-token", "a@a.com");

        assertEquals("xyz-token", u.getRefreshToken());
        verify(userRepository).save(u);
    }

    @Test
    @DisplayName("UpdateUserToken: Do nothing when user not found")
    void updateUserToken_UserNotFound_DoNothing() {
        when(userRepository.findByEmail("ghost@a.com")).thenReturn(null);
        // Đảm bảo không gọi save nếu không tìm thấy user
        assertThrows(ResourceNotFoundException.class,
                () -> userService.UpdateUserToken("token", "ghost@a.com"));
        verify(userRepository, never()).save(any());
    }

    // ==========================================
    // 10. TEST getUserByRefreshTokenAndEmail
    // ==========================================
    @Test
    @DisplayName("getUserByRefreshTokenAndEmail: Success")
    void getUserByRefreshTokenAndEmail_Success() {
        User u = new User();
        when(userRepository.findByRefreshTokenAndEmail("token", "mail")).thenReturn(u);

        User res = userService.getUserByRefreshTokenAndEmail("token", "mail");
        assertEquals(u, res);
    }
}