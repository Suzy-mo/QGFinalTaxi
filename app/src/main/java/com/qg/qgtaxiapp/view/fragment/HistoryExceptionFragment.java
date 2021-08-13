package com.qg.qgtaxiapp.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.adapter.ExceptionItemAdapter;
import com.qg.qgtaxiapp.databinding.FragmentHistoryExceptionBinding;
import com.qg.qgtaxiapp.entity.ExceptionItem;

import java.util.ArrayList;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/11/10:56
 * @Description:
 */
public class HistoryExceptionFragment extends Fragment {
    private FragmentHistoryExceptionBinding binding;
    private RecyclerView recyclerView;
    private GridLayoutManager manager;
    private ArrayList<ExceptionItem> list=new ArrayList<>();
    private ExceptionItemAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding=FragmentHistoryExceptionBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        recyclerView=binding.exceptionRv;
        for (int i = 0; i < 10; i++) {
            list.add(new ExceptionItem());
        }
        adapter = new ExceptionItemAdapter(R.layout.exception_item,list);
        manager=new GridLayoutManager(getContext(),1,RecyclerView.VERTICAL,false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }
}
