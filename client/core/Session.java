package core;

/**
 * 로그인한 사용자 정보를 저장하는 클래스
 * - userId
 * - username
 * 세션 기반 인증을 사용하므로 쿠키로 관리됩니다.
 */
public class Session {
    private static Session instance;
    private String userId;
    private String username;
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
    public void login(String userId, String username) {
        this.userId = userId;
        this.username = username;
        this.isLoggedIn = true;
    }
    
    /**
     * 로그아웃
     */
    public void logout() {
        this.userId = null;
        this.username = null;
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
     * 사용자명 반환
     */
    public String getUsername() {
        return username;
    }
}

