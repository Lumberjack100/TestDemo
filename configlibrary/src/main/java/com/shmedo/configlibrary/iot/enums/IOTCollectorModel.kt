package com.shmedo.configlibrary.iot.enums;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/7/16 <br/>
 * 描述：     TODO
 */
@Deprecated
public enum IOTCollectorModel {
    /**
     * 振弦式采集器
     */
    VW08("0","振弦式采集器"),

    /**
     * 雨量采集器
     */
    RAIN08("1","雨量计"),

    /**
     * 裂缝计采集器
     */
    DS08("2","裂缝计"),

    /**
     * 土壤湿度采集器
     */
    HD08("3","管式含水率计"),

    /**
     * 测斜仪采集器
     */
    CX08("4","固定测斜仪"),

    /**
     * 超声波采集器
     */
    UDS08("6","超声波液(物)位计"),

    /**
     * 雷达采集器
     */
    RD08("7","雷达液(物)位计"),

    /**
     * 墒情采集器
     */
    SMC08("8","墒情计"),

    /**
     * 温湿度采集器
     */
    TH08("12","温湿度计"),

    /**
     * 数字水位计采集器
     */
    DVWP("15","扬压力计"),

    /**
     * 倾角仪采集器
     */
    QJY08("16","倾角仪"),

    /**
     * 单通道采集器
     */
    VW01("20","单通道采集器"),

    /**
     * 次声采集器
     */
    CS08("21","次声仪"),

    /**
     * 量水堰
     */
    LSY("22","量水堰计"),

    /**
     * 静力水准
     */
    JLSZ("24", "静力水准"),

    /**
     * 气象站
     */
    QXZ("25","气象计"),

    /**
     * 浊度仪传感器
     */
    ZDY("26","浊度仪"),

    /**
     * 浊度仪传感器
     */
    SZSW("27","数字式水位计"),

    WATER_QUALITY_METER("28", "多参数水质仪"),

    UNKNOWN_TYPE("-1", "未知类型");

    IOTCollectorModel(String code,String description) {
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

    public static IOTCollectorModel value(String code) {
        if (TextUtils.isEmpty(code))
            return VW08;

        for (IOTCollectorModel collectorModel : IOTCollectorModel.values()) {
            if(collectorModel.getCode().equals(code))
                return collectorModel;
        }

        return UNKNOWN_TYPE;
    }

    public static boolean isValidCollector(String coll) {
        if (TextUtils.isEmpty(coll))
            return false;

        List<String> allCollectors = new ArrayList<>();
        for (IOTCollectorModel modle : IOTCollectorModel.values()) {
            allCollectors.add(modle.getCode());
        }

        return allCollectors.contains(coll);
    }
}
