package com.qg.qgtaxiapp.view.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.adapter.HistoryMapFragmentAdapter;
import com.qg.qgtaxiapp.databinding.FragmentHistoryMapBinding;
import com.qg.qgtaxiapp.databinding.HistoryTabItemBinding;
import com.qg.qgtaxiapp.viewmodel.HistoryMapViewModel;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/10/17:13
 * @Description:
 */
public class HistoryMapFragment extends Fragment {

    private FragmentHistoryMapBinding binding;
    private final String[] tabList = {"行驶路径", "车主信息", "异常情况"};
    private final ArrayList<Fragment> fragments = new ArrayList<>();
    private HistoryMapFragmentAdapter adapter;
    private TabLayout tabLayout;
    private HistoryMapViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel= new ViewModelProvider(getActivity()).get(HistoryMapViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryMapBinding.inflate(inflater, container, false);
        viewModel.viewPager2=binding.historyViewpager2;
        if(savedInstanceState==null){
            Log.d("===============","黑屏了吗");
            fragments.add(new HistoryRouteFragment());
            fragments.add(new HistoryCarOwnerFragment());
            fragments.add(new HistoryExceptionFragment());
            adapter = new HistoryMapFragmentAdapter(getActivity(), fragments);
            binding.historyViewpager2.setAdapter(adapter);
        }
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        tabLayout = binding.historyTabLayout;
        for (int i = 0; i < tabList.length; i++) {
            tabLayout.addTab(tabLayout.newTab());
            new TabLayoutMediator(binding.historyTabLayout, binding.historyViewpager2, new TabLayoutMediator.TabConfigurationStrategy() {
                @Override
                public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                    tab.setText(tabList[position]);
                }
            }).attach();
            binding.historyViewpager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    binding.historyViewpager2.setCurrentItem(position);
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("key",1);
    }
}
