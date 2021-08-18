package com.qg.qgtaxiapp.view.activity;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.databinding.ActivityTestChartBinding;
import com.qg.qgtaxiapp.view.myview.MarkerView;

import java.util.ArrayList;
import java.util.List;

public class TestChartActivity extends AppCompatActivity {

    private ActivityTestChartBinding binding;
    private LineChart lineChart;
    private final List list = new ArrayList();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestChartBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        lineChart = binding.LineChart;
        //边界
        lineChart.setDrawBorders(false);
        lineChart.setDrawGridBackground(false);

        //去掉标签
        lineChart.getDescription().setEnabled(false);

        //设置X轴
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//位置
        xAxis.setGranularity(1f);//间隔
        xAxis.setLabelCount(12,false);//刻度数量
        xAxis.setAxisMinimum(1f);//最小值
        // xAxis.setAxisMaximum(20f);//最大值

        //设置Y轴
        YAxis leftYAxis = lineChart.getAxisLeft();
        YAxis rightYAxis = lineChart.getAxisRight();
        rightYAxis.setDrawGridLines(false);
        leftYAxis.setDrawGridLines(true);
        leftYAxis.enableGridDashedLine(10f,10f,0f);
        leftYAxis.setAxisMinimum(0f);
        leftYAxis.setAxisMaximum(100f);

        //rightYAxis.setAxisMinimum(0f);
        //rightYAxis.setAxisMaximum(100f);
        rightYAxis.setEnabled(false);

        leftYAxis.setValueFormatter(new IndexAxisValueFormatter(){
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "%";
            }
        });


        //数据
        List<Entry> entries = new ArrayList<>();
        for (int i = 1; i < 13; i++) {
            entries.add(new Entry(i, (float) (Math.random()) * 80));
        }
        //一个LineDataSet就是一条线
        LineDataSet lineDataSet = new LineDataSet(entries,"出租车利用率");

        lineDataSet.setDrawCircleHole(false);//设置圆点是否空心
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);//设置为曲线
        lineDataSet.setDrawValues(false);//不显示值
        //设置颜色
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillDrawable(getDrawable(R.drawable.linechart_linestyle));
        LineData data = new LineData(lineDataSet);
        lineChart.setData(data);

        setMarketView();
    }


    public void setMarketView(){
        MarkerView markerView = new MarkerView(this);
        markerView.setChartView(lineChart);
        lineChart.setMarker(markerView);
        lineChart.invalidate();
    }
}