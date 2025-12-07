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
import vn.manh.findJob.dto.ResponseData;
import vn.manh.findJob.dto.ResultPaginationDTO;
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
        User currentUser = this.userService.handleGetUserByUserName(email);

        if (currentUser != null) {
            Company userCompany = currentUser.getCompany();

            // 2. Nếu User thuộc 1 công ty (HR) -> Chỉ xem job của công ty đó
            if (userCompany != null) {

                // --- THAY THẾ FILTER BUILDER BẰNG JPA SPECIFICATION ---
                Specification<Job> companySpec = (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("company").get("id"), userCompany.getId());
                // ------------------------------------------------------

                // Kết hợp: (Thuộc công ty này) AND (Điều kiện lọc từ Frontend nếu có)
                Specification<Job> finalSpec = companySpec;
                if (specification != null) {
                    finalSpec = companySpec.and(specification);
                }

                ResultPaginationDTO result = jobService.getAllJob(finalSpec, pageable);
                return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Fetch jobs successful", result));
            }
        }

        // 3. Nếu là Admin hoặc User thường -> Xem tất cả
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