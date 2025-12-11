package vn.manh.findJob.repository;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import vn.manh.findJob.domain.Job;
import vn.manh.findJob.domain.Skill;
import vn.manh.findJob.dto.ReqJobFilterDTO;


import java.util.ArrayList;
import java.util.List;

public class JobSpecifications {

    public static Specification<Job> filterJob(ReqJobFilterDTO filter) {
        return (Root<Job> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. TÌM KIẾM GẦN ĐÚNG (KEYWORD)
            // Logic: Keyword có thể nằm trong (Tên Job) HOẶC (Tên Công ty) HOẶC (Kỹ năng)
            if (filter.getKeyword() != null && !filter.getKeyword().isEmpty()) {
                String searchKey = "%" + filter.getKeyword().toLowerCase() + "%";

                Predicate nameLike = cb.like(cb.lower(root.get("name")), searchKey);
                Predicate companyLike = cb.like(cb.lower(root.get("company").get("name")), searchKey);

                // Join bảng Skill để tìm trong kỹ năng
                Join<Job, Skill> skillJoin = root.join("skills", JoinType.LEFT);
                Predicate skillLike = cb.like(cb.lower(skillJoin.get("name")), searchKey);

                // Gộp lại bằng OR
                predicates.add(cb.or(nameLike, companyLike, skillLike));
            }

            // 2. LỌC THEO ĐỊA ĐIỂM (Location)
            if (filter.getLocation() != null && !filter.getLocation().isEmpty()) {
                // Dùng LIKE để tìm gần đúng địa điểm (VD: nhập "HCM" ra "TP. Hồ Chí Minh")
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + filter.getLocation().toLowerCase() + "%"));
            }

            // 3. LỌC THEO MỨC LƯƠNG (Salary Range)
            if (filter.getMinSalary() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salary"), filter.getMinSalary()));
            }
            if (filter.getMaxSalary() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salary"), filter.getMaxSalary()));
            }

            // 4. LỌC THEO LIST KỸ NĂNG (Nếu user chọn checkbox nhiều skill)
            if (filter.getSkills() != null && !filter.getSkills().isEmpty()) {
                // Cần join bảng skill nếu chưa join ở bước 1
                // Lưu ý: Logic này hơi phức tạp nếu kết hợp với bước 1,
                // nhưng đơn giản nhất là dùng subquery hoặc join tiếp.
                // Ở đây mình ví dụ đơn giản: Job phải chứa MỘT TRONG CÁC skill này
                Join<Job, Skill> skillsJoin = root.join("skills", JoinType.INNER);
                predicates.add(skillsJoin.get("name").in(filter.getSkills()));
            }

            // QUAN TRỌNG: Loại bỏ các bản ghi trùng lặp (do phép Join)
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}