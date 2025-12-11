package vn.manh.findJob.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ReqJobFilterDTO {
    // Tìm kiếm chung (tên job, tên công ty, kỹ năng) - Đây là phần "Gần đúng"
    private String keyword;

    // Bộ lọc chính xác
    private String location; // Địa điểm (Hồ Chí Minh, Hà Nội...)
    private Double minSalary;
    private Double maxSalary;
    private List<String> skills; // List các kỹ năng muốn lọc (Java, React...)
}