package vn.manh.findJob.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.manh.findJob.domain.Resume;
import vn.manh.findJob.dto.Resume.ResResumeDTO;


@Mapper(componentModel = "spring")
public interface ResumeMapper {

    // THÊM ANNOTATION @Mapping VÀO ĐÂY
    @Mapping(source = "job.company.name", target = "companyName")
    ResResumeDTO toResumeDTO(Resume resume);
}