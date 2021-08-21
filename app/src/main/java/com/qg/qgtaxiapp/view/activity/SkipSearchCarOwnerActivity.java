package com.qg.qgtaxiapp.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.adapter.HistoryInfoAdapter;
import com.qg.qgtaxiapp.databinding.ActivitySeachCarOwnerLayoutBinding;
import com.qg.qgtaxiapp.entity.CarOwnerItem;
import com.qg.qgtaxiapp.entity.HistoryInfo;
import com.qg.qgtaxiapp.model.SPModel;
import com.qg.qgtaxiapp.utils.Constants;
import com.qg.qgtaxiapp.utils.NetUtils;

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
 * @Date: 2021/08/11/21:23
 * @Description:
 */
public class SkipSearchCarOwnerActivity extends AppCompatActivity {
    private ActivitySeachCarOwnerLayoutBinding binding;
    private String carID;
    private GridLayoutManager manager;
    private HistoryInfoAdapter adapter;
    private final ArrayList<HistoryInfo> searchData = new ArrayList<>();//历史记录信息
    private ArrayList<String> history;//历史记录
    private final String Tag = "car_owner_history";
    private final SPModel instance = SPModel.getInstance();
    private final NetUtils netUtils = NetUtils.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = ActivitySeachCarOwnerLayoutBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        manager = new GridLayoutManager(this, 2, RecyclerView.VERTICAL, false);
        instance.init(this);
        history = instance.getDataBean(Tag);
        if (history != null && history.size() != 0) {
            for (int i = 0; i < history.size(); i++) {
                searchData.add(new HistoryInfo(history.get(i)));
            }
        }
        adapter = new HistoryInfoAdapter(R.layout.car_search_history_item, searchData);
        binding.ownerRv.setLayoutManager(manager);
        binding.ownerRv.setAdapter(adapter);
        initListener();

    }

    private void initListener() {
        binding.searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                carID = binding.searchText.getText().toString();
                if (carID.length() <= 0) {
                    Toast.makeText(SkipSearchCarOwnerActivity.this, "请检查输入!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d("==============1", carID);
                searchData.add(new HistoryInfo(carID));
                adapter.notifyDataSetChanged();
                requestData(carID);
            }
        });
        binding.ownerSelectBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.ownerDeleteBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchData.clear();
                history.clear();
                instance.cleanData(Tag);
                adapter.notifyDataSetChanged();
                Toast.makeText(SkipSearchCarOwnerActivity.this, "删除历史记录成功!", Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                if (searchData.get(position) != null){
                    String ID = searchData.get(position).getCarID();
                    requestData(ID);
                }
            }
        });
    }

    private void requestData(String carID) {
        netUtils.getCarOwnerData(carID, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    String message = jsonObject.getString("message");
                    if (message.equals("查询成功，返回该车牌号的车主信息")) {
                        JSONObject data = jsonObject.getJSONObject("data");
                        String carID = data.getString("plate_no");
                        String companyID = "公司ID:"+data.getString("company_id");
                        String mile = data.getString("load_mile");
                        String score = data.getString("evaluate");
                        CarOwnerItem carOwnerItem = new CarOwnerItem(carID, mile, score, companyID);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("carOwnerData", carOwnerItem);
                                Intent intent = new Intent();
                                intent.putExtra("data", bundle);
                                setResult(Constants.CAR_OWNER_CODE, intent);
                                history.add(carID);
                                instance.setDataList(Tag, history);
                                Toast.makeText(SkipSearchCarOwnerActivity.this, "查询成功!", Toast.LENGTH_SHORT).show();
                                SkipSearchCarOwnerActivity.this.finish();
                            }
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }
    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
