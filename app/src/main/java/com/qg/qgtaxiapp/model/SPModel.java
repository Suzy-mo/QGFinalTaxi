package com.qg.qgtaxiapp.model;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created with Android studio
 *
 * @Author: EDGClearlove7
 * @Date: 2021/08/14/20:29
 * @Description:
 */
public class SPModel {
    private static SPModel INSTANCE = new SPModel();
    private SPModel(){}
    public static SPModel getInstance(){
        return INSTANCE;
    }
    private SharedPreferences preferences ;
    private SharedPreferences.Editor editor;

    public void init(Context context){
        preferences = context.getSharedPreferences("my_data",MODE_PRIVATE);
        editor = preferences.edit();
    }

    public void setDataList(String tag, ArrayList<String> list){
        if(list==null||list.size()<=0){
            return;
        }
        Gson gson=new Gson();
        String str=gson.toJson(list);
        editor.putString(tag,str);
        editor.apply();
    }

    public ArrayList<String> getDataBean(String tag){
        ArrayList<String> list=new ArrayList<>();
        String str=preferences.getString(tag,null);
        if(str==null){
            return list;
        }
        Gson gson=new Gson();
        list=gson.fromJson(str,new TypeToken<ArrayList<String>>(){}.getType());
        return list;
    }

    public void cleanData(String Tag){
        editor.remove(Tag);
        editor.commit();
    }
}
