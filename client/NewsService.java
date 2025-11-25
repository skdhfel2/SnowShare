package javaProject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RSS 뉴스 서비스 - 뉴스 가져오기 및 필터링
 */
public class NewsService {
  private static final String[] FILTER_KEYWORDS = {"폭설", "한파", "제설"};
  
  /**
   * Google 뉴스 RSS 피드에서 뉴스 가져오기
   */
  public List<NewsItem> fetchNews(String query) {
    List<NewsItem> allNews = new ArrayList<>();
    
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
      
      // XML 파싱
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(inputStream);
      
      // RSS 아이템 추출
      NodeList itemList = document.getElementsByTagName("item");
      
      for (int i = 0; i < itemList.getLength(); i++) {
        Node itemNode = itemList.item(i);
        
        if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
          Element itemElement = (Element) itemNode;
          
          String title = getElementText(itemElement, "title");
          String link = getElementText(itemElement, "link");
          String pubDateStr = getElementText(itemElement, "pubDate");
          String description = getElementText(itemElement, "description");
          
          Date pubDate = parseRSSDate(pubDateStr);
          
          NewsItem newsItem = new NewsItem(title, link, pubDate, description);
          allNews.add(newsItem);
        }
      }
      
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
  private List<NewsItem> filterNews(List<NewsItem> newsList, String[] keywords) {
    List<NewsItem> filteredNews = new ArrayList<>();
    
    for (NewsItem news : newsList) {
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
  
  /**
   * XML 요소에서 텍스트 추출
   */
  private String getElementText(Element parent, String tagName) {
    NodeList nodeList = parent.getElementsByTagName(tagName);
    if (nodeList.getLength() > 0) {
      Node node = nodeList.item(0);
      if (node != null && node.getFirstChild() != null) {
        return node.getFirstChild().getNodeValue();
      }
    }
    return "";
  }
  
  /**
   * RSS 날짜 형식 파싱
   */
  private Date parseRSSDate(String dateStr) {
    if (dateStr == null || dateStr.isEmpty()) {
      return new Date();
    }
    
    try {
      SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
      return format.parse(dateStr);
    } catch (Exception e) {
      return new Date();
    }
  }
}


