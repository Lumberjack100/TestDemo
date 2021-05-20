package com.shmedo.configlibrary.iot.model.e40;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/5/20 <br/>
 * 描述：     TODO
 */
public class CurrentExtendStateInfo {

    private BaseBean base;
    private StorageBean storage;
    private NetBean net;
    private SolarBean solar;
    private MemsBean mems;
    private List<SensorBean> sensor;
    private GnssBean gnss;

    public BaseBean getBase() {
        return base;
    }

    public void setBase(BaseBean base) {
        this.base = base;
    }

    public StorageBean getStorage() {
        return storage;
    }

    public void setStorage(StorageBean storage) {
        this.storage = storage;
    }

    public NetBean getNet() {
        return net;
    }

    public void setNet(NetBean net) {
        this.net = net;
    }

    public SolarBean getSolar() {
        return solar;
    }

    public void setSolar(SolarBean solar) {
        this.solar = solar;
    }

    public MemsBean getMems() {
        return mems;
    }

    public void setMems(MemsBean mems) {
        this.mems = mems;
    }

    public List<SensorBean> getSensor() {
        return sensor == null ? new ArrayList<SensorBean>() : sensor;
    }

    public void setSensor(List<SensorBean> sensor) {
        this.sensor = sensor;
    }

    public GnssBean getGnss() {
        return gnss;
    }

    public void setGnss(GnssBean gnss) {
        this.gnss = gnss;
    }
}
