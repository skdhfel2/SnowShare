package utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * AI 요약 서비스 - 백엔드 서버를 통한 안전한 API 호출
 * 
 * ✅ 보안: API 키는 서버에만 존재, 클라이언트 코드에 노출 안 됨
 * ✅ 실무 패턴: 클라이언트 → 우리 서버 → 외부 API
 */
public class AISummarizer {
    // 기본 서버 URL (config.properties에서 로드)
    private static final String DEFAULT_BASE_URL = "https://snowshare-production.up.railway.app/api";
    private static final String CONFIG_FILE = "config.properties";
    private static final String API_BASE_URL = loadBaseUrl();
    private static final String SUMMARIZE_ENDPOINT = "/summarize";
    
    /**
     * 설정 파일에서 API 기본 URL을 로드합니다.
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
            System.err.println("설정 파일을 읽을 수 없습니다. 기본 URL을 사용합니다: " + e.getMessage());
        }
        
        return DEFAULT_BASE_URL;
    }
    
    /**
     * 백엔드 서버를 통해 텍스트 요약
     * 
     * @param text 요약할 텍스트
     * @return 요약된 텍스트
     * @throws Exception API 호출 실패 시
     */
    public String summarize(String text) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            throw new Exception("요약할 텍스트가 없습니다.");
        }
        
        String url = API_BASE_URL + SUMMARIZE_ENDPOINT;
        
        // 요청 본문 생성 (서버로 보낼 JSON)
        String requestBody = String.format("{\"text\":%s}", escapeJsonString(text));
        
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
        
        // 요청 본문 전송
        try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                connection.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(requestBody);
            writer.flush();
        }
        
        int statusCode = connection.getResponseCode();
        InputStream inputStream;
        
        if (statusCode >= 200 && statusCode < 300) {
            inputStream = connection.getInputStream();
        } else {
            inputStream = connection.getErrorStream();
        }
        
        String response = readAll(inputStream);
        
        if (statusCode < 200 || statusCode >= 300) {
            // 에러 응답에서 상세 정보 추출
            String errorMessage = "서버 API 호출 실패 (" + statusCode + ")";
            try {
                JsonObject errorJson = JsonParser.parseString(response).getAsJsonObject();
                if (errorJson.has("error")) {
                    errorMessage += ": " + errorJson.get("error").getAsString();
                } else {
                    errorMessage += ": " + response;
                }
            } catch (Exception e) {
                errorMessage += ": " + response;
            }
            throw new Exception(errorMessage);
        }
        
        if (response == null || response.trim().isEmpty()) {
            throw new Exception("서버 응답이 비어있습니다.");
        }
        
        // JSON 파싱
        JsonObject jsonResponse;
        try {
            jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        } catch (Exception e) {
            throw new Exception("JSON 파싱 실패: " + e.getMessage() + "\n응답: " + response);
        }
        
        // 서버 응답 구조: { "success": true, "summary": "요약 텍스트" }
        if (jsonResponse.has("success") && jsonResponse.get("success").getAsBoolean()) {
            if (jsonResponse.has("summary")) {
                return jsonResponse.get("summary").getAsString();
            }
        }
        
        // 응답 구조가 예상과 다를 때
        throw new Exception("서버 응답에서 요약을 찾을 수 없습니다. 응답: " + response);
    }
    
    /**
     * JSON 문자열로 변환 (따옴표로 감싸고 이스케이프 처리)
     */
    private String escapeJsonString(String text) {
        if (text == null) return "\"\"";
        String escaped = text.replace("\\", "\\\\")
                             .replace("\"", "\\\"")
                             .replace("\n", "\\n")
                             .replace("\r", "\\r")
                             .replace("\t", "\\t");
        return "\"" + escaped + "\"";
    }
    
    /**
     * InputStream에서 모든 내용 읽기
     */
    private String readAll(InputStream stream) throws Exception {
        if (stream == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}

