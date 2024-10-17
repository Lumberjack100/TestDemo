package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentUdAltitudeParamBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.UDAltitudeParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.UDSensorParamViewModel
import org.koin.android.ext.android.inject

/**
 * @author：gonghe
 * @time: 2024/4/26
 * @desc: 一体化雷达泥位计海拔测量模式选择
 *
 */
class UDAltitudeParamFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUdAltitudeParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: UDAltitudeParamViewModel by viewModels()
    private val udSensorParamViewModel: UDSensorParamViewModel by activityViewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val modelList = arrayListOf("自动", "手动")


    override fun initViewModel() {
        super.initViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_ud_altitude_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUdAltitudeParamBinding
        toolbarViewModel.toolbarTitleText.set("海拔高度")
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
    }

    override fun initData() {
        super.initData()
        resetDefaultParams()
    }

    private fun resetDefaultParams() {
        mStates.altitudeMeasureMode.set(udSensorParamViewModel.altitudeMeasureMode.get())
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 选择模式
         */
        fun onModelChooseClick() {
            val selectedIndex = modelList.indexOf(mStates.altitudeMeasureMode.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", modelList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.altitudeMeasureMode.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 恢复默认配置
         */
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

        if (mStates.altitudeMeasureMode.get() == "自动") {
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_MUD_LEVEL_METER_ALTITUDE_MEASURE_MODE,
                "alt_get_mode=0"
            )
            commandItems.add(command)
        }else{
            if (mStates.altitude.get().isEmpty()) {
                showMessageDialog("请输入海拔高度!")
                return
            }
            try {
                val value = mStates.altitude.get().toDouble()
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的海拔高度!")
                return
            }
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_MUD_LEVEL_METER_ALTITUDE_MEASURE_MODE,
                "alt_get_mode=1&alt=${mStates.altitude.get()}"
            )
            commandItems.add(command)
        }
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_SET_MUD_LEVEL_METER_ALTITUDE_MEASURE_MODE -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("数据保存成功")
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}