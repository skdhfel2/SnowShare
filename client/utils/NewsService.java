package utils;

import models.News;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * RSS 뉴스 서비스 - 뉴스 가져오기 및 필터링
 */
public class NewsService {
    private static final String[] FILTER_KEYWORDS = {"폭설", "한파", "제설"};
    
    /**
     * Google 뉴스 RSS 피드에서 뉴스 가져오기
     */
    public List<News> fetchNews(String query) {
        List<News> allNews = new ArrayList<>();
        
        try {
            String rssUrl = String.format(
                    "https://news.google.com/rss/search?q=%s&hl=ko&gl=KR&ceid=KR:ko",
                    URLEncoder.encode(query, StandardCharsets.UTF_8));
            
            HttpURLConnection connection = (HttpURLConnection) new URL(rssUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            InputStream inputStream = connection.getInputStream();
            
            // RSS 파싱
            allNews = RssParser.parseRssFeed(inputStream);
            
            inputStream.close();
            connection.disconnect();
            
        } catch (Exception e) {
            System.err.println("RSS 피드 가져오기 오류: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 키워드 필터링
        return filterNews(allNews, FILTER_KEYWORDS);
    }
    
    /**
     * 키워드로 뉴스 필터링
     */
    private List<News> filterNews(List<News> newsList, String[] keywords) {
        List<News> filteredNews = new ArrayList<>();
        
        for (News news : newsList) {
            String title = news.title.toLowerCase();
            String description = news.description != null ? news.description.toLowerCase() : "";
            
            for (String keyword : keywords) {
                if (title.contains(keyword.toLowerCase()) || description.contains(keyword.toLowerCase())) {
                    filteredNews.add(news);
                    break;
                }
            }
        }
        
        return filteredNews;
    }
}

