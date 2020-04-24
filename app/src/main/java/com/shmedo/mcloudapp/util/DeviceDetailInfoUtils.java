package com.shmedo.mcloudapp.util;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.TextView;

import com.shmedo.mcloudapp.R;


/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util
 * 创建者:   dpc
 * 创建时间:  2019-12-12
 * 描述：   设备详情页面工具类
 */
public class DeviceDetailInfoUtils {
    /**
     * 计算内部电压
     * @param internalBattery
     * @return
     */
    public static String setDeviceInternalBattery(double internalBattery) {
        if (internalBattery <= 6) {
            return "1%";
        } else {
            int result = (int) ((internalBattery - 6) / (8.2 - 6) * 100);
            if (result > 100) {
                return "100%";
            } else {
                return result + "%";
            }
        }
    }

    /**
     * 设置设备状态
     * @param textView
     * @param status
     */
    public static void setDeviceStatus(TextView textView, String status, Context context) {
        if (status.equals("1")) {
            textView.setText("异常");
            textView.setTextColor(Color.RED);
        } else if (status.equals("0")) {
            textView.setText("正常");
            textView.setTextColor(context.getResources().getColor(R.color.green_53a659));
        }
    }

    /**
     * 修改传感器数据状态
     * @param status
     * @return
     */
    public static String setSensorDataStatus(TextView textView, String status, Context context) {
        if (status.equals("0")) {
            textView.setTextColor(context.getResources().getColor(R.color.green_53a659));
            return "正常";
        } else if (status.equals("1")) {
            textView.setTextColor(Color.RED);
            return "异常";
        }
        return "";
    }

    /**
     * 修改中心状态
     * @param
     * @param status
     */
    public static void setLinkStatus(TextView tvLinkStatus, TextView tvSendData, TextView tvUnSend, String status, String enable,
                                     String sendData, String generatedData, Context context) {
        String unSendData = String.valueOf(Integer.parseInt(generatedData) - Integer.parseInt(sendData));
        //如果中心使能
        if (enable.equals("1")) {
            if (status.equals("1")) {
                tvLinkStatus.setText("已上线");
                tvLinkStatus.setTextColor(context.getResources().getColor(R.color.green_53a659));
            } else if (status.equals("0")) {
                tvLinkStatus.setText("未上线");
                tvLinkStatus.setTextColor(Color.RED);
            }
            tvSendData.setText(sendData);
            tvUnSend.setText(unSendData);
        } else if (enable.equals("0")) {
            //0:未使能
            tvLinkStatus.setText("未开启");
            tvLinkStatus.setTextColor(Color.GRAY);
            tvSendData.setText("0");
            tvUnSend.setText("0");
        }
    }

    /**
     * 设置通道号地址
     */
    public static void setChannelNumber(TextView textView, String number) {
        switch (number) {
            case "2":
                textView.setText("裂缝计地址");
                break;
            case "3":
                textView.setText("土壤含水率地址");
                break;
            case "7":
                textView.setText("雷达物位计地址");
                break;
            case "21":
                textView.setText("次声地址");
                break;
        }
    }

    //设置运营商类型
    public static String setOperatorType(String type) {
        switch (type) {
            case "CMCC":
                return "移动";
            case "UNICOM":
                return "联通";
            case "CT":
                return "电信";
            default:
                return "";
        }
    }

    /**
     * 设置信号强度
     * 1~11为1格信号，12~18为2格信号，19~25为3格信号，26~31为4格信号
     *
     * @param imageView
     * @param status
     */
    public static void setSignalStrength(ImageView imageView, int status) {
        if (status >= 1 && status <= 11) {
            imageView.setBackgroundResource(R.drawable.signalstrengthone);
        } else if (status >= 12 && status <= 18) {
            imageView.setBackgroundResource(R.drawable.signalstrengthtwo);
        } else if (status >= 19 && status <= 25) {
            imageView.setBackgroundResource(R.drawable.signalstrengththree);
        } else if (status >= 16 && status <= 31) {
            imageView.setBackgroundResource(R.drawable.signalstrengthfour);
        }
    }
}
