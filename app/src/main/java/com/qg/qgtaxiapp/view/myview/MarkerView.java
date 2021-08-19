package com.qg.qgtaxiapp.view.myview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.qg.qgtaxiapp.R;

import java.text.DecimalFormat;

/**
 * @author: Hx
 * @date: 2021年08月10日 15:26
 */
public class MarkerView extends com.github.mikephil.charting.components.MarkerView {

    private final TextView tvDate;
    private final TextView tvValue;
    private DecimalFormat df = new DecimalFormat(" ");
    public MarkerView(Context context) {
        super(context, R.layout.markview);

        tvDate = findViewById(R.id.tv_date);
        tvValue = findViewById(R.id.tv_value);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        tvDate.setText("时间：" + df.format(e.getX()) + "时");
        tvValue.setText("流量：" + df.format(e.getY()) + "辆");
        super.refreshContent(e, highlight);

    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth()/2), (float) (-getHeight() * 1.5));
    }
}
