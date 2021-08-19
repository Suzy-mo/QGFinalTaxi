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
    public LineChartsUtils() {

    }



    public void setNowLine(LineChart line, List<Entry> workdayData,List<Entry> weekendData){
        LineData lineData = new LineData(setLineData(workdayData,"#FF1E6BFF"),setLineData(weekendData,"#FF03DAC5"));
        line.setData(lineData);
        setLineBG(line);
        setLineXY(line);
        setChange(line);
        setPicture(line);
        setAnimate(line);
    }

    public void setFeatureLine(LineChart line, List<Entry>mData){
        LineData lineData = new LineData(setLineData(mData,"#FF03DAC5"));
        line.setData(lineData);
        setLineBG(line);
        setLineXY(line);
        setPicture(line);
        setChange(line);
        //setAnimate(line);
    }

    public LineDataSet setLineData(List<Entry> mData,String color){
        LineDataSet lineDataSet = new LineDataSet(mData,"工作日");
        //折线
        //设置折线的式样   这个是圆滑的曲线（有好几种自己试试）     默认是直线
        //lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setColor(Color.parseColor(color));  //折线的颜色
        lineDataSet.setLineWidth(2);        //折线的粗细
        //是否画折线点上的空心圆  false表示直接画成实心圆
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setCircleHoleRadius(3);  //空心圆的圆心半径
        //圆点的颜色     可以实现超过某个值定义成某个颜色的功能   这里先不讲 后面单独写一篇
        lineDataSet.setCircleColor(Color.parseColor(color));
        lineDataSet.setCircleRadius(3);      //圆点的半径
        //定义折线上的数据显示    可以实现加单位    以及显示整数（默认是显示小数）
        lineDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                return " ";
            }
        });
        return lineDataSet;
    }

    public void setLineBG(LineChart line){
        //折线图背景
        //line.setBackgroundColor(0x30000000);   //背景颜色
        line.getXAxis().setDrawGridLines(false);  //是否绘制X轴上的网格线（背景里面的竖线）
        line.getAxisLeft().setDrawGridLines(false);  //是否绘制Y轴上的网格线（背景里面的横线）

    }

    public void setPicture(LineChart line){
        //图例
        Legend legend=line.getLegend();
        legend.setEnabled(false);    //是否显示图例
        line.getDescription().setEnabled(false);
        //legend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);    //图例的位置
    }

    public void setLineXY(LineChart line){
        //X轴
        XAxis xAxis=line.getXAxis();
        xAxis.setDrawGridLines(false);  //是否绘制X轴上的网格线（背景里面的竖线）
        xAxis.setAxisLineColor(Color.parseColor("#61FFFFFF"));   //X轴颜色
        xAxis.setAxisLineWidth(3);           //X轴粗细
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);        //X轴所在位置   默认为上面
        xAxis.setTextColor(Color.parseColor("#61FFFFFF"));
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                for(int i = 1 ; i < 24 ; i ++){
                    if (value == i){
                        return String.valueOf(i);
                    }
                }
                return "";//注意这里需要改成 ""
            }
        });

        xAxis.setAxisMaximum(25);   //X轴最大数值
        xAxis.setAxisMinimum(0);   //X轴最小数值
        xAxis.setGranularity(2f); //设置X轴坐标间隔
        //X轴坐标的个数    第二个参数一般填false     true表示强制设置标签数 可能会导致X轴坐标显示不全等问题
        xAxis.setLabelCount(24,false);


        //Y轴
        YAxis AxisLeft=line.getAxisLeft();
        AxisLeft.setDrawGridLines(true);  //是否绘制Y轴上的网格线（背景里面的横线）
        AxisLeft.setAxisLineColor(Color.argb(1,41,41,41));  //Y轴颜色
        AxisLeft.setGridColor(Color.parseColor("#61FFFFFF"));
        AxisLeft.setGridLineWidth(1);
        AxisLeft.setAxisLineWidth(3);           //Y轴粗细

        AxisLeft.setTextColor(Color.parseColor("#61FFFFFF"));
        AxisLeft.setAxisMaximum(3600f);   //Y轴最大数值
        AxisLeft.setAxisMinimum(0f);   //Y轴最小数值
        AxisLeft.setGranularity(600f); //Y轴标签间隔
        //Y轴坐标的个数    第二个参数一般填false     true表示强制设置标签数 可能会导致X轴坐标显示不全等问题
        AxisLeft.setLabelCount(7,true);

        //是否隐藏右边的Y轴（不设置的话有两条Y轴 同理可以隐藏左边的Y轴）
        line.getAxisRight().setEnabled(false);
        line.getAxisLeft().setEnabled(true);
    }

    public void setChange(LineChart line){
        //数据更新
        line.notifyDataSetChanged();
        line.invalidate();
    }

    public void setAnimate(LineChart line){
        //动画（如果使用了动画可以则省去更新数据的那一步）
        line.animateY(1000); //折线在Y轴的动画  参数是动画执行时间 毫秒为单位
    }

}
