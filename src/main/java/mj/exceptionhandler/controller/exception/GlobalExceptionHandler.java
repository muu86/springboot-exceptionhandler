package mj.exceptionhandler.controller.exception;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.WebUtils;

import mj.exceptionhandler.domain.ApiError;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger LOGGER =  LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler({ UserNotFoundException.class, ContentNotAllowedException.class })
    public final ResponseEntity<ApiError> handleException(Exception exception, WebRequest request) {
        HttpHeaders headers = new HttpHeaders();
        
        LOGGER.error("Handling " + exception.getClass().getSimpleName() + " due to " + exception.getMessage());
        
        if (exception instanceof UserNotFoundException) {
            return handleUserNotFoundException((UserNotFoundException) exception, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
        } else if (exception instanceof ContentNotAllowedException) {
            return handleContentNotAllowedException((ContentNotAllowedException) exception, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
        } else {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Unknown exception type: {}", exception.getClass().getName());
            }
            
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            
            return handleExceptionInternal(exception, null, headers, status, request);
        }
    }
    
    protected ResponseEntity<ApiError> handleUserNotFoundException(UserNotFoundException exception,
                                                                HttpHeaders headers,
                                                                HttpStatus status,
                                                                WebRequest request) {
        List<String> errors = Collections.singletonList(exception.getMessage());
        return handleExceptionInternal(exception, new ApiError(errors), headers, status, request);
    }
    
    protected ResponseEntity<ApiError> handleContentNotAllowedException(ContentNotAllowedException exception,
                                                                    HttpHeaders headers,
                                                                    HttpStatus status,
                                                                    WebRequest request) {
        List<String> errorMessages = exception.getErrors()
            .stream()
            .map(contentError -> contentError.getObjectName() + " " + contentError.getDefaultMessage())
            .collect(Collectors.toList());
            
        return handleExceptionInternal(exception, new ApiError(errorMessages), headers, status, request);
    }
    
    protected ResponseEntity<ApiError> handleExceptionInternal(Exception exception,
                                                            ApiError body,
                                                            HttpHeaders headers,
                                                            HttpStatus status,
                                                            WebRequest request) {
                                                                
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, exception, WebRequest.SCOPE_REQUEST);
        }
        
        return new ResponseEntity<>(body, headers, status);
    }
}