package vn.manh.findJob.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobDTO {

    private long Id;
    private String name;
    private String location;
    private double salary;
    private int quantity;
    private String description;
    private Instant startDate;
    private Instant endDate;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private List<SkillJob> skills;
    private CompanySkill company;
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class SkillJob{
        private long id;
        private String name;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class CompanySkill{
        private String name;
        private String description;
        private String address;
        private String logo;
        private Instant  createdAt;
        private Instant  updatedAt;
        private String  createdBy;
        private String updatedBy;
    }
}
