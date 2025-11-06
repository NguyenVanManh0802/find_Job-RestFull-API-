package vn.manh.findJob.controller;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import vn.manh.findJob.domain.User;
import vn.manh.findJob.dto.ResponseData;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.dto.User.UserResponseDTO;
import vn.manh.findJob.service.UserService;

import java.net.URI;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * API để tạo một người dùng mới.
     * Endpoint: POST /api/v1/users
     * Chuẩn: Trả về 201 Created, header Location, và body chuẩn hóa.
     */
    @PostMapping()
    public ResponseEntity<ResponseData<UserResponseDTO>> createUser(@Valid @RequestBody User user) {
        log.info("Request add user, {}",user.getName());
        String hashPassword = this.passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPassword);
        UserResponseDTO savedUser = userService.saveUser(user);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedUser.getId())
                .toUri();
        ResponseData<UserResponseDTO> responseData = new ResponseData<>(
                HttpStatus.CREATED.value(),
                "User created successfully",
                savedUser
        );
        return ResponseEntity.created(location).body(responseData);
    }

    /**
     * API để lấy tất cả người dùng.
     * Endpoint: GET /api/v1/users
     */
    @GetMapping()
    public ResponseEntity<ResponseData<ResultPaginationDTO>> getAllUsers(@Filter Specification<User> specification, Pageable pageable)
    {
//        List<User> users = userService.getUsers(specification);
//        log.info("Request get user list count {}",users.stream().count());
        ResultPaginationDTO resultPaginationDTO=userService.getUsers(specification,pageable);
        ResponseData<ResultPaginationDTO> responseData = new ResponseData<>(
                HttpStatus.OK.value(),
                "Fetch all users successfully",
                resultPaginationDTO
        );


        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    /**
     * API để lấy người dùng theo ID.
     * Endpoint: GET /api/v1/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<UserResponseDTO>> getUserById(@PathVariable @Min(1) long id) {
        UserResponseDTO userResponseDTO = userService.getUserById(id);
        ResponseData<UserResponseDTO> responseData = new ResponseData<>(
                HttpStatus.OK.value(),
                "Fetch user successfully",
                userResponseDTO
        );
        return ResponseEntity.ok(responseData);
    }

    /**
     * API để cập nhật người dùng.
     * Endpoint: PUT /api/v1/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<UserResponseDTO>> updateUser(
            @PathVariable @Min(1) long id,
            @Valid @RequestBody User user) {
        UserResponseDTO updatedUser = userService.updateUser(id, user);
        ResponseData<UserResponseDTO> responseData = new ResponseData<>(
                HttpStatus.OK.value(),
                "User updated successfully",
                updatedUser
        );
        return ResponseEntity.ok(responseData);
    }

    /**
     * API để xóa người dùng.
     * Endpoint: DELETE /api/v1/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteUser(@PathVariable @Min(1) long id) {
        userService.deleteUser(id);

        ResponseData<Void> responseData = new ResponseData<>(
                HttpStatus.OK.value(),
                "User deleted successfully"
        );

        // Trả về 200 OK cùng message xác nhận là một cách tiếp cận thực tế.
        return ResponseEntity.ok(responseData);
    }

}