package com.qg.qgtaxiapp.utils;

import static com.google.android.material.internal.ContextUtils.getActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.CustomListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.qg.qgtaxiapp.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author: Hx
 * @date: 2021年08月13日 16:20
 */
public class TimePickerUtils{

    private TimePickerView timePickerView = null;
    private String mDate = null;
    /*
       获取时间
       yyyy-MM-dd HH:mm:ss
    */
    private String getDate(Date date) {//可根据需要自行截取数据显示
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    private String getTimeslot(Date date) {//可根据需要自行截取数据显示
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(date);
    }

    /*
        初始化时间选择器(日期)
     */
    public TimePickerView initDatePicker(Context context, Activity activity){
        /*
        设置时间跨度
         */
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        startDate.set(2017,0,1);
        endDate.set(2017,11,31);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            timePickerView = new TimePickerBuilder(context, new OnTimeSelectListener() {

                @Override
                public void onTimeSelect(Date date, View v) {//选项数据回调
                    mDate = getDate(date);
                    Log.d("TAG",mDate);
                }
            })
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
                    .setType(new boolean[]{false,true,true,false,false,false})//是否显示年月日，时分秒
                    .isAlphaGradient(true)//滚轮透明
                    .build();
        }
    /*
     *  设置dialog的宽度
     */
        Dialog timePickerDialog;
        timePickerDialog = timePickerView.getDialog();
        Window window = timePickerDialog.getWindow();
        WindowManager manager = activity.getWindowManager();
        Display display = manager.getDefaultDisplay();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = (int) (display.getWidth() * 0.95);
        window.setAttributes(params);

        return timePickerView;
    }






}
