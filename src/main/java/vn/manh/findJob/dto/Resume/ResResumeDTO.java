package vn.manh.findJob.dto.Resume;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResResumeDTO {
    private String id;
    private String email;
    private String url;
    private String status;
    private Instant updatedAt;
    private String updatedBy;
    private Instant createdAt;
    private String createdBy;
    private String companyName;
    private UserResume user;
    private JobResume job;

    @AllArgsConstructor
    @Getter
    @Setter
    public static class UserResume{
        private String id;
        private String email;
        private String name;
        private int age;
        private String gender;
        private String address;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class JobResume{
        private long id;
        private String name;
        private String location;
        private double salary;
        private int quantity;
        private String level;
        @Column(columnDefinition = "MEDIUMTEXT")
        private String description;
        private Instant startDate;
        private Instant endDate;
        private boolean active;
    }


}
