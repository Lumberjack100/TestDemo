package com.shmedo.configlibrary.iot.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/13 <br/>
 * 描述：    传感器错误码实体
 */
public class SensorErrnoBean {
    /**
     * sensoraddr : 0
     * sensortype : 58
     * errno : -4
     * sensorval : 0
     */

    private int sensoraddr;
    private int sensortype;
    private int errno;//传感器错误码
    private int sensorval;

    public int getSensoraddr() {
        return sensoraddr;
    }

    public void setSensoraddr(int sensoraddr) {
        this.sensoraddr = sensoraddr;
    }

    public int getSensortype() {
        return sensortype;
    }

    public void setSensortype(int sensortype) {
        this.sensortype = sensortype;
    }

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

    public int getSensorval() {
        return sensorval;
    }

    public void setSensorval(int sensorval) {
        this.sensorval = sensorval;
    }
}
