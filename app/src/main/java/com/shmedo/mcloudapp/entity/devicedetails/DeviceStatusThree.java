package com.shmedo.mcloudapp.entity.devicedetails;

import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.devicedetails
 * 创建者:   dpc
 * 创建时间:  2019-12-11
 * 描述：    设备状态3
 */
public class DeviceStatusThree {
    /**
     * $$043,(1),(2),(3),(4)
     *  (1) sn号
     * （2）采集器型号
     * （3）传感器状态，用冒号分隔的字符串
     * ①:②:③，其中①：传感器地址，②：传感器状态，0正常，1异常，③：传感器数据
     * （4）传感器状态，和（2）格式相同，
     * 注：传感器状态可能有很多个，有接入传感器个数决定。
     * 示例：
     * $$043,150000L,2,  3:0:3.1,   5:0:3.1\r\n
     */
    private String snNumber;
    private String collectorModel;
    private List<String> sensorStatus;

    public String getSnNumber() {
        return snNumber;
    }

    public void setSnNumber(String snNumber) {
        this.snNumber = snNumber;
    }

    public String getCollectorModel() {
        return collectorModel;
    }

    public void setCollectorModel(String collectorModel) {
        this.collectorModel = collectorModel;
    }

    public List<String> getSensorStatus() {
        return sensorStatus;
    }

    public void setSensorStatus(List<String> sensorStatus) {
        this.sensorStatus = sensorStatus;
    }

    @Override
    public String toString() {
        return "DeviceStatusThree{" +
                "collectorModel='" + collectorModel + '\'' +
                ", sensorStatus=" + sensorStatus +
                '}';
    }
}
