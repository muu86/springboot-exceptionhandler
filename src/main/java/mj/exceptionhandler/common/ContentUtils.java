package mj.exceptionhandler.common;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.validation.ObjectError;

import mj.exceptionhandler.domain.Containable;

public class ContentUtils {
    
    private static final List<String> NOT_ALLOWED_WORDS = Arrays.asList(
        "politics",
        "terrorism",
        "murder");
        
    public static List<ObjectError> getContentErrorsFrom(Containable containable) {
        return Arrays.stream(containable.getContent().split(" "))
            .filter(NOT_ALLOWED_WORDS::contains)
            .map(notAllowedWord -> new ObjectError(notAllowedWord, "is not appropriate"))
            .collect(Collectors.toList());
    }
}