package utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 제설함(제설함 번호/위치/관리기관) 정보를 로컬 JSON에서 읽어
 * 메모리에 캐싱하고 검색/ID 조회를 제공하는 유틸 클래스.
 *
 * - 데이터 소스: public/data/seoul_snowbox_location.json
 * - ID 규칙: JSON의 DATA 배열 인덱스(0부터 시작)에 1을 더한 값
 *           => reviews.saltbox_id 와 매핑
 */
public class SnowBoxStore {

    /**
     * 제설함 한 개의 정보를 담는 DTO
     */
    public static class SnowBoxInfo {
        private final int id;          // 1부터 시작하는 내부 ID
        private final String sboxNum;  // 제설함 번호 (예: 금천-133)
        private final String detlCn;   // 위치 상세 정보
        private final String mgcNm;    // 관리 기관 명

        public SnowBoxInfo(int id, String sboxNum, String detlCn, String mgcNm) {
            this.id = id;
            this.sboxNum = sboxNum;
            this.detlCn = detlCn;
            this.mgcNm = mgcNm;
        }

        public int getId() {
            return id;
        }

        public String getSboxNum() {
            return sboxNum;
        }

        public String getDetlCn() {
            return detlCn;
        }

        public String getMgcNm() {
            return mgcNm;
        }

        @Override
        public String toString() {
            // 리스트나 콤보박스에 표시될 기본 문자열
            String num = (sboxNum == null || sboxNum.isEmpty()) ? "(번호 없음)" : sboxNum;
            String loc = (detlCn == null || detlCn.isEmpty()) ? "(위치 정보 없음)" : detlCn;
            String org = (mgcNm == null || mgcNm.isEmpty()) ? "" : " - " + mgcNm;
            return num + " - " + loc + org;
        }
    }

    private static final List<SnowBoxInfo> SNOW_BOXES = new ArrayList<>();
    private static boolean initialized = false;

    private SnowBoxStore() {
        // 유틸 클래스
    }

    /**
     * 전체 제설함 목록을 반환 (읽기 전용 리스트)
     */
    public static List<SnowBoxInfo> getAll() {
        ensureInitialized();
        return Collections.unmodifiableList(SNOW_BOXES);
    }

    /**
     * 내부 ID(1부터 시작)로 제설함 정보를 조회
     */
    public static SnowBoxInfo getById(int id) {
        ensureInitialized();
        if (id <= 0 || id > SNOW_BOXES.size()) {
            return null;
        }
        // id는 1부터 시작, 리스트 인덱스는 0부터 시작
        return SNOW_BOXES.get(id - 1);
    }

    /**
     * 번호/위치/관리기관에 대해 간단한 부분 문자열 검색
     */
    public static List<SnowBoxInfo> search(String keyword) {
        ensureInitialized();
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>(SNOW_BOXES);
        }
        String lower = keyword.toLowerCase();
        List<SnowBoxInfo> result = new ArrayList<>();
        for (SnowBoxInfo info : SNOW_BOXES) {
            String num = info.getSboxNum() == null ? "" : info.getSboxNum().toLowerCase();
            String loc = info.getDetlCn() == null ? "" : info.getDetlCn().toLowerCase();
            String org = info.getMgcNm() == null ? "" : info.getMgcNm().toLowerCase();
            if (num.contains(lower) || loc.contains(lower) || org.contains(lower)) {
                result.add(info);
            }
        }
        return result;
    }

    private static synchronized void ensureInitialized() {
        if (initialized) return;
        loadFromJson();
        initialized = true;
    }

    /**
     * 로컬 JSON 파일에서 제설함 데이터를 읽어 SNOW_BOXES 에 채움
     */
    private static void loadFromJson() {
        SNOW_BOXES.clear();

        String[] possiblePaths = {
                "public/data/seoul_snowbox_location.json",
                "client/public/data/seoul_snowbox_location.json"
        };

        String foundPath = null;
        for (String path : possiblePaths) {
            if (Files.exists(Paths.get(path))) {
                foundPath = path;
                break;
            }
        }

        if (foundPath == null) {
            System.err.println("[SnowBoxStore] 제설함 JSON 파일을 찾을 수 없습니다.");
            return;
        }

        try (InputStream is = Files.newInputStream(Paths.get(foundPath))) {
            String jsonText = readAll(is);
            if (jsonText == null || jsonText.isEmpty()) {
                System.err.println("[SnowBoxStore] 제설함 JSON 파일이 비어 있습니다.");
                return;
            }

            JSONObject root = new JSONObject(jsonText);
            // seoul_snowbox_location.json 구조: { "DESCRIPTION": {...}, "DATA": [ ... ] }
            JSONArray dataArray = root.optJSONArray("DATA");
            if (dataArray == null) {
                System.err.println("[SnowBoxStore] JSON에서 DATA 배열을 찾을 수 없습니다.");
                return;
            }

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject item = dataArray.optJSONObject(i);
                if (item == null) continue;

                // 필드 이름은 영문/한글 케이스가 섞일 수 있으므로 둘 다 시도
                String sboxNum = item.optString("sbox_num",
                        item.optString("제설함번호", ""));
                String detlCn = item.optString("detl_cn",
                        item.optString("위치상세정보", ""));
                String mgcNm = item.optString("mgc_nm",
                        item.optString("관리기관명", ""));

                int id = i + 1; // 1부터 시작
                SnowBoxInfo info = new SnowBoxInfo(id, sboxNum, detlCn, mgcNm);
                SNOW_BOXES.add(info);
            }

            System.out.println("[SnowBoxStore] 제설함 데이터 로드 완료: " + SNOW_BOXES.size() + "개");
        } catch (IOException e) {
            System.err.println("[SnowBoxStore] 제설함 JSON 읽기 실패: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[SnowBoxStore] 제설함 JSON 파싱 중 오류: " + e.getMessage());
        }
    }

    private static String readAll(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}


