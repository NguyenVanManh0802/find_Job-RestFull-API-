package vn.manh.findJob.controller;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import vn.manh.findJob.domain.Company;
import vn.manh.findJob.domain.Job;
import vn.manh.findJob.domain.User;
import vn.manh.findJob.dto.JobDTO;
import vn.manh.findJob.dto.ReqJobFilterDTO;
import vn.manh.findJob.dto.ResponseData;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.repository.JobSpecifications;
import vn.manh.findJob.service.JobService;
import vn.manh.findJob.service.SecurityUtil;
import vn.manh.findJob.service.UserService;

import java.net.URI;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    private final JobService jobService;
    private final UserService userService;
    // Không cần FilterBuilder hay Converter ở đây nếu dùng cách JPA thuần

    // create job
    @PostMapping()
    public ResponseEntity<ResponseData<JobDTO>> createJob(@Valid @RequestBody Job job) {
        log.info("Request add Job");
        JobDTO jobs1 = jobService.saveJob(job);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(jobs1.getId())
                .toUri();
        ResponseData<JobDTO> responseData = new ResponseData<>(
                HttpStatus.CREATED.value(),
                "Job created successfully",
                jobs1);
        return ResponseEntity.created(location).body(responseData);
    }

    // get all job (SỬ DỤNG JPA SPECIFICATION THUẦN)
    @GetMapping()
    public ResponseEntity<ResponseData<ResultPaginationDTO>> getAllJob(
            @Filter Specification<Job> specification,
            Pageable pageable) {

        // 1. Lấy thông tin User hiện tại
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        if (email.equals("") || email.equals("anonymousUser")) {
            Specification<Job> activeSpec = (root, query, cb) -> cb.isTrue(root.get("active"));

            Specification<Job> finalSpec = activeSpec;
            if (specification != null) finalSpec = activeSpec.and(specification);

            ResultPaginationDTO result = jobService.getAllJob(finalSpec, pageable);
            return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Fetch active jobs successful", result));
        }

        // 2. Logic cho User đã đăng nhập
        User currentUser = this.userService.handleGetUserByUserName(email);
        if (currentUser != null) {
            Company userCompany = currentUser.getCompany();

            // A. Nếu là HR (thuộc công ty) -> Xem job của công ty mình (kể cả chưa active)
            if (userCompany != null) {
                Specification<Job> companySpec = (root, query, cb) -> cb.equal(root.get("company").get("id"), userCompany.getId());

                Specification<Job> finalSpec = companySpec;
                if (specification != null) finalSpec = companySpec.and(specification);

                ResultPaginationDTO result = jobService.getAllJob(finalSpec, pageable);
                return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Fetch company jobs successful", result));
            }

            // B. Nếu là Ứng viên (Role USER) -> Chỉ xem Active = true
            // Cần check role name, giả sử role name của ứng viên là "USER"
            String roleName = currentUser.getRole() != null ? currentUser.getRole().getName() : "";
            if ("USER".equals(roleName)) {
                Specification<Job> activeSpec = (root, query, cb) -> cb.isTrue(root.get("active"));
                Specification<Job> finalSpec = activeSpec;
                if (specification != null) finalSpec = activeSpec.and(specification);

                ResultPaginationDTO result = jobService.getAllJob(finalSpec, pageable);
                return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Fetch active jobs successful", result));
            }
        }

        // 3. Nếu là ADMIN (Không thuộc công ty, role != USER) -> Xem tất cả (bao gồm chưa active để duyệt)
        ResultPaginationDTO result = jobService.getAllJob(specification, pageable);
        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Fetch all jobs successful", result));
    }

    // get Job by id
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<JobDTO>> getJobDTOById(@PathVariable long id) {
        log.info("get job with id = {}", id);
        JobDTO job = jobService.getJobById(id);
        ResponseData<JobDTO> responseData = new ResponseData<>(
                HttpStatus.OK.value(),
                "get job by Id successful",
                job
        );
        return ResponseEntity.ok(responseData);
    }


    @PostMapping("/search")
    public ResponseEntity<ResponseData<ResultPaginationDTO>> searchJobs(
            @RequestBody ReqJobFilterDTO jobFilterDTO,
            Pageable pageable) {

        // 1. Tạo Specification từ DTO
        Specification<Job> spec = JobSpecifications.filterJob(jobFilterDTO);

        // 2. Gọi Service (Service gọi Repository.findAll(spec, pageable))
        // Bạn có thể tận dụng hàm getAllJob cũ nếu nó nhận tham số Specification
        ResultPaginationDTO result = jobService.getAllJob(spec, pageable);

        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Search jobs successful",
                result));
    }
    // update job by id
    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<JobDTO>> updateJobById(@PathVariable long id, @RequestBody Job job) {
        JobDTO jobDTO = jobService.updateJobById(id, job);
        ResponseData<JobDTO> responseData = new ResponseData<>(
                HttpStatus.OK.value(),
                "update job by Id successful",
                jobDTO
        );
        return ResponseEntity.ok(responseData);
    }

    // delete by id
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteJobById(@PathVariable long id) {
        log.info("delete job by id ={}", id);
        jobService.deleteJobById(id);
        ResponseData<Void> responseData = new ResponseData<>(
                HttpStatus.OK.value(),
                "delete job successful"
        );
        return ResponseEntity.ok(responseData);
    }
}