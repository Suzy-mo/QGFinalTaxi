package view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author: Hx
 * @date: 2021年08月09日 20:24
 */
public class MyView extends View {
    private int XPoint = 60;
    private int YPoint = 260;
    private int XScale = 8;  //刻度长度
    private int YScale = 40;
    private int XLength = 380;
    private int YLength = 240;
    private MyHandler handler = new MyHandler(this);

    private int MaxDataSize = XLength / XScale;
    private List<Integer> data = new ArrayList<Integer>();
    private String[] YLabel = new String[YLength / YScale];

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        for (int i = 0; i < YLabel.length; i++) {
            YLabel[i] = (i + 1) + "M/s";
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(data.size() >= MaxDataSize){
                        data.remove(0);
                    }
                    data.add(new Random().nextInt(4) + 1);
                    handler.sendEmptyMessage(0x1234);
                }
            }
        }).start();
    }

     class MyHandler extends Handler{
        WeakReference<MyView> myViewWeakReference;

        public MyHandler(MyView myView){
            myViewWeakReference = new WeakReference<MyView>(myView);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            MyView myView = myViewWeakReference.get();
            if (myView == null){
                return;
            }
            switch (msg.what){
                case 0:{
                    MyView.this.invalidate();
                }break;
            }

        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true); //去锯齿
        paint.setColor(Color.BLUE);


        //画Y轴
        canvas.drawLine(XPoint, YPoint - YLength, XPoint, YPoint, paint);

        //Y轴箭头
        canvas.drawLine(XPoint, YPoint - YLength, XPoint - 3, YPoint-YLength + 6, paint);  //箭头
        canvas.drawLine(XPoint, YPoint - YLength, XPoint + 3, YPoint-YLength + 6 ,paint);


        //添加刻度和文字
        for(int i=0; i * YScale < YLength; i++) {
            canvas.drawLine(XPoint, YPoint - i * YScale, XPoint + 5, YPoint - i * YScale, paint);  //刻度

            canvas.drawText(YLabel[i], XPoint - 50, YPoint - i * YScale, paint);//文字
        }

        //画X轴
        canvas.drawLine(XPoint, YPoint, XPoint + XLength, YPoint, paint);
        System.out.println("Data.size = " + data.size());
        if(data.size() > 1){
            for(int i=1; i<data.size(); i++){
                canvas.drawLine(XPoint + (i-1) * XScale, YPoint - data.get(i-1) * YScale,
                        XPoint + i * XScale, YPoint - data.get(i) * YScale, paint);
            }
        }



    }
}
