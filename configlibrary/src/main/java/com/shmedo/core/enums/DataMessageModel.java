package com.shmedo.core.enums;

/**
 * Created by adu on 2017/12/15.
 */
public enum DataMessageModel {

    /**
     * GPRS模式
     */
    GPRS_MODEL(1),

    /**
     * 短信息模式
     */
    MESSAGE_MODEL(2),

    /**
     * 北斗短报文模式
     */
    BD_NOTICE_MODEL(3);

    private int model;
    DataMessageModel(int model) {
        this.model = model;
    }
    public static DataMessageModel valueOf(int model) {
        switch (model) {
            case 1: return GPRS_MODEL;
            case 2: return MESSAGE_MODEL;
            case 3: return BD_NOTICE_MODEL;
            default: return GPRS_MODEL;
        }
    }
}
