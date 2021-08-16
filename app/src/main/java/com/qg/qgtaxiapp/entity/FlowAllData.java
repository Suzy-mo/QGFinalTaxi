package com.qg.qgtaxiapp.entity;

import java.util.List;

/**
 * @Name：QGTaxiApp
 * @Description：
 * @Author：Suzy.Mo
 * @Date：2021/8/14 19:42
 */
public class FlowAllData {

    private Integer code;
    private List<DataBean> data;
    private String message;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class DataBean {
        private Double onLongitude;
        private Double offLongitude;
        private Double offLatitude;
        private Double onLatitude;

        public Double getOnLongitude() {
            return onLongitude;
        }

        public void setOnLongitude(Double onLongitude) {
            this.onLongitude = onLongitude;
        }

        public Double getOffLongitude() {
            return offLongitude;
        }

        public void setOffLongitude(Double offLongitude) {
            this.offLongitude = offLongitude;
        }

        public Double getOffLatitude() {
            return offLatitude;
        }

        public void setOffLatitude(Double offLatitude) {
            this.offLatitude = offLatitude;
        }

        public Double getOnLatitude() {
            return onLatitude;
        }

        public void setOnLatitude(Double onLatitude) {
            this.onLatitude = onLatitude;
        }
    }
}
