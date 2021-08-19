package com.qg.qgtaxiapp.view.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.amap.api.maps.model.LatLng;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.databinding.ActivityMainBinding;
import com.qg.qgtaxiapp.utils.Constants;
import com.qg.qgtaxiapp.view.fragment.HistoryMapFragment;
import com.qg.qgtaxiapp.view.fragment.HistoryRouteFragment;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/10/17:15
 * @Description:
 */
public class MainActivity extends AppCompatActivity {

    /*
        保存MyTouchListener接口的列表
    */
    private final ArrayList<MyTouchListener> myTouchListeners = new ArrayList<>();
    private ActivityMainBinding binding;
    //请求权限码
    private static final int REQUEST_PERMISSIONS = 9527;
    private int code = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        binding.navView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(binding.navView, navController);
        checkingAndroidVersion();
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
     *
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
     *
     * @param msg 提示内容
     */
    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public interface MyTouchListener {
        /**
         * onTOuchEvent的实现
         */
        boolean onTouchEvent(MotionEvent event);
    }

    /**
     * 提供给Fragment通过getActivity()方法来注册自己的触摸事件的方法
     */
    public void registerMyTouchListener(MyTouchListener listener) {
        myTouchListeners.add(listener);
    }

    /**
     * 提供给Fragment通过getActivity()方法来取消注册自己的触摸事件的方法
     */
    public void unRegisterMyTouchListener(MyTouchListener listener) {
        myTouchListeners.remove(listener);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        for (MyTouchListener listener : myTouchListeners) {
            listener.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Constants.REQUEST_ROUTE_CODE){
            if (resultCode == Constants.ROUTE_CODE) {
                Log.d("===========","是不是黑屏");
                Bundle key = data.getBundleExtra("key");
                ArrayList<LatLng> list = (ArrayList<LatLng>) key.getSerializable("data");
                HistoryMapFragment fragment = (HistoryMapFragment) getFragment(HistoryMapFragment.class);
                fragment.setRouteData(list, code);
                code++;
                if (code == 5) {
                    code = 0;
                }
            }
        }
    }

    /**
     * 获取碎片的方法
     *
     * @param clazz
     * @return
     */
    public Fragment getFragment(Class<?> clazz) {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null && fragments.size() > 0) {
            NavHostFragment navHostFragment = (NavHostFragment) fragments.get(0);
            List<Fragment> childfragments = navHostFragment.getChildFragmentManager().getFragments();
            if (childfragments != null && childfragments.size() > 0) {
                for (int j = 0; j < childfragments.size(); j++) {
                    Fragment fragment = childfragments.get(j);
                    if (fragment.getClass().isAssignableFrom(clazz)) {
                        Log.i("======", "getFragment1: " + fragment);
                        return fragment;
                    }
                }
            }
        }
        return null;
    }

    public void startToRequest(String selectDate){
        Intent intent = new Intent(this, SkipSearchCarRouteActivity.class);
        intent.putExtra("searchStr", selectDate);
        startActivityForResult(intent, Constants.REQUEST_ROUTE_CODE);
    }

}
