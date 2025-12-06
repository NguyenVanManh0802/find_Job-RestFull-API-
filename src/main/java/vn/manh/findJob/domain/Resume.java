package vn.manh.findJob.domain;

import jakarta.persistence.*;
import lombok.*;
import vn.manh.findJob.service.SecurityUtil;
import vn.manh.findJob.util.constant.ResumeStateEnum;


import java.time.Instant;

@Getter //mục đích là lombok sẽ tự động tạo getter cho các field khác static
@Setter  // ---------------------------------setter
@Builder //có thể sử dụng để xây dựng đối tượng một cách linh hoạt
@NoArgsConstructor
@AllArgsConstructor //tạo constructor với đầy đủ tham so
@Entity
@Table(name="resumes")
public class Resume {
    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;
    private String email;
    private String url;

    @Enumerated(EnumType.STRING)
    private ResumeStateEnum status;

    private Instant createdAt;
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name="job_id")
    private Job job;






    @PrePersist
    public void handleBeforeSave()
    {
        this.createdAt=Instant.now();
        this.createdBy= SecurityUtil.getCurrentUserLogin().isPresent()==true ?
                SecurityUtil.getCurrentUserLogin().get() : "" ;
    }

    //chạy pương thức này trước khi cập nhật 1 entity (chỉ chạy khi data thay đổi để cập nhật)
    @PreUpdate
    public void UpdateBeforeSave()
    {
        this.updatedBy=SecurityUtil.getCurrentUserLogin().orElse("");
        this.updatedAt=Instant.now();
    }
}
