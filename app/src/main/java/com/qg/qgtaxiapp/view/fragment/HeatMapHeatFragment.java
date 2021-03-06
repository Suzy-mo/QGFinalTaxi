package com.qg.qgtaxiapp.view.fragment;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.AMapGestureListener;
import com.amap.api.maps.model.HeatmapTileProvider;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.TileOverlayOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.google.gson.Gson;
import com.qg.qgtaxiapp.databinding.FragmentHeatmapHeatBinding;
import com.qg.qgtaxiapp.entity.HeatMapData;
import com.qg.qgtaxiapp.utils.MapUtils;
import com.qg.qgtaxiapp.utils.NetUtils;
import com.qg.qgtaxiapp.utils.PolygonRunnable;
import com.qg.qgtaxiapp.utils.TimePickerUtils;
import com.qg.qgtaxiapp.viewmodel.HeatMapViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author: Hx
 * @date: 2021年08月14日 23:21
 */
public class HeatMapHeatFragment extends Fragment {

    private FragmentHeatmapHeatBinding binding;
    private HeatMapViewModel heatMapViewModel;
    private MapView mapView;
    private AMap aMap = null;//地图控制器
    private DistrictSearch districtSearch;
    private DistrictSearchQuery districtSearchQuery;
    private PolygonRunnable polygonRunnable;
    private TimePickerView datePickerView;
    private TimePickerUtils timePickerUtils;
    private MapUtils mapUtils;
    private AlertDialog dialog = null;
    private TextView tv_changeTime;
    private TextView tv_date;
    private TextView tv_timeslot;
    private TextView tv_chooseTime;
    private ConstraintLayout cl_timeSet;
    private ConstraintLayout cl_chooseTime;
    private final NetUtils netUtils = NetUtils.getInstance();
    private HeatmapTileProvider heatmapTileProvider;
    private TileOverlayOptions tileOverlayOptions;
    private OnTimeSelectListener onTimeSelectListener;
    private View.OnClickListener onClickListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        heatMapViewModel = new ViewModelProvider(getActivity()).get(HeatMapViewModel.class);
        districtSearch = new DistrictSearch(getContext());
        timePickerUtils = new TimePickerUtils();
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
                            polygonRunnable = new PolygonRunnable(item,handler);
                            new Thread(polygonRunnable).start();
                        }
                    }else {
                        showLog(districtResult.getAMapException().getErrorMessage());
                    }
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHeatmapHeatBinding.inflate(inflater,container,false);
        mapView = binding.mapHeatView;
        mapView.onCreate(savedInstanceState);
        tv_changeTime = binding.tvHeatChangeTime;
        tv_date = binding.tvHeatDate;
        tv_timeslot = binding.tvHeatTimeslot;
        tv_chooseTime = binding.tvHeatChooseTime;
        cl_chooseTime = binding.clFragmentHeatChooseTime;
        cl_timeSet = binding.clFragmentHeatTime;
        aMap = mapUtils.initMap(getContext(),mapView);
        /*
            日期选择监听
         */
        onTimeSelectListener = new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                heatMapViewModel.heatTime = timePickerUtils.getDate(date);
                timePickerUtils.setmDate(heatMapViewModel.heatTime);
                showTimeSlotSet();
            }
        };
        datePickerView = timePickerUtils.initDatePicker(getContext(),getActivity(),onTimeSelectListener);

        heatMapViewModel.heatData.observe(getActivity(), new Observer<List<LatLng>>() {
            @Override
            public void onChanged(List<LatLng> list) {
                binding.pbHeat.setVisibility(View.GONE);
                aMap.clear();
                heatmapTileProvider = mapUtils.initBuildHeatmapTileProvider(list);
                tileOverlayOptions = new TileOverlayOptions();
                tileOverlayOptions.tileProvider(heatmapTileProvider);
                aMap.addPolyline(heatMapViewModel.polylineOptions);
                aMap.addTileOverlay(tileOverlayOptions);
            }
        });

        /*
            时间监测
         */
        heatMapViewModel.heat_date.observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tv_date.setText(s);
            }
        });
        heatMapViewModel.heat_timeslot.observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tv_timeslot.setText(s);
            }
        });


        tv_changeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerView.show();
            }
        });

        tv_chooseTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerView.show();
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
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
        Log.d("TAG_HeatMapFragment", log);
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
    private final Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){

                case 0:{
                    PolylineOptions options = (PolylineOptions) msg.obj;
                    aMap.addPolyline(options);
                    heatMapViewModel.polylineOptions = options;
                }break;

                case 1:{
                    if (cl_chooseTime.getVisibility() == View.VISIBLE) {
                        cl_chooseTime.setVisibility(View.GONE);
                        cl_timeSet.setVisibility(View.VISIBLE);
                    }

                    List<HeatMapData.data> data = (List<HeatMapData.data>) msg.obj;
                    heatMapViewModel.heatData.setValue(mapUtils.initHeatMapData(data));
                }break;

                case 2:{
                    binding.pbHeat.setVisibility(View.GONE);
                    showMsg("获取数据失败");
                }break;
            }
        }
    };

    /*
        显示时间段设置Dialog
     */
    private void showTimeSlotSet(){
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((timePickerUtils.h2 < timePickerUtils.h1)
                        || (timePickerUtils.h2 == timePickerUtils.h1 && timePickerUtils.m2 < timePickerUtils.m1 )
                        || (timePickerUtils.h2 == timePickerUtils.h1 && timePickerUtils.m2 == timePickerUtils.m1 )) {
                    Toast.makeText(getContext(),"结束时间应高于起始时间",Toast.LENGTH_SHORT).show();
                } else {

                    heatMapViewModel.heat_date.setValue(timePickerUtils.getmDate());
                    heatMapViewModel.heat_timeslot.setValue(timePickerUtils.getTimeslot());
                    heatMapViewModel.heatTime = timePickerUtils.getTime();
                    showLog(heatMapViewModel.heatTime);

                    getHeatData();
                    binding.pbHeat.setVisibility(View.VISIBLE);
                    dialog.dismiss();
                }
            }
        };
        dialog = timePickerUtils.initTimeSlotDialog(getContext(), getActivity(), onClickListener);
        timePickerUtils.reSetTime();
        dialog.show();

        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager manager = getActivity().getWindowManager();
        Display display = manager.getDefaultDisplay();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = (int) (display.getWidth() * 0.9);
        window.setAttributes(params);
    }

    /*
        获取热力图数据
     */
    private void getHeatData(){

        netUtils.getHeatMapData(heatMapViewModel.heatTime, 0, 2000, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showLog("热力图数据获取失败");
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
                HeatMapData heatMapData = gson.fromJson(json,HeatMapData.class);
                if (heatMapData != null && heatMapData.getCode() == 1){
                    List<HeatMapData.data> data = heatMapData.getData();

                    message.what = 1;
                    message.obj = data;
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