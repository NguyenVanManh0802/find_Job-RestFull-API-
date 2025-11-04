package vn.manh.findJob.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import vn.manh.findJob.util.constant.LevelEnum;

import java.time.Instant;
import java.util.List;

@Getter //mục đích là lombok sẽ tự động tạo getter cho các field khác static
@Setter  // ---------------------------------setter
@Builder //có thể sử dụng để xây dựng đối tượng một cách linh hoạt
@NoArgsConstructor
@AllArgsConstructor //tạo constructor với đầy đủ tham so
@Entity
@Table(name="Jobs")
public class Job {

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;
    @NotBlank(message = "name not blank")
    private String name;
    private String location;
    private double salary;
    private int quantity;
    @Enumerated(EnumType.STRING)
    private LevelEnum level;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;
    private Instant startDate;
    private Instant endDate;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    //job
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    //mối quan hệ giữa Job và Skill là N-N
    //job
    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "jobs" })
    @JoinTable(name = "job_skill", joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private List<Skill> skills;


    @OneToMany(mappedBy ="job" ,fetch = FetchType.LAZY)
    @JsonIgnore
    List<Resume> resumes;


    //chạy phuương này trươ khi lưu mơi một entity
//    @PrePersist
//    public void handleBeforeSave()
//    {
//        this.createdAt=Instant.now();
//        this.createdBy= SecurityUtil.getCurrentUserLogin().isPresent()==true ?
//                SecurityUtil.getCurrentUserLogin().get() : "" ;
//    }
//
//    //chạy pương thức này trước khi cập nhật 1 entity (chỉ chạy khi data thay đổi để cập nhật)
//    @PreUpdate
//    public void UpdateBeforeSave()
//    {
//        this.updatedBy=SecurityUtil.getCurrentUserLogin().orElse("");
//        this.updatedAt=Instant.now();
//    }
}
