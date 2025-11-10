package vn.manh.findJob.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.manh.findJob.domain.Skill;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.exception.ResourceAlreadyExistsException;
import vn.manh.findJob.exception.ResourceNotFoundException;
import vn.manh.findJob.repository.SkillRepository;

import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
@Service
public class SkillService {

    private final SkillRepository skillRepository;

    public Skill saveSkill(Skill skill) {
        // SỬA LẠI LOGIC KIỂM TRA CHO ĐÚNG
        // Gọi thẳng đến phương thức existsByName
        if (this.skillRepository.existsByName(skill.getName())) {
            throw new ResourceAlreadyExistsException("Skill với tên '" + skill.getName() + "' đã tồn tại.");
        }

        Skill newSkill = skillRepository.save(skill);
        log.info("Skill đã được lưu thành công, skillName = {} ", newSkill.getName());
        return newSkill;
    }
    public ResultPaginationDTO getAllSkill(Specification<Skill> specification, Pageable pageable) {
        Page<Skill> pageSkill=skillRepository.findAll(specification,pageable);
        ResultPaginationDTO rs=new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber());
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageSkill.getTotalPages());
        meta.setTotal(pageSkill.getTotalElements());
        rs.setMeta(meta);
        rs.setResult(pageSkill.getContent());
        return rs;
    }

    //get skill by id
    public Skill getSkillById(long id) {
        log.info("get Skill by id ={} ",id);
        return skillRepository.findById(id)
                .orElseThrow(()-> {
                    log.info("Skill not found with id={}",id);
                    return new ResourceNotFoundException("Skill not found with id: " + id);
                });
    }

    //update skill by id
    public Skill updateSkillById(long id, Skill skill)
    {
        log.info("get skill existed by id ");
        if (this.skillRepository.existsByName(skill.getName())) {
            throw new ResourceAlreadyExistsException("Skill với tên '" + skill.getName() + "' đã tồn tại.");
        }

        Skill skillExisted=this.getSkillById(id);

        log.info("update skill by new skill");
        skillExisted.setName(skill.getName());
        Skill skill1=skillRepository.save(skillExisted);
        log.info("skill save successful");
        return skill1;

    }

    //delete skill by id
    public void deleteSkill (long id)
    {
        //delete job (inside job_skill table
        Optional<Skill> skillOptional=this.skillRepository.findById(id);
        Skill currentSkill=skillOptional.get();
        currentSkill.getJobs().forEach(job->job.getSkills().remove(currentSkill));

        //delete subscriber
        currentSkill.getSubscribers().forEach(subs->subs.getSkills().remove(currentSkill));

        //delete skill
        skillRepository.delete(currentSkill);
    }
    
}