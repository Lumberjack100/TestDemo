package com.shmedo.configlibrary.iot.enums;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/11/4 <br/>
 * 描述：    传感器异常信息
 */
public enum SensorErrorType {
    NORMAL("0", "正常"),

    ABNORMAL_POWER_SUPPLY("-1", "供电异常"),

    ABNORMAL_DATA("-2", "数据异常"),

    NO_DATA_COLLECTED("-3", "未采集到数据"),

    NOT_CONNECTED("-4", "未接入"),

    SHORT_CIRCUIT("-5", "短路"),

    POOR_CONTACT("-6", "接触不良"),

    UNKNOWN_ERROR("-100", "未知错误");

    SensorErrorType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    private String code;
    private String description;

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code;
    }

    /**
     * 根据传感器错误码返回对应的错误信息
     *
     * @param errorCode
     * @return
     */
    public static String getErrorMessageByCode(String errorCode) {
        if (TextUtils.isEmpty(errorCode))
            return UNKNOWN_ERROR.getDescription();

        for (SensorErrorType sensorErrorType : SensorErrorType.values()) {
            if (sensorErrorType.getCode().equals(errorCode))
                return sensorErrorType.getDescription();
        }

        return UNKNOWN_ERROR.getDescription();
    }
}
