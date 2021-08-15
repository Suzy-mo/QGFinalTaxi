package com.qg.qgtaxiapp.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.qg.qgtaxiapp.R;
import com.qg.qgtaxiapp.entity.HistoryInfo;

import java.util.ArrayList;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/12/21:25
 * @Description:
 */
public class HistoryInfoAdapter extends BaseQuickAdapter<HistoryInfo, BaseViewHolder> {
    private ArrayList<HistoryInfo> list;
    public HistoryInfoAdapter(int layoutResId, ArrayList<HistoryInfo> data) {
        super(layoutResId, data);
        this.list=data;
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, HistoryInfo historyInfo) {
        baseViewHolder.setText(R.id.car_search_owner_id_tv,historyInfo.getCarID());
    }
}
