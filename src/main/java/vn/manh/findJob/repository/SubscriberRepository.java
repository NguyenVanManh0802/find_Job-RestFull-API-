package vn.manh.findJob.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.manh.findJob.domain.Subscriber;


import java.util.Optional;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long>, JpaSpecificationExecutor<Subscriber> {

    // Dùng để kiểm tra khi tạo mới
    boolean existsByEmail(String email);

    // Dùng để kiểm tra khi cập nhật
    Optional<Subscriber> findByEmail(String email);
}