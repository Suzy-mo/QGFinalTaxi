package com.qg.qgtaxiapp.view.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.adapter.ExceptionItemAdapter;
import com.qg.qgtaxiapp.databinding.FragmentHistoryExceptionBinding;
import com.qg.qgtaxiapp.entity.ExceptionItem;
import com.qg.qgtaxiapp.utils.NetUtils;

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
 * @Date: 2021/08/11/10:56
 * @Description:
 */
public class HistoryExceptionFragment extends Fragment {
    private FragmentHistoryExceptionBinding binding;
    private RecyclerView recyclerView;
    private GridLayoutManager manager;
    private ArrayList<ExceptionItem> list=new ArrayList<>();
    private ExceptionItemAdapter adapter;
    private NetUtils netUtils=NetUtils.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding=FragmentHistoryExceptionBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(list.size()==0){
            manager=new GridLayoutManager(getContext(),1,RecyclerView.VERTICAL,false);
            initData();
        }
    }

    private void initData() {
        netUtils.getExceptionData(1, 100, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                try {
                    JSONObject jsonObject=new JSONObject(data);
                    String message = jsonObject.getString("message");
                    if(message.equals("查询数据成功")){
                        JSONArray exceptionData = jsonObject.getJSONArray("data");
                        for (int i = 0; i < exceptionData.length(); i++) {
                            JSONObject item = exceptionData.getJSONObject(i);
                            String carID = item.getString("plate_no");
                            String companyID = "公司ID:"+item.getString("company_id");
                            String location=item.getString("location");
                            String exceptionInfo=item.getString("error");
                            list.add(new ExceptionItem(carID,location,companyID,exceptionInfo));
                        }
                        ExceptionItemAdapter adapter=new ExceptionItemAdapter(R.layout.exception_item,list);
                        initView();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initView() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recyclerView=binding.exceptionRv;
                adapter = new ExceptionItemAdapter(R.layout.exception_item,list);
                manager=new GridLayoutManager(getContext(),1,RecyclerView.VERTICAL,false);
                recyclerView.setLayoutManager(manager);
                recyclerView.setAdapter(adapter);
            }
        });

    }
}
