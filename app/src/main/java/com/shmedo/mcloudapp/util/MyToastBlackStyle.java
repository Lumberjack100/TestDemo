package com.shmedo.mcloudapp.util;

import android.content.Context;

import com.hjq.toast.style.ToastBlackStyle;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/8/21 <br/>
 * 描述：     默认黑色样式实现
 */
public class MyToastBlackStyle extends ToastBlackStyle {
    public MyToastBlackStyle(Context context) {
        super(context);
    }

    @Override
    public int getMaxLines() {
        return 10;
    }
}
