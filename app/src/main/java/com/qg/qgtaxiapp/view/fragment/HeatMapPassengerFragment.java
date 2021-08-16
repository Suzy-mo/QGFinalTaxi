package com.qg.qgtaxiapp.view.fragment;

import android.app.AlertDialog;
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
import androidx.viewpager2.widget.ViewPager2;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.AMapGestureListener;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.google.gson.Gson;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.databinding.FragmentHeatmapPassengerBinding;
import com.qg.qgtaxiapp.entity.HeatMapData;
import com.qg.qgtaxiapp.utils.MapUtils;
import com.qg.qgtaxiapp.utils.NetUtils;
import com.qg.qgtaxiapp.utils.PolygonRunnable;
import com.qg.qgtaxiapp.utils.TimePickerUtils;
import com.qg.qgtaxiapp.viewmodel.HeatMapViewModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
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
    private NetUtils netUtils = NetUtils.getInstance();
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
        tv_changeTime = binding.tvPassengerChangeTime;
        tv_date = binding.tvPassengerDate;
        tv_timeslot = binding.tvPassengerTimeslot;
        tv_chooseTime = binding.tvPassengerChooseTime;
        cl_chooseTime = binding.clFragmentPassengerChooseTime;
        cl_timeSet = binding.clFragmentHeatTime;
        aMap = mapUtils.initMap(getContext(),mapView);
        /*
            日期选择监听
         */
        onTimeSelectListener = new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                heatMapViewModel.passengerTime = timePickerUtils.getDate(date);
                showTimeSlotSet();
            }
        };
        datePickerView = timePickerUtils.initDatePicker(getContext(),getActivity(),onTimeSelectListener);

        heatMapViewModel.passengerData.observe(getActivity(), new Observer<List<LatLng>>() {
            @Override
            public void onChanged(List<LatLng> list) {
                aMap.clear();
//                PolygonOptions polygonOptions = new PolygonOptions();
                ArrayList<MarkerOptions> list1 = new ArrayList<>();

                for (LatLng latLng : list){
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.texi));
//                    polygonOptions.add(latLng);
                    list1.add(markerOptions);
                }
                /*polygonOptions.strokeColor(Color.argb(50,1,1,1))
                        .fillColor(Color.argb(1,1,1,1))
                        .strokeWidth(10);
                aMap.addPolygon(polygonOptions);*/
                aMap.addPolyline(heatMapViewModel.polylineOptions);
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
        getTest();
        //getPassengerData();
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
                    heatMapViewModel.passengerData.setValue(mapUtils.initHeatMapData(data));
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
                if (timePickerUtils.h2 != 0 && timePickerUtils.h2 < timePickerUtils.h1) {
                    Toast.makeText(getContext(),"结束时间应高于起始时间",Toast.LENGTH_SHORT).show();
                } else {

                    if (cl_chooseTime.getVisibility() == View.VISIBLE) {
                        cl_chooseTime.setVisibility(View.GONE);
                        cl_timeSet.setVisibility(View.VISIBLE);
                    }
                    heatMapViewModel.passenger_date.setValue(timePickerUtils.getmDate());
                    heatMapViewModel.passenger_timeslot.setValue(timePickerUtils.getTimeslot());
                    heatMapViewModel.passengerTime = timePickerUtils.getTime();

                    dialog.dismiss();
                }
            }
        };
        dialog = timePickerUtils.initTimeSlotDialog(getContext(),onClickListener);
        dialog.show();

        Window window = dialog.getWindow();
        WindowManager manager = getActivity().getWindowManager();
        Display display = manager.getDefaultDisplay();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = (int) (display.getWidth() * 0.98);
        window.setAttributes(params);
    }
    /*
        获取载客热点数据
     */
    private void getPassengerData(){

        netUtils.getPassengerData(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showLog("载客热点数据获取失败");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                Gson gson = new Gson();
                HeatMapData heatMapData = gson.fromJson(json,HeatMapData.class);
                if (heatMapData.getCode() == 1){

                    List<HeatMapData.data> data = heatMapData.getData();
                    Message message = handler.obtainMessage();
                    message.what = 1;
                    message.obj = data;
                    handler.sendMessage(message);
                }else {
                    showLog("无数据");
                }
            }
        });
    }

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
