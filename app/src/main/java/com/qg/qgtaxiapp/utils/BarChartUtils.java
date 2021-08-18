package com.qg.qgtaxiapp.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @Name：QGTaxiApp
 * @Description：
 * @Author：Suzy.Mo
 * @Date：2021/8/17 19:59
 */
public class BarChartUtils {

    private BarChart bar;
    List<BarEntry> list = new ArrayList<>();


    public BarChartUtils(BarChart bar, List<BarEntry> list) {
        this.bar = bar;
        this.list = list;
    }

    public void settingBar(){
        BarDataSet barDataSet=new BarDataSet(list,"语文");   //list是你这条线的数据  "语文" 是你对这条线的描述
        BarData barData=new BarData(barDataSet);
        bar.setData(barData);

        barData.setBarWidth(0.5f);

        //柱状图手势交互
        bar.setTouchEnabled(false);//是否有手势操作
        bar.setScaleEnabled(false);//是否可缩放

        //折线图背景
        //bar.setBackgroundColor(Color.parseColor("#FF1F1F1F"));   //背景颜色
        bar.getXAxis().setDrawGridLines(true);  //是否绘制X轴上的网格线（背景里面的竖线）
        bar.getAxisLeft().setDrawGridLines(true);  //是否绘制Y轴上的网格线（背景里面的横线）
        bar.setFitBars(true);

        //对于右下角一串字母的操作
        bar.getDescription().setEnabled(false);                  //是否显示右下角描述
        bar.getDescription().setText("这是修改那串英文的方法");    //修改右下角字母的显示
        bar.getDescription().setTextSize(20);                    //字体大小
        bar.getDescription().setTextColor(Color.RED);             //字体颜色

//        //图例
       Legend legend = bar.getLegend();
       legend.setEnabled(false);    //是否显示图例
//        legend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);    //图例的位置

        //X轴
        XAxis xAxis=bar.getXAxis();
        xAxis.setDrawGridLines(false);  //是否绘制X轴上的网格线（背景里面的竖线）
        xAxis.setGridColor(Color.parseColor("#61FFFFFF"));
        xAxis.setAxisLineColor(Color.parseColor("#61FFFFFF"));   //X轴颜色
        xAxis.setAxisLineWidth(3);           //X轴粗细
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);        //X轴所在位置   默认为上面
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                if (value==1){
                    return "清晨";
                }
                if (value==2){
                    return "早上";
                }
                if (value==3){
                    return "下午";
                }
                if (value==4){
                    return "晚上";
                }
                return "";//注意这里需要改成 ""
            }
        });
        xAxis.setAxisMaximum(5);   //X轴最大数值
        xAxis.setAxisMinimum(0);   //X轴最小数值
        //X轴坐标的个数    第二个参数一般填false     true表示强制设置标签数 可能会导致X轴坐标显示不全等问题
        xAxis.setLabelCount(5,false);


        //Y轴
        YAxis AxisLeft=bar.getAxisLeft();
        AxisLeft.setDrawGridLines(true);  //是否绘制Y轴上的网格线（背景里面的横线）
        AxisLeft.setAxisLineColor(Color.argb(1,41,41,41));  //Y轴颜色
        AxisLeft.setGridColor(Color.parseColor("#61FFFFFF"));
        AxisLeft.setGridLineWidth(1);
        AxisLeft.setAxisLineWidth(3);           //Y轴粗细
        AxisLeft.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                for (int a = 0; a < 13 ; a++){     //用个for循环方便
                    if (a==value/10){
                        return String.valueOf(a*10);
                    }
                }
                return "";
            }
        });
        AxisLeft.setAxisMaximum(130f);   //Y轴最大数值
        AxisLeft.setAxisMinimum(0f);   //Y轴最小数值
        AxisLeft.setGranularity(20f); //Y轴标签间隔
        //Y轴坐标的个数    第二个参数一般填false     true表示强制设置标签数 可能会导致X轴坐标显示不全等问题
        AxisLeft.setLabelCount(100,false);

        //是否隐藏右边的Y轴（不设置的话有两条Y轴 同理可以隐藏左边的Y轴）
        bar.getAxisRight().setEnabled(false);
        bar.getAxisLeft().setEnabled(true);


        //柱子
        barDataSet.setDrawValues(false);//是否显示柱子上的文字
        barDataSet.setColor(Color.parseColor("#FF03DAC5"));  //柱子的颜色
        //barDataSet.setColors(Color.BLACK,Color.BLUE);//设置柱子多种颜色  循环使用
        barDataSet.setBarBorderColor(Color.parseColor("#FF03DAC5"));//柱子边框颜色
        barDataSet.setBarBorderWidth(1);       //柱子边框厚度
        barDataSet.setBarShadowColor(Color.parseColor("#FF03DAC5"));
        barDataSet.setHighlightEnabled(true);//选中柱子是否高亮显示  默认为true
        barDataSet.setStackLabels(new String[]{"aaa","bbb","ccc"});
        barDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                return barEntry.toString();
            }
        });
        //定义柱子上的数据显示    可以实现加单位    以及显示整数（默认是显示小数）
//        barDataSet.setValueFormatter(new IValueFormatter() {
//            @Override
//            public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
//
//                return entry.toString();
//            }
//        });

        //数据更新
        bar.notifyDataSetChanged();
        bar.invalidate();

        //动画（如果使用了动画可以则省去更新数据的那一步）
        bar.animateY(500); //在Y轴的动画  参数是动画执行时间 毫秒为单位
    }
    public void setBar(){
        BarDataSet barDataSet=new BarDataSet(list,"司机收入分析");   //list是你这条线的数据  "语文" 是你对这条线的描述
        BarData barData=new BarData(barDataSet);
        bar.setData(barData);
        barDataSet.setColor(Color.parseColor("#FF03DAC5"));  //柱子的颜色
        //barDataSet.setColors(new int[]{Color.BLUE,Color.RED});//设置柱子多种颜色  循环使用
        //barDataSet.setBarBorderColor(Color.CYAN);//柱子边框颜色
        //barDataSet.setBarBorderWidth(2);       //柱子边框厚度
        //barDataSet.setBarShadowColor(Color.RED);
        barDataSet.setHighlightEnabled(true);//选中柱子是否高亮显示  默认为true
        //barDataSet.setStackLabels(new String[]{"aaa","bbb","ccc"});
        //定义柱子上的数据显示    可以实现加单位    以及显示整数（默认是显示小数）
        barDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                if (entry.getY()==value){
                    return String.valueOf(value);
                }
                return "";
            }
        });

        setBarBG();
        setBarXY();
        setBarAnimate();
        setBarChange();
    }

    public void setBarBG(){
        //折线图背景
        bar.setBackgroundColor(Color.parseColor("#FF1F1F1F"));   //背景颜色
        bar.getXAxis().setDrawGridLines(true);  //是否绘制X轴上的网格线（背景里面的竖线）
        bar.getAxisLeft().setDrawGridLines(true);  //是否绘制Y轴上的网格线（背景里面的横线）
    }

//    public void setBarPicture(){
//        //图例
//        Legend legend=bar.getLegend();
//        legend.setEnabled(true);    //是否显示图例
//        legend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);    //图例的位置
//    }

    public void setBarXY(){
        //X轴
        XAxis xAxis=bar.getXAxis();
        xAxis.setDrawGridLines(false);  //是否绘制X轴上的网格线（背景里面的竖线）
        xAxis.setGridColor(Color.parseColor("#61FFFFFF"));
        xAxis.setAxisLineColor(Color.parseColor("#61FFFFFF"));   //X轴颜色
        xAxis.setAxisLineWidth(3);           //X轴粗细
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//X轴所在位置   默认为上面
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value==1){
                    return "清晨";
                }
                if (value==2){
                    return "早上";
                }
                if (value==3){
                    return "中午";
                }
                if (value==4){
                    return "晚上";
                }
                return "";//注意这里需要改成 ""
                //return super.getFormattedValue(value);
            }
        });
        xAxis.setAxisMaximum(5);
        xAxis.setAxisMinimum(0);

        //X轴坐标的个数    第二个参数一般填false     true表示强制设置标签数 可能会导致X轴坐标显示不全等问题
        xAxis.setLabelCount(5,false);


        //Y轴
        YAxis AxisLeft=bar.getAxisLeft();
        AxisLeft.setDrawGridLines(true);//是否绘制Y轴上的网格线（背景里面的横线）
        AxisLeft.setAxisLineColor(Color.parseColor("#61FFFFFF"));  //Y轴颜色
        AxisLeft.setAxisLineWidth(3);           //Y轴粗细
        AxisLeft.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                for (int a = 0; a < 10; a++) {     //用个for循环方便
                    if (a == value) {
                        return String.valueOf(a * 10);
                    }
                }
                return " ";
            }
        });
        AxisLeft.setAxisMinimum(0);
        AxisLeft.setAxisMinimum(120);
        //Y轴坐标的个数    第二个参数一般填false     true表示强制设置标签数 可能会导致X轴坐标显示不全等问题
        AxisLeft.setLabelCount(120,false);

        //是否隐藏右边的Y轴（不设置的话有两条Y轴 同理可以隐藏左边的Y轴）
        bar.getAxisRight().setEnabled(false);
        bar.getAxisLeft().setEnabled(false);

    }

    public void setBarChange(){
        bar.notifyDataSetChanged();
        bar.invalidate();
    }

    public void setBarAnimate(){
        //动画（如果使用了动画可以则省去更新数据的那一步）
        bar.animateY(3000); //在Y轴的动画  参数是动画执行时间 毫秒为单位
//        bar.animateX(2000); //X轴动画
//        bar.animateXY(2000,2000);//XY两轴混合动画

    }

}

