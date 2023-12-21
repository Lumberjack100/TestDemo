package com.shmedo.lib.device.base.iot_cmd.model.adme;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/6/21 <br/>
 * 描述：    ADME的测量孔深配置参数
 */
public class AdmeMeasuringHoleDepthInfo implements Parcelable {
    private String morunstate;//电机运行状态(0:停止，1:运动)
    private String movementway;//运动方式（0:上拉，1:下放）
    private String motorspeed;//电机速度
    private String movedistance;//运动距离

    public AdmeMeasuringHoleDepthInfo() {
    }

    protected AdmeMeasuringHoleDepthInfo(Parcel in) {
        morunstate = in.readString();
        movementway = in.readString();
        motorspeed = in.readString();
        movedistance = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(morunstate);
        dest.writeString(movementway);
        dest.writeString(motorspeed);
        dest.writeString(movedistance);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AdmeMeasuringHoleDepthInfo> CREATOR = new Creator<AdmeMeasuringHoleDepthInfo>() {
        @Override
        public AdmeMeasuringHoleDepthInfo createFromParcel(Parcel in) {
            return new AdmeMeasuringHoleDepthInfo(in);
        }

        @Override
        public AdmeMeasuringHoleDepthInfo[] newArray(int size) {
            return new AdmeMeasuringHoleDepthInfo[size];
        }
    };

    public String getMorunstate() {
        return TextUtils.isEmpty(morunstate) ? "" : morunstate;
    }

    public void setMorunstate(String morunstate) {
        this.morunstate = morunstate;
    }

    public String getMovementway() {
        return TextUtils.isEmpty(movementway) ? "" : movementway;
    }

    public void setMovementway(String movementway) {
        this.movementway = movementway;
    }

    public String getMotorspeed() {
        return TextUtils.isEmpty(motorspeed) ? "" : motorspeed;
    }

    public void setMotorspeed(String motorspeed) {
        this.motorspeed = motorspeed;
    }

    public String getMovedistance() {
        return TextUtils.isEmpty(movedistance) ? "" : movedistance;
    }

    public void setMovedistance(String movedistance) {
        this.movedistance = movedistance;
    }
}
