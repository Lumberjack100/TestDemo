package com.shmedo.mcloudapp.util;

import android.content.Context;
import android.content.Intent;

/**
 * 项目名：  eMeas
 * 包名：    com.shmedo.emeas.util
 * 文件名:   StartActivityUtil
 * 创建者:   dpc
 * 创建时间:  2017/7/18 上午9:36
 * 描述：    跳转页面工具类
 */

public class StartActivityUtil {
    public static void comeOnBaby(Context context, Class<?> cls){
        if(null == context){
            ToastUtil.showSToast("context is null");
            return;
        }
        if(null == cls){
            ToastUtil.showSToast("cls is null");
            return;
        }
        context.startActivity(new Intent(context,cls));
    }
}
