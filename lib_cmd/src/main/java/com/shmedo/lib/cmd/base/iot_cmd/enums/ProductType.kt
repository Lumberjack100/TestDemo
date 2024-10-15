package com.shmedo.lib.cmd.base.iot_cmd.enums

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
    val productName: String = "",//产品名称
    val productToken: String = "",//设备名称
    val description: String = ""//产品描述
) : Parcelable {
    //COLLECTOR(采集器产品线)产品线  G(自组网网关)
    COLLECTOR_G_0(
        prefix = "",
        oldSuffix = "",
        newSuffix = "CG0",
        productName = "自组网报警网关",
        productToken = "",
        description = ""
    ),

    //COLLECTOR(采集器产品线)产品线  R(RTU)
    COLLECTOR_R_1(
        prefix = "",
        oldSuffix = "",
        newSuffix = "CR1",
        productName = "智能采集器",
        productToken = "",
        description = "对应MR-701"
    ),
    COLLECTOR_R_2(
        prefix = "",
        oldSuffix = "",
        newSuffix = "CR2",
        productName = "智能采集器",
        productToken = "",
        description = "对应MR-702"
    ),

    //GNSS产品线 E(分体机)
    GNSS_E_1(
        prefix = "",
        oldSuffix = "",
        newSuffix = "GE1",
        productName = "测地形GNSS接收机",
        productToken = "",
        description = "对应E40"
    ),
    GNSS_E_2(
        prefix = "",
        oldSuffix = "",
        newSuffix = "GE2",
        productName = "测地形GNSS接收机",
        productToken = "",
        description = "对应E40S"
    ),
    GNSS_E_3(
        prefix = "",
        oldSuffix = "",
        newSuffix = "GE3",
        productName = "测地形GNSS接收机",
        productToken = "",
        description = "基站接收机,应E50 Pro"
    ),

    //GNSS产品线 M(一体机)
    GNSS_M_1(
        prefix = "",
        oldSuffix = "",
        newSuffix = "GM1",
        productName = "测地形GNSS接收机",
        productToken = "",
        description = "单北斗,对应UM960版本"
    ),
    GNSS_M_2(
        prefix = "",
        oldSuffix = "",
        newSuffix = "GM2",
        productName = "测地形GNSS接收机",
        productToken = "",
        description = "全星座,对应UM960D版本"
    ),
    GNSS_M_5(
        prefix = "",
        oldSuffix = "",
        newSuffix = "GM5",
        productName = "测地形GNSS接收机",
        productToken = "",
        description = "自供电版,待立项"
    ),


    //GNSS产品线 T(抗干扰)
    GNSS_T_1(
        prefix = "",
        oldSuffix = "",
        newSuffix = "GT1",
        productName = "测地形GNSS接收机",
        productToken = "",
        description = "对应当前GT600"
    ),

    //M(机电产品线) A(自动测斜仪)
    M_A_1(
        prefix = "",
        oldSuffix = "",
        newSuffix = "MA1",
        productName = "基坑自动测斜仪",
        productToken = "",
        description = "AC50,基坑自动测斜仪"
    ),
    M_A_2(
        prefix = "",
        oldSuffix = "",
        newSuffix = "MA2",
        productName = "水电站深层测斜仪",
        productToken = "",
        description = "BC10,水电站深层测斜仪"
    ),

    //SINGLE(单传感器产品线) A(倾角计)
    SINGLE_A_1(
        prefix = "",
        oldSuffix = "",
        newSuffix = "SA1",
        productName = "倾角计",
        productToken = "",
        description = "单轴"
    ),
    SINGLE_A_2(
        prefix = "",
        oldSuffix = "",
        newSuffix = "SA2",
        productName = "倾角计",
        productToken = "",
        description = "双轴"
    ),
    SINGLE_A_3(
        prefix = "",
        oldSuffix = "",
        newSuffix = "SA3",
        productName = "倾角计",
        productToken = "",
        description = "三轴,对应MD-QJ390P"
    ),

    //SINGLE(单传感器产品线)  V(VMS)
    SINGLE_V_1(
        prefix = "",
        oldSuffix = "",
        newSuffix = "SV1",
        productName = "振弦式采集仪",
        productToken = "",
        description = ""
    ),

    //INTEGRATION(一体化传感器产品线) D(雷达计)
    U_D_1(
        prefix = "",
        oldSuffix = "",
        newSuffix = "UD1",
        productName = "一体化雷达水位计",
        productToken = "MD-DR030",
        description = "对应型号 MD-DR030"
    ),
    U_D_2(
        prefix = "",
        oldSuffix = "",
        newSuffix = "UD2",
        productName = "一体化雷达泥位计",
        productToken = "MD-NW030",
        description = "对应型号 MD-NW030"
    ),
    U_D_3(
        prefix = "",
        oldSuffix = "",
        newSuffix = "UD3",
        productName = "一体化雷达流量计",
        productToken = "MD-LL030",
        description = "对应型号 MD-LL030"
    ),

    //INTEGRATION(一体化传感器产品线) L(裂缝计)
    U_L_1(
        prefix = "",
        oldSuffix = "",
        newSuffix = "UL1",
        productName = "裂缝计",
        productToken = "",
        description = ""
    ),

    //INTEGRATION(一体化传感器产品线) I(倾斜仪)
    U_I_1(
        prefix = "",
        oldSuffix = "",
        newSuffix = "UI1",
        productName = "倾斜仪",
        productToken = "",
        description = "三轴,对应BHY-3S"
    ),

    //INTEGRATION(一体化传感器产品线) R(雨量计)
    U_R_1(
        prefix = "",
        oldSuffix = "",
        newSuffix = "UR1",
        productName = "雨量计",
        productToken = "",
        description = ""
    ),

    ADME_HAC(
        prefix = "ADME_HAC10",
        oldSuffix = "T",
        productName = "半自动化测斜机器人",
        productToken = "",
        description = ""
    ),
    ADME(
        prefix = "ADME",
        oldSuffix = "T",
        productName = "自动化测斜机器人",
        productToken = "",
        description = ""
    ),
    BHY(
        prefix = "BHY",
        oldSuffix = "H",
        productName = "崩滑仪",
        productToken = "",
        description = ""
    ),
    DAS(
        prefix = "DAS",
        oldSuffix = "L",
        productName = "智能采集器",
        productToken = "",
        description = ""
    ),
    LR200(
        prefix = "LR200",
        oldSuffix = "Z",
        productName = "米度一体式裂缝计",
        productToken = "",
        description = ""
    ),
    M20(
        prefix = "M20",
        oldSuffix = "V",
        productName = "M20-GNSS",
        productToken = "",
        description = ""
    ),
    MR702(
        prefix = "MR702",
        oldSuffix = "A",
        productName = "水利遥测终端机",
        productToken = "",
        description = ""
    ),
    RN20(
        prefix = "RN20",
        oldSuffix = "Y",
        productName = "轴力计",
        productToken = "",
        description = ""
    ),
    LB20S(
        prefix = "MD-LB20S",
        oldSuffix = "S",
        productName = "无线预警广播",
        productToken = "",
        description = ""
    ),

    INCLINOMETER_DEBUG_BOX(
        prefix = "INCLINOMETER",
        oldSuffix = "#",
        productName = "蓝牙测斜仪调试盒子",
        productToken = "",
        description = ""
    ),
    TEST_DEVICE(
        prefix = "TEST",
        oldSuffix = "",
        productName = "测试设备",
        productToken = "",
        description = ""
    ),
    UnKnown(
        prefix = "UnKnown",
        oldSuffix = "",
        productName = "未知类型",
        productToken = "",
        description = ""
    );

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

                productType.prefix.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .forEach { tag ->
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