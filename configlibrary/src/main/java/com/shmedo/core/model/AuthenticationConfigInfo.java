package com.shmedo.core.model;

/**
 * Created by adu on 2018/1/10.
 * 认证请求配置，发送验证码
 */
public class AuthenticationConfigInfo {
    private String snNumber;
    private String key;

    public String getSnNumber() {
        return snNumber;
    }

    public void setSnNumber(String snNumber) {
        this.snNumber = snNumber;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "AuthenticationConfigInfo{" +
                "snNumber='" + snNumber + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
