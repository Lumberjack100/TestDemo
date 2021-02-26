package com.shmedo.configlibrary.iot.cmd.entity.e40;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2/26/21 <br/>
 * 描述：     生成 CORS 参数拼接指令
 */
public class E40CORSEntity implements Validater {
    private String sw;//0：表示关闭，1：表示打开
    private String addr;//服务器地址
    private String port;//服务器端口号
    private String user;//用户名
    private String pswd;//密码
    private String sta;//站点名

    public void setSw(String sw) {
        this.sw = sw;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPswd(String pswd) {
        this.pswd = pswd;
    }

    public void setSta(String sta) {
        this.sta = sta;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("sw=" + sw);
        stringBuilder.append("&");
        if (!TextUtils.isEmpty(addr)) {
            stringBuilder.append("addr=" + addr);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(port)) {
            stringBuilder.append("port=" + port);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(user)) {
            stringBuilder.append("user=" + user);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(pswd)) {
            stringBuilder.append("pswd=" + pswd);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(sta)) {
            stringBuilder.append("sta=" + sta);
            stringBuilder.append("&");
        }

        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }

        return stringBuilder.toString();
    }
}
