package com.shmedo.mcloudapp.network.api;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/5/26 <br/>
 * 描述：   访问的后台地址切换
 */
public enum ServiceAddressType {

    /**
     * 物联网权限服务地址
     */
    AUTHORITY_SERVICE_ADDRESS(1),

    /**
     * 物联网设备管理服务地址
     */
    IOT_MANAGER_SERVICE_ADDRESS(2),

    /**
     * 物联网指令交互服务地址
     */
    IOT_INTERACTIVE_SERVICE_ADDRESS(3),

    /**
     * 云平台原始数据地址
     */
    CLOUD_PLATFORM_DATA_ADDRESS(4);

    private int type;

    ServiceAddressType(int type) {
        this.type = type;
    }

    public int toInt() {
        return type;
    }
}
