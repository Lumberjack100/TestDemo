package com.shmedo.configlibrary.ble.enums;


import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adu on 2017/12/11.
 * 采集器型号
 */
public enum CollectorModel {
    /**
     *
     */
    VW08("00"),
    /**
     * 裂缝计采集器
     */
    DS08("02"),
    /**
     * 土壤湿度采集器
     */
    HD08("03"),
    /**
     * 测斜仪采集器
     */
    CX08("04"),
    /**
     * 超声波采集器
     */
    UDS08("06"),
    /**
     * 雷达采集器
     */
    RD08("07"),
    /**
     * 墒情采集器
     */
    SMC08("08"),
    /**
     * 温湿度采集器
     */
    TH08("12"),
    /**
     * 数字式渗压计采集器
     */
    DVWP("15"),
    /**
     * 倾角仪采集器
     */
    QJY08("16"),
    /**
     * 单通道采集器
     */
    VW01("20"),
    /**
     * 次声采集器
     */
    CS08("21");

    private String model;

    CollectorModel(String model) {
        this.model = model;
    }


    @Override
    public String toString() {
        return this.model;
    }

    public static CollectorModel value(String model) {
        switch (model) {
            case "00":
                return VW08;
            case "02":
                return DS08;
            case "03":
                return HD08;
            case "04":
                return CX08;
            case "06":
                return UDS08;
            case "07":
                return RD08;
            case "08":
                return SMC08;
            case "12":
                return TH08;
            case "15":
                return DVWP;
            case "16":
                return QJY08;
            case "20":
                return VW01;
            case "21":
                return CS08;
            default:
                return VW08;
        }
    }

    public static boolean isValidCollector(String coll) {
        if (TextUtils.isEmpty(coll))
            return false;

        List<String> allCollectors = new ArrayList<>();
        for (CollectorModel modle : CollectorModel.values()) {
            allCollectors.add(modle.toString());
        }

        return allCollectors.contains(coll);
    }
}
