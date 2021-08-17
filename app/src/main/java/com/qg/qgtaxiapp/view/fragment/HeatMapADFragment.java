package com.qg.qgtaxiapp.view.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.AMapGestureListener;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.amap.api.maps.model.animation.TranslateAnimation;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;
import com.google.gson.Gson;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.databinding.FragmentHeatmapAdBinding;
import com.qg.qgtaxiapp.entity.HeatMapData;
import com.qg.qgtaxiapp.utils.MapUtils;
import com.qg.qgtaxiapp.utils.NetUtils;
import com.qg.qgtaxiapp.utils.PolygonRunnable;
import com.qg.qgtaxiapp.viewmodel.HeatMapViewModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Hx
 * @date: 2021年08月14日 23:23
 */
public class HeatMapADFragment extends Fragment {

    private FragmentHeatmapAdBinding binding;
    private HeatMapViewModel heatMapViewModel;
    private MapView mapView;
    private AMap aMap = null;//地图控制器
    private DistrictSearch districtSearch;
    private DistrictSearchQuery districtSearchQuery;
    private PolygonRunnable polygonRunnable;
    private MapUtils mapUtils;
    private NetUtils netUtils = NetUtils.getInstance();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        heatMapViewModel = new ViewModelProvider(getActivity()).get(HeatMapViewModel.class);
        districtSearch = new DistrictSearch(getContext());
        mapUtils = new MapUtils();
        /*
            获取边界数据回调
         */
        districtSearch.setOnDistrictSearchListener(new DistrictSearch.OnDistrictSearchListener() {
            @Override
            public void onDistrictSearched(DistrictResult districtResult) {

                if (districtResult != null && districtResult.getDistrict() != null){
                    if (districtResult.getAMapException().getErrorCode() == AMapException.CODE_AMAP_SUCCESS){

                        ArrayList<DistrictItem> districtItems = districtResult.getDistrict();
                        DistrictItem item = null;
                        if (districtItems != null && districtItems.size() > 0){
                            //广州市 adcode：440100
                            for (DistrictItem districtItem : districtItems){
                                if(districtItem.getAdcode().equals("440100")){
                                    item = districtItem;
                                    break;
                                }
                            }
                            if (item == null){
                                return;
                            }
                            Log.d("TAG_Hx","创建子线程");
                            polygonRunnable = new PolygonRunnable(item,handler);
                            new Thread(polygonRunnable).start();
                        }
                    }
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHeatmapAdBinding.inflate(inflater,container,false);
        mapView = binding.mapAdView;
        mapView.onCreate(savedInstanceState);
        aMap = mapUtils.initMap(getContext(),mapView);

        aMap.addOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.isInfoWindowShown()) {
                    marker.hideInfoWindow();
                } else {
                    marker.showInfoWindow();
                }
                LatLng latLng = new LatLng(marker.getPosition().latitude + 0.01,marker.getPosition().longitude);
                Animation animation = new TranslateAnimation(latLng);
                animation.setDuration(100L);
                animation.setInterpolator(new LinearInterpolator());

                marker.setAnimation(animation);
                marker.startAnimation();
                return true;
            }
        });
        heatMapViewModel.adData.observe(getActivity(), new Observer<List<LatLng>>() {
            @Override
            public void onChanged(List<LatLng> list) {

                ArrayList<MarkerOptions> list1 = new ArrayList<>();

                for (LatLng latLng : list){
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(latLng)
                            .title("AD")
                            .snippet(latLng.toString())
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ad));
                    list1.add(markerOptions);
                    aMap.addMarkers(list1,false);
                }
            }
        });
        /*
            设置屏幕滑动检测
            用于区分滑动地图还是滑动页面
         */
        AMapGestureListener aMapGestureListener = new AMapGestureListener() {
            @Override
            public void onDoubleTap(float v, float v1) {
            }
            @Override
            public void onSingleTap(float v, float v1) {
            }
            @Override
            public void onFling(float v, float v1) {
            }
            @Override
            public void onLongPress(float v, float v1) {
            }
            @Override
            public void onDown(float v, float v1) {
            }
            @Override
            public void onMapStable() {
            }
            @Override
            public void onScroll(float v, float v1) {
                heatMapViewModel.heatMapVP.setUserInputEnabled(false);
            }
            @Override
            public void onUp(float v, float v1) {
                heatMapViewModel.heatMapVP.setUserInputEnabled(true);
            }
        };
        aMap.setAMapGestureListener(aMapGestureListener);
        drawBoundary();
        getTest();
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null){
            mapView.onDestroy();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * Toast提示
     * @param msg 提示内容
     */
    private void showMsg(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
    /**
     * Log.d打印日志
     */
    private void showLog(String log){
        Log.d("TAG_ADFragment", log);
    }

    /*
        获取边界点的设置
     */
    private void drawBoundary(){
        if (heatMapViewModel.polylineOptions != null){
            aMap.addPolyline(heatMapViewModel.polylineOptions);
            return;
        }
        String city = "广州";
        districtSearchQuery = new DistrictSearchQuery();
        //设置关键字
        districtSearchQuery.setKeywords(city);
        //设置是否返回边界值
        districtSearchQuery.setShowBoundary(true);
        districtSearch.setQuery(districtSearchQuery);
        districtSearch.searchDistrictAsyn();
    }

    /*
       消息处理
    */
    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){

                case 0:{
                    PolylineOptions options = (PolylineOptions) msg.obj;
                    aMap.addPolyline(options);
                    heatMapViewModel.polylineOptions = options;
                }break;

                case 1:{
                    List<HeatMapData.data> data = (List<HeatMapData.data>) msg.obj;
                    heatMapViewModel.adData.setValue(mapUtils.initHeatMapData(data));
                }break;
            }
        }
    };

    private void getTest(){

        InputStream fis = null;
        try {
            fis = getResources().getAssets().open("test.txt");

            int size = fis.available();

            byte[] bytes = new byte[size];
            fis.read(bytes);
            fis.close();
            String s = new String(bytes,"UTF-8");
            Gson gson = new Gson();
            HeatMapData heatMapData = gson.fromJson(s,HeatMapData.class);
            List<HeatMapData.data> data = heatMapData.getData();
            Message message = handler.obtainMessage();
            message.what = 1;
            message.obj = data;
            handler.sendMessage(message);
            showLog(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
