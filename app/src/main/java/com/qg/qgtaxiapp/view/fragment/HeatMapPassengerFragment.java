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

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
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
import com.qg.qgtaxiapp.databinding.FragmentHeatmapPassengerBinding;
import com.qg.qgtaxiapp.entity.HeatMapData;
import com.qg.qgtaxiapp.utils.MapUtils;
import com.qg.qgtaxiapp.utils.NetUtils;
import com.qg.qgtaxiapp.utils.PolygonRunnable;
import com.qg.qgtaxiapp.utils.TimePickerUtils;
import com.qg.qgtaxiapp.viewmodel.HeatMapViewModel;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
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
    private String mDate = null;

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

        /*
            日期选择监听
         */
        onTimeSelectListener = new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                mDate = timePickerUtils.getDate(date);
                showTimeSlotSet();

            }
        };
        datePickerView = timePickerUtils.initDatePicker(getContext(),getActivity(),onTimeSelectListener);

        aMap = mapUtils.initMap(getContext(),mapView);
        drawBoundary();
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
    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){

                case 0:{
                    PolylineOptions options = (PolylineOptions) msg.obj;
                    aMap.addPolyline(options);
                    heatMapViewModel.polylineOptions = options;
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
                    heatMapViewModel.passenger_date.setValue(mDate);
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

}