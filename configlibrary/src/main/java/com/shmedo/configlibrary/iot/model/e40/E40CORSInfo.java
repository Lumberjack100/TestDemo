package com.shmedo.configlibrary.iot.model.e40;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2/26/21 <br/>
 * 描述：     E40 CORS 服务参数信息
 */
public class E40CORSInfo {
    private String sw;//0：表示关闭，1：表示打开
    private String addr;//服务器地址
    private String port;//服务器端口号
    private String user;//用户名
    private String pswd;//密码
    private String sta;//站点名

    public String getSw() {
        return sw;
    }

    public void setSw(String sw) {
        this.sw = sw;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPswd() {
        return pswd;
    }

    public void setPswd(String pswd) {
        this.pswd = pswd;
    }

    public String getSta() {
        return sta;
    }

    public void setSta(String sta) {
        this.sta = sta;
    }
}
