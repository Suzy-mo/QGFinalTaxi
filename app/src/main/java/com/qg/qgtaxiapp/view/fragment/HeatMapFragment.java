package com.qg.qgtaxiapp.view.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.qg.qgtaxiapp.databinding.FragmentHeatMapBinding;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/10/17:09
 * @Description:
 */
public class HeatMapFragment extends Fragment {
    private FragmentHeatMapBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding=FragmentHeatMapBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
}
