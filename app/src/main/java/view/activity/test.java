package view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;

import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.databinding.ActivityTestBinding;

public class test extends AppCompatActivity {

    private ActivityTestBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        binding = ActivityTestBinding.inflate(LayoutInflater.from(this));

    }
}