package com.shmedo.mcloudapp.network.api;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/5/26 <br/>
 * 描述：   访问的后台地址切换
 */
public enum ServiceAddressType {
    /**
     * http 访问
     */
    HTTP(1),

    /**
     * https 访问
     */
    HTTPS(2),


    HTTPS_NO_API_VERSION(3);

    private int type;
    ServiceAddressType(int type) {
        this.type = type;
    }

    public int toInt() {
        return type;
    }
}
