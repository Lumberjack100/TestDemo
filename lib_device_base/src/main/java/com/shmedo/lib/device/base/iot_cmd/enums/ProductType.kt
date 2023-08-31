package com.shmedo.lib.device.base.iot_cmd.enums

import android.text.TextUtils
import java.util.Locale

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/2/24 <br></br>
 * 描述：     产品类型
 */
enum class ProductType(// 产品类型前缀
    val prefix: String, //SN 后缀标识
    val suffix: String, //产品描述
    val description: String
) {
    DAS("DAS", "L", "智能采集器"),
    HAC("ADME_HAC10", "T", "半自动化测斜机器人"),
    ADME("ADME", "T", "自动化测斜机器人"),
    VMS("VMS|GW300", "G", "振弦式采集仪"),
    E40("E40|E60", "B", "GNSS"),
    M20("M20", "V", "GNSS"),
    RN20("RN20", "Y", "轴力计"),
    BHY("BHY", "H", "崩滑仪"),
    LR200("LR200", "Z", "一体式裂缝计"),
    DMS("DMS", "F", "数字式采集仪"),
    INCLINOMETER_DEBUG_BOX("INCLINOMETER", "#", "蓝牙测斜仪调试盒子"),

    UnKnown("UnKnown", "#", "未知类型");

    override fun toString(): String {
        return prefix
    }

    companion object {
        /**
         * 根据产品类型名称前缀标识匹配产品类型
         *
         * @param productToken
         * @return
         */
        @JvmStatic
        fun valueByPrefix(productToken: String): ProductType {
            if (TextUtils.isEmpty(productToken))
                return UnKnown
            for (productType in values()) {
                val tags = productType.prefix.split("\\|").toTypedArray()
                for (tag in tags) {
                    if (productToken.uppercase(Locale.getDefault()).startsWith(tag))
                        return productType
                }
            }
            return UnKnown
        }

        /**
         * 根据产品 SN 号后缀标识匹配产品类型
         *
         * @param deviceToken
         * @return
         */
        @JvmStatic
        fun valueBySuffix(deviceToken: String): ProductType {
            if (TextUtils.isEmpty(deviceToken))
                return UnKnown
            if (deviceToken.endsWith("T")) {
                return if (deviceToken.startsWith("M20")) M20 else ADME
            }
            for (productType in values()) {
                if (deviceToken.endsWith(productType.suffix)) return productType
            }
            return UnKnown
        }
    }
}