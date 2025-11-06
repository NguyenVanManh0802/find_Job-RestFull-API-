package vn.manh.findJob.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import vn.manh.findJob.dto.ApiError;
import vn.manh.findJob.dto.ErrorResponse;

import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    // ==================================================================================================================
    // == 400 BAD REQUEST GROUP ==
    // ==================================================================================================================

    /**
     * Handler cho lỗi validation của Request Body (@RequestBody)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        List<ApiError> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ApiError(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), getPath(request),
                "Validation Failed", "Request body is invalid", details
        );
    }

    /**
     * Handler cho lỗi validation của các tham số trên URL (@PathVariable, @RequestParam)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        List<ApiError> details = ex.getConstraintViolations()
                .stream()
                .map(violation -> {
                    String fullPath = violation.getPropertyPath().toString();
                    String paramName = fullPath.substring(fullPath.lastIndexOf('.') + 1);
                    return new ApiError(paramName, violation.getMessage());
                })
                .collect(Collectors.toList());
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), getPath(request),
                "Validation Failed", "URL parameters are invalid", details
        );
    }

    /**
     * Handler khi request body không phải là JSON hợp lệ hoặc lỗi giá trị Enum
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        String message = "The request body is not a valid JSON.";
        String error = "Malformed JSON";

        // Tùy chỉnh thông báo nếu lỗi là do giá trị Enum không hợp lệ
        if (ex.getCause() instanceof InvalidFormatException ifx && ifx.getTargetType().isEnum()) {
            message = String.format("Invalid value '%s' for field '%s'. Allowed values are: %s",
                    ifx.getValue(),
                    ifx.getPath().get(ifx.getPath().size() - 1).getFieldName(),
                    Arrays.toString(ifx.getTargetType().getEnumConstants()));
            error = "Invalid Enum Value";
        }

        log.warn("Malformed JSON request on path {}: {}", getPath(request), ex.getMessage());
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), getPath(request), error, message
        );
    }

    /**
     * Handler cho lỗi upload file
     */
    @ExceptionHandler(StorageException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleFileUploadException(StorageException ex, WebRequest request) {
        log.error("File upload failed: {}", ex.getMessage());
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), getPath(request), "File Upload Error", ex.getMessage()
        );
    }

    // ==================================================================================================================
    // == 403 FORBIDDEN GROUP ==
    // ==================================================================================================================

    /**
     * Handler khi user đã xác thực nhưng không có quyền truy cập tài nguyên
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied on path {}: {}", getPath(request), ex.getMessage());
        return new ErrorResponse(
                HttpStatus.FORBIDDEN.value(), getPath(request), "Access Denied",
                "You do not have permission to access this resource."
        );
    }

    /**
     * Handler cho lỗi nghiệp vụ về quyền (tùy chỉnh)
     */
    @ExceptionHandler(PermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN) // Sửa lại mã lỗi
    public ErrorResponse handlePermissionException(PermissionException ex, WebRequest request) {
        log.warn("Permission logic error: {}", ex.getMessage());
        return new ErrorResponse(
                HttpStatus.FORBIDDEN.value(), getPath(request), "Permission Denied", ex.getMessage()
        );
    }

    // ==================================================================================================================
    // == 404 NOT FOUND GROUP ==
    // ==================================================================================================================

    /**
     * Handler khi không tìm thấy API endpoint (URL không tồn tại)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoResourceFoundException(NoResourceFoundException ex, WebRequest request) {
        log.warn("404 NOT FOUND for path {}: {}", getPath(request), ex.getMessage());
        return new ErrorResponse(
                HttpStatus.NOT_FOUND.value(), getPath(request), "Not Found", "API endpoint không tồn tại."
        );
    }

    /**
     * Handler chung cho tất cả các lỗi "Resource Not Found" nghiệp vụ
     * GỘP TẤT CẢ CÁC EXCEPTION "NOT FOUND" VÀO ĐÂY
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage()); // Log ra thông báo cụ thể
        return new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                getPath(request),
                "Resource Not Found",
                ex.getMessage() // <--- SỬ DỤNG LẠI THÔNG BÁO CỤ THỂ
        );
    }

    // ==================================================================================================================
    // == 405 METHOD NOT ALLOWED ==
    // ==================================================================================================================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponse handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        String message = "Method '" + ex.getMethod() + "' is not supported. Supported methods are " + ex.getSupportedHttpMethods();
        log.warn("Method not allowed on path {}: {}", getPath(request), message);
        return new ErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED.value(), getPath(request), "Method Not Allowed", message
        );
    }

    // ==================================================================================================================
    // == 409 CONFLICT GROUP ==
    // ==================================================================================================================

    /**
     * Handler chung cho tất cả các lỗi "Resource Already Exists" nghiệp vụ
     * GỘP TẤT CẢ CÁC EXCEPTION "ALREADY EXISTS" VÀO ĐÂY
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleResourceAlreadyExists(RuntimeException ex, WebRequest request) {
        log.warn("Resource conflict: {}", ex.getMessage());
        return new ErrorResponse(
                HttpStatus.CONFLICT.value(), getPath(request), "Conflict", ex.getMessage()
        );
    }

    /**
     * Handler cho lỗi vi phạm ràng buộc dữ liệu từ Database (ví dụ: UNIQUE constraint)
     * Giữ riêng vì cách xử lý message có thể khác
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        String message = "Dữ liệu gửi lên vi phạm một ràng buộc.";
        String rootCause = ex.getMostSpecificCause().getMessage();

        if (rootCause.toLowerCase().contains("duplicate entry")) {
            message = "Dữ liệu đã tồn tại (vi phạm ràng buộc UNIQUE).";
        } else if (rootCause.toLowerCase().contains("foreign key constraint fails")) {
            message = "Dữ liệu không hợp lệ (vi phạm ràng buộc khóa ngoại).";
        }

        log.error("Data integrity violation on path {}: {}", getPath(request), rootCause);
        return new ErrorResponse(
                HttpStatus.CONFLICT.value(), getPath(request), "Data Conflict", message
        );
    }

    // ==================================================================================================================
    // == 500 INTERNAL SERVER ERROR ==
    // ==================================================================================================================

    /**
     * Handler "bắt tất cả" cho các lỗi không mong muốn
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllExceptions(Exception ex, WebRequest request) {
        log.error("An unexpected error occurred on path {}:", getPath(request), ex);
        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), getPath(request),
                "Internal Server Error",
                "An unexpected error occurred. Please contact support."
        );
    }
}