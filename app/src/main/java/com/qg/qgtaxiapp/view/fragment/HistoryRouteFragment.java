package com.qg.qgtaxiapp.view.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.qg.qgtaxiapp.databinding.FragmentHistoryRouteBinding;
import com.qg.qgtaxiapp.databinding.SearchDateLayoutBinding;

import java.util.Calendar;

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
    private int year;
    private int monthOfYear;
    private int dayOfMonth;
    private Calendar calendar;
    private AlertDialog dialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding=FragmentHistoryRouteBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        initListener();
    }

    private void initListener() {
        binding.routeSearchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding1 = SearchDateLayoutBinding.inflate(getLayoutInflater());
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setView(binding1.getRoot());
                dialog = builder.create();
                initDate();
                binding1.datePicker.init(year, monthOfYear, dayOfMonth, new MyOnDateChangeListener());
                binding1.dismissIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        binding.routeSearchRl.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
                if(dialog.isShowing()){
                    binding.routeSearchRl.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void initDate() {
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        monthOfYear = calendar.get(Calendar.MONTH);
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
    }

    class MyOnDateChangeListener implements DatePicker.OnDateChangedListener {
        @Override
        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Log.d("==========", year + "");
            Log.d("==========", monthOfYear + "");
            Log.d("==========", dayOfMonth + 1 + "");
        }
    }
}
