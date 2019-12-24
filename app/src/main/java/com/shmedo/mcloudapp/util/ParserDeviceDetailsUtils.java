package com.shmedo.mcloudapp.util;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.TextView;
import com.shmedo.das.das.cmd.CommandResult;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.devicedetails.*;

import java.util.ArrayList;
import java.util.List;

import static com.shmedo.mcloudapp.util.StringUtil.isNullOrEmpty;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util
 * 创建者:   dpc
 * 创建时间:  2019-12-12
 * 描述：    解析设备详情数据
 */
public class ParserDeviceDetailsUtils {

    /**
     * 解析设备版本信息
     * @param result $$040,150000L,DAS-LF-V3.0.2,170817
     * @return DeviceVersionInfo
     */
    public static DeviceVersionInfo parserVersionInfo(String result){
        if (isNullOrEmpty(result) ||  result.length() < CommandResult.RESULT_MIN_LENGTH)
            throw new IllegalArgumentException("指令结果格式错误:" + result);
        if (result.replace("\r\n","").endsWith("e")){
            return null;
        }

        DeviceVersionInfo info = new DeviceVersionInfo();
        String[] cmd = result.replace("\r\n", "").split(",",-1);
        info.setSnNumber(cmd[1]);
        info.setFirmwareVersion(cmd[2]);
        info.setProductionDate(cmd[3]);
        return info;
    }

    /**
     * 设备状态1
     * @param result $$041,150000L,865860047575320,898604061918C0643348,20,1,9
     * @return DeviceStatusOne
     */
    public static DeviceStatusOne parserDeviceStatusOne(String result){
        if (isNullOrEmpty(result) ||  result.length() < CommandResult.RESULT_MIN_LENGTH)
            throw new IllegalArgumentException("指令结果格式错误:" + result);
        if (result.replace("\r\n","").endsWith("e")){
            return null;
        }

        DeviceStatusOne statusOne = new DeviceStatusOne();
        String[] cmd = result.replace("\r\n", "").split(",",-1);
        statusOne.setSnNumber(cmd[1]);
        statusOne.setImeiNumber(cmd[2]);
        statusOne.setSimNumber(cmd[3]);
        statusOne.setStartCodeOne(cmd[4]);
        statusOne.setStartCodeTwo(cmd[5]);
        statusOne.setSignalStrength(cmd[6]);
        return statusOne;
    }

    /**
     * 设备状态2
     * @param result $$042,150000L,0.000000,0.000000,8.4,11.9,0,1.1,11.9,0.0,0.0,0,23.1,35.9,0,23.8,35.1,2,0.0
     * @return DeviceStatusTwo
     */
    public static DeviceStatusTwo parserDeviceStatusTwo(String result){
        if (isNullOrEmpty(result) ||  result.length() < CommandResult.RESULT_MIN_LENGTH)
            throw new IllegalArgumentException("指令结果格式错误:" + result);
        if (result.replace("\r\n","").endsWith("e")){
            return null;
        }

        DeviceStatusTwo statusTwo = new DeviceStatusTwo();
        String[] cmd = result.replace("\r\n", "").split(",",-1);
        statusTwo.setSnNumber(cmd[1]);
        statusTwo.setLongitude(cmd[2]);
        statusTwo.setLatitude(cmd[3]);
        statusTwo.setInternalVoltage(Double.parseDouble(cmd[4]));
        statusTwo.setExternalVoltage(cmd[5]);
        statusTwo.setSolarControllerStatus(cmd[6]);
        statusTwo.setSolarPanelVoltage(cmd[7]);
        statusTwo.setBatteryVoltage(cmd[8]);
        statusTwo.setDailyPowerGeneration(cmd[9]);
        statusTwo.setDailyPowerConsumption(cmd[10]);
        statusTwo.setInternalTempHumidityStatus(cmd[11]);
        statusTwo.setInternalTemperature(cmd[12]);
        statusTwo.setInternalHumidity(cmd[13]);
        statusTwo.setExternalTempHumidityStatus(cmd[14]);
        statusTwo.setExternalTemperature(cmd[15]);
        statusTwo.setExternalHumidity(cmd[16]);
        statusTwo.setSwitchType(cmd[17]);
        statusTwo.setRainfallStatus(cmd[18]);
        return statusTwo;
    }

    /**
     * 设备状态3
     * @param result $$043,150000L,2,2,3:0:3.1,5:0:3.1\r\n
     * @return DeviceStatusThree
     */
    public static DeviceStatusThree parserDeviceStatusThree(String result){
        if (isNullOrEmpty(result) ||  result.length() < CommandResult.RESULT_MIN_LENGTH)
            throw new IllegalArgumentException("指令结果格式错误:" + result);
        if (result.replace("\r\n","").endsWith("e")){
            return null;
        }
        List<String> list = new ArrayList<>();
        DeviceStatusThree statusThree = new DeviceStatusThree();
        String[] cmd = result.replace("\r\n", "").split(",",-1);
        for (int i = 4; i < cmd.length; i++) {
            list.add(cmd[i]);
        }
        statusThree.setSnNumber(cmd[1]);
        statusThree.setCollectorModel(cmd[2]);
        statusThree.setCollectorAddress(cmd[3]);
        statusThree.setSensorStatus(list);
        return statusThree;
    }


