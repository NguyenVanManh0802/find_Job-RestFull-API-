package vn.manh.findJob.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import vn.manh.findJob.service.SecurityUtil;

import java.time.Instant;
import java.util.List;

@Getter //mục đích là lombok sẽ tự động tạo getter cho các field khác static
@Setter  // ---------------------------------setter
@Builder //có thể sử dụng để xây dựng đối tượng một cách linh hoạt
@NoArgsConstructor
@AllArgsConstructor //tạo constructor với đầy đủ tham so
@Entity
@Table(name="companies")
public class Company {
    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;
    @NotBlank(message = "Company name cannot be empty")
    private String name;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;
    @NotBlank(message = "Address cannot be empty")
    private String address;
    private String logo;
    //    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a",timezone = "GMT+7")
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @OneToMany(mappedBy = "company", fetch = FetchType.EAGER) //fetch =FetchType.LZAY nói cho hibernate bt rằng
    // khi nào cần lấy ds user thì hãy fetch còn không thì không cần
    @JsonIgnore //TRÁNH LẶP VÔ HẠN KHI LOAD DỮ LIỆU
    List<User> users;

    //mối quan hệ giữa company và jo
    @OneToMany(mappedBy = "company", fetch = FetchType.EAGER)
    @JsonIgnore
    List<Job> jobs;


    //chạy phuương này trươ khi lưu mơi một entity
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