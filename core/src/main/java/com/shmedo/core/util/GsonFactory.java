package com.shmedo.core.util;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 项目名：  mobileAndroid
 * 包名：    com.shmedo.mobileandroid.util
 * 文件名:   GsonFactory
 * 创建者:   dpc
 * 创建时间:  2018/11/24 00:37
 *
 */
public class GsonFactory {
    private static GsonBuilder gsonBuilder;

    static {
        gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeNulls();
        gsonBuilder.setDateFormat("yyyy-MM-dd HH:mm:ss");
        //gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE);
    }


    public static Gson getGson() {
        return gsonBuilder.create();
    }


    public static Gson getLowerCaseGson() {
        GsonBuilder gb = new GsonBuilder();
        gb.serializeNulls();
        gb.setDateFormat("yyyy-MM-dd HH:mm:ss");
//        gb.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        return gb.create();
    }
}
