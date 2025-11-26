package models;

import org.json.JSONObject;

/**
 * 게시글 DTO
 */
public class Post {
    private int id;
    private String title;
    private String content;
    private String userId;
    private String username;
    private String createdAt;
    private String updatedAt;
    private int commentCount;
    private int viewCount;
    
    public Post() {}
    
    public Post(JSONObject json) {
        this.id = json.optInt("id", 0);
        this.title = json.optString("title", "");
        this.content = json.optString("content", "");
        this.userId = json.optString("user_id", "");
        this.username = json.optString("username", "");
        this.createdAt = json.optString("created_at", "");
        this.updatedAt = json.optString("updated_at", "");
        this.commentCount = json.optInt("comment_count", 0);
        this.viewCount = json.optInt("view_count", 0);
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
    
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("title", title);
        json.put("content", content);
        json.put("user_id", userId);
        return json;
    }
}

