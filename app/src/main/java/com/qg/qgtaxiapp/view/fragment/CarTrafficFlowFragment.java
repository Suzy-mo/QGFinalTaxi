package com.qg.qgtaxiapp.view.fragment;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.databinding.FragmentCarTraficFlowBinding;
import com.qg.qgtaxiapp.databinding.PoWindowLineBinding;
import com.qg.qgtaxiapp.entity.BaseCreator;
import com.qg.qgtaxiapp.entity.CarLineChartBean;
import com.qg.qgtaxiapp.entity.CarTrafficMarkBean;
import com.qg.qgtaxiapp.entity.IPost;
import com.qg.qgtaxiapp.utils.LineChartsUtils;
import com.qg.qgtaxiapp.utils.MapUtils;
import com.qg.qgtaxiapp.utils.PolygonRunnable;
import com.qg.qgtaxiapp.viewmodel.CarTrafficViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @Name：QGTaxiApp
 * @Description：
 * @Author：Suzy.Mo
 * @Date：2021/8/16 23:18
 */
public class CarTrafficFlowFragment extends Fragment {

    private final int NOW_LINE = 0,FEATURE = 1;
    private FragmentCarTraficFlowBinding binding;
    private PoWindowLineBinding pwBinding;
    private CarTrafficViewModel viewModel;

    private MapView mapView;
    private AMap aMap = null;//地图控制器
    private DistrictSearch districtSearch;
    private DistrictSearchQuery districtSearchQuery;
    private PolygonRunnable polygonRunnable;
    private MapUtils mapUtils;
    private List<Marker> markers ;

    private PopupWindow popupWindow;
    private View rootView,contentView;
    ImageView backIV,chooseIv ;
    LineChart lineChart;
    List<Entry> lineCharData;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        districtSearch = new DistrictSearch(getContext());
        viewModel = new ViewModelProvider(getActivity()).get(CarTrafficViewModel.class);
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
        binding = FragmentCarTraficFlowBinding.inflate(inflater,container,false);
        binding.windowsPwIv.setVisibility(View.INVISIBLE);
        mapView = binding.carTrafficFlowMap;
        mapView.onCreate(savedInstanceState);
        aMap = mapUtils.initMap(getContext(),mapView);
        drawBoundary();

        initMapMarkers();
        setMarkerListener();

