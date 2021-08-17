package com.qg.qgtaxiapp.view.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarEntry;
import com.qg.qgtaxiapp.databinding.FragmentCarIncomeBinding;
import com.qg.qgtaxiapp.entity.BaseCreator;
import com.qg.qgtaxiapp.entity.CarIncomeBean;
import com.qg.qgtaxiapp.entity.IPost;
import com.qg.qgtaxiapp.utils.BarChartUtils;
import com.qg.qgtaxiapp.utils.TimePickerUtils;
import com.qg.qgtaxiapp.viewmodel.CarInfoIncomeViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @Name：QGTaxiApp
 * @Description：
 * @Author：Suzy.Mo
 * @Date：2021/8/16 23:15
 */
public class CarIncomeFragment extends Fragment {

    private FragmentCarIncomeBinding binding;
    private CarInfoIncomeViewModel viewModel;

    private TimePickerView datePickerView;
    private TimePickerUtils timePickerUtils;
    private BarChartUtils nowBarChartUtils,featureBarCharUtils;

    private TextView tv_setTime,tv_date,tv_choose,tv_timeTable;

    private BarChart nowBar,featureBar;
    List<BarEntry> featureList =new ArrayList<>(),nowList =new ArrayList<>();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(getActivity()).get(CarInfoIncomeViewModel.class);
        timePickerUtils = new TimePickerUtils();
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCarIncomeBinding.inflate(inflater,container,false);

        bindingView();
        initFirstView();
        initTimeChoose();
        initListener();

        setFeatureChart();
        setTimeChooseObserve();
        setNowChartObserve();

        return binding.getRoot();
    }

    private void setNowChartObserve() {
        viewModel.carIncomeData.observe(getActivity(), new Observer<CarIncomeBean>() {
            @Override
            public void onChanged(CarIncomeBean carIncomeBean) {
                CarIncomeBean.DataBean mData = carIncomeBean.getData().get(0);
                showLog("观察到数据变化 准备画图");
                nowList.add(new BarEntry(1,mData.getEarly_morning().floatValue()));
                nowList.add(new BarEntry(2,mData.getMorning().floatValue()));
                nowList.add(new BarEntry(3,mData.getAfternoon().floatValue()));
                nowList.add(new BarEntry(4,mData.getNight().floatValue()));
                showLog(mData.getEarly_morning().toString()+"\n"+mData.getMorning().floatValue()+"\n"+mData.getAfternoon().floatValue()+"\n"+mData.getNight().floatValue());
                nowBarChartUtils = new BarChartUtils(nowBar,nowList);
                nowBarChartUtils.settingBar();
            }
        });
    }

    private void setFeatureChart() {
        binding.carIncomeFeatureBarChart.invalidate();
        new Thread(()->{
            IPost iPost = BaseCreator.createCarInfo(IPost.class);
            iPost.getCarInfo("-----").enqueue(new Callback<CarIncomeBean>() {
                @Override
                public void onResponse(Call<CarIncomeBean> call, Response<CarIncomeBean> response) {
                    CarIncomeBean.DataBean mData = response.body().getData().get(0);
                    Log.d("-----",response.toString());
                    showLog(mData.getEarly_morning().toString()+"\n"+mData.getMorning().floatValue()+"\n"+mData.getAfternoon().floatValue()+"\n"+mData.getNight().floatValue());
                    featureList.add(new BarEntry(1,mData.getEarly_morning().floatValue()));
                    featureList.add(new BarEntry(2,mData.getMorning().floatValue()));
                    featureList.add(new BarEntry(3,mData.getAfternoon().floatValue()));
                    featureList.add(new BarEntry(4,mData.getNight().floatValue()));
                    getActivity().runOnUiThread(()->{
                        featureBarCharUtils = new BarChartUtils(featureBar,featureList);
                        featureBarCharUtils.settingBar();
                    });

                }

                @Override
                public void onFailure(Call<CarIncomeBean> call, Throwable t) {
                        showLog("onFailure: 数据获取失败");
                }
            });
        }).start();

    }

    private void initListener() {
        tv_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initLastView();
                datePickerView.show();
            }
        });

        tv_setTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerView.show();
            }
        });
    }

    private void bindingView() {
        tv_setTime = binding.tvSetTime;
        tv_date = binding.tvDate;
        tv_choose = binding.tvHeatChooseTime;
        tv_timeTable = binding.tvTimeLabel;

        nowBar = binding.carIncomeNowBarChart;
        featureBar = binding.carIncomeFeatureBarChart;
    }

    /**
     * 设置初始的界面
     */
    private void initFirstView() {
        tv_choose.setVisibility(View.VISIBLE);
        tv_setTime.setVisibility(View.INVISIBLE);
        tv_date.setVisibility(View.INVISIBLE);
        tv_timeTable.setVisibility(View.INVISIBLE);
    }

    /**
     * 点击修改时间后
     */
    private void initLastView() {
        tv_choose.setVisibility(View.INVISIBLE);
        tv_setTime.setVisibility(View.VISIBLE);
        tv_date.setVisibility(View.VISIBLE);
        tv_timeTable.setVisibility(View.VISIBLE);
    }

    /**
     * 时间选择器的设置
     */
    private void initTimeChoose() {
        datePickerView = timePickerUtils.initFlowDatePicker(getContext(), getActivity(), new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                showLog(timePickerUtils.getDate(date).toString());
                viewModel.date.setValue(timePickerUtils.getDate(date));
            }
        });
    }

    /**
     * Log.d打印日志
     */
    private void showLog(String log){
        Log.d("TAG_CarIncomeFragment",log);
    }

    private void setTimeChooseObserve() {
        viewModel.date.observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tv_date.setText(s);
                new Thread(()->{
                    IPost iPost = BaseCreator.createCarInfo(IPost.class);
                    iPost.getCarInfo(s).enqueue(new Callback<CarIncomeBean>() {
                        @Override
                        public void onResponse(Call<CarIncomeBean> call, Response<CarIncomeBean> response) {
                            getActivity().runOnUiThread(()->{
                                if(response.isSuccessful()){
                                    showLog("获取数据成功 将存到viewModel里面");
                                    viewModel.carIncomeData.setValue(response.body());
                                    showLog(response.body().getData().get(0).toString());
                                }else {
                                    showLog(response.toString());
                                }
                            });
                        }

                        @Override
                        public void onFailure(Call<CarIncomeBean> call, Throwable t) {
                            showLog("onFailure: 数据获取失败");
                        }
                    });
                }).start();
            }
        });
    }

}
