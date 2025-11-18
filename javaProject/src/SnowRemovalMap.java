package javaProject;

import javax.swing.*;
import java.awt.*;
import java.awt.MediaTracker;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SnowRemovalMap extends JFrame {
  private static final String GOOGLE_MAPS_API_KEY = "AIzaSyAmqp1khCMy-wdof_llEq_XMPvdHO2mgmc";
  private static final String SNOWBOX_API_KEY = "596765645667796f37336a497a4c4c";
  private static final String SALT_API_KEY = "5147634f7667796f37367045665244";
  private static final String SNOWBOX_ENDPOINT =
      "https://api.odcloud.kr/api/15086762/v1/uddi:b2f84553-0a08-4d35-b444-f2a9b0324c04";
  private static final String LOCAL_JSON_PATH =
      "/Users/pingye_03/Downloads/서울시 제설함 위치정보.json";
  private static final boolean USE_LOCAL_FILE = true; // 로컬 파일 사용 여부
  private static final int MAP_WIDTH = 800;
  private static final int MAP_HEIGHT = 600;

  private double centerLat = 37.5665;
  private double centerLng = 126.9780;
  private int zoom = 12;

  private JLabel mapLabel;
  private JList<SnowBoxInfo> snowBoxListComponent;
  private DefaultListModel<SnowBoxInfo> listModel;
  private final List<Point2D.Double> snowBoxList = new ArrayList<>();
  
  // 제설함 정보를 저장하는 클래스
  private static class SnowBoxInfo {
    Point2D.Double location;
    String sboxNum;
    String mgcNm;
    String detlCn;
    
    SnowBoxInfo(Point2D.Double location, String sboxNum, String mgcNm, String detlCn) {
      this.location = location;
      this.sboxNum = sboxNum;
      this.mgcNm = mgcNm;
      this.detlCn = detlCn;
    }
    
    @Override
    public String toString() {
      return String.format("%s - %s", sboxNum, detlCn);
    }
  }
  
  private final List<SnowBoxInfo> snowBoxInfoList = new ArrayList<>();

  public SnowRemovalMap() {
    setTitle("서울시 제설함 지도 (Gson + Google Static Map)");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(MAP_WIDTH + 300, MAP_HEIGHT); // 사이드바 공간 추가
    setLocationRelativeTo(null);
    setLayout(new BorderLayout());

    // 제설함 리스트 초기화
    listModel = new DefaultListModel<>();
    snowBoxListComponent = new JList<>(listModel);
    snowBoxListComponent.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    snowBoxListComponent.setCellRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index,
          boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof SnowBoxInfo) {
          SnowBoxInfo info = (SnowBoxInfo) value;
          setText(String.format("<html><b>%s</b><br/>%s<br/><font size='-2' color='gray'>%s</font></html>",
              info.sboxNum, info.detlCn, info.mgcNm));
        }
        return this;
      }
    });
    
    // 리스트 클릭 이벤트: 해당 위치로 이동
    snowBoxListComponent.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        SnowBoxInfo selected = snowBoxListComponent.getSelectedValue();
        if (selected != null) {
          System.out.println("제설함 선택: " + selected.sboxNum);
          System.out.println("이동할 좌표: 위도=" + selected.location.y + ", 경도=" + selected.location.x);
          moveToLocation(selected.location.y, selected.location.x);
        }
      }
    });

    // 사이드바 패널 생성
    JPanel sidePanel = new JPanel(new BorderLayout());
    sidePanel.setPreferredSize(new Dimension(300, MAP_HEIGHT));
    sidePanel.setBorder(BorderFactory.createTitledBorder("제설함 목록"));
    
    JScrollPane scrollPane = new JScrollPane(snowBoxListComponent);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    sidePanel.add(scrollPane, BorderLayout.CENTER);
    
    // 검색 필드 추가
    JPanel searchPanel = new JPanel(new BorderLayout());
    JTextField searchField = new JTextField();
    searchField.setBorder(BorderFactory.createTitledBorder("검색"));
    searchField.addActionListener(e -> filterSnowBoxList(searchField.getText()));
    searchPanel.add(searchField, BorderLayout.CENTER);
    
    // 사용자 위치 찾기 버튼 추가
    findLocationButton = new JButton("📍 내 위치 찾기");
    findLocationButton.addActionListener(e -> findUserLocation());
    searchPanel.add(findLocationButton, BorderLayout.SOUTH);
    sidePanel.add(searchPanel, BorderLayout.NORTH);

    // MapPanel 생성 (지도 영역)
    JPanel mapPanel = new JPanel(new BorderLayout());
    mapLabel = new JLabel("지도를 불러오는 중...", SwingConstants.CENTER);
    mapPanel.add(mapLabel, BorderLayout.CENTER);
    
    // 지도와 사이드바를 나란히 배치
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidePanel, mapPanel);
    splitPane.setDividerLocation(300);
    splitPane.setResizeWeight(0.0);
    add(splitPane, BorderLayout.CENTER);

    loadSnowBoxData();
    loadMap();

    mapLabel.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            // 단일 클릭: 가장 가까운 제설함 정보 표시 (정보창)
            if (e.getClickCount() == 1) {
              showNearestSnowBoxInfo(e.getX(), e.getY());
            } else if (e.getClickCount() == 2) {
              // 더블클릭: 지도 이동
              int x = e.getX();
              int y = e.getY();
              int dx = x - MAP_WIDTH / 2;
              int dy = y - MAP_HEIGHT / 2;
              double scale = 0.002 / Math.pow(2, zoom - 10);
              centerLat -= dy * scale;
              centerLng += dx * scale;
              loadMap();
            }
          }
        });

    mapLabel.addMouseWheelListener(
        e -> {
          zoom -= e.getWheelRotation();
          zoom = Math.max(5, Math.min(18, zoom));
          loadMap();
        });

    setVisible(true);
  }

  private void loadMap() {
    SwingUtilities.invokeLater(
        () -> {
          try {
            if (snowBoxList.isEmpty()) {
              mapLabel.setText("제설함 데이터가 없습니다. (개수: 0)");
              System.out.println("⚠️ 제설함 리스트가 비어있습니다.");
              return;
            }
            
            StringBuilder markers = new StringBuilder();
            int markerCount = 0;
            for (Point2D.Double point : snowBoxList) {
              // Point2D.Double: x = 경도(longitude), y = 위도(latitude)
              double lat = point.y; // 위도
              double lng = point.x; // 경도
              
              // 좌표 범위 검증을 완화 (한국 전체 범위로 확대)
              // 위도: 33~43도 (한국 전체), 경도: 124~132도 (한국 전체)
              if (lat >= 33.0 && lat <= 43.0 && lng >= 124.0 && lng <= 132.0) {
                markers.append("&markers=color:blue%7C")
                    .append(lat)
                    .append(",")
                    .append(lng);
                markerCount++;
                // Google Static Map은 마커 개수 제한이 있으므로 최대 100개만 표시
                if (markerCount >= 100) break;
              } else if (markerCount < 5) {
                // 처음 몇 개 좌표가 범위 밖이면 출력
                System.out.println(String.format("범위 밖 좌표: (%.6f, %.6f)", lat, lng));
              }
            }
            
            System.out.println("지도에 표시할 마커 개수: " + markerCount + " / 전체: " + snowBoxList.size());
            System.out.println("지도 중심: (" + centerLat + ", " + centerLng + "), 줌: " + zoom);

            // 마커가 없어도 기본 지도는 표시
            String mapUrl =
                String.format(
                    "https://maps.googleapis.com/maps/api/staticmap?center=%.6f,%.6f&zoom=%d&size=%dx%d&maptype=roadmap%s&key=%s",
                    centerLat,
                    centerLng,
                    zoom,
                    MAP_WIDTH,
                    MAP_HEIGHT,
                    markers,
                    GOOGLE_MAPS_API_KEY);
            
            System.out.println("지도 URL 생성 완료 (길이: " + mapUrl.length() + ")");
            System.out.println("지도 URL (처음 200자): " + mapUrl.substring(0, Math.min(200, mapUrl.length())));

            // 이미지 로드 시도 (비동기로 로드하여 UI 블로킹 방지)
            new Thread(() -> {
              try {
                System.out.println("지도 이미지 다운로드 시작...");
                ImageIcon icon = new ImageIcon(new URL(mapUrl));
                
                // 이미지가 완전히 로드될 때까지 대기
                while (icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
                  try {
                    Thread.sleep(100);
                  } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                  }
                }
                
                SwingUtilities.invokeLater(() -> {
                  if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                    mapLabel.setIcon(icon);
                    mapLabel.setText(null);
                    System.out.println("지도 이미지 로드 성공: " + icon.getIconWidth() + "x" + icon.getIconHeight());
                  } else {
                    mapLabel.setText("지도 이미지 로드 실패: 이미지 크기가 0입니다.");
                    System.out.println("⚠️ 지도 이미지 크기가 0입니다.");
                  }
                });
              } catch (Exception imgEx) {
                SwingUtilities.invokeLater(() -> {
                  mapLabel.setText("지도 로드 실패: " + imgEx.getMessage());
                });
                System.err.println("지도 이미지 로드 오류: " + imgEx.getMessage());
                imgEx.printStackTrace();
              }
            }).start();
          } catch (Exception ex) {
            mapLabel.setText("지도 로드 실패: " + ex.getMessage());
            System.err.println("예상치 못한 오류: " + ex.getMessage());
            ex.printStackTrace();
          }
        });
  }

  private void loadSnowBoxData() {
    try {
      String json;
      if (USE_LOCAL_FILE) {
        // 로컬 JSON 파일 읽기
        System.out.println("로컬 JSON 파일 읽기: " + LOCAL_JSON_PATH);
        json = Files.readString(Paths.get(LOCAL_JSON_PATH), StandardCharsets.UTF_8);
        System.out.println("JSON 파일 크기: " + json.length() + " 문자");
      } else {
        // API 호출
        String url =
            SNOWBOX_ENDPOINT
                + "?page=1&perPage=1000&returnType=JSON&serviceKey="
                + URLEncoder.encode(SNOWBOX_API_KEY, StandardCharsets.UTF_8);

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int statusCode = connection.getResponseCode();
        InputStream stream =
            statusCode >= 200 && statusCode < 300
                ? connection.getInputStream()
                : connection.getErrorStream();

        json = readAll(stream);
        if (statusCode < 200 || statusCode >= 300) {
          throw new IllegalStateException(
              "API 호출 실패(" + statusCode + "): " + json.substring(0, Math.min(200, json.length())));
        }
      }
      parseSnowBoxResponse(json);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  private void parseSnowBoxResponse(String json) {
    JsonObject root = JsonParser.parseString(json).getAsJsonObject();
    JsonArray dataArray = null;
    
    // 로컬 파일은 "DATA" 키 사용, API는 "data" 또는 "records" 사용
    if (root.has("DATA")) {
      dataArray = root.getAsJsonArray("DATA");
    } else if (root.has("data")) {
      dataArray = root.getAsJsonArray("data");
    } else if (root.has("records")) {
      dataArray = root.getAsJsonArray("records");
    }

    snowBoxList.clear();
    if (dataArray == null) {
      System.out.println("⚠️ 데이터 배열을 찾을 수 없습니다.");
      return;
    }

    for (JsonElement element : dataArray) {
      JsonObject item = element.getAsJsonObject();
      
      // 위도/경도가 있는 경우 (API 응답)
      if (item.has("위도") && item.has("경도")) {
        try {
          double lat = Double.parseDouble(item.get("위도").getAsString());
          double lng = Double.parseDouble(item.get("경도").getAsString());
          Point2D.Double latLng = new Point2D.Double(lng, lat);
          snowBoxList.add(latLng);
          
          // 제설함 정보 저장
          String sboxNum = item.has("제설함번호") ? item.get("제설함번호").getAsString() : 
                          (item.has("sbox_num") ? item.get("sbox_num").getAsString() : "");
          String mgcNm = item.has("관리기관명") ? item.get("관리기관명").getAsString() : 
                        (item.has("mgc_nm") ? item.get("mgc_nm").getAsString() : "");
          String detlCn = item.has("위치상세정보") ? item.get("위치상세정보").getAsString() : 
                         (item.has("detl_cn") ? item.get("detl_cn").getAsString() : "");
          snowBoxInfoList.add(new SnowBoxInfo(latLng, sboxNum, mgcNm, detlCn));
        } catch (NumberFormatException ignored) {
        }
      }
      // TM 좌표가 있는 경우 (로컬 JSON 파일)
      else if (item.has("g2_xmin") && item.has("g2_ymin")) {
        try {
          long xmin = item.get("g2_xmin").getAsLong();
          long ymin = item.get("g2_ymin").getAsLong();
          // TM 좌표를 WGS84 위경도로 변환
          Point2D.Double latLng = convertTMToWGS84(xmin, ymin);
          snowBoxList.add(latLng);
          
          // 제설함 정보 저장
          String sboxNum = item.has("sbox_num") ? item.get("sbox_num").getAsString() : "";
          String mgcNm = item.has("mgc_nm") ? item.get("mgc_nm").getAsString() : "";
          String detlCn = item.has("detl_cn") ? item.get("detl_cn").getAsString() : "";
          snowBoxInfoList.add(new SnowBoxInfo(latLng, sboxNum, mgcNm, detlCn));
        } catch (Exception e) {
          System.err.println("좌표 변환 실패: " + e.getMessage());
        }
      }
    }
    
    System.out.println("제설함 개수: " + snowBoxList.size());
    
    // 리스트 모델 업데이트
    SwingUtilities.invokeLater(() -> {
      listModel.clear();
      for (SnowBoxInfo info : snowBoxInfoList) {
        listModel.addElement(info);
      }
      System.out.println("제설함 리스트 업데이트 완료: " + listModel.size() + "개");
    });
  }
  
  /**
   * 제설함 리스트 필터링
   */
  private void filterSnowBoxList(String searchText) {
    listModel.clear();
    String lowerSearch = searchText.toLowerCase();
    for (SnowBoxInfo info : snowBoxInfoList) {
      if (searchText.isEmpty() ||
          info.sboxNum.toLowerCase().contains(lowerSearch) ||
          info.mgcNm.toLowerCase().contains(lowerSearch) ||
          info.detlCn.toLowerCase().contains(lowerSearch)) {
        listModel.addElement(info);
      }
    }
  }
  
  /**
   * 지정된 위치로 지도 이동
   */
  private void moveToLocation(double lat, double lng) {
    System.out.println("지도 이동: (" + lat + ", " + lng + ")");
    
    // 좌표가 범위를 벗어나도 일단 이동 시도
    // (좌표 변환 공식이 부정확할 수 있으므로)
    centerLat = lat;
    centerLng = lng;
    zoom = 15; // 줌 레벨 증가하여 상세 보기
    
    // 지도 로드 (새 스레드에서 실행하여 UI 블로킹 방지)
    new Thread(() -> {
      loadMap();
    }).start();
  }
  
  /**
   * 사용자 위치 찾기 (IP 기반 위치 서비스 사용)
   */
  private void findUserLocation() {
    // IP 기반 위치 서비스 사용
    findLocationButton.setEnabled(false);
    findLocationButton.setText("위치 찾는 중...");
    
    new Thread(() -> {
      try {
        // IP 기반 위치 API 호출 (무료 서비스)
        String apiUrl = "http://ip-api.com/json/?fields=status,lat,lon,city,country";
        
        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        
        int statusCode = connection.getResponseCode();
        if (statusCode == 200) {
          String response = readAll(connection.getInputStream());
          JsonObject json = JsonParser.parseString(response).getAsJsonObject();
          
          if (json.has("status") && json.get("status").getAsString().equals("success")) {
            double lat = json.get("lat").getAsDouble();
            double lng = json.get("lon").getAsDouble();
            String city = json.has("city") ? json.get("city").getAsString() : "";
            String country = json.has("country") ? json.get("country").getAsString() : "";
            
            SwingUtilities.invokeLater(() -> {
              centerLat = lat;
              centerLng = lng;
              zoom = 13;
              
              String message = String.format(
                  "위치를 찾았습니다:\n\n도시: %s, %s\n좌표: (%.6f, %.6f)\n\n해당 위치로 이동합니다.",
                  city, country, lat, lng);
              
              JOptionPane.showMessageDialog(
                  this,
                  message,
                  "위치 찾기",
                  JOptionPane.INFORMATION_MESSAGE);
              
              loadMap();
              findLocationButton.setEnabled(true);
              findLocationButton.setText("📍 내 위치 찾기");
            });
            return;
          }
        }
      } catch (Exception e) {
        System.err.println("IP 기반 위치 찾기 실패: " + e.getMessage());
      }
      
      // 실패 시 기본 위치(서울시청)로 이동
      SwingUtilities.invokeLater(() -> {
        centerLat = 37.5665;
        centerLng = 126.9780;
        zoom = 13;
        
        JOptionPane.showMessageDialog(
            this,
            "IP 기반 위치를 찾을 수 없습니다.\n서울시청 위치로 이동합니다.\n\n참고: 더 정확한 위치를 원하시면\n브라우저 기반 웹 애플리케이션을 사용하세요.",
            "위치 찾기",
            JOptionPane.INFORMATION_MESSAGE);
        
        loadMap();
        findLocationButton.setEnabled(true);
        findLocationButton.setText("📍 내 위치 찾기");
      });
    }).start();
  }
  
  private JButton findLocationButton; // 버튼 참조를 저장하기 위한 필드
  
  /**
   * 클릭한 위치에서 가장 가까운 제설함 정보 표시 (정보창)
   */
  private void showNearestSnowBoxInfo(int clickX, int clickY) {
    if (snowBoxInfoList.isEmpty()) {
      return;
    }
    
    // 클릭한 화면 좌표를 지도 좌표로 변환
    double clickLat = centerLat - (clickY - MAP_HEIGHT / 2.0) * (0.002 / Math.pow(2, zoom - 10));
    double clickLng = centerLng + (clickX - MAP_WIDTH / 2.0) * (0.002 / Math.pow(2, zoom - 10)) / Math.cos(Math.toRadians(centerLat));
    
    // 가장 가까운 제설함 찾기
    SnowBoxInfo nearest = null;
    double minDistance = Double.MAX_VALUE;
    
    for (SnowBoxInfo info : snowBoxInfoList) {
      // 위경도 거리 계산
      double latDiff = info.location.y - clickLat;
      double lngDiff = info.location.x - clickLng;
      double distance = Math.sqrt(latDiff * latDiff + lngDiff * lngDiff);
      
      if (distance < minDistance) {
        minDistance = distance;
        nearest = info;
      }
    }
    
    // 거리가 가까우면 정보창 표시 (약 0.005도 = 약 500m)
    if (nearest != null && minDistance < 0.005) {
      showSnowBoxInfoWindow(nearest);
    }
  }
  
  /**
   * 제설함 정보창 표시
   */
  private void showSnowBoxInfoWindow(SnowBoxInfo info) {
    JDialog infoDialog = new JDialog(this, "제설함 정보", true);
    infoDialog.setSize(350, 250);
    infoDialog.setLocationRelativeTo(this);
    infoDialog.setLayout(new BorderLayout());
    
    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
    // 제설함 번호
    JLabel numLabel = new JLabel("<html><b>제설함 번호:</b> " + info.sboxNum + "</html>");
    numLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
    contentPanel.add(numLabel);
    contentPanel.add(Box.createVerticalStrut(10));
    
    // 관리기관
    JLabel mgcLabel = new JLabel("<html><b>관리기관:</b> " + info.mgcNm + "</html>");
    contentPanel.add(mgcLabel);
    contentPanel.add(Box.createVerticalStrut(10));
    
    // 위치
    JLabel detlLabel = new JLabel("<html><b>위치:</b><br/>" + info.detlCn + "</html>");
    contentPanel.add(detlLabel);
    contentPanel.add(Box.createVerticalStrut(10));
    
    // 좌표
    JLabel coordLabel = new JLabel(String.format(
        "<html><b>좌표:</b> (%.6f, %.6f)</html>",
        info.location.y, info.location.x));
    coordLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
    contentPanel.add(coordLabel);
    contentPanel.add(Box.createVerticalStrut(15));
    
    // 지도에서 보기 버튼
    JButton showOnMapButton = new JButton("지도에서 보기");
    showOnMapButton.addActionListener(e -> {
      moveToLocation(info.location.y, info.location.x);
      infoDialog.dispose();
    });
    contentPanel.add(showOnMapButton);
    
    infoDialog.add(contentPanel, BorderLayout.CENTER);
    
    // 닫기 버튼
    JButton closeButton = new JButton("닫기");
    closeButton.addActionListener(e -> infoDialog.dispose());
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(closeButton);
    infoDialog.add(buttonPanel, BorderLayout.SOUTH);
    
    infoDialog.setVisible(true);
  }
  
  /**
   * 클릭한 위치에서 가장 가까운 제설함 정보 표시 (기존 메서드 - 호환성 유지)
   */
  private void showNearestSnowBox(int clickX, int clickY) {
    if (snowBoxInfoList.isEmpty()) {
      JOptionPane.showMessageDialog(this, "제설함 정보가 없습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    
    // 클릭한 화면 좌표를 지도 좌표로 변환
    double clickLat = centerLat - (clickY - MAP_HEIGHT / 2.0) * (0.002 / Math.pow(2, zoom - 10));
    double clickLng = centerLng + (clickX - MAP_WIDTH / 2.0) * (0.002 / Math.pow(2, zoom - 10)) / Math.cos(Math.toRadians(centerLat));
    
    // 가장 가까운 제설함 찾기
    SnowBoxInfo nearest = null;
    double minDistance = Double.MAX_VALUE;
    
    for (SnowBoxInfo info : snowBoxInfoList) {
      // 위경도 거리 계산 (간단한 유클리드 거리)
      double latDiff = info.location.y - clickLat;
      double lngDiff = info.location.x - clickLng;
      double distance = Math.sqrt(latDiff * latDiff + lngDiff * lngDiff);
      
      if (distance < minDistance) {
        minDistance = distance;
        nearest = info;
      }
    }
    
    // 거리가 너무 멀면 무시 (약 0.01도 = 약 1km)
    if (nearest != null && minDistance < 0.01) {
      String message = String.format(
          "제설함 정보\n\n" +
          "번호: %s\n" +
          "관리기관: %s\n" +
          "위치: %s\n" +
          "좌표: (%.6f, %.6f)",
          nearest.sboxNum,
          nearest.mgcNm,
          nearest.detlCn,
          nearest.location.y,
          nearest.location.x
      );
      
      JOptionPane.showMessageDialog(
          this,
          message,
          "제설함 정보",
          JOptionPane.INFORMATION_MESSAGE
      );
    } else {
      JOptionPane.showMessageDialog(
          this,
          "클릭한 위치 근처에 제설함이 없습니다.\n\n더블클릭으로 제설함 정보를 확인할 수 있습니다.",
          "알림",
          JOptionPane.INFORMATION_MESSAGE
      );
    }
  }

  /**
   * TM 좌표(중부원점)를 WGS84 위경도로 변환
   * 한국 중부원점(EPSG:5186) → WGS84(EPSG:4326) 변환
   * @param x TM X 좌표 (g2_xmin) - 단위: 0.1mm (1/10000 미터)
   * @param y TM Y 좌표 (g2_ymin) - 단위: 0.1mm (1/10000 미터)
   * @return WGS84 위경도 (lat, lng)
   */
  private Point2D.Double convertTMToWGS84(long x, long y) {
    // TM 좌표 단위 확인: 원본 값이 190766121, 541751996
    // 서울시 실제 TM 좌표 범위: X 약 190000~200000, Y 약 540000~550000
    // 원본 값을 1000으로 나누면: 190766.121, 541751.996 (이것도 범위 밖)
    // 원본 값을 10000으로 나누면: 19076.6121, 54175.1996 (너무 작음)
    
    // 실제로는 원본 값이 이미 미터 단위일 가능성이 높음
    // 하지만 값이 너무 크므로, 실제로는 다른 좌표계일 수 있음
    // 공공데이터포털의 좌표 형식 확인 필요
    
    // 시도: 원본 값을 1000으로 나눔 (mm 단위 가정)
    double tmX = x / 1000.0; // 미터 단위
    double tmY = y / 1000.0; // 미터 단위
    
    // 중부원점 기준점
    double originX = 200000.0; // 미터
    double originY = 500000.0; // 미터
    
    // TM 좌표를 기준점 기준으로 변환 (미터 단위)
    double dx = tmX - originX;
    double dy = tmY - originY;
    
    // 서울시 중심 기준 근사 변환
    // 위도 1도 ≈ 111,000m
    // 경도 1도 ≈ 111,000m * cos(위도)
    double baseLat = 37.5665; // 서울시청 위도
    double baseLng = 126.9780; // 서울시청 경도
    
    // 위도 변환 (Y축, 남북) - dy가 음수면 남쪽, 양수면 북쪽
    double lat = baseLat + (dy / 111000.0);
    
    // 경도 변환 (X축, 동서) - dx가 음수면 서쪽, 양수면 동쪽
    double latRad = Math.toRadians(lat);
    double lng = baseLng + (dx / (111320.0 * Math.cos(latRad)));
    
    // 변환된 좌표 출력 (디버깅용, 처음 몇 개만)
    if (snowBoxList.size() < 5) {
      System.out.println(String.format("TM(%d, %d) -> 미터(%.2f, %.2f) -> dx=%.2f, dy=%.2f -> WGS84(%.6f, %.6f)", 
          x, y, tmX, tmY, dx, dy, lat, lng));
    }
    
    // 좌표 범위 검증
    if (lat < 37.0 || lat > 38.0 || lng < 126.0 || lng > 128.0) {
      System.err.println(String.format("⚠️ 변환된 좌표가 서울시 범위를 벗어남: (%.6f, %.6f)", lat, lng));
      // 범위를 벗어나도 일단 반환 (나중에 수정)
    }
    
    // Point2D.Double: x = 경도(longitude), y = 위도(latitude)
    return new Point2D.Double(lng, lat);
  }

  private String readAll(InputStream stream) throws IOException {
    if (stream == null) {
      return "";
    }
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
      return sb.toString();
    }
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(SnowRemovalMap::new);
  }
}
