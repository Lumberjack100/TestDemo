package com.shmedo.configlibrary.ble.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/1/13 <br/>
 * 描述：     通用数字式传感器信息
 */
public class CommonDigitalSensorInfo implements Parcelable {
    private String triggerThreshold;       //触发阈值
    private String correctionValue;        //修正值
    private String exValue1;   //扩展字段
    private String exValue2;
    private String exValue3;
    private String exValue4;
    private String exValue5;
    private String exValue6;
    private String exValue7;
    private String exValue8;
    private String exValue9;
    private String exValue10;
    private String exValue11;
    private String exValue12;

    public CommonDigitalSensorInfo() {
    }


    protected CommonDigitalSensorInfo(Parcel in) {
        triggerThreshold = in.readString();
        correctionValue = in.readString();
        exValue1 = in.readString();
        exValue2 = in.readString();
        exValue3 = in.readString();
        exValue4 = in.readString();
        exValue5 = in.readString();
        exValue6 = in.readString();
        exValue7 = in.readString();
        exValue8 = in.readString();
        exValue9 = in.readString();
        exValue10 = in.readString();
        exValue11 = in.readString();
        exValue12 = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(triggerThreshold);
        dest.writeString(correctionValue);
        dest.writeString(exValue1);
        dest.writeString(exValue2);
        dest.writeString(exValue3);
        dest.writeString(exValue4);
        dest.writeString(exValue5);
        dest.writeString(exValue6);
        dest.writeString(exValue7);
        dest.writeString(exValue8);
        dest.writeString(exValue9);
        dest.writeString(exValue10);
        dest.writeString(exValue11);
        dest.writeString(exValue12);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CommonDigitalSensorInfo> CREATOR = new Creator<CommonDigitalSensorInfo>() {
        @Override
        public CommonDigitalSensorInfo createFromParcel(Parcel in) {
            return new CommonDigitalSensorInfo(in);
        }

        @Override
        public CommonDigitalSensorInfo[] newArray(int size) {
            return new CommonDigitalSensorInfo[size];
        }
    };

    public String getTriggerThreshold() {
        return triggerThreshold;
    }

    public void setTriggerThreshold(String triggerThreshold) {
        this.triggerThreshold = triggerThreshold;
    }

    public String getCorrectionValue() {
        return correctionValue;
    }

    public void setCorrectionValue(String correctionValue) {
        this.correctionValue = correctionValue;
    }

    public String getExValue1() {
        return exValue1;
    }

    public void setExValue1(String exValue1) {
        this.exValue1 = exValue1;
    }

    public String getExValue2() {
        return exValue2;
    }

    public void setExValue2(String exValue2) {
        this.exValue2 = exValue2;
    }

    public String getExValue3() {
        return exValue3;
    }

    public void setExValue3(String exValue3) {
        this.exValue3 = exValue3;
    }

    public String getExValue4() {
        return exValue4;
    }

    public void setExValue4(String exValue4) {
        this.exValue4 = exValue4;
    }

    public String getExValue5() {
        return exValue5;
    }

    public void setExValue5(String exValue5) {
        this.exValue5 = exValue5;
    }

    public String getExValue6() {
        return exValue6;
    }

    public void setExValue6(String exValue6) {
        this.exValue6 = exValue6;
    }

    public String getExValue7() {
        return exValue7;
    }

    public void setExValue7(String exValue7) {
        this.exValue7 = exValue7;
    }

    public String getExValue8() {
        return exValue8;
    }

    public void setExValue8(String exValue8) {
        this.exValue8 = exValue8;
    }

    public String getExValue9() {
        return exValue9;
    }

    public void setExValue9(String exValue9) {
        this.exValue9 = exValue9;
    }

    public String getExValue10() {
        return exValue10;
    }

    public void setExValue10(String exValue10) {
        this.exValue10 = exValue10;
    }

    public String getExValue11() {
        return exValue11;
    }

    public void setExValue11(String exValue11) {
        this.exValue11 = exValue11;
    }

    public String getExValue12() {
        return exValue12;
    }

    public void setExValue12(String exValue12) {
        this.exValue12 = exValue12;
    }
}
