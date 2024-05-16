package com.shmedo.mcloudapp.device.ui.adme.fragment

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.adme.AdmeVoltageConfigEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeVoltageConfigInfo
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessageDialog
import com.shmedo.mcloudapp.databinding.FragmentAdmeThresholdBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.AdmeThresholdViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * @author：gonghe
 * @time: 2023/12/18
 * @desc:  ADME 阈值参数配置页面
 *
 */
class AdmeThresholdFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAdmeThresholdBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: AdmeThresholdViewModel
    private val iotParseManager: IOTParserManager by inject()


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_adme_threshold,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAdmeThresholdBinding
        binding.llToolbar.toolbar.title = "阈值参数"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        toolbarViewModel.toolbarIvActionVisible.set(mMessenger.admeDeviceMode.get() == "0")
        toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_device_param_edit)
        toolbarViewModel.toolbarTvActionText.set("取消")
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryData()
        }
    }

    private fun setEditable(editable: Boolean) {
        toolbarViewModel.toolbarIvActionVisible.set(!editable)
        toolbarViewModel.toolbarTvActionVisible.set(editable)
        mStates.isEditable.set(editable)
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onToolbarIvClick() {
            setEditable(true)
        }

        override fun onToolbarTvClick() {
            setEditable(false)
        }

        fun onSubmitClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun initSaveCommand() {
        if (mStates.driveStandardVoltageThreshold.get().isEmpty()) {
            showMessageDialog("请输入驱动器标压阈值!")
            return
        }
        try {
            val value = mStates.driveStandardVoltageThreshold.get().toDouble()
            if (value < 1) {
                showMessageDialog("驱动器标压阈值不能小于 1!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("驱动器标压阈值不能小于 1!")
            return
        }

        if (mStates.driveLowVoltageThreshold.get().isEmpty()) {
            showMessageDialog("请输入驱动器低压阈值!")
            return
        }
        try {
            val value = mStates.driveLowVoltageThreshold.get().toDouble()
            if (value < 1) {
                showMessageDialog("驱动器低压阈值不能小于 1!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("驱动器低压阈值不能小于 1!")
            return
        }

        if (mStates.driveUnderVoltageThreshold.get().isEmpty()) {
            showMessageDialog("请输入驱动器欠压阈值!")
            return
        }
        try {
            val value = mStates.driveUnderVoltageThreshold.get().toDouble()
            if (value < 1) {
                showMessageDialog("驱动器欠压阈值不能小于 1!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("驱动器欠压阈值不能小于 1!")
            return
        }

        if (mStates.driveUnderVoltageThreshold.get().isEmpty()) {
            showMessageDialog("请输入测斜仪标压阈值!")
            return
        }
        try {
            val value = mStates.driveUnderVoltageThreshold.get().toDouble()
            if (value < 1) {
                showMessageDialog("测斜仪标压阈值不能小于 1!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("测斜仪标压阈值不能小于 1!")
            return
        }

        if (mStates.driveUnderVoltageThreshold.get().isEmpty()) {
            showMessageDialog("请输入测斜仪低压阈值!")
            return
        }
        try {
            val value = mStates.driveUnderVoltageThreshold.get().toDouble()
            if (value < 1) {
                showMessageDialog("测斜仪低压阈值不能小于 1!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("测斜仪低压阈值不能小于 1!")
            return
        }

        if (mStates.driveUnderVoltageThreshold.get().isEmpty()) {
            showMessageDialog("请输入测斜仪欠压阈值!")
            return
        }
        try {
            val value = mStates.driveUnderVoltageThreshold.get().toDouble()
            if (value < 1) {
                showMessageDialog("测斜仪欠压阈值不能小于 1!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("测斜仪欠压阈值不能小于 1!")
            return
        }

        if (mStates.driveUnderVoltageThreshold.get().isEmpty()) {
            showMessageDialog("请输入钢丝绳长度!")
            return
        }
        try {
            val value = mStates.driveUnderVoltageThreshold.get().toDouble()

        } catch (ex: Exception) {
            showMessageDialog("请输入正确的钢丝绳长度!")
            return
        }

        val entity = AdmeVoltageConfigEntity(
            volt_power_standard = mStates.driveStandardVoltageThreshold.get(),
            volt_power_low = mStates.driveLowVoltageThreshold.get(),
            volt_power_under = mStates.driveUnderVoltageThreshold.get(),
            volt_sensor_standard = mStates.inclinometerStandardVoltageThreshold.get(),
            volt_sensor_low = mStates.inclinometerLowVoltageThreshold.get(),
            volt_sensor_under = mStates.inclinometerUnderVoltageThreshold.get(),
            rope_length = mStates.wireRopeLength.get(),
            antifdis = if (mStates.isAntifreezeSupport.get()) mStates.antifreezeDistance.get() else IOTConstants.NULL_KEY
        )
        commandItems.clear()
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_SET_VOLTAGE,
            entity.toCommandString()
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_SAVE_CONFIG_PARAM)
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_GET_VOLTAGE
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.ADME_MD_GET_VOLTAGE -> {
                val result = iotParseManager.parse<AdmeVoltageConfigInfo>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_VOLTAGE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询阈值参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        //设备版本不支持，隐藏编辑按钮
                        toolbarViewModel.toolbarIvActionVisible.set(!errMsg.contains("设备版本不支持"))
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initParamData(result.data)
                    }
                }
            }

            IOTCommandType.ADME_MD_SET_VOLTAGE -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置阈值参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            IOTCommandType.MD_SAVE_CONFIG_PARAM -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "保存出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("保存成功")
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initParamData(info: AdmeVoltageConfigInfo) {
        val decimalFormat = DecimalFormat("#.##", DecimalFormatSymbols(Locale.getDefault()))
        try {
            mStates.driveStandardVoltageThreshold.set(decimalFormat.format(info.volt_power_standard.toDouble()))
            mStates.driveLowVoltageThreshold.set(decimalFormat.format(info.volt_power_low.toDouble()))
            mStates.driveUnderVoltageThreshold.set(decimalFormat.format(info.volt_power_under.toDouble()))
            mStates.inclinometerStandardVoltageThreshold.set(decimalFormat.format(info.volt_sensor_standard.toDouble()))
            mStates.inclinometerLowVoltageThreshold.set(decimalFormat.format(info.volt_sensor_low.toDouble()))
            mStates.inclinometerUnderVoltageThreshold.set(decimalFormat.format(info.volt_sensor_under.toDouble()))

            decimalFormat.applyPattern("#.###")
            mStates.wireRopeLength.set(decimalFormat.format(info.rope_length.toDouble()))
            if (info.antifdis == IOTConstants.NULL_KEY) {
                mStates.isAntifreezeSupport.set(false)
            } else {
                mStates.isAntifreezeSupport.set(true)
                mStates.antifreezeDistance.set(info.antifdis)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}