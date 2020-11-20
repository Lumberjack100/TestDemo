package com.shmedo.mcloudapp.deviceconfig.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.shmedo.configlibrary.iot.model.SensorErrnoBean;

import java.util.List;


/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/2 <br/>
 * 描述：     设备当前状态信息
 */
public class DevcieCurrentState implements Parcelable {

    /**
     * ext_power_volt : 23.72
     * inner_power_volt : 0.0
     * temp : -50.0
     * humidity : 0.0
     * temp_out : 21.1
     * humidity_out : 0.0
     * 4g_signal : 25
     * bd_signal : 0.0
     * nb_signal : 0.0
     * sw_version : 1.1.16
     * sensor_errno : [{"errno":0,"sensor_id":3}]
     * location : 0.0E.0.0N
     * solar_volt : 0.0
     * battery_volt : 3.5
     * supply_power : 0.0
     * consume_power : 0.0
     * work_current : 0.0
     */

    private double ext_power_volt;//外接电源电压
    private double inner_power_volt;//内部电源电压
    private double temp;//设备内部环境温度，单位摄氏度
    private double humidity;//设备内部湿度，单位 RH%
    private double temp_out;//设备外部环境温度，单位摄氏度
    private double humidity_out;//设备外部环境湿度，单位RH%
    @SerializedName("4g_signal")
    private int _$4g_signal;//4g信号强度
    private boolean on_4g;
    private double bd_signal;//北斗信号强度
    private double nb_signal;//窄带信号强度
    private String sw_version;//固件版本号
    private String location;//设备位置-经纬度，经度在前,纬度在后。E表示东经，W表示西经，N表示北纬，S表示南纬。
    private double solar_volt;//太阳能板电压,单位V
    private double battery_volt;//蓄电池电压，单位V
    private double supply_power;//近12小时补充功率，单位千瓦时
    private double consume_power;//近12小时消耗功率，单位千瓦时
    private double work_current;//设备工作电流，单位A
    private double video_sun_volt;
    private double video_battery_volt;
    private double video_wind_power;
    private List<SensorErrnoBean> sensor_errno;//传感器错误码，


    private String IMEI;
    private String CCID;
    private String uptime;
    @SerializedName("1minload")
    private String _$1minload;
    private String freeram;
    private String memunit;
    private String procs;
    private String poweroffcount;
    private String socket1;
    private String socket1stat;
    private String socket2;
    private String socket2stat;
    private String socket3;
    private String socket3stat;
    private String socket4;
    private String socket4stat;
    private String socketRes;
    private String socketResstat;
    private String gnsscom1sfdog;
    private String gnsscom2sfdog;
    private String gnsscom3sfdog;

