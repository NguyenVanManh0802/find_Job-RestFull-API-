package vn.manh.findJob.controller;


import com.turkraft.springfilter.boot.Filter;
import com.turkraft.springfilter.builder.FilterBuilder;
import com.turkraft.springfilter.converter.FilterSpecificationConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import vn.manh.findJob.domain.Company;
import vn.manh.findJob.domain.Job;
import vn.manh.findJob.domain.Resume;
import vn.manh.findJob.domain.User;
import vn.manh.findJob.dto.ResponseData;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.dto.Resume.ResResumeDTO;
import vn.manh.findJob.service.ResumeService;
import vn.manh.findJob.service.SecurityUtil;
import vn.manh.findJob.service.UserService;


import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeController {
    private final ResumeService resumeService;
    private final UserService userService;
    private final FilterSpecificationConverter filterSpecificationConverter;
    private final FilterBuilder filterBuilder;
    //create Resume
    @PostMapping("")
    public ResponseEntity<ResponseData<ResResumeDTO>> creatResume(@RequestBody Resume resume)
    {
        log.info("Request add Resume");
        ResResumeDTO resResumeDTO=resumeService.saveResume(resume);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(resResumeDTO.getId())
                .toUri();
        ResponseData<ResResumeDTO> responseData = new ResponseData<>(
                HttpStatus.CREATED.value(),
                "Resume created successfully",
                resResumeDTO);
        return ResponseEntity.created(location).body(responseData);
    }

    //update resume by id
    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<ResResumeDTO>> updateResumeByid(@RequestBody Resume resume,@PathVariable long id)
    {
        ResponseData<ResResumeDTO>responseData=new ResponseData<>(
                HttpStatus.OK.value(),
                "update resume by Id successful",
                resumeService.updateResume(resume,id)
        );
        return ResponseEntity.ok(responseData);
    }

    //delete
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteResumeById( @PathVariable  long id)
    {
        log.info("delete job by id ={}",id);
        resumeService.deleteResume(id);
        ResponseData<Void> responseData= new ResponseData<>(
                HttpStatus.OK.value(),
                "delete resume successful"
        );
        return ResponseEntity.ok(responseData);
    }

    //get resume by id
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<ResResumeDTO>> getResumeById(@PathVariable long id)
    {
        log.info("get resume with id = {}",id);
        ResResumeDTO resResumeDTO = resumeService.getResumeById(id);
        ResponseData<ResResumeDTO>responseData=new ResponseData<>(
                HttpStatus.OK.value(),
                "get resume by Id successful",
                resResumeDTO
        );
        return ResponseEntity.ok(responseData);
    }

    //get all
    @GetMapping()
    public ResponseEntity<ResponseData<ResultPaginationDTO>> getAllResume(@Filter Specification<Resume> specification, Pageable pageable)
    {
        List<Long> arrJobs=null;
        String email= SecurityUtil.getCurrentUserLogin().isPresent()==true ?SecurityUtil.getCurrentUserLogin().get() : "";
        User curentUser=this.userService.handleGetUserByUserName(email);

        if(curentUser!=null)
        {
            Company userCompany=curentUser.getCompany();
            if(userCompany!=null)
            {
                List<Job> companyJobs=userCompany.getJobs();
                if(companyJobs!=null && companyJobs.size()>0)
                {
                    arrJobs=companyJobs.stream().map(x->x.getId())
                            .collect(Collectors.toList());
                }
            }
            Specification<Resume> jobInSpec=filterSpecificationConverter.convert(filterBuilder.field("job")
                    .in(filterBuilder.input(arrJobs)).get());
            Specification<Resume> finalSpec=jobInSpec.and(specification);
            ResultPaginationDTO resultPaginationDTO=resumeService.getAllResume(finalSpec,pageable);
            ResponseData<ResultPaginationDTO>responseData=new ResponseData<>(
                    HttpStatus.OK.value(),
                    "FETCH RESUME SUCCESSFUL",
                    resultPaginationDTO
            );
            return ResponseEntity.ok(responseData);


        }

        ResultPaginationDTO resultPaginationDTO=resumeService.getAllResume(specification,pageable);
        ResponseData<ResultPaginationDTO> responseData = new ResponseData<>(
                HttpStatus.OK.value(),
                "Fetch all resumes successfully",
                resultPaginationDTO
        );


        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    //get resume by user
    @PostMapping("/by-users")
    public ResponseEntity<ResponseData<ResultPaginationDTO>> fetchResumeByUser(Pageable pageable)
    {

        ResponseData<ResultPaginationDTO>responseData=new ResponseData<>(
                HttpStatus.OK.value(),
                "get resume by user",
                resumeService.fetchResumeByUser(pageable)
        );
        return ResponseEntity.ok(responseData);
    }
}