    /**
     * 网络状态
     * @param result $$0441,7,7,1,0x001000D4,0x001000D4,1,1,4,7,93.6
     * @return  DeviceInternetStatus
     */
    public static DeviceInternetStatus parserInternetStatus(String result){
        if (isNullOrEmpty(result) ||  result.length() < CommandResult.RESULT_MIN_LENGTH)
            throw new IllegalArgumentException("指令结果格式错误:" + result);
        if (result.replace("\r\n","").endsWith("e")){
            return null;
        }

        DeviceInternetStatus internetStatus = new DeviceInternetStatus();
        String[] cmd = result.replace("\r\n", "").split(",",-1);
        String linkNumber = cmd[0].substring(5,6);
        internetStatus.setLinkNumber(linkNumber);
        internetStatus.setSentData(cmd[1]);
        internetStatus.setGeneratedData(cmd[2]);
        internetStatus.setFlashEnable(cmd[3]);
        internetStatus.setFlashReadPointer(cmd[4]);
        internetStatus.setFlashWritePointer(cmd[5]);
        internetStatus.setLinkEnable(cmd[6]);
        internetStatus.setLinkStatus(cmd[7]);
        internetStatus.setFourGModuleStatus(cmd[8]);
        internetStatus.setMqttStatus(cmd[9]);
        internetStatus.setOnlineRate(cmd[10]);
        return internetStatus;
    }

    /**
     * 运营商信息
     * @param result $$014,22,0,20,1,89860446091891274425,0.0,6.9,0.1,CMCC,7
     * @return  OperatorInformation
     */
    public static OperatorInformation parserOperatorInformation(String result){
        if (isNullOrEmpty(result) ||  result.length() < CommandResult.RESULT_MIN_LENGTH)
            throw new IllegalArgumentException("指令结果格式错误:" + result);
        if (result.replace("\r\n","").endsWith("e")){
            return null;
        }

        OperatorInformation operatorInformation = new OperatorInformation();
        String[] cmd = result.replace("\r\n", "").split(",",-1);

        operatorInformation.setSignalStrength(Integer.parseInt(cmd[1]));
        operatorInformation.setGPSSearchStars(cmd[2]);
        operatorInformation.setStartupCode1(cmd[3]);
        operatorInformation.setStartupCode2(cmd[4]);
        operatorInformation.setSIMCardNumber(cmd[5]);
        operatorInformation.setMCUTemperature(cmd[6]);
        operatorInformation.setDeviceInternalVoltage(cmd[7]);
        operatorInformation.setDeviceExternalVoltage(cmd[8]);
        operatorInformation.setOperatorType(cmd[9]);
        operatorInformation.setNetworkType(cmd[10]);
        return operatorInformation;
    }

    /**
     * 计算内部电压
     * @param internalBattery
     * @return
     */
    public static String setDeviceInternalBattery(double internalBattery){
        if (internalBattery <= 6){
            return "1%";
        }else {
            int result = (int) ((internalBattery - 6)/(8.2-6)*100);
            if (result>100){
                return "100%";
            }else {
                return result+"%";
            }
        }
    }
    /**
     * 设置设备状态
     * @param textView
     * @param status
     */
    public static void setDeviceStatus(TextView textView, String status,Context context){
        if (status.equals("1")){
            textView.setText("异常");
            textView.setTextColor(Color.RED);
        }else if (status.equals("0")){
            textView.setText("正常");
            textView.setTextColor(context.getResources().getColor(R.color.green_53a659));
        }
    }

    /**
     * 修改传感器数据状态
     * @param status
     * @return
     */
    public static String setSensorDataStatus(TextView textView,String status,Context context){
        if (status.equals("0")){
            textView.setTextColor(context.getResources().getColor(R.color.green_53a659));
            return "正常";
        }else if (status.equals("1")){
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
    public static void setLinkStatus(TextView tvLinkStatus,TextView tvSendData,TextView tvUnSend, String status,String enable,
                                     String sendData,String generatedData,Context context){
        String unSendData =String.valueOf( Integer.parseInt(generatedData) - Integer.parseInt(sendData));
        //如果中心使能
        if (enable.equals("1")){
            if (status.equals("1")){
                tvLinkStatus.setText("已上线");
                tvLinkStatus.setTextColor(context.getResources().getColor(R.color.green_53a659));
            }else if (status.equals("0")){
                tvLinkStatus.setText("未上线");
                tvLinkStatus.setTextColor(Color.RED);
            }
            tvSendData.setText(sendData);
            tvUnSend.setText(unSendData);
        }else if (enable.equals("0")){
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
    public static void setChannelNumber(TextView textView, String number){
        switch (number){
            case "2":
                textView.setText("裂缝计地址");
                break;
            case "3":
                textView.setText("雷达物位计地址");
                break;
            case "7":
                textView.setText("土壤含水率地址");
                break;
            case "21":
                textView.setText("次声地址");
                break;
        }
    }

    //设置运营商类型
    public static String setOperatorType(String type){
        switch (type){
            case "CMCC": return "移动";
            case "UNICOM": return "联通";
            case "CT": return "电信";
            default:return "";
        }
    }

    /**
     * 设置信号强度
     * 1~11为1格信号，12~18为2格信号，19~25为3格信号，26~31为4格信号
     * @param imageView
     * @param status
     */
    public static void setSignalStrength(ImageView imageView,int status){
       if (status >= 1 && status <= 11){
            imageView.setBackgroundResource(R.drawable.signalstrengthone);
       } else if (status >= 12 && status <= 18){
           imageView.setBackgroundResource(R.drawable.signalstrengthtwo);
       }else if (status >= 19 && status <= 25){
           imageView.setBackgroundResource(R.drawable.signalstrengththree);
       }else if (status >= 16 && status <= 31){
           imageView.setBackgroundResource(R.drawable.signalstrengthfour);
       }
    }
}
