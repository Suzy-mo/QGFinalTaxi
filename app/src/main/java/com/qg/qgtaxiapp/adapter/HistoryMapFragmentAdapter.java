package com.qg.qgtaxiapp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/11/10:39
 * @Description:
 */
public class HistoryMapFragmentAdapter extends FragmentStateAdapter {
    private final ArrayList<Fragment> fragments;

    public HistoryMapFragmentAdapter(@NonNull FragmentActivity fragmentActivity,ArrayList<Fragment> list) {
        super(fragmentActivity);
        this.fragments=list;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }
}
