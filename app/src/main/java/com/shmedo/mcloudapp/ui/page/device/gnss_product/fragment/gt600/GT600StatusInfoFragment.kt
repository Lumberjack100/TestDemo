package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.gt600

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.GT600DeviceStatusInfo
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2026/01/08
 * 描述：GT600 设备状态信息页面
 *
 * 功能说明：
 * 1. 继承自 OptimizedBaseDeviceStatusInfoStyleFragment，使用统一的设备信息展示样式
 * 2. 使用 QUERY_DEVICE_STATUS (getstatus) 指令查询设备状态
 * 3. 展示五个信息分组：
 *    - 工作信息：工作模式、卫星数量
 *    - 供电信息：光伏板电压、外部电压
 *    - 环境信息：内部温度、内部湿度
 *    - 姿态信息：X轴角度、Y轴角度、Z轴角度
 *    - 模块信息：GNSS模块、倾角加速度模块、4G模块、存储卡、SIM卡
 *
 * 数据来源：
 * - 使用 getstatus 指令查询设备状态
 * - JSON 响应数据结构参考 GT600DeviceStatusInfo 类注释
 *
 * 参考实现：
 * - M50StatusInfoFragment.kt
 */
class GT600StatusInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {

    // ==================== 生命周期与初始化 ====================

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        // 设置页面标题
        binding.llToolbar.toolbar.title = "状态信息"
    }

    // ==================== 数据处理 ====================

    /**
     * 初始化状态信息 - 处理 GT600 设备状态数据
     *
     * 该方法在基类接收到指令响应后被调用，负责：
     * 1. 解析 JSON 数据为 GT600DeviceStatusInfo 对象
     * 2. 构建各信息分组的列表项
     * 3. 更新 RecyclerView 显示
     *
     * @param content 设备状态信息的 JSON 字符串
     */
    override fun <T> initStatusInfo(content: T) {
        launchWithViewLifecycle {
            try {
                // 在 IO 线程解析 JSON 数据，避免阻塞主线程
                val statusInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<GT600DeviceStatusInfo>(content as String)
                }

                // 数据校验：确保状态信息不为空
                if (statusInfo == null) {
                    binding.refreshLayout.showError()
                    return@launchWithViewLifecycle
                }

                // 显示内容区域
                binding.refreshLayout.showContent()

                // 构建 UI 列表
                val groupList = mutableListOf<Any>()

                // 添加各信息分组
                addWorkInfoGroup(groupList, statusInfo)
                addPowerInfoGroup(groupList, statusInfo)
                addEnvironmentInfoGroup(groupList, statusInfo)
                addAttitudeInfoGroup(groupList, statusInfo)
                addModuleInfoGroup(groupList, statusInfo)

                // 更新 RecyclerView 数据
                binding.recyclerview.models = groupList

            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
                binding.refreshLayout.showError()
            }
        }
    }

    // ==================== 分组构建方法 ====================

    /**
     * 添加工作信息分组
     *
     * 包含字段：
     * - 工作模式：暂用占位符
     * - 卫星数量：从 attach_data.Sata 获取
     */
    private fun addWorkInfoGroup(
        groupList: MutableList<Any>,
        statusInfo: GT600DeviceStatusInfo
    ) {
        groupList.add(DeviceStatusInfoGroupItem("工作信息"))

        // 工作模式 - 暂用占位符表示
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "工作模式",
            value = AppContants.PLACE_HOLDER_VALUE
        )

        // 卫星数量
        // 数据来源：attach_data.Sata
        val satelliteCount = statusInfo.satelliteCount.ifEmpty { "0" }
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "卫星数量",
            value = satelliteCount,
            isBottomItem = true // 该分组的最后一项
        )
    }

    /**
     * 添加供电信息分组
     *
     * 包含字段：
     * - 光伏板电压：从 attach_data.solar 获取，单位 V
     * - 外部电压：从 ext_power_volt 获取，单位 V
     */
    private fun addPowerInfoGroup(
        groupList: MutableList<Any>,
        statusInfo: GT600DeviceStatusInfo
    ) {
        // 添加分组间隔
        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        groupList.add(DeviceStatusInfoGroupItem("供电信息"))

        // 光伏板电压
        // 数据来源：attach_data.solar
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "光伏板电压",
            value = statusInfo.solarVoltage.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
            unit = "V"
        )

        // 外部电压
        // 数据来源：ext_power_volt
        val externalVoltage = statusInfo.ext_power_volt.toDoubleOrNull() ?: 0.0
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "外部电压",
            value = statusInfo.ext_power_volt.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
            // 外部电压低于 11V 时显示警告色
            textColorRes = if (externalVoltage > 0 && externalVoltage < 11) ColorUtils.getColor(R.color.warn_FF9D00) else 0,
            unit = "V",
            isBottomItem = true // 该分组的最后一项
        )
    }

    /**
     * 添加环境信息分组
     *
     * 包含字段：
     * - 内部温度：从 temp 获取，单位 ℃
     * - 内部湿度：从 humidity 获取，单位 %
     */
    private fun addEnvironmentInfoGroup(
        groupList: MutableList<Any>,
        statusInfo: GT600DeviceStatusInfo
    ) {
        // 添加分组间隔
        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        groupList.add(DeviceStatusInfoGroupItem("环境信息"))

        // 内部温度
        // 数据来源：temp
        val internalTemp = statusInfo.temp.toDoubleOrNull() ?: Double.MAX_VALUE
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "内部温度",
            value = statusInfo.temp.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
            // 温度超出正常范围 (-20℃ ~ 70℃) 时显示警告色
            textColorRes = if ((internalTemp > -20 && internalTemp < 70) || internalTemp == Double.MAX_VALUE) 0 else ColorUtils.getColor(
                R.color.warn_FF9D00
            ),
            unit = "℃"
        )

        // 内部湿度
        // 数据来源：humidity
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "内部湿度",
            value = statusInfo.humidity.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
            unit = "%",
            isBottomItem = true // 该分组的最后一项
        )
    }

    /**
     * 添加姿态信息分组
     *
     * 包含字段:
     * - X轴角度: 设备在 X 轴方向的倾斜角度,单位 °
     * - Y轴角度: 设备在 Y 轴方向的倾斜角度,单位 °
     * - Z轴角度: 设备在 Z 轴方向的倾斜角度,单位 °
     *
     * 数据来源:
     * - attach_data.MEMs: 倾角加速度模块状态 (OK/FAIL)
     * - attach_data.memsRawData: 原始角度数据,格式为 "x,y,z"
     *
     * 数据处理逻辑:
     * 1. 检查 MEMs 模块状态,如果包含 "FAIL" 则所有角度显示为占位符
     * 2. 解析 memsRawData 字符串,按逗号分割获取三轴角度
     * 3. 验证数据格式,确保有三个角度值
     * 4. 空值或异常数据使用占位符显示
     */
    private fun addAttitudeInfoGroup(
        groupList: MutableList<Any>,
        statusInfo: GT600DeviceStatusInfo
    ) {
        // 添加分组间隔
        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        groupList.add(DeviceStatusInfoGroupItem("姿态信息"))

        // 解析三轴角度数据
        val (xAngle, yAngle, zAngle) = parseAttitudeAngles(statusInfo)

        // X轴角度
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "X轴角度",
            value = xAngle.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
            unit = "°"
        )

        // Y轴角度
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "Y轴角度",
            value = yAngle.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
            unit = "°"
        )

        // Z轴角度
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "Z轴角度",
            value = zAngle.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
            unit = "°",
            isBottomItem = true // 该分组的最后一项
        )
    }

    /**
     * 解析姿态角度数据
     *
     * 从设备状态信息中提取三轴角度值
     *
     * @param statusInfo 设备状态信息对象
     * @return Triple<String, String, String> 依次为 X、Y、Z 轴角度值
     *
     * 解析规则:
     * 1. 如果 MEMs 模块状态包含 "FAIL",返回空字符串
     * 2. 解析 memsRawData 字符串 (格式: "x,y,z")
     * 3. 验证分割后的数组长度为 3
     * 4. 任何异常情况返回空字符串,由调用方使用占位符显示
     */
    private fun parseAttitudeAngles(statusInfo: GT600DeviceStatusInfo): Triple<String, String, String> {
        // 检查 MEMs 模块状态
        if (statusInfo.memsStatus.contains("FAIL", ignoreCase = true)) {
            return Triple("", "", "")
        }

        // 解析原始角度数据
        val angleValues = statusInfo.memsRawData.split(",")
        
        // 验证数据格式: 必须包含三个角度值
        return if (angleValues.size == 3) {
            Triple(
                angleValues[0].trim(),
                angleValues[1].trim(),
                angleValues[2].trim()
            )
        } else {
            // 数据格式异常,返回空值
            Triple("", "", "")
        }
    }

    /**
     * 添加模块信息分组
     *
     * 包含字段：
     * - GNSS模块：
     * - 倾角加速度模块：从 attach_data.MEMs 状态判断
     * - 4G模块：从 attach_data.LTEMod 获取
     * - 存储卡：从 attach_data.sysinfo.tfcard_status 获取
     * - SIM卡：从 attach_data.SIM 获取
     */
    private fun addModuleInfoGroup(
        groupList: MutableList<Any>,
        statusInfo: GT600DeviceStatusInfo
    ) {
        // 添加分组间隔
        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        groupList.add(DeviceStatusInfoGroupItem("模块信息"))

        // GNSS 模块
        // 判断逻辑：卫星数量 > 0 表示正常
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "GNSS模块",
            value = statusInfo.gnssStatus.uppercase().compareAndReturn("OK", "正常", "故障"),
            textColorRes = if (statusInfo.gnssStatus.uppercase() == "OK") 0 else ColorUtils.getColor(
                R.color.error_FF4400
            )
        )

        // 倾角加速度模块
        // 判断逻辑：MEMs 数据包含 "OK" 表示正常
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "倾角加速度模块",
            value = statusInfo.memsStatus.uppercase().compareAndReturn("OK", "正常", "故障"),
            textColorRes = if (statusInfo.memsStatus.uppercase() == "OK") 0 else ColorUtils.getColor(
                R.color.error_FF4400
            )
        )

        // 4G 模块
        // 数据来源：attach_data.LTEMod
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "4G模块",
            value = statusInfo.lteModuleStatus.uppercase().compareAndReturn("OK", "正常", "故障"),
            textColorRes = if (statusInfo.lteModuleStatus.uppercase() == "OK") 0 else ColorUtils.getColor(
                R.color.error_FF4400
            )
        )

        // 存储卡
        // 数据来源：attach_data.sysinfo.tfcard_status
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "存储卡",
            value = statusInfo.storageCardStatus.uppercase().compareAndReturn("OK", "有", "无"),
            textColorRes = if (statusInfo.storageCardStatus.uppercase() == "OK") 0 else ColorUtils.getColor(
                R.color.error_FF4400
            )
        )

        // SIM 卡
        // 数据来源：attach_data.SIM
        DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
            groupList,
            name = "SIM卡",
            value = statusInfo.simStatus.uppercase().compareAndReturn("OK", "有", "无"),
            textColorRes = if (statusInfo.simStatus.uppercase() == "OK") 0 else ColorUtils.getColor(
                R.color.error_FF4400
            ),
            isBottomItem = true // 该分组的最后一项
        )
    }
}
