package vn.manh.findJob.service;


import com.turkraft.springfilter.converter.FilterSpecification;
import com.turkraft.springfilter.converter.FilterSpecificationConverter;
import com.turkraft.springfilter.parser.FilterParser;
import com.turkraft.springfilter.parser.node.FilterNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.manh.findJob.domain.Company;
import vn.manh.findJob.domain.Job;
import vn.manh.findJob.domain.Resume;
import vn.manh.findJob.domain.User;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.dto.Resume.ResResumeDTO;
import vn.manh.findJob.exception.ResourceNotFoundException;
import vn.manh.findJob.mapper.ResumeMapper;
import vn.manh.findJob.repository.ResumeRepository;
import vn.manh.findJob.repository.UserRepository;
import vn.manh.findJob.util.constant.ResumeStateEnum;


import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ResumeService {
    private final ResumeRepository resumeRepository;
    private final UserService userService;
    private final JobService jobService;
    private final ResumeMapper resumeMapper;
    private final UserRepository userRepository; // Inject thêm cái này
    private final EmailService emailService;
    @Autowired
    private FilterParser filterParser;

    @Autowired
    private FilterSpecificationConverter filterSpecificationConverter;
    //find resume by id
    private Resume findResumeById(long id)
    {
        return resumeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Resume not found with id: {}", id);
                    return new ResourceNotFoundException("Resume not found with id: " + id);
                });
    }

    //tạo resume
    public ResResumeDTO saveResume(Resume resume) {
        // 1. Lấy thông tin người đang đăng nhập
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentUser = userService.handleGetUserByUserName(email);

        // 2. KIỂM TRA QUYỀN
        if (currentUser != null) {
            // Kiểm tra null cho Role trước khi lấy tên
            if (currentUser.getRole() != null) {
                String roleName = currentUser.getRole().getName();

                // Chặn Admin và HR
                if ("SUPER_ADMIN".equals(roleName) || "HUMAN_RESOURCE".equals(roleName)) {
                    throw new IllegalArgumentException("Tài khoản này không được phép nộp hồ sơ.");
                }
            }

            // Chặn thêm nếu User thuộc về một Company (trường hợp HR chưa có Role chuẩn nhưng có Company)
            if (currentUser.getCompany() != null) {
                throw new IllegalArgumentException("Nhà tuyển dụng không được phép nộp hồ sơ.");
            }
        }

        // 3. Logic cũ giữ nguyên
        if (resume.getJob() == null || resume.getJob().getId() <= 0) {
            throw new IllegalArgumentException("Job information is missing or invalid.");
        }

        // Gán user thực tế đang đăng nhập vào Resume (để tránh việc gửi ID user khác)
        resume.setUser(currentUser);

        Job job = jobService.findJobById(resume.getJob().getId());
        resume.setJob(job);

        Resume savedResume = resumeRepository.save(resume);

        // 6. --- GỬI EMAIL (PHẦN MỚI) ---

        // A. Gửi cho Ứng viên (Gửi vào email đang đăng nhập)
        if (currentUser != null) {
            this.emailService.sendEmailToCandidateAfterApply(
                    currentUser.getEmail(), // Email người đang login
                    job.getName(),
                    currentUser.getName()
            );
        }


        // B. Gửi cho HR (Lấy từ Job -> Company -> List<User>)
        Company company = job.getCompany();
        if (company != null) {
            // Lấy tất cả nhân viên thuộc công ty này
            List<User> companyUsers = this.userRepository.findByCompany(company);

            // Link xem hồ sơ (để HR click vào)
            String resumeLink = "http://localhost:3000/admin/resume";

            if(companyUsers!=null)
            {
                for (User staff : companyUsers){
                    // (Optional) Kiểm tra kỹ: Chỉ gửi cho tài khoản có quyền HR
                    // Nếu công ty có nhân viên khác không phải HR, ta không nên spam họ
                    // Giả sử tên role HR trong DB là "HUMAN_RESOURCE"
                    if (staff.getRole() != null && "HUMAN_RESOURCE".equals(staff.getRole().getName())) {

                        this.emailService.sendEmailToHRAfterApply(
                                staff.getEmail(),   // Email của HR
                                job.getName(),
                                currentUser.getName(),
                                resumeLink
                        );
                    }
                }
            }

        }
        ResResumeDTO resResumeDTO = resumeMapper.toResumeDTO(savedResume);
        resResumeDTO.setCompanyName(job.getCompany().getName());
        return resResumeDTO;
    }

    // update a resume
    public ResResumeDTO updateResume(Resume resume,long id)
    {

        Resume resume1=this.findResumeById(id);
        boolean isStatusChanged = resume1.getStatus() != resume.getStatus();
        resume1.setStatus(resume.getStatus());
        Resume currentResume=resumeRepository.save(resume1);
        ResResumeDTO resResumeDTO = resumeMapper.toResumeDTO(currentResume);
        // 4. GỬI MAIL THÔNG BÁO (Nếu status thay đổi)
        if (isStatusChanged) {
            String candidateEmail = currentResume.getUser().getEmail();
            String candidateName = currentResume.getUser().getName();
            String jobName = currentResume.getJob().getName();

            // Trường hợp 1: APPROVED -> Gửi mail mời phỏng vấn
            if (currentResume.getStatus() == ResumeStateEnum.APPROVED) {
                this.emailService.sendEmailInterview(candidateEmail, candidateName, jobName);
            }

            // Trường hợp 2: REJECTED -> Gửi mail cảm ơn
            else if (currentResume.getStatus() == ResumeStateEnum.REJECTED) {
                this.emailService.sendEmailReject(candidateEmail, candidateName, jobName);
            }
        }
        return resResumeDTO;
    }

    //DELETE
    public void deleteResume(long id)
    {
        Resume resume=this.findResumeById(id);
        resumeRepository.delete(resume);
    }

    //get resume by id
    public ResResumeDTO getResumeById(long id)
    {
        Resume resume=this.findResumeById(id);
        ResResumeDTO resResumeDTO=resumeMapper.toResumeDTO(resumeRepository.save(resume));
        return  resResumeDTO;

    }

    //get all
    public ResultPaginationDTO getAllResume(Specification<Resume> specification, Pageable pageable) {
        Page<Resume> pageResume=resumeRepository.findAll(specification,pageable);
        ResultPaginationDTO rs=new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber());
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageResume.getTotalPages());
        meta.setTotal(pageResume.getTotalElements());
        rs.setMeta(meta);
        rs.setResult(pageResume.getContent());
        // Lấy danh sách Resume entity từ Page
        List<Resume> resume = pageResume.getContent();

        // DÙNG STREAM API ĐỂ CHUYỂN ĐỔI DANH SÁCH
        List<ResResumeDTO> ResumeDTOList = resume.stream()
                .map(resumeMapper::toResumeDTO) // Áp dụng hàm mapping cho từng user
                .collect(Collectors.toList());     // Gom kết quả lại thành một List mới

        //Gán danh sách DTO đã được chuyển đổi vào kết quả
        rs.setResult(ResumeDTOList);
        return rs;
    }

    //get all resume by user email login
    public ResultPaginationDTO fetchResumeByUser(Pageable pageable) {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        FilterNode node = filterParser.parse("email='" + email + "'");
        FilterSpecification<Resume> spec = filterSpecificationConverter.convert(node);

        Page<Resume> resumePage = this.resumeRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1); // Fix page index
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(resumePage.getTotalPages());
        meta.setTotal(resumePage.getTotalElements());
        rs.setMeta(meta);
        // Nên map sang DTO để bảo mật và đẹp hơn
        List<ResResumeDTO> resumeDTOList = resumePage.getContent().stream()
                .map(resumeMapper::toResumeDTO)
                .collect(Collectors.toList());
        rs.setResult(resumeDTOList);
        return rs;
    }
}
