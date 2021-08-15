package com.qg.qgtaxiapp.entity;

/**
 * @author: Hx
 * @date: 2021年08月14日 0:17
 */
public class EventBusEvent {

    public static class showTimeSlotSet{
        private String date;
        public showTimeSlotSet(String date){
            this.date = date;
        }
        public String getDate(){
            return date;
        }
    }

    public static class setTimeFinish{
        private String msg;
        public setTimeFinish(String msg){
            this.msg = msg;
        }
        public String getMsg(){
            return msg;
        }
    }


}
