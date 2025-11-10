package vn.manh.findJob.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
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
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public Company saveCompany(Company company) {
        Company company1 = companyRepository.save(company);
        log.info("company have been saved successful ,companyName = {} ",company1.getName());
        return company1;
    }

    public ResultPaginationDTO getAllCompany(Specification<Company> specification, Pageable pageable) {
        Page<Company> pageCompany=companyRepository.findAll(specification,pageable);
        ResultPaginationDTO rs=new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber()+1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageCompany.getTotalPages());
        meta.setTotal(pageCompany.getTotalElements());
        rs.setMeta(meta);
        rs.setResult(pageCompany.getContent());
        return rs;
    }
    public Company getCompanyById(long id) {
        log.info("get company by id ={} ",id);
        return companyRepository.findById(id)
                .orElseThrow(()-> {
                        log.info("company not found with id={}",id);
                        return new ResourceNotFoundException("Company not found with id: " + id);
                });
    }
    public void deleteCompanyById(long id)
    {
        log.info("delete company with id ={}",id);

        if(!companyRepository.existsById(id))
        {
            log.info("company not found with id={}",id);
            throw  new ResourceNotFoundException("Company not found with id : "+id );
        }
        Optional<Company> companyOptional=this.companyRepository.findById(id);
        if(companyOptional.isPresent())
        {
            Company com=companyOptional.get();
            //fetch all user belong to this company
            List<User> users=this.userRepository.findByCompany(com);
            this.userRepository.deleteAll(users);
        }
        companyRepository.deleteById(id);
        log.info("delete company successful");
    }
    public Company updateCompanyById(long id,Company company)
    {
        log.info("get company existed by id ");
        Company companyExisted=this.getCompanyById(id);

        log.info("update company by new company");
        companyExisted.setName(company.getName());
        companyExisted.setAddress(company.getAddress());
        companyExisted.setLogo(company.getLogo());
        companyExisted.setDescription(company.getDescription());

        Company company1=companyRepository.save(companyExisted);
        log.info("company save successful");
        return company1;

    }
}
