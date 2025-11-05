package vn.manh.findJob.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.manh.findJob.domain.Job;
import vn.manh.findJob.domain.Skill;


import java.util.List;


@Repository
public interface JobRepository extends JpaRepository<Job,Long>, JpaSpecificationExecutor<Job> {
    List<Job> findBySkillsIn(List<Skill>skills);
}
