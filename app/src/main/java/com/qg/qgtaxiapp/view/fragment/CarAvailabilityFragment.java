package com.qg.qgtaxiapp.view.fragment;

import android.app.Dialog;
import android.graphics.BlendMode;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.CustomListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.adapter.SpinnerAdapter;
import com.qg.qgtaxiapp.databinding.FragmentCarAvailabilityBinding;
import com.qg.qgtaxiapp.utils.NetUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @Name：QGTaxiApp
 * @Description：
 * @Author：Suzy.Mo
 * @Date：2021/8/16 23:17
 */
public class CarAvailabilityFragment extends Fragment {
    private String[] mItem = {"清晨", "上午", "下午", "晚上"};
    private FragmentCarAvailabilityBinding binding;
    private SpinnerAdapter mCurrentAdapter;
    private SpinnerAdapter mFutureAdapter;
    private TimePickerView timePickerView;

    private PieData currentPieData;
    private PieDataSet currentPieDataSet;
    private ArrayList<PieEntry> currentValues = new ArrayList<>();
    private ArrayList<Double> currentDataList = new ArrayList<>();

    private PieData futurePieData;
    private PieDataSet futurePieDataSet;
    private ArrayList<PieEntry> futureValues = new ArrayList<>();
    private ArrayList<Double> futureDataList = new ArrayList<>();

    private Dialog timePickerDialog;
    private NetUtils netUtils = NetUtils.getInstance();
    private ArrayList<Integer> colors = new ArrayList<>();

    private SpannableString currentText;
    private SpannableString futureText;
    private StringBuilder currentDate;

