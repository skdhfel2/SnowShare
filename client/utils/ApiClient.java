package utils;

import java.io.*;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 백엔드 API와 통신하는 HTTP 요청 클래스
 * 세션 쿠키를 자동으로 관리합니다.
 */
public class ApiClient {
    private static final String DEFAULT_BASE_URL = "http://localhost:3000/api";
    private static final String CONFIG_FILE = "config.properties";
    private static final String BASE_URL = loadBaseUrl();
    private static final CookieManager cookieManager = new CookieManager();
    
    /**
     * 설정 파일에서 API 기본 URL을 로드합니다.
     * 설정 파일이 없거나 속성이 없으면 기본값을 사용합니다.
     */
    private static String loadBaseUrl() {
        try {
            Properties props = new Properties();
            File configFile = new File(CONFIG_FILE);
            
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    props.load(fis);
                    String baseUrl = props.getProperty("api.base.url");
                    if (baseUrl != null && !baseUrl.trim().isEmpty()) {
                        return baseUrl.trim();
                    }
                }
            }
        } catch (IOException e) {
            // 설정 파일을 읽을 수 없으면 기본값 사용
            System.err.println("설정 파일을 읽을 수 없습니다. 기본 URL을 사용합니다: " + e.getMessage());
        }
        
        return DEFAULT_BASE_URL;
    }
    
    /**
     * GET 요청
     */
    public static JSONObject get(String endpoint) throws IOException {
        return sendRequest("GET", endpoint, null);
    }
    
    /**
     * POST 요청
     */
    public static JSONObject post(String endpoint, JSONObject data) throws IOException {
        return sendRequest("POST", endpoint, data);
    }
    
    /**
     * PUT 요청
     */
    public static JSONObject put(String endpoint, JSONObject data) throws IOException {
        return sendRequest("PUT", endpoint, data);
    }
    
    /**
     * DELETE 요청
     */
    public static JSONObject delete(String endpoint, JSONObject data) throws IOException {
        return sendRequest("DELETE", endpoint, data);
    }
    
    /**
     * HTTP 요청 전송
     */
    private static JSONObject sendRequest(String method, String endpoint, JSONObject data) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod(method);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            // 쿠키 추가 (세션 관리)
            URI uri = url.toURI();
            CookieStore cookieStore = cookieManager.getCookieStore();
            List<HttpCookie> cookies = cookieStore.get(uri);
            if (cookies != null && !cookies.isEmpty()) {
                StringBuilder cookieHeader = new StringBuilder();
                for (HttpCookie cookie : cookies) {
                    if (cookieHeader.length() > 0) {
                        cookieHeader.append("; ");
                    }
                    cookieHeader.append(cookie.toString());
                }
                conn.setRequestProperty("Cookie", cookieHeader.toString());
            }
            
            // POST, PUT, DELETE 요청인 경우 데이터 전송
            if ((method.equals("POST") || method.equals("PUT") || method.equals("DELETE")) && data != null) {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = data.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }
            
            // 응답에서 쿠키 저장
            String setCookieHeader = conn.getHeaderField("Set-Cookie");
            if (setCookieHeader != null) {
                List<String> cookieHeaders = conn.getHeaderFields().get("Set-Cookie");
                if (cookieHeaders != null) {
                    for (String cookieHeader : cookieHeaders) {
                        List<HttpCookie> httpCookies = HttpCookie.parse(cookieHeader);
                        for (HttpCookie cookie : httpCookies) {
                            cookieStore.add(uri, cookie);
                        }
                    }
                }
            }
            
            int responseCode = conn.getResponseCode();
            InputStream inputStream = responseCode >= 200 && responseCode < 300
                    ? conn.getInputStream()
                    : conn.getErrorStream();
            
            if (inputStream == null) {
                throw new IOException("응답 스트림이 null입니다.");
            }
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                
                if (response.length() == 0) {
                    return new JSONObject().put("success", responseCode >= 200 && responseCode < 300);
                }
                
                return new JSONObject(response.toString());
            }
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException("요청 처리 중 오류 발생: " + e.getMessage(), e);
        } finally {
            conn.disconnect();
        }
    }
    
    /**
     * 쿠키 초기화 (로그아웃 시 사용)
     */
    public static void clearCookies() {
        cookieManager.getCookieStore().removeAll();
    }
    
    /**
     * JSON 배열을 가져오는 GET 요청
     */
    public static JSONArray getArray(String endpoint) throws IOException {
        JSONObject response = get(endpoint);
        if (response.has("data") && response.get("data") instanceof JSONArray) {
            return response.getJSONArray("data");
        }
        return new JSONArray();
    }
}

