package com.qg.qgtaxiapp.view.activity;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.databinding.ActivitySeachCarOwnerLayoutBinding;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/11/21:23
 * @Description:
 */
public class SkipSearchCarOwnerActivity extends AppCompatActivity {
    private ActivitySeachCarOwnerLayoutBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySeachCarOwnerLayoutBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
    }
}
