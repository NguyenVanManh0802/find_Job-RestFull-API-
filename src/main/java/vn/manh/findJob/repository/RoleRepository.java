package vn.manh.findJob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.manh.findJob.domain.Role;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {

    boolean existsByName(String name);

    Role findByName(String name);

    // ---------------------------------------------------------------
    // ĐÂY LÀ CHÌA KHÓA GIẢI QUYẾT VẤN ĐỀ
    // ---------------------------------------------------------------
    // 1. LEFT JOIN FETCH: Lấy Role và lấy LUÔN cả Permissions đi kèm (kể cả list rỗng).
    // 2. Vì lấy luôn rồi, nên Permissions trở thành dữ liệu thật (không phải Proxy).
    // => Redis (Jackson) có thể đọc thoải mái mà không cần kết nối DB nữa.
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.id = :id")
    Optional<Role> findByIdWithPermissions(long id);
}