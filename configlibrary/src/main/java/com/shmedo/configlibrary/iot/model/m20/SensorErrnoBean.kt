package com.shmedo.configlibrary.iot.model.m20;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/19/21 <br/>
 * 描述：     TODO #gh#
 */
public class SensorErrnoBean {
    /**
     * errno : 0
     * sensor_id : 203_1
     */

    private Integer errno;
    private String sensor_id;

    public Integer getErrno() {
        return errno;
    }

    public void setErrno(Integer errno) {
        this.errno = errno;
    }

    public String getSensor_id() {
        return sensor_id;
    }

    public void setSensor_id(String sensor_id) {
        this.sensor_id = sensor_id;
    }
}
