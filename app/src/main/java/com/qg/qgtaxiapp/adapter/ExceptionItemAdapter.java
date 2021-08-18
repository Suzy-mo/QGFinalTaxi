package com.qg.qgtaxiapp.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.qg.qgtaxiapp.R;
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
    private final ArrayList<ExceptionItem> list;
    public ExceptionItemAdapter(int layoutResId, ArrayList<ExceptionItem> data) {
        super(layoutResId, data);
        this.list=data;
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, ExceptionItem exceptionItem) {
        baseViewHolder.setText(R.id.exception_car_id_tv,exceptionItem.getCarID());
        baseViewHolder.setText(R.id.exception_date_id_tv,exceptionItem.getDate());
        baseViewHolder.setText(R.id.exception_start_location_tv,exceptionItem.getStartAddress());
        baseViewHolder.setText(R.id.exception_end_location_tv,exceptionItem.getEndAddress());
        baseViewHolder.setText(R.id.exception_info_tv,exceptionItem.getExceptionText());
    }
}
