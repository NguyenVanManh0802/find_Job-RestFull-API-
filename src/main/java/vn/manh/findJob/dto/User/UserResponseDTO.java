package vn.manh.findJob.dto.User;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.manh.findJob.util.constant.GenderEnum;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "name", "email", "age", "gender", "address",
        "createdAt", "createdBy", "updatedAt", "updatedBy","company" }) //THÊM ANNOTATION NÀY để xuaats dl ra theo uu tien
public class UserResponseDTO {
    private long Id;
    private String email;
    private String name;
    private int age;
    private GenderEnum gender;
    private boolean active;
    private String address;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private CompanyUser company;
    private RoleUser role;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @JsonPropertyOrder({ "id", "name"}) // THÊM ANNOTATION NÀY để xuaats dl ra theo uu tien
    public static class CompanyUser{
        private long Id;
        private String name;
    }
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @JsonPropertyOrder({ "id", "name"}) // THÊM ANNOTATION NÀY để xuaats dl ra theo uu tien
    public static class RoleUser{
        private long Id;
        private String name;
    }
}
