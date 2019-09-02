package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.content.Context;
import android.util.Log;
import android.view.View;

/**
 * 项目名：  mCloudAPP
 * 包名：    com.shmedo.mcloudapp.dialog
 * 文件名:   DialogFactory
 * 创建者:   dpc
 * 创建时间:  2019/6/14 10:34
 * 描述：    TODO
 */
public class DialogFactory {

    public <T> IDialogOpt<T> createDialog(Context context,String type, T data,int channelNumber){
        Log.i("adu","==="+type+"==="+data.toString());
        IDialogOpt opt = null;
        switch (type){
            case "02":
                opt = new DialogStyle02(context,channelNumber);
                opt.getDialog().show();
                ((DialogStyle02) opt).setCancelOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Log.i("adu","取消");
                    }
                });
                final IDialogOpt finalOpt = opt;
                ((DialogStyle02) opt).setSureonClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Log.i("adu","确认"+ finalOpt.getData().toString());

                    }
                });
                break;
            case "03":
                opt = new DialogStyle03(context);
                opt.getDialog().show();
                opt.getDialog().show();
                ((DialogStyle03) opt).setCancelOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Log.i("adu","取消");
                    }
                });

                ((DialogStyle03) opt).setSureonClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Log.i("adu","确认");

                    }
                });
                break;
            case "04":
                opt = new DialogStyle04(context);
                opt.getDialog().show();
                ((DialogStyle04) opt).setCancelClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Log.i("adu","取消");
                    }
                });
                ((DialogStyle04) opt).setSureClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Log.i("adu","确认");
                    }
                });
                break;
            case "06":
                opt = new DialogStyle06(context);
                opt.getDialog().show();
                ((DialogStyle06) opt).setCancelClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Log.i("adu","取消");
                    }
                });
                ((DialogStyle06) opt).setSureClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Log.i("adu","确认");
                    }
                });
                break;
            case "07":
                opt = new DialogStyle07(context);
                opt.getDialog().show();
                ((DialogStyle07) opt).setCancelClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Log.i("adu","取消");
                    }
                });
                ((DialogStyle07) opt).setSureClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Log.i("adu","确认");
                    }
                });
                break;
            case "08":
                opt = new DialogStyle08(context);
                opt.getDialog().show();
                ((DialogStyle08) opt).setCancelClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Log.i("adu","取消");
                    }
                });
                ((DialogStyle08) opt).setSureClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Log.i("adu","确认");
                    }
                });
                break;
            case "12":
                opt = new DialogStyle12(context);
                opt.getDialog().show();
                ((DialogStyle12) opt).setCancelClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Log.i("adu","取消");
                    }
                });
                ((DialogStyle12) opt).setSureClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Log.i("adu","确认");
                    }
                });
                break;
            case "15":
                opt = new DialogStyle15(context);
                opt.getDialog().show();
                ((DialogStyle15) opt).setCancelClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Log.i("adu","取消");
                    }
                });
                ((DialogStyle15) opt).setSureClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Log.i("adu","确认");
                    }
                });
                break;
            case "50":
                opt = new DialogStyle50(context);
                opt.getDialog().show();
                ((DialogStyle50) opt).setCancelClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Log.i("adu","取消");
                    }
                });
                ((DialogStyle50) opt).setSureClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Log.i("adu","确认");
                    }
                });
                break;
            case "53":
            case "54":
                opt = new DialogStyle5354(context);
                opt.getDialog().show();
                ((DialogStyle5354) opt).setCancelClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Log.i("adu","取消");
                    }
                });
                ((DialogStyle5354) opt).setSureClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Log.i("adu","确认");
                    }
                });
                break;
            case "51":
            case "52":
            case "55":
                opt = new DialogStyle515255(context);
                opt.getDialog().show();
                ((DialogStyle515255) opt).setCancelClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Log.i("adu","取消");
                    }
                });
                ((DialogStyle515255) opt).setSureClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Log.i("adu","确认");
                    }
                });
                break;
                default:
        }
        if (null != opt){
            opt.initData(data);
        }
        return null;
    }
}
