package com.qg.qgtaxiapp.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.qg.qgtaxiapp.adapter.HeatMapFragmentAdapter;
import com.qg.qgtaxiapp.databinding.FragmentHeatMapBinding;
import com.qg.qgtaxiapp.viewmodel.HeatMapViewModel;

import java.util.ArrayList;
import java.util.List;


/**
 * Created with Android studio
 *
 * @Author: Hx
 * @Date: 2021/08/10/17:09
 * @Description:
 */
public class HeatMapFragment extends Fragment{

    private FragmentHeatMapBinding binding;
    private HeatMapViewModel heatMapViewModel;
    private final String[] tabList = {"热力图","载客热点","广告牌"};
    private final List<Fragment> fragments = new ArrayList<>();
    private TabLayout tabLayout;
    private HeatMapFragmentAdapter adapter;
    private ViewPager2 heatMapViewPager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        heatMapViewModel = new ViewModelProvider(getActivity()).get(HeatMapViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHeatMapBinding.inflate(inflater,container,false);
        heatMapViewPager = binding.vp2HeatMap;
        heatMapViewModel.heatMapVP = binding.vp2HeatMap;
        if (savedInstanceState == null){
            fragments.add(new HeatMapHeatFragment());
            fragments.add(new HeatMapPassengerFragment());
            fragments.add(new HeatMapADFragment());
            adapter = new HeatMapFragmentAdapter(getActivity().getSupportFragmentManager(), getLifecycle(), fragments);
            heatMapViewPager.setAdapter(adapter);
        }
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        tabLayout = binding.fragmentHeatTabLayout;
        for (int i = 0; i < tabList.length; i++){
            tabLayout.addTab(tabLayout.newTab());

            new TabLayoutMediator(binding.fragmentHeatTabLayout, heatMapViewPager, new TabLayoutMediator.TabConfigurationStrategy() {
                @Override
                public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                    tab.setText(tabList[position]);
                }
            }).attach();

            heatMapViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    heatMapViewPager.setCurrentItem(position);
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("key",1);
    }


}
