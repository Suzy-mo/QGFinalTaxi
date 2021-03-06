package com.qg.qgtaxiapp.view.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.amap.api.services.core.AMapException;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.google.gson.Gson;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.adapter.CustomInfoWindowAdapter;
import com.qg.qgtaxiapp.databinding.FragmentHeatmapPassengerBinding;
import com.qg.qgtaxiapp.entity.HeatMapData;
import com.qg.qgtaxiapp.utils.MapUtils;
import com.qg.qgtaxiapp.utils.NetUtils;
import com.qg.qgtaxiapp.utils.PolygonRunnable;
import com.qg.qgtaxiapp.viewmodel.HeatMapViewModel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author: Hx
 * @date: 2021年08月14日 23:23
 */
public class HeatMapPassengerFragment extends Fragment {

    private FragmentHeatmapPassengerBinding binding;
    private HeatMapViewModel heatMapViewModel;
    private MapView mapView;
    private AMap aMap = null;//地图控制器
    private DistrictSearch districtSearch;
    private DistrictSearchQuery districtSearchQuery;
    private PolygonRunnable polygonRunnable;
    private MapUtils mapUtils;
    private final NetUtils netUtils = NetUtils.getInstance();
    private GeocodeSearch geocodeSearch;
    private Marker mMarker;
    private GeocodeSearch.OnGeocodeSearchListener geocodeSearchListener;
    private CustomInfoWindowAdapter customInfoWindowAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        heatMapViewModel = new ViewModelProvider(getActivity()).get(HeatMapViewModel.class);
        districtSearch = new DistrictSearch(getContext());
        mapUtils = new MapUtils();
        geocodeSearch = new GeocodeSearch(getContext());
        customInfoWindowAdapter = new CustomInfoWindowAdapter(getContext());
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
        binding = FragmentHeatmapPassengerBinding.inflate(inflater,container,false);
        mapView = binding.mapPassengerView;
        mapView.onCreate(savedInstanceState);
        aMap = mapUtils.initMap(getContext(),mapView);
        aMap.setInfoWindowAdapter(customInfoWindowAdapter);

        /*
            InfoWindow点击回调
         */
        aMap.setOnInfoWindowClickListener(new AMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                mMarker.hideInfoWindow();
            }
        });

        /*
            坐标逆编回调
         */
        geocodeSearchListener = new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                mMarker.setSnippet(regeocodeResult.getRegeocodeAddress().getFormatAddress());
                if (mMarker.isInfoWindowShown()) {
                    mMarker.hideInfoWindow();
                } else {
                    mMarker.showInfoWindow();
                }
            }
            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

            }
        };
        geocodeSearch.setOnGeocodeSearchListener(geocodeSearchListener);

        /*
            Marker点击事件
         */
        aMap.addOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mMarker = marker;
                if (marker.getSnippet() == null){
                    mapUtils.getAddress(marker.getPosition(),geocodeSearch);
                }else {
                    if (mMarker.isInfoWindowShown()) {
                        mMarker.hideInfoWindow();
                    } else {
                        mMarker.showInfoWindow();
                    }
                }
                return true;
            }
        });
        heatMapViewModel.passengerData.observe(getActivity(), new Observer<List<LatLng>>() {
            @Override
            public void onChanged(List<LatLng> list) {

                ArrayList<MarkerOptions> list1 = new ArrayList<>();

                for (LatLng latLng : list){
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(latLng)
                            .draggable(false)
                            .title(latLng.toString())
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.texi));

                    list1.add(markerOptions);
                }
                binding.pbPassenger.setVisibility(View.GONE);
                aMap.addMarkers(list1,false);
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

        if (heatMapViewModel.passengerData.getValue() == null){
            binding.pbPassenger.setVisibility(View.VISIBLE);
            getPassengerData();
        }

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
        Log.d("TAG_PassengerFragment", log);
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
    private final Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){

                case 0:{
                    PolylineOptions options = (PolylineOptions) msg.obj;
                    aMap.addPolyline(options);
                    heatMapViewModel.polylineOptions = options;
                }break;

                case 1:{
                    List<LatLng> list = (List<LatLng>) msg.obj;
                    heatMapViewModel.passengerData.setValue(list);
                }break;

                case 2:{
                    binding.pbPassenger.setVisibility(View.GONE);
                    showMsg("获取数据失败");
                }break;
            }
        }
    };

    /*
        获取载客热点数据
     */
    private void getPassengerData(){

        netUtils.getPassengerData(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showLog("载客热点数据获取失败");
                Message message = handler.obtainMessage();
                message.what = 2;
                handler.sendMessage(message);
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String json = response.body().string();
                Gson gson = new Gson();
                Message message = handler.obtainMessage();
                HeatMapData heatMapData = gson.fromJson(json, HeatMapData.class);
                if (heatMapData != null && heatMapData.getCode() == 1) {
                    List<HeatMapData.data> data = heatMapData.getData();
                    List<LatLng> latLngs = mapUtils.initHeatMapData(data);
                    message.what = 1;
                    message.obj = latLngs;
                    handler.sendMessage(message);
                }else {
                    message.what = 2;
                    handler.sendMessage(message);
                    showLog("无数据");
                }

            }
        });
    }
}
