package javaProject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;
import com.google.gson.*;

public class SnowRemovalMap extends JFrame {
    // ✅ Google Maps API 키
    private static final String GOOGLE_API_KEY = "AIzaSyAmqp1khCMy-wdof_llEq_XMPvdHO2mgmc";
    
    // ✅ 서울열린데이터광장 제설함 API
    private static final String SERVICE_KEY = "596765645667796f37336a497a4c4c";
    private static final String API_URL = "https://api.odcloud.kr/api/15087209/v1/uddi:4b5c046f-0b27-41bb-9f2a-09e6e3d6370b";

    private static final int MAP_WIDTH = 800;
    private static final int MAP_HEIGHT = 600;

    private double centerLat = 37.5665; // 서울 시청 기준
    private double centerLng = 126.9780;
    private int zoom = 12;

    private JLabel mapLabel;
    private java.util.List<double[]> snowBoxLocations = new ArrayList<>();

    public SnowRemovalMap() {
        setTitle("서울시 제설함 지도 뷰어");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(MAP_WIDTH, MAP_HEIGHT);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        mapLabel = new JLabel("지도를 불러오는 중...", SwingConstants.CENTER);
        add(mapLabel, BorderLayout.CENTER);

        // 제설함 데이터 불러오기
        loadSnowBoxData();

        // 지도 로드
        loadMap();

        // ✅ 마우스 클릭 → 해당 위치를 지도 중심으로 이동
        mapLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();

                int dx = x - MAP_WIDTH / 2;
                int dy = y - MAP_HEIGHT / 2;

                double scale = 0.002 / Math.pow(2, zoom - 10);

                centerLat -= dy * scale;
                centerLng += dx * scale;

                loadMap();
            }
        });

        // ✅ 마우스 휠 줌 인/아웃
        mapLabel.addMouseWheelListener(e -> {
            int notches = e.getWheelRotation();
            zoom -= notches;
            zoom = Math.max(1, Math.min(20, zoom));
            loadMap();
        });

        setVisible(true);
    }

    /**
     * 서울열린데이터광장에서 제설함 위치 데이터를 불러옴
     */
    private void loadSnowBoxData() {
        try {
            String urlString = API_URL + "?serviceKey=" + URLEncoder.encode(SERVICE_KEY, "UTF-8");
            URL url = new URL(urlString);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                JOptionPane.showMessageDialog(this, 
                    "제설함 데이터 로드 실패: Server returned HTTP response code: " + conn.getResponseCode(),
                    "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JsonObject root = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonArray data = root.getAsJsonArray("data");

            snowBoxLocations.clear();
            for (JsonElement elem : data) {
                JsonObject obj = elem.getAsJsonObject();
                if (obj.has("위도") && obj.has("경도")) {
                    try {
                        double lat = obj.get("위도").getAsDouble();
                        double lng = obj.get("경도").getAsDouble();
                        snowBoxLocations.add(new double[]{lat, lng});
                    } catch (Exception ignored) {}
                }
            }

            System.out.println("제설함 데이터 로드 완료 (" + snowBoxLocations.size() + "개)");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "제설함 데이터 로드 실패: " + e.getMessage(), 
                "오류", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Google Static Map을 새로 로드
     */
    private void loadMap() {
        SwingUtilities.invokeLater(() -> {
            try {
                StringBuilder markerParams = new StringBuilder();
                for (double[] loc : snowBoxLocations) {
                    markerParams.append("&markers=color:blue%7C")
                                .append(loc[0]).append(",").append(loc[1]);
                }

                String urlStr = String.format(
                    "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=%d&size=%dx%d&maptype=roadmap%s&key=%s",
                    centerLat, centerLng, zoom, MAP_WIDTH, MAP_HEIGHT, markerParams, GOOGLE_API_KEY
                );

                ImageIcon icon = new ImageIcon(new URL(urlStr));
                mapLabel.setIcon(icon);
                mapLabel.setText(null);

            } catch (Exception e) {
                mapLabel.setText("지도 로드 실패: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SnowRemovalMap::new);
    }
}
