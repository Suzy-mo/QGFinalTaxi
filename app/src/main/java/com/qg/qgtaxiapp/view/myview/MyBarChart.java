package com.qg.qgtaxiapp.view.myview;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.BarChart;
import com.qg.qgtaxiapp.utils.MyBarChartRenderer;

/**
 * @author: Hx
 * @date: 2021年08月18日 12:55
 */
public class MyBarChart extends BarChart {

    public MyBarChart(Context context) {
        super(context);
    }

    public MyBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyBarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();
        this.mRenderer = new MyBarChartRenderer(this, this.mAnimator, this.mViewPortHandler);
    }
}
