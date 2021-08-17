package com.qg.qgtaxiapp.entity;

import java.util.List;

/**
 * @Name：QGTaxiApp
 * @Description：
 * @Author：Suzy.Mo
 * @Date：2021/8/17 15:15
 */
public class CarIncomeBean {

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
        private Double afternoon;
        private Double early_morning;
        private Double morning;
        private Double night;

        public Double getAfternoon() {
            return afternoon;
        }

        public void setAfternoon(Double afternoon) {
            this.afternoon = afternoon;
        }

        public Double getEarly_morning() {
            return early_morning;
        }

        public void setEarly_morning(Double early_morning) {
            this.early_morning = early_morning;
        }

        public Double getMorning() {
            return morning;
        }

        public void setMorning(Double morning) {
            this.morning = morning;
        }

        public Double getNight() {
            return night;
        }

        public void setNight(Double night) {
            this.night = night;
        }
    }
}
