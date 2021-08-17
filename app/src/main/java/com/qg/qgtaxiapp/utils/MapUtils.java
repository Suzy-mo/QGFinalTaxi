package com.qg.qgtaxiapp.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CustomMapStyleOptions;
import com.amap.api.maps.model.Gradient;
import com.amap.api.maps.model.HeatmapTileProvider;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.district.DistrictItem;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.application.MyApplication;
import com.qg.qgtaxiapp.entity.HeatMapData;
import com.qg.qgtaxiapp.entity.FlowAllData;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author: Hx
 * @date: 2021年08月09日 16:43
 */
public class MapUtils {

    /*
     *设置热力图颜色
     */
    private static final int[] ALT_HEATMAP_GRADIENT_COLORS = {
            Color.rgb( 0, 0, 255),
            Color.rgb( 0, 211, 248),
            Color.rgb(0, 255, 0),
            Color.rgb(185, 71, 0),
            Color.rgb(255, 0, 0)
    };

    public static final float[] ALT_HEATMAP_GRADIENT_START_POINTS = { 0.1f,
            0.2f, 0.25f, 0.4f, 1f };

    public static final Gradient ALT_HEATMAP_GRADIENT = new Gradient(
            ALT_HEATMAP_GRADIENT_COLORS, ALT_HEATMAP_GRADIENT_START_POINTS);


    //生成HeatmapTileProvider
    public HeatmapTileProvider initBuildHeatmapTileProvider(List<LatLng> list) {
        HeatmapTileProvider.Builder builder = new HeatmapTileProvider.Builder();
        builder.data(list);
        builder.gradient(ALT_HEATMAP_GRADIENT);
        HeatmapTileProvider heatmapTileProvider = builder.build();
        return heatmapTileProvider;
    }



    //生成热力点坐标列表
    public List<LatLng> initHeatMapData(List<HeatMapData.data> data){
        List<LatLng> list = new ArrayList<>();

        for (HeatMapData.data data1 : data){
         double latitude = data1.getLatitude();
         double longitude = data1.getLongitude();
         LatLng latLng = new LatLng(latitude,longitude);
         list.add(latLng);
        }
        return list;
    }

    /*
        读取自定义地图样式
     */
    public static byte[] getAssetsStyle(Context context){
        byte[] buffer = null;
        InputStream is = null;
        try {
            is = context.getResources().getAssets().open("style.data");
            int len = is.available();
            buffer = new byte[len];
            is.read(buffer);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if (is != null){
                    is.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return buffer;
    }
    public static byte[] getAssetsStyleExtra(Context context){
        byte[] buffer = null;
        InputStream is = null;
        try {
            is = context.getResources().getAssets().open("style_extra.data");
            int len = is.available();
            buffer = new byte[len];
            is.read(buffer);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if (is != null){
                    is.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return buffer;
    }



    /**
     * 初始化地图
     * @param mapView
     */
    public AMap initMap(Context context,MapView mapView) {

        UiSettings uiSettings = null;
        AMap aMap = null;
        //初始化地图控制器对象
        aMap = mapView.getMap();

        CustomMapStyleOptions customMapStyleOptions = new CustomMapStyleOptions();
        customMapStyleOptions.setEnable(true);
        customMapStyleOptions.setStyleData(getAssetsStyle(context));
        customMapStyleOptions.setStyleExtraData(getAssetsStyleExtra(context));
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

        return aMap;
    }

    /**
     * @param  data
     * @return List<LatLng>
     * @description  转换流向图的坐标
     * @author Suzy.Mo
     * @time
     */

    public  List<LatLng> readLatLng(List<FlowAllData> data) {
        Log.d("Flow_TAG","readLatLng: flowAllData-->LatLng");
        List<LatLng> points = new ArrayList<LatLng>();
        for (int i = 0; i < data.size(); i ++) {
            points.add(new LatLng(data.get(i).getOffLatitude(),data.get(i).getOffLongitude()));
            points.add(new LatLng(data.get(i).getOnLatitude(),data.get(i).getOnLongitude()));
        }
        Log.d("Flow_TAG","readLatLng: flowAllData-->LatLng转换完成");
        return points;
    }

    /**
     * @param  list aMap
     * @return Polyline
     * @description  画全部流向图的曲线
     * @author Suzy.Mo
     * @time
     */

    public Polyline setFlowAllLine(List<LatLng> list,AMap aMap) {

        // 设置当前地图级别为4
        //aMap.moveCamera(CameraUpdateFactory.zoomTo(4));
        // 设置地图底图文字的z轴指数，默认为0
        //aMap.setMapTextZIndex(2);

        // 绘制流向图
        Polyline mPolyline = aMap.addPolyline((new PolylineOptions())//.setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.custtexture)))
                .addAll(list)
                .width(2)
                .color(Color.parseColor("#03DAC5")));
        return mPolyline;
    }

    public List<Polyline> setFlowAllLine2(List<LatLng> list,AMap aMap) {
        Log.d("Flow_TAG","setFlowAllLine2: LatLng-->List<Polyline>进入");
        List<Polyline> polylines = new ArrayList<>();
        // 设置当前地图级别为4
        //aMap.moveCamera(CameraUpdateFactory.zoomTo(4));
        // 设置地图底图文字的z轴指数，默认为0
        //aMap.setMapTextZIndex(2);

        // 绘制流向图
        for (int i = 0; i < list.size() ; i += 2){
            Polyline mPolyline = aMap.addPolyline((new PolylineOptions())//.setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.custtexture)))
                    .add(list.get(i),list.get(i+1))
                    .width(2)
                    .color(Color.parseColor("#03DAC5")));
            polylines.add(mPolyline);
        }
        Log.d("Flow_TAG","setFlowAllLine2: LatLng-->List<Polyline>转换完成");
        return polylines;
    }


    /**
     * @param list mAMap
     * @return void
     * @description  画主流向的函数
     * @author Suzy.Mo
     * @time
     */

    public Polyline setFlowMainLine(List<LatLng> list, AMap mAMap) {
        List<Integer> colorList = new ArrayList<Integer>();
        List<BitmapDescriptor> bitmapDescriptors = new ArrayList<BitmapDescriptor>();

        int[] colors = new int[]{Color.argb(255, 0, 255, 0),Color.argb(255, 255, 255, 0),Color.argb(255, 255, 0, 0)};

        //用一个数组来存放纹理
        List<BitmapDescriptor> textureList = new ArrayList<BitmapDescriptor>();
        textureList.add(BitmapDescriptorFactory.fromResource(R.mipmap.custtexture));

        List<Integer> texIndexList = new ArrayList<Integer>();
        texIndexList.add(0);//对应上面的第0个纹理
        texIndexList.add(1);
        texIndexList.add(2);

        Random random = new Random();
        for (int i = 0; i < list.size(); i++) {
            colorList.add(colors[random.nextInt(3)]);
            bitmapDescriptors.add(textureList.get(0));

        }

        Polyline mPolyline =  mAMap.addPolyline(new PolylineOptions().setCustomTexture(BitmapDescriptorFactory.fromResource(R.mipmap.custtexture)) //setCustomTextureList(bitmapDescriptors)
//				.setCustomTextureIndex(texIndexList)
                .addAll(list)
                .useGradient(true)
                .width(18));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(list.get(0));
        builder.include(list.get(list.size() - 2));

        mAMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));

        return mPolyline;
    }

//    public List<FlowAllData.DataBean> setAllData(){
//        List<FlowAllData.DataBean> mData = new ArrayList<>();
//        FlowAllData.DataBean dataBean = new FlowAllData.DataBean();
//        dataBean.setLatitude(23.131467);dataBean.setLongitude(113.284837);mData.add(dataBean);
//        FlowAllData.DataBean dataBean1 = new FlowAllData.DataBean();
//        dataBean1.setLatitude(23.138388);dataBean1.setLongitude(113.286461);mData.add(dataBean1);
//        FlowAllData.DataBean dataBean2 = new FlowAllData.DataBean();
//        dataBean2.setLatitude(23.140177);dataBean2.setLongitude(113.280282);mData.add(dataBean2);
//        FlowAllData.DataBean dataBean3 = new FlowAllData.DataBean();
//        dataBean3.setLatitude(23.142006);dataBean3.setLongitude(113.282005);mData.add(dataBean3);
//        FlowAllData.DataBean dataBean4 = new FlowAllData.DataBean();
//        dataBean4.setLatitude(23.141077);dataBean4.setLongitude(113.285651);mData.add(dataBean4);
//        FlowAllData.DataBean dataBean5 = new FlowAllData.DataBean();
//        dataBean5.setLatitude(23.129552);dataBean5.setLongitude(113.284006);mData.add(dataBean5);
//        FlowAllData.DataBean dataBean6 = new FlowAllData.DataBean();
//        dataBean6.setLatitude(23.136475);dataBean6.setLongitude(113.289954);mData.add(dataBean6);
//        FlowAllData.DataBean dataBean7 = new FlowAllData.DataBean();
//        dataBean7.setLatitude(23.130344);dataBean7.setLongitude(113.296271);mData.add(dataBean7);
//        FlowAllData.DataBean dataBean8 = new FlowAllData.DataBean();
//        dataBean8.setLatitude( 23.132371);dataBean8.setLongitude(113.292864);mData.add(dataBean8);
//        FlowAllData.DataBean dataBean9 = new FlowAllData.DataBean();
//        dataBean9.setLatitude(23.116095);dataBean9.setLongitude(113.300873);mData.add(dataBean9);
//        FlowAllData.DataBean dataBean10 = new FlowAllData.DataBean();
//        dataBean10.setLatitude(23.11092);dataBean10.setLongitude(113.296599);mData.add(dataBean10);
//        FlowAllData.DataBean dataBean11 = new FlowAllData.DataBean();
//        dataBean11.setLatitude( 23.13906);dataBean11.setLongitude(113.296599);mData.add(dataBean11);
//        FlowAllData.DataBean dataBean12 = new FlowAllData.DataBean();
//        dataBean12.setLatitude(23.137483);dataBean12.setLongitude(113.298799);mData.add(dataBean12);
//        FlowAllData.DataBean dataBean13 = new FlowAllData.DataBean();
//        dataBean13.setLatitude(23.137361);dataBean13.setLongitude(113.295664);mData.add(dataBean13);
//        FlowAllData.DataBean dataBean14 = new FlowAllData.DataBean();
//        dataBean14.setLatitude(23.131446);dataBean14.setLongitude(113.282472);mData.add(dataBean14);
//        FlowAllData.DataBean dataBean15 = new FlowAllData.DataBean();
//        dataBean15.setLatitude( 23.130885);dataBean15.setLongitude(113.29107);mData.add(dataBean15);
//        FlowAllData.DataBean dataBean16 = new FlowAllData.DataBean();
//        dataBean16.setLatitude(23.136245);dataBean16.setLongitude(113.299721);mData.add(dataBean16);
//        FlowAllData.DataBean dataBean17 = new FlowAllData.DataBean();
//        dataBean17.setLatitude(23.132397);dataBean17.setLongitude(113.282472);mData.add(dataBean17);
//
//        FlowAllData.DataBean dataBean18 = new FlowAllData.DataBean();
//        dataBean18.setLatitude( 23.129604);dataBean18.setLongitude(113.292724);mData.add(dataBean1);
//        FlowAllData.DataBean dataBean21 = new FlowAllData.DataBean();
//        dataBean21.setLatitude(23.128679);dataBean21.setLongitude(113.292576);mData.add(dataBean2);
//        FlowAllData.DataBean dataBean31 = new FlowAllData.DataBean();
//        dataBean31.setLatitude(23.125535);dataBean31.setLongitude(113.286996);mData.add(dataBean31);
//        FlowAllData.DataBean dataBean41 = new FlowAllData.DataBean();
//        dataBean41.setLatitude(23.120084);dataBean41.setLongitude(113.292576);mData.add(dataBean41);
//        FlowAllData.DataBean dataBean61 = new FlowAllData.DataBean();
//
//        dataBean61.setLatitude(23.129378);dataBean61.setLongitude(113.285085);mData.add(dataBean61);
//        FlowAllData.DataBean dataBean71 = new FlowAllData.DataBean();
//        dataBean71.setLatitude(23.131466);dataBean71.setLongitude(113.291927);mData.add(dataBean71);
//        FlowAllData.DataBean dataBean81 = new FlowAllData.DataBean();
//
//        dataBean8.setLatitude(23.137665);dataBean81.setLongitude(113.286911);mData.add(dataBean81);
//        FlowAllData.DataBean dataBean91 = new FlowAllData.DataBean();
//        dataBean91.setLatitude(23.137967);dataBean91.setLongitude(113.286911);mData.add(dataBean91);
//        FlowAllData.DataBean dataBean101 = new FlowAllData.DataBean();
//
//        dataBean101.setLatitude(23.140546);dataBean101.setLongitude(113.288655);mData.add(dataBean101);
//        FlowAllData.DataBean dataBean111 = new FlowAllData.DataBean();
//        dataBean111.setLatitude( 23.134311);dataBean111.setLongitude(113.291571);mData.add(dataBean11);
//        FlowAllData.DataBean dataBean121 = new FlowAllData.DataBean();
//
//        dataBean121.setLatitude(23.125185);dataBean121.setLongitude(113.278443);mData.add(dataBean121);
//        FlowAllData.DataBean dataBean131 = new FlowAllData.DataBean();
//        dataBean131.setLatitude(23.129195);dataBean131.setLongitude(113.276893);mData.add(dataBean131);
//
//        return mData;
//    }
}

