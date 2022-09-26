package com.shmedo.configlibrary.iot.enums;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/9/26 <br/>
 * 描述：    SL651水文 遥测站分类码
 */
public enum StationCode {
    PRECIPITATION("降水", "P"),
    RIVER_COURSE("河道", "H"),
    RESERVOIR("水库(湖泊)", "K"),
    DAM("闸坝", "Z"),
    PUMPING_STATION("泵站", "D"),
    TIDAL("潮汐", "T"),
    MOISTURE("墒情", "M"),
    GROUNDWATER("地下水", "G"),
    WATER_QUALITY("水质", "Q"),
    WATER_INTAKE("取水口", "I"),
    DRAIN("排水口", "O"),

    CUSTOM("自定义", "1");


    private String name;
    private String code;

    StationCode(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public static StationCode valueByName(String name) {
        if (TextUtils.isEmpty(name))
            return CUSTOM;

        for (StationCode errorType : StationCode.values()) {
            if (errorType.getName().equals(name))
                return errorType;
        }
        return CUSTOM;
    }

    public static StationCode valueByCode(String code) {
        if (TextUtils.isEmpty(code))
            return CUSTOM;

        for (StationCode errorType : StationCode.values()) {
            if (errorType.getCode().equals(code))
                return errorType;
        }
        return CUSTOM;
    }

    public static List<String> getNames() {
        List<String> tempList = new ArrayList<>();
        for (StationCode errorType : StationCode.values()) {
            tempList.add(errorType.getName());
        }

        return tempList;
    }
}