        setLineDataObserve();
        setChooseObserve();
        return binding.getRoot();
    }

    private void setChooseObserve() {
    }

    private void setLineDataObserve() {
        viewModel.lineChartData.observe(getActivity(), new Observer<CarLineChartBean>() {
            @Override
            public void onChanged(CarLineChartBean carLineChartBean) {
                if(carLineChartBean!=null){
                    showPopupWindows(carLineChartBean);
                }
            }
        });
    }

    private void showPopupWindows(CarLineChartBean carLineChartBean) {
        viewModel.choose.setValue(NOW_LINE);
        binding.windowsPwIv.setVisibility(View.VISIBLE);
        pwBinding = PoWindowLineBinding.inflate(LayoutInflater.from(getContext()),null,false);
        popupWindow = new PopupWindow(getContext());
        contentView = LayoutInflater.from(getContext()).inflate(R.layout.po_window_line,null);
        popupWindow.setContentView(contentView);//加载子布局
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);//设置大小
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(getActivity().getDrawable(R.drawable.po_windows_bg));//设置背景
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_car_trafic_flow,null);//加载父布局
        popupWindow.showAtLocation(rootView, Gravity.CENTER,0,0);//设置位置
        popupWindow.setOutsideTouchable(true);//点击外部可消失
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        //popupWindow.getBackground().setAlpha(50);

        setCharline(carLineChartBean);
        binding.windowsPwIv.setVisibility(View.VISIBLE);
        binding.windowsPwIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                binding.windowsPwIv.setVisibility(View.INVISIBLE);
            }
        });

        backIV = contentView.findViewById(R.id.back_tv);
        backIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        chooseIv = contentView.findViewById(R.id.car_trafic_choose_iv);
        chooseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewModel.choose.getValue() == NOW_LINE){
                    setNowLineChart(viewModel.lineChartData.getValue());
                }else if(viewModel.choose.getValue() == FEATURE){
                    setFeatureChart(viewModel.lineChartData.getValue());
                }else{
                    showLog("选择出错");
                }
            }
        });
    }

    private void setCharline(CarLineChartBean carLineChartBean) {
        lineChart = contentView.findViewById(R.id.line_char);
        if(viewModel.choose.getValue() == NOW_LINE){
            setNowLineChart(carLineChartBean);

        }else if(viewModel.choose.getValue() == FEATURE){
            setFeatureChart(carLineChartBean);
        }else{
            showLog("选择出错");
        }

    }

    private void setFeatureChart(CarLineChartBean carLineChartBean) {
        for(int i = 0 ; i < 24 ;i ++){
            lineCharData.add(new Entry(i,carLineChartBean.getData().get(0).getFeature().get(i).getNumber().floatValue()));
        }
        LineChartsUtils chartsUtils = new LineChartsUtils(lineChart,lineCharData);
        chartsUtils.setFirstLine();
        chartsUtils.setLineBG();
        chartsUtils.setPicture();
        chartsUtils.setLineXY();
        chartsUtils.setChange();
        chartsUtils.setAnimate();
    }

    private void setNowLineChart(CarLineChartBean carLineChartBean) {
        for(int i = 0 ; i < 24 ;i ++){
            lineCharData.add(new Entry(i,carLineChartBean.getData().get(0).getWorkday().get(i).getNumber().floatValue()));
        }
        LineChartsUtils chartsUtils = new LineChartsUtils(lineChart,lineCharData);
        chartsUtils.setFirstLine();
        chartsUtils.setLineBG();
        chartsUtils.setPicture();
        chartsUtils.setLineXY();
        chartsUtils.setChange();
        chartsUtils.setAnimate();
    }

    private void setMarkerListener() {
        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                getLineChartData(marker);
                return true;
            }
        });
    }

    /**
     * 根据点击的坐标获取数据
     * @param marker
     */
    private void getLineChartData(Marker marker) {
        viewModel.lineChartData.setValue(mapUtils.testChartLine());
//        String location = String.valueOf(marker.getPosition().latitude)+"-"+String.valueOf(marker.getPosition().longitude);
//        new Thread(()->{
//            IPost iPost = BaseCreator.createCarInfo(IPost.class);
//            iPost.getCarLineChart(location).enqueue(new Callback<CarLineChartBean>() {
//                @Override
//                public void onResponse(Call<CarLineChartBean> call, Response<CarLineChartBean> response) {
//                    if(response.isSuccessful()){
//                        getActivity().runOnUiThread(()->{
//                            viewModel.lineChartData.setValue(response.body());
//                        });
//                    }else {
//                        getActivity().runOnUiThread(()->{
//                            showLog("获取数据失败");
//                        });
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<CarLineChartBean> call, Throwable t) {
//                    getActivity().runOnUiThread(()->{
//                        showLog("获取数据失败 请检查服务器的问题或请求格式的问题");
//                    });
//                }
//            });
//        }).start();
    }

    /**
     * 初始化地图的坐标
     */
    private void initMapMarkers() {
        markers = mapUtils.setCarTrafficMarkers(mapUtils.testTrafficMarkers(),aMap);
//        new Thread(()->{
//            IPost iPost = BaseCreator.createCarInfo(IPost.class);
//            iPost.getCarMarkers().enqueue(new Callback<CarTrafficMarkBean>() {
//                @Override
//                public void onResponse(Call<CarTrafficMarkBean> call, Response<CarTrafficMarkBean> response) {
//                    if(response.isSuccessful()){
//                        List<Marker> markerList = mapUtils.setCarTrafficMarkers(response.body(),aMap);
//                        getActivity().runOnUiThread(()->{
//                            markers = markerList;
//                        });
//                    }else {
//                        getActivity().runOnUiThread(()->{
//                            showLog("获取数据失败");
//                        });
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<CarTrafficMarkBean> call, Throwable t) {
//                    getActivity().runOnUiThread(()->{
//                        showLog("获取数据失败 请检查服务器的问题或请求格式的问题");
//                    });
//                }
//            });
//        }).start();
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
    private Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){

                case 0:{
                    PolylineOptions options = (PolylineOptions) msg.obj;
                    aMap.addPolyline(options);
                    viewModel.polylineOptions = options;
                }break;

                case 1:{
//                    List<HeatMapData.data> data = (List<HeatMapData.data>) msg.obj;
//                    viewModel.heatData.setValue(mapUtils.initHeatMapData(data));
                }break;
            }
        }
    };
}
