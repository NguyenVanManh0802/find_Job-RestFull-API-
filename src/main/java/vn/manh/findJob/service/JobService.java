package vn.manh.findJob.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
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
public class JobService {
    private final JobRepository jobRepository;
    private final SkillRepository skillRepository; // Tiêm SkillRepository vào
    private final JobMapper jobMapper;
    private final CompanyRepository companyRepository;

    //tìm job theo id
    public Job findJobById(long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Job not found with id: {}", id);
                    return new ResourceNotFoundException("Job not found with id: " + id);
                });
    }
    public JobDTO saveJob(Job job) {
        // BƯỚC 1: KIỂM TRA VÀ LẤY CÁC SKILL TỪ DATABASE
        if (job.getSkills() != null) {
            // 1.1: Trích xuất danh sách các ID từ List<Skill> đầu vào
            List<Long> skillIds = job.getSkills()
                    .stream()
                    .map(Skill::getId) // Với mỗi skill, lấy ra ID
                    .collect(Collectors.toList());

            // 1.2: Tìm tất cả các skill có trong DB dựa trên danh sách ID
            List<Skill> dbSkills = this.skillRepository.findByIdIn(skillIds);


            // 1.4: Gán lại danh sách các skill đã được quản lý bởi JPA
            job.setSkills(dbSkills);
        }

        //check có company không
        if(job.getCompany()!=null)
        {
            Optional<Company> companyOptional=companyRepository.findById(job.getCompany().getId());
            if(companyOptional.isPresent())
            {
                job.setCompany(companyOptional.get());
            }
        }

        // BƯỚC 2: LƯU JOB VỚI CÁC SKILL HỢP LỆ
        Job newJob = jobRepository.save(job);
        JobDTO jobDTO=new JobDTO();
        jobDTO =this.jobMapper.toJobDTO(newJob);
        log.info("Job đã được lưu thành công, jobName = {} ", newJob.getName());
        return jobDTO;
    }

    //get all job
    public ResultPaginationDTO getAllJob(Specification<Job> specification, Pageable pageable) {
        Page<Job> pageJob=jobRepository.findAll(specification,pageable);
        ResultPaginationDTO rs=new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber());
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageJob.getTotalPages());
        meta.setTotal(pageJob.getTotalElements());
        rs.setMeta(meta);
        rs.setResult(pageJob.getContent());
        // Lấy danh sách Job entity từ Page
        List<Job> jobs = pageJob.getContent();

        // DÙNG STREAM API ĐỂ CHUYỂN ĐỔI DANH SÁCH
        List<JobDTO> jobDTOList = jobs.stream()
                .map(jobMapper::toJobDTO) // Áp dụng hàm mapping cho từng user
                .collect(Collectors.toList());     // Gom kết quả lại thành một List mới

        // Gán danh sách DTO đã được chuyển đổi vào kết quả
        rs.setResult(jobDTOList);
        return rs;
    }

    //update job by id
    public JobDTO updateJobById(long id, Job job)
    {
        if(job.getSkills()!=null)
        {
            List<Long> skillIds = job.getSkills()
                    .stream()
                    .map(Skill::getId) // Với mỗi skill, lấy ra ID
                    .collect(Collectors.toList());

            // 1.2: Tìm tất cả các skill có trong DB dựa trên danh sách ID
            List<Skill> dbSkills = this.skillRepository.findByIdIn(skillIds);


            // 1.4: Gán lại danh sách các skill đã được quản lý bởi JPA
            job.setSkills(dbSkills);
        }
        //check có company không
        if(job.getCompany()!=null)
        {
            Optional<Company> companyOptional=companyRepository.findById(job.getCompany().getId());
            if(companyOptional.isPresent())
            {
                job.setCompany(companyOptional.get());
            }
        }
        log.info("get job existed by id ");
        Job jobExisted=this.findJobById(id);

        log.info("update job by new job");
        jobExisted.setName(job.getName());
        jobExisted.setActive(job.isActive());
        jobExisted.setLevel(job.getLevel());
        jobExisted.setLocation(job.getLocation());
        jobExisted.setQuantity(job.getQuantity());
        jobExisted.setSalary(job.getSalary());
        jobExisted.setCompany(job.getCompany());
        jobExisted.setSkills(job.getSkills());
        jobExisted.setDescription(job.getDescription());

        Job job1=jobRepository.save(jobExisted);
        JobDTO jobDTO=new JobDTO();
        jobDTO=jobMapper.toJobDTO(job1);
        log.info("job save successful");
        return jobDTO;

    }

    //get job by id
    public JobDTO getJobById(long id) {
        log.info("Fetching job with id={}", id);

        return jobRepository.findById(id) // Bước 1: Trả về Optional<Job>
                .map(jobMapper::toJobDTO)    // Bước 2: Nếu có Job, chuyển nó thành JobDTO
                .orElseThrow(() -> {         // Bước 3: Nếu không có, ném ra exception
                    log.warn("Job not found with id={}", id);
                    return new ResourceNotFoundException("Job not found with id: " + id);
                });
    }

    //delete Job by id
    public void deleteJobById(long id)
    {
        log.info("delete job with id ={}",id);
        if(!jobRepository.existsById(id))
        {
            log.info("job not found with id={}",id);
            throw  new ResourceNotFoundException("Job not found with id : "+id );
        }
        jobRepository.deleteById(id);
        log.info("delete job successful");
    }




}