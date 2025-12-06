package vn.manh.findJob.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import vn.manh.findJob.domain.Company;
import vn.manh.findJob.domain.Job;
import vn.manh.findJob.domain.User;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.exception.ResourceNotFoundException;
import vn.manh.findJob.repository.CompanyRepository;
import vn.manh.findJob.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CompanyService companyService;

    // ==========================================
    // 1. TEST saveCompany
    // ==========================================
    @Test
    @DisplayName("saveCompany: Should return saved company")
    void saveCompany_Success() {
        // Arrange
        Company input = new Company();
        input.setName("FPT Software");

        Company saved = new Company();
        saved.setId(1L);
        saved.setName("FPT Software");

        when(companyRepository.save(input)).thenReturn(saved);

        // Act
        Company result = companyService.saveCompany(input);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("FPT Software", result.getName());
        verify(companyRepository).save(input);
    }

    // ==========================================
    // 2. TEST getCompanyById (Kiểm tra logic Sanitize)
    // ==========================================
    @Test
    @DisplayName("getCompanyById: Success & Must return Sanitized Object (Users/Jobs must be null)")
    void getCompanyById_Success_SanitizeCheck() {
        // --- ARRANGE ---
        long id = 1L;
        Company dbCompany = new Company();
        dbCompany.setId(id);
        dbCompany.setName("Viettel");

        // Giả lập DB trả về Company đang có kết nối với User và Job (Lazy Loading Risk)
        dbCompany.setUsers(List.of(new User()));
        dbCompany.setJobs(List.of(new Job()));

        when(companyRepository.findById(id)).thenReturn(Optional.of(dbCompany));

        // --- ACT ---
        Company result = companyService.getCompanyById(id);

        // --- ASSERT ---
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Viettel", result.getName());

        // QUAN TRỌNG: Kiểm tra xem logic sanitizeCompany có hoạt động không?
        // Result trả về phải cắt đứt quan hệ (null) để an toàn cho Redis
        assertNull(result.getUsers(), "Users list must be null after sanitization");
        assertNull(result.getJobs(), "Jobs list must be null after sanitization");

        // Đảm bảo object trả về là object mới (bản sao), không phải object gốc của DB
        assertNotSame(dbCompany, result);
    }

    @Test
    @DisplayName("getCompanyById: Throw Exception when ID not found")
    void getCompanyById_NotFound() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> companyService.getCompanyById(99L));
    }

    // ==========================================
    // 3. TEST getAllCompany (Pagination)
    // ==========================================
    @Test
    @DisplayName("getAllCompany: Should return correct pagination structure")
    void getAllCompany_Success() {
        // Arrange
        Company c1 = new Company(); c1.setName("A");
        List<Company> list = List.of(c1);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Company> pageResult = new PageImpl<>(list, pageable, 100);

        Specification<Company> spec = Mockito.mock(Specification.class);
        when(companyRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pageResult);

        // Act
        ResultPaginationDTO result = companyService.getAllCompany(spec, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getMeta().getPage()); // pageNumber + 1 logic
        assertEquals(10, result.getMeta().getPageSize());
        assertEquals(100, result.getMeta().getTotal());
    }

    // ==========================================
    // 4. TEST updateCompanyById (Update + Sanitize)
    // ==========================================
    @Test
    @DisplayName("updateCompanyById: Should update, save and return sanitized object")
    void updateCompanyById_Success() {
        // --- ARRANGE ---
        long id = 1L;
        // Data cũ trong DB
        Company existingCompany = new Company();
        existingCompany.setId(id);
        existingCompany.setName("Old Name");
        existingCompany.setUsers(new ArrayList<>()); // Đang có list user

        // Request update gửi lên
        Company updateRequest = new Company();
        updateRequest.setName("New Name");
        updateRequest.setAddress("Hanoi");

        // Mock tìm thấy
        when(companyRepository.findById(id)).thenReturn(Optional.of(existingCompany));

        // Mock save: Giả sử repo save xong trả về object đã update
        // Lưu ý: Spring Data JPA thường trả về chính instance đó hoặc bản copy
        when(companyRepository.save(existingCompany)).thenReturn(existingCompany);

        // --- ACT ---
        Company result = companyService.updateCompanyById(id, updateRequest);

        // --- ASSERT ---
        // 1. Kiểm tra trường dữ liệu update
        assertEquals("New Name", result.getName());
        assertEquals("Hanoi", result.getAddress());

        // 2. Kiểm tra Sanitize (Cắt User/Job)
        assertNull(result.getUsers(), "Users list must be null to safely put in Cache");

        // 3. Verify gọi repo
        verify(companyRepository).save(existingCompany);
    }

    @Test
    @DisplayName("updateCompanyById: Throw Exception if ID not found")
    void updateCompanyById_NotFound() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());
        Company req = new Company();

        assertThrows(ResourceNotFoundException.class, () -> companyService.updateCompanyById(99L, req));
        verify(companyRepository, never()).save(any());
    }

    // ==========================================
    // 5. TEST deleteCompanyById (Logic cascade delete)
    // ==========================================
    @Test
    @DisplayName("deleteCompanyById: Should delete Users first, then Company")
    void deleteCompanyById_Success() {
        // Arrange
        long companyId = 1L;
        Company company = new Company();
        company.setId(companyId);

        List<User> usersInCompany = List.of(new User(), new User());

        // Mock tìm thấy company
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        // Mock tìm thấy users của company
        when(userRepository.findByCompany(company)).thenReturn(usersInCompany);

        // Act
        companyService.deleteCompanyById(companyId);

        // Assert - Kiểm tra thứ tự gọi (InOrder)
        org.mockito.InOrder inOrder = Mockito.inOrder(userRepository, companyRepository);

        // 1. Tìm users
        inOrder.verify(userRepository).findByCompany(company);
        // 2. Xóa users (Quan trọng nhất)
        inOrder.verify(userRepository).deleteAll(usersInCompany);
        // 3. Xóa company
        inOrder.verify(companyRepository).deleteById(companyId);
    }

    @Test
    @DisplayName("deleteCompanyById: Throw Exception if Company not found")
    void deleteCompanyById_NotFound() {
        long companyId = 99L;
        // Mock không tìm thấy
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> companyService.deleteCompanyById(companyId));

        // Verify: Đảm bảo không có lệnh xóa nào được gọi
        verify(userRepository, never()).deleteAll(any());
        verify(companyRepository, never()).deleteById(anyLong());
    }
}