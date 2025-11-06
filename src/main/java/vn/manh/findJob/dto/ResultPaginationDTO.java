package vn.manh.findJob.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultPaginationDTO {
    private Meta meta;
    private Object result; // Sử dụng Object để có thể tái sử dụng cho nhiều loại dữ liệu

    @Getter
    @Setter
    public static class Meta {
        private int page;       // Trang hiện tại
        private int pageSize;   // Số lượng phần tử trên một trang
        private int pages;      // Tổng số trang
        private long total;     // Tổng số phần tử
    }
}