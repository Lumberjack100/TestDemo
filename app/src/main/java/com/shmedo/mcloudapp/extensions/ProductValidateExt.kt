package com.shmedo.mcloudapp.extensions

import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.R

/**
 * 创建者：gonghe
 * 创建时间：2025/10/24
 * 描述： 产品类型校验扩展 - 统一管理所有产品类型判断逻辑
 */

// ==================== GNSS 产品线 ====================

/**
 * 判断是否为 M20 系列产品（包含 M20、GNSS_M_1、GNSS_M_2）
 */
fun ProductType.isM20Series(): Boolean {
    return this == ProductType.M20 || this == ProductType.GNSS_M_1 || this == ProductType.GNSS_M_2
}

/**
 * 判断是否为 M50 系列产品（GNSS_M_5、GNSS_M_6、GNSS_M_7、GNSS_M_8）
 */
fun ProductType.isM50Series(): Boolean {
    return this == ProductType.GNSS_M_5 || this == ProductType.GNSS_M_6 ||
            this == ProductType.GNSS_M_7 || this == ProductType.GNSS_M_8
}

/**
 * 判断是否为 E 系列产品（分体机：GNSS_E_1、GNSS_E_2）
 */
fun ProductType.isESeries(): Boolean {
    return this == ProductType.GNSS_E_1 || this == ProductType.GNSS_E_2
}

/**
 * 判断是否为 E50 Pro 产品
 */
fun ProductType.isE50Pro(): Boolean {
    return this == ProductType.GNSS_E_3
}

/**
 * 判断是否为 GT 系列产品（抗干扰）
 */
fun ProductType.isGTSeries(): Boolean {
    return this == ProductType.GNSS_T_1
}

/**
 * 判断是否为所有 GNSS 产品
 */
fun ProductType.isGNSSProduct(): Boolean {
    return isM20Series() || isM50Series() || isESeries() || isE50Pro() || isGTSeries()
}

// ==================== UD 产品线 ====================
/**
 * 判断是否为 DR030 系列产品（雷达水位计：U_D_1、U_D_2）
 */
fun ProductType.isDR030Series(): Boolean {
    return this == ProductType.U_D_1 || this == ProductType.U_D_2
}

/**
 * 判断是否为 LL030 产品（雷达流量计：U_D_3）
 */
fun ProductType.isLL030(): Boolean {
    return this == ProductType.U_D_3
}

/**
 * 判断是否为 UD 系列产品（U_D_1、U_D_2、U_D_3）
 */
fun ProductType.isUDSeries(): Boolean {
    return this == ProductType.U_D_1 || this == ProductType.U_D_2 || this == ProductType.U_D_3
}

// ==================== 采集器产品线 ====================

/**
 * 判断是否为自组网报警网关
 */
fun ProductType.isGateway(): Boolean {
    return this == ProductType.COLLECTOR_G_0
}

/**
 * 判断是否为 多模融合网关 系列产品
 */
fun ProductType.isMultiModeGatewaySeries(): Boolean {
    return this == ProductType.COLLECTOR_G_3 || this == ProductType.COLLECTOR_G_4
}


/**
 * 判断是否为 MR701 产品
 */
fun ProductType.isMR701(): Boolean {
    return this == ProductType.COLLECTOR_R_1
}


/**
 * 判断是否为 DAS 系列产品
 */
fun ProductType.isDASBHYSeries(): Boolean {
    return this == ProductType.DAS || this == ProductType.BHY
}


/**
 * 判断是否为 MR702 产品
 */
fun ProductType.isMR702(): Boolean {
    return this == ProductType.COLLECTOR_R_2
}

// ==================== 其他产品线 ====================

/**
 * 判断是否为 LR200 产品
 */
fun ProductType.isLR200(): Boolean {
    return this == ProductType.LR200
}

/**
 * 判断是否为 U_I_1 产品
 */
fun ProductType.isUIProduct(): Boolean {
    return this == ProductType.U_I_1
}

/**
 * 判断是否为 U_R_1 产品
 */
fun ProductType.isURProduct(): Boolean {
    return this == ProductType.U_R_1
}

/**
 * 判断是否为 U_I_1/U_R_1 系列产品
 */
fun ProductType.isUIURSeries(): Boolean {
    return this == ProductType.U_I_1 || this == ProductType.U_R_1
}

/**
 * 判断是否为 LB20S 产品
 */
fun ProductType.isLB20S(): Boolean {
    return this == ProductType.LB20S
}

// ==================== 功能特性判断 ====================

/**
 * 判断是否支持抓拍图片功能
 */
fun ProductType.supportsCaptureImage(): Boolean {
    return isUDSeries() || isM50Series()
}


/**
 * 判断是否支持固件升级
 */
fun ProductType.isSupportFirmwareUpgrade(): Boolean {
    return this != ProductType.COLLECTOR_G_0 && this != ProductType.U_L_1
}


// ==================== 设备图标相关 ====================

/**
 * 获取设备 Logo 资源 ID 列表（正常、报警、错误状态）
 */
