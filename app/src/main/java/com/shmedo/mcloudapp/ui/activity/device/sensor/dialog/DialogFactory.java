package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.content.Context;

import com.shmedo.mcloudapp.interfaces.MyOnClickListener;

/**
 * 项目名：  mCloudAPP
 * 包名：    com.shmedo.mcloudapp.dialog
 * 文件名:   DialogFactory
 * 创建者:   dpc
 * 创建时间:  2019/6/14 10:34
 *
 */
public class DialogFactory {

    public <T> IDialogOpt<T> createDialog(final Context context, String type, T data, String channelNumber, MyOnClickListener onClickListener) {
        IDialogOpt opt = null;

        switch (type) {
            case "02"://拉线位移计
            case "03"://土壤含水率
            case "07"://雷达物位计
            case "21"://次声
                opt = new DialogStyle02(context, channelNumber);
                opt.getDialog().show();

                if (onClickListener != null) {
                    ((DialogStyle02) opt).setMyOnClickListener(onClickListener);
                }
                break;

            case "04"://测斜仪采集器
                opt = new DialogStyle04(context, channelNumber);
                opt.getDialog().show();

                if (onClickListener != null) {
                    ((DialogStyle04) opt).setMyOnClickListener(onClickListener);
                }
                break;

            case "08":
                opt = new DialogStyle08(context, channelNumber);
                opt.getDialog().show();

                if (onClickListener != null) {
                    ((DialogStyle08) opt).setMyOnClickListener(onClickListener);
                }
                break;

            case "50":
                opt = new Osmometer_AxialForceGaugeDialog(context, channelNumber);
                opt.getDialog().show();

                if (onClickListener != null) {
                    ((DialogStyle50) opt).setMyOnClickListener(onClickListener);
                }
                break;

            case "53":
            case "54":
                opt = new DialogStyle5354(context, channelNumber);
                opt.getDialog().show();

                if (onClickListener != null) {
                    ((DialogStyle5354) opt).setMyOnClickListener(onClickListener);
                }
                break;

            case "51":
            case "52":
            case "55":
                opt = new DialogStyle515255(context, channelNumber);
                opt.getDialog().show();

                if (onClickListener != null) {
                    ((DialogStyle515255) opt).setMyOnClickListener(onClickListener);
                }
                break;

            default:
        }

        if (null != opt) {
            opt.initData(data);
        }

        return null;
    }

}
