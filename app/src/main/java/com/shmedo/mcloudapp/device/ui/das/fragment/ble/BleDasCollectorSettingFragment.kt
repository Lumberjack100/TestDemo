package com.shmedo.mcloudapp.device.ui.das.fragment.ble

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.model.das.DasCollectorInfo
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.enums.SaveConfigMode
import com.shmedo.lib.device.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.device.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.device.base.md_cmd.utils.MDCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentDasCollectorSettingBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.DasCollectorSettingViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessageDialog
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * 创建者：gonghe
 * 创建时间：2024/4/11
 * 描述： TODO
 */
class BleDasCollectorSettingFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasCollectorSettingBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: DasCollectorSettingViewModel
    private val mdParseManager: MDParserManager by inject()
    private val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.getDefault()))

    private var collectorModel = "-1"

    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_das_collector_setting,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDasCollectorSettingBinding
        binding.llToolbar.toolbar.title = "采集器参数"
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

    override fun initData() {
        super.initData()
        arguments?.let {
            collectorModel = it.getString(COLLECTOR_MODEL, "-1")
        }
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryCollectorInfo()
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun initSaveCommand() {
        if (mStates.collectorAddress.get().isEmpty()) {
            showMessageDialog("请输入采集器地址!")
            return
        }
        try {
            val value = mStates.collectorAddress.get().toDouble()
            if (value < 0 || value > 255) {
                showMessageDialog("采集器地址数值范围[0,255]!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的采集器地址!")
            return
        }
        if (mStates.solvingInterval.get().isEmpty()) {
            showMessageDialog("请输入解算间隔!")
            return
        }
        if (mStates.standbyTime.get().isEmpty()) {
            showMessageDialog("请输入待机时长!")
            return
        }
        if (mStates.collectionInterval.get().isEmpty()) {
            showMessageDialog("请输入采集间隔!")
            return
        }
        if (mStates.isShowSensitivity.get()) {
            if (mStates.sensitivity.get().isEmpty()) {
                showMessageDialog("请输入灵敏度!")
                return
            }
            try {
                val value = mStates.sensitivity.get().toDouble()
                if (value < 30 || value > 150) {
                    showMessageDialog("灵敏度数值范围[30,150]!")
                    return
                }
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的灵敏度!")
                return
            }
        }

        commandItems.clear()
        var command = MDCommandUtil.getCommand(
            MDCommandType.SET_COLLECTOR_ADDRESS,
            mStates.collectorAddress.get()
        )
        commandItems.add(command)

        command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_SOLUTION_FREQUENCY,
            "$collectorModel${MDCommandUtil.formatStringFour(mStates.solvingInterval.get())}"
        )
        commandItems.add(command)

        command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_STANDBY_TIME,
            "$collectorModel${MDCommandUtil.formatStringFour(mStates.standbyTime.get())}"
        )
        commandItems.add(command)

        command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_FREQUENCY,
            "$collectorModel${MDCommandUtil.formatStringFive(mStates.collectionInterval.get())}"
        )
        commandItems.add(command)

        if (mStates.isShowSensitivity.get()) {
            command = MDCommandUtil.getCommand(
                MDCommandType.SET_COLLECTOR_SENSITIVITY,
                mStates.sensitivity.get()
            )
            commandItems.add(command)
        }

        command = MDCommandUtil.getCommand(
            MDCommandType.SAVE_CONFIG_INFO,
            SaveConfigMode.SAVE_NO_REBOOT.toString()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 查询采集器配置信息
     */
    private fun queryCollectorInfo() {
        commandItems.clear()
        val command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_CONFIG, collectorModel
        )
        commandItems.add(command)
        Timber.d("查询采集器配置信息===%s", command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.COLLECTOR_CONFIG -> {
                val result = mdParseManager.parse<DasCollectorInfo>(
                    cmdStr,
                    MDCommandType.COLLECTOR_CONFIG
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询采集器参数出错"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initCollectorInfo(result.data)
                    }
                }
            }

            MDCommandType.SET_COLLECTOR_ADDRESS -> {//
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "采集器地址配置错误!"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            MDCommandType.COLLECTOR_SOLUTION_FREQUENCY -> {//
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "采集器解算间隔配置错误!"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            MDCommandType.COLLECTOR_STANDBY_TIME -> {//
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "采集器待机时长配置错误!"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            MDCommandType.COLLECTOR_FREQUENCY -> {//
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "采集器采集间隔配置错误!"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            MDCommandType.SET_COLLECTOR_SENSITIVITY -> {//
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "采集器灵敏度配置错误!"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            MDCommandType.SAVE_CONFIG_INFO -> {//
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "保存出错!"
                        Timber.e("$errMsg: ${result.message}")
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

            else -> {}
        }
    }

    private fun initCollectorInfo(collectorInfo: DasCollectorInfo) {
        try {
            mStates.infoWrapper.set(collectorInfo)
            mStates.collectorAddress.set(collectorInfo.addr)
            mStates.solvingInterval.set(collectorInfo.calcgap)
            mStates.standbyTime.set(collectorInfo.standbygap)
            mStates.collectionInterval.set(collectorInfo.collgap)
            mStates.sensitivity.set(collectorInfo.sensitivity)
            mStates.isShowSensitivity.set(collectorInfo.sensitivity != IOTConstants.NULL_KEY)
            if (mStates.isShowSensitivity.get()) {
                collectorInfo.sensitivity.toDoubleOrNull()?.let {
                    mStates.sensitivity.set(decimalFormat.format(it))
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        private const val COLLECTOR_MODEL = "collector_model"
        fun newBundleArguments(
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            collectorModel: String,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putString(COLLECTOR_MODEL, collectorModel)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}