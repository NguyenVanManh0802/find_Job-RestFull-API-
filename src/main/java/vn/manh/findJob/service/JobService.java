package vn.manh.findJob.service;

import com.turkraft.springfilter.boot.Filter;
import com.turkraft.springfilter.builder.FilterBuilder;
import com.turkraft.springfilter.converter.FilterSpecificationConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional
import vn.manh.findJob.domain.*;
import vn.manh.findJob.dto.JobDTO;
import vn.manh.findJob.dto.ResponseData;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.exception.ResourceNotFoundException;
import vn.manh.findJob.mapper.JobMapper;
import vn.manh.findJob.repository.*;

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
    private final UserRepository userRepository;
    private final SubscriberRepository subscriberRepository;
    private final EmailService emailService;

    // Hàm private hỗ trợ tìm kiếm (Luôn gọi DB)
    public Job findJobById(long id) {
        return jobRepository.findByIdWithDetails(id) // Dùng hàm mới có JOIN FETCH
                .orElseThrow(() -> {
                    log.warn("Job not found with id: {}", id);
                    return new ResourceNotFoundException("Job not found with id: " + id);
                });
    }

    public JobDTO saveJob(Job job) {
        // 1. Lấy thông tin người đang đăng nhập
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentUser = this.userRepository.findByEmail(email);

        if (currentUser != null) {
            // --- LOGIC TỰ GÁN COMPANY ---
            // Nếu người dùng thuộc 1 công ty (HR) -> Ép buộc Job thuộc công ty đó
            if (currentUser.getCompany() != null) {
                job.setCompany(currentUser.getCompany());
                System.out.println(">>>>>>>>>> " + job.getCompany().getId());
            }
            // Nếu người dùng không thuộc công ty (Admin) -> Giữ nguyên logic chọn company từ FE gửi lên
            else if (job.getCompany() != null) {
                Optional<Company> companyOptional = companyRepository.findById(job.getCompany().getId());
                companyOptional.ifPresent(job::setCompany);
            }
        }

        // 2. Xử lý Skill
        if (job.getSkills() != null) {
            List<Long> skillIds = job.getSkills()
                    .stream()
                    .map(Skill::getId)
                    .collect(Collectors.toList());
            job.setSkills(this.skillRepository.findByIdIn(skillIds));
        }

        // 3. --- CHẶN ACTIVE ---
        // Bất kể FE gửi lên active là true hay false, khi tạo mới luôn ép về FALSE
        job.setActive(false);
        // ---------------------

        Job newJob = jobRepository.save(job);
        return this.jobMapper.toJobDTO(newJob);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO getAllJob(Specification<Job> specification, Pageable pageable) {
        Page<Job> pageJob = jobRepository.findAll(specification, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        // FIX: +1 vào page index
        meta.setPage(pageable.getPageNumber() + 1);

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
        Job jobInDB = this.findJobById(id);

        if(job.getSkills() != null) {
            List<Long> skillIds = job.getSkills()
                    .stream()
                    .map(Skill::getId)
                    .collect(Collectors.toList());
            List<Skill> dbSkills = this.skillRepository.findByIdIn(skillIds);
            jobInDB.setSkills(dbSkills);
        }

        if(job.getCompany() != null) {
            Optional<Company> companyOptional = companyRepository.findById(job.getCompany().getId());
            if(companyOptional.isPresent()) {
                jobInDB.setCompany(companyOptional.get());
            }
        }

        log.info("update job by new job");
        jobInDB.setName(job.getName());
        jobInDB.setActive(job.isActive());
        jobInDB.setLevel(job.getLevel());
        jobInDB.setLocation(job.getLocation());
        jobInDB.setQuantity(job.getQuantity());
        jobInDB.setSalary(job.getSalary());
        jobInDB.setDescription(job.getDescription());

// --- BỔ SUNG QUAN TRỌNG: CHECK QUYỀN DUYỆT BÀI ---
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentUser = this.userRepository.findByEmail(email);

        if (currentUser != null) {
            // Nếu là Admin -> Cho phép đổi Company (nếu cần) và Đổi trạng thái Active
            if ("SUPER_ADMIN".equals(currentUser.getRole().getName())) {
                if (job.getCompany() != null) {
                    Optional<Company> companyOptional = companyRepository.findById(job.getCompany().getId());
                    companyOptional.ifPresent(jobInDB::setCompany);
                }
                // Admin được quyền duyệt bài
                jobInDB.setActive(job.isActive());
            }
            // Nếu là HR -> KHÔNG được đổi Company (giữ cũ) và KHÔNG được đổi Active
            else {
                // Giữ nguyên company cũ của Job
                jobInDB.setCompany(jobInDB.getCompany());

                // Giữ nguyên trạng thái active cũ (hoặc reset về false để duyệt lại)
                jobInDB.setActive(jobInDB.isActive());
            }
        }
        // Lưu vào DB
        Job savedJob = jobRepository.save(jobInDB);


        log.info("job save successful");
        // 3.---LOGIC GỬI EMAIL TỰ ĐỘNG---
        // Chỉ gửi khi: Trạng thái Cũ là FALSE (Chưa duyệt) VÀ Trạng thái Mới là TRUE (Đã duyệt)
        if (savedJob.isActive()) {
            this.sendEmailToSubscribers(savedJob);
        }
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
    // Viết hàm riêng cho gọn code
    private void sendEmailToSubscribers(Job job) {
        Company company = job.getCompany();
        if (company != null) {
            // Lấy danh sách người theo dõi công ty này
            List<Subscriber> subscribers = this.subscriberRepository.findByCompanies(company);

            // Link job chi tiết (Frontend)
            String jobLink = "http://localhost:3000/job/" + job.getId();

            for (Subscriber sub : subscribers) {
                this.emailService.sendEmailNewJobAlert(
                        sub.getEmail(),
                        sub.getName(),
                        job.getName(),
                        company.getName(),
                        jobLink,
                        job.getSalary()
                );
            }
        }
    }
}