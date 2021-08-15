package com.qg.qgtaxiapp.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.qg.qgtaxiapp.databinding.FragmentCarInformationBinding;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/15/21:15
 * @Description:
 */
public class CarInformationMapFragment extends Fragment {
    private FragmentCarInformationBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding=FragmentCarInformationBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
}
