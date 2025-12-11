package vn.manh.findJob.dto.Auth;

import lombok.Getter;
import org.springframework.stereotype.Service;

@Getter
@Service
public class GoogleLoginRequest {
    private String idToken;



}