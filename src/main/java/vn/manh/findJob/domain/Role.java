package vn.manh.findJob.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;


import java.time.Instant;
import java.util.List;

@Getter //mục đích là lombok sẽ tự động tạo getter cho các field khác static
@Setter  // ---------------------------------setter
@Builder //có thể sử dụng để xây dựng đối tượng một cách linh hoạt
@NoArgsConstructor
@AllArgsConstructor //tạo constructor với đầy đủ tham so
@Entity
@Table(name="roles")
public class Role {
    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;
    private String name;
    private String description;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private  String createdBy;
    private String updatedBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "roles" })
    @JoinTable(name = "permission_role", joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private List<Permission> permissions;


    //role -user
    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    @JsonIgnore //tránh vòng lặp vô hạn
            List<User> users;



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