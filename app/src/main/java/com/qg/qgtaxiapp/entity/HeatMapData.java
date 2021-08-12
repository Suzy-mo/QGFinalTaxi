package com.qg.qgtaxiapp.entity;

import java.util.List;

/**
 * @author: Hx
 * @date: 2021年08月12日 22:16
 */
class HeatMapData {
    private int code;
    private List<data> data;
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<HeatMapData.data> getData() {
        return data;
    }

    public void setData(List<HeatMapData.data> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    /**
     *   latitude: 纬度
     *  longitude: 经度
     */
    public static class data{

        private double latitude;
        private double longitude;


        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }
}
