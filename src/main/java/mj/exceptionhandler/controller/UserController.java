package mj.exceptionhandler.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mj.exceptionhandler.controller.exception.UserNotFoundException;
import mj.exceptionhandler.domain.User;

@RestController
@RequestMapping("/users")
public class UserController {
    
    @GetMapping("/{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) throws UserNotFoundException {
        throw UserNotFoundException.createWith(username);    
    }
    
}