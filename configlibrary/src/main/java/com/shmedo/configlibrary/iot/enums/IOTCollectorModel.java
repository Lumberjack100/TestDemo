package com.shmedo.configlibrary.iot.enums;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/7/16 <br/>
 * 描述：     TODO
 */
public enum IOTCollectorModel {
    /**
     * 振弦式采集器
     */
    VW08("0"),
    /**
     * 雨量采集器
     */
    RAIN08("1"),
    /**
     * 裂缝计采集器
     */
    DS08("2"),
    /**
     * 土壤湿度采集器
     */
    HD08("3"),
    /**
     * 测斜仪采集器
     */
    CX08("4"),
    /**
     * 超声波采集器
     */
    UDS08("6"),
    /**
     * 雷达采集器
     */
    RD08("7"),
    /**
     * 墒情采集器
     */
    SMC08("8"),
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
    CS08("21"),
    /**
     * 气象站
     */
    QXZ("25");

    private String model;

    IOTCollectorModel(String model) {
        this.model = model;
    }


    @Override
    public String toString() {
        return this.model;
    }

    public static IOTCollectorModel value(String model) {
        switch (model) {
            case "0":
                return VW08;
            case "1":
                return RAIN08;
            case "2":
                return DS08;
            case "3":
                return HD08;
            case "4":
                return CX08;
            case "6":
                return UDS08;
            case "7":
                return RD08;
            case "8":
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
            case "25":
                return QXZ;
            default:
                return VW08;
        }
    }

    public static boolean isValidCollector(String coll) {
        if (TextUtils.isEmpty(coll))
            return false;

        List<String> allCollectors = new ArrayList<>();
        for (IOTCollectorModel modle : IOTCollectorModel.values()) {
            allCollectors.add(modle.toString());
        }

        return allCollectors.contains(coll);
    }
}
