package vn.manh.findJob.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter //mục đích là lombok sẽ tự động tạo getter cho các field khác static
@Setter  // ---------------------------------setter
@Builder //có thể sử dụng để xây dựng đối tượng một cách linh hoạt
@NoArgsConstructor
@AllArgsConstructor //tạo constructor với đầy đủ tham so
@Entity
@Table(name="Subcribers")
public class Subscriber {
    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private String name;
    private String email;


    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"subscribers"})
    @JoinTable(name = "subscriber_skill", joinColumns = @JoinColumn(name = "subscriber_id"), inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private List<Skill> skills;
}