package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702.port

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRRS232Port1ParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS232Port1Param
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.communication.model.ErrorHandlingStrategy
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs232Port1Binding
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702PortHomeViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702RS232Port1ViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class MR702RS232Port1Fragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702Rs232Port1Binding
    private val mInterfaceHomeViewModel: MR702PortHomeViewModel by activityViewModels()
    private val mStates: MR702RS232Port1ViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val cameraModelList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_rs232_port1_camera_model) }
    private val cameraResolutionList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_rs232_port2_camera_resolution) }

    //工作模式
    private val workModelList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_rs232_port1_work_model) }
    private val dataBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_data_bit) }
    private val checkBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_check_bit) }
    private val stopBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_stop_bit) }


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_rs232_port1, BR.stateVM, mStates)
            .addBindingParam(BR.homeVM, mInterfaceHomeViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702Rs232Port1Binding
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
        mStates.cameraModel.set(cameraModelList[0])
        mStates.cameraResolution.set(cameraResolutionList[0])
        mStates.photoInterval.set("65535")
        mStates.workModel.set(workModelList[0])
        mStates.baudRate.set("115200")
        mStates.dataBit.set(dataBitList[3])
        mStates.checkBit.set(checkBitList[0])
        mStates.stopBit.set(stopBitList[0])
    }

    inner class ClickProxy : BaseClickProxy() {

        fun onChooseCameraModelClick() {
            val selectedIndex = cameraModelList.indexOf(mStates.cameraModel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择型号", cameraModelList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.cameraModel.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onChooseCameraResolutionClick() {
            val selectedIndex = cameraResolutionList.indexOf(mStates.cameraResolution.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择分辨率", cameraResolutionList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.cameraResolution.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 选择工作模式
         */
        fun onChooseWorkModelClick() {
            val selectedIndex = workModelList.indexOf(mStates.workModel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择工作模式", workModelList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.workModel.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onDataBitChooseClick() {
            val selectedIndex = dataBitList.indexOf(mStates.dataBit.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择数据位", dataBitList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.dataBit.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onCheckBitChooseClick() {
            val selectedIndex = checkBitList.indexOf(mStates.checkBit.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择校验位", checkBitList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.checkBit.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onStopBitChooseClick() {
            val selectedIndex = stopBitList.indexOf(mStates.stopBit.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择停止位", stopBitList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.stopBit.set(text)
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
        val commands = mutableListOf<String>()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_SET_RS232_PORT1_PARAM,
            "switch=0"
        )
        commands.add(command)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    /**
     * 保存摄像头参数
     */
    private fun initSaveCommand() {
        val commands = mutableListOf<String>()

        if (mStates.photoInterval.get().isEmpty()) {
            showMessageDialog("请输入拍照间隔")
            return
        }
        if (mStates.baudRate.get().isEmpty()) {
            showMessageDialog("请输入波特率")
            return
        }
        val entity = MRRS232Port1ParamEntity(
            switch = "1",
            type = cameraModelList.indexOf(mStates.cameraModel.get()).toString(),
            resolut = (cameraResolutionList.indexOf(mStates.cameraResolution.get()) + 1).toString(),
            interval = mStates.photoInterval.get(),
            workmode = workModelList.indexOf(mStates.workModel.get()).toString(),
            baud = mStates.baudRate.get(),
            databit = mStates.dataBit.get(),
            parity = checkBitList.indexOf(mStates.checkBit.get()).toString(),
            stopbit = stopBitList.indexOf(mStates.stopBit.get()).toString(),
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_SET_RS232_PORT1_PARAM,
            entity.toCommandString()
        )
        commands.add(command)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryInfo() {
        val commands = mutableListOf<String>()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_RS232_PORT1_PARAM)
        commands.add(command)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig(
                    strategy = ErrorHandlingStrategy.Dialog
                )
            )
        )
    }

    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MR_MD_GET_RS232_PORT1_PARAM -> {
                val result = iotParseManager.parse<MRRS232Port1Param>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_RS232_PORT1_PARAM
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

            IOTCommandType.MR_MD_SET_RS232_PORT1_PARAM -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
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

    private fun initParamData(sensorParam: MRRS232Port1Param) {
        try {
            mStates.status.set(if (sensorParam.status == "1") "已接入" else "未接入")
            mStates.isOpened.set(sensorParam.switch == "1")
            sensorParam.type.toInt().let {
                if (it in cameraModelList.indices) {
                    mStates.cameraModel.set(cameraModelList[it])
                }
            }
            sensorParam.resolut.toInt().let {
                val index = it - 1
                if (index in cameraResolutionList.indices) {
                    mStates.cameraResolution.set(cameraResolutionList[index])
                }
            }
            mStates.photoInterval.set(sensorParam.interval)
            sensorParam.workmode.toInt().let {
                if (it in workModelList.indices) {
                    mStates.workModel.set(workModelList[it])
                }
            }
            mStates.baudRate.set(sensorParam.baud)
            mStates.dataBit.set(sensorParam.databit)
            sensorParam.parity.toInt().let {
                if (it in checkBitList.indices) {
                    mStates.checkBit.set(checkBitList[it])
                }
            }
            sensorParam.stopbit.toInt().let {
                if (it in stopBitList.indices) {
                    mStates.stopBit.set(stopBitList[it])
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
        fun newInstance() = MR702RS232Port1Fragment()
    }
}