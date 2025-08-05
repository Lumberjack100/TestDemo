package com.shmedo.mcloudapp.ui.page.device.common

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.AlarmMonitorPointInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.AlarmSwitchInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.FragmentAlarmSettingBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.notNullKey
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.AlarmSettingViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/4/25
 * @desc: 优化后的报警配置页面
 *
 * 优化特点：
 * 1. 使用新的通信架构，代码更简洁
 * 2. 统一的错误处理策略
 * 3. 响应驱动的指令执行
 * 4. 支持产品类型差异化处理
 * 5. 提供便捷的测试功能
 */
class AlarmSettingFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAlarmSettingBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: AlarmSettingViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_alarm_setting,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAlarmSettingBinding
        binding.llToolbar.toolbar.title = "报警配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_refresh_fail_warn))
                return@onRefresh
            }
            queryData()
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 查询数据 - 使用新架构的指令序列API
     */
    private fun queryData() {
        val commands =
            if (productType == ProductType.GNSS_M_1 || productType == ProductType.GNSS_M_2) {
                listOf(
                    // 获取设备状态，用于检查电台模块状态
                    IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS),
                    // 获取报警控制参数
                    IOTCommandUtil.getCommand(IOTCommandType.MD_GET_ALRAM_BROADCAST_CTRL)
                )
            } else {
                listOf(IOTCommandUtil.getCommand(IOTCommandType.MD_GET_ALRAM_BROADCAST_SWITCH))
            }

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig() // 查询失败显示Dialog
            )
        )
    }

    /**
     * 开启或关闭报警 - 使用新架构的简化API
     */
    private fun toggleAlarm(isEnabled: Boolean) {
        val sw = if (isEnabled) "1" else "0"
        val command =
            if (productType == ProductType.GNSS_M_1 || productType == ProductType.GNSS_M_2) {
                IOTCommandUtil.getCommand(IOTCommandType.MD_SET_ALRAM_BROADCAST_CTRL, "sw=$sw")
            } else {
                IOTCommandUtil.getCommand(IOTCommandType.MD_SET_ALRAM_BROADCAST_SWITCH, "sw=$sw")
            }

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    /**
     * 报警测试 - 使用单条指令发送
     */
    private fun testAlarm(level: Int) {
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_TEST_ALRAM_BROADCAST,
            "level=$level"
        )

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    /**
     * 处理指令响应 - 这是唯一需要实现的方法
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS -> {
                val result =
                    iotParseManager.parse<String>(cmdStr, IOTCommandType.QUERY_DEVICE_STATUS)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询设备状态出错: ${result.message}"
                        handleFailureResult(errMsg, isShowErrMsg = true)
                    }

                    is IOTCommandResult.Success -> {
                        initStatusInfo(result.data)
                    }
                }
            }

            IOTCommandType.MD_GET_ALRAM_BROADCAST_CTRL -> {
                val result = iotParseManager.parse<AlarmMonitorPointInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_ALRAM_BROADCAST_CTRL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is IOTCommandResult.Success -> {
                        initAlarmMonitorPointData(result.data)
                    }
                }
            }

            IOTCommandType.MD_GET_ALRAM_BROADCAST_SWITCH -> {
                val result = iotParseManager.parse<AlarmSwitchInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_ALRAM_BROADCAST_SWITCH
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is IOTCommandResult.Success -> {
                        initEnableAlarmData(result.data)
                    }
                }
            }

            IOTCommandType.MD_SET_ALRAM_BROADCAST_CTRL -> {
                val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = if (cmdStr.contains("sw=0")) {
                            "关闭出错: ${result.message}"
                        } else {
                            "数据保存出错: ${result.message}"
                        }
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        Toaster.show("数据保存成功")
                    }
                }
            }

            IOTCommandType.MD_SET_ALRAM_BROADCAST_SWITCH -> {
                val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = if (cmdStr.contains("sw=0")) {
                            "关闭出错: ${result.message}"
                        } else {
                            "打开出错: ${result.message}"
                        }
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        Toaster.show("数据保存成功")
                    }
                }
            }

            IOTCommandType.MD_TEST_ALRAM_BROADCAST -> {
                val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "发送预警测试指令出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        Toaster.show("预警测试成功")
                    }
                }
            }

            else -> {
                Timber.d("未处理的指令类型: ${IOTCommandUtil.extractCommandType(cmdStr)}")
            }
        }
    }

    /**
     * 初始化状态信息 - 处理M20S设备状态数据
     */
    private fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val stateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<CommonCurrentStateInfo>(content)
                }
                if (stateInfo != null) {
                    // 根据设备状态信息判断电台模块是否启用
                    val isRadioEnabled = stateInfo.self_check.uppercase().contains("RADIO:1")
                    mStates.isRadioEnable.set(isRadioEnabled)

                    addDeviceLogItem(
                        Log.INFO,
                        "电台模块状态: ${if (isRadioEnabled) "已启用" else "未启用"}"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
                // 默认设置为启用，避免UI异常
                mStates.isRadioEnable.set(true)
            }
        }
    }

    /**
     * 初始化报警监测点数据（GNSS产品）
     */
    private fun initAlarmMonitorPointData(info: AlarmMonitorPointInfo) {
        try {
            info.sw.notNullKey {
                mStates.isOpened.set(it == "1")
            }
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 初始化报警开关数据（通用产品）
     */
    private fun initEnableAlarmData(info: AlarmSwitchInfo) {
        try {
            mStates.isOpened.set(info.sw == "1")
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 点击事件处理
     */
    inner class ClickProxy : BaseClickProxy() {
        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
                return
            }
            mStates.isOpened.set(isChecked)
            toggleAlarm(isChecked)
        }

        /**
         * 测试一级报警
         */
        fun onTestFirstAlarmClick() {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            testAlarm(1)
        }

        /**
         * 测试二级报警
         */
        fun onTestSecondAlarmClick() {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            testAlarm(2)
        }

        /**
         * 测试三级报警
         */
        fun onTestThirdAlarmClick() {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            testAlarm(3)
        }

        /**
         * 测试四级报警
         */
        fun onTestFourthAlarmClick() {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            testAlarm(4)
        }

        /**
         * 参数设置
         */
        fun onGoToParamSettingClick() {
            if (!mStates.isOpened.get()) {
                Toaster.show("请先开启报警")
                return
            }
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            val bundle = BaseIOTDeviceFragment.newBundleArguments(
                productType,
                communicateWay,
                deviceInfo,
                bleDevice
            )
            nav().safeNavigate(
                R.id.action_global_to_alarmParamSettingFragment,
                bundle
            )
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}