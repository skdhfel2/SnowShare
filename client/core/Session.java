package core;

/**
 * 로그인한 사용자 정보를 저장하는 클래스
 * - userId
 * - token
 */
public class Session {
    private static Session instance;
    private String userId;
    private String token;
    private boolean isLoggedIn;
    
    private Session() {
        this.isLoggedIn = false;
    }
    
    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }
    
    /**
     * 로그인 정보 저장
     */
    public void login(String userId, String token) {
        this.userId = userId;
        this.token = token;
        this.isLoggedIn = true;
    }
    
    /**
     * 로그아웃
     */
    public void logout() {
        this.userId = null;
        this.token = null;
        this.isLoggedIn = false;
    }
    
    /**
     * 로그인 여부 확인
     */
    public boolean isLoggedIn() {
        return isLoggedIn;
    }
    
    /**
     * 사용자 ID 반환
     */
    public String getUserId() {
        return userId;
    }
    
    /**
     * 토큰 반환
     */
    public String getToken() {
        return token;
    }
}

