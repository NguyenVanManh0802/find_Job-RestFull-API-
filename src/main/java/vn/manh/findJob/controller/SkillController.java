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
import vn.manh.findJob.domain.Skill;
import vn.manh.findJob.dto.ResponseData;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.service.SkillService;

import java.net.URI;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/skills")
public class SkillController {
    private final SkillService skillService;
    @PostMapping()
    public ResponseEntity<ResponseData<Skill>> createSkill(@Valid @RequestBody Skill skill)
    {
        log.info("Request add Skill");
        Skill skills1=skillService.saveSkill(skill);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(skills1.getId())
                .toUri();
        ResponseData<Skill> responseData = new ResponseData<>(
                HttpStatus.CREATED.value(),
                "Skill created successfully",
                skills1);
        return ResponseEntity.created(location).body(responseData);
    }

    //get all Skill
    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getAllCompanies(@Filter Specification<Skill> specification,
                                                               Pageable pageable
    ) {
        ResultPaginationDTO rs=skillService.getAllSkill(specification,pageable);
        return ResponseEntity.status(HttpStatus.OK).body(rs);
    }

    //get skill by id
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<Skill>>getSkillById(@PathVariable  long id)
    {
        log.info("get skill with id = {}",id);
        Skill skill = skillService.getSkillById(id);
        ResponseData<Skill>responseData=new ResponseData<>(
                HttpStatus.OK.value(),
                "get skill by Id successful",
                skill
        );
        return ResponseEntity.ok(responseData);
    }

    //update skill by id
    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<Skill>> updateSkillById(@PathVariable long id, @RequestBody Skill skill)
    {
        log.info("update skill with id ={} ",id);
        Skill skill1=skillService.updateSkillById(id,skill);
        ResponseData<Skill> responseData=new ResponseData<>(
                HttpStatus.OK.value(),
                "update skill successful",
                skill1
        );
        return ResponseEntity.ok(responseData);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteSkillById( @PathVariable  long id)
    {
        log.info("delete job by id ={}",id);
        skillService.deleteSkill(id);
        ResponseData<Void> responseData= new ResponseData<>(
                HttpStatus.OK.value(),
                "delete skill successful"
        );
        return ResponseEntity.ok(responseData);
    }
}
