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
    val oldSuffix: String = "", //旧的产品类型后缀标识
    val newSuffix: String = "", //新的产品类型后缀标识
    val productName: String,//产品名称
    val description: String = ""//产品描述
) : Parcelable {
    //COLLECTOR(采集器产品线)产品线  G(自组网网关)
    COLLECTOR_G_0(
        "",
        newSuffix = "CG0",
        productName = "自组网报警网关",
        description = ""
    ),

    //COLLECTOR(采集器产品线)产品线  R(RTU)
    COLLECTOR_R_1(
        "",
        newSuffix = "CR1",
        productName = "智能采集器",
        description = "对应MR-701"
    ),
    COLLECTOR_R_2(
        "",
        newSuffix = "CR2",
        productName = "智能采集器",
        description = "对应MR-702"
    ),

    //GNSS产品线 E(分体机)
    GNSS_E_1(
        "",
        newSuffix = "GE1",
        productName = "测地形GNSS接收机",
        description = "对应E40"
    ),
    GNSS_E_2(
        "",
        newSuffix = "GE2",
        productName = "测地形GNSS接收机",
        description = "对应E40S"
    ),
    GNSS_E_3(
        "",
        newSuffix = "GE3",
        productName = "测地形GNSS接收机",
        description = "基站接收机,应E50 Pro"
    ),

    //GNSS产品线 M(一体机)
    GNSS_M_1(
        "",
        newSuffix = "GM1",
        productName = "测地形GNSS接收机",
        description = "单北斗,对应UM960版本"
    ),
    GNSS_M_2(
        "",
        newSuffix = "GM2",
        productName = "测地形GNSS接收机",
        description = "全星座,对应UM960D版本"
    ),
    GNSS_M_3(
        "",
        newSuffix = "GM3",
        productName = "测地形GNSS接收机",
        description = "自供电版,待立项"
    ),


    //GNSS产品线 T(抗干扰)
    GNSS_T_1(
        "",
        newSuffix = "GT1",
        productName = "测地形GNSS接收机",
        description = "对应当前GT600"
    ),

    //M(机电产品线) A(自动测斜仪)
    M_A_1(
        "",
        newSuffix = "MA1",
        productName = "基坑自动测斜仪",
        description = "AC50,基坑自动测斜仪"
    ),
    M_A_2(
        "",
        newSuffix = "MA2",
        productName = "水电站深层测斜仪",
        description = "BC10,水电站深层测斜仪"
    ),

    //SINGLE(单传感器产品线) A(倾角计)
    SINGLE_A_1(
        "",
        newSuffix = "SA1",
        productName = "倾角计",
        description = "单轴"
    ),
    SINGLE_A_2(
        "",
        newSuffix = "SA2",
        productName = "倾角计",
        description = "双轴"
    ),
    SINGLE_A_3(
        "",
        newSuffix = "SA3",
        productName = "倾角计",
        description = "三轴,对应MD-QJ390P"
    ),

    //SINGLE(单传感器产品线)  V(VMS)
    SINGLE_V_1(
        "",
        newSuffix = "SV1",
        productName = "振弦式采集仪",
        description = ""
    ),

    //INTEGRATION(一体化传感器产品线) D(雷达计)
    U_D_1(
        "",
        newSuffix = "UD1",
        productName = "雷达计",
        description = "水位,对应MD-DR030"
    ),
    U_D_2(
        "",
        newSuffix = "UD2",
        productName = "雷达计",
        description = "泥位,对应MD-LD30"
    ),

    //INTEGRATION(一体化传感器产品线) L(裂缝计)
    U_L_1(
        "",
        newSuffix = "UL1",
        productName = "裂缝计",
        description = ""
    ),

    //INTEGRATION(一体化传感器产品线) I(倾斜仪)
    U_I_1(
        "",
        newSuffix = "UI1",
        productName = "倾斜仪",
        description = "三轴,对应BHY-3S"
    ),

    //INTEGRATION(一体化传感器产品线) R(雨量计)
    U_R_1(
        "",
        newSuffix = "UR1",
        productName = "雨量计",
        description = ""
    ),

    ADME_HAC("ADME_HAC10", oldSuffix = "T", productName = "半自动化测斜机器人"),
    ADME("ADME", oldSuffix = "T", productName = "自动化测斜机器人"),
    BHY("BHY", oldSuffix = "H", productName = "崩滑仪"),
    DAS("DAS", oldSuffix = "L", productName = "智能采集器"),
    LR200("LR200", oldSuffix = "Z", productName = "米度一体式裂缝计"),
    M20("M20", oldSuffix = "V", productName = "M20-GNSS"),
    MR702("MR702", oldSuffix = "A", productName = "水利遥测终端机"),
    RN20("RN20", oldSuffix = "Y", productName = "轴力计"),
    LB20S("MD-LB20S", oldSuffix = "S", productName = "无线预警广播"),

    INCLINOMETER_DEBUG_BOX("INCLINOMETER", "#", productName = "蓝牙测斜仪调试盒子"),
    TEST_DEVICE("TEST", oldSuffix = "", productName = "测试设备"),
    UnKnown("UnKnown", oldSuffix = "", productName = "未知类型");

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

            for (productType in entries) {
                if (productType.prefix.isEmpty())
                    continue

                val tags = productType.prefix.split(",").toTypedArray()
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
        fun valueByOldSuffix(deviceToken: String): ProductType {
            if (TextUtils.isEmpty(deviceToken))
                return UnKnown

            return entries.firstOrNull { it.oldSuffix.isNotEmpty() && deviceToken.endsWith(it.oldSuffix) }
                ?: UnKnown
        }

        @JvmStatic
        fun valueByNewSuffix(deviceToken: String): ProductType {
            if (TextUtils.isEmpty(deviceToken))
                return UnKnown

            return entries.firstOrNull { it.newSuffix.isNotEmpty() && deviceToken.endsWith(it.newSuffix) }
                ?: UnKnown
        }
    }
}