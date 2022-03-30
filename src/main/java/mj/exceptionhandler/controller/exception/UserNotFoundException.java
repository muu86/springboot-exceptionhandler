package mj.exceptionhandler.controller.exception;

@SuppressWarnings("serial")
public class UserNotFoundException extends RuntimeException {
    
    private String username;
    
    private UserNotFoundException(String username) {
        this.username = username;
    }
    
    public static UserNotFoundException createWith(String username) {
        return new UserNotFoundException(username);
    }
    
    @Override
    public String getMessage() {
        return "User `" + username + "` not found"; 
    }
}