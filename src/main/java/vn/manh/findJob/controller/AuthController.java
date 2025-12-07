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
import vn.manh.findJob.dto.Auth.LoginDTO;
import vn.manh.findJob.dto.Auth.ResLoginDTO;
import vn.manh.findJob.dto.ResponseData;
import vn.manh.findJob.dto.User.UserResponseDTO;
import vn.manh.findJob.exception.ResourceAlreadyExistsException;
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
    public ResponseEntity<ResponseData<ResLoginDTO>> Login(@RequestBody @Valid LoginDTO loginDTO){
        //Nạp input gồm username/password vào Security
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());
        //xác thực người dùng => cần viết hàm loadUserByUsername
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        //nếu authentication xác thực thành công thì nó không lưu mật khẩu mà chỉ lưu thông tin người dùng
        //create token
        //set thoong tin user co the dung sau nay
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User userCurrent=userService.handleGetUserByUserName(loginDTO.getUsername());
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
        String accessToken=this.securityUtil.createAccessToken(authentication.getName(), res);
        res.setAccessToken(accessToken);
        //create refresh token
        String refreshToken=this.securityUtil.createRefreshToken(loginDTO.getUsername(),res);
        ResponseData<ResLoginDTO> responseData = new ResponseData<>(
                HttpStatus.OK.value(),
                "create token successful",
                res

        );

        //update user
        userService.UpdateUserToken(refreshToken,loginDTO.getUsername());

        ResponseCookie responseCookie=ResponseCookie
                .from("refresh_token",refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,responseCookie.toString())
                .body(responseData);
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
        //check user by token +email
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
    public ResponseEntity<ResponseData<UserResponseDTO>>register(@Valid @RequestBody User user)
    {
        boolean  userExist=userService.isEmailExist(user.getEmail());
        if(userExist)
        {
            throw new ResourceAlreadyExistsException("Email : "+ user.getEmail());
        }
        String hashPassword = this.passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPassword);
        UserResponseDTO  user1=userService.saveUser(user);
        ResponseData<UserResponseDTO> responseData=new ResponseData<>(
                HttpStatus.CREATED.value(),
                "REGISTER USER SUCCESSFUL",
                user1

        );
        return ResponseEntity.ok(responseData);
    }

}
