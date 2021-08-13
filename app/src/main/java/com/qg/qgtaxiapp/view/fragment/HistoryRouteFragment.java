package com.qg.qgtaxiapp.view.fragment;

import static com.qg.qgtaxiapp.utils.MapUtils.getAssetsStyle;
import static com.qg.qgtaxiapp.utils.MapUtils.getAssetsStyleExtra;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.CustomMapStyleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;
import com.qg.qgtaxiapp.databinding.FragmentHistoryRouteBinding;
import com.qg.qgtaxiapp.databinding.SearchDateLayoutBinding;
import com.qg.qgtaxiapp.utils.PolygonRunnable;
import com.qg.qgtaxiapp.view.activity.SkipSearchCarRouteActivity;
import com.qg.qgtaxiapp.viewmodel.HeatMapViewModel;
import com.qg.qgtaxiapp.viewmodel.HistoryRouteViewModel;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/11/10:48
 * @Description:
 */
public class HistoryRouteFragment extends Fragment {
    private FragmentHistoryRouteBinding binding;
    private SearchDateLayoutBinding binding1;
    private UiSettings uiSettings;
    private int year;
    private int monthOfYear;
    private int dayOfMonth;
    private PolygonRunnable polygonRunnable;
    private Calendar calendar;
    private AlertDialog dialog;
    private String searchMonth;
    private String searchDay;
    private DistrictSearch districtSearch;
    private TextureMapView mapView;
    private HistoryRouteViewModel historyRouteViewModel;
    private AMap aMap = null;
    private Bundle mMySavedInstanceState;
    private DistrictSearchQuery districtSearchQuery;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        districtSearch = new DistrictSearch(getContext());
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
                    }
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding=FragmentHistoryRouteBinding.inflate(inflater,container,false);
        mMySavedInstanceState = savedInstanceState;
        mapView=binding.routeMapView;
        historyRouteViewModel= new ViewModelProvider(getActivity()).get(HistoryRouteViewModel.class);
        initMap(mMySavedInstanceState);
        drawBoundary();
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        initListener();
    }

    private void initMap(Bundle bundle) {
        mapView.onCreate(bundle);
        //初始化地图控制器对象
        aMap = mapView.getMap();

        CustomMapStyleOptions customMapStyleOptions = new CustomMapStyleOptions();
        customMapStyleOptions.setEnable(true);
        customMapStyleOptions.setStyleData(getAssetsStyle(getContext()));
        customMapStyleOptions.setStyleExtraData(getAssetsStyleExtra(getContext()));
        aMap.setCustomMapStyle(customMapStyleOptions);

        uiSettings = aMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_LEFT);
        uiSettings.setLogoBottomMargin(-100);
        uiSettings.setScaleControlsEnabled(true);
        uiSettings.setZoomControlsEnabled(false);
        /*
            设置地图的范围
         */
        LatLng northeast = new LatLng(23.955343,114.054936);
        LatLng southwest = new LatLng(22.506530,112.968270);
        LatLngBounds bounds = new LatLngBounds.Builder().include(northeast).include(southwest).build();
        aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,10));
        aMap.setMapStatusLimits(bounds);
    }

    private void initListener() {
        binding.routeSearchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding1 = SearchDateLayoutBinding.inflate(getLayoutInflater());
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setView(binding1.getRoot());
                dialog = builder.create();
                initDate();
                binding1.dismissIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                binding1.nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(searchMonth==null||searchDay==null){
                            Toast.makeText(getContext(),"请选择2月或者3月",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int var= Integer.parseInt(searchMonth);
                        if(2<=var&&var<=3){
                            String searchStr = searchMonth + searchDay;
                            Intent intent = new Intent(getActivity(), SkipSearchCarRouteActivity.class);
                            intent.putExtra("searchStr", searchStr);
                            startActivity(intent);
                        }else {
                            Toast.makeText(getContext(),"请选择2月或者3月",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                binding1.datePicker.init(year, monthOfYear, dayOfMonth, new MyOnDateChangeListener());
                dialog.show();
            }
        });
    }

    private void initDate() {
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        monthOfYear = calendar.get(Calendar.MONTH);
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
    }

    class MyOnDateChangeListener implements DatePicker.OnDateChangedListener {
        @Override
        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            searchMonth = "0" + (monthOfYear + 1);
            if (dayOfMonth < 10) {
                searchDay = "0" + dayOfMonth;
            } else if (dayOfMonth > 10) {
                searchDay = String.valueOf(dayOfMonth);
            }
        }
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
                    historyRouteViewModel.polylineOptions = options;
                }break;

            }
        }
    };

    private void drawBoundary(){
        if (historyRouteViewModel.polylineOptions != null){
            aMap.addPolyline(historyRouteViewModel.polylineOptions);
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


}
