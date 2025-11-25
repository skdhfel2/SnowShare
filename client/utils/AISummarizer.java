package utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * AI 요약 서비스 - Google Gemini API 연동
 */
public class AISummarizer {
    private static final String GEMINI_API_KEY = "AIzaSyASUQPkWCztalunI20N_ARsCmU1oa4h6-w";
    // v1 API에서 사용 가능한 모델: gemini-pro 사용
    private static final String GEMINI_API_URL =
    "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=";

    
    /**
     * Google Gemini API를 사용하여 텍스트 요약
     */
    public String summarize(String text) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            throw new Exception("요약할 텍스트가 없습니다.");
        }
        
        String url = GEMINI_API_URL + GEMINI_API_KEY;
        
        // 텍스트 이스케이프 처리 (JSON 안전하게 만들기)
        String escapedText = escapeJsonString(text);
        
        // 요청 본문 생성
        String requestBody = String.format(
                "{\"contents\":[{\"parts\":[{\"text\":\"다음 뉴스 기사를 한국어로 간단하고 명확하게 3-5문장으로 요약해주세요:\\n\\n%s\"}]}]}",
                escapedText);
        
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
            String errorMessage = "API 호출 실패 (" + statusCode + ")";
            try {
                JsonObject errorJson = JsonParser.parseString(response).getAsJsonObject();
                if (errorJson.has("error")) {
                    JsonObject error = errorJson.getAsJsonObject("error");
                    if (error.has("message")) {
                        errorMessage += ": " + error.get("message").getAsString();
                    }
                } else {
                    errorMessage += ": " + response;
                }
            } catch (Exception e) {
                errorMessage += ": " + response;
            }
            throw new Exception(errorMessage);
        }
        
        if (response == null || response.trim().isEmpty()) {
            throw new Exception("API 응답이 비어있습니다.");
        }
        
        // JSON 파싱
        JsonObject jsonResponse;
        try {
            jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        } catch (Exception e) {
            throw new Exception("JSON 파싱 실패: " + e.getMessage() + "\n응답: " + response);
        }
        
        // 응답 구조 확인
        if (jsonResponse.has("candidates") && jsonResponse.getAsJsonArray("candidates").size() > 0) {
            JsonObject candidate = jsonResponse.getAsJsonArray("candidates").get(0).getAsJsonObject();
            if (candidate.has("content")) {
                JsonObject content = candidate.getAsJsonObject("content");
                if (content.has("parts") && content.getAsJsonArray("parts").size() > 0) {
                    JsonObject part = content.getAsJsonArray("parts").get(0).getAsJsonObject();
                    if (part.has("text")) {
                        return part.get("text").getAsString();
                    }
                }
            }
        }
        
        // 응답 구조가 예상과 다를 때 전체 응답 포함
        throw new Exception("API 응답에서 요약을 찾을 수 없습니다. 응답: " + response);
    }
    
    /**
     * JSON 문자열 이스케이프 처리
     */
    private String escapeJsonString(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
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

