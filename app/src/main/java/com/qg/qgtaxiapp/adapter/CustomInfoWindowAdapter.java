package com.qg.qgtaxiapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.Marker;
import com.qg.qgtaxiapp.R;

/**
 * @author: Hx
 * @date: 2021年08月17日 16:02
 */
public class CustomInfoWindowAdapter implements AMap.InfoWindowAdapter{

    private Context context;

    public CustomInfoWindowAdapter(Context context) {
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View view = LayoutInflater.from(context).inflate(R.layout.map_info_window,null);
        setViewContent(marker,view);
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
    private void setViewContent(Marker marker, View view) {
        //实例：
        TextView tv_lat = view.findViewById(R.id.tv_infoWindow_lat);
        tv_lat.setText("纬度：" + marker.getPosition().latitude);
        TextView tv_lng = view.findViewById(R.id.tv_infoWindow_lng);
        tv_lng.setText("经度：" + marker.getPosition().longitude);
        TextView tv_title = view.findViewById(R.id.tv_infoWindow_title);
        tv_title.setText("地址：" + marker.getSnippet());
    }


}
