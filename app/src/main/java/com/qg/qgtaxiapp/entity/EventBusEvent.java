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
}
