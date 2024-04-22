package com.shmedo.mcloudapp.device.ui.mr702.fragment.port

import android.os.Bundle
import android.widget.CompoundButton
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.ext.getActivityScopeViewModel
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr.MRRS232Port1ParamEntity
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr.MRRS232Port2ParamEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRRS232Port1Param
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessage
import com.shmedo.mcloudapp.ext.showMessageDialog
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs232Port1Binding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.MR702PortHomeViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.MR702RS232Port1ViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class MR702RS232Port1Fragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702Rs232Port1Binding
    private lateinit var mInterfaceHomeViewModel: MR702PortHomeViewModel
    private lateinit var mStates: MR702RS232Port1ViewModel
    private val iotParseManager: IOTParserManager by inject()

    private val cameraModelList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_rs232_port1_camera_model) }
    private val cameraResolutionList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_rs232_port2_camera_resolution) }
    private val dataBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_data_bit) }
    private val checkBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_check_bit) }
    private val stopBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_stop_bit) }


    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
        mInterfaceHomeViewModel = getActivityScopeViewModel()
    }

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
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryInfo()
        }
    }

    override fun initData() {
        super.initData()
        mStates.dataBit.set(dataBitList[3])
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
        val entity = MRRS232Port2ParamEntity(
            switch = "0"
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_SET_RS232_PORT1_PARAM,
            entity.toCommandString()
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 保存摄像头参数
     */
    private fun initSaveCommand() {
        commandItems.clear()
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
            resolut = cameraResolutionList.indexOf(mStates.cameraResolution.get()).toString(),
            interval = mStates.photoInterval.get(),
            baud = mStates.baudRate.get(),
            databit = mStates.dataBit.get(),
            parity = checkBitList.indexOf(mStates.checkBit.get()).toString(),
            stopbit = stopBitList.indexOf(mStates.stopBit.get()).toString(),
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_SET_RS232_PORT1_PARAM,
            entity.toCommandString()
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
//        testData()
    }

    private fun queryInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_RS232_PORT1_PARAM)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_RS232_PORT1_PARAM -> {
                val result = iotParseManager.parse<MRRS232Port1Param>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_RS232_PORT1_PARAM
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
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initParamData(result.data)
                    }
                }
            }

            IOTCommandType.MD_MR_SET_RS232_PORT1_PARAM -> {
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

    private fun initParamData(sensorParam: MRRS232Port1Param) {
        try {
            mStates.status.set(if (sensorParam.status == "1") "已接入" else "未接入")
            mStates.isOpened.set(sensorParam.switch == "1")
            mStates.cameraModel.set(cameraModelList[sensorParam.type.toInt()])
            mStates.cameraResolution.set(cameraResolutionList[sensorParam.resolut.toInt()])
            mStates.photoInterval.set(sensorParam.interval)

            mStates.baudRate.set(sensorParam.baud)
            mStates.dataBit.set(sensorParam.databit)
            mStates.checkBit.set(checkBitList[sensorParam.parity.toInt()])
            mStates.stopBit.set(stopBitList[sensorParam.stopbit.toInt()])
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun newInstance() = MR702RS232Port1Fragment()
    }
}