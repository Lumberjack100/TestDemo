package com.shmedo.mcloudapp.device.ui.adme.fragment

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.adme.AdmeMeterWheelEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeMeterWheelInfo
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessageDialog
import com.shmedo.mcloudapp.databinding.FragmentAdmeMeterWheelBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.AdmeMeterWheelViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * @author：gonghe
 * @time: 2023/12/13
 * @desc:  ADME 计米轮参数配置页面
 *
 */
class AdmeMeterWheelFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAdmeMeterWheelBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: AdmeMeterWheelViewModel
    private val iotParseManager: IOTParserManager by inject()


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_adme_meter_wheel,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAdmeMeterWheelBinding
        binding.llToolbar.toolbar.title = "计米轮参数"
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
        if (mStates.encoderLineNumber.get().isEmpty()) {
            showMessageDialog("请输入编码器线数!")
            return
        }
        try {
            val value: Int = mStates.encoderLineNumber.get().toInt()
            if (value < 1) {
                showMessageDialog("请输入正确的编码器线数!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的编码器线数!")
            return
        }

        if (mStates.outerDiameter.get().isEmpty()) {
            showMessageDialog("请输入外径!")
            return
        }
        try {
            val value: Int = mStates.outerDiameter.get().toInt()
            if (value < 1) {
                showMessageDialog("请输入正确的外径!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的外径!")
            return
        }

        if (mStates.upCorrectionParametersOne.get().isEmpty()) {
            showMessageDialog("请输入上拉一次修正参数!")
            return
        }
        try {
            val value = mStates.upCorrectionParametersOne.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的上拉一次修正参数!")
            return
        }

        if (mStates.upCorrectionParametersTwo.get().isEmpty()) {
            showMessageDialog("请输入上拉二次修正参数!")
            return
        }
        try {
            val value = mStates.upCorrectionParametersTwo.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的上拉二次修正参数!")
            return
        }

        if (mStates.upConstant.get().isEmpty()) {
            showMessageDialog("请输入上拉常数!")
            return
        }
        try {
            val value = mStates.upConstant.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的上拉常数!")
            return
        }
        if (mStates.upFilterCoefficient.get().isEmpty()) {
            showMessageDialog("请输入上拉滤波器系数!")
            return
        }
        if (!mStates.upFilterCoefficient.get().matches(Regex("[A-F0-9]"))) {
            showMessageDialog("上拉滤波器系数数值范围(0-F)!")
            return
        }
        if (mStates.downCorrectionParametersOne.get().isEmpty()) {
            showMessageDialog("请输入下放一次修正参数!")
            return
        }
        try {
            val value = mStates.downCorrectionParametersOne.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的下放一次修正参数!")
            return
        }

        if (mStates.downCorrectionParametersTwo.get().isEmpty()) {
            showMessageDialog("请输入下放二次修正参数!")
            return
        }
        try {
            val value = mStates.downCorrectionParametersTwo.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的下放二次修正参数!")
            return
        }

        if (mStates.downConstant.get().isEmpty()) {
            showMessageDialog("请输入下放常数!")
            return
        }
        try {
            val value = mStates.downConstant.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的下放常数!")
            return
        }
        if (mStates.downFilterCoefficient.get().isEmpty()) {
            showMessageDialog("请输入下放滤波器系数!")
            return
        }
        if (!mStates.downFilterCoefficient.get().matches(Regex("[A-F0-9]"))) {
            showMessageDialog("下放滤波器系数数值范围(0-F)!")
            return
        }
        val entity = AdmeMeterWheelEntity(
            enclinenum = mStates.encoderLineNumber.get(),
            outline = mStates.outerDiameter.get(),
            uptiona = mStates.upCorrectionParametersOne.get(),
            uptionb = mStates.upCorrectionParametersTwo.get(),
            upconstant = mStates.upConstant.get(),
            upfilter = mStates.upFilterCoefficient.get(),
            downtiona = mStates.downCorrectionParametersOne.get(),
            downtionb = mStates.downCorrectionParametersTwo.get(),
            downconstant = mStates.downConstant.get(),
            downfilter = mStates.downFilterCoefficient.get()
        )

        commandItems.clear()
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_SET_METER_WHEEL,
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
            IOTCommandType.ADME_MD_GET_METER_WHEEL
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.ADME_MD_GET_METER_WHEEL -> {
                val result = iotParseManager.parse<AdmeMeterWheelInfo>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_METER_WHEEL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询计米轮参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
//                        PopTip.show(errMsg).autoDismiss(4500).iconError()
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList{
                            binding.refreshLayout.finish()
                        }
                        initParamData(result.data)
                    }
                }
            }

            IOTCommandType.ADME_MD_SET_METER_WHEEL -> {//设置ADME的计米轮配置参数
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置计米轮参数出错: ${result.message}"
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

    private fun initParamData(info: AdmeMeterWheelInfo) {
        val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.getDefault()))
        try {
            mStates.encoderLineNumber.set(info.enclinenum)
            decimalFormat.applyPattern("#")
            info.outline.toDoubleOrNull()?.let {
                mStates.outerDiameter.set(decimalFormat.format(it))
            }

            decimalFormat.applyPattern("#.######")
            mStates.upCorrectionParametersOne.set(decimalFormat.format(info.uptiona.toDouble()))
            mStates.upCorrectionParametersTwo.set(decimalFormat.format(info.uptionb.toDouble()))
            mStates.upConstant.set(decimalFormat.format(info.upconstant.toDouble()))
            mStates.upFilterCoefficient.set(decimalFormat.format(info.upfilter.toDouble()))
            mStates.downCorrectionParametersOne.set(decimalFormat.format(info.downtiona.toDouble()))
            mStates.downCorrectionParametersTwo.set(decimalFormat.format(info.downtionb.toDouble()))
            mStates.downConstant.set(decimalFormat.format(info.downconstant.toDouble()))
            mStates.downFilterCoefficient.set(decimalFormat.format(info.downfilter.toDouble()))
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}