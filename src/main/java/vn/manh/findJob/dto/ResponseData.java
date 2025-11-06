package vn.manh.findJob.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

public class ResponseData<T> { //KIỂU DỮ LIỆU GENERICS :TRẢ VỀ BẤT KÌ DỮ LIỆU NÀO TA QUI ĐỊNH TRONG T
    private int status;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)  //dữ liệu trong data nêÚ null thì không hiêển thị ra
    private T data;

    //put,patch,delete
    public ResponseData(int status, String message) {
        this.status = status;
        this.message = message;
    }

    //get,post
    public ResponseData(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
