package mj.exceptionhandler.domain;

public class Post implements Containable {
    private String ownerName;
    private Long id;
    private String content;
    
    public Post(String ownerName, Long id, String content) {
        this.ownerName = ownerName;
        this.id = id;
        this.content = content;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwner(String ownerName) {
        this.ownerName = ownerName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}