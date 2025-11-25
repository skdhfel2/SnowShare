package components.map;

import core.BasePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

public class MapPanel extends BasePanel {

  private static final String GOOGLE_MAPS_API_KEY = "AIzaSyAmqp1khCMy-wdof_llEq_XMPvdHO2mgmc";
  private static final String SNOWBOX_API_KEY = "596765645667796f37336a497a4c4c";
  private static final String SNOWBOX_ENDPOINT = "https://api.odcloud.kr/api/15086762/v1/uddi:b2f84553-0a08-4d35-b444-f2a9b0324c04";

  private static final String LOCAL_JSON_PATH = "public/data/seoul_snowbox_location.json";
  private static final boolean USE_LOCAL_FILE = true;

  private static final int MAP_WIDTH = 800;
  private static final int MAP_HEIGHT = 600;

  private double centerLat = 37.5665;
  private double centerLng = 126.9780;
  private int zoom = 16;

  private JLabel mapLabel;
  private JList<SnowBoxInfo> snowBoxListComponent;
  private DefaultListModel<SnowBoxInfo> listModel;
  private final List<Point2D.Double> snowBoxList = new ArrayList<>();

  private Point2D.Double selectedMarker = null;

  private final CoordinateConverter coordConverter;

  // 제설함 정보 구조체
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

  public MapPanel() {
    coordConverter = new CoordinateConverter();
    setLayout(new BorderLayout());

    // 제설함 리스트 UI 구성
    listModel = new DefaultListModel<>();
    snowBoxListComponent = new JList<>(listModel);
    snowBoxListComponent.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    snowBoxListComponent.setCellRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
          boolean cellHasFocus) {

        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof SnowBoxInfo) {
          SnowBoxInfo info = (SnowBoxInfo) value;
          setText(String.format("<html><b>%s</b><br/>%s<br/><font size='-2' color='gray'>%s</font></html>",
              info.sboxNum, info.detlCn, info.mgcNm));
        }
        return this;
      }
    });

    // 리스트 선택 시 해당 위치로 이동
    snowBoxListComponent.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        SnowBoxInfo selected = snowBoxListComponent.getSelectedValue();
        if (selected != null) {
          selectedMarker = selected.location;
          moveToLocation(selected.location.y, selected.location.x);
          loadMap();
        }
      }
    });

    snowBoxListComponent.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        SnowBoxInfo selected = snowBoxListComponent.getSelectedValue();
        if (selected != null) {
          selectedMarker = selected.location;
          loadMap();
        }
      }
    });

    // 사이드바 UI
    JPanel sidePanel = new JPanel(new BorderLayout());
    sidePanel.setPreferredSize(new Dimension(300, MAP_HEIGHT));
    sidePanel.setBorder(BorderFactory.createTitledBorder("제설함 목록"));

    JScrollPane scrollPane = new JScrollPane(snowBoxListComponent);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    sidePanel.add(scrollPane, BorderLayout.CENTER);

    // 검색 기능
    JPanel searchPanel = new JPanel(new BorderLayout());
    JTextField searchField = new JTextField();
    searchField.setBorder(BorderFactory.createTitledBorder("검색"));
    searchField.addActionListener(e -> filterSnowBoxList(searchField.getText()));
    searchPanel.add(searchField, BorderLayout.CENTER);
    sidePanel.add(searchPanel, BorderLayout.NORTH);

    mapLabel = new JLabel("지도를 불러오는 중...", SwingConstants.CENTER);

    JPanel mapPanel = new JPanel(new BorderLayout());
    mapPanel.add(mapLabel, BorderLayout.CENTER);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidePanel, mapPanel);
    splitPane.setDividerLocation(300);
    splitPane.setResizeWeight(0.0);
    add(splitPane, BorderLayout.CENTER);

    loadSnowBoxData();
    loadMap();

    // 지도 클릭 이벤트 (제설함 정보 표시)
    mapLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 1) {
          showNearestSnowBoxInfo(e.getX(), e.getY());
        } else if (e.getClickCount() == 2) {
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

    // 줌 기능
    mapLabel.addMouseWheelListener(e -> {
      zoom -= e.getWheelRotation();
      zoom = Math.max(5, Math.min(18, zoom));
      loadMap();
    });
  }

  // 지도 로딩
  private void loadMap() {
    SwingUtilities.invokeLater(() -> {
      try {
        if (snowBoxList.isEmpty()) {
          mapLabel.setText("제설함 데이터가 없습니다.");
          return;
        }

        List<Point2D.Double> sorted = new ArrayList<>(snowBoxList);
        sorted.sort((p1, p2) -> {
          double d1 = Math.pow(p1.y - centerLat, 2) + Math.pow(p1.x - centerLng, 2);
          double d2 = Math.pow(p2.y - centerLat, 2) + Math.pow(p2.x - centerLng, 2);
          return Double.compare(d1, d2);
        });

        StringBuilder markers = new StringBuilder();
        int count = 0;

        for (Point2D.Double p : sorted) {
          double lat = p.y;
          double lng = p.x;

          if (selectedMarker != null && p.equals(selectedMarker)) {
            continue;
          }

          if (lat >= 33 && lat <= 43 && lng >= 124 && lng <= 132) {
            markers.append("&markers=color:blue%7C").append(String.format("%.6f,%.6f", lat, lng));

            count++;
            if (count >= 70)
              break;
          }
        }

        String selectedMarkerParam = "";
        if (selectedMarker != null) {
          selectedMarkerParam = "&markers=color:red%7C"
              + String.format("%.6f,%.6f", selectedMarker.y, selectedMarker.x);
        }

        String url = String.format(
            "https://maps.googleapis.com/maps/api/staticmap?center=%.6f,%.6f&zoom=%d&size=%dx%d&maptype=roadmap%s%s&key=%s",
            centerLat, centerLng, zoom, MAP_WIDTH, MAP_HEIGHT, markers.toString(), selectedMarkerParam,
            GOOGLE_MAPS_API_KEY);

        new Thread(() -> {
          try {
            ImageIcon icon = new ImageIcon(new URL(url));
            while (icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
              Thread.sleep(100);
            }

            SwingUtilities.invokeLater(() -> {
              if (icon.getIconWidth() > 0) {
                mapLabel.setIcon(icon);
                mapLabel.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
                mapLabel.setSize(icon.getIconWidth(), icon.getIconHeight());
                mapLabel.setMinimumSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
                mapLabel.setMaximumSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
                mapLabel.revalidate();
                mapLabel.repaint();
                mapLabel.setText(null);
              } else {
                mapLabel.setText("지도 로드 실패");
              }
            });
          } catch (Exception ignored) {
          }
        }).start();

      } catch (Exception ex) {
        ex.printStackTrace();
      }
    });
  }

  // 제설함 JSON 데이터 로드
  private void loadSnowBoxData() {
    try {
      String json;

      if (USE_LOCAL_FILE) {
        // 실행 위치에 따라 경로 동적 찾기
        String jsonPath = findJsonPath(); 
        json = readAll(Files.newInputStream(Paths.get(jsonPath)));
      } else {
        String url = SNOWBOX_ENDPOINT + "?page=1&perPage=1000&returnType=JSON&serviceKey="
            + URLEncoder.encode(SNOWBOX_API_KEY, "UTF-8");

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        InputStream stream = conn.getResponseCode() < 300 ? conn.getInputStream() : conn.getErrorStream();
        json = readAll(stream);
      }

      parseSnowBoxResponse(json);

    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  // 제설함 JSON 파싱 및 정보 저장
  private void parseSnowBoxResponse(String json) {
    JsonObject root = JsonParser.parseString(json).getAsJsonObject();
    JsonArray dataArray = null;

    if (root.has("DATA"))
      dataArray = root.getAsJsonArray("DATA");
    else if (root.has("data"))
      dataArray = root.getAsJsonArray("data");
    else if (root.has("records"))
      dataArray = root.getAsJsonArray("records");

    snowBoxList.clear();
    if (dataArray == null)
      return;

    for (JsonElement el : dataArray) {
      JsonObject item = el.getAsJsonObject();

      if (item.has("위도") && item.has("경도")) {
        try {
          double lat = Double.parseDouble(item.get("위도").getAsString());
          double lng = Double.parseDouble(item.get("경도").getAsString());

          Point2D.Double latLng = new Point2D.Double(lng, lat);
          snowBoxList.add(latLng);

          String sboxNum = item.has("제설함번호") ? item.get("제설함번호").getAsString()
              : (item.has("sbox_num") ? item.get("sbox_num").getAsString() : "");
          String mgcNm = item.has("관리기관명") ? item.get("관리기관명").getAsString()
              : (item.has("mgc_nm") ? item.get("mgc_nm").getAsString() : "");
          String detlCn = item.has("위치상세정보") ? item.get("위치상세정보").getAsString()
              : (item.has("detl_cn") ? item.get("detl_cn").getAsString() : "");

          snowBoxInfoList.add(new SnowBoxInfo(latLng, sboxNum, mgcNm, detlCn));

        } catch (Exception ignored) {
        }
      }

      else if (item.has("g2_xmin") && item.has("g2_ymin")) {
        try {
          long x = item.get("g2_xmin").getAsLong();
          long y = item.get("g2_ymin").getAsLong();

          double tmX = x / 1000.0;
          double tmY = y / 1000.0;

          Point2D.Double latLng = coordConverter.convert(tmX, tmY);
          snowBoxList.add(latLng);

          String sboxNum = item.has("sbox_num") ? item.get("sbox_num").getAsString() : "";
          String mgcNm = item.has("mgc_nm") ? item.get("mgc_nm").getAsString() : "";
          String detlCn = item.has("detl_cn") ? item.get("detl_cn").getAsString() : "";

          snowBoxInfoList.add(new SnowBoxInfo(latLng, sboxNum, mgcNm, detlCn));

        } catch (Exception ignored) {
        }
      }
    }

    SwingUtilities.invokeLater(() -> {
      listModel.clear();
      for (SnowBoxInfo info : snowBoxInfoList)
        listModel.addElement(info);
    });
  }

  // 검색 기능
  private void filterSnowBoxList(String searchText) {
    listModel.clear();
    String s = searchText.toLowerCase();

    for (SnowBoxInfo info : snowBoxInfoList) {
      if (searchText.isEmpty() || info.sboxNum.toLowerCase().contains(s) || info.mgcNm.toLowerCase().contains(s)
          || info.detlCn.toLowerCase().contains(s)) {

        listModel.addElement(info);
      }
    }
  }

  // 지도 중심 이동 기능
  private void moveToLocation(double lat, double lng) {
    centerLat = lat;
    centerLng = lng;
    zoom = 16;
    new Thread(() -> loadMap()).start();
  }

  private double mapScale() {
    return 256.0 * Math.pow(2.0, zoom);
  }

  private double lngToPixelX(double lng) {
    double scale = mapScale();
    return (lng + 180.0) / 360.0 * scale;
  }

  private double latToPixelY(double lat) {
    double scale = mapScale();
    double sinLat = Math.sin(Math.toRadians(lat));
    double y = 0.5 - (Math.log((1.0 + sinLat) / (1.0 - sinLat)) / (4.0 * Math.PI));
    return y * scale;
  }

  private double pixelXToLng(double pixelX) {
    double scale = mapScale();
    return pixelX / scale * 360.0 - 180.0;
  }

  private double pixelYToLat(double pixelY) {
    double scale = mapScale();
    double y = Math.PI - (2.0 * Math.PI * pixelY / scale);
    return Math.toDegrees(Math.atan(Math.sinh(y)));
  }

  // 지도 클릭 → 가장 가까운 제설함 찾기
  private void showNearestSnowBoxInfo(int clickX, int clickY) {
    if (snowBoxInfoList.isEmpty())
      return;

    double scale = mapScale();

    double centerPixelX = lngToPixelX(centerLng);
    double centerPixelY = latToPixelY(centerLat);

    double markerAnchorOffset = 30.0;
    double clickPixelX = centerPixelX + (clickX - MAP_WIDTH / 2.0)+markerAnchorOffset/3.2;
    double clickPixelY = centerPixelY + (clickY - MAP_HEIGHT / 2.0)+markerAnchorOffset;

    SnowBoxInfo nearest = null;
    double minDistSq = Double.MAX_VALUE;

    for (SnowBoxInfo info : snowBoxInfoList) {
      double px = lngToPixelX(info.location.x); // x = lng
      double py = latToPixelY(info.location.y); // y = lat

      double dx = px - clickPixelX;
      double dy = py - clickPixelY;
      double distSq = dx * dx + dy * dy;

      if (distSq < minDistSq) {
        minDistSq = distSq;
        nearest = info;
      }
    }

    double thresholdPx = 15.0;

    if (nearest != null && minDistSq <= thresholdPx * thresholdPx) {
      showSnowBoxInfoWindow(nearest);
    }
  }

  // 제설함 정보창 UI 표시
  private void showSnowBoxInfoWindow(SnowBoxInfo info) {
    Window parentWindow = SwingUtilities.getWindowAncestor(this);
    JDialog dialog = new JDialog((Frame) parentWindow, "제설함 정보", true);
    dialog.setSize(350, 250);
    dialog.setLocationRelativeTo(parentWindow);
    dialog.setLayout(new BorderLayout());

    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    JLabel num = new JLabel("<html><b>제설함 번호:</b> " + info.sboxNum + "</html>");
    num.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
    content.add(num);
    content.add(Box.createVerticalStrut(10));

    JLabel mgc = new JLabel("<html><b>관리기관:</b> " + info.mgcNm + "</html>");
    content.add(mgc);
    content.add(Box.createVerticalStrut(10));

    JLabel detl = new JLabel("<html><b>위치:</b> " + info.detlCn + "</html>");
    content.add(detl);
    content.add(Box.createVerticalStrut(10));

    JLabel coord = new JLabel(String.format("<html><b>좌표:</b> (%.6f, %.6f)</html>", info.location.y, info.location.x));
    coord.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
    content.add(coord);
    content.add(Box.createVerticalStrut(15));

    dialog.add(content, BorderLayout.CENTER);

    JButton close = new JButton("닫기");
    close.addActionListener(e -> dialog.dispose());

    JPanel panel = new JPanel();
    panel.add(close);
    dialog.add(panel, BorderLayout.SOUTH);

    dialog.setVisible(true);
  }

  /**
   * JSON 파일 경로를 동적으로 찾기
   * - client 폴더에서 실행: public/data/seoul_snowbox_location.json
   * - 프로젝트 루트에서 실행: client/public/data/seoul_snowbox_location.json
   */
  private String findJsonPath() {
    String[] possiblePaths = {
        "public/data/seoul_snowbox_location.json",
        "client/public/data/seoul_snowbox_location.json"
    };
    
    for (String path : possiblePaths) {
      if (Files.exists(Paths.get(path))) {
        return path;
      }
    }
    
    // 둘 다 없으면 기본값 반환
    return LOCAL_JSON_PATH;
  }

  private String readAll(InputStream stream) throws IOException {
    if (stream == null)
      return "";
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null)
        sb.append(line);
      return sb.toString();
    }
  }

}
