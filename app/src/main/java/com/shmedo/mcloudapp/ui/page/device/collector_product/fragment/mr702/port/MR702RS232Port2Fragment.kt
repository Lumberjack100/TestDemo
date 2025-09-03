package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702.port

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRRS232Port2ParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS232Port2Param
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.DeviceError
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs232Port2Binding
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702PortHomeViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702RS232Port2ViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class MR702RS232Port2Fragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702Rs232Port2Binding
    private val mInterfaceHomeViewModel: MR702PortHomeViewModel by activityViewModels()
    private val mStates: MR702RS232Port2ViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val encodingTypeList =
        arrayListOf("汉字", "ASCII", "混编", "压缩汉字", "压缩ASCII")//编码类型
    private val dataLinkList =
        arrayListOf("4G数据链路1", "4G数据链路2", "4G数据链路3", "4G数据链路4", "4G数据链路5")//数据链路
    private val inStationConfirmList = arrayListOf("不需要", "需要")//入站确认


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_rs232_port2, BR.stateVM, mStates)
            .addBindingParam(BR.homeVM, mInterfaceHomeViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702Rs232Port2Binding
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
            queryInfo()
        }
    }

    override fun initData() {
        super.initData()
        resetDefaultParams()
        // 保存初始状态
        mStates.saveInitialState()
    }

    /**
     * 初始化默认参数
     */
    private fun resetDefaultParams() {
        mStates.isOpened.set(true)
        mStates.address.set("")
        mStates.baudRate.set("115200")
        mStates.encodingType.set(encodingTypeList[2])
        mStates.dataLink.set(dataLinkList[1])
        mStates.inStationConfirm.set(inStationConfirmList[0])
    }

    inner class ClickProxy : BaseClickProxy() {
        fun onEncodingTypeChooseClick() {
            val selectedIndex = encodingTypeList.indexOf(mStates.encodingType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择编码类型", encodingTypeList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.encodingType.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onInStationConfirmChooseClick() {
            val selectedIndex = inStationConfirmList.indexOf(mStates.inStationConfirm.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", inStationConfirmList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.inStationConfirm.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onDataLinkChooseClick() {
            val selectedIndex = dataLinkList.indexOf(mStates.dataLink.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择数据源", dataLinkList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.dataLink.set(text)
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
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            if (!mStates.isOpened.get()) {
                closeSwitch()
                return
            }
            initSaveCommand()
        }
    }

    private fun closeSwitch() {
        val entity = MRRS232Port2ParamEntity(
            sw = "0"
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_SET_RS232_PORT2_PARAM,
            entity.toCommandString()
        )
        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                timeout = AppContants.Communication.DELAY_10000_MILLIS,//默认10秒超时
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.customConfig { error ->
                    if (error is DeviceError.Timeout)
                        Toaster.show("Feature not supported")
                    else
                        Toaster.show(error.message)
                }
            )
        )
    }

    /**
     * 保存采集参数
     */
    private fun initSaveCommand() {
        if (mStates.address.get().isEmpty()) {
            showMessageDialog("请输入地址")
            return
        }
        if (mStates.baudRate.get().isEmpty()) {
            showMessageDialog("请输入波特率")
            return
        }
        val entity = MRRS232Port2ParamEntity(
            sw = "1",
            destaddr = mStates.address.get(),
            baud = mStates.baudRate.get(),
            codetype = (encodingTypeList.indexOf(mStates.encodingType.get()) + 1).toString(),
            linkid = (dataLinkList.indexOf(mStates.dataLink.get())).toString(),
            confirm = (inStationConfirmList.indexOf(mStates.inStationConfirm.get()) + 1).toString(),
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_SET_RS232_PORT2_PARAM,
            entity.toCommandString()
        )
        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                timeout = AppContants.Communication.DELAY_10000_MILLIS,//默认10秒超时
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.customConfig { error ->
                    if (error is DeviceError.Timeout)
                        Toaster.show("Feature not supported")
                    else
                        Toaster.show(error.message)
                }
            )
        )
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryInfo() {
        val command = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_RS232_PORT2_PARAM)
        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                timeout = AppContants.Communication.DELAY_10000_MILLIS,//默认10秒超时
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.customConfig { error ->
                    if (error is DeviceError.Timeout)
                        Toaster.show("Feature not supported")
                    else
                        Toaster.show(error.message)
                }
            )
        )
    }

    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MR_MD_GET_RS232_PORT2_PARAM -> {
                val result = iotParseManager.parse<MRRS232Port2Param>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_RS232_PORT2_PARAM
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        initParamData(result.data)
                    }
                }
            }

            IOTCommandType.MR_MD_SET_RS232_PORT2_PARAM -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        if (!isCommunicationExecuting()) {
                            Toaster.show("数据保存成功")
                            // 保存初始状态
                            mStates.saveInitialState()
                        }
                    }
                }
            }

            else -> {

            }
        }
    }

    private fun initParamData(sensorParam: MRRS232Port2Param) {
        try {
            mStates.isOpened.set(sensorParam.sw == "1")
            mStates.address.set(sensorParam.destaddr)
            mStates.baudRate.set(sensorParam.baud)

            sensorParam.codetype.toIntOrNull()?.let {
                if (it - 1 in encodingTypeList.indices) {
                    mStates.encodingType.set(encodingTypeList[it - 1])
                }
            }

            sensorParam.linkid.toIntOrNull()?.let {
                if (it in dataLinkList.indices) {
                    mStates.dataLink.set(dataLinkList[it])
                }
            }

            sensorParam.confirm.toIntOrNull()?.let {
                if (it - 1 in inStationConfirmList.indices) {
                    mStates.inStationConfirm.set(inStationConfirmList[it - 1])
                }
            }

            // 保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.Forest.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    companion object {
        fun newInstance() = MR702RS232Port2Fragment()
    }
}