package mj.exceptionhandler.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mj.exceptionhandler.common.ContentUtils;
import mj.exceptionhandler.controller.exception.ContentNotAllowedException;
import mj.exceptionhandler.domain.Post;

@RestController
@RequestMapping("/users/{username}/posts")
public class PostController {
    
    @PostMapping
    public ResponseEntity<Post> create(@PathVariable String username, @RequestBody Post post) throws ContentNotAllowedException {
        List<ObjectError> contentNotAllowedErrors = ContentUtils.getContentErrorsFrom(post);
        
        if (!contentNotAllowedErrors.isEmpty()) {
            throw ContentNotAllowedException.createWith(contentNotAllowedErrors);
        }
        
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}