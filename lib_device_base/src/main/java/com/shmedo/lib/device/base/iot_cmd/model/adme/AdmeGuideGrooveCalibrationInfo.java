package com.shmedo.lib.device.base.iot_cmd.model.adme;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/8/21 <br/>
 * 描述：      ADME的导槽校准配置参数
 */
public class AdmeGuideGrooveCalibrationInfo implements Parcelable {

    private String morunstate;//电机运行状态(0:停止，1:运动)
    private String movementway;//运动方式（0:正转，1:反转）
    private String motorspeed;//电机速度
    private String movepulse;// 运动脉冲

    public AdmeGuideGrooveCalibrationInfo() {
    }


    protected AdmeGuideGrooveCalibrationInfo(Parcel in) {
        morunstate = in.readString();
        movementway = in.readString();
        motorspeed = in.readString();
        movepulse = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(morunstate);
        dest.writeString(movementway);
        dest.writeString(motorspeed);
        dest.writeString(movepulse);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AdmeGuideGrooveCalibrationInfo> CREATOR = new Creator<AdmeGuideGrooveCalibrationInfo>() {
        @Override
        public AdmeGuideGrooveCalibrationInfo createFromParcel(Parcel in) {
            return new AdmeGuideGrooveCalibrationInfo(in);
        }

        @Override
        public AdmeGuideGrooveCalibrationInfo[] newArray(int size) {
            return new AdmeGuideGrooveCalibrationInfo[size];
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

    public String getMovePulse() {
        return TextUtils.isEmpty(movepulse) ? "" : movepulse;
    }

    public void setMovePulse(String moveangle) {
        this.movepulse = moveangle;
    }
}
