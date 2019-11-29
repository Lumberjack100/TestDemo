package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.content.Context;

import com.shmedo.mcloudapp.inter.MyOnClickListener;

/**
 * 项目名：  mCloudAPP
 * 包名：    com.shmedo.mcloudapp.dialog
 * 文件名:   DialogFactory
 * 创建者:   dpc
 * 创建时间:  2019/6/14 10:34
 * 描述：    TODO
 */
public class DialogFactory {

    public <T> IDialogOpt<T> createDialog(final Context context, String type, T data, String channelNumber, MyOnClickListener onClickListener) {
        IDialogOpt opt = null;

        switch (type) {
            case "02":
                opt = new DialogStyle02(context, channelNumber);
                opt.getDialog().show();

                if (onClickListener != null) {
                    ((DialogStyle02) opt).setMyOnClickListener(onClickListener);
                }
                break;

            case "03":
                opt = new DialogStyle03(context, channelNumber);
                opt.getDialog().show();

                if (onClickListener != null) {
                    ((DialogStyle03) opt).setMyOnClickListener(onClickListener);
                }
                break;

            case "04":
                opt = new DialogStyle04(context, channelNumber);
                opt.getDialog().show();

                if (onClickListener != null) {
                    ((DialogStyle04) opt).setMyOnClickListener(onClickListener);
                }
                break;

            case "06":
                opt = new DialogStyle06(context, channelNumber);
                opt.getDialog().show();

                if (onClickListener != null) {
                    ((DialogStyle06) opt).setMyOnClickListener(onClickListener);
                }
                break;

            case "07":
                opt = new DialogStyle07(context, channelNumber);
                opt.getDialog().show();

                if (onClickListener != null) {
                    ((DialogStyle07) opt).setMyOnClickListener(onClickListener);
                }
                break;

            case "08":
                opt = new DialogStyle08(context, channelNumber);
                opt.getDialog().show();

                if (onClickListener != null) {
                    ((DialogStyle08) opt).setMyOnClickListener(onClickListener);
                }
                break;

            case "12":
                opt = new DialogStyle12(context, channelNumber);
                opt.getDialog().show();

                if (onClickListener != null) {
                    ((DialogStyle12) opt).setMyOnClickListener(onClickListener);
                }
                break;

            case "15":
                opt = new DialogStyle15(context, channelNumber);
                opt.getDialog().show();

                if (onClickListener != null) {
                    ((DialogStyle15) opt).setMyOnClickListener(onClickListener);
                }
                break;

            case "50":
                opt = new DialogStyle50(context, channelNumber);
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
