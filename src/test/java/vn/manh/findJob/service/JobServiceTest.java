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
import vn.manh.findJob.domain.Skill;
import vn.manh.findJob.dto.JobDTO;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.exception.ResourceNotFoundException;
import vn.manh.findJob.mapper.JobMapper;
import vn.manh.findJob.repository.CompanyRepository;
import vn.manh.findJob.repository.JobRepository;
import vn.manh.findJob.repository.SkillRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private JobMapper jobMapper;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private JobService jobService;

    // ==========================================
    // 1. TEST findJobById (Hàm nền tảng)
    // ==========================================
    @Test
    @DisplayName("findJobById: Success when ID exists")
    void findJobById_Success() {
        Job job = new Job();
        job.setId(1L);

        // Lưu ý: Code của bạn dùng findByIdWithDetails, nên phải mock chính xác hàm này
        when(jobRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(job));

        Job result = jobService.findJobById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("findJobById: Throw Exception when ID not found")
    void findJobById_NotFound() {
        when(jobRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> jobService.findJobById(99L));
    }

    // ==========================================
    // 2. TEST saveJob
    // ==========================================
    @Test
    @DisplayName("saveJob: Success with Skills and Company mapping")
    void saveJob_Success() {
        // --- ARRANGE ---
        // Input request
        Job inputJob = new Job();
        inputJob.setName("Java Dev");

        // Input có chứa List Skill (chỉ có ID)
        Skill sInput = new Skill(); sInput.setId(10L);
        inputJob.setSkills(List.of(sInput));

        // Input có Company (chỉ có ID)
        Company cInput = new Company(); cInput.setId(5L);
        inputJob.setCompany(cInput);

        // Mock DB Data
        Skill dbSkill = new Skill(); dbSkill.setId(10L); dbSkill.setName("Spring");
        Company dbCompany = new Company(); dbCompany.setId(5L); dbCompany.setName("FPT");

        Job savedJob = new Job(); savedJob.setId(1L); savedJob.setName("Java Dev");

        // Mock Behaviors
        when(skillRepository.findByIdIn(anyList())).thenReturn(List.of(dbSkill));
        when(companyRepository.findById(5L)).thenReturn(Optional.of(dbCompany));
        when(jobRepository.save(inputJob)).thenReturn(savedJob);
        when(jobMapper.toJobDTO(savedJob)).thenReturn(new JobDTO());

        // --- ACT ---
        JobDTO result = jobService.saveJob(inputJob);

        // --- ASSERT ---
        assertNotNull(result);

        // Verify Logic quan trọng:
        // Job input phải được set lại skill thật từ DB (dbSkill) chứ không phải skill rỗng từ input
        assertEquals(dbSkill, inputJob.getSkills().get(0));
        // Job input phải được set lại company thật
        assertEquals(dbCompany, inputJob.getCompany());

        verify(jobRepository).save(inputJob);
    }

    @Test
    @DisplayName("saveJob: Success with minimal info (Null checks)")
    void saveJob_NoSkill_NoCompany() {
        Job inputJob = new Job(); // Skills null, Company null
        Job savedJob = new Job();

        when(jobRepository.save(inputJob)).thenReturn(savedJob);
        when(jobMapper.toJobDTO(savedJob)).thenReturn(new JobDTO());

        jobService.saveJob(inputJob);

        verify(skillRepository, never()).findByIdIn(anyList());
        verify(companyRepository, never()).findById(anyLong());
        verify(jobRepository).save(inputJob);
    }

    // ==========================================
    // 3. TEST getAllJob (Pagination)
    // ==========================================
    @Test
    @DisplayName("getAllJob: Should return ResultPaginationDTO with mapped DTOs")
    void getAllJob_Success() {
        // Arrange
        Job job = new Job();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Job> page = new PageImpl<>(List.of(job), pageable, 1);

        Specification<Job> spec = Mockito.mock(Specification.class);

        when(jobRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(jobMapper.toJobDTO(any())).thenReturn(new JobDTO());

        // Act
        ResultPaginationDTO result = jobService.getAllJob(spec, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getMeta().getTotal());
        assertEquals(1, ((List<?>) result.getResult()).size());
    }

    // ==========================================
    // 4. TEST getJobById (@Cacheable logic)
    // ==========================================
    @Test
    @DisplayName("getJobById: Should fetch via findByIdWithDetails and convert to DTO")
    void getJobById_Success() {
        // Arrange
        long id = 1L;
        Job job = new Job();
        job.setId(id);

        // Mock hàm gọi nội bộ: findJobById -> gọi repo.findByIdWithDetails
        when(jobRepository.findByIdWithDetails(id)).thenReturn(Optional.of(job));
        when(jobMapper.toJobDTO(job)).thenReturn(new JobDTO());

        // Act
        JobDTO result = jobService.getJobById(id);

        // Assert
        assertNotNull(result);
        verify(jobRepository).findByIdWithDetails(id);
        verify(jobMapper).toJobDTO(job);
    }

    // ==========================================
    // 5. TEST updateJobById (@CachePut logic)
    // ==========================================
    @Test
    @DisplayName("updateJobById: Should update fields, fetch relations and save")
    void updateJobById_Success() {
        // --- ARRANGE ---
        long id = 1L;

        // 1. Job cũ trong DB
        Job existingJob = new Job();
        existingJob.setId(id);
        existingJob.setName("Old Name");

        // 2. Request Update
        Job updateReq = new Job();
        updateReq.setName("New Name");


        // Request có update Skill
        Skill sReq = new Skill(); sReq.setId(99L);
        updateReq.setSkills(List.of(sReq));

        // Request có update Company
        Company cReq = new Company(); cReq.setId(88L);
        updateReq.setCompany(cReq);

        // 3. Mock Data DB trả về cho Skill và Company
        Skill dbSkill = new Skill(); dbSkill.setId(99L); dbSkill.setName("ReactJS");
        Company dbCompany = new Company(); dbCompany.setId(88L); dbCompany.setName("Viettel");

        // 4. Setup Mocking
        when(jobRepository.findByIdWithDetails(id)).thenReturn(Optional.of(existingJob));
        when(skillRepository.findByIdIn(anyList())).thenReturn(List.of(dbSkill));
        when(companyRepository.findById(88L)).thenReturn(Optional.of(dbCompany));

        // Mock save: trả về chính object đã update
        when(jobRepository.save(existingJob)).thenReturn(existingJob);
        when(jobMapper.toJobDTO(existingJob)).thenReturn(new JobDTO());

        // --- ACT ---
        JobDTO result = jobService.updateJobById(id, updateReq);

        // --- ASSERT ---
        // Kiểm tra các trường primitive
        assertEquals("New Name", existingJob.getName());
        assertEquals("Senior", existingJob.getLevel());

        // Kiểm tra Relations đã được cập nhật từ DB chưa
        assertEquals(dbSkill, existingJob.getSkills().get(0));
        assertEquals(dbCompany, existingJob.getCompany());

        verify(jobRepository).save(existingJob);
    }

    @Test
    @DisplayName("updateJobById: Throw Exception if Job not found")
    void updateJobById_NotFound() {
        when(jobRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> jobService.updateJobById(99L, new Job()));

        verify(jobRepository, never()).save(any());
    }

    // ==========================================
    // 6. TEST deleteJobById (@CacheEvict logic)
    // ==========================================
    @Test
    @DisplayName("deleteJobById: Should find first then delete")
    void deleteJobById_Success() {
        // Arrange
        long id = 1L;
        Job job = new Job(); job.setId(id);

        // Logic mới của bạn gọi findJobById (tức là findByIdWithDetails) để check tồn tại trước
        when(jobRepository.findByIdWithDetails(id)).thenReturn(Optional.of(job));

        // Act
        jobService.deleteJobById(id);

        // Assert
        verify(jobRepository).delete(job);
    }

    @Test
    @DisplayName("deleteJobById: Throw exception if not found")
    void deleteJobById_NotFound() {
        when(jobRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobService.deleteJobById(99L));


    }
}