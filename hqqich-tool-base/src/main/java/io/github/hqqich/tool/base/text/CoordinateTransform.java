package io.github.hqqich.tool.base.text;



public class CoordinateTransform {
    /**
     * 圆周率
     */
    private static final double PI = 3.1415926535897932384626D;

    /**
     * 火星坐标系与百度坐标系转换的中间量
     */
    private static final double X_PI = 3.14159265358979324 * 3000.0 / 180.0D;

    /**
     * 地球长半轴
     */
    private static final double SEMI_MAJOR = 6378245.0D;

    /**
     * 扁率
     * 计算方式： 长半轴 a,  1/f = 298.3
     * b = a * (1 - f)
     * 扁率 ae = (a^2 - b^2) / a^2
     */
    private static final double FLATTENING = 0.00669342162296594323D;

    /**
     * 中国的经纬度位置
     */
    private static final double LNG_E1 = 72.004;
    private static final double LNG_E2 = 137.8347;
    private static final double LAT_N1 = 0.8293;
    private static final double LAT_N2 = 55.8271;

    /**
     * WGS84 => GCJ02 地球坐标系 => 火星坐标系
     * @param lng
     * @param lat
     * @return
     */
    public static Point wgs84ToGcj02(double lng, double lat) {
        //if (isInChina(lng, lat)) {
        //    return new Point(lng, lat);
        //}

        double[] offset = offset(lng, lat);
        double mglng = lng + offset[0];
        double mglat = lat + offset[1];

        return new Point(mglng, mglat);
    }

    /**
     * GCJ02 => WGS84 火星坐标系 => 地球坐标系(有误差)
     * @param lng
     * @param lat
     * @return
     */
    public static Point gcj02ToWgs84(double lng, double lat) {
        //if (isInChina(lng, lat)) {
        //    return new Point(lng, lat);
        //}

        double[] offset = offset(lng, lat);
        double mglng = lng - offset[0];
        double mglat = lat - offset[1];

        return new Point(mglng, mglat);
    }

    /**
     * GCJ02 => WGS84 火星坐标系 => 地球坐标系（精确）
     * @param lng
     * @param lat
     * @return
     */
    public static Point gcj02ToWgs84Exactly(double lng, double lat) {
        //if (isInChina(lng, lat)) {
        //    return new Point(lng, lat);
        //}

        double initDelta = 0.01;
        double threshold = 0.000000001;
        double dLat = initDelta, dLon = initDelta;
        double mLat = lat - dLat, mLon = lng - dLon;
        double pLat = lat + dLat, pLon = lng + dLon;
        double wgsLat, wgsLng, i = 0;
        while (true) {
            wgsLat = (mLat + pLat) / 2;
            wgsLng = (mLon + pLon) / 2;
            Point point = wgs84ToGcj02(wgsLng, wgsLat);
            dLon = point.getLng() - lng;
            dLat = point.getLat() - lat;
            if ((Math.abs(dLat) < threshold) && (Math.abs(dLon) < threshold)) {
                break;
            }
            if (dLat > 0) {
                pLat = wgsLat;
            } else{
                mLat = wgsLat;
            }
            if (dLon > 0) {
                pLon = wgsLng;
            } else {
                mLon = wgsLng;
            }
            if (++i > 10000) {
                break;
            }
        }

        return new Point(wgsLng, wgsLat);
    }

    /**
     * GCJ02 => BD09 火星坐标系 => 百度坐标系
     * @param lng
     * @param lat
     * @return
     */
    public static Point gcj02ToBd09(double lng, double lat) {
        double z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * X_PI);
        double theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * X_PI);
        double bd_lng = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        return new Point(bd_lng, bd_lat);
    }

    /**
     * BD09 => GCJ02 百度坐标系 => 火星坐标系
     * @param lng
     * @param lat
     * @return
     */
    public static Point bd09ToGcj02(double lng, double lat) {
        double x = lng - 0.0065;
        double y = lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
        double gcj_lng = z * Math.cos(theta);
        double gcj_lat = z * Math.sin(theta);
        return new Point(gcj_lng, gcj_lat);
    }

    /**
     * WGS84 => BD09 地球坐标系 => 百度坐标系
     * @param lng
     * @param lat
     * @return
     */
    public static Point wgs84ToBd09(double lng, double lat) {
        Point point = wgs84ToGcj02(lng, lat);
        return gcj02ToBd09(point.getLng(), point.getLat());
    }

    /**
     * BD09 => WGS84 百度坐标系 => 地球坐标系
     * @param lng
     * @param lat
     * @return
     */
    public static Point bd09ToWgs84(double lng, double lat) {
        Point point = bd09ToGcj02(lng, lat);
        return gcj02ToWgs84(point.getLng(), point.getLat());
    }

    /**
     * 经纬度位于中国境外返回true,境内返回false
     * @param lng 	经度
     * @param lat	纬度
     * @return
     */
    public static boolean isInChina(double lng, double lat) {
        if (lng < LNG_E1 || lng > LNG_E2) {
            return true;
        }
        if (lat < LAT_N1 || lat > LAT_N2) {
            return true;
        }
        return false;
    }

    /**
     * 经度偏移量
     * @param lng
     * @param lat
     * @return
     */
    private static double transformLng(double lng, double lat) {
        double ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lng * PI) + 40.0 * Math.sin(lng / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lng / 12.0 * PI) + 300.0 * Math.sin(lng / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 纬度偏移量
     * @param lng
     * @param lat
     * @return
     */
    private static double transformLat(double lng, double lat) {
        double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat
                + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * PI) + 40.0 * Math.sin(lat / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * PI) + 320 * Math.sin(lat * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 偏移量
     * @param lng
     * @param lat
     * @return
     */
    public static double[] offset(double lng, double lat) {
        double[] lngLat = new double[2];
        double dlng = transformLng(lng - 105.0, lat - 35.0);
        double dlat = transformLat(lng - 105.0, lat - 35.0);
        double radlat = lat / 180.0 * PI;
        double magic = Math.sin(radlat);
        magic = 1 - FLATTENING * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dlng = (dlng * 180.0) / (SEMI_MAJOR / sqrtMagic * Math.cos(radlat) * PI);
        dlat = (dlat * 180.0) / ((SEMI_MAJOR * (1 - FLATTENING)) / (magic * sqrtMagic) * PI);
        lngLat[0] = dlng;
        lngLat[1] = dlat;
        return lngLat;
    }
}


