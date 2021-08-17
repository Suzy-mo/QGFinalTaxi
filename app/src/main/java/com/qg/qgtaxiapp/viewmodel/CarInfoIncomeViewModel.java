package com.qg.qgtaxiapp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.qg.qgtaxiapp.entity.CarIncomeBean;

/**
 * @Name：QGTaxiApp
 * @Description：
 * @Author：Suzy.Mo
 * @Date：2021/8/17 15:13
 */
public class CarInfoIncomeViewModel extends ViewModel {

    public MutableLiveData<String> date = new MutableLiveData<>();
    public MutableLiveData<CarIncomeBean> carIncomeData = new MutableLiveData<>();

}
