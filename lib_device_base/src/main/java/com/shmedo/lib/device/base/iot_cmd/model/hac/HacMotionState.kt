package com.shmedo.lib.device.base.iot_cmd.model.hac;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/28/20 <br/>
 * 描述：     ADME HAC的电机运动状态
 */
public class HacMotionState implements Parcelable {
    private String abndiasis;//设备异常诊断 0：正常
    private String measmode;//测量模式 (0：正测  1：反测)
    //电机运动信息(0：上拉至管口 1：测斜仪配对,设置参数 2：测斜仪下放 3：管底等待 4：测点测量 5：磁开关触发，测量结束 6：测斜仪配对,读取数据 7：数据上传 8：数据上传完成等待下次测量 9:等待反测)
    //注：单次测量过程 0、1、2、3、4、5、6、7、8
    //   正反测量过程 0、1、2、3、4、5、6、9—->1、2、3、4、5、6、7、8
    private String motorinfo;//电机运动信息
    private String measpoint;//测点信息 (1|20.5  表示第一个测量点：20.5米)
    private String waittime;//等待时间
    private String incvoltage;//测斜仪电压
    private String driveinputv;//驱动器输入电压

    public HacMotionState() {
    }

    protected HacMotionState(Parcel in) {
        abndiasis = in.readString();
        measmode = in.readString();
        motorinfo = in.readString();
        measpoint = in.readString();
        waittime = in.readString();
        incvoltage = in.readString();
        driveinputv = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(abndiasis);
        dest.writeString(measmode);
        dest.writeString(motorinfo);
        dest.writeString(measpoint);
        dest.writeString(waittime);
        dest.writeString(incvoltage);
        dest.writeString(driveinputv);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<HacMotionState> CREATOR = new Creator<HacMotionState>() {
        @Override
        public HacMotionState createFromParcel(Parcel in) {
            return new HacMotionState(in);
        }

        @Override
        public HacMotionState[] newArray(int size) {
            return new HacMotionState[size];
        }
    };


    public String getAbndiasis() {
        return TextUtils.isEmpty(abndiasis) || abndiasis.equals("NullKey") ? "0" : abndiasis;
    }

    public void setAbndiasis(String abndiasis) {
        this.abndiasis = abndiasis;
    }

    public String getMeasmode() {
        return TextUtils.isEmpty(measmode) || measmode.equals("NullKey")  ? "0" : measmode;
    }

    public void setMeasmode(String measmode) {
        this.measmode = measmode;
    }

    public String getMotorinfo() {
        return TextUtils.isEmpty(motorinfo) ? "" : motorinfo;
    }

    public void setMotorinfo(String motorinfo) {
        this.motorinfo = motorinfo;
    }

    public String getMeaspoint() {
        return TextUtils.isEmpty(measpoint) ? "" : measpoint;
    }

    public void setMeaspoint(String measpoint) {
        this.measpoint = measpoint;
    }

    public String getWaittime() {
        return TextUtils.isEmpty(waittime) ? "0" : waittime;
    }

    public void setWaittime(String waittime) {
        this.waittime = waittime;
    }

    public String getIncvoltage() {
        return TextUtils.isEmpty(incvoltage) ? "" : incvoltage;
    }

    public void setIncvoltage(String incvoltage) {
        this.incvoltage = incvoltage;
    }

    public String getDriveinputv() {
        return TextUtils.isEmpty(driveinputv) ? "" : driveinputv;
    }

    public void setDriveinputv(String driveinputv) {
        this.driveinputv = driveinputv;
    }

    @Override
    public String toString() {
        return "HacMotionState{" +
                "abndiasis='" + abndiasis + '\'' +
                ", measmode='" + measmode + '\'' +
                ", motorinfo='" + motorinfo + '\'' +
                ", measpoint='" + measpoint + '\'' +
                ", waittime='" + waittime + '\'' +
                ", incvoltage='" + incvoltage + '\'' +
                ", driveinputv='" + driveinputv + '\'' +
                '}';
    }
}
