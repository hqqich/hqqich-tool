package io.github.hqqich.tool.base.text;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Point {

    private static final int SCALE = 1_000_000; // 提取魔法数字为常量

    private double lng;
    private double lat;

    public Point() {
    }

    public Point(double lng, double lat) {
        this.lng = roundCoordinate(lng);
        this.lat = roundCoordinate(lat);
    }

    private double fastRound(double value) {
        // 使用浮点数快速舍入（适合对精度要求不极端的场景）
        return Math.round(value * SCALE) / (double) SCALE;
    }

    private double roundCoordinate(double value) {
        // 使用BigDecimal进行精确舍入，避免浮点精度问题
        BigDecimal bd = new BigDecimal(Double.toString(value));
        return bd.setScale(6, RoundingMode.HALF_UP).doubleValue();
    }

    // 可选：添加范围验证（根据业务需求）
    private void validateCoordinate(double value, boolean isLng) {
        double min = isLng ? -180 : -90;
        double max = isLng ? 180 : 90;
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    String.format("%s坐标超出有效范围[-%f, %f]: %f", isLng ? "经度" : "纬度", max, max, value));
        }
    }


    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    @Override
    public String toString() {
        return lng + "," + lat;
    }
}
