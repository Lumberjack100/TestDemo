package com.shmedo.mcloudapp.deviceconfig.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/2 <br/>
 * 描述：     设备当前状态信息
 */
public class DevcieCurrentState implements Parcelable {

    /**
     * ext_power_volt : 8.2
     * temp : 0.0
     * humidity : 0.0
     * temp_out : 0.0
     * humidity_out : 0.0
     * 4g_signal : -51
     * on_4g : true
     * bd_signal : 0
     * sw_version : 3.0.8
     * solar_volt : 0.0
     * battery_volt : 0.0
     * supply_power : 0.0
     * consume_power : 0.0
     * video_sun_volt : 0.0
     * video_battery_volt : 0.0
     * video_wind_power : 0.0
     */

    private double ext_power_volt;//外接电源电压
    private double temp;//设备内部环境温度，单位摄氏度
    private double humidity;//设备内部湿度，单位 RH%
    private double temp_out;//设备外部环境温度，单位摄氏度
    private double humidity_out;//设备外部环境湿度，单位RH%
    @SerializedName("4g_signal")
    private int _$4g_signal;//4g信号强度
    private boolean on_4g;
    private int bd_signal;//北斗信号强度
    private String sw_version;//固件版本号
    private double solar_volt;//太阳能板电压,单位V
    private double battery_volt;//蓄电池电压，单位V
    private double supply_power;//近12小时补充功率，单位千瓦时
    private double consume_power;//近12小时消耗功率，单位千瓦时
    private double video_sun_volt;
    private double video_battery_volt;
    private double video_wind_power;

    protected DevcieCurrentState(Parcel in) {
        ext_power_volt = in.readDouble();
        temp = in.readDouble();
        humidity = in.readDouble();
        temp_out = in.readDouble();
        humidity_out = in.readDouble();
        _$4g_signal = in.readInt();
        on_4g = in.readByte() != 0;
        bd_signal = in.readInt();
        sw_version = in.readString();
        solar_volt = in.readDouble();
        battery_volt = in.readDouble();
        supply_power = in.readDouble();
        consume_power = in.readDouble();
        video_sun_volt = in.readDouble();
        video_battery_volt = in.readDouble();
        video_wind_power = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(ext_power_volt);
        dest.writeDouble(temp);
        dest.writeDouble(humidity);
        dest.writeDouble(temp_out);
        dest.writeDouble(humidity_out);
        dest.writeInt(_$4g_signal);
        dest.writeByte((byte) (on_4g ? 1 : 0));
        dest.writeInt(bd_signal);
        dest.writeString(sw_version);
        dest.writeDouble(solar_volt);
        dest.writeDouble(battery_volt);
        dest.writeDouble(supply_power);
        dest.writeDouble(consume_power);
        dest.writeDouble(video_sun_volt);
        dest.writeDouble(video_battery_volt);
        dest.writeDouble(video_wind_power);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DevcieCurrentState> CREATOR = new Creator<DevcieCurrentState>() {
        @Override
        public DevcieCurrentState createFromParcel(Parcel in) {
            return new DevcieCurrentState(in);
        }

        @Override
        public DevcieCurrentState[] newArray(int size) {
            return new DevcieCurrentState[size];
        }
    };

    public double getExt_power_volt() {
        return ext_power_volt;
    }

    public void setExt_power_volt(double ext_power_volt) {
        this.ext_power_volt = ext_power_volt;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getTemp_out() {
        return temp_out;
    }

    public void setTemp_out(double temp_out) {
        this.temp_out = temp_out;
    }

    public double getHumidity_out() {
        return humidity_out;
    }

    public void setHumidity_out(double humidity_out) {
        this.humidity_out = humidity_out;
    }

    public int get_$4g_signal() {
        return _$4g_signal;
    }

    public void set_$4g_signal(int _$4g_signal) {
        this._$4g_signal = _$4g_signal;
    }

    public boolean isOn_4g() {
        return on_4g;
    }

    public void setOn_4g(boolean on_4g) {
        this.on_4g = on_4g;
    }

    public int getBd_signal() {
        return bd_signal;
    }

    public void setBd_signal(int bd_signal) {
        this.bd_signal = bd_signal;
    }

    public String getSw_version() {
        return sw_version;
    }

    public void setSw_version(String sw_version) {
        this.sw_version = sw_version;
    }

    public double getSolar_volt() {
        return solar_volt;
    }

    public void setSolar_volt(double solar_volt) {
        this.solar_volt = solar_volt;
    }

    public double getBattery_volt() {
        return battery_volt;
    }

    public void setBattery_volt(double battery_volt) {
        this.battery_volt = battery_volt;
    }

    public double getSupply_power() {
        return supply_power;
    }

    public void setSupply_power(double supply_power) {
        this.supply_power = supply_power;
    }

    public double getConsume_power() {
        return consume_power;
    }

    public void setConsume_power(double consume_power) {
        this.consume_power = consume_power;
    }

    public double getVideo_sun_volt() {
        return video_sun_volt;
    }

    public void setVideo_sun_volt(double video_sun_volt) {
        this.video_sun_volt = video_sun_volt;
    }

    public double getVideo_battery_volt() {
        return video_battery_volt;
    }

    public void setVideo_battery_volt(double video_battery_volt) {
        this.video_battery_volt = video_battery_volt;
    }

    public double getVideo_wind_power() {
        return video_wind_power;
    }

    public void setVideo_wind_power(double video_wind_power) {
        this.video_wind_power = video_wind_power;
    }
}
