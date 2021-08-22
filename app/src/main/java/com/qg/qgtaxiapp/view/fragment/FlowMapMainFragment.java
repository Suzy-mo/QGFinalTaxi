package com.qg.qgtaxiapp.view.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.AMapGestureListener;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.qg.qgtaxiapp.databinding.FragmentFlowMapMainBinding;
import com.qg.qgtaxiapp.entity.BaseCreator;
import com.qg.qgtaxiapp.entity.FlowMainDataArea;
import com.qg.qgtaxiapp.entity.FlowMainDataLine;
import com.qg.qgtaxiapp.entity.IPost;
import com.qg.qgtaxiapp.utils.MapUtils;
import com.qg.qgtaxiapp.utils.PolygonRunnable;
import com.qg.qgtaxiapp.utils.TimePickerUtils;
import com.qg.qgtaxiapp.viewmodel.FlowMapViewModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @Name：QGTaxiApp
 * @Description：
 * @Author：Suzy.Mo
 * @Date：2021/8/20 8:45
 */
public class FlowMapMainFragment extends Fragment {

    private FragmentFlowMapMainBinding binding;
    private FlowMapViewModel viewModel;

    private MapView mapView;
    private AMap aMap = null;//地图控制器
    private DistrictSearch districtSearch;
    private DistrictSearchQuery districtSearchQuery;
    private PolygonRunnable polygonRunnable;
    private TextView tv_setTime,tv_date,tv_choose,tv_timeTable;
    private TimePickerView datePickerView;
    private TimePickerUtils timePickerUtils;
    private MapUtils mapUtils;
    private List<Polyline> mainPolyLines = new ArrayList<>() ;//全流图和主流向图的画图工具
    private List<Circle> circles = new ArrayList<>();
    private AMapGestureListener listener;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(getActivity()).get(FlowMapViewModel.class);
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
        binding = FragmentFlowMapMainBinding.inflate(inflater,container,false);
        mapView = binding.flowMap;
        mapView.onCreate(savedInstanceState);
        tv_setTime = binding.tvSetTime;
        tv_date = binding.tvDate;
        tv_choose = binding.tvHeatChooseTime;
        tv_timeTable = binding.tvTimeLabel;

        initFirstView();

        aMap = mapUtils.initMap(getContext(),mapView);
        initMapListener();
        aMap.setAMapGestureListener(listener);
        drawBoundary();
        datePickerView = timePickerUtils.initFlowDatePicker(getContext(), getActivity(), new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                showLog("onTimeSelect: 选择时间"+timePickerUtils.getDate(date));
                getMainData(timePickerUtils.getDate(date));
                tv_date.setText(timePickerUtils.getDate(date));
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        clearMap();
        super.onResume();
    }

    private void initMapListener() {
        listener=new AMapGestureListener() {
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
            public void onScroll(float v, float v1) {
                viewModel.vp2.setUserInputEnabled(false);
            }

            @Override
            public void onLongPress(float v, float v1) {

            }

            @Override
            public void onDown(float v, float v1) {

            }

            @Override
            public void onUp(float v, float v1) {
                viewModel.vp2.setUserInputEnabled(true);
            }

            @Override
            public void onMapStable() {

            }
        };
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initListener();
        initArea();

        setMainDataObserve();

        //super.onViewCreated(view, savedInstanceState);
    }

    private void initArea() {
        showLog("getMainArea 进入需求区域的设置");
        IPost iPost = BaseCreator.createMain(IPost.class);
        iPost.getFlowMainDataArea().enqueue(new Callback<FlowMainDataArea>() {
            @Override
            public void onResponse(Call<FlowMainDataArea> call, Response<FlowMainDataArea> response) {
                showLog(response.toString());
                if (response.isSuccessful()) {
                    showLog("getMainData: onResponse: 拿到数据");
                    circles = mapUtils.setMainAreaCircle(response.body(), aMap);
                } else {
                    showMsg("暂时没有相关数据");
                }
            }

            @Override
            public void onFailure(Call<FlowMainDataArea> call, Throwable t) {
                showLog("获取数据失败");
            }
        });
    }


    private void setMainDataObserve() {

        viewModel.MainDataLine.observe(getActivity(), new Observer<FlowMainDataLine>() {
            @Override
            public void onChanged(FlowMainDataLine flowMainDataLine) {
                clearMap();
                showLog("主流图数据变化监听成功");
                mainPolyLines = mapUtils.setFlowMainLines(flowMainDataLine, aMap);
                showLog("主流图更新成功");
            }
        });

    }



    /**
     * 设置控件的监听器
     */
    private void initListener() {
        tv_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initLastView();
                datePickerView.show();
            }
        });

        tv_setTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerView.show();
            }
        });

    }

    /**
     * 设置初始的界面
     */
    private void initFirstView() {
        tv_choose.setVisibility(View.VISIBLE);
        tv_setTime.setVisibility(View.INVISIBLE);
        tv_date.setVisibility(View.INVISIBLE);
        tv_timeTable.setVisibility(View.INVISIBLE);
    }

    /**
     * 点击修改时间后
     */
    private void initLastView() {
        tv_choose.setVisibility(View.INVISIBLE);
        tv_setTime.setVisibility(View.VISIBLE);
        tv_date.setVisibility(View.VISIBLE);
        tv_timeTable.setVisibility(View.VISIBLE);
    }

    private void getMainData(String s) {
        if(s!=null){
            showLog("getMainData : 开子线程去获取");
            IPost iPost = BaseCreator.createMain(IPost.class);
            iPost.getFlowMainDataLine(s).enqueue(new Callback<FlowMainDataLine>() {
                @Override
                public void onResponse(Call<FlowMainDataLine> call, Response<FlowMainDataLine> response) {
                    showLog(response.toString());
                    if (response.isSuccessful()) {
                        showLog("getMainData: onResponse: 拿到数据");
                        if(response.body()!=null&&!s.equals("2017-02-28")){
                            viewModel.MainDataLine.setValue(response.body());
                            showLog("拿到的第一个数据是：" + response.body().getData().get(0).getLocation().get(0).getLatitude() + "\n主流图拿到数成功");
                        }else {
                            showLog("getMainData：数据为空");
                        }

                    } else {
                        showMsg("暂时没有相关数据");
                    }
                }

                @Override
                public void onFailure(Call<FlowMainDataLine> call, Throwable t) {
                    showLog("获取数据失败");
                }
            });
        }else {
            Toast.makeText(getContext(),"请选择要查看的日期",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null){
            mapView.onDestroy();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
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
        Log.d("TAG_FlowMapFragment", log);
    }

    /*
        获取边界点的设置
     */
    private void drawBoundary(){
        if (viewModel.polylineOptions != null){
            aMap.addPolyline(viewModel.polylineOptions);
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
                    viewModel.polylineOptions = options;
                }break;
            }
        }
    };

    private void clearMap(){
        showLog("clearMap: 清空划线");
        for (int i = 0 ; i <mainPolyLines.size(); i++){
            mainPolyLines.get(i).setVisible(false);
        }
        mainPolyLines.clear();
    }
}
