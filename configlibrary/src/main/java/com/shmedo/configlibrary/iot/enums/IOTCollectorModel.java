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
    VW08("0"){
        @Override
        public String getDescription() {
            return "振弦式采集器";
        }
    },

    /**
     * 雨量采集器
     */
    RAIN08("1"){
        @Override
        public String getDescription() {
            return "雨量计";
        }
    },

    /**
     * 裂缝计采集器
     */
    DS08("2"){
        @Override
        public String getDescription() {
            return "裂缝计";
        }
    },

    /**
     * 土壤湿度采集器
     */
    HD08("3"){
        @Override
        public String getDescription() {
            return "管式含水率计";
        }
    },

    /**
     * 测斜仪采集器
     */
    CX08("4"){
        @Override
        public String getDescription() {
            return "固定测斜仪";
        }
    },

    /**
     * 超声波采集器
     */
    UDS08("6"){
        @Override
        public String getDescription() {
            return "超声波液(物)位计";
        }
    },

    /**
     * 雷达采集器
     */
    RD08("7"){
        @Override
        public String getDescription() {
            return "雷达液(物)位计";
        }
    },

    /**
     * 墒情采集器
     */
    SMC08("8"){
        @Override
        public String getDescription() {
            return "墒情计";
        }
    },

    /**
     * 温湿度采集器
     */
    TH08("12"){
        @Override
        public String getDescription() {
            return "温湿度计";
        }
    },

    /**
     * 数字式渗压计采集器
     */
    DVWP("15"){
        @Override
        public String getDescription() {
            return "扬压力计";
        }
    },

    /**
     * 倾角仪采集器
     */
    QJY08("16"){
        @Override
        public String getDescription() {
            return "倾角仪";
        }
    },

    /**
     * 单通道采集器
     */
    VW01("20"){
        @Override
        public String getDescription() {
            return "单通道采集器";
        }
    },

    /**
     * 次声采集器
     */
    CS08("21"){
        @Override
        public String getDescription() {
            return "次声仪";
        }
    },

    /**
     * 量水堰
     */
    LSY("22"){
        @Override
        public String getDescription() {
            return "量水堰计";
        }
    },

    /**
     * 气象站
     */
    QXZ("25"){
        @Override
        public String getDescription() {
            return "气象计";
        }
    },

    /**
     * 浊度仪传感器
     */
    ZDY("26"){
        @Override
        public String getDescription() {
            return "浊度仪";
        }
    };

    private String code;

    IOTCollectorModel(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return this.code;
    }

    public abstract String getDescription(); // 抽象方法

    public static IOTCollectorModel value(String code) {
        if (TextUtils.isEmpty(code))
            return VW08;

        for (IOTCollectorModel collectorModel : IOTCollectorModel.values()) {
            if(collectorModel.code.equals(code))
                return collectorModel;
        }

        return VW08;
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
