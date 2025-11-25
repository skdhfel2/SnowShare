package javaProject;

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
  private static final String GEMINI_API_KEY = "AIzaSyAIfS8qJyI1cyzpImD8mEKp4uNWZnwDfVU";
  private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=";
  
  /**
   * Google Gemini API를 사용하여 텍스트 요약
   */
  public String summarize(String text) throws Exception {
    String url = GEMINI_API_URL + GEMINI_API_KEY;
    
    // 요청 본문 생성
    String requestBody = String.format(
        "{\"contents\":[{\"parts\":[{\"text\":\"다음 뉴스 기사를 한국어로 간단하고 명확하게 3-5문장으로 요약해주세요:\\n\\n%s\"}]}]}",
        text.replace("\"", "\\\"").replace("\n", "\\n"));
    
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setRequestMethod("POST");
    connection.setRequestProperty("Content-Type", "application/json");
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
    InputStream inputStream = statusCode >= 200 && statusCode < 300
        ? connection.getInputStream()
        : connection.getErrorStream();
    
    String response = readAll(inputStream);
    
    if (statusCode < 200 || statusCode >= 300) {
      throw new Exception("API 호출 실패 (" + statusCode + "): " + response);
    }
    
    // JSON 파싱
    JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
    
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
    
    throw new Exception("API 응답에서 요약을 찾을 수 없습니다.");
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


