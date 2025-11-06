package vn.manh.findJob.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Không serialize các trường null
public class ErrorResponse {
    private Date timestamp;
    private int status;
    private String path;
    private String error;
    private String message;
    private String traceId; // Thêm traceId
    private List<ApiError> details; // Dùng một list các object lỗi thay vì Map

    // Constructor tiện lợi
    public ErrorResponse(int status, String path, String error, String message, List<ApiError> details) {
        this.timestamp = new Date();
        this.status = status;
        this.path = path;
        this.error = error;
        this.message = message;
        this.traceId = UUID.randomUUID().toString(); // Tự động tạo
        this.details = details;
    }

    // Constructor cho các lỗi đơn giản không có details list
    public ErrorResponse(int status, String path, String error, String message) {
        this(status, path, error, message, null);
    }
}


