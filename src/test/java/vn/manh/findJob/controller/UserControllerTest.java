package vn.manh.findJob.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    @Test
    void createUser() {
    }

    @Test
    void getAllUsers() {
    }

    @Test
    void getUserById() {
    }

    @Test
    void updateUser() {
    }

    @Test
    void deleteUser() {
    }


//    package vn.manh.findJob.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//import vn.manh.findJob.domain.User;
//import vn.manh.findJob.repository.UserRepository;
//
//import static org.hamcrest.Matchers.is;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//    // 1. Khởi động toàn bộ ứng dụng (Spring Context)
//    @SpringBootTest
//// 2. Cấu hình MockMvc để giả lập gửi Request
//    @AutoConfigureMockMvc
//// 3. Tự động Rollback dữ liệu sau khi test xong (Rất quan trọng)
//    @Transactional
//// 4. (Tuỳ chọn) Dùng file application-test.properties nếu có cấu hình riêng
//// @ActiveProfiles("test")
//    class UserControllerIntegrationTest {
//
//        @Autowired
//        private MockMvc mockMvc; // Đây là "Postman" ảo
//
//        @Autowired
//        private ObjectMapper objectMapper; // Để chuyển Object -> JSON string
//
//        @Autowired
//        private UserRepository userRepository; // Inject Repo thật để check DB
//
//        @Test
//        @DisplayName("Create User: Success Flow (Controller -> DB)")
//        void createUser_Api_ShouldReturn201() throws Exception {
//            // --- ARRANGE ---
//            User request = new User();
//            request.setName("Integration Manh");
//            request.setEmail("integ@gmail.com");
//            request.setPassword("123456");
//
//            // --- ACT ---
//            // Giả lập gửi POST request
//            mockMvc.perform(post("/api/v1/users")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request))) // Convert user thành JSON
//
//                    // --- ASSERT (HTTP Response) ---
//                    .andExpect(status().isCreated()) // Mong đợi 201 Created
//                    .andExpect(jsonPath("$.data.name", is("Integration Manh"))) // Check JSON trả về
//                    .andExpect(jsonPath("$.data.email", is("integ@gmail.com")));
//
//            // --- ASSERT (Database State) ---
//            // Kiểm tra xem dữ liệu đã thực sự chui vào DB chưa?
//            // Đây là điều Unit Test không làm được!
//            boolean exists = userRepository.existsByEmail("integ@gmail.com");
//            assert(exists);
//        }
//    }
}