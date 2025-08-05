package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.das.DasDigitalPiezometerEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasDigitalPiezometerInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.enums.MDOsmometerStatus
import com.shmedo.lib.cmd.base.md_cmd.model.das.MDDasDigitalPiezometerInfo
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentDasDigitalOsmometerBinding
import com.shmedo.mcloudapp.extensions.formatDoubleValue
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.DasDigitalOsmometerViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/6/17
 * @desc: 物联网采集器(DAS)数字式水位计参数配置页面 - 支持4G和蓝牙两种通讯方式
 *
 */
class DasDigitalOsmometerFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasDigitalOsmometerBinding
    private val mStates: DasDigitalOsmometerViewModel by viewModels()
    private val mdParseManager: MDParserManager by inject()
    private val iotParseManager: IOTParserManager by inject()


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
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_refresh_fail_warn))
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
        mStates.isOpened.set(false)
        mStates.address.set("1")
        mStates.triggerValue.set("100")
        mStates.correctValue.set("0")
        mStates.wireRopeLength.set("0")
        mStates.installElevation.set("0")
    }

    inner class ClickProxy : BaseClickProxy() {

        /** 恢复默认配置 */
        override fun onResetButtonClick() {
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
        // 参数验证
        if (mStates.isOpened.get() && !validateInputs()) {
            return
        }

        if (communicateWay == BleConnect) {
            initBleSaveCommand()
        } else {
            init4GSaveCommand()
        }
    }

    /** 蓝牙通讯模式保存指令 */
    private fun initBleSaveCommand() {
        commandItems.clear()

        var command = MDCommandUtil.getCommand(
            MDCommandType.SET_OSMOMETER_ADDRESS,
            mStates.address.get()
        )
        commandItems.add(command)

        command = MDCommandUtil.getCommand(
            MDCommandType.SET_OSMOMETER_TRIGGER,
            "${mStates.triggerValue.get()},10" //深度触发值,温度触发值
        )
        commandItems.add(command)

        command = MDCommandUtil.getCommand(
            MDCommandType.SET_OSMOMETR_CORRECT,
            "${mStates.correctValue.get()},10" //深度修正值,温度修正值
        )
        commandItems.add(command)

        command = MDCommandUtil.getCommand(
            MDCommandType.SET_CORD_LENGTH,
            mStates.wireRopeLength.get()
        )
        commandItems.add(command)

        command = MDCommandUtil.getCommand(
            MDCommandType.SET_OSMOMETR_NOZZEL_HEIGHT,
            mStates.installElevation.get()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /** 4G通讯模式保存指令 */
    private fun init4GSaveCommand() {
        val entity = DasDigitalPiezometerEntity(
            sw = if (mStates.isOpened.get()) "1" else "0",
            addr = if (mStates.isOpened.get()) mStates.address.get() else IOTConstants.NULL_KEY,
            threshold = if (mStates.isOpened.get()) mStates.triggerValue.get() else IOTConstants.NULL_KEY,
            corrval = if (mStates.isOpened.get()) mStates.correctValue.get() else IOTConstants.NULL_KEY,
            ropelen = if (mStates.isOpened.get()) mStates.wireRopeLength.get() else IOTConstants.NULL_KEY,
            tubealti = if (mStates.isOpened.get()) mStates.installElevation.get() else IOTConstants.NULL_KEY,
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

    /**
     * 输入参数验证
     */
    private fun validateInputs(): Boolean {
        if (mStates.address.get().isEmpty()) {
            showMessageDialog("请输入水位计地址!")
            return false
        }
        try {
            val value = mStates.address.get().toDouble()
            if (value < 0 || value > 255) {
                showMessageDialog("水位计地址数值范围[0,255]!")
                return false
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的水位计地址!")
            return false
        }

        if (mStates.triggerValue.get().isEmpty()) {
            showMessageDialog("请输入水位报警值!")
            return false
        }
        try {
            val value = mStates.triggerValue.get().toInt()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的水位报警值!")
            return false
        }

        if (mStates.correctValue.get().isEmpty()) {
            showMessageDialog("请输入水深修正值!")
            return false
        }
        try {
            val value = mStates.correctValue.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的水深修正值!")
            return false
        }

        if (mStates.wireRopeLength.get().isEmpty()) {
            showMessageDialog("请输入水位计绳长!")
            return false
        }
        try {
            val value = mStates.wireRopeLength.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的水位计绳长!")
            return false
        }

        if (mStates.installElevation.get().isEmpty()) {
            showMessageDialog("请输入安装高程值!")
            return false
        }
        try {
            val value = mStates.installElevation.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的安装高程值!")
            return false
        }

        return true
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        if (communicateWay == BleConnect) {
            queryBleParamInfo()
        } else {
            query4GParamInfo()
        }
    }

    /**
     * 蓝牙通讯模式查询信息
     */
    private fun queryBleParamInfo() {
        commandItems.clear()
        val command = MDCommandUtil.getCommand(
            MDCommandType.QUERY_OSMOMETER_PARAMETER
        )
        commandItems.add(command)
        Timber.Forest.d("查询数字水位计配置信息===%s", command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 4G通讯模式查询信息
     */
    private fun query4GParamInfo() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_GET_DIGITAL_PIEZOMETER_INFO
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 4G 下发指令响应失败
     */
    override fun doCmdResponseResultError(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        super.doCmdResponseResultError(
            cmdStr = cmdStr,
            errMsg = errMsg,
            isShowErrMsg = true,
            isMessageDialog = true
        )
    }

    /**
     * 4G 下发指令响应超时
     */
    override fun doCmdResponseResultTimeOut(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        super.doCmdResponseResultTimeOut(
            cmdStr = cmdStr,
            errMsg = errMsg,
            isShowErrMsg = true,
            isMessageDialog = true
        )
    }

    /**
     * 蓝牙下发指令响应超时
     */
    override fun showNearbyCommunicationTimeoutAlert(
        cmdStr: String,
        isDismissLoadingDialog: Boolean,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean,
        errMsg: String
    ) {
        super.showNearbyCommunicationTimeoutAlert(
            cmdStr = cmdStr,
            isDismissLoadingDialog = isDismissLoadingDialog,
            isShowErrMsg = true,
            isMessageDialog = true,
            errMsg = errMsg
        )
    }

    override fun setResultData(cmdStr: String) {
        //判断是否页面是否处于 resume 状态
        if (!isResumed) {
            return
        }
        if (communicateWay == BleConnect) {
            handleBleCommandResult(cmdStr)
        } else {
            handle4GCommandResult(cmdStr)
        }
    }

    /**
     * 处理蓝牙通讯指令结果
     */
    private fun handleBleCommandResult(cmdStr: String) {
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.QUERY_OSMOMETER_PARAMETER -> {
                val result = mdParseManager.parse<MDDasDigitalPiezometerInfo>(
                    cmdStr,
                    MDCommandType.QUERY_OSMOMETER_PARAMETER
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询参数出错"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initBleParamData(result.data)
                    }
                }
            }

            MDCommandType.DIGITAL_OSMOMETER_FUNCTION -> {//开启/关闭数字水位计功能 401
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg =
                            if (cmdStr.contains("4011")) "开启数字水位计出错"
                            else "关闭数字水位计出错" //4012
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("数据保存成功")
                            // 保存初始状态
                            mStates.saveInitialState()
                        }
                    }
                }
            }

            MDCommandType.SET_OSMOMETER_ADDRESS -> {//设置数字水位计地址 402
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "地址配置出错!"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("数据保存成功")
                            // 保存初始状态
                            mStates.saveInitialState()
                        }
                    }
                }
            }

            MDCommandType.SET_OSMOMETER_TRIGGER -> {//设置数字水位计水位报警值 403
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "水位报警值配置出错!"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("数据保存成功")
                            // 保存初始状态
                            mStates.saveInitialState()
                        }
                    }
                }
            }

            MDCommandType.SET_OSMOMETR_CORRECT -> {//设置数字水位计水深修正值 404
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "水深修正值配置出错!"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("数据保存成功")
                            // 保存初始状态
                            mStates.saveInitialState()
                        }
                    }
                }
            }

            MDCommandType.SET_CORD_LENGTH -> {//设置数字水位计绳长 405
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "绳长配置出错!"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("数据保存成功")
                            // 保存初始状态
                            mStates.saveInitialState()
                        }
                    }
                }
            }

            MDCommandType.SET_OSMOMETR_NOZZEL_HEIGHT -> {//设置数字水位计安装高程 406
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "安装高程配置出错!"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("数据保存成功")
                            // 保存初始状态
                            mStates.saveInitialState()
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initBleParamData(digitalPiezometerInfo: MDDasDigitalPiezometerInfo) {
        try {
            MDOsmometerStatus.Companion.value(digitalPiezometerInfo.osmometerStatus).let {
                mStates.isOpened.set(it == MDOsmometerStatus.OSMOMETER_OPEN)
            }
            mStates.address.set(digitalPiezometerInfo.osmometerAddress)
            mStates.triggerValue.set(digitalPiezometerInfo.depthTrigger.formatDoubleValue("100", 1))
            mStates.correctValue.set(digitalPiezometerInfo.depthCorrect.formatDoubleValue("0", 1))
            mStates.wireRopeLength.set(
                digitalPiezometerInfo.wireRopeLength.formatDoubleValue(
                    "0",
                    1
                )
            )
            mStates.installElevation.set(
                digitalPiezometerInfo.installElevation.formatDoubleValue(
                    "0",
                    1
                )
            )

            // 保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.Forest.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 处理4G通讯指令结果
     */
    private fun handle4GCommandResult(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.DAS_MD_GET_DIGITAL_PIEZOMETER_INFO -> {//
                val result = iotParseManager.parse<DasDigitalPiezometerInfo>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_DIGITAL_PIEZOMETER_INFO
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult("$errMsg: ${result.message}")
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        init4GParamData(result.data)
                    }
                }
            }

            IOTCommandType.DAS_MD_SET_DIGITAL_PIEZOMETER_INFO -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "配置参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("数据保存成功")
                            // 保存初始状态
                            mStates.saveInitialState()
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun init4GParamData(digitalPiezometerInfo: DasDigitalPiezometerInfo) {
        try {
            mStates.isOpened.set(digitalPiezometerInfo.sw == "1")
            mStates.address.set(digitalPiezometerInfo.addr)
            mStates.triggerValue.set(digitalPiezometerInfo.threshold.formatDoubleValue("100", 1))
            mStates.correctValue.set(digitalPiezometerInfo.corrval.formatDoubleValue("0", 1))
            mStates.wireRopeLength.set(digitalPiezometerInfo.ropelen.formatDoubleValue("0", 1))
            mStates.installElevation.set(digitalPiezometerInfo.tubealti.formatDoubleValue("0", 1))

            // 保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.Forest.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    companion object {
        fun newInstance() = DasDigitalOsmometerFragment()
    }
}