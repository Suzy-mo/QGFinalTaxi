package com.qg.qgtaxiapp.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.maps.model.LatLng;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.adapter.HistoryInfoAdapter;
import com.qg.qgtaxiapp.databinding.ActivitySearchRouteLayoutBinding;
import com.qg.qgtaxiapp.entity.HistoryInfo;
import com.qg.qgtaxiapp.utils.Constants;
import com.qg.qgtaxiapp.utils.NetUtils;
import com.qg.qgtaxiapp.view.fragment.HistoryMapFragment;
import com.qg.qgtaxiapp.view.fragment.HistoryRouteFragment;
import com.qg.qgtaxiapp.viewmodel.MainAndHistoryRouteViewModel;
import com.qg.qgtaxiapp.viewmodel.SkipAndHistoryRouteViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/12/21:42
 * @Description:
 */
public class SkipSearchCarRouteActivity extends AppCompatActivity {
    private ActivitySearchRouteLayoutBinding binding;
    private String searchStr;
    private NetUtils netUtils=NetUtils.getInstance();
    private String carID;
    private ArrayList<LatLng> latLngList=new ArrayList<>();
    private ArrayList<HistoryInfo> searchData=new ArrayList<>();
    private GridLayoutManager manager;
    private HistoryInfoAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySearchRouteLayoutBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        searchStr=getIntent().getStringExtra("searchStr");
        manager=new GridLayoutManager(SkipSearchCarRouteActivity.this,2, RecyclerView.VERTICAL,false);
        for (int i = 0; i <5; i++) {
            searchData.add(new HistoryInfo());
        }
        adapter=new HistoryInfoAdapter(R.layout.car_search_history_item,searchData);
        binding.routeHistoryRv.setLayoutManager(manager);
        binding.routeHistoryRv.setAdapter(adapter);
        Log.d("==============",searchStr);
        initListener();
    }

    private void initListener() {
        binding.searchRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                carID = binding.searchText.getText().toString();
                Log.d("==============1", carID);
                searchData.add(new HistoryInfo(carID));
                adapter.notifyDataSetChanged();
                netUtils.getRouteData(carID, searchStr, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData=response.body().string();
                        try {
                            JSONObject jsonObject=new JSONObject(responseData);
                            String message = jsonObject.getString("message");
                            if(message.equals("查询数据成功")){
                                JSONArray jsonArray=jsonObject.getJSONArray("data");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject data=jsonArray.getJSONObject(i);
                                    double latitude = data.getDouble("latitude");
                                    double longitude = data.getDouble("longitude");
                                    latLngList.add(new LatLng(latitude,longitude));
                                }
                                Log.d("============",latLngList.size()+"");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Bundle bundle=new Bundle();
                                        bundle.putSerializable("data",latLngList);
                                        Intent intent=new Intent();
                                        intent.putExtra("key",bundle);
                                        setResult(Constants.ROUTE_CODE,intent);
                                        Toast.makeText(SkipSearchCarRouteActivity.this,"查询成功",Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                            }else{
                                Toast.makeText(SkipSearchCarRouteActivity.this,"查无此车，请重试!",Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });
        binding.routeSelectBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.ownerDeleteBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchData.clear();
                adapter.notifyDataSetChanged();
                Toast.makeText(SkipSearchCarRouteActivity.this,"删除历史记录成功",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
