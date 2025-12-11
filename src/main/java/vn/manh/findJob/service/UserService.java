package vn.manh.findJob.service;


import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import vn.manh.findJob.domain.Company;
import vn.manh.findJob.domain.Role;
import vn.manh.findJob.domain.User;
import vn.manh.findJob.dto.Auth.ReqChangePasswordDTO;
import vn.manh.findJob.dto.Auth.ReqRegisterDTO;
import vn.manh.findJob.dto.Auth.ResLoginDTO;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.dto.Auth.ReqResetPasswordDTO;
import vn.manh.findJob.dto.User.UserResponseDTO;
import vn.manh.findJob.exception.ResourceAlreadyExistsException;
import vn.manh.findJob.exception.ResourceNotFoundException;
import vn.manh.findJob.mapper.UserMapper;
import vn.manh.findJob.repository.CompanyRepository;
import vn.manh.findJob.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserService{
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CompanyRepository companyRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecurityUtil securityUtil;
    //tìm user theo id
    public User findUserById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new ResourceNotFoundException("User not found with id: " + id);
                });
    }


    public boolean isEmailExist(String email) {
        return true ? userRepository.findByEmail(email)!=null : false;
    }


    public UserResponseDTO saveUser(User request) {
        log.info("Saving new user with email: {}", request.getEmail());
        if (this.userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email " + request.getEmail() + " đã tồn tại.");
        }

        //check company existed ?
        if(request.getCompany()!=null)
        {
            Optional<Company> companyOptional= companyRepository.findById(request.getCompany().getId());
            request.setCompany(companyOptional.isPresent() ? companyOptional.get():null);
        }
        //check role
        if(request.getRole()!=null)
        {
            Role r= this.roleService.getRoleById(request.getRole().getId());
            request.setRole(r!=null ? r: null);
        }

        UserResponseDTO savedUser = userMapper.toUserResponseDTO(userRepository.save(request));
        log.info("User has been saved successfully, userId={}", savedUser.getId());
        return savedUser;
    }


    public UserResponseDTO updateUser(long userId, User request) {
        // 1. Tìm user có trong DB không, nếu không có thì báo lỗi
        User existingUser =this.findUserById(userId);

        //kiểm tra email đã có tồn tại trong database hay không
        if (this.userRepository.existsByEmail(request.getEmail())) {
            if(!existingUser.getEmail().equals(request.getEmail()) && existingUser.getEmail()!=request.getEmail() )
            {
                throw new ResourceAlreadyExistsException("Email " + request.getEmail() + " đã tồn tại.");
            }
        }
        // 2. Cập nhật các trường cần thiết
        log.info("Updating user with id: {}", userId);
        existingUser.setName(request.getName());
        existingUser.setGender(request.getGender());
        existingUser.setAge(request.getAge());
        existingUser.setAddress(request.getAddress());
        if(request.getEmail()!=null)
        {
            existingUser.setEmail(request.getEmail());
        }

        // Lưu ý: Không cập nhật password ở đây trừ khi có logic riêng về đổi mật khẩu

        //check company
        if(request.getCompany()!=null)
        {
            Optional<Company> companyOptional= companyRepository.findById(request.getCompany().getId());
            existingUser.setCompany(companyOptional.orElse(null));
        }
        else {
            // Nếu request không gửi company (hoặc gửi null), thì set null cho user
            existingUser.setCompany(null);
        }
        //check role
        if(request.getRole() != null && request.getRole().getId() > 0)
        {
            Role r= this.roleService.getRoleById(request.getRole().getId());
            existingUser.setRole(r!=null ? r: null);
        }
        // 3. Lưu lại vào DB
        UserResponseDTO userResponseDTO=userMapper.toUserResponseDTO(userRepository.save(existingUser));
        log.info("User with id: {} has been updated successfully.", userResponseDTO.getId());
        return userResponseDTO;
    }

    public void deleteUser(long userId) {
        // Kiểm tra sự tồn tại của user trước khi xóa để đảm bảo an toàn
        if (!userRepository.existsById(userId)) {
            log.warn("Attempted to delete non-existing user with id: {}", userId);
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        log.info("Deleting user with id: {}", userId);
        userRepository.deleteById(userId);
        log.info("User with id: {} has been deleted successfully.", userId);
    }


    public UserResponseDTO getUserById(long userId) {
        log.info("Fetching user with id: {}", userId);
        // Sử dụng Optional để xử lý trường hợp không tìm thấy user một cách an toàn
        User user=this.findUserById(userId);
        return userMapper.toUserResponseDTO(user);
    }
    //fitler data và phân trang ,sort theo tên

    public ResultPaginationDTO getUsers(Specification<User> spec, Pageable pageable) {
        Page<User> pageUser = userRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageUser.getPageable().getPageNumber() + 1);
        meta.setPageSize(pageUser.getSize());
        meta.setPages(pageUser.getTotalPages());
        meta.setTotal(pageUser.getTotalElements());

        rs.setMeta(meta);

        // Lấy danh sách User entity từ Page
        List<User> users = pageUser.getContent();

        // DÙNG STREAM API ĐỂ CHUYỂN ĐỔI DANH SÁCH
        List<UserResponseDTO> userResponseDTOs = users.stream()
                .map(userMapper::toUserResponseDTO) // Áp dụng hàm mapping cho từng user
                .collect(Collectors.toList());     // Gom kết quả lại thành một List mới

        // Gán danh sách DTO đã được chuyển đổi vào kết quả
        rs.setResult(userResponseDTOs);

        return rs;
    }


    public User  handleGetUserByUserName(String userName) {
        User user=userRepository.findByEmail(userName);
        if (user == null) {
            //Ném ra exception chuẩn của Spring Security
            throw new ResourceNotFoundException("User not found with email: " + userName);
        }
        return user;
    }

    public void UpdateUserToken(String token, String email) {
        User userCurrent=this.handleGetUserByUserName(email);
        if(userCurrent!=null)
        {
            userCurrent.setRefreshToken(token);
            userRepository.save(userCurrent);
        }
    }

    public User getUserByRefreshTokenAndEmail(String token,String email)
    {
        return userRepository.findByRefreshTokenAndEmail(token,email);
    }

    public UserResponseDTO handleRegister(ReqRegisterDTO req)   {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ResourceAlreadyExistsException("Email " + req.getEmail() + " đã tồn tại.");
        }

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setAge(req.getAge());
        user.setGender(req.getGender());
        user.setAddress(req.getAddress());
        Role userRole = this.roleService.handleGetRoleByName("USER");
        // 2. Gán vào user
        if (userRole != null) {
            user.setRole(userRole);
        }
        // QUAN TRỌNG: Mặc định chưa kích hoạt
        user.setActive(false);

        // Lưu vào DB
        User savedUser = userRepository.save(user);
        UserResponseDTO userResponseDTO=userMapper.toUserResponseDTO(savedUser);

        // Tạo JWT xác thực và gửi email
        String token = securityUtil.createEmailVerificationToken(savedUser.getEmail());
        emailService.sendVerificationEmail(savedUser.getName(), savedUser.getEmail(), token);
        return userResponseDTO;
    }

    // 2. Xử lý Xác Thực (Khi user click link)
    public void handleVerifyAccount(String token) {
        // Giải mã token (Nếu hết hạn hoặc sai format sẽ throw lỗi tại đây)
        Jwt decodedToken = securityUtil.checkValidToken(token);

        // Lấy email từ subject của token
        String email = decodedToken.getSubject();

        // Tìm user và kích hoạt
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("Tài khoản không tồn tại.");
        }
        if (user.isActive()) {
            throw new ResourceNotFoundException("Tài khoản đã được kích hoạt trước đó.");
        }

        user.setActive(true);
        userRepository.save(user);
    }

    //logic thay đổi password
    public void handleChangePassword(ReqChangePasswordDTO request){
        // 1. Lấy email user đang đăng nhập
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        System.out.println("Mật khẩu User nhập: [" + request.getCurrentPassword() + "]");
        if (email.equals("")) {
            throw new ResourceNotFoundException("Bạn chưa đăng nhập.");
        }

        User currentUser = this.userRepository.findByEmail(email);
        if (currentUser == null) {
            throw new ResourceNotFoundException("Người dùng không tồn tại.");
        }

        // 2. Kiểm tra mật khẩu cũ có đúng không
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new ResourceNotFoundException("Mật khẩu hiện tại không chính xác.");
        }

        // 3. (Tuỳ chọn) Kiểm tra xem newPassword có trùng confirmPassword không
        // Mặc dù frontend đã check, backend check lại cho chắc
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResourceNotFoundException("Mật khẩu xác nhận không khớp.");
        }

        // 4. Cập nhật mật khẩu mới (Nhớ encode)
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        System.out.println(">>> Đang lưu mật khẩu mới: " + passwordEncoder.encode(request.getNewPassword()));
        // 5. Lưu xuống DB
        this.userRepository.save(currentUser);
    }

    // 1. Xử lý Forgot Password
    public void handleForgotPassword(String email) {
        User user = this.userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("Email không tồn tại.");
        }

        // TẠO JWT THAY VÌ UUID
        String jwtToken = securityUtil.createPasswordResetToken(email);

        // Gửi email chứa JWT
        this.emailService.sendResetPasswordEmail(user.getEmail(), jwtToken);
    }

    // 2. Xử lý Reset Password
    public void handleResetPassword(ReqResetPasswordDTO request)  {
        // GIẢI MÃ JWT
        Jwt decodedToken;
        try {
            decodedToken = securityUtil.checkValidToken(request.getToken());
        } catch (Exception e) {
            throw new ResourceNotFoundException("Token không hợp lệ hoặc đã hết hạn.");
        }

        // Lấy email từ trong token
        String email = decodedToken.getSubject();

        // Tìm user để đổi pass
        User user = this.userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("Người dùng không tồn tại.");
        }

        // Cập nhật mật khẩu
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        this.userRepository.save(user);
    }



    public ResLoginDTO handleLoginGoogle(String idTokenString) throws Exception {
        // 1. Cấu hình Verifier
        //để chắc chắn token do Google cấp và dành cho Client ID của app mình (tránh giả mạo).
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                // Chỉ chấp nhận token được tạo cho Client ID của ứng dụng này
                .setAudience(Collections.singletonList("260514782848-0elml36gqbvutbmbbftnaes2ab48hsgh.apps.googleusercontent.com"))
                .build();

        // 2. Xác thực Token
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) {
            throw new ResourceNotFoundException("Token Google không hợp lệ!");
        }

        // 3. Lấy thông tin người dùng từ Token
        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = (String) payload.get("name");

        // 4. Kiểm tra User trong DB
        User currentUser = this.userRepository.findByEmail(email);

        // Nếu user chưa tồn tại -> TỰ ĐỘNG ĐĂNG KÝ
        if (currentUser == null) {
            currentUser = new User();
            currentUser.setEmail(email);
            currentUser.setName(name);
            // Đặt pass ngẫu nhiên vì họ login bằng Google
            currentUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            currentUser.setActive(true); // Active luôn
            Role userRole = this.roleService.handleGetRoleByName("USER");
            // 2. Gán vào user
            if (userRole != null) {
                currentUser.setRole(userRole);
            }

            this.userRepository.save(currentUser);
        }

        // 5. Tạo Access Token & Refresh Token (Logic giống hệt login thường)
        // (Bạn nên copy logic tạo ResLoginDTO từ hàm login thường xuống đây)
        ResLoginDTO res = new ResLoginDTO();
        ResLoginDTO.UserRefreshToken userLogin = new ResLoginDTO.UserRefreshToken();
        userLogin.setEmail( currentUser.getEmail());
        userLogin.setName(currentUser.getName());
        userLogin.setId(currentUser.getId());
        if (currentUser.getRole() != null) {
            // Nếu sau này user có role thì map vào
            userLogin.setRole(currentUser.getRole());
        } else {
            // Nếu không có role -> trả về null
            userLogin.setRole(null);
        }
        res.setUser(userLogin);

        // Tạo token JWT hệ thống
        String access_token = this.securityUtil.createAccessToken(currentUser.getEmail(), res);
        res.setAccessToken(access_token);

        // Cập nhật refresh token vào DB... (giống login thường)
        String refresh_token = this.securityUtil.createRefreshToken(email, res);
        this.UpdateUserToken(refresh_token, email);

        // Lưu refresh token vào res để controller set cookie
        // (Bạn có thể trả về object chứa cả refresh token để controller xử lý)
        res.setRefreshToken(refresh_token);

        return res;
    }
}
