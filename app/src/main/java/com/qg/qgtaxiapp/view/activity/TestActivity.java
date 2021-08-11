package com.qg.qgtaxiapp.view.activity;

import android.Manifest;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;
import com.qg.qgtaxiapp.databinding.ActivityTestBinding;

import java.util.ArrayList;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class TestActivity extends AppCompatActivity{

    //请求权限码
    private static final int REQUEST_PERMISSIONS = 9527;

    private ActivityTestBinding binding;
    private UiSettings uiSettings;
    private MapView mapView;
    //地图控制器
    private AMap aMap = null;

    private DistrictSearch districtSearch;
    private DistrictSearchQuery districtSearchQuery;
    private PolygonRunnable polygonRunnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);

        //初始化地图
        initMap(savedInstanceState);
        //检查安卓版本
        checkingAndroidVersion();

        districtSearch = new DistrictSearch(this);
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
        drawBoundary();
    }

    /*
        获取边界点的设置
     */
    private void drawBoundary(){
        String city = "广州";
        districtSearchQuery = new DistrictSearchQuery();
        //设置关键字
        districtSearchQuery.setKeywords(city);
        //设置是否返回边界值
        districtSearchQuery.setShowBoundary(true);

        districtSearch.setQuery(districtSearchQuery);
        districtSearch.searchDistrictAsyn();
    }

    /**
     * 检查Android版本
     */
    private void checkingAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Android6.0及以上先获取权限再定位
            requestPermission();

        }
    }

    /**
     * 动态请求权限
     */
    @AfterPermissionGranted(REQUEST_PERMISSIONS)
    private void requestPermission() {
        String[] permissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        if (EasyPermissions.hasPermissions(this, permissions)) {
            showMsg("权限已获取");

        } else {
            //false 无权限
            EasyPermissions.requestPermissions(this, "需要权限", REQUEST_PERMISSIONS, permissions);
        }
    }

    /**
     * 请求权限结果
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //设置权限请求结果
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * Toast提示
     * @param msg 提示内容
     */
    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }



    @Override
    protected void onDestroy() {
        //销毁定位客户端，同时销毁本地定位服务。
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 初始化地图
     * @param savedInstanceState
     */
    private void initMap(Bundle savedInstanceState) {

        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mapView.onCreate(savedInstanceState);
        //初始化地图控制器对象
        aMap = mapView.getMap();
        aMap.setMapType(AMap.MAP_TYPE_NIGHT);
//        aMap.setCustomMapStyle(
//                new com.amap.api.maps.model.CustomMapStyleOptions()
//                        .setEnable(true)
//                        .setStyleId("7f431e5f5cf8c616d10cfaa2907a229e")//官网控制台-自定义样式 获取
//        );
        uiSettings = aMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_LEFT);
        uiSettings.setLogoBottomMargin(-100);
        uiSettings.setScaleControlsEnabled(true);

        /*
            设置地图的范围
         */
        LatLng northeast = new LatLng(23.955343,114.054936);
        LatLng southwest = new LatLng(22.506530,112.968270);
        LatLngBounds bounds = new LatLngBounds.Builder().include(northeast).include(southwest).build();
        aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,10));
        aMap.setMapStatusLimits(bounds);

        /*LatLng latLng = new LatLng(23.129112,113.264385);//广州市
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,11));*/
    }

    /*
        消息处理
     */
    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){

                case 0:{

                }break;

                case 1:{
                    PolylineOptions options = (PolylineOptions) msg.obj;
                    aMap.addPolyline(options);
                }break;

            }
        }
    };

}

/**
 * 画地图边界
 * 获取广州全部边界点的经纬度数据
 */
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
