package com.qg.qgtaxiapp.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;

import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.databinding.ActivityTestBinding;

import com.qg.qgtaxiapp.myview.MyView;

public class test extends AppCompatActivity {


    private ActivityTestBinding binding;
    private MyView myView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        binding = ActivityTestBinding.inflate(LayoutInflater.from(this));

        myView = binding.linechart;

        new Thread(){
            public void run(){
                while (true){
                    try {
                        Thread.sleep(1000);
                        myView.setNewData((float)Math.random(),(float)Math.random());
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }
}