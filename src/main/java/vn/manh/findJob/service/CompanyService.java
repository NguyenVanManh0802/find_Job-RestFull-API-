package vn.manh.findJob.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional
import vn.manh.findJob.domain.Company;
import vn.manh.findJob.domain.User;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.exception.ResourceNotFoundException;
import vn.manh.findJob.repository.CompanyRepository;
import vn.manh.findJob.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional // 1. Giữ Transaction để Hibernate Session luôn mở
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    // --- HÀM HỖ TRỢ (QUAN TRỌNG) ---
    // Tạo bản sao sạch sẽ (POJO) để đưa vào Redis
    private Company sanitizeCompany(Company dbCompany) {
        Company cleanCompany = new Company();
        cleanCompany.setId(dbCompany.getId());
        cleanCompany.setName(dbCompany.getName());
        cleanCompany.setAddress(dbCompany.getAddress());
        cleanCompany.setLogo(dbCompany.getLogo());
        cleanCompany.setDescription(dbCompany.getDescription());
        cleanCompany.setCreatedAt(dbCompany.getCreatedAt());
        cleanCompany.setUpdatedAt(dbCompany.getUpdatedAt());
        cleanCompany.setCreatedBy(dbCompany.getCreatedBy());
        cleanCompany.setUpdatedBy(dbCompany.getUpdatedBy());

        // QUAN TRỌNG: Cắt đứt quan hệ để tránh lỗi Lazy và vòng lặp
        // Khi Cache Company, ta không cần lưu danh sách User hay Job của họ vào Redis
        cleanCompany.setUsers(null);
        cleanCompany.setJobs(null);

        return cleanCompany;
    }

    // Hàm private tìm kiếm nội bộ (Luôn gọi DB)
    private Company findCompanyByIdInternal(long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("company not found with id={}", id);
                    return new ResourceNotFoundException("Company not found with id: " + id);
                });
    }

    public Company saveCompany(Company company) {
        Company newCompany = companyRepository.save(company);
        log.info("company have been saved successful, companyName = {} ", newCompany.getName());
        return newCompany;
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO getAllCompany(Specification<Company> specification, Pageable pageable) {
        Page<Company> pageCompany = companyRepository.findAll(specification, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageCompany.getTotalPages());
        meta.setTotal(pageCompany.getTotalElements());
        rs.setMeta(meta);
        rs.setResult(pageCompany.getContent());
        return rs;
    }

    // 2. Cacheable: Lấy từ DB -> Sanitize -> Lưu Redis
    @Cacheable(value = "companies", key = "#id")
    public Company getCompanyById(long id) {
        log.info("get company by id ={} ", id);

        // Gọi hàm tìm kiếm nội bộ
        Company dbCompany = this.findCompanyByIdInternal(id);

        // Trả về object sạch
        return this.sanitizeCompany(dbCompany);
    }

    // 3. CacheEvict: Xóa Company -> Xóa Cache
    @CacheEvict(value = "companies", key = "#id")
    public void deleteCompanyById(long id) {
        log.info("delete company with id ={}", id);

        // Logic cũ của bạn: Xóa User trước
        Optional<Company> companyOptional = this.companyRepository.findById(id);
        if (companyOptional.isPresent()) {
            Company com = companyOptional.get();
            List<User> users = this.userRepository.findByCompany(com);
            this.userRepository.deleteAll(users);

            companyRepository.deleteById(id);
            log.info("delete company successful");
        } else {
            // Ném lỗi nếu không tìm thấy (logic cũ bạn dùng existsById, ở đây mình gộp luôn cho gọn)
            throw new ResourceNotFoundException("Company not found with id: " + id);
        }
    }

    // 4. CachePut: Update -> Sanitize -> Cập nhật Redis
    @CachePut(value = "companies", key = "#id")
    public Company updateCompanyById(long id, Company company) {
        log.info("update company id: {}", id);

        // Lấy Company cũ từ DB
        Company companyExisted = this.findCompanyByIdInternal(id);

        companyExisted.setName(company.getName());
        companyExisted.setAddress(company.getAddress());
        companyExisted.setLogo(company.getLogo());
        companyExisted.setDescription(company.getDescription());

        Company savedCompany = companyRepository.save(companyExisted);
        log.info("company save successful");

        // Trả về object sạch
        return this.sanitizeCompany(savedCompany);
    }
}