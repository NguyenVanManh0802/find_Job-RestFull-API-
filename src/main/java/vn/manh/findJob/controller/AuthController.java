package vn.manh.findJob.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.manh.findJob.domain.User;
import vn.manh.findJob.dto.Auth.*;
import vn.manh.findJob.dto.ResponseData;
import vn.manh.findJob.dto.User.UserResponseDTO;
import vn.manh.findJob.exception.ResourceNotFoundException;
import vn.manh.findJob.service.SecurityUtil;
import vn.manh.findJob.service.UserService;


@RestController
@RequestMapping("/api/v1")
@Validated
@RequiredArgsConstructor   // annotaion tự động tạo constructor cho các trường có final key
public class AuthController {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    @Value("${hoidanit.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    @PostMapping("/auth/login")
    public ResponseEntity<ResponseData<ResLoginDTO>> login(@RequestBody @Valid LoginDTO loginDTO) {
        // 1. Nạp input vào Security
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());

        // 2. Xác thực người dùng
        // AuthenticationManager sẽ gọi UserDetailsService để check user/pass và trạng thái active (nếu config đúng)
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. Set thông tin vào Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 4. Tối ưu: Lấy thông tin User từ chính đối tượng Authentication (Tránh query DB lần 2)
        // Lưu ý: Cần đảm bảo UserDetails của bạn có chứa thông tin User entity hoặc email để query nhẹ
        // Ở đây tôi giữ cách query DB của bạn để đảm bảo code không bị lỗi do thiếu context về UserDetails,
        // nhưng hãy nhớ bước authenticate ở trên đã load user rồi.
        User userCurrent = userService.handleGetUserByUserName(loginDTO.getUsername());

        // --- KIỂM TRA ACTIVE (Nên để Spring Security lo, nhưng viết tay ở đây cũng được) ---
        if (userCurrent != null && !userCurrent.isActive()) {
            throw new ResourceNotFoundException("Tài khoản chưa được kích hoạt/bị khóa.");
        }

        // 5. Chuẩn bị DTO trả về (Access Token + User Info)
        ResLoginDTO res = new ResLoginDTO();
        ResLoginDTO.UserRefreshToken userLogin = new ResLoginDTO.UserRefreshToken(); // Sửa lại theo DTO mới nhất của bạn

        if (userCurrent != null) {
            userLogin.setId(userCurrent.getId());
            userLogin.setEmail(userCurrent.getEmail());
            userLogin.setName(userCurrent.getName());

            // Map Role an toàn (như đã bàn ở các bước trước)
            if (userCurrent.getRole() != null) {
                userLogin.setRole(userCurrent.getRole());
            }
            res.setUser(userLogin);
        }

        // 6. Create Access Token (Token ngắn hạn - trả về Body)
        String accessToken = this.securityUtil.createAccessToken(userCurrent.getEmail(), res);
        res.setAccessToken(accessToken);

        // 7. Create Refresh Token (Token dài hạn - lưu vào Cookie)
        String refreshToken = this.securityUtil.createRefreshToken(userCurrent.getEmail(), res);

        // 8. Update Refresh Token vào DB
        this.userService.UpdateUserToken(refreshToken, userCurrent.getEmail());

        // 9. --- CẢI TIẾN QUAN TRỌNG: SET COOKIE ---
        // Tạo HttpOnly Cookie để chứa Refresh Token
        ResponseCookie resCookies = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)     // JS không đọc được (Chống XSS)
                .secure(true)       // Chỉ gửi qua HTTPS (Production bật cái này)
                .path("/")          // Cookie có hiệu lực toàn domain
                .maxAge(7 * 24 * 60 * 60) // Thời gian sống (ví dụ 7 ngày, khớp với hạn token)
                .sameSite("Strict") // Chống CSRF
                .build();

