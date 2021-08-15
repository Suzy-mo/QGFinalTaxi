package com.qg.qgtaxiapp.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.adapter.ArrayWheelAdapter;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.CustomListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.bigkoo.pickerview.view.TimePickerView;
import com.contrarywind.listener.OnItemSelectedListener;
import com.contrarywind.view.WheelView;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.entity.EventBusEvent;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author: Hx
 * @date: 2021年08月13日 16:20
 */
public class TimePickerUtils{

    private TimePickerView timePickerView = null;
    private OptionsPickerView optionsPickerView = null;
    private String mDate = null;
    private String mTimeslot = null;
    private AlertDialog dialog = null;
    private List<Integer> hour;
    private List<Integer> min;
    public int h1;
    public int h2;
    private int m1;
    private int m2;
    /*
       获取日期 yyyy-MM-dd
    */
    public String getDate(Date date) {//可根据需要自行截取数据显示
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }
    /*
       获取时间段 HH:mm:ss
    */
    public String getTimeslot(Date date) {//可根据需要自行截取数据显示
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(date);
    }

    /*
        初始化日期选择器
     */
    public TimePickerView initDatePicker(Context context, Activity activity,OnTimeSelectListener onTimeSelectListener){
        /*
        设置时间跨度
         */
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        startDate.set(2017,1,1);
        endDate.set(2017,2,31);
        timePickerView = new TimePickerBuilder(context,onTimeSelectListener)
        //自定义布局
        .setLayoutRes(R.layout.timepicker_date, new CustomListener() {
            @Override
            public void customLayout(View v) {
                TextView next = v.findViewById(R.id.timepicker_date_next);
                ImageView cancel = v.findViewById(R.id.timepicker_date_cancel);
                //点击下一步
                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        timePickerView.returnData();
                        timePickerView.dismiss();
                    }
                });
                //点击取消
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        timePickerView.dismiss();
                    }
                });

            }
        })
        .isDialog(true)//显示模式
        //.setTitleText("时间选择 • 日期")
        .setOutSideCancelable(false)//外部是否可以点击
        .setRangDate(startDate,endDate)//设置时间跨度
        .setOutSideColor(activity.getResources().getColor(R.color.timepicker_outside))//外部背景颜色
        .setBgColor(activity.getResources().getColor(R.color.timepicker_background))//背景颜色
        .setTextColorCenter(activity.getResources().getColor(R.color.timepicker_selectText))//选中字体颜色
        .setTextColorOut(activity.getResources().getColor(R.color.timepicker_unselectText))//未选中字体颜色
        .isCenterLabel(true)//只显示中央标签
        .setItemVisibleCount(5)//可见标签数
        .setDividerColor(Color.argb(0,0,0,0))
        .setType(new boolean[]{false,true,true,false,false,false})//是否显示年月日，时分秒
        .isAlphaGradient(true)//滚轮透明
        .build();

    /*
     *  设置dialog的宽度
     */
        Dialog timePickerDialog ;
        timePickerDialog = timePickerView.getDialog();
        Window window = timePickerDialog.getWindow();
        WindowManager manager = activity.getWindowManager();
        Display display = manager.getDefaultDisplay();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = (int) (display.getWidth() * 0.95);
        window.setAttributes(params);

        return timePickerView;
    }



    /*
     *  初始化时间段数据
     */
    private void initTimeslotData(){
        hour = new ArrayList<>();
        min = new ArrayList<>();

        for (int i = 0; i < 24; i++){
            hour.add(i);
        }
        for (int i = 0; i < 60; i++){
            min.add(i);
        }
    }
    /*
     *  初始化时间段选择器
     */
    public AlertDialog initTimeSlotDialog(Context context, View.OnClickListener onClickListener){
        TextView tv_confirm;
        ImageView iv_cancel;
        initTimeslotData();
        View view = LayoutInflater.from(context).inflate(R.layout.timepicker_timeslot,null,false);
        dialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setView(view)
                .create();

        tv_confirm = view.findViewById(R.id.timepicker_timeslot_confirm);
        iv_cancel = view.findViewById(R.id.timepicker_timeslot_cancel);

        tv_confirm.setOnClickListener(onClickListener);

        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        WheelView hour1 = view.findViewById(R.id.options1);
        WheelView min1 = view.findViewById(R.id.options2);
        WheelView hour2 = view.findViewById(R.id.options3);
        WheelView min2 = view.findViewById(R.id.options4);

        hour1.setCyclic(false);
        min1.setCyclic(false);
        hour2.setCyclic(false);
        min2.setCyclic(false);
        hour1.setDividerColor(Color.argb(0,0,0,0));
        min1.setDividerColor(Color.argb(0,0,0,0));
        hour2.setDividerColor(Color.argb(0,0,0,0));
        min2.setDividerColor(Color.argb(0,0,0,0));

        hour1.setAdapter(new ArrayWheelAdapter(hour));
        min1.setAdapter(new ArrayWheelAdapter(min));
        hour2.setAdapter(new ArrayWheelAdapter(hour));
        min2.setAdapter(new ArrayWheelAdapter(min));

        hour1.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
               h1 = hour.get(index);
            }
        });

        min1.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                m1 = min.get(index);
            }
        });

        hour2.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                h2 = hour.get(index);
            }
        });

        min2.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                m2 = min.get(index);
            }
        });

        return dialog;
    }

    /*
     *   获取完整的起始时间和终止时间
     */
    public String getTime(){
        StringBuffer buffer = new StringBuffer();
        if (h1 < 10){
            buffer.append(mDate + "_" + "0" + h1);
        }else {
            buffer.append(mDate + "_" + h1);
        }

        if (m1 < 10){
            buffer.append(":" + "0" + m1 + ":00");
        }else {
            buffer.append(":" + m1 + ":00");
        }

        buffer.append("/");

        if (h2 < 10){
            buffer.append(mDate + "_" + "0" + h2);
        }else {
            buffer.append(mDate + "_" + h2);
        }

        if (m2 < 10){
            buffer.append(":" + "0" + m2 + ":00");
        }else {
            buffer.append(":" + m2 + ":00");
        }
        return buffer.toString();
    }

    /*
     * 获取时间段
     */
    public String getTimeslot(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("" + h1);

        if (m1 < 10){
            buffer.append("：" + "0" + m1);
        }else {
            buffer.append("：" + m1);
        }

        buffer.append(" - ");
        buffer.append("" + h2);
        if (m2 < 10){
            buffer.append("：" + "0" + m2);
        }else {
            buffer.append("：" + m2);
        }
        return buffer.toString();
    }

    public String getmDate() {
        return mDate;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }
}
