package mj.exceptionhandler.controller.exception;

import java.util.List;

import org.springframework.validation.ObjectError;

@SuppressWarnings("serial")
public class ContentNotAllowedException extends RuntimeException {
    
    List<ObjectError> errors;
    
    private ContentNotAllowedException(List<ObjectError> errors) {
        this.errors = errors;
    }
    
    public static ContentNotAllowedException createWith(List<ObjectError> errors) {
        return new ContentNotAllowedException(errors);
    }
    
    public List<ObjectError> getErrors() {
        return errors;
    }
}