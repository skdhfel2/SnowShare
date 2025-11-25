package components.news;

import java.util.Date;

/**
 * 뉴스 아이템 데이터 클래스
 */
public class NewsItem {
  public String title;
  public String link;
  public Date pubDate;
  public String description;
  
  public NewsItem(String title, String link, Date pubDate, String description) {
    this.title = title;
    this.link = link;
    this.pubDate = pubDate;
    this.description = description;
  }
  
  @Override
  public String toString() {
    return title;
  }
}

