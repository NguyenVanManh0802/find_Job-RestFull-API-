package vn.manh.findJob.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class PermissionException extends RuntimeException{
    public PermissionException(String message){super(message);}
}