    protected DevcieCurrentState(Parcel in) {
        ext_power_volt = in.readDouble();
        inner_power_volt = in.readDouble();
        temp = in.readDouble();
        humidity = in.readDouble();
        temp_out = in.readDouble();
        humidity_out = in.readDouble();
        _$4g_signal = in.readInt();
        on_4g = in.readByte() != 0;
        bd_signal = in.readDouble();
        nb_signal = in.readDouble();
        sw_version = in.readString();
        location = in.readString();
        solar_volt = in.readDouble();
        battery_volt = in.readDouble();
        supply_power = in.readDouble();
        consume_power = in.readDouble();
        work_current = in.readDouble();
        video_sun_volt = in.readDouble();
        video_battery_volt = in.readDouble();
        video_wind_power = in.readDouble();
        sensor_errno = in.createTypedArrayList(SensorErrnoBean.CREATOR);
        IMEI = in.readString();
        CCID = in.readString();
        uptime = in.readString();
        _$1minload = in.readString();
        freeram = in.readString();
        memunit = in.readString();
        procs = in.readString();
        poweroffcount = in.readString();
        socket1 = in.readString();
        socket1stat = in.readString();
        socket2 = in.readString();
        socket2stat = in.readString();
        socket3 = in.readString();
        socket3stat = in.readString();
        socket4 = in.readString();
        socket4stat = in.readString();
        socketRes = in.readString();
        socketResstat = in.readString();
        gnsscom1sfdog = in.readString();
        gnsscom2sfdog = in.readString();
        gnsscom3sfdog = in.readString();
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

    public double getInner_power_volt() {
        return inner_power_volt;
    }

    public void setInner_power_volt(double inner_power_volt) {
        this.inner_power_volt = inner_power_volt;
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

    public double getBd_signal() {
        return bd_signal;
    }

    public void setBd_signal(double bd_signal) {
        this.bd_signal = bd_signal;
    }

    public double getNb_signal() {
        return nb_signal;
    }

    public void setNb_signal(double nb_signal) {
        this.nb_signal = nb_signal;
    }

    public String getSw_version() {
        return sw_version;
    }

    public void setSw_version(String sw_version) {
        this.sw_version = sw_version;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public double getWork_current() {
        return work_current;
    }

    public void setWork_current(double work_current) {
        this.work_current = work_current;
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

    public List<SensorErrnoBean> getSensor_errno() {
        return sensor_errno;
    }

    public void setSensor_errno(List<SensorErrnoBean> sensor_errno) {
        this.sensor_errno = sensor_errno;
    }

    public String getIMEI() {
        return IMEI;
    }

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }

    public String getCCID() {
        return CCID;
    }

    public void setCCID(String CCID) {
        this.CCID = CCID;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public String get_$1minload() {
        return _$1minload;
    }

    public void set_$1minload(String _$1minload) {
        this._$1minload = _$1minload;
    }

    public String getFreeram() {
        return freeram;
    }

    public void setFreeram(String freeram) {
        this.freeram = freeram;
    }

    public String getMemunit() {
        return memunit;
    }

    public void setMemunit(String memunit) {
        this.memunit = memunit;
    }

    public String getProcs() {
        return procs;
    }

    public void setProcs(String procs) {
        this.procs = procs;
    }

    public String getPoweroffcount() {
        return poweroffcount;
    }

    public void setPoweroffcount(String poweroffcount) {
        this.poweroffcount = poweroffcount;
    }

    public String getSocket1() {
        return socket1;
    }

    public void setSocket1(String socket1) {
        this.socket1 = socket1;
    }

    public String getSocket1stat() {
        return socket1stat;
    }

    public void setSocket1stat(String socket1stat) {
        this.socket1stat = socket1stat;
    }

    public String getSocket2() {
        return socket2;
    }

    public void setSocket2(String socket2) {
        this.socket2 = socket2;
    }

    public String getSocket2stat() {
        return socket2stat;
    }

    public void setSocket2stat(String socket2stat) {
        this.socket2stat = socket2stat;
    }

    public String getSocket3() {
        return socket3;
    }

    public void setSocket3(String socket3) {
        this.socket3 = socket3;
    }

    public String getSocket3stat() {
        return socket3stat;
    }

    public void setSocket3stat(String socket3stat) {
        this.socket3stat = socket3stat;
    }

    public String getSocket4() {
        return socket4;
    }

    public void setSocket4(String socket4) {
        this.socket4 = socket4;
    }

    public String getSocket4stat() {
        return socket4stat;
    }

    public void setSocket4stat(String socket4stat) {
        this.socket4stat = socket4stat;
    }

    public String getSocketRes() {
        return socketRes;
    }

    public void setSocketRes(String socketRes) {
        this.socketRes = socketRes;
    }

    public String getSocketResstat() {
        return socketResstat;
    }

    public void setSocketResstat(String socketResstat) {
        this.socketResstat = socketResstat;
    }

    public String getGnsscom1sfdog() {
        return gnsscom1sfdog;
    }

    public void setGnsscom1sfdog(String gnsscom1sfdog) {
        this.gnsscom1sfdog = gnsscom1sfdog;
    }

    public String getGnsscom2sfdog() {
        return gnsscom2sfdog;
    }

    public void setGnsscom2sfdog(String gnsscom2sfdog) {
        this.gnsscom2sfdog = gnsscom2sfdog;
    }

    public String getGnsscom3sfdog() {
        return gnsscom3sfdog;
    }

    public void setGnsscom3sfdog(String gnsscom3sfdog) {
        this.gnsscom3sfdog = gnsscom3sfdog;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(ext_power_volt);
        dest.writeDouble(inner_power_volt);
        dest.writeDouble(temp);
        dest.writeDouble(humidity);
        dest.writeDouble(temp_out);
        dest.writeDouble(humidity_out);
        dest.writeInt(_$4g_signal);
        dest.writeByte((byte) (on_4g ? 1 : 0));
        dest.writeDouble(bd_signal);
        dest.writeDouble(nb_signal);
        dest.writeString(sw_version);
        dest.writeString(location);
        dest.writeDouble(solar_volt);
        dest.writeDouble(battery_volt);
        dest.writeDouble(supply_power);
        dest.writeDouble(consume_power);
        dest.writeDouble(work_current);
        dest.writeDouble(video_sun_volt);
        dest.writeDouble(video_battery_volt);
        dest.writeDouble(video_wind_power);
        dest.writeTypedList(sensor_errno);
        dest.writeString(IMEI);
        dest.writeString(CCID);
        dest.writeString(uptime);
        dest.writeString(_$1minload);
        dest.writeString(freeram);
        dest.writeString(memunit);
        dest.writeString(procs);
        dest.writeString(poweroffcount);
        dest.writeString(socket1);
        dest.writeString(socket1stat);
        dest.writeString(socket2);
        dest.writeString(socket2stat);
        dest.writeString(socket3);
        dest.writeString(socket3stat);
        dest.writeString(socket4);
        dest.writeString(socket4stat);
        dest.writeString(socketRes);
        dest.writeString(socketResstat);
        dest.writeString(gnsscom1sfdog);
        dest.writeString(gnsscom2sfdog);
        dest.writeString(gnsscom3sfdog);
    }
}
