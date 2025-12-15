package vn.manh.findJob.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional
import vn.manh.findJob.domain.Skill;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.exception.ResourceAlreadyExistsException;
import vn.manh.findJob.exception.ResourceNotFoundException;
import vn.manh.findJob.repository.SkillRepository;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional // 1. Giữ kết nối DB ổn định cho cả Class
public class SkillService {

    private final SkillRepository skillRepository;

    // --- HÀM HỖ TRỢ (QUAN TRỌNG) ---
    // Tạo bản sao sạch sẽ (POJO) để đưa vào Redis
    private Skill sanitizeSkill(Skill dbSkill) {
        Skill cleanSkill = new Skill();
        cleanSkill.setId(dbSkill.getId());
        cleanSkill.setName(dbSkill.getName());
        cleanSkill.setCreatedAt(dbSkill.getCreatedAt());
        cleanSkill.setUpdatedAt(dbSkill.getUpdatedAt());
        cleanSkill.setCreatedBy(dbSkill.getCreatedBy());
        cleanSkill.setUpdatedBy(dbSkill.getUpdatedBy());

        // QUAN TRỌNG: Cắt đứt quan hệ với Job và Subscriber
        // Khi Cache thông tin Skill (ví dụ: JAVA), ta không cần lưu kèm danh sách 1000 Jobs đang tuyển Java
        // Để null giúp Cache cực nhẹ và tránh lỗi Lazy/Vòng lặp
        cleanSkill.setJobs(null);
        return cleanSkill;
    }

    public Skill saveSkill(Skill skill) {
        if (this.skillRepository.existsByName(skill.getName())) {
            throw new ResourceAlreadyExistsException("Skill với tên '" + skill.getName() + "' đã tồn tại.");
        }

        Skill newSkill = skillRepository.save(skill);
        log.info("Skill đã được lưu thành công, skillName = {} ", newSkill.getName());
        return newSkill;
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO getAllSkill(Specification<Skill> specification, Pageable pageable) {
        Page<Skill> pageSkill = skillRepository.findAll(specification, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber());
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageSkill.getTotalPages());
        meta.setTotal(pageSkill.getTotalElements());
        rs.setMeta(meta);
        rs.setResult(pageSkill.getContent());
        return rs;
    }

    // 2. Cacheable: Lấy từ DB -> Sanitize -> Lưu Redis
    @Cacheable(value = "skills", key = "#id")
    public Skill getSkillById(long id) {
        log.info("Fetching Skill from Database with id ={} ", id);

        Skill dbSkill = skillRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("Skill not found with id={}", id);
                    return new ResourceNotFoundException("Skill not found with id: " + id);
                });

        // Trả về object sạch
        return this.sanitizeSkill(dbSkill);
    }

    // 3. CachePut: Update -> Sanitize -> Cập nhật Redis
    @CachePut(value = "skills", key = "#id")
    public Skill updateSkillById(long id, Skill skill) {
        Skill skillExisted = this.skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));

        if (skill.getName() != null && !skill.getName().equals(skillExisted.getName())) {
            if (this.skillRepository.existsByName(skill.getName())) {
                throw new ResourceAlreadyExistsException("Skill với tên '" + skill.getName() + "' đã tồn tại.");
            }
        }

        skillExisted.setName(skill.getName());
        Skill updatedSkill = skillRepository.save(skillExisted);

        log.info("Skill updated successfully");

        // Trả về object sạch
        return this.sanitizeSkill(updatedSkill);
    }

    // 4. CacheEvict: Xóa -> Xóa Redis
    @CacheEvict(value = "skills", key = "#id")
    public void deleteSkill(long id) {
        Optional<Skill> skillOptional = this.skillRepository.findById(id);
        if (skillOptional.isEmpty()) {
            throw new ResourceNotFoundException("Skill not found with id: " + id);
        }

        Skill currentSkill = skillOptional.get();
        // Xóa relationship (Logic cũ của bạn giữ nguyên là đúng)
        if(currentSkill.getJobs() != null) {
            currentSkill.getJobs().forEach(job -> job.getSkills().remove(currentSkill));
        }


        skillRepository.delete(currentSkill);
        log.info("Deleted skill and evicted from cache with id: {}", id);
    }
}