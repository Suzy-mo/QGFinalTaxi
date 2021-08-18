package com.qg.qgtaxiapp.utils;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @Name：QGTaxiApp
 * @Description：
 * @Author：Suzy.Mo
 * @Date：2021/8/17 23:12
 */
public class LineChartsUtils {
    private LineChart line;
    private List<Entry> mData = new ArrayList<>();

    public LineChartsUtils(LineChart line, List<Entry> mData) {
        this.line = line;
        this.mData = mData;

    }

    public void setFirstLineWork(){
        LineDataSet lineDataSet = new LineDataSet(mData,"工作日");
        LineData lineData = new LineData(lineDataSet);
        line.setData(lineData);

        //折线
        //设置折线的式样   这个是圆滑的曲线（有好几种自己试试）     默认是直线
        //lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setColor(Color.GREEN);  //折线的颜色
        lineDataSet.setLineWidth(2);        //折线的粗细
        //是否画折线点上的空心圆  false表示直接画成实心圆
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setCircleHoleRadius(3);  //空心圆的圆心半径
        //圆点的颜色     可以实现超过某个值定义成某个颜色的功能   这里先不讲 后面单独写一篇
        lineDataSet.setCircleColor(Color.RED);
        lineDataSet.setCircleRadius(3);      //圆点的半径
        //定义折线上的数据显示    可以实现加单位    以及显示整数（默认是显示小数）
//        lineDataSet.setValueFormatter(new ValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
//                if (entry.getY()==value){
//                    return value+"℃";
//                }
//                return "";
//            }
//        });
    }
    public void setFirstLine(){
        LineDataSet lineDataSet = new LineDataSet(mData,"非工作日");
        LineData lineData = new LineData(lineDataSet);
        line.setData(lineData);

        //折线
        //设置折线的式样   这个是圆滑的曲线（有好几种自己试试）     默认是直线
        //lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setColor(Color.GREEN);  //折线的颜色
        lineDataSet.setLineWidth(2);        //折线的粗细
        //是否画折线点上的空心圆  false表示直接画成实心圆
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setCircleHoleRadius(3);  //空心圆的圆心半径
        //圆点的颜色     可以实现超过某个值定义成某个颜色的功能   这里先不讲 后面单独写一篇
        lineDataSet.setCircleColor(Color.RED);
        lineDataSet.setCircleRadius(3);      //圆点的半径
        //定义折线上的数据显示    可以实现加单位    以及显示整数（默认是显示小数）
//        lineDataSet.setValueFormatter(new ValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
//                if (entry.getY()==value){
//                    return value+"℃";
//                }
//                return "";
//            }
//        });
    }

    public void setLineBG(){
        //折线图背景
        line.setBackgroundColor(0x30000000);   //背景颜色
        line.getXAxis().setDrawGridLines(false);  //是否绘制X轴上的网格线（背景里面的竖线）
        line.getAxisLeft().setDrawGridLines(false);  //是否绘制Y轴上的网格线（背景里面的横线）

    }

    public void setPicture(){
        //图例
        Legend legend=line.getLegend();
        legend.setEnabled(true);    //是否显示图例
        //legend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);    //图例的位置
    }

    public void setLineXY(){
        //X轴
        XAxis xAxis=line.getXAxis();
        xAxis.setDrawGridLines(false);  //是否绘制X轴上的网格线（背景里面的竖线）
        xAxis.setAxisLineColor(Color.RED);   //X轴颜色
        xAxis.setAxisLineWidth(2);           //X轴粗细
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
                    return "中午";
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
        YAxis AxisLeft=line.getAxisLeft();
        AxisLeft.setDrawGridLines(false);  //是否绘制Y轴上的网格线（背景里面的横线）
        AxisLeft.setAxisLineColor(Color.BLUE);  //Y轴颜色
        AxisLeft.setAxisLineWidth(2);           //Y轴粗细
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
        AxisLeft.setAxisMaximum(130);   //Y轴最大数值
        AxisLeft.setAxisMinimum(0);   //Y轴最小数值
        //Y轴坐标的个数    第二个参数一般填false     true表示强制设置标签数 可能会导致X轴坐标显示不全等问题
        AxisLeft.setLabelCount(15,false);

        //是否隐藏右边的Y轴（不设置的话有两条Y轴 同理可以隐藏左边的Y轴）
        line.getAxisRight().setEnabled(false);
    }

    public void setChange(){
        //数据更新
        line.notifyDataSetChanged();
        line.invalidate();
    }

    public void setAnimate(){
        //动画（如果使用了动画可以则省去更新数据的那一步）
        line.animateY(3000); //折线在Y轴的动画  参数是动画执行时间 毫秒为单位
//        line.animateX(2000); //X轴动画
//        line.animateXY(2000,2000);//XY两轴混合动画

    }

}
