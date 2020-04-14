package com.shmedo.core.model;

import com.shmedo.core.enums.CollectorModel;
import com.shmedo.core.enums.DataCommunicateMode;
import com.shmedo.core.enums.DataEncryption;
import com.shmedo.core.enums.DebugModel;
import com.shmedo.core.enums.EquipmentStatus;
import com.shmedo.core.enums.RainfallStation;
import com.shmedo.core.enums.SIMChoose;
import com.shmedo.core.enums.SensorInterfaceType;

/**
 * Created by adu on 2017/12/11.
 * 获取基础配置信息
 */
public class BaseConfigInfo {
    private String token;  //设备识别码
    private String localBGNum;      //本地北斗卡号
    private String targetBGNum;     //目标北斗卡号
    private EquipmentStatus equipmentStatus;    //设备状态
    private DataCommunicateMode dataCommunicateMode;     //数据通讯模式
    private RainfallStation rainfallStation;    //雨量站
    private int rainAccuracy;     //雨量计精度
    private int locationSensitivity;  //定位灵敏度
    private int locationAccuracy;     //定位精度
    private int heartbeatTimeInterval;      //心跳包时间间隔（单位s，为0表示关闭心跳功能）
    private long debugBandRate;     //调试口波特率
    private long sensorBandRate;    //传感器波特率
    private CollectorModel collectorModel;  //采集器型号
    private int dataReportInterval; //数据上报间隔
    private String batteryOverProtect;  //电池过放保护
    private DebugModel debugModel;      //调试模式
    private SensorInterfaceType sensorInterfaceType;    //传感器接口类型
    private com.shmedo.core.enums.DataEncryption DataEncryption;      //数据加密
    private com.shmedo.core.enums.SIMChoose SIMChoose;        //SIM选择

    public BaseConfigInfo() {
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    public String getLocalBGNum() {
        return localBGNum;
    }

    public void setLocalBGNum(String localBGNum) {
        this.localBGNum = localBGNum;
    }


    public String getTargetBGNum() {
        return targetBGNum;
    }


    public void setTargetBGNum(String targetBGNum) {
        this.targetBGNum = targetBGNum;
    }


    public EquipmentStatus getEquipmentStatus() {
        return equipmentStatus;
    }


    public void setEquipmentStatus(EquipmentStatus equipmentStatus) {
        this.equipmentStatus = equipmentStatus;
    }
    public DataCommunicateMode getDataCommunicateMode() {
        return dataCommunicateMode;
    }
    public void setDataCommunicateMode(DataCommunicateMode dataCommunicateMode) {
        this.dataCommunicateMode = dataCommunicateMode;
    }
    public RainfallStation getRainfallStation() {
        return rainfallStation;
    }
    public void setRainfallStation(RainfallStation rainfallStation) {
        this.rainfallStation = rainfallStation;
    }
    public int getRainAccuracy() {
        return rainAccuracy;
    }
    public void setRainAccuracy(int rainAccuracy) {
        this.rainAccuracy = rainAccuracy;
    }

    public int getLocationSensitivity() {
        return locationSensitivity;
    }

    public void setLocationSensitivity(int locationSensitivity) {
        this.locationSensitivity = locationSensitivity;
    }
    public int getLocationAccuracy() {
        return locationAccuracy;
    }

    public void setLocationAccuracy(int locationAccuracy) {
        this.locationAccuracy = locationAccuracy;
    }

    public int getHeartbeatTimeInterval() {
        return heartbeatTimeInterval;
    }

    public void setHeartbeatTimeInterval(int heartbeatTimeInterval) {
        this.heartbeatTimeInterval = heartbeatTimeInterval;
    }

    public long getDebugBandRate() {
        return debugBandRate;
    }

    public void setDebugBandRate(long debugBandRate) {
        this.debugBandRate = debugBandRate;
    }

    public long getSensorBandRate() {
        return sensorBandRate;
    }

    public void setSensorBandRate(long sensorBandRate) {
        this.sensorBandRate = sensorBandRate;
    }

    public CollectorModel getCollectorModel() {
        return collectorModel;
    }

    public void setCollectorModel(CollectorModel collectorModel) {
        this.collectorModel = collectorModel;
    }

    public int getDataReportInterval() {
        return dataReportInterval;
    }

    public void setDataReportInterval(int dataReportInterval) {
        this.dataReportInterval = dataReportInterval;
    }

    public String getBatteryOverProtect() {
        return batteryOverProtect;
    }

    public void setBatteryOverProtect(String batteryOverProtect) {
        this.batteryOverProtect = batteryOverProtect;
    }

    public DebugModel getDebugModel() {
        return debugModel;
    }

    public void setDebugModel(DebugModel debugModel) {
        this.debugModel = debugModel;
    }

    public SensorInterfaceType getSensorInterfaceType() {
        return sensorInterfaceType;
    }

    public void setSensorInterfaceType(SensorInterfaceType sensorInterfaceType) {
        this.sensorInterfaceType = sensorInterfaceType;
    }

    public DataEncryption getDataEncryption() {
        return DataEncryption;
    }

    public void setDataEncryption(DataEncryption dataEncryption) {
        DataEncryption = dataEncryption;
    }

    public SIMChoose getSIMChoose() {
        return SIMChoose;
    }

    public void setSIMChoose(SIMChoose SIMChoose) {
        this.SIMChoose = SIMChoose;
    }

    @Override
    public String toString() {
        return "BaseConfigInfo{" +
                "token='" + token + '\'' +
                ", localBGNum='" + localBGNum + '\'' +
                ", targetBGNum='" + targetBGNum + '\'' +
                ", equipmentStatus=" + equipmentStatus +
                ", dataCommunicateMode=" + dataCommunicateMode +
                ", rainfallStation=" + rainfallStation +
                ", rainAccuracy=" + rainAccuracy +
                ", locationSensitivity=" + locationSensitivity +
                ", locationAccuracy=" + locationAccuracy +
                ", heartbeatTimeInterval=" + heartbeatTimeInterval +
                ", debugBandRate=" + debugBandRate +
                ", sensorBandRate=" + sensorBandRate +
                ", collectorModel=" + collectorModel +
                ", dataReportInterval=" + dataReportInterval +
                ", batteryOverProtect='" + batteryOverProtect + '\'' +
                ", debugModel=" + debugModel +
                ", sensorInterfaceType=" + sensorInterfaceType +
                ", DataEncryption=" + DataEncryption +
                ", SIMChoose=" + SIMChoose +
                '}';
    }
}
