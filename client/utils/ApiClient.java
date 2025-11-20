package utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 백엔드 API와 통신하는 HTTP 요청 클래스
 */
public class ApiClient {
    private static final String BASE_URL = "http://localhost:3000/api";
    
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
            
            // POST, PUT, DELETE 요청인 경우 데이터 전송
            if ((method.equals("POST") || method.equals("PUT") || method.equals("DELETE")) && data != null) {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = data.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
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
        } finally {
            conn.disconnect();
        }
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

