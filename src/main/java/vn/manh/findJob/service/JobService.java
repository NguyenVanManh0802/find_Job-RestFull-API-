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
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional // 1. Giữ Transaction để Hibernate Session luôn mở
public class JobService {

    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;
    private final JobMapper jobMapper;
    private final CompanyRepository companyRepository;

    // Hàm private hỗ trợ tìm kiếm (Luôn gọi DB)
    public Job findJobById(long id) {
        return jobRepository.findByIdWithDetails(id) // Dùng hàm mới có JOIN FETCH
                .orElseThrow(() -> {
                    log.warn("Job not found with id: {}", id);
                    return new ResourceNotFoundException("Job not found with id: " + id);
                });
    }

    public JobDTO saveJob(Job job) {
        // BƯỚC 1: KIỂM TRA VÀ LẤY CÁC SKILL TỪ DATABASE
        if (job.getSkills() != null) {
            List<Long> skillIds = job.getSkills()
                    .stream()
                    .map(Skill::getId)
                    .collect(Collectors.toList());
            List<Skill> dbSkills = this.skillRepository.findByIdIn(skillIds);
            job.setSkills(dbSkills);
        }

        // check có company không
        if(job.getCompany() != null) {
            Optional<Company> companyOptional = companyRepository.findById(job.getCompany().getId());
            if(companyOptional.isPresent()) {
                job.setCompany(companyOptional.get());
            }
        }

        Job newJob = jobRepository.save(job);
        log.info("Job đã được lưu thành công, jobName = {} ", newJob.getName());

        // Convert sang DTO để trả về
        return this.jobMapper.toJobDTO(newJob);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO getAllJob(Specification<Job> specification, Pageable pageable) {
        Page<Job> pageJob = jobRepository.findAll(specification, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber());
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageJob.getTotalPages());
        meta.setTotal(pageJob.getTotalElements());
        rs.setMeta(meta);

        List<Job> jobs = pageJob.getContent();
        List<JobDTO> jobDTOList = jobs.stream()
                .map(jobMapper::toJobDTO)
                .collect(Collectors.toList());

        rs.setResult(jobDTOList);
        return rs;
    }

    // 2. Cacheable: Lấy JobDTO từ Redis (nếu có) hoặc từ DB
    // Vì JobDTO là POJO (Object thuần) nên Redis lưu thoải mái, không cần sanitize thủ công
    @Cacheable(value = "jobs", key = "#id")
    public JobDTO getJobById(long id) {
        log.info("Fetching job with id={}", id);

        // Gọi hàm findJobById (private) -> lấy Entity
        Job job = this.findJobById(id);

        // Convert Entity -> DTO (JobMapper sẽ làm sạch dữ liệu)
        return jobMapper.toJobDTO(job);
    }

    // 3. CachePut: Cập nhật DB xong -> Convert ra DTO -> Lưu đè vào Redis
    @CachePut(value = "jobs", key = "#id")
    public JobDTO updateJobById(long id, Job job) {
        // Lấy Job cũ từ DB (đã kèm Skill, Company nhờ hàm findByIdWithDetails)
        Job jobExisted = this.findJobById(id);

        if(job.getSkills() != null) {
            List<Long> skillIds = job.getSkills()
                    .stream()
                    .map(Skill::getId)
                    .collect(Collectors.toList());
            List<Skill> dbSkills = this.skillRepository.findByIdIn(skillIds);
            jobExisted.setSkills(dbSkills);
        }

        if(job.getCompany() != null) {
            Optional<Company> companyOptional = companyRepository.findById(job.getCompany().getId());
            if(companyOptional.isPresent()) {
                jobExisted.setCompany(companyOptional.get());
            }
        }

        log.info("update job by new job");
        jobExisted.setName(job.getName());
        jobExisted.setActive(job.isActive());
        jobExisted.setLevel(job.getLevel());
        jobExisted.setLocation(job.getLocation());
        jobExisted.setQuantity(job.getQuantity());
        jobExisted.setSalary(job.getSalary());
        jobExisted.setDescription(job.getDescription());
        // Company và Skills đã set ở trên

        // Lưu vào DB
        Job savedJob = jobRepository.save(jobExisted);

        log.info("job save successful");

        // Convert sang DTO để Redis lưu
        return jobMapper.toJobDTO(savedJob);
    }

    // 4. CacheEvict: Xóa Job -> Xóa cache
    @CacheEvict(value = "jobs", key = "#id")
    public void deleteJobById(long id) {
        log.info("delete job with id ={}", id);
        // Kiểm tra tồn tại trước khi xóa
        // Dùng findJobById để tận dụng luôn exception nếu không tìm thấy
        Job job = this.findJobById(id);

        jobRepository.delete(job);
        log.info("delete job successful");
    }
}