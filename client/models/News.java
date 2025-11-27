package models;

import java.util.Date;

/**
 * 뉴스 데이터 모델
 */
public class News {
    public String title;
    public String link;
    public Date pubDate;
    public String description;
    
    public News(String title, String link, Date pubDate, String description) {
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

