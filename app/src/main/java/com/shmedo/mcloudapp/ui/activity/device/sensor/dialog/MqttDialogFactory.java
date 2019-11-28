package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.content.Context;
import com.shmedo.mcloudapp.inter.MQttOnClickListener;

/**
 *
 */
public class MqttDialogFactory {

    public <T> IDialogOpt<T> createDialog(final Context context, String link, T data, MQttOnClickListener onClickListener) {
        IDialogOpt opt = new MqttConfigDialog(context,link);
        opt.getDialog().show();
        if (onClickListener != null) {
            ((MqttConfigDialog) opt).setMyOnClickListener(onClickListener);
        }
        if (null != opt) {
            opt.initData(data);
        }

        return null;
    }
}
