package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.gt600

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.GT600NetBean
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.GT600StatusInfo
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.model.DataCenterStatusItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoSignalItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.ui.page.device.common.OptimizedBaseDeviceStatusInfoStyleFragment
import com.shmedo.mcloudapp.ui.page.device.common.UniversalDataCenterParamFragment
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2026/01/08
 * @desc: GT600 设备网络信息页面
 *
 * 功能说明：
 * 1. 继承自 OptimizedBaseDeviceStatusInfoStyleFragment，使用统一的设备信息展示样式
 * 2. 展示两个分组：
 *    - 数据网络：移动网络、网络类型、运营商、信号强度、IMEI、ICCID
 *    - 数据链路：数据中心 1-4 的连接状态
 *
 * 数据来源：
 * - 使用 md_getExstatus 指令查询设备状态
 * - 解析返回的 JSON 数据中的 net 对象
 *
 * 参考实现：
 * - M50NetInfoFragment.kt
 * - UDNetInfoFragment.kt
 */
class GT600NetInfoFragment : OptimizedBaseDeviceStatusInfoStyleFragment() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.llToolbar.toolbar.title = "网络信息"
    }

    override fun queryStatusInfo() {
        val commands = mutableListOf<String>()

        val command = IOTCommandUtil.getCommand(IOTCommandType.GT600_GET_EXSTATUS)
        commands.add(command)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    /**
     * 初始化状态信息 - 处理 GT600 设备网络数据
     *
     * @param content 设备状态信息的 JSON 字符串
     */
    override fun <T> initStatusInfo(content: T) {
        launchWithViewLifecycle {
            try {
                // 在 IO 线程解析 JSON 数据
                val statusInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<GT600StatusInfo>(content as String)
                }

                // 数据校验：确保状态信息和网络信息不为空
                val netInfo: GT600NetBean = statusInfo?.net ?: run {
                    binding.refreshLayout.showError()
                    return@launchWithViewLifecycle
                }
                binding.refreshLayout.showContent()
                val groupList = mutableListOf<Any>()

                // ==================== 数据网络分组 ====================
                groupList.add(DeviceStatusInfoGroupItem("数据网络"))

                // 移动网络状态
                // 字段说明：4g 字段，值为 "on" 表示已启用，"off" 表示未启用
                val mobileNetStatus = netInfo._4g.compareAndReturn("on", "已启用", "未启用")
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "移动网络",
                    value = mobileNetStatus,
                    textColorRes = if (netInfo._4g == "on") ColorUtils.getColor(
                        R.color.online_colorPrimary
                    ) else 0
                )

                // 网络类型
                // 字段说明：type 字段，显示网络类型 (4G/2G)
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "网络类型",
                    value = netInfo.type.ifEmpty { AppContants.PLACE_HOLDER_VALUE }
                )

                // 运营商
                // 字段说明：isp 字段，映射运营商名称
                // CHN-CT -> 中国电信, CHINA MOBILE -> 中国移动, CHN-UNICOM -> 中国联通
                val operatorName = when (netInfo.isp.uppercase()) {
                    "CHINA MOBILE" -> "中国移动"
                    "CHN-CT" -> "中国电信"
                    "CHN-UNICOM" -> "中国联通"
                    else -> netInfo.isp.ifEmpty { AppContants.PLACE_HOLDER_VALUE }
                }
                DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                    groupList,
                    name = "运营商",
                    value = operatorName
                )

                // 信号强度
                // 字段说明：csq 字段，需要加负号处理
                // 有效范围：-110 ~ -50 dBm
                val csqValue = netInfo.csq.toIntOrNull() ?: 0
                var signalValue = if (csqValue > 0) (2 * csqValue - 113) else csqValue// 加负号
                // 校验信号强度范围，超出范围设为 0
                if (signalValue !in -110..-50) {
                    signalValue = 0
                }
                groupList.add(
                    DeviceStatusInfoSignalItem(
                        name = "信号强度",
                        signalValue = signalValue,
                        textColorRes = if (signalValue == 0) ColorUtils.getColor(
                            R.color.error_FF4400
                        ) else 0
                    )
                )

                statusInfo.base?.let { baseInfo ->
                    // IMEI
                    // 字段说明：imei 字段，移动设备识别码，支持复制到剪贴板
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "IMEI",
                        value = baseInfo.imei.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                        isClipboard = true
                    )

                    // ICCID
                    // 字段说明：iccid 字段，SIM 卡号，支持复制到剪贴板
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "ICCID",
                        value = baseInfo.iccid.ifEmpty { AppContants.PLACE_HOLDER_VALUE },
                        isClipboard = true,
                        isBottomItem = true // 数据网络分组的最后一项
                    )
                }

                // ==================== 数据链路分组 ====================
                // 添加分组间隔
                groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
                groupList.add(DeviceStatusInfoGroupItem("数据链路"))

                // 数据中心列表
                // 字段说明：socket1-4 字段
                // 0: 未启用, 1: 已连接, 2: 未连接
                val dataCenterList = listOf(
                    netInfo.socket1,
                    netInfo.socket2,
                    netInfo.socket3,
                    netInfo.socket4
                )

                // 遍历处理每个数据中心
                dataCenterList.forEachIndexed { index, socketStatus ->
                    // 根据状态码转换显示文本
                    val statusText = when (socketStatus) {
                        "0" -> "未启用"
                        "1" -> "已连接"
                        "2" -> "未连接"
                        else -> "未启用"
                    }

                    // 添加数据中心状态项
                    DeviceStatusInfoProcessor.addDeviceStatusInfoBasicItemFromString(
                        groupList,
                        name = "数据中心${index + 1}",
                        value = statusText,
                        // 已连接状态显示为绿色，其他状态为默认颜色
                        textColorRes = if (socketStatus == "1") ColorUtils.getColor(
                            R.color.online_colorPrimary
                        ) else 0,
                        isClickable = true, // 支持点击查看详情
                        isBottomItem = index == dataCenterList.size - 1 // 最后一项
                    )
                }

                // 更新 RecyclerView 数据
                binding.recyclerview.models = groupList
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
                binding.refreshLayout.showError()
            }
        }
    }

    /**
     * 处理列表项点击事件
     *
     * 当点击数据链路项时，跳转到数据中心参数配置页面
     *
     * @param infoBasicItem 被点击的列表项
     */
    override fun processItemClick(infoBasicItem: DeviceStatusInfoBasicItem) {
        // 检查是否点击了数据链路项
        if (infoBasicItem.name.startsWith("数据中心")) {
            // 提取数据中心编号
            val index = infoBasicItem.name.substringAfter("数据中心").toIntOrNull() ?: 0

            // 构建数据中心状态数据
            val item = DataCenterStatusItem(
                centerid = index,
                name = "数据中心$index",
                status = when {
                    infoBasicItem.value.contains("未启用") -> "0"
                    infoBasicItem.value.contains("已连接") -> "1"
                    infoBasicItem.value.contains("未连接") -> "2"
                    else -> "0"
                }
            )

            // 创建跳转参数 Bundle
            val bundle = UniversalDataCenterParamFragment.newBundleArguments(
                item,
                productType,
                communicateWay,
                deviceInfo,
                bleDevice
            )

            // 跳转到数据中心参数页面
            nav().safeNavigate(
                R.id.action_global_to_dataCenterParamFragment,
                bundle
            )
        }
    }
}
