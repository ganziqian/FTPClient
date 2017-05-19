package com.xjkb.ftpclient.utils;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * 创建人：  ganziqian
 * 作用：
 * 时间：2015/9/22
 */
public class SaveUtils {


    /**
     * 登录保存用户信息
     */
    public static void saveInfo(Context context,String ip,String name ,String psw){
        SharedPreferences preferences=context.getSharedPreferences("logininfo",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString("ip",ip);
        editor.putString("name",name);
        editor.putString("psw",psw);
        editor.commit();
    }

    /**
     * 删除用户登录信息
     */
    public static void deletInfo(Context context){
        SharedPreferences preferences= context.getSharedPreferences("logininfo", Context.MODE_PRIVATE);
        preferences.edit().clear().commit();

    }

    /**
     * 国银后台获取登录信息
     * @param tyge
     * @return
     */
    public static String  getLoginMesg(Context context,String tyge){
        SharedPreferences sharedPreferences=context.getSharedPreferences("logininfo", Context.MODE_PRIVATE);
        String type=sharedPreferences.getString(tyge, "");
        return type;
    }









}
