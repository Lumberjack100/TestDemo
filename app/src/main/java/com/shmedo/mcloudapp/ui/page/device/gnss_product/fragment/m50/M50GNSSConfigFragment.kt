package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.m50

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gnss_m.GNSSRawConfigEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gnss_m.ModuleParamConfigEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m.GNSSRawData
import com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m.ModuleParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentM50GnssConfigBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.M50GNSSConfigViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/1/22
 * @desc: 一体式自供电 GNSS 接收机(M50)GNSS配置页面
 */
class M50GNSSConfigFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentM50GnssConfigBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: M50GNSSConfigViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()
    
    // 采样率选项列表 - 根据需求文档："1秒"、"5秒"，默认值为"1秒"
    private val samplingRateList = arrayListOf("1秒", "5秒")
    private val samplingRateValueList = arrayListOf("1", "5") // 对应的实际值
    
    // 截至高度角选项列表 - 根据需求文档："5度"到"30度"，默认值为"5度"
    private val elevationAngleList = arrayListOf("5°", "10°", "15°", "20°", "25°", "30°")
    private val elevationAngleValueList = arrayListOf("5", "10", "15", "20", "25", "30") // 对应的实际值

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_m50_gnss_config, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentM50GnssConfigBinding
        binding.llToolbar.toolbar.title = "GNSS配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            handleBackByCheckDataModified()
        }
        registerOnBackPressedDispatcher { handleBackByCheckDataModified() }
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

    override fun initData() {
        super.initData()
        resetDefaultParams()
        // 保存初始状态
        mStates.saveInitialState()
    }

    private fun resetDefaultParams() {
        mStates.samplingRate.set(samplingRateList[0]) // 默认"1秒"
        mStates.elevationAngle.set(elevationAngleList[0]) // 默认"5度"
    }

    inner class ClickProxy : BaseClickProxy() {

        /** 选择采样率 */
        fun onSamplingRateSelected() {
            val selectedIndex = samplingRateList.indexOf(mStates.samplingRate.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", samplingRateList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.samplingRate.set(text)
                    },
                    0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /** 选择截至高度角 */
        fun onElevationAngleSelected() {
            val selectedIndex = elevationAngleList.indexOf(mStates.elevationAngle.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", elevationAngleList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.elevationAngle.set(text)
                    },
                    0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /** 恢复默认配置 */
        fun onResetClick() {
            resetDefaultParams()
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun initSaveCommand() {
        commandItems.clear()

        // 设置采样率指令
        val samplingRateIndex = samplingRateList.indexOf(mStates.samplingRate.get())
        val samplingRateValue = if (samplingRateIndex >= 0) samplingRateValueList[samplingRateIndex] else "1"
        val samplingRateEntity = GNSSRawConfigEntity(obs = samplingRateValue)
        val samplingRateCommand = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_SAMPLING_RATE,
            samplingRateEntity.toCommandString()
        )
        commandItems.add(samplingRateCommand)

        // 设置截至高度角指令
        val elevationAngleIndex = elevationAngleList.indexOf(mStates.elevationAngle.get())
        val elevationAngleValue = if (elevationAngleIndex >= 0) elevationAngleValueList[elevationAngleIndex] else "5"
        val elevationAngleEntity = ModuleParamConfigEntity(altitude_angle = elevationAngleValue)
        val elevationAngleCommand = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_ELEVATION_ANGLE,
            elevationAngleEntity.toCommandString()
        )
        commandItems.add(elevationAngleCommand)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()
        
        // 查询采样率
        val samplingRateCommand = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_SAMPLING_RATE)
        commandItems.add(samplingRateCommand)
        
        // 查询截至高度角
        val elevationAngleCommand = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_ELEVATION_ANGLE, "type=gnss")
        commandItems.add(elevationAngleCommand)
        
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun isTargetCommandType(commandType: IOTCommandType): Boolean =
        (commandType == IOTCommandType.MD_GET_SAMPLING_RATE) || 
        (commandType == IOTCommandType.MD_SET_SAMPLING_RATE) ||
        (commandType == IOTCommandType.MD_GET_ELEVATION_ANGLE) || 
        (commandType == IOTCommandType.MD_SET_ELEVATION_ANGLE)

    /**
     * 4G 下发指令响应失败
     */
    override fun doCmdResponseResultError(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        val isShowMessage = isTargetCommandType(IOTCommandUtil.extractCommandType(cmdStr))
        super.doCmdResponseResultError(
            cmdStr = cmdStr,
            errMsg = errMsg,
            isShowErrMsg = isShowMessage,
            isMessageDialog = isShowMessage
        )
    }

    /**
     * 4G 下发指令响应超时
     */
    override fun showNearbyCommunicationTimeoutAlert(
        cmdStr: String,
        isDismissLoadingDialog: Boolean,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean,
        errMsg: String
    ) {
        val isShowMessage = isTargetCommandType(IOTCommandUtil.extractCommandType(cmdStr))
        super.showNearbyCommunicationTimeoutAlert(
            cmdStr = cmdStr,
            isDismissLoadingDialog = isDismissLoadingDialog,
            isShowErrMsg = isShowMessage,
            isMessageDialog = isShowMessage,
            errMsg = errMsg
        )
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_SAMPLING_RATE -> {
                val result = iotParseManager.parse<GNSSRawData>(
                    cmdStr,
                    IOTCommandType.MD_GET_SAMPLING_RATE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询采样率出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList { 
                            if (commandItems.isEmpty()) {
                                binding.refreshLayout.finish()
                            }
                        }
                        initSamplingRateData(result.data)
                    }
                }
            }

            IOTCommandType.MD_GET_ELEVATION_ANGLE -> {
                val result = iotParseManager.parse<ModuleParam>(
                    cmdStr,
                    IOTCommandType.MD_GET_ELEVATION_ANGLE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询截至高度角出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList { 
                            if (commandItems.isEmpty()) {
                                binding.refreshLayout.finish()
                            }
                        }
                        initElevationAngleData(result.data)
                    }
                }
            }

            IOTCommandType.MD_SET_SAMPLING_RATE -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "采样率保存出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList { 
                            if (commandItems.isEmpty()) {
                                processNavigateUp()
                            }
                        }
                    }
                }
            }

            IOTCommandType.MD_SET_ELEVATION_ANGLE -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "截至高度角保存出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList { 
                            if (commandItems.isEmpty()) {
                                processNavigateUp()
                            }
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initSamplingRateData(rawData: GNSSRawData) {
        try {
            // 根据返回的 obs 值设置采样率显示
            val obsValue = rawData.obs
            val index = samplingRateValueList.indexOf(obsValue)
            if (index >= 0) {
                mStates.samplingRate.set(samplingRateList[index])
            } else {
                // 如果返回的值不在预定义列表中，使用默认值
                mStates.samplingRate.set(samplingRateList[0])
            }
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initElevationAngleData(moduleParam: ModuleParam) {
        try {
            // 根据返回的 altitude_angle 值设置截至高度角显示
            val altitudeAngleValue = moduleParam.altitude_angle.split(".")[0] // 去掉小数部分
            val index = elevationAngleValueList.indexOf(altitudeAngleValue)
            if (index >= 0) {
                mStates.elevationAngle.set(elevationAngleList[index])
            } else {
                // 如果返回的值不在预定义列表中，使用默认值
                mStates.elevationAngle.set(elevationAngleList[0])
            }

            // 保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    override fun handleBackByCheckDataModified() {
        if (mStates.isDataModified.value == true) {
            showExitConfirmationDialog()
            return
        }
        nav().navigateUp()
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}