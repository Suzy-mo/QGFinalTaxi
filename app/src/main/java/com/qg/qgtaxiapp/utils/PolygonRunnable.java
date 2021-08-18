package com.qg.qgtaxiapp.utils;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.district.DistrictItem;
import com.qg.qgtaxiapp.R;

/**
 * 画地图边界
 * 获取广州全部边界点的经纬度数据
 */
public class PolygonRunnable implements Runnable {
    private final DistrictItem item;
    private final Handler handler;
    private boolean isCancel = false;

    /**
     * districtBoundary()
     * 以字符串数组形式返回行政区划边界值。
     * 字符串拆分规则： 经纬度，经度和纬度之间用","分隔，坐标点之间用";"分隔。
     * 例如：116.076498,40.115153;116.076603,40.115071;116.076333,40.115257;116.076498,40.115153。
     * 字符串数组由来： 如果行政区包括的是群岛，则坐标点是各个岛屿的边界，各个岛屿之间的经纬度使用"|"分隔。
     * 一个字符串数组可包含多个封闭区域，一个字符串表示一个封闭区域
     */

    public PolygonRunnable(DistrictItem districtItem, Handler handler) {
        this.item = districtItem;
        this.handler = handler;
    }

    public void cancel() {
        isCancel = true;
    }

    @Override
    public void run() {

        if (!isCancel) {
            try {
                String[] boundary = item.districtBoundary();
                if (boundary != null && boundary.length > 0) {
                    Log.d("TAG_Hx", "boundary:" + boundary.toString());

                    for (String b : boundary) {
                        if (!b.contains("|")) {
                            String[] split = b.split(";");
                            PolylineOptions polylineOptions = new PolylineOptions();
                            boolean isFirst = true;
                            LatLng firstLatLng = null;

                            for (String s : split) {
                                String[] ll = s.split(",");
                                if (isFirst) {
                                    isFirst = false;
                                    firstLatLng = new LatLng(Double.parseDouble(ll[1]), Double.parseDouble(ll[0]));
                                }
                                polylineOptions.add(new LatLng(Double.parseDouble(ll[1]), Double.parseDouble(ll[0])));
                            }
                            if (firstLatLng != null) {
                                polylineOptions.add(firstLatLng);
                            }

                            polylineOptions.width(5).color(Color.BLACK).setDottedLine(false);
                            Message message = handler.obtainMessage();

                            message.what = 0;
                            message.obj = polylineOptions;
                            handler.sendMessage(message);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
