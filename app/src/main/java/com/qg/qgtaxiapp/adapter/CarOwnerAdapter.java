package com.qg.qgtaxiapp.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.databinding.CarOwnerItemBinding;
import com.qg.qgtaxiapp.entity.CarOwnerItem;

import java.util.ArrayList;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/11/19:54
 * @Description:
 */
public class CarOwnerAdapter extends BaseQuickAdapter<CarOwnerItem, BaseViewHolder> {
    private ArrayList<CarOwnerItem> list;
    public CarOwnerAdapter(int layoutResId, ArrayList<CarOwnerItem> data) {
        super(layoutResId, data);
        this.list=data;
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, CarOwnerItem carOwnerItem) {
//        baseViewHolder.setText(R.id.car_id,carOwnerItem.getCarID());
//        baseViewHolder.setText(R.id.company_tv,carOwnerItem.getCompanyID());
//        baseViewHolder.setText(R.id.mile_tv,carOwnerItem.getMile());
//        baseViewHolder.setText(R.id.score_tv,carOwnerItem.getScore());
//        baseViewHolder.setText(R.id.date_tv,carOwnerItem.getDate());
    }
}