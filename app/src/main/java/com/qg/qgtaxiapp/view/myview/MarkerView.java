package com.qg.qgtaxiapp.view.myview;

import android.annotation.SuppressLint;
import android.content.Context;
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
    DecimalFormat df = new DecimalFormat(".00");
    public MarkerView(Context context) {
        super(context, R.layout.markview);

        tvDate = findViewById(R.id.tv_date);
        tvValue = findViewById(R.id.tv_value);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        tvDate.setText(e.getX() + "");
        tvValue.setText("利用率：" + df.format(e.getY()) + "%");
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth()/2),-getHeight());
    }
}
