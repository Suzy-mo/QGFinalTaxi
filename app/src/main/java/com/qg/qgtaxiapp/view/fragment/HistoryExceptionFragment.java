package com.qg.qgtaxiapp.view.fragment;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.fence.GeoFenceListener;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.adapter.ExceptionItemAdapter;
import com.qg.qgtaxiapp.databinding.ExceptionCheckDialogLayoutBinding;
import com.qg.qgtaxiapp.databinding.FragmentHistoryExceptionBinding;
import com.qg.qgtaxiapp.entity.ExceptionItem;
import com.qg.qgtaxiapp.utils.NetUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
    private final ArrayList<ExceptionItem> list=new ArrayList<>();
    private ExceptionItemAdapter adapter;
    private final NetUtils netUtils=NetUtils.getInstance();
    private ExceptionCheckDialogLayoutBinding dialogLayoutBinding;
    private String select=null;
    private AlertDialog dialog;//选择
    private AlertDialog.Builder builder;

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
            builder = new AlertDialog.Builder(getContext());
            dialogLayoutBinding = ExceptionCheckDialogLayoutBinding.inflate(getLayoutInflater());
            builder.setView(dialogLayoutBinding.getRoot());
            dialog = builder.create();
            dialogLayoutBinding.allBtn.setChecked(true);
            initListener();
            initData("");
        }
    }

    private void initListener() {
        binding.exceptionChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initSelectDialogListener();
                dialog.show();
                changeDialogSize(dialog);
            }
        });
    }

    private void initSelectDialogListener() {
        dialogLayoutBinding.selectGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.all_btn:
                        select ="";
                        break;
                    case R.id.k_btn:
                        select=dialogLayoutBinding.kBtn.getText().toString();
                        break;
                    case R.id.address_btn:
                        select=dialogLayoutBinding.addressBtn.getText().toString();
                        break;
                    case R.id.mile_btn:
                        select=dialogLayoutBinding.mileBtn.getText().toString();
                        break;
                    case R.id.time_btn:
                        select=dialogLayoutBinding.timeBtn.getText().toString();
                        break;
                }
            }
        });
        dialogLayoutBinding.sureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(select!=null){
                    initData(select);
                    dialog.dismiss();
                    if(select.length()==0){
                        select="全部异常";
                    }
                    binding.exceptionMainTv.setText(select);
                }
            }
        });
        dialogLayoutBinding.dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void initData(String name) {
        netUtils.getExceptionData(1, 200,name,new Callback() {
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
                        list.clear();
                        for (int i = 0; i < exceptionData.length(); i++) {
                            JSONObject item = exceptionData.getJSONObject(i);
                            String carID = item.getString("plateNo");
                            String startLocation = item.getString("onLocation");
                            String endLocation=item.getString("offLocation");
                            String exceptionInfo=item.getString("error");
                            long date=item.getLong("errorDate");
                            String errorDate = changeFormat(date);
                            ExceptionItem exceptionItem=new ExceptionItem(carID,startLocation,endLocation,errorDate,exceptionInfo);
                            list.add(exceptionItem);
                        }
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


    private void changeDialogSize(AlertDialog dialog){
        WindowManager windowManager= getActivity().getWindowManager();
        DisplayMetrics dm = new DisplayMetrics();
        Window window=dialog.getWindow();
        WindowManager.LayoutParams p =window.getAttributes();
        windowManager.getDefaultDisplay().getMetrics(dm);
        p.height= (int) (dm.heightPixels*0.45);
        p.width= (int) (dm.widthPixels*0.83);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setAttributes(p);
    }
    private void showMsg(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private String changeFormat(long time){
        Date date = new Date();
        date.setTime(time);
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = dateFormat.format(date);
        Log.d("=========",format);
        return format;
    }



}
