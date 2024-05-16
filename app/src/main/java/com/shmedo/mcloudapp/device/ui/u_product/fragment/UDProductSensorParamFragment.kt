package com.shmedo.mcloudapp.device.ui.u_product.fragment

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.common.MudLevelMeterSensorEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonCurrentStateInfo2
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentUDProductSensorParamBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.UDProductSensorParamViewModel
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessageDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * @author：gonghe
 * @time: 2024/4/26
 * @desc: 泥位计传感参数
 *
 */
class UDProductSensorParamFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUDProductSensorParamBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: UDProductSensorParamViewModel
    private val iotParseManager: IOTParserManager by inject()
    private val decimalFormat = DecimalFormat("#.###", DecimalFormatSymbols(Locale.getDefault()))

    private val measureIntervalList = arrayListOf("1", "2", "5", "10")
    private val averageTimesList = arrayListOf("2", "3", "5", "10")
    private val triggerCaptureLevelList =
        arrayListOf("无触发", "一级报警", "二级报警", "三级报警", "四级报警")
    private val imageResolutionList = arrayListOf("640x480", "1920x1080", "2560x1920")

    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_u_d_product_sensor_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUDProductSensorParamBinding
        binding.llToolbar.toolbar.title = "传感设置"
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
        resetParams()
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

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 测量间隔
         */
        fun onMeasureIntervalClick() {
            val selectedIndex = measureIntervalList.indexOf(mStates.measureInterval.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", measureIntervalList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.measureInterval.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 平均次数
         */
        fun onAverageTimesClick() {
            val selectedIndex = averageTimesList.indexOf(mStates.averageTimes.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", averageTimesList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.averageTimes.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 设置雷达初始值
         */
        fun onSetInitialValueClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            commandItems.clear()
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_SENSOR_INITIAL,
                "method=1&type=2"
            )
            commandItems.add(command)

            showLoadingDialog(StringUtils.getString(R.string.processing))
            sendCommandFromCmdList(isStartTimeoutJob = true)
        }

        /**
         * 触发抓拍级别
         */
        fun onTriggerCaptureLevelClick() {
            val selectedIndex = triggerCaptureLevelList.indexOf(mStates.triggerCaptureLevel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", triggerCaptureLevelList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.triggerCaptureLevel.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 图片分辨率
         */
        fun onImageResolutionClick() {
            val selectedIndex = imageResolutionList.indexOf(mStates.imageResolution.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", imageResolutionList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.imageResolution.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 恢复默认配置
         */
        fun onResetClick() {
            resetParams()
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun resetParams() {
        mStates.measureInterval.set(measureIntervalList[0])//测量间隔：1s、2s、5s、10s；默认为1s，当前置灰不可配置
        mStates.averageTimes.set(averageTimesList[2])
        mStates.triggerCaptureLevel.set(triggerCaptureLevelList[triggerCaptureLevelList.lastIndex])
        mStates.imageResolution.set(imageResolutionList[1])
    }

    private fun initSaveCommand() {
        if (mStates.installHeight.get().isEmpty()) {
            showMessageDialog("请输入安装高度!")
            return
        }
        try {
            val value = mStates.installHeight.get().toDouble()

        } catch (ex: Exception) {
            showMessageDialog("请输入正确的安装高度!")
            return
        }

        val entity = MudLevelMeterSensorEntity(
            height = mStates.installHeight.get(),
            //gap = mStates.measureInterval.get(),  当前置灰不可配置
            //times = mStates.averageTimes.get(),  当前置灰不可配置
            capture_level = triggerCaptureLevelList.indexOf(mStates.triggerCaptureLevel.get())
                .toString(),
            pixx = mStates.imageResolution.get().split("x")[0],
            pixy = mStates.imageResolution.get().split("x")[1]
        )
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_MUD_LEVEL_METER_SENSOR,
            entity.toCommandString()
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
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_DEVICE_STATUS
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DEVICE_STATUS -> {//
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MD_GET_DEVICE_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
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

            IOTCommandType.MD_SET_MUD_LEVEL_METER_SENSOR -> {//设置
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置参数出错: ${result.message}"
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

            IOTCommandType.MD_SET_SENSOR_INITIAL -> {//设置开关量传感器
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "更新雷达初始值出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("更新雷达初始值成功")
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initParamData(content: String) {
        launchWithViewLifecycle {
            try {
                val commonCurrentStateInfoList = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<List<CommonCurrentStateInfo2>>(content)
                } ?: return@launchWithViewLifecycle

                if (commonCurrentStateInfoList.isEmpty()) {
                    Toaster.show("数据为空")
                    return@launchWithViewLifecycle
                }
                val info = commonCurrentStateInfoList[0]
                info.height.toDoubleOrNull()?.let {
                    mStates.installHeight.set(decimalFormat.format(it))
                }
                //mStates.measureInterval.set(info.gap)
                //mStates.averageTimes.set(info.times)
                mStates.triggerCaptureLevel.set(if (info.capture_level.toInt() < triggerCaptureLevelList.size) triggerCaptureLevelList[info.capture_level.toInt()] else triggerCaptureLevelList.last())
                mStates.imageResolution.set("${info.pixx}x${info.pixy}")

            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}