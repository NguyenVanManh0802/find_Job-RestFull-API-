package vn.manh.findJob.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity // Bật tính năng bảo mật ở cấp độ phương thức (ví dụ: @PreAuthorize)
public class SecurityConfiguration {

    /**
     * Định nghĩa Bean PasswordEncoder.
     * Spring sẽ quản lý Bean này và bạn có thể tiêm (inject) nó vào bất cứ đâu
     * (ví dụ: trong UserService) để mã hóa mật khẩu.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Cấu hình chuỗi bộ lọc bảo mật (Security Filter Chain).
     * Đây là nơi bạn định nghĩa các quy tắc bảo mật chính.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Vô hiệu hóa CSRF (Cross-Site Request Forgery)
                // Vì chúng ta đang xây dựng API stateless (phi trạng thái) dùng token,
                // nên không cần cơ chế bảo vệ CSRF dựa trên session.
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Vô hiệu hóa Form Login
                // Chúng ta không dùng trang đăng nhập do Spring tự tạo, mà sẽ tự xây dựng API /login
                .formLogin(AbstractHttpConfigurer::disable)

                // 3. Cấu hình Session
                // API của chúng ta sẽ là STATELESS (phi trạng thái),
                // server không lưu trữ bất kỳ thông tin session nào của người dùng.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Phân quyền cho các HTTP Request
                .authorizeHttpRequests(auth -> auth
                        // Cho phép tất cả mọi người truy cập các API liên quan đến xác thực
                        .requestMatchers("/api/v1/users/**","/api/v1/jobs/**", "/api/v1/companies/**").permitAll()

                        // (Ví dụ) Cho phép các API public khác nếu có (ví dụ: xem file)
                        .requestMatchers("/storage/**").permitAll()

                        // Đối với TẤT CẢ các request còn lại:
                        .anyRequest().authenticated() // Bắt buộc phải được xác thực (đăng nhập)
                );

        return http.build();
    }
}