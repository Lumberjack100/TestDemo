package com.shmedo.configlibrary.iot.enums;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/2/24 <br/>
 * 描述：     产品类型
 */
public enum ProductType {

    DAS("DAS", "L", "自动化测斜机器人"),

    ADME("ADME", "T", "自动化测斜机器人"),

    VMS("VMS|GW300", "G", "振弦式采集仪"),

    E40("E40|E60", "B", "GNSS"),

    M20("M20", "V", "GNSS"),

    RN20("RN20", "Y", "轴力计"),

    BHY("BHY", "H", "轴力计"),

    LR200("LR200", "Z", "一体式裂缝计"),


    INCLINOMETER_DEBUG_BOX("INCLINOMETER", "#", "蓝牙测斜仪调试盒子"),

    UnKnown("UnKnown", "#", "未知类型");

    ProductType(String prefix, String suffix, String description) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.description = description;
    }

    private String prefix;// 产品类型前缀
    private String suffix;//SN 后缀标识
    private String description;//产品描述

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return prefix;
    }

    /**
     * 根据产品类型名称前缀标识匹配产品类型
     * @param productToken
     * @return
     */
    public static ProductType valueByPrefix(String productToken) {
        if (TextUtils.isEmpty(productToken))
            return UnKnown;

        if (productToken.toUpperCase().contains("E60"))
            return ProductType.E40;

        if (productToken.toUpperCase().contains("GW300"))
            return ProductType.VMS;

        for (ProductType productType : ProductType.values()) {
            if (productToken.toUpperCase().contains(productType.getPrefix()))
                return productType;
        }

        return UnKnown;
    }

    /**
     * 根据产品 SN 号后缀标识匹配产品类型
     * @param deviceToken
     * @return
     */
    public static ProductType valueBySuffix(String deviceToken) {
        if (TextUtils.isEmpty(deviceToken))
            return UnKnown;

        if (deviceToken.startsWith("M20") && deviceToken.endsWith("T"))
            return ProductType.M20;

        for (ProductType productType : ProductType.values()) {
            if (deviceToken.endsWith(productType.getSuffix()))
                return productType;
        }

        return UnKnown;
    }
}
