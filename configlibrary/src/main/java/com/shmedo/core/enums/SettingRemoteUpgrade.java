package com.shmedo.core.enums;

/**
 * Created by adu on 2017/12/18.
 * 设置远程升级
 */
public enum  SettingRemoteUpgrade {
    OPEN_UPGRADE_MODEL(0),

    CLOSE_UPGRADE_MODEL(1);

    private int model;

    SettingRemoteUpgrade(int model) {
        this.model = model;
    }

   public static SettingRemoteUpgrade valueOf(int model) {
        switch (model) {
            case 0: return OPEN_UPGRADE_MODEL;
            case 1: return CLOSE_UPGRADE_MODEL;
            default:return OPEN_UPGRADE_MODEL;
        }
   }
}
