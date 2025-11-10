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
import vn.manh.findJob.dto.ResponseData;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.service.CompanyService;

import java.net.URI;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/companies")
@CrossOrigin(origins = "*")
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping()
    public ResponseEntity<ResponseData<Company>> createCompany(@Valid @RequestBody Company company)
    {
        log.info("Request add company");
        Company companySave=companyService.saveCompany(company);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(companySave.getId())
                .toUri();
        ResponseData<Company> responseData = new ResponseData<>(
                HttpStatus.CREATED.value(),
                "Company created successfully",
                companySave);
        return ResponseEntity.created(location).body(responseData);
    }
    //tại sao ta không sử dụng @RequestParam mà vẫn tự động lấy được gtri của page và size nhờ cơ chế của PageableHandlerMethodArgumentResolver
    //tự động đọc các tham số quen thuộc trên thanh url như page,size,sort
    @GetMapping
    public ResponseEntity<ResponseData<ResultPaginationDTO>> getAllCompanies(@Filter Specification<Company> specification,
                                                                             Pageable pageable
    ) {
        ResultPaginationDTO rs=companyService.getAllCompany(specification,pageable);
        ResponseData<ResultPaginationDTO>responseData=new ResponseData<>(
                HttpStatus.OK.value(),
                "fetch all company successful",
                rs
        );
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<Company>>getCompanyById(@PathVariable  long id)
    {
        log.info("get company with id = {}",id);
        Company company = companyService.getCompanyById(id);
        ResponseData<Company>responseData=new ResponseData<>(
            HttpStatus.OK.value(),
            "get company by Id successful",
            company
        );
        return ResponseEntity.ok(responseData);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteCompanyById( @PathVariable  long id)
    {
        log.info("delete company by id ={}",id);
        companyService.deleteCompanyById(id);
        ResponseData<Void> responseData= new ResponseData<>(
          HttpStatus.OK.value(),
          "delete company successful"
        );
        return ResponseEntity.ok(responseData);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<Company>> updateCompanyById(@PathVariable long id,@RequestBody Company company)
    {
        log.info("update company with id ={} ",id);
        Company company1=companyService.updateCompanyById(id,company);
        ResponseData<Company> responseData=new ResponseData<>(
            HttpStatus.OK.value(),
            "update company successful",
            company1
        );
        return ResponseEntity.ok(responseData);
    }
}
