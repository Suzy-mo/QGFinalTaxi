package com.qg.qgtaxiapp.entity;

/**
 * @Name：QGTaxiApp
 * @Description：
 * @Author：Suzy.Mo
 * @Date：2021/8/14 19:44
 */
public class ResponseData <T>  {

    private boolean flag = true;
    private String msg = "";
    private T data = null;

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }


    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setData(T data) {
        this.data = data;
    }

}
