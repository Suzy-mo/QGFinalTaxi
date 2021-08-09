package com.qg.qgtaxiapp.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import com.qg.qgtaxiapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Hx
 * @date: 2021年08月09日 20:24
 */
public class MyView extends View {
    private Paint paint;
    private float XPoint = 0;
    private float XScale = 0;
    private float YScale = 0;
    private float XLength = 0;
    private float YLength = 0;
    private float DEFAULT_XPoint = 50;
    private float DEFAULT_XScale = 8;
    private float DEFAULT_YScale = 60;
    private float DEFAULT_XLength = 320;
    private float DEFAULT_YLength = 300;
    private int MaxDataSize = 0 ;
    //内存数据
    private List<Float> dataMemory = new ArrayList<Float>();
    //CPU数据
    private List<Float> dataCPU = new ArrayList<Float>();
    //刻度
    private String[] YLabel = new String[]{"0","25","50","75","100"};

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case 1:
                    MyView.this.invalidate();
            }
            return false;
        }
    });

    public MyView(Context context) {
        super(context);
        init();
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttr(context, attrs);
        init();
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs);
        init();
    }

    /**
     * 获得属性值
     * @param context
     * @param attrs
     */
    private void initAttr(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.LineChartView);
        int n = array.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = array.getIndex(i);
            if (attr == R.styleable.LineChartView_XPoint) {
                XPoint = array.getDimension(attr, dp2px(context, DEFAULT_XPoint));
            } else if (attr == R.styleable.LineChartView_XScale) {
                XScale = array.getDimension(attr, dp2px(context, DEFAULT_XScale));
            } else if (attr == R.styleable.LineChartView_YScale) {
                YScale = array.getDimension(attr, dp2px(context, DEFAULT_YScale));
            }else if (attr == R.styleable.LineChartView_XLength) {
                XLength = array.getDimension(attr, dp2px(context, DEFAULT_XLength));
            }else if (attr == R.styleable.LineChartView_YLength) {
                YLength = array.getDimension(attr, dp2px(context, DEFAULT_YLength));
            }
        }
        array.recycle();
    }

    private void init(){
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        //刻度
        YLabel[0]="0";
        YLabel[1]="25";
        YLabel[2]="50";
        YLabel[3]="75";
        YLabel[4]="100";
        //计算最大数据数
        MaxDataSize = Math.round(XLength / XScale);
    }

    /**
     * 更新内存、CPU数据
     * @param newDataMemory
     * @param newDataCPU
     */
    public void setNewData(Float newDataMemory,Float newDataCPU){
        //memory
        if(dataMemory.size() >= MaxDataSize){
            dataMemory.remove(0);
        }
        dataMemory.add(newDataMemory);
        //cpu
        if(dataCPU.size() >= MaxDataSize){
            dataCPU.remove(0);
        }
        dataCPU.add(newDataCPU);
        //update
        handler.sendEmptyMessage(1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(Color.BLUE);
        //画Y轴
        canvas.drawLine(XPoint, 0, XPoint, YLength, paint);
        //Y轴箭头
        canvas.drawLine(XPoint, 0, XPoint - 3, 6, paint);  //箭头
        canvas.drawLine(XPoint, 0, XPoint + 3, 6 ,paint);
        //添加刻度和文字
        for(int i=0; i * YScale < YLength; i++) {
            canvas.drawLine(XPoint, YLength - i * YScale, XPoint + 5, YLength - i * YScale, paint);  //刻度
            canvas.drawText(YLabel[i], XPoint - 30, YLength - i * YScale, paint);//文字
        }
        //画X轴
        canvas.drawLine(XPoint, YLength, XPoint + XLength, YLength, paint);
        //画标识
        paint.setColor(Color.RED);
        canvas.drawText("内存百分比", XLength, 15, paint);//文字
        canvas.drawLine(XLength-20, 15, XLength-10, 15, paint);
        paint.setColor(Color.MAGENTA);
        canvas.drawText("CPU百分比", XLength, 30, paint);//文字
        canvas.drawLine(XLength-20, 30, XLength-10, 30, paint);

        //画内存数据
        paint.setColor(Color.RED);
        if(dataMemory.size() > 1){
            for(int i=1; i<dataMemory.size(); i++){
                canvas.drawLine(XPoint + (i-1) * XScale, YLength - dataMemory.get(i-1) * (YScale*4),
                        XPoint + i * XScale, YLength - dataMemory.get(i) * (YScale*4), paint);
            }
        }
        //画CPU数据
        paint.setColor(Color.MAGENTA);
        if(dataCPU.size() > 1){
            for(int i=1; i<dataCPU.size(); i++){
                canvas.drawLine(XPoint + (i-1) * XScale, YLength - dataCPU.get(i-1) * (YScale*4),
                        XPoint + i * XScale, YLength - dataCPU.get(i) * (YScale*4), paint);
            }
        }
    }

    /**
     * sp2px
     * @param context
     * @param spValue
     * @return
     */
    private int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * dp2px
     * @param context
     * @param dpValue
     * @return
     */
    private int dp2px(Context context, float dpValue) {
        final float densityScale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * densityScale + 0.5f);
    }
}
