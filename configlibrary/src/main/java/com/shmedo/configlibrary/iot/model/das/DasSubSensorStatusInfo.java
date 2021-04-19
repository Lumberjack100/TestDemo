package com.shmedo.configlibrary.iot.model.das;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/15 <br/>
 * 描述：      DAS 状态页面辅传感器状态
 */
public class DasSubSensorStatusInfo {
    private IoBean io;
    private VwpBean vwp;
    private MemsBean mems;

    public IoBean getIo() {
        return io;
    }

    public void setIo(IoBean io) {
        this.io = io;
    }

    public VwpBean getVwp() {
        return vwp;
    }

    public void setVwp(VwpBean vwp) {
        this.vwp = vwp;
    }

    public MemsBean getMems() {
        return mems;
    }

    public void setMems(MemsBean mems) {
        this.mems = mems;
    }
}
