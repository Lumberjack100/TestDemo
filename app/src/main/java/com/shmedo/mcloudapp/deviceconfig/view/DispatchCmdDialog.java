package com.shmedo.mcloudapp.deviceconfig.view;

import android.content.Context;

import androidx.annotation.NonNull;

import com.lxj.xpopup.core.CenterPopupView;
import com.shmedo.mcloudapp.R;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/30 <br/>
 * 描述：     TODO
 */
public class DispatchCmdDialog extends CenterPopupView {
    public DispatchCmdDialog(@NonNull Context context) {
        super(context);
    }


    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_expired_guide;
    }

    @Override
    protected void onCreate() {
        super.onCreate();

    }
}
