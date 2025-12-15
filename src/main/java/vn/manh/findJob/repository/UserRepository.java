package vn.manh.findJob.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.manh.findJob.domain.Company;
import vn.manh.findJob.domain.User;

import java.util.List;


@Repository
public interface UserRepository extends JpaRepository<User,Long>, JpaSpecificationExecutor<User> {

    User findByEmail( String userName);
    boolean existsByEmail(String email);
    List<User> findByCompany(Company company);
    //laays user theo email vaf token de kiem tra user co thatj toon tai ko
    User findByRefreshTokenAndEmail(String token,String email);

}
