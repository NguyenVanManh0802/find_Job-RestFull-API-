package vn.manh.findJob.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.manh.findJob.domain.Job;
import vn.manh.findJob.domain.Skill;


import java.util.List;
import java.util.Optional;


@Repository
public interface JobRepository extends JpaRepository<Job,Long>, JpaSpecificationExecutor<Job> {
    List<Job> findBySkillsIn(List<Skill>skills);
    // Câu lệnh ép Hibernate lấy Job + Company + Skills trong 1 lần gọi
    // Giúp JobMapper có đủ dữ liệu để convert sang DTO mà không lỗi Lazy
    @Query("SELECT j FROM Job j " +
            "LEFT JOIN FETCH j.company " +
            "LEFT JOIN FETCH j.skills " +
            "WHERE j.id = :id")
    Optional<Job> findByIdWithDetails(long id);
}
