package com.qg.qgtaxiapp.view.fragment;

import static com.qg.qgtaxiapp.utils.MapUtils.getAssetsStyle;
import static com.qg.qgtaxiapp.utils.MapUtils.getAssetsStyleExtra;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.AMapGestureListener;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CustomMapStyleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.CustomListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.databinding.FragmentHistoryRouteBinding;
import com.qg.qgtaxiapp.databinding.SearchDateLayoutBinding;
import com.qg.qgtaxiapp.databinding.SelectBinLayoutBinding;
import com.qg.qgtaxiapp.utils.Constants;
import com.qg.qgtaxiapp.utils.PolygonRunnable;
import com.qg.qgtaxiapp.view.activity.MainActivity;
import com.qg.qgtaxiapp.view.activity.SkipSearchCarRouteActivity;
import com.qg.qgtaxiapp.viewmodel.HistoryMapViewModel;
import com.qg.qgtaxiapp.viewmodel.MainAndHistoryRouteViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
    private MainAndHistoryRouteViewModel mainAndHistoryRouteViewModel;
    private AMap aMap = null;
    private Bundle mMySavedInstanceState;
    private DistrictSearchQuery districtSearchQuery;
    private HistoryMapViewModel viewModel;
    private TimePickerView timePickerView;
    private String selectDate;
    private SelectBinLayoutBinding binLayoutBinding;
    private AMapGestureListener aMapGestureListener;
    private int code = 0;
    private final ArrayList<BitmapDescriptor> mTexTureList = new ArrayList<BitmapDescriptor>();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(getActivity()).get(HistoryMapViewModel.class);
        initDistrictSearch();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryRouteBinding.inflate(inflater, container, false);
        mMySavedInstanceState = savedInstanceState;
        mapView = binding.routeMapView;
        mainAndHistoryRouteViewModel = new ViewModelProvider(getActivity()).get(MainAndHistoryRouteViewModel.class);
        initMap(mMySavedInstanceState);
        initLayoutListener();
        aMap.setAMapGestureListener(aMapGestureListener);
        initBitmapData();
        drawBoundary();
        return binding.getRoot();
    }

    private void initBitmapData() {
        mTexTureList.add(BitmapDescriptorFactory.fromResource(R.drawable.blue_route));
        mTexTureList.add(BitmapDescriptorFactory.fromResource(R.drawable.green_route));
        mTexTureList.add(BitmapDescriptorFactory.fromResource(R.drawable.red_route));
        mTexTureList.add(BitmapDescriptorFactory.fromResource(R.drawable.yellow_route));
        mTexTureList.add(BitmapDescriptorFactory.fromResource(R.drawable.orange_route));
    }

    /**
     * 对用户滑动的监听
     */
    private void initLayoutListener() {
        aMapGestureListener = new AMapGestureListener() {
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
                viewModel.viewPager2.setUserInputEnabled(false);
            }

            @Override
            public void onLongPress(float v, float v1) {

            }

            @Override
            public void onDown(float v, float v1) {

            }

            @Override
            public void onUp(float v, float v1) {
                viewModel.viewPager2.setUserInputEnabled(true);
            }

            @Override
            public void onMapStable() {

            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        initListener();
    }

    private void initMap(Bundle bundle) {
        mapView.onCreate(bundle);
        aMap = mapView.getMap();
        //设置样式
        CustomMapStyleOptions customMapStyleOptions = new CustomMapStyleOptions();
        customMapStyleOptions.setEnable(true);
        customMapStyleOptions.setStyleData(getAssetsStyle(getContext()));
        customMapStyleOptions.setStyleExtraData(getAssetsStyleExtra(getContext()));
        aMap.setCustomMapStyle(customMapStyleOptions);
        //设置地图
        uiSettings = aMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_LEFT);
        uiSettings.setLogoBottomMargin(-100);
        uiSettings.setScaleControlsEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
        //设置边界
        LatLng northeast = new LatLng(23.955343, 114.054936);
        LatLng southwest = new LatLng(22.506530, 112.968270);
        LatLngBounds bounds = new LatLngBounds.Builder().include(northeast).include(southwest).build();
        aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
        aMap.setMapStatusLimits(bounds);
    }

    /**
     * 事件监听
     */
    private void initListener() {
        binding.routeLayoutSearchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initMyDialog();
            }
        });
        binding.deleteRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binLayoutBinding = SelectBinLayoutBinding.inflate(getLayoutInflater());
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setView(binLayoutBinding.getRoot());
                AlertDialog dialog = builder.create();
                binLayoutBinding.cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                binLayoutBinding.sureBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        aMap.clear();
                        drawBoundary();
                        dialog.dismiss();
                    }
                });
                dialog.show();
                WindowManager windowManager = getActivity().getWindowManager();
                DisplayMetrics dm = new DisplayMetrics();
                Window window = dialog.getWindow();
                WindowManager.LayoutParams p = window.getAttributes();
                windowManager.getDefaultDisplay().getMetrics(dm);
                p.height = (int) (dm.heightPixels * 0.22);
                p.width = (int) (dm.widthPixels * 0.7);
                window.setAttributes(p);
            }
        });
    }

    private void initMyDialog() {
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        startDate.set(2017, 1, 1);
        endDate.set(2017, 2, 31);
        timePickerView = new TimePickerBuilder(getContext(), new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                selectDate = getDate(date);
                MainActivity activity = (MainActivity) getActivity();
                activity.startToRequest(selectDate);
//                Intent intent = new Intent(activity, SkipSearchCarRouteActivity.class);
//                intent.putExtra("searchStr", selectDate);
//                startActivityForResult(intent, Constants.REQUEST_ROUTE_CODE);
            }
        }).setLayoutRes(R.layout.timepicker_date_single, new CustomListener() {
            @Override
            public void customLayout(View v) {
                TextView next = v.findViewById(R.id.timepicker_date_next);
                ImageView cancel = v.findViewById(R.id.timepicker_date_cancel);
                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        timePickerView.returnData();
                        timePickerView.dismiss();
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        timePickerView.dismiss();
                    }
                });
            }
        }).isDialog(true).setOutSideCancelable(false)
                .setRangDate(startDate, endDate)
                .setOutSideColor(getActivity().getResources().getColor(R.color.timepicker_outside))//外部背景颜色
                .setBgColor(getActivity().getResources().getColor(R.color.timepicker_background))//背景颜色
                .setTextColorCenter(getActivity().getResources().getColor(R.color.timepicker_selectText))//选中字体颜色
                .setTextColorOut(getActivity().getResources().getColor(R.color.timepicker_unselectText))//未选中字体颜色
                .isCenterLabel(true)//只显示中央标签
                .setItemVisibleCount(5)//可见标签数
                .setContentTextSize(16)
                .setDividerColor(Color.argb(0, 0, 0, 0))
                .setType(new boolean[]{false, true, true, false, false, false})//是否显示年月日，时分秒
                .isAlphaGradient(true)//滚轮透明
                .build();
        Dialog timePickerDialog;
        timePickerDialog = timePickerView.getDialog();
        timePickerDialog.show();
        Window window = timePickerDialog.getWindow();
        WindowManager manager = getActivity().getWindowManager();
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = (int) (dm.widthPixels * 0.95);
        window.setAttributes(params);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
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
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0: {
                    PolylineOptions options = (PolylineOptions) msg.obj;
                    aMap.addPolyline(options);
                    mainAndHistoryRouteViewModel.polylineOptions = options;
                }
                break;

            }
        }
    };

    /**
     * 绘制边界的方法
     */
    private void drawBoundary() {
        if (mainAndHistoryRouteViewModel.polylineOptions != null) {
            aMap.addPolyline(mainAndHistoryRouteViewModel.polylineOptions);
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

    private void initDistrictSearch() {
        districtSearch = new DistrictSearch(getContext());
        districtSearch.setOnDistrictSearchListener(new DistrictSearch.OnDistrictSearchListener() {
            @Override
            public void onDistrictSearched(DistrictResult districtResult) {

                if (districtResult != null && districtResult.getDistrict() != null) {
                    if (districtResult.getAMapException().getErrorCode() == AMapException.CODE_AMAP_SUCCESS) {

                        ArrayList<DistrictItem> districtItems = districtResult.getDistrict();
                        DistrictItem item = null;
                        if (districtItems != null && districtItems.size() > 0) {
                            //广州市 adcode：440100
                            for (DistrictItem districtItem : districtItems) {
                                if (districtItem.getAdcode().equals("440100")) {
                                    item = districtItem;
                                    break;
                                }
                            }
                            if (item == null) {
                                return;
                            }
                            polygonRunnable = new PolygonRunnable(item, handler);
                            new Thread(polygonRunnable).start();
                        }
                    }
                }
            }
        });
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == Constants.REQUEST_ROUTE_CODE) {
//            if (resultCode == Constants.ROUTE_CODE) {
//                Bundle key = data.getBundleExtra("key");
//                ArrayList<LatLng> list = (ArrayList<LatLng>) key.getSerializable("data");
//                if (list != null) {
//                    Log.d("===========daot", list.size() + "");
//                    PolylineOptions options=new PolylineOptions();
//                    LatLng start=list.get(0);
//                    LatLng end=list.get(list.size()-1);
//                    aMap.addMarker(new MarkerOptions().position(start).icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
//                    aMap.addMarker(new MarkerOptions().position(end).icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
//                    aMap.addPolyline(options.setCustomTexture(mTexTureList.get(code)).addAll(list).width(15));
//                    code++;
//                    if(code==5){
//                        code=0;
//                    }
//                }
//            }
//        }
//    }

    private String getDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String myDate = format.format(date);
        String[] myDateArray = myDate.split("-");
        String data = myDateArray[1] + myDateArray[2];
        return data;
    }

    public void drawLine(ArrayList<LatLng> list, int code) {
        if(list==null){
            return;
        }
        PolylineOptions options = new PolylineOptions();
        LatLng start = list.get(0);
        LatLng end = list.get(list.size() - 1);
        aMap.addMarker(new MarkerOptions().position(start).icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
        aMap.addMarker(new MarkerOptions().position(end).icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
        aMap.addPolyline(options.setCustomTexture(mTexTureList.get(code)).addAll(list).width(15));
    }
}
