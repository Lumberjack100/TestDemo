package com.shmedo.mcloudapp.deviceconfig.model.usb_serial;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/13 <br/>
 * 描述：    蓝牙测斜仪
 */
public class InclinometerMacInfo {
    private String no;//所在扫描列表序号
    private String addr;// MAC 地址
    private String rssi;// 信号强度

    private boolean isChecked = false;


    public InclinometerMacInfo() {
    }

    public InclinometerMacInfo(String no, String addr, String rssi) {
        this.no = no;
        this.addr = addr;
        this.rssi = rssi;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
