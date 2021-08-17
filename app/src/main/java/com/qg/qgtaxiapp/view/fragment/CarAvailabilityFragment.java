package com.qg.qgtaxiapp.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.qg.qgtaxiapp.databinding.FragmentCarAvailibilityBinding;
import com.qg.qgtaxiapp.databinding.FragmentCarInformationBinding;

/**
 * @Name：QGTaxiApp
 * @Description：
 * @Author：Suzy.Mo
 * @Date：2021/8/16 23:17
 */
public class CarAvailabilityFragment extends Fragment {

    private FragmentCarAvailibilityBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCarAvailibilityBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
}
