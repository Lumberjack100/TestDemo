package com.shmedo.configlibrary.iot.model.das;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/15 <br/>
 * 描述：      DAS 状态页面数据中心状态
 */
public class DasNetStatusInfo {

    private int index;//中心编号
    private int errno;//错误码
    private int send;//已发送
    private int unsend;//未发生
    private float rate;//在线率

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

    public int getSend() {
        return send;
    }

    public void setSend(int send) {
        this.send = send;
    }

    public int getUnsend() {
        return unsend;
    }

    public void setUnsend(int unsend) {
        this.unsend = unsend;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }
}
