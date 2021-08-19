package com.qg.qgtaxiapp.entity;

import java.util.List;

/**
 * @Name：QGTaxiApp
 * @Description：
 * @Author：Suzy.Mo
 * @Date：2021/8/18 10:39
 */
public class CarLineChartBean {

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
        private List<FeatureBean> feature;
        private List<WeekendBean> weekend;
        private List<WeekdayBean> weekday;

        public List<FeatureBean> getFeature() {
            return feature;
        }

        public void setFeature(List<FeatureBean> feature) {
            this.feature = feature;
        }

        public List<WeekendBean> getWeekend() {
            return weekend;
        }

        public void setWeekend(List<WeekendBean> weekend) {
            this.weekend = weekend;
        }

        public List<WeekdayBean> getWeekday() {
            return weekday;
        }

        public void setWeekday(List<WeekdayBean> weekday) {
            this.weekday = weekday;
        }

        public static class FeatureBean {
            private Double number;

            public Double getNumber() {
                return number;
            }

            public void setNumber(Double number) {
                this.number = number;
            }
        }

        public static class WeekendBean {
            private Double number;

            public Double getNumber() {
                return number;
            }

            public void setNumber(Double number) {
                this.number = number;
            }
        }

        public static class WeekdayBean {
            private Double number;

            public Double getNumber() {
                return number;
            }

            public void setNumber(Double number) {
                this.number = number;
            }
        }
    }
}
