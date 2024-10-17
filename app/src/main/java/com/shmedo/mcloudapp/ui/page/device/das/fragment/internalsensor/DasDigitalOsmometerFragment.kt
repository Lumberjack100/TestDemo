package com.shmedo.mcloudapp.ui.page.device.das.fragment.internalsensor

import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.das.DasDigitalPiezometerEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasDigitalPiezometerInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.databinding.FragmentDasDigitalOsmometerBinding
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.DasDigitalOsmometerViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class DasDigitalOsmometerFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasDigitalOsmometerBinding
    private lateinit var mStates: DasDigitalOsmometerViewModel
    private val iotParseManager: IOTParserManager by inject()
    private val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.getDefault()))


    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_das_digital_osmometer,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDasDigitalOsmometerBinding
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

    inner class ClickProxy : BaseClickProxy() {
        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
                return
            }
            mStates.isOpened.set(isChecked)
            if (!isChecked) {
                disableDigitalPiezometer()
            }
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

    /**
     * 关闭水位计
     */
    private fun disableDigitalPiezometer() {
        val entity = DasDigitalPiezometerEntity(
            sw = "0"
        )
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_SET_DIGITAL_PIEZOMETER_INFO,
            entity.toCommandString()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun initSaveCommand() {
        if (mStates.address.get().isEmpty()) {
            showMessageDialog("请输入水位计地址!")
            return
        }
        try {
            val value = mStates.address.get().toDouble()
            if (value < 0 || value > 255) {
                showMessageDialog("水位计地址数值范围[0,255]!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的水位计地址!")
            return
        }

        if (mStates.triggerValue.get().isEmpty()) {
            showMessageDialog("请输入水位报警值!")
            return
        }
        try {
            val value = mStates.triggerValue.get().toInt()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的水位报警值!")
            return
        }

        if (mStates.correctValue.get().isEmpty()) {
            showMessageDialog("请输入水深修正值!")
            return
        }
        try {
            val value = mStates.correctValue.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的水深修正值!")
            return
        }
        if (mStates.wireRopeLength.get().isEmpty()) {
            showMessageDialog("请输入水位计绳长!")
            return
        }
        try {
            val value = mStates.installElevation.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入安装高程值!")
            return
        }

        try {
            val value = mStates.installElevation.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的安装高程值!")
            return
        }

        val entity = DasDigitalPiezometerEntity(
            sw = "1",
            addr = mStates.address.get(),
            threshold = mStates.triggerValue.get(),
            corrval = mStates.correctValue.get(),
            ropelen = mStates.wireRopeLength.get(),
            tubealti = mStates.installElevation.get()
        )
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_SET_DIGITAL_PIEZOMETER_INFO,
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
            IOTCommandType.DAS_MD_GET_DIGITAL_PIEZOMETER_INFO
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.DAS_MD_GET_DIGITAL_PIEZOMETER_INFO -> {//
                val result = iotParseManager.parse<DasDigitalPiezometerInfo>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_DIGITAL_PIEZOMETER_INFO
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询数字水位计参数出错: ${result.message}"
                        handleFailureResult("$errMsg: ${result.message}")
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

            IOTCommandType.DAS_MD_SET_DIGITAL_PIEZOMETER_INFO -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置数字水位计参数出错: ${result.message}"
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

    private fun initParamData(digitalPiezometerInfo: DasDigitalPiezometerInfo) {
        try {
            mStates.isOpened.set(digitalPiezometerInfo.sw == "1")
            mStates.address.set(digitalPiezometerInfo.addr)
            mStates.triggerValue.set(digitalPiezometerInfo.threshold)
            mStates.correctValue.set(digitalPiezometerInfo.corrval)
            mStates.wireRopeLength.set(digitalPiezometerInfo.ropelen)
            mStates.installElevation.set(digitalPiezometerInfo.tubealti)

            decimalFormat.applyPattern("#.###")
            digitalPiezometerInfo.corrval.toDoubleOrNull()?.let {
                mStates.correctValue.set(decimalFormat.format(it))
            }
            digitalPiezometerInfo.ropelen.toDoubleOrNull()?.let {
                mStates.wireRopeLength.set(decimalFormat.format(it))
            }
            digitalPiezometerInfo.tubealti.toDoubleOrNull()?.let {
                mStates.installElevation.set(decimalFormat.format(it))
            }
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    companion object {
        fun newInstance() = DasDigitalOsmometerFragment()
    }
}