package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ur

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.das.DasIOSensorEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTRainStation
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasIOSensorInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.FragmentUrProductSensorParamBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.URProductSensorParamViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * @author：gonghe
 * @time: 2024/4/26
 * @desc: 一体式雨量计传感参数
 *
 */
class URSensorParamFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUrProductSensorParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: URProductSensorParamViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()
    private val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.getDefault()))

    private val rainResolutionList by lazy { Utils.getApp().resources.getStringArray(R.array.rain_value) }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_ur_product_sensor_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUrProductSensorParamBinding
        binding.llToolbar.toolbar.title = "传感配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            handleBackByCheckDataModified()
        }
        registerOnBackPressedDispatcher {
            handleBackByCheckDataModified()
        }
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_refresh_fail_warn))
                finishRefresh()
                return@onRefresh
            }
            queryData()
        }
    }

    override fun initData() {
        super.initData()
        resetDefaultParams()
        //添加这行来保存初始状态
        mStates.saveInitialState()
    }

    private fun resetDefaultParams() {
        mStates.rainResolution.set(rainResolutionList[0])
    }

    inner class ClickProxy : BaseClickProxy() {
        fun onChooseResolutionClick() {
            val selectedIndex = rainResolutionList.indexOf(mStates.rainResolution.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", rainResolutionList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.rainResolution.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 清空雨量值
         */
        fun onRainValueClearClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_SENSOR_INITIAL,
                "method=1&type=0"
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
         * 恢复默认配置
         */
        fun onResetClick() {
            resetDefaultParams()
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun initSaveCommand() {
        val entity = DasIOSensorEntity(
            type = "1",
            value = mStates.rainResolution.get(),
            min_time = IOTConstants.NULL_KEY,
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_SET_IO_SENSOR_INFO,
            entity.toCommandString()
        )

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_GET_IO_SENSOR_INFO
        )

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig() // 查询失败显示Dialog
            )
        )
    }

    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.DAS_MD_GET_IO_SENSOR_INFO -> {//查询开关量传感器参数
                val result = iotParseManager.parse<DasIOSensorInfo>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_IO_SENSOR_INFO
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        initParamData(result.data)
                    }
                }
            }

            IOTCommandType.DAS_MD_SET_IO_SENSOR_INFO -> {//设置开关量传感器
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        if (!isCommunicationExecuting()) {
                            processNavigateUp()
                        }
                    }
                }
            }

            IOTCommandType.MD_SET_SENSOR_INITIAL -> {//设置开关量传感器
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "雨量值清零出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        if (!isCommunicationExecuting()) {
                            Toaster.show("雨量值清零成功")
                        }
                    }
                }
            }

            else -> {

            }
        }
    }

    private fun initParamData(ioSensorInfo: DasIOSensorInfo) {
        try {
            when (IOTRainStation.Companion.value(ioSensorInfo.type)) {
                IOTRainStation.RAIN_OPEN -> {//1：雨量站模式
                    ioSensorInfo.value.toDoubleOrNull()?.let { value ->
                        mStates.rainResolution.set(decimalFormat.format(value))
                    }
                }

                else -> {

                }
            }

            // 保存初始状态，用于后续修改检测
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.Forest.e(e)
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

}