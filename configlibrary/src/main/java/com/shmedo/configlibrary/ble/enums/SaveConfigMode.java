package com.shmedo.configlibrary.ble.enums;

/**
 * 项目名：  mCloudapp <br/>
 * 包名：    com.shmedo.core.enums <br/>
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/4/23 <br/>
 * 描述：    保存设备配置参数方式
 */
public enum SaveConfigMode {

    //保存并重启
    SAVE_REBOOT(1),

    //保存
    SAVE_NO_REBOOT(2);

    private int model;
    SaveConfigMode(int model) {
        this.model = model;
    }

    public int toInt() {
        return model;
    }

    public static SaveConfigMode valueOf(int model) {
        switch (model) {
            case 2:
                return SAVE_NO_REBOOT;

            default:
                return SAVE_REBOOT;
        }
    }
}
