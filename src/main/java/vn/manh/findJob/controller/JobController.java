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
import vn.manh.findJob.domain.Job;
import vn.manh.findJob.dto.JobDTO;
import vn.manh.findJob.dto.ResponseData;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.service.JobService;

import java.net.URI;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {
    private final JobService jobService;
    //create job
    @PostMapping()
    public ResponseEntity<ResponseData<JobDTO>> createJob(@Valid @RequestBody Job job)
    {
        log.info("Request add Job");
        JobDTO jobs1=jobService.saveJob(job);
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

    //get all job
    @GetMapping()
    public ResponseEntity<ResponseData<ResultPaginationDTO>> getAllJob(@Filter Specification<Job> specification, Pageable pageable)
    {
        ResultPaginationDTO resultPaginationDTO=jobService.getAllJob(specification,pageable);
        ResponseData<ResultPaginationDTO> responseData = new ResponseData<>(
                HttpStatus.OK.value(),
                "Fetch all jobs successfully",
                resultPaginationDTO
        );


        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    //get Job by id
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<JobDTO>>getJobDTOById(@PathVariable  long id)
    {
        log.info("get job with id = {}",id);
        JobDTO job = jobService.getJobById(id);
        ResponseData<JobDTO>responseData=new ResponseData<>(
                HttpStatus.OK.value(),
                "get job by Id successful",
                job
        );
        return ResponseEntity.ok(responseData);
    }

    //update job by id
    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<JobDTO>> updateJobById(@PathVariable long id,@RequestBody Job job)
    {
        JobDTO jobDTO=new JobDTO();
        jobDTO=jobService.updateJobById(id,job);
        ResponseData<JobDTO>responseData=new ResponseData<>(
                HttpStatus.OK.value(),
                "update job by Id successful",
                jobDTO
        );
        return ResponseEntity.ok(responseData);
    }

    //delete by id
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteJobById( @PathVariable  long id)
    {
        log.info("delete job by id ={}",id);
        jobService.deleteJobById(id);
        ResponseData<Void> responseData= new ResponseData<>(
                HttpStatus.OK.value(),
                "delete job successful"
        );
        return ResponseEntity.ok(responseData);
    }
}
