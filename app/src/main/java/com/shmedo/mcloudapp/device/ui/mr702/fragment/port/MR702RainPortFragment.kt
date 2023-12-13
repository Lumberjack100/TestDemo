package com.shmedo.mcloudapp.device.ui.mr702.fragment.port

import android.os.Bundle
import android.widget.CompoundButton
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.lxj.xpopup.XPopup
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr.MRRainGaugeParamEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRRainGaugeParam
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.common.ext.showMessage
import com.shmedo.mcloudapp.databinding.FragmentMr702RainPortBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.MR702PortHomeViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.MR702RainPortViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class MR702RainPortFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentMr702RainPortBinding by lazy { getBinding() as FragmentMr702RainPortBinding }
    private val mInterfaceHomeViewModel: MR702PortHomeViewModel by activityViewModels()
    private val mStates: MR702RainPortViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val rainResolutionList by lazy { Utils.getApp().resources.getStringArray(R.array.rain_value) }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_rain_port, BR.stateVM, mStates)
            .addBindingParam(BR.homeVM, mInterfaceHomeViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
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
            queryInfo()
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
                return
            }
            mStates.isOpened.set(isChecked)
            if (!isChecked) {
                showMessage("确定要关闭吗？", "温馨提示", "确定", {
                    closeSwitch()
                }, "取消", {
                    mStates.isOpened.set(true)
                    (button as SwitchButton).setCheckedImmediatelyNoEvent(true)
                })
            }
        }

        fun onChooseResolutionClick() {
            val selectedIndex = rainResolutionList.indexOf(mStates.rainResolution.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择分辨率", rainResolutionList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.rainResolution.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onSubmitClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun closeSwitch() {
        commandItems.clear()
        val entity = MRRainGaugeParamEntity(
            switch = "0"
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_SET_RAIN_GAUGE_PORT_PARAM,
            entity.toCommandString()
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 保存采集参数
     */
    private fun initSaveCommand() {
        commandItems.clear()
        if (mStates.debounceCoefficient.get().isEmpty()) {
            Toaster.show("请输入消抖系数")
            return
        }
        val entity = MRRainGaugeParamEntity(
            switch = "1",
            rainaccuracy = mStates.debounceCoefficient.get(),
            rainelim = mStates.rainResolution.get()
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_SET_RAIN_GAUGE_PORT_PARAM,
            entity.toCommandString()
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_RAIN_GAUGE_PORT_PARAM)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_RAIN_GAUGE_PORT_PARAM -> {
                val result = iotParseManager.parse<MRRainGaugeParam>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_RAIN_GAUGE_PORT_PARAM
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
                        sendCommandFromCmdList()
                        binding.refreshLayout.finish()
                        initParamData(result.data)
                    }
                }
            }

            IOTCommandType.MD_MR_SET_RAIN_GAUGE_PORT_PARAM -> {
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

            else -> {}
        }
    }

    private fun initParamData(rainGaugeParam: MRRainGaugeParam) {
        try {
            mStates.status.set(if (rainGaugeParam.status == "1") "已接入" else "未接入")
            binding.sensorSB.setCheckedImmediatelyNoEvent(rainGaugeParam.switch == "1")
            mStates.isOpened.set(rainGaugeParam.switch == "1")
            mStates.rainResolution.set(rainGaugeParam.rainelim)
            mStates.debounceCoefficient.set(rainGaugeParam.rainaccuracy)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun newInstance() = MR702RainPortFragment()
    }
}