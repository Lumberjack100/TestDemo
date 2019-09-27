package com.shmedo.mcloudapp.util;

import android.widget.Toast;

import com.shmedo.mcloudapp.App;

/**
 * 项目名：  eMeas
 * 包名：    com.shmedo.emeas.util
 * 文件名:   ToastUtil
 * 创建者:   dpc
 * 创建时间:  2017/7/18 上午9:40
 * 描述：    吐司工具类
 */

public class ToastUtil {

    private static Toast toast;
    public static void showLongToast(String content) {
        if (toast == null) {  //判断Toast对象是否为空
            toast = Toast.makeText(App.getContext(), content, Toast.LENGTH_LONG);
        } else {
            toast.setText(content);
        }
        toast.show();
    }
    public static void showShortToast(String content) {
        if (toast == null) {  //判断Toast对象是否为空
            toast = Toast.makeText(App.getContext(), content, Toast.LENGTH_SHORT);
        } else {
            toast.setText(content);
        }
        toast.show();
    }
}
