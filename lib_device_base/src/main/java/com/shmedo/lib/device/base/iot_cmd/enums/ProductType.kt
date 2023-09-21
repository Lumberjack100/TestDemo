package com.shmedo.lib.device.base.iot_cmd.enums

import android.os.Parcelable
import android.text.TextUtils
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2022/2/24 <br></br>
 * 描述：     产品类型
 */
@Parcelize
enum class ProductType(
    val prefix: String, //产品类型前缀
    val suffix: String, //SN 后缀标识
    val description: String//产品描述
) : Parcelable {
    ADME("ADME", "T", "自动化测斜机器人"),
    BHY("BHY", "H", "崩滑仪"),
    DAS("DAS", "L", "智能采集器"),
    DMS("DMS", "F", "数字式采集仪"),
    E40("E40|E60", "B", "GNSS"),
    HAC("ADME_HAC10", "T", "半自动化测斜机器人"),
    INCLINOMETER_DEBUG_BOX("INCLINOMETER", "#", "蓝牙测斜仪调试盒子"),
    LR200("LR200", "Z", "一体式裂缝计"),
    M20("M20", "V", "M20-GNSS"),
    MR702("SL651", "L", "水利遥测终端机"),
    RN20("RN20", "Y", "轴力计"),
    VMS("VMS|GW300", "G", "振弦式采集仪"),

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