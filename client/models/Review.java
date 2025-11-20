package models;

import org.json.JSONObject;

/**
 * 후기 DTO
 */
public class Review {
    private int id;
    private int saltboxId;
    private String userId;
    private String username;
    private int rating;
    private String content;
    private String createdAt;
    
    public Review() {}
    
    public Review(JSONObject json) {
        this.id = json.optInt("id", 0);
        this.saltboxId = json.optInt("saltbox_id", 0);
        this.userId = json.optString("user_id", "");
        this.username = json.optString("username", "");
        this.rating = json.optInt("rating", 0);
        this.content = json.optString("content", "");
        this.createdAt = json.optString("created_at", "");
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getSaltboxId() { return saltboxId; }
    public void setSaltboxId(int saltboxId) { this.saltboxId = saltboxId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("saltbox_id", saltboxId);
        json.put("user_id", userId);
        json.put("rating", rating);
        json.put("content", content);
        return json;
    }
}