        // 10. Trả về
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString()) // Đính kèm Cookie vào Header
                .body(new ResponseData<>(
                        HttpStatus.OK.value(),
                        "Đăng nhập thành công",
                        res
                ));
    }

    @GetMapping("/auth/account")
    public ResponseEntity<ResponseData<ResLoginDTO.UserGetAccount>> getAccount()
    {
        String email=SecurityUtil.getCurrentUserLogin().isPresent()? SecurityUtil.getCurrentUserLogin().get():"";
        User userCurrent=userService.handleGetUserByUserName(email);
        ResLoginDTO.UserRefreshToken res=new ResLoginDTO.UserRefreshToken();
        ResLoginDTO.UserGetAccount userGetAccount=new ResLoginDTO.UserGetAccount();
        if(userCurrent!=null)
        {
            res.setName(userCurrent.getName());
            res.setEmail(userCurrent.getEmail());
            res.setId(userCurrent.getId());
            res.setRole(userCurrent.getRole());
            userGetAccount.setUser(res);
        }
        ResponseData<ResLoginDTO.UserGetAccount> responseData=new ResponseData<>(
                HttpStatus.OK.value(),
                "get account successful",
                userGetAccount
        );
        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/auth/refresh")
    public ResponseEntity<ResponseData<ResLoginDTO>> getRefreshToken( @CookieValue(name="refresh_token",defaultValue = "abc")String refresh_token){
        //check valid refresh token
        Jwt decodedToken=this.securityUtil.checkValidRefreshToken(refresh_token);
        String email= decodedToken.getSubject();
        //check user by token + email
        User currentUser=this.userService.getUserByRefreshTokenAndEmail(refresh_token,email);
        if(currentUser==null)
        {
            throw new ResourceNotFoundException("Refresh Token khong hop le");
        }


        //issue new token/set refresh token as cookies

        User userCurrent=userService.handleGetUserByUserName(email);
        ResLoginDTO res=new ResLoginDTO();
        ResLoginDTO.UserRefreshToken userRefreshToken=new ResLoginDTO.UserRefreshToken();
        if(userCurrent!=null)
        {
            userRefreshToken.setName(userCurrent.getName());
            userRefreshToken.setEmail(userCurrent.getEmail());
            userRefreshToken.setId(userCurrent.getId());
            userRefreshToken.setRole(userCurrent.getRole());
            res.setUser(userRefreshToken);
        }
        String accessToken=this.securityUtil.createAccessToken(email,res);
        res.setAccessToken(accessToken);
        //create refresh token
        String new_refreshToken=this.securityUtil.createRefreshToken(email,res);
        ResponseData<ResLoginDTO> responseData = new ResponseData<>(
                HttpStatus.OK.value(),
                "create token successful",
                res

        );

        //update user
        userService.UpdateUserToken(new_refreshToken,email);

        ResponseCookie responseCookie=ResponseCookie
                .from("refresh_token",new_refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,responseCookie.toString())
                .body(responseData);
    }

    //api login bằng gg
    @PostMapping("/auth/login-google")
    public ResponseEntity<ResponseData<ResLoginDTO>> loginGoogle(@RequestBody GoogleLoginRequest request) throws Exception {

        // 1. Gọi logic xử lý từ Service
        ResLoginDTO res = this.userService.handleLoginGoogle(request.getIdToken());

        // 2. Tạo HttpOnly Cookie cho Refresh Token
        // Mục đích: Bảo mật, tránh bị Javascript ở Client đọc được (chống XSS)
        ResponseCookie resCookie = ResponseCookie.from("refresh_token", res.getRefreshToken())
                .httpOnly(true)
                .secure(true) // Đặt true nếu chạy trên HTTPS (Production)
                .path("/")
                .maxAge(86400)
                .sameSite("Strict") // Hoặc "Lax" tùy domain frontend
                .build();

        ResponseData<ResLoginDTO>responseData= new ResponseData<>(
                HttpStatus.OK.value(),
                "login successful",
                res
        );
        // 3. Trả về Access Token trong Body và Refresh Token trong Header Set-Cookie
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookie.toString())
                .body(responseData);
    }

    //api log out user
    @PostMapping("/auth/logout")
    public ResponseEntity<ResponseData<String>>logout()
    {
        String email=SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : " ";
        if (email.equals(""))
        {
            throw new ResourceNotFoundException("user not found");
        }
        this.userService.UpdateUserToken(null,email);


        ResponseData<String> response=new ResponseData<>(
                HttpStatus.OK.value(),
                "logout user sucessful"
            );
        //remove refreshtoken
        ResponseCookie deleteSpringCookie=ResponseCookie
                .from("refresh_token",null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,deleteSpringCookie.toString())
                .body(response);

    }

    @PostMapping("/auth/register")
    public ResponseEntity<ResponseData<UserResponseDTO>>register(@Valid @RequestBody ReqRegisterDTO user)
    {
        UserResponseDTO  user1=userService.handleRegister(user);
        ResponseData<UserResponseDTO> responseData=new ResponseData<>(
                HttpStatus.CREATED.value(),
                "REGISTER USER SUCCESSFUL",
                user1

        );
        return ResponseEntity.ok(responseData);
    }
    @GetMapping("/auth/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token)  {
        this.userService.handleVerifyAccount(token);
        return ResponseEntity.ok("Xác thực tài khoản thành công! Bạn có thể đăng nhập ngay bây giờ.");
    }



    @PostMapping("/auth/forgot-password")
    public ResponseEntity<ResponseData<String>> forgotPassword(@Valid @RequestBody ReqForgotPasswordDTO req){
        this.userService.handleForgotPassword(req.getEmail());

        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Vui lòng kiểm tra email để đặt lại mật khẩu.",
                null
        ));
    }

    /**
     * API 2: Đặt lại mật khẩu
     * Input: Token (từ URL), Mật khẩu mới, Xác nhận mật khẩu
     * Output: Cập nhật mật khẩu trong DB
     */
    @PostMapping("/auth/reset-password")
    public ResponseEntity<ResponseData<String>> resetPassword(@Valid @RequestBody ReqResetPasswordDTO req) {
        this.userService.handleResetPassword(req);

        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Đặt lại mật khẩu thành công.",
                null
        ));
    }


}
