package components.map;

import java.awt.geom.Point2D;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

public class CoordinateConverter {

    // 🌟 [수정] 서울시 공공데이터(제설함 등)에 맞는 EPSG:5186 (GRS80, Y_0=600000) 좌표계 정의
    private static final String TM_PROJ4_STRING = 
        "+proj=tmerc +lat_0=38 +lon_0=127 +k=1 +x_0=200000 +y_0=600000 +ellps=GRS80 +units=m +no_defs";
    
    private static final String WGS84_PROJ4_STRING = 
        "+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs";
        
    private CoordinateTransform transform;

    public CoordinateConverter() {
        try {
            CRSFactory csFactory = new CRSFactory();
            CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
            CoordinateReferenceSystem tmCRS = csFactory.createFromParameters("TM", TM_PROJ4_STRING);
            CoordinateReferenceSystem wgs84CRS = csFactory.createFromParameters("WGS84", WGS84_PROJ4_STRING);
            this.transform = ctFactory.createTransform(tmCRS, wgs84CRS);
        } catch (Exception e) {
            e.printStackTrace();
            this.transform = null; 
        }
    }

    public Point2D.Double convert(double tmX, double tmY) {
        if (transform == null) return new Point2D.Double(0.0, 0.0); 
        
        // X, Y 순서 그대로 입력
        ProjCoordinate tmPoint = new ProjCoordinate(tmX, tmY);
        ProjCoordinate wgs84Point = new ProjCoordinate();

        transform.transform(tmPoint, wgs84Point);

        return new Point2D.Double(wgs84Point.x, wgs84Point.y);
    }
}