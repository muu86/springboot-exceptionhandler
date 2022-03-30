### Springboot @ExceptionHandler, @ControllerAdvice

#### ExceptionHandler  

@ExceptionHandler  
컨트롤러 레벨에서 발생하는 모든 예외를 처리할 수 있다.  
어노테이션이 설정된 해당 컨트롤러에서 발생하는 예외만 처리하므로 각 컨트롤러 마다 예외 처리 로직이 분산되는 것이 단점.  
모든 컨트롤러가 BaseController 를 상속하도록 해서 BaseController 에서 모든 예외 처리를 할 수도 있겠지만 좋은 방법은 아니다.  
컨트롤러가 이미 다른 클래스를 상속하고 있는 경우 이미 형성된 상속 체계에 예외 처리를 위한 BaseController를 끼워 넣는게 쉽지 않다.  


```java
@RestController
@RequestMapping("/users")
public class UserController {
    
    @GetMapping("/{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) throws UserNotFoundException {
        throw UserNotFoundException.createWith(username);    
    }
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFoundException(UserNotFoundException exception) {
        List<String> errors = Collections.singletonList(exception.getMessage());
        
        return new ResponseEntity<>(new ApiError(errors), HttpStatus.NOT_FOUND);
    }
}
```

> **Collections.singletonList(T t)**  
하나의 객체만 요소로 가지는 불변 리스트(SingletonList)를 리턴한다.  
리스트를 수정하는 add 메소드 등을 사용할 수 없다.  
SingletonList 가 필요한 경우: <https://stackoverflow.com/questions/1239579/helper-to-remove-null-references-in-a-java-list/1239631#1239631>  
<https://stackoverflow.com/questions/4801794/use-of-javas-collections-singletonlist>  

#### HandlerExceptionResolver(Interface)
예외를 한 곳에서 처리하기 위한 방법 중 하나.  

```java
HandlerExceptionResolver.resolveException(HttpServletRequest request, 
                                            HttpServletResponse response, 
                                            Object handler, Exception ex)
```
                                        
파라미터로 주어진 예외를 처리하고 **ModelAndView** 를 리턴함.  
인터페이스르 구현한 클래스를 컴포넌트로 등록하면 원하는 예외에 대한 처리를 할 수 있다.  
**HttpServletRequest**를 직접 다뤄야 하는 단점.  
**ModelAndView**를 리턴하므로 REST 서비스에 적용은 완벽하지 않다는 단점.  

#### ControllerAdvice  

@ControllerAdvice  
하나의 클래스가 애플리케이션 전체 컨트롤러에서 발생하는 예외를 처리할 수 있다.  

```java
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

```

#### ResponseStatusException

#### Testing

```java

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
```