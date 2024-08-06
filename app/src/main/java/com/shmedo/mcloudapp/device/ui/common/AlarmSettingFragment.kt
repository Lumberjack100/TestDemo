package com.shmedo.mcloudapp.device.ui.common

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.AlarmMonitorPointInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.AlarmSwitchInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentAlarmSettingBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.AlarmSettingViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.notNullKey
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ext.showLoadingDialog
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/4/25
 * @desc: 报警设置
 *
 */
class AlarmSettingFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAlarmSettingBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: AlarmSettingViewModel
    private val iotParseManager: IOTParserManager by inject()

    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

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
        binding.llToolbar.toolbar.title = "报警设置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryData()
        }
    }


    inner class ClickProxy : BaseClickProxy() {
        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
                return
            }
            mStates.isOpened.set(isChecked)
            disableOrEnableAlram(if (isChecked) "1" else "0")
        }

        /**
         * 测试一级报警
         */
        fun onTestFirstAlarmClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initTestAlarmCommand(1)
        }

        /**
         * 测试二级报警
         */
        fun onTestSecondAlarmClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initTestAlarmCommand(2)
        }

        /**
         * 测试三级报警
         */
        fun onTestThirdAlarmClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initTestAlarmCommand(3)
        }

        /**
         * 测试四级报警
         */
        fun onTestFourthAlarmClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initTestAlarmCommand(4)
        }

        /**
         * 参数设置
         */
        fun onGoToParamSettingClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            val bundle = BaseIOTDeviceFragment.newBundleArguments(
                productType,
                communicateWay,
                deviceInfo,
                bleDevice
            )
            nav().navigate(
                R.id.action_global_alarmParamSettingFragment,
                bundle
            )
        }
    }

    /**
     * 关闭或者打开报警
     */
    private fun disableOrEnableAlram(sw: String = "1") {
        commandItems.clear()
        val command =
            if (productType == ProductType.GNSS_M_1 || productType == ProductType.GNSS_M_2) IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_ALRAM_BROADCAST_CTRL,
                "sw=$sw"
            )
            else IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_ALRAM_BROADCAST_SWITCH,
                "sw=$sw"
            )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun initTestAlarmCommand(level: Int) {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_TEST_ALRAM_BROADCAST,
            "level=$level"
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()

        val command =
            if (productType == ProductType.GNSS_M_1 || productType == ProductType.GNSS_M_2)
                IOTCommandUtil.getCommand(
                    IOTCommandType.MD_GET_ALRAM_BROADCAST_CTRL
                )
            else IOTCommandUtil.getCommand(
                IOTCommandType.MD_GET_ALRAM_BROADCAST_SWITCH
            )

        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_ALRAM_BROADCAST_SWITCH -> {
                val result = iotParseManager.parse<AlarmSwitchInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_ALRAM_BROADCAST_SWITCH
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询参数出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initEnableAlarmData(result.data)
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
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询参数出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initAlarmMonitorPointData(result.data)
                    }
                }
            }

            IOTCommandType.MD_SET_ALRAM_BROADCAST_SWITCH -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg =
                            if (cmdStr.contains("sw=0")) "关闭出错: ${result.message}" else "打开出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("保存成功")
                        }
                    }
                }
            }

            IOTCommandType.MD_SET_ALRAM_BROADCAST_CTRL -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg =
                            if (cmdStr.contains("sw=0")) "关闭出错: ${result.message}" else "设置参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("保存成功")
                        }
                    }
                }
            }

            IOTCommandType.MD_TEST_ALRAM_BROADCAST -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "发送预警测试指令出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("预警测试成功")
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initEnableAlarmData(info: AlarmSwitchInfo) {
        try {
            mStates.isOpened.set(info.sw == "1")
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initAlarmMonitorPointData(info: AlarmMonitorPointInfo) {
        try {
            info.sw.notNullKey {
                mStates.isOpened.set(it == "1")
            }
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}