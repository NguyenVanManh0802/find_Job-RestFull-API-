package vn.manh.findJob.mapper;

import org.mapstruct.Mapper;
import vn.manh.findJob.domain.User;
import vn.manh.findJob.dto.User.UserAuth;
import vn.manh.findJob.dto.User.UserResponseDTO;


@Mapper(componentModel = "spring") // Báo cho MapStruct tạo ra một Spring Bean
public interface UserMapper {

    /**
     * Chuyển đổi từ User Entity sang UserResponseDTO.
     * MapStruct sẽ tự động ánh xạ các trường có cùng tên.
     * Các trường như 'password' và 'refreshToken' trong User entity
     * sẽ được bỏ qua một cách an toàn vì chúng không tồn tại trong DTO.
     * * @param user Đối tượng User entity đầu vào.
     * @return Đối tượng UserResponseDTO đã được ánh xạ.
     */
    UserResponseDTO toUserResponseDTO(User user);
    UserAuth toUserAuth(User user);
}