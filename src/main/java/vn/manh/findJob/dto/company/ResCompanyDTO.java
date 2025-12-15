package vn.manh.findJob.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResCompanyDTO {
    private long id;
    private String name;
    private String description;
    private String address;
    private String logo;
    private Instant createdAt;
    private Instant updatedAt;
    // --- TRƯỜNG QUAN TRỌNG MỚI THÊM ---
    // Mặc định là false. Nếu user đang login đã theo dõi thì set = true
    // Ép buộc JSON trả về phải giữ nguyên tên là "isFollowed"
    @JsonProperty("isFollowed")
    private boolean isFollowed = false;
}