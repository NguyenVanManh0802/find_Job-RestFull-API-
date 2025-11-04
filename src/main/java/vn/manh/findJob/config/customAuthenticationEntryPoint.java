package vn.manh.findJob.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class customAuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPointBean() {
        return (request, response, authException) -> {
            // Đặt mã trạng thái HTTP là 401 Unauthorized
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            // Tạo một đối tượng Map để chứa thông tin lỗi
            Map<String, Object> body = new HashMap<>();
            body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
            body.put("error", "Unauthorized");
            body.put("message", "Xác thực không thành công. Vui lòng cung cấp token hợp lệ.");
            body.put("path", request.getServletPath());
            body.put("timestamp", new Date().getTime());

            // Dùng ObjectMapper đã được inject để chuyển Map thành chuỗi JSON
            objectMapper.writeValue(response.getOutputStream(), body);
        };
    }
}