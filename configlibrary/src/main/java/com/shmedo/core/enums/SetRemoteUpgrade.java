package com.shmedo.core.enums;

/**
 * Created by adu on 2017/12/18.
 * 设置远程升级
 */
public enum SetRemoteUpgrade {
    /**
     * 关闭升级模式
     */
    CLOSE_UPGRADE_MODEL(0),

    /**
     * 打开升级模式，Y…Y为升级服务器端口号（最长支持5位数字）
     */
    OPEN_UPGRADE_MODEL1(1),

    /**
     * 打开升级模式，Y…Y为服务器地址和端口，用空格分割
     */
    OPEN_UPGRADE_MODEL2(1);

    private int model;
    SetRemoteUpgrade(int model) {
        this.model = model;
    }

    public int toInt() {
        return model;
    }

    public static SetRemoteUpgrade valueOf(int model) {
        switch (model) {
            case 0:
                return CLOSE_UPGRADE_MODEL;

            case 1:
                return OPEN_UPGRADE_MODEL1;

            case 2:
                return OPEN_UPGRADE_MODEL2;

            default:
                return CLOSE_UPGRADE_MODEL;
        }
    }
}
