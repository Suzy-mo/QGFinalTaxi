package com.qg.qgtaxiapp.view.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.databinding.FragmentFlowMapBinding;

import java.util.ArrayList;
import java.util.List;

import okhttp3.internal.Util;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/10/17:12
 * @Description:
 */
public class FlowMapFragment extends Fragment {

    private FragmentFlowMapBinding binding;

    private AMap aMap;
    private TextureMapView mapView;

    //请求权限码
    private static final int REQUEST_PERMISSIONS = 9527;

    private UiSettings uiSettings;

    private DistrictSearch districtSearch;
    private DistrictSearchQuery districtSearchQuery;
    private PolygonRunnable polygonRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding=FragmentFlowMapBinding.inflate(inflater,container,false);
        Log.d("=================","daozhelile");
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        initMap();
        binding.choiceTurnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setClick();
            }
        });
    }

    private void initMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
    }

    public void setClick(){
        List<LatLng> latLngs =new ArrayList<>();
        latLngs.add(new LatLng(43.828, 87.621));
        latLngs.add(new LatLng(45.808, 100.55));
        latLngs.add(new LatLng(43.828, 87.621));
        latLngs.add(new LatLng(46.808, 86.55));
        for (int i = 0 ; i < latLngs.size();i+=2){
            setUpMap(latLngs.get(i),latLngs.get(i+1));
        }
    }

    private void setUpMap(LatLng begin,LatLng end) {

        // 设置当前地图级别为4
        aMap.moveCamera(CameraUpdateFactory.zoomTo(4));
        // 设置地图底图文字的z轴指数，默认为0
        aMap.setMapTextZIndex(2);

        // 绘制一个乌鲁木齐到哈尔滨的大地曲线
        aMap.addPolyline((new PolylineOptions())
                .add(begin,end)
                .geodesic(true).color(Color.BLUE));

    }



    /**
     * 方法必须重写
     */
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }


    /**
     * 方法必须重写
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }


}

class PolygonRunnable implements Runnable{
    private DistrictItem item;
    private Handler handler;
    private boolean isCancel = false;
    /**
     * districtBoundary()
     * 以字符串数组形式返回行政区划边界值。
     * 字符串拆分规则： 经纬度，经度和纬度之间用","分隔，坐标点之间用";"分隔。
     * 例如：116.076498,40.115153;116.076603,40.115071;116.076333,40.115257;116.076498,40.115153。
     * 字符串数组由来： 如果行政区包括的是群岛，则坐标点是各个岛屿的边界，各个岛屿之间的经纬度使用"|"分隔。
     * 一个字符串数组可包含多个封闭区域，一个字符串表示一个封闭区域
     */

    public PolygonRunnable(DistrictItem districtItem, Handler handler){
        this.item = districtItem;
        this.handler = handler;
    }

    public void cancel(){
        isCancel = true;
    }

    @Override
    public void run() {

        if (!isCancel){
            try{
                String[] boundary = item.districtBoundary();
                if (boundary != null && boundary.length > 0){
                    Log.d("TAG_Hx","boundary:" + boundary.toString());

                    for (String b : boundary){
                        if (!b.contains("|")){
                            String[] split = b.split(";");
                            PolylineOptions polylineOptions = new PolylineOptions();
                            boolean isFirst = true;
                            LatLng firstLatLng = null;

                            for (String s : split){
                                String[] ll = s.split(",");
                                if (isFirst){
                                    isFirst = false;
                                    firstLatLng = new LatLng(Double.parseDouble(ll[1]),Double.parseDouble(ll[0]));
                                }
                                polylineOptions.add(new LatLng(Double.parseDouble(ll[1]), Double.parseDouble(ll[0])));
                            }
                            if (firstLatLng != null){
                                polylineOptions.add(firstLatLng);
                            }

                            polylineOptions.width(10).color(Color.BLUE).setDottedLine(true);
                            Message message = handler.obtainMessage();

                            message.what = 1;
                            message.obj = polylineOptions;
                            handler.sendMessage(message);
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}