package vn.manh.findJob.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.manh.findJob.domain.Company;
import vn.manh.findJob.domain.Role;
import vn.manh.findJob.domain.User;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.dto.User.UserResponseDTO;
import vn.manh.findJob.exception.ResourceAlreadyExistsException;
import vn.manh.findJob.exception.ResourceNotFoundException;
import vn.manh.findJob.mapper.UserMapper;
import vn.manh.findJob.repository.CompanyRepository;
import vn.manh.findJob.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserService{
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CompanyRepository companyRepository;
    private final RoleService roleService;

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
            throw new ResourceAlreadyExistsException("Email " + request.getEmail() + " đã tồn tại.");
        }

        // 2. Cập nhật các trường cần thiết
        log.info("Updating user with id: {}", userId);
        existingUser.setName(request.getName());
        existingUser.setGender(request.getGender());
        existingUser.setAge(request.getAge());
        existingUser.setAddress(request.getAddress());
        existingUser.setEmail(request.getEmail());
        // Lưu ý: Không cập nhật password ở đây trừ khi có logic riêng về đổi mật khẩu

        //check company
        if(request.getCompany()!=null)
        {
            Optional<Company> companyOptional= companyRepository.findById(request.getCompany().getId());
            existingUser.setCompany(companyOptional.isPresent() ? companyOptional.get():null);
        }

        //check role
        if(request.getRole()!=null)
        {
            Role r= this.roleService.getRoleById(request.getRole().getId());
            request.setRole(r!=null ? r: null);
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
}
