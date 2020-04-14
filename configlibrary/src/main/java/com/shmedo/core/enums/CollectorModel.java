package com.shmedo.core.enums;


import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adu on 2017/12/11.
 * 采集器型号
 */
public enum CollectorModel {
    VW08("00"),
    DS08("02"),
    HD08("03"),
    CX08("04"),
    UDS08("06"),
    RD08("07"),
    SMC08("08"),
    TH08("12"),
    DVWP("15"),
    QJY08("16"),
    VW01("20"),
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
            case "00": return VW08;
            case "02": return DS08;
            case "03": return HD08;
            case "04": return CX08;
            case "06": return UDS08;
            case "07": return RD08;
            case "08": return SMC08;
            case "12": return TH08;
            case "15": return DVWP;
            case "16": return QJY08;
            case "20": return VW01;
            case "21": return CS08;
            default: return VW08;
        }
    }

    public static boolean isValidCollector(String coll)
    {
        if(TextUtils.isEmpty(coll))
            return false;
        List<String> allCollectors= new ArrayList<>();
        for(CollectorModel modle:CollectorModel.values()){
            allCollectors.add(modle.toString());
        }
        return allCollectors.contains(coll);
    }
}