fun ProductType.getDeviceLogoResIds(): List<Int> {
    return when {
        isM20Series() -> listOf(
            R.drawable.device_logo_m20,
            R.drawable.device_logo_m20_alarm,
            R.drawable.device_logo_m20_error
        )

        isM50Series() -> listOf(
            R.drawable.device_logo_m50,
            R.drawable.device_logo_m50_alarm,
            R.drawable.device_logo_m50_error
        )

        isESeries() -> listOf(
            R.drawable.device_logo_e40,
            R.drawable.device_logo_e40_alarm,
            R.drawable.device_logo_e40_error
        )

        isE50Pro() -> listOf(
            R.drawable.device_logo_e50_pro,
            R.drawable.device_logo_e50_pro_alarm,
            R.drawable.device_logo_e50_pro_error
        )

        isGTSeries() -> listOf(
            R.drawable.device_logo_gt600,
            R.drawable.device_logo_gt600_alarm,
            R.drawable.device_logo_gt600_error
        )

        isGateway() -> listOf(
            R.drawable.device_logo_gateway,
            R.drawable.device_logo_gateway_alarm,
            R.drawable.device_logo_gateway_error
        )

        isMultiModeGatewaySeries() -> listOf(
            R.drawable.device_logo_multimode_gateway,
            R.drawable.device_logo_multimode_gateway_alarm,
            R.drawable.device_logo_multimode_gateway_error
        )

        isDASBHYSeries() -> listOf(
            R.drawable.device_logo_das,
            R.drawable.device_logo_das_alarm,
            R.drawable.device_logo_das_error
        )

        isMR701() -> listOf(
            R.drawable.device_logo_mr701_new,
            R.drawable.device_logo_mr701_new_alarm,
            R.drawable.device_logo_mr701_new_error
        )

        isMR702() -> listOf(
            R.drawable.device_logo_mr702,
            R.drawable.device_logo_mr702_alarm,
            R.drawable.device_logo_mr702_error
        )

        isLR200() -> listOf(
            R.drawable.device_logo_bhy_3_lr200,
            R.drawable.device_logo_bhy_3_lr200_alarm,
            R.drawable.device_logo_bhy_3_lr200_error
        )

        isUIURSeries() -> listOf(
            R.drawable.device_logo_bhy_3s,
            R.drawable.device_logo_bhy_3s_alarm,
            R.drawable.device_logo_bhy_3s_error
        )

        isUDSeries() -> listOf(
            R.drawable.device_logo_dr030,
            R.drawable.device_logo_dr030_alarm,
            R.drawable.device_logo_dr030_error
        )

        isLB20S() -> listOf(
            R.drawable.device_logo_lb20s,
            R.drawable.device_logo_lb20s_alarm,
            R.drawable.device_logo_lb20s_error
        )

        else -> listOf(
            R.drawable.device_logo_default,
            R.drawable.device_logo_default_alarm,
            R.drawable.device_logo_default_error
        )
    }
}

// ==================== 历史数据相关 ====================

/**
 * 获取支持的监测数据类型配置
 */
fun ProductType.getSensorDataConfig(): SensorDataConfig? {
    return when {
        isDR030Series() -> SensorDataConfig(
            modelNames = listOf("液位海拔", "空高距离", "安装角度", "抓拍图片"),
            modelTokens = listOf("904", "904", "206", "10001"),
            valueDescs = listOf("高度(m)", "高度(m)", "角度(°)", "操作"),
            fieldPaths = listOf("liquid_surface_alt", "ullage", "z")
        )

        isLL030() -> SensorDataConfig(
            modelNames = listOf(
                "液位海拔",
                "空高距离",
                "瞬时流速",
                "瞬时流量",
                "累计流量",
                "安装角度",
                "抓拍图片"
            ),
            modelTokens = listOf("904", "904", "217", "220", "233", "206", "10001"),
            valueDescs = listOf(
                "高度(m)",
                "高度(m)",
                "瞬时流速(m/s)",
                "瞬时流量(m³/s)",
                "累计流量(m³)",
                "角度(°)",
                "操作"
            ),
            fieldPaths = listOf("liquid_surface_alt", "ullage", "value", "value", "totalQ", "z")
        )

        isM20Series() -> SensorDataConfig(
            modelNames = listOf("X位移量", "Y位移量", "Z位移量", "X轴角度", "Y轴角度", "Z轴角度"),
            modelTokens = listOf("224", "224", "224", "103", "103", "103"),
            valueDescs = listOf("位移(mm)", "位移(mm)", "位移(mm)", "角度(°)", "角度(°)", "角度(°)"),
            fieldPaths = listOf("x", "y", "z", "x", "y", "z")
        )

        isM50Series() -> SensorDataConfig(
            modelNames = listOf("X位移量", "Y位移量", "Z位移量", "X轴角度", "Y轴角度", "Z轴角度"),
            modelTokens = listOf("224", "224", "224", "103", "103", "103"),
            valueDescs = listOf("位移(mm)", "位移(mm)", "位移(mm)", "角度(°)", "角度(°)", "角度(°)"),
            fieldPaths = listOf("x", "y", "z", "x", "y", "z")
        )

        else -> null
    }
}

/**
 * 传感器数据配置数据类
 */
data class SensorDataConfig(
    val modelNames: List<String>,
    val modelTokens: List<String>,
    val valueDescs: List<String>,
    val fieldPaths: List<String>
)