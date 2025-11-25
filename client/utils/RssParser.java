package utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import models.News;

/**
 * RSS 피드 파서 유틸리티
 */
public class RssParser {
    /**
     * RSS XML 문서를 파싱하여 News 리스트로 변환
     */
    public static List<News> parseRssFeed(InputStream inputStream) throws Exception {
        List<News> newsList = new ArrayList<>();
        
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
                
                News newsItem = new News(title, link, pubDate, description);
                newsList.add(newsItem);
            }
        }
        
        return newsList;
    }
    
    /**
     * XML 요소에서 텍스트 추출
     */
    private static String getElementText(Element parent, String tagName) {
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
    private static Date parseRSSDate(String dateStr) {
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

