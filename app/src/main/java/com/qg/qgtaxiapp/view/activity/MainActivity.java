package com.qg.qgtaxiapp.view.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.databinding.ActivityMainBinding;

import java.util.ArrayList;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/10/17:15
 * @Description:
 */
public class MainActivity extends AppCompatActivity {
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        NavController navController = Navigation.findNavController(this,R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }
}
