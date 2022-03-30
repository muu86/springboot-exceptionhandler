package mj.exceptionhandler.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import mj.exceptionhandler.controller.exception.ContentNotAllowedException;
import mj.exceptionhandler.controller.exception.UserNotFoundException;
import mj.exceptionhandler.domain.Post;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest
public class ControllerTests {
    
    @Autowired
    private MockMvc mvc;

    @Test
    void nonExistingUser_getUser_handleNotFound() throws Exception {
        mvc.perform(get("/users/mj"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException));
    }
    
    @Test
    void notAllowedWords_createPost_handlerContentNotAllowedException() throws Exception {
        Post body = new Post("mj", 1L, "terrorism 11");
        
        mvc.perform(post("/users/mj/posts")
                    .content(asJsonString(body))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof ContentNotAllowedException));
    }
    
    @Test
    void allowedWords_createPost_201Create() throws Exception {
        Post body = new Post("mj", 1L, "hahah 11");
        
        mvc.perform(post("/users/mj/posts")
                    .content(asJsonString(body))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isCreated());
    }
    
    private String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}