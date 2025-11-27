package models;

import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

/**
 * 댓글 DTO
 */
public class Comment {
    private int id;
    private int postId;
    private String postType;
    private String userId;
    private String username;
    private String content;
    private Integer parentCommentId;
    private String createdAt;
    private Integer rating; // 제설함 댓글인 경우 별점
    private List<Comment> replies;
    
    public Comment() {
        this.replies = new ArrayList<>();
    }
    
    public Comment(JSONObject json) {
        this();
        this.id = json.optInt("id", 0);
        this.postId = json.optInt("post_id", 0);
        this.postType = json.optString("post_type", "post");
        this.userId = json.optString("user_id", "");
        this.username = json.optString("username", "");
        this.content = json.optString("content", "");
        if (!json.isNull("parent_comment_id")) {
            this.parentCommentId = json.optInt("parent_comment_id", 0);
        }
        this.createdAt = json.optString("created_at", "");
        if (!json.isNull("rating")) {
            this.rating = json.optInt("rating", 0);
        }

        // 대댓글(replies) 파싱 (있을 수도 있고 없을 수도 있음)
        JSONArray repliesArray = json.optJSONArray("replies");
        if (repliesArray != null) {
            for (int i = 0; i < repliesArray.length(); i++) {
                JSONObject childJson = repliesArray.optJSONObject(i);
                if (childJson != null) {
                    this.replies.add(new Comment(childJson));
                }
            }
        }
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }
    
    public String getPostType() { return postType; }
    public void setPostType(String postType) { this.postType = postType; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Integer getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(Integer parentCommentId) { this.parentCommentId = parentCommentId; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public List<Comment> getReplies() { return replies; }
    public void setReplies(List<Comment> replies) { this.replies = replies; }
    
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("post_id", postId);
        json.put("post_type", postType);
        json.put("user_id", userId);
        json.put("content", content);
        if (parentCommentId != null) {
            json.put("parent_comment_id", parentCommentId);
        }
        return json;
    }
}

