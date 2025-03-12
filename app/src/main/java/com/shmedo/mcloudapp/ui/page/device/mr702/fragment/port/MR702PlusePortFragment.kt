package com.shmedo.mcloudapp.ui.page.device.mr702.fragment.port

import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRPulsePortParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRPulsePortParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentMr702PlusePortBinding
import com.shmedo.mcloudapp.extensions.getActivityScopeViewModel
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702PlusePortViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702PortHomeViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/2/25
 * @desc: 脉冲端口
 *
 */
class MR702PlusePortFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702PlusePortBinding
    private lateinit var mInterfaceHomeViewModel: MR702PortHomeViewModel
    private val mStates: MR702PlusePortViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val modeList by lazy { Utils.getApp().resources.getStringArray(R.array.pulse_mode_value) }

    override fun initViewModel() {
        super.initViewModel()
        mInterfaceHomeViewModel = getActivityScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_pluse_port, BR.stateVM, mStates)
            .addBindingParam(BR.homeVM, mInterfaceHomeViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702PlusePortBinding
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
            queryInfo()
        }
    }

    override fun initData() {
        super.initData()
        mStates.workMode.set(modeList[0])
        mStates.pulseResolution.set("1")
        mStates.debounceCoefficient.set("5")
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (isBleDisconnected()) {
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

        fun onChooseModeClick() {
            val selectedIndex = modeList.indexOf(mStates.workMode.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择功能模式", modeList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.workMode.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onSubmitClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun closeSwitch() {
        commandItems.clear()
        val entity = MRPulsePortParamEntity(
            switch = "0"
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_SET_PULSE_PORT_PARAM,
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
        if (mStates.pulseResolution.get().isEmpty()) {
            showMessageDialog("请输入脉冲分辨率")
            return
        }
        if (mStates.debounceCoefficient.get().isEmpty()) {
            showMessageDialog("请输入消抖系数")
            return
        }

        val pulseResolution = mStates.pulseResolution.get().toIntOrNull()
        if (pulseResolution == null || pulseResolution <= 0 || pulseResolution > 9999) {
            showMessageDialog("脉冲分辨率必须是大于0且不超过9999的整数")
            return
        }

        val debounceCoefficient = mStates.debounceCoefficient.get().toIntOrNull()
        if (debounceCoefficient == null || debounceCoefficient <= 0 || debounceCoefficient > 60) {
            showMessageDialog("消抖系数必须是大于0且不超过60的整数")
            return
        }

        val entity = MRPulsePortParamEntity(
            switch = "1",
            workmode = modeList.indexOf(mStates.workMode.get()).toString(),
            dryaccuracy = mStates.pulseResolution.get(),
            dryelim = mStates.debounceCoefficient.get()
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_SET_PULSE_PORT_PARAM,
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

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_PULSE_PORT_PARAM)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_PULSE_PORT_PARAM -> {
                val result = iotParseManager.parse<MRPulsePortParam>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_PULSE_PORT_PARAM
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg)
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

            IOTCommandType.MD_MR_SET_PULSE_PORT_PARAM -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
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

    private fun initParamData(pulsePortParam: MRPulsePortParam) {
        try {
            mStates.isOpened.set(pulsePortParam.switch == "1")
            pulsePortParam.workmode.toIntOrNull()?.let {
                if (it in modeList.indices) {
                    mStates.workMode.set(modeList[it])
                }
            }
            mStates.pulseResolution.set(pulsePortParam.dryaccuracy)
            mStates.debounceCoefficient.set(pulsePortParam.dryelim)
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    companion object {
        fun newInstance() = MR702PlusePortFragment()
    }
}