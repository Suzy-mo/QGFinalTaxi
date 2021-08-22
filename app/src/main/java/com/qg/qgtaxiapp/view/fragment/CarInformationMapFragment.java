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
import com.qg.qgtaxiapp.adapter.CarInfoAdapter;
import com.qg.qgtaxiapp.databinding.FragmentCarInformationBinding;
import com.qg.qgtaxiapp.viewmodel.CarTrafficViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/15/21:15
 * @Description:
 */
public class CarInformationMapFragment extends Fragment {

    private FragmentCarInformationBinding binding;
    private CarTrafficViewModel viewModel;
    List<String> titles=new ArrayList<>();
    List<Fragment> fragments=new ArrayList<>();
    CarInfoAdapter mPagerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding=FragmentCarInformationBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //新建
        titles.add("流量分析");
        titles.add("司机收入");
        titles.add("车辆利用率");
        fragments.add(new CarTrafficFlowFragment());
        fragments.add(new CarIncomeFragment());
        fragments.add(new CarAvailabilityFragment());

        viewModel= new ViewModelProvider(getActivity()).get(CarTrafficViewModel.class);
        mPagerAdapter = new CarInfoAdapter(getParentFragmentManager(),getLifecycle(),fragments);
        viewModel.vp2=binding.vp2HeatMap;
        binding.vp2HeatMap.setAdapter(mPagerAdapter);

//和tabLayout绑定
        new TabLayoutMediator(binding.fragmentHeatTabLayout, binding.vp2HeatMap, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(titles.get(position));
            }
        }).attach();
        binding.vp2HeatMap.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.vp2HeatMap.setCurrentItem(position);
            }
        });
    }
}