    public CarAvailabilityFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCarAvailabilityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        initTimeDialog();
        initData();
        initListener();
        mCurrentAdapter = new SpinnerAdapter(getContext(), mItem);
        mCurrentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.currentSpinner.setAdapter(mCurrentAdapter);
        binding.currentSpinner.setDropDownVerticalOffset(70);
        binding.currentSpinner.setDropDownHorizontalOffset(80);
        binding.currentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentAdapter.setType(position);
                if (currentDataList.size() != 0) {
                    setCurrentData(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mFutureAdapter = new SpinnerAdapter(getContext(), mItem);
        mFutureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.futureSpinner.setAdapter(mFutureAdapter);
        binding.futureSpinner.setDropDownVerticalOffset(70);
        binding.futureSpinner.setDropDownHorizontalOffset(80);
        binding.futureSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mFutureAdapter.setType(position);
                if (futureDataList.size() != 0) {
                    setFutureData(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        initView();
    }

    private void initView() {

    }

    private void initTimeDialog() {
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        startDate.set(2017, 1, 1);
        endDate.set(2017, 1, 28);
        timePickerView = new TimePickerBuilder(getContext(), new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                String date1 = getDate(date);
                StringBuilder builder = new StringBuilder(date1);
                currentDate = new StringBuilder(date1);
                builder.insert(2, "-");
                initCurrentData(builder.toString());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.currentPieChart.invalidate();
                        if (currentDate != null) {
                            currentDate.insert(2, ".");
                            binding.dateTv.setText("2017." + currentDate.toString());
                            binding.currentSpinner.setSelection(0,true);
                        }
                    }
                });
            }
        }).setLayoutRes(R.layout.route_dialog_view, new CustomListener() {
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
                .setDividerColor(Color.argb(0, 0, 0, 0))
                .setType(new boolean[]{false, true, true, false, false, false})//是否显示年月日，时分秒
                .isAlphaGradient(true)//滚轮透明
                .build();
    }

    private void initListener() {
        binding.changeSelectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog = timePickerView.getDialog();
                timePickerDialog.show();
                createDialogSize();
            }
        });
    }

    private void createDialogSize() {
        Window window = timePickerDialog.getWindow();
        WindowManager manager = getActivity().getWindowManager();
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = (int) (dm.widthPixels * 0.95);
        window.setAttributes(params);
    }

    private void initData() {
        colors.add(Color.parseColor("#FF03DAC5"));
        colors.add(Color.parseColor("#6103DAC5"));
        initCurrentData("02-01");
        ArrayList<Double> doubles = new ArrayList<>();
        doubles.add(0.6035877);
        doubles.add(0.6985845);
        doubles.add(0.70420796);
        doubles.add(0.7321395);
        changeFutureData(doubles);
    }

    private void initCurrentData(String date) {
        netUtils.getCurrentPieData(date, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                try {
                    getData(responseData, 1);
//                    initFutureData();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initFutureData() {
        netUtils.getCurrentPieData(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                if (responseData.length() == 0) {
                    ArrayList<Double> doubles = new ArrayList<>();
                    doubles.add(0.6035877);
                    doubles.add(0.6985845);
                    doubles.add(0.70420796);
                    doubles.add(0.7321395);
                    changeFutureData(doubles);
                } else if (responseData.length() != 0) {
                    try {
                        getData(responseData, 2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 获取数据
     *
     * @param responseData
     * @param code
     * @throws JSONException
     */
    private void getData(String responseData, int code) throws JSONException {
        JSONObject jsonObject = new JSONObject(responseData);
        if (jsonObject.getString("message").equals("查询数据成功")) {
            JSONArray data = jsonObject.getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                JSONObject currentData = data.getJSONObject(i);
                double early_morning = currentData.getDouble("early_morning");
                double morning = currentData.getDouble("morning");
                double afternoon = currentData.getDouble("afternoon");
                double night = currentData.getDouble("night");
                ArrayList<Double> doubles = new ArrayList<>();
                doubles.add(early_morning);
                doubles.add(morning);
                doubles.add(afternoon);
                doubles.add(night);
                if (code == 1) {
                    changeCurrentData(doubles);
                } else if (code == 2) {
                    changeFutureData(doubles);
                }
            }
        }
    }

    /**
     * 获取时间
     *
     * @param date
     * @return
     */
    private String getDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String myDate = format.format(date);
        String myDateArray[] = myDate.split("-");
        String data = myDateArray[1] + myDateArray[2];
        return data;
    }

    /**
     * 当前数据
     *
     * @param doubles
     */
    private void changeCurrentData(ArrayList<Double> doubles) {
        if (currentDataList.size() != 0) {
            currentDataList.clear();
        }
        for (int i = 0; i < doubles.size(); i++) {
            double a = doubles.get(i);
            BigDecimal b = new BigDecimal(a);
            double f1 = b.setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue();
            Log.d("=============", f1 + "");
            currentDataList.add(f1);
        }
        Log.d("===============", currentDataList.size() + "");
        setCurrentData(0);
    }

    /**
     * 预测数据
     *
     * @param doubles
     */
    private void changeFutureData(ArrayList<Double> doubles) {
        if (futureDataList.size() != 0) {
            futureDataList.clear();
        }
        for (int i = 0; i < doubles.size(); i++) {
            double a = doubles.get(i);
            BigDecimal b = new BigDecimal(a);
            double f1 = b.setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue();
            futureDataList.add(f1);
        }
        setFutureData(0);
    }

    private void setCurrentData(int position) {
        if (currentValues.size() != 0) {
            currentValues.clear();
        }
        Double a = currentDataList.get(position);
        //数据
        float data1 = (float) (a * 100);
        float data2 = (float) ((1.0 - a) * 100);
        //初始化数据
        currentValues.add(new PieEntry(data1,"已利用"));
        currentValues.add(new PieEntry(data2, "未利用"));
        currentPieDataSet = new PieDataSet(currentValues, "");
        currentPieDataSet.setSliceSpace(0f);
        currentPieDataSet.setDrawValues(false);
        currentPieDataSet.setColors(colors);

        currentPieData = new PieData(currentPieDataSet);
        //去除描述
        Description description = new Description();
        description.setText("");
        binding.currentPieChart.setDescription(description);
        binding.currentPieChart.setData(currentPieData);
        binding.currentPieChart.setEntryLabelTextSize(0f);
        //圆心中间字体颜色
        String text = "已利用\n\n" + data1 + "%";
        currentText = new SpannableString(text);
        ForegroundColorSpan span = new ForegroundColorSpan(Color.parseColor("#DEFFFFFF"));
        ForegroundColorSpan span1 = new ForegroundColorSpan(Color.parseColor("#FF03DAC5"));
        RelativeSizeSpan relativeSizeSpan = new RelativeSizeSpan(1.5f);
        currentText.setSpan(span, 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        currentText.setSpan(span1, 3, (text.length()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        currentText.setSpan(relativeSizeSpan, 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //设置圆心
        binding.currentPieChart.setCenterText(currentText);
        binding.currentPieChart.setHoleColor(Color.parseColor("#FF292929"));
        binding.currentPieChart.setTransparentCircleRadius(0f);
        //禁止旋转和点击
        binding.currentPieChart.setRotationEnabled(false);
        binding.currentPieChart.setHighlightPerTapEnabled(false);
        //设置字体颜色

        Legend legend=binding.currentPieChart.getLegend();
        legend.setTextColor(Color.WHITE);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.currentPieChart.invalidate();
            }
        });
    }

    private void setFutureData(int position) {
        if (futureValues.size() != 0) {
            futureValues.clear();
        }
        Double a = futureDataList.get(position);
        float data1 = (float) (a * 100);
        float data2 = (float) ((1.0 - a) * 100);

        futureValues.add(new PieEntry(data1, "已利用"));
        futureValues.add(new PieEntry(data2, "未利用"));
        futurePieDataSet = new PieDataSet(futureValues, "");
        futurePieDataSet.setSliceSpace(0f);
        futurePieDataSet.setDrawValues(false);
        futurePieDataSet.setColors(colors);
        futurePieData = new PieData(futurePieDataSet);

        Description description = new Description();
        description.setText("");
        binding.futurePieChart.setDescription(description);
        binding.futurePieChart.setData(futurePieData);
        binding.futurePieChart.setEntryLabelTextSize(0f);

        binding.futurePieChart.setRotationEnabled(false);
        binding.futurePieChart.setHighlightPerTapEnabled(false);

        String text = "已利用\n\n" + data1 + "%";
        futureText = new SpannableString(text);
        ForegroundColorSpan span = new ForegroundColorSpan(Color.parseColor("#DEFFFFFF"));
        ForegroundColorSpan span1 = new ForegroundColorSpan(Color.parseColor("#FF03DAC5"));
        RelativeSizeSpan relativeSizeSpan = new RelativeSizeSpan(1.5f);
        futureText.setSpan(span, 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        futureText.setSpan(span1, 3, (text.length()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        futureText.setSpan(relativeSizeSpan, 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.futurePieChart.setCenterText(futureText);
        binding.futurePieChart.setHoleColor(Color.parseColor("#FF292929"));
        binding.futurePieChart.setTransparentCircleRadius(0f);

        Legend legend=binding.futurePieChart.getLegend();
        legend.setTextColor(Color.WHITE);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.futurePieChart.invalidate();
            }
        });
    }


}
