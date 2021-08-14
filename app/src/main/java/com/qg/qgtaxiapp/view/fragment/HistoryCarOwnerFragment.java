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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.adapter.CarOwnerAdapter;
import com.qg.qgtaxiapp.databinding.FragmentHistoryCarOwnerBinding;
import com.qg.qgtaxiapp.databinding.SearchDateLayoutBinding;
import com.qg.qgtaxiapp.entity.CarOwnerItem;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/11/10:53
 * @Description:
 */
public class HistoryCarOwnerFragment extends Fragment {

    private FragmentHistoryCarOwnerBinding binding;
    private GridLayoutManager manager;
    private ArrayList<CarOwnerItem> list = new ArrayList<>();
    private CarOwnerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryCarOwnerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        for (int i = 0; i < 50; i++) {
            list.add(new CarOwnerItem());
        }
        adapter = new CarOwnerAdapter(R.layout.car_owner_item, list);
        manager = new GridLayoutManager(getContext(), 1, RecyclerView.VERTICAL, false);
        binding.carSearchRl.setLayoutManager(manager);
        binding.carSearchRl.setAdapter(adapter);
        binding.carSearchView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });
    }

}
