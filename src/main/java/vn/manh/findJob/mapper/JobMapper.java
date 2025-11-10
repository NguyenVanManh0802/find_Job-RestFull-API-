package vn.manh.findJob.mapper;

import org.mapstruct.Mapper;
import vn.manh.findJob.domain.Job;
import vn.manh.findJob.dto.JobDTO;

@Mapper(componentModel = "spring") // Báo cho MapStruct tạo ra một Spring Bean
public interface JobMapper {
    JobDTO toJobDTO(Job job);
}
