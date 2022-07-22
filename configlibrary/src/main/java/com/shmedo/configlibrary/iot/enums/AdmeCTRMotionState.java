package com.shmedo.configlibrary.iot.enums;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/18 <br/>
 * 描述：    设备测量过程状态枚举
 */
public enum AdmeCTRMotionState {
    NOZZLE_WAITING("0", "上拉至管口", "上拉至管口"),

    PAIR_SETTING_PARAM("1", "测斜仪配对,设置参数", "配对,设置参数"),

    DOWN("2", "测斜仪下放", "下放"),

    BOTTOM_WAITING("3", "管底等待", "管底等待"),

    POINT_MEASUREMENT("4", "测点测量", "测点测量"),

    MEASUREMENT_OVER("5", "磁开关触发,测量结束", "测量结束"),

    READ_DATA("6", "测斜仪配对,读取数据", "读取数据"),

    UPLOAD_DATA("7", "数据上传", "数据上传"),

    WAITING_NEXT_TESTING("8", "数据上传完成等待下次测量", "等待下次测量"),

    WAITING_BACK_TESTING("9", "等待反测", "等待反测"),

    UNKNOWN_ERROR("-100", "未知", "未知");


    AdmeCTRMotionState(String code, String description, String simpleInfo) {
        this.code = code;
        this.description = description;
        this.simpleInfo = simpleInfo;
    }

    private String code;
    private String description;
    private String simpleInfo;

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getSimpleInfo() {
        return simpleInfo;
    }

    public static AdmeCTRMotionState valueByCode(String code) {
        if (TextUtils.isEmpty(code))
            return UNKNOWN_ERROR;

        for (AdmeCTRMotionState motionState : AdmeCTRMotionState.values()) {
            if (motionState.getCode().equals(code))
                return motionState;
        }
        return UNKNOWN_ERROR;
    }
}
