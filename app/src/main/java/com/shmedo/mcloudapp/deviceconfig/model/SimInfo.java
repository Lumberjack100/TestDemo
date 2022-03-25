package com.shmedo.mcloudapp.deviceconfig.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/3/16 <br/>
 * 描述：     物联网流量卡信息
 */
public class SimInfo {
    private int id;//物联网卡编号
    private String ccid;//物联网卡CCID
    private String simNo;//物联网卡卡号
    private boolean pool;//流量池卡或者单卡
    private String simStatus;//物联网卡状态
    private long totalFlow;//总流量，单位字节
    private long useFlow;//已使用流量，单位字节
    private long remaindFlow;//剩余流量，单位字节
    private String simIsp;//物联网卡运营商
    private String vendor;//物联网供应商

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCcid() {
        return ccid;
    }

    public void setCcid(String ccid) {
        this.ccid = ccid;
    }

    public String getSimNo() {
        return simNo;
    }

    public void setSimNo(String simNo) {
        this.simNo = simNo;
    }

    public boolean isPool() {
        return pool;
    }

    public void setPool(boolean pool) {
        this.pool = pool;
    }

    public String getSimStatus() {
        return simStatus;
    }

    public void setSimStatus(String simStatus) {
        this.simStatus = simStatus;
    }

    public long getTotalFlow() {
        return totalFlow;
    }

    public void setTotalFlow(long totalFlow) {
        this.totalFlow = totalFlow;
    }

    public long getUseFlow() {
        return useFlow;
    }

    public void setUseFlow(long useFlow) {
        this.useFlow = useFlow;
    }

    public long getRemaindFlow() {
        return remaindFlow;
    }

    public void setRemaindFlow(long remaindFlow) {
        this.remaindFlow = remaindFlow;
    }

    public String getSimIsp() {
        return simIsp;
    }

    public void setSimIsp(String simIsp) {
        this.simIsp = simIsp;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
}
