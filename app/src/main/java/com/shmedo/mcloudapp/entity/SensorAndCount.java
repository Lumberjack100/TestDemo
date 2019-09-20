package com.shmedo.mcloudapp.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity
 * 文件名:   SensorAndCount
 * 创建者:   dpc
 * 创建时间:  2019/8/15 16:57
 * 描述：    传感器数量
 */
@Entity
public class SensorAndCount implements Serializable {
    private static final long serialVersionUID = -2558078052690221888L;
    
    @Id(autoincrement = true)
    private Long id;
    private Long ownerId;
    private int sensorType;//传感器类型
    private int sensorCount;//传感器数量


    @Generated(hash = 163766939)
    public SensorAndCount(Long id, Long ownerId, int sensorType, int sensorCount) {
        this.id = id;
        this.ownerId = ownerId;
        this.sensorType = sensorType;
        this.sensorCount = sensorCount;
    }


    @Generated(hash = 384146275)
    public SensorAndCount() {
    }


    public int getSensorType() {
        return sensorType;
    }


    public void setSensorType(int sensorType) {
        this.sensorType = sensorType;
    }


    public int getSensorCount() {
        return sensorCount;
    }


    public void setSensorCount(int sensorCount) {
        this.sensorCount = sensorCount;
    }


    @Override public String toString() {
        return "SensorAndCount{" +
            "sensorType=" + sensorType +
            ", sensorCount=" + sensorCount +
            '}';
    }


    public Long getId() {
        return this.id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public Long getOwnerId() {
        return this.ownerId;
    }


    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
}
