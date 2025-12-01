package vn.manh.findJob.dto.Auth;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {
    @NotBlank(message = "username không được để trống")
    private String Username;
    @NotBlank(message = "password không được để trống")
    private String password;
}
