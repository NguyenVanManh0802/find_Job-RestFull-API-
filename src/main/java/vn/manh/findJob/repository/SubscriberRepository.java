package vn.manh.findJob.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.manh.findJob.domain.Company;
import vn.manh.findJob.domain.Subscriber;


import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long>, JpaSpecificationExecutor<Subscriber> {
    boolean existsByEmail(String email);
    Subscriber findByEmail(String email);
    boolean existsByEmailAndCompanies_Id(String email, long companyId);
    List<Subscriber> findByCompanies(Company company);
}