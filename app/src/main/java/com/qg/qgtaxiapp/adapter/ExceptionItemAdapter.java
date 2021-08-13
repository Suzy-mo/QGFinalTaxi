package com.qg.qgtaxiapp.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.qg.qgtaxiapp.entity.ExceptionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/12/14:31
 * @Description:
 */
public class ExceptionItemAdapter extends BaseQuickAdapter<ExceptionItem, BaseViewHolder> {
    private ArrayList<ExceptionItem> list;
    public ExceptionItemAdapter(int layoutResId, ArrayList<ExceptionItem> data) {
        super(layoutResId, data);
        this.list=data;
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, ExceptionItem exceptionItem) {

    }
}
