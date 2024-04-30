package com.shmedo.mcloudapp.device.ui.adme.fragment

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.adme.AdmeWorkModeEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeWorkModeInfo
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessage
import com.shmedo.mcloudapp.databinding.FragmentAdmeAdvancedSettingBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.AdmeAdvancedSettingViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/1/4
 * @desc: ADME 设置页面
 *
 */
class AdmeAdvancedSettingFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAdmeAdvancedSettingBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: AdmeAdvancedSettingViewModel
    private val iotParseManager: IOTParserManager by inject()

    private val measureWorkModeList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_measure_work_mode) }


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_adme_advanced_setting,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAdmeAdvancedSettingBinding
        binding.llToolbar.toolbar.title = "设置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
    }

    override fun initData() {
        super.initData()
        mStates.measureWorkMode.set(measureWorkModeList[0])
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 测量工作模式选择
         */
        fun onMeasureWorkModeChooseClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            val selectedIndex = measureWorkModeList.indexOf(mStates.measureWorkMode.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", measureWorkModeList,
                    null, selectedIndex,
                    { _, text ->
                        mStates.measureWorkMode.set(text)
                        setWorkMode()
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onFirmWareSelectClick() {
            val bundle = BaseIOTDeviceFragment.newBundleArguments(
                productType,
                communicateWay,
                deviceInfo,
                bleDevice
            )
            nav().navigate(R.id.action_global_to_firmwareUpgradeFragment, bundle)
        }

        fun onResetClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            showMessage("确定恢复出厂设置吗？", "温馨提示", "确定", {
                commandItems.clear()
                val command = IOTCommandUtil.getCommand(IOTCommandType.RESET)
                commandItems.add(command)
                showLoadingDialog(StringUtils.getString(R.string.processing))
                sendCommandFromCmdList(isStartTimeoutJob = true)
            }, "取消")
        }
    }

    /**
     * 设置设备工作模式
     */
    private fun setWorkMode() {
        val entity = AdmeWorkModeEntity(
            when (mStates.measureWorkMode.get()) {
                measureWorkModeList[0] -> "0"
                measureWorkModeList[1] -> "1"
                measureWorkModeList[2] -> "2"
                else -> "3"
            }
        )
        commandItems.clear()
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_SET_WORK_MODE,
            entity.toCommandString()
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_SAVE_CONFIG_PARAM)
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(IOTCommandType.ADME_MD_GET_WORK_MODE)
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.loading))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.ADME_MD_GET_WORK_MODE -> {//获取设备的工作模式
                val result = iotParseManager.parse<AdmeWorkModeInfo>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_WORK_MODE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "获取工作模式出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        val workModeInfo: AdmeWorkModeInfo = result.data
                        mStates.measureWorkMode.set(
                            when (workModeInfo.workmode) {
                                "0" -> measureWorkModeList[0]
                                "1" -> measureWorkModeList[1]
                                "2" -> measureWorkModeList[2]
                                else -> measureWorkModeList[3]
                            }
                        )
                    }
                }
            }

            IOTCommandType.ADME_MD_SET_WORK_MODE -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置工作模式出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            IOTCommandType.RESET -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = StringUtils.getString(R.string.reset_failed) + result.message
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show(StringUtils.getString(R.string.device_reset_tip))
                        }
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

            else -> {}
        }
    }


    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

}