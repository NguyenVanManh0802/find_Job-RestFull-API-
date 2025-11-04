package vn.manh.findJob.domain;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


import java.time.Instant;
import java.util.List;

@Getter //mục đích là lombok sẽ tự động tạo getter cho các field khác static
@Setter  // ---------------------------------setter
@Builder //có thể sử dụng để xây dựng đối tượng một cách linh hoạt
@NoArgsConstructor
@AllArgsConstructor //tạo constructor với đầy đủ tham so
@Entity
@Table(name="skills")
public class Skill {

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;

    @NotBlank(message = "name not blank")
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;


    //skill
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "skills")
    @JsonIgnore
    private List<Job> jobs;

    //subscriber
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "skills")
    @JsonIgnore
    private List<Subscriber> subscribers;


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