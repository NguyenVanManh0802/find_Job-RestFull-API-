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
import vn.manh.findJob.domain.Job;
import vn.manh.findJob.domain.Skill;
import vn.manh.findJob.domain.Subscriber;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.exception.ResourceAlreadyExistsException;
import vn.manh.findJob.exception.ResourceNotFoundException;
import vn.manh.findJob.repository.SkillRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillService skillService;

    // ==========================================
    // 1. TEST saveSkill
    // ==========================================
    @Test
    @DisplayName("saveSkill: Success")
    void saveSkill_Success() {
        // Arrange
        Skill req = new Skill();
        req.setName("JAVA");

        Skill saved = new Skill();
        saved.setId(1L);
        saved.setName("JAVA");

        // Mock: Tên chưa tồn tại
        when(skillRepository.existsByName("JAVA")).thenReturn(false);
        when(skillRepository.save(req)).thenReturn(saved);

        // Act
        Skill result = skillService.saveSkill(req);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(skillRepository).save(req);
    }

    @Test
    @DisplayName("saveSkill: Fail if Name exists")
    void saveSkill_Fail_DuplicateName() {
        Skill req = new Skill();
        req.setName("JAVA");

        // Mock: Tên đã tồn tại
        when(skillRepository.existsByName("JAVA")).thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> skillService.saveSkill(req));

        // Verify: Không bao giờ gọi hàm save
        verify(skillRepository, never()).save(any());
    }

    // ==========================================
    // 2. TEST getSkillById (Sanitize Check)
    // ==========================================
    @Test
    @DisplayName("getSkillById: Success & Must return SANITIZED object (No Jobs/Subscribers)")
    void getSkillById_Success_Sanitize() {
        // --- ARRANGE ---
        long id = 1L;
        Skill dbSkill = new Skill();
        dbSkill.setId(id);
        dbSkill.setName("SPRING");

        // Giả lập Skill từ DB đang dính líu đến Job và Subscriber (Lazy Loading potential)
        List<Job> jobs = new ArrayList<>(); jobs.add(new Job());
        dbSkill.setJobs(jobs);

        List<Subscriber> subs = new ArrayList<>(); subs.add(new Subscriber());
        dbSkill.setSubscribers(subs);

        when(skillRepository.findById(id)).thenReturn(Optional.of(dbSkill));

        // --- ACT ---
        Skill result = skillService.getSkillById(id);

        // --- ASSERT ---
        assertNotNull(result);
        assertEquals("SPRING", result.getName());

        // 1. Check Sanitize: Quan hệ phải bị cắt đứt (null) để an toàn cho Redis
        assertNull(result.getJobs(), "Jobs list must be null after sanitize");
        assertNull(result.getSubscribers(), "Subscribers list must be null after sanitize");

        // 2. Check Instance: Phải là object mới (new Skill()), không phải object gốc
        assertNotSame(dbSkill, result, "Result must be a new instance (sanitized copy)");
    }

    @Test
    @DisplayName("getSkillById: Not Found")
    void getSkillById_NotFound() {
        when(skillRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> skillService.getSkillById(99L));
    }

    // ==========================================
    // 3. TEST getAllSkill
    // ==========================================
    @Test
    @DisplayName("getAllSkill: Success")
    void getAllSkill_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Skill> page = new PageImpl<>(List.of(new Skill()), pageable, 1);
        Specification<Skill> spec = Mockito.mock(Specification.class);

        when(skillRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        ResultPaginationDTO result = skillService.getAllSkill(spec, pageable);

        assertNotNull(result);
        assertEquals(1, result.getMeta().getTotal());
    }

    // ==========================================
    // 4. TEST updateSkillById
    // ==========================================
    @Test
    @DisplayName("updateSkillById: Success update Name & Sanitize return")
    void updateSkillById_Success() {
        // Arrange
        long id = 1L;
        Skill existingSkill = new Skill();
        existingSkill.setId(id);
        existingSkill.setName("OLD_NAME");

        Skill updateReq = new Skill();
        updateReq.setName("NEW_NAME");

        when(skillRepository.findById(id)).thenReturn(Optional.of(existingSkill));
        // Mock check duplicate: Tên thay đổi và chưa tồn tại
        when(skillRepository.existsByName("NEW_NAME")).thenReturn(false);
        // Mock save
        when(skillRepository.save(existingSkill)).thenReturn(existingSkill);

        // Act
        Skill result = skillService.updateSkillById(id, updateReq);

        // Assert
        assertEquals("NEW_NAME", existingSkill.getName());

        // Verify Sanitize: Result trả về phải là object mới, sạch sẽ
        assertNotSame(existingSkill, result);
        assertEquals("NEW_NAME", result.getName());
    }

    @Test
    @DisplayName("updateSkillById: Fail if Name duplicate")
    void updateSkillById_Fail_Duplicate() {
        long id = 1L;
        Skill existingSkill = new Skill(); existingSkill.setId(id); existingSkill.setName("OLD");

        Skill req = new Skill(); req.setName("EXISTING"); // Trùng tên

        when(skillRepository.findById(id)).thenReturn(Optional.of(existingSkill));
        when(skillRepository.existsByName("EXISTING")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> skillService.updateSkillById(id, req));
        verify(skillRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateSkillById: Not Found")
    void updateSkillById_NotFound() {
        when(skillRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> skillService.updateSkillById(99L, new Skill()));
    }

    // ==========================================
    // 5. TEST deleteSkill (Logic remove relationships)
    // ==========================================
    @Test
    @DisplayName("deleteSkill: Should remove skill from Jobs/Subscribers then delete")
    void deleteSkill_Success() {
        // --- ARRANGE ---
        long id = 1L;
        Skill skillToDelete = new Skill();
        skillToDelete.setId(id);

        // 1. Giả lập Skill đang nằm trong 1 Job
        Job job = new Job();
        job.setId(10L);
        // Job có list skill (bao gồm skill sắp xóa)
        List<Skill> jobSkills = new ArrayList<>();
        jobSkills.add(skillToDelete);
        job.setSkills(jobSkills);

        // Gán ngược lại vào Skill (Bidirectional)
        List<Job> jobsContainingSkill = new ArrayList<>();
        jobsContainingSkill.add(job);
        skillToDelete.setJobs(jobsContainingSkill);

        // 2. Giả lập Skill đang nằm trong 1 Subscriber
        Subscriber sub = new Subscriber();
        List<Skill> subSkills = new ArrayList<>();
        subSkills.add(skillToDelete);
        sub.setSkills(subSkills);

        List<Subscriber> subsContainingSkill = new ArrayList<>();
        subsContainingSkill.add(sub);
        skillToDelete.setSubscribers(subsContainingSkill);

        // Mock tìm thấy
        when(skillRepository.findById(id)).thenReturn(Optional.of(skillToDelete));

        // --- ACT ---
        skillService.deleteSkill(id);

        // --- ASSERT ---

        // 1. Kiểm tra logic In-Memory: Skill phải biến mất khỏi list của Job
        assertFalse(job.getSkills().contains(skillToDelete), "Skill must be removed from Job's skill list");
        assertTrue(job.getSkills().isEmpty());

        // 2. Kiểm tra logic In-Memory: Skill phải biến mất khỏi list của Subscriber
        assertFalse(sub.getSkills().contains(skillToDelete), "Skill must be removed from Subscriber's skill list");

        // 3. Kiểm tra lệnh xóa DB
        verify(skillRepository).delete(skillToDelete);
    }

    @Test
    @DisplayName("deleteSkill: Not Found")
    void deleteSkill_NotFound() {
        when(skillRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> skillService.deleteSkill(99L));

    }
}