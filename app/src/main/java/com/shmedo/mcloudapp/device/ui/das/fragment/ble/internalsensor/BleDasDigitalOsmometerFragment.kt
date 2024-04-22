package com.shmedo.mcloudapp.device.ui.das.fragment.ble.internalsensor

import android.os.Bundle
import android.widget.CompoundButton
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.enums.MDOsmometerStatus
import com.shmedo.lib.device.base.md_cmd.enums.SaveConfigMode
import com.shmedo.lib.device.base.md_cmd.model.das.MDDasDigitalPiezometerInfo
import com.shmedo.lib.device.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.device.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.device.base.md_cmd.utils.MDCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessageDialog
import com.shmedo.mcloudapp.databinding.FragmentDasDigitalOsmometerBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.das.fragment.ble.BaseMDDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.DasDigitalOsmometerViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * 创建者：gonghe
 * 创建时间：2024/4/16
 * 描述： TODO
 */
class BleDasDigitalOsmometerFragment : BaseMDDeviceFragment() {
    private lateinit var binding: FragmentDasDigitalOsmometerBinding
    private lateinit var mStates: DasDigitalOsmometerViewModel
    private val mdParseManager: MDParserManager by inject()
    val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.getDefault()))

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
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryData()
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
            enableOrDisableDigitalPiezometer(isChecked)
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

    /**
     * 水位计
     */
    private fun enableOrDisableDigitalPiezometer(isChecked: Boolean) {
        commandItems.clear()
        val command = MDCommandUtil.getCommand(
            MDCommandType.DIGITAL_OSMOMETER_FUNCTION,
            if(isChecked) MDOsmometerStatus.OSMOMETER_OPEN.toString() else MDOsmometerStatus.OSMOMETER_CLOSE.toString()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendMDCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun initSaveCommand() {
        commandItems.clear()

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
        var command = MDCommandUtil.getCommand(
            MDCommandType.SET_OSMOMETER_ADDRESS,
            mStates.address.get()
        )
        commandItems.add(command)


        if (mStates.wrapInfoBackUp.get().depthTrigger != mStates.triggerValue.get()) {
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

            command = MDCommandUtil.getCommand(
                MDCommandType.SET_OSMOMETER_TRIGGER,
                "${mStates.triggerValue.get()},10" //深度触发值,温度触发值
            )
            commandItems.add(command)
        }

        if (mStates.wrapInfoBackUp.get().depthCorrect != mStates.correctValue.get()) {
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

            command = MDCommandUtil.getCommand(
                MDCommandType.SET_OSMOMETR_CORRECT,
                "${mStates.correctValue.get()},10" //深度修正值,温度修正值
            )
            commandItems.add(command)
        }
        if (mStates.wrapInfoBackUp.get().wireRopeLength != mStates.wireRopeLength.get()) {
            if (mStates.wireRopeLength.get().isEmpty()) {
                showMessageDialog("请输入水位计绳长!")
                return
            }
            try {
                val value = mStates.wireRopeLength.get().toDouble()
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的水位计绳长!")
                return
            }
            command = MDCommandUtil.getCommand(
                MDCommandType.SET_CORD_LENGTH,
                mStates.wireRopeLength.get()
            )
            commandItems.add(command)
        }
        if (mStates.wrapInfoBackUp.get().installElevation != mStates.installElevation.get()) {
            if (mStates.installElevation.get().isEmpty()) {
                showMessageDialog("请输入安装高程值!")
                return
            }
            try {
                val value = mStates.installElevation.get().toDouble()
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的安装高程值!")
                return
            }
            command = MDCommandUtil.getCommand(
                MDCommandType.SET_OSMOMETR_NOZZEL_HEIGHT,
                mStates.installElevation.get()
            )
            commandItems.add(command)
        }

        command = MDCommandUtil.getCommand(
            MDCommandType.SAVE_CONFIG_INFO,
            SaveConfigMode.SAVE_NO_REBOOT.toString()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendMDCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()
        val command = MDCommandUtil.getCommand(
            MDCommandType.QUERY_OSMOMETER_PARAMETER
        )
        commandItems.add(command)
        Timber.d("查询数字水位计配置信息===%s", command)
        sendMDCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.QUERY_OSMOMETER_PARAMETER -> {
                val result = mdParseManager.parse<MDDasDigitalPiezometerInfo>(
                    cmdStr,
                    MDCommandType.QUERY_OSMOMETER_PARAMETER
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询数字水位计参数出错"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendMDCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initDigitalOsmometerInfo(result.data)
                    }
                }
            }

            MDCommandType.DIGITAL_OSMOMETER_FUNCTION -> {//开启/关闭数字水位计功能 401
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg =
                            if (cmdStr.contains("4011")) "开启数字水位计错误"
                            else "关闭数字水位计错误" //4012
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendMDCommandFromCmdList()
                    }
                }
            }

            MDCommandType.SET_OSMOMETER_ADDRESS -> {//设置数字水位计地址 402
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "地址配置错误!"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendMDCommandFromCmdList()
                    }
                }
            }

            MDCommandType.SET_OSMOMETER_TRIGGER -> {//设置数字水位计水位报警值 403
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "水位报警值配置错误!"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendMDCommandFromCmdList()
                    }
                }
            }

            MDCommandType.SET_OSMOMETR_CORRECT -> {//设置数字水位计水深修正值 404
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "水深修正值配置错误!"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendMDCommandFromCmdList()
                    }
                }
            }

            MDCommandType.SET_CORD_LENGTH -> {//设置数字水位计绳长 405
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "绳长配置错误!"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendMDCommandFromCmdList()
                    }
                }
            }

            MDCommandType.SET_OSMOMETR_NOZZEL_HEIGHT -> {//设置数字水位计安装高程 406
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "安装高程配置错误!"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendMDCommandFromCmdList()
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
                        sendMDCommandFromCmdList {
                            Toaster.show("保存成功")
                        }
                    }
                }
            }

            else -> {}
        }
    }

    private fun initDigitalOsmometerInfo(digitalPiezometerInfo: MDDasDigitalPiezometerInfo) {
        try {
            MDOsmometerStatus.value(digitalPiezometerInfo.osmometerStatus).let {
                mStates.isOpened.set(it == MDOsmometerStatus.OSMOMETER_OPEN)
            }
            mStates.address.set(digitalPiezometerInfo.osmometerAddress)
            mStates.triggerValue.set(digitalPiezometerInfo.depthTrigger)
            mStates.correctValue.set(digitalPiezometerInfo.depthCorrect)
            mStates.wireRopeLength.set(digitalPiezometerInfo.wireRopeLength)
            mStates.installElevation.set(digitalPiezometerInfo.installElevation)

            decimalFormat.applyPattern("#.###")
            digitalPiezometerInfo.depthCorrect.toDoubleOrNull()?.let {
                mStates.correctValue.set(decimalFormat.format(it))
            }
            digitalPiezometerInfo.wireRopeLength.toDoubleOrNull()?.let {
                mStates.wireRopeLength.set(decimalFormat.format(it))
            }
            digitalPiezometerInfo.installElevation.toDoubleOrNull()?.let {
                mStates.installElevation.set(decimalFormat.format(it))
            }

            mStates.wrapInfoBackUp.get().apply {
                osmometerStatus = digitalPiezometerInfo.osmometerStatus
                osmometerAddress = mStates.address.get()
                depthTrigger = mStates.triggerValue.get()
                depthCorrect = mStates.correctValue.get()
                wireRopeLength = mStates.wireRopeLength.get()
                installElevation = mStates.installElevation.get()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun newInstance() = BleDasDigitalOsmometerFragment()
    }
}