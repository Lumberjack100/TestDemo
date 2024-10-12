package com.shmedo.mcloudapp.ui.page.device.m50

import android.os.Bundle
import android.util.Log
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
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.m50.M50SerialPortParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m.M50SerialPortParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentM50SerialPortParamBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.M50SerialPortParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/9/10
 * @desc: M50 串口参数配置
 *
 */
class M50SerialPortParamFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentM50SerialPortParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: M50SerialPortParamViewModel by activityViewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val rs232ExternalDeviceList =
        arrayListOf("无", "抓拍相机", "卫星通信终端")
    private val captureFrequencyList =
        arrayListOf("15分钟/次", "30分钟/次", "1小时/次", "2小时/次")
    private val captureFrequencyMinList =
        arrayListOf("15", "30", "60", "120")//抓拍频率
    private val imageResolutionList = arrayListOf("1024x768", "1280x960", "1600x1200", "1920x1080")
    private val rs485ExternalDeviceList = arrayListOf("无", "压电式雨量计")
    private val rs485BaudRateList = arrayListOf("2400", "4800", "9600", "14400", "19200")

    override fun initViewModel() {
        super.initViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_m50_serial_port_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentM50SerialPortParamBinding
        binding.llToolbar.toolbar.title = "串口设置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
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

    override fun initData() {
        super.initData()
        resetDefaultParams()
    }

    private fun resetDefaultParams() {
        mStates.isExternalPower.set(false)
        mStates.rs232ExternalDevice.set(rs232ExternalDeviceList[0])//默认为 无
        mStates.captureFrequency.set(captureFrequencyList[captureFrequencyList.lastIndex])//默认为 2小时/次
        mStates.imageResolution.set(imageResolutionList[2])//默认为 1600x1200
        mStates.rs485ExternalDevice.set(rs485ExternalDeviceList[0])//默认为 无
        mStates.rs485BaudRate.set(rs485BaudRateList[2])//默认为 9600
        mStates.rs485ExternalDeviceAddr.set("2")//默认为 2
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 选择 232外部设备
         */
        fun on232ExternalDeviceChooseClick() {
            val selectedIndex = rs232ExternalDeviceList.indexOf(mStates.rs232ExternalDevice.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", rs232ExternalDeviceList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.rs232ExternalDevice.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 抓拍频率
         */
        fun onCaptureFrequencyClick() {
            val selectedIndex = captureFrequencyList.indexOf(mStates.captureFrequency.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", captureFrequencyList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.captureFrequency.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 图片分辨率
         */
        fun onImageResolutionClick() {
            val selectedIndex = imageResolutionList.indexOf(mStates.imageResolution.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", imageResolutionList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.imageResolution.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 选择 485外部设备
         */
        fun on485ExternalDeviceChooseClick() {
            val selectedIndex = rs485ExternalDeviceList.indexOf(mStates.rs485ExternalDevice.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使���一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", rs485ExternalDeviceList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.rs485ExternalDevice.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun on485BaudRateChooseClick() {
            val selectedIndex = rs485BaudRateList.indexOf(mStates.rs485BaudRate.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", rs485BaudRateList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.rs485BaudRate.set(text)
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

        if (mStates.rs232ExternalDevice.get() != "无" && mStates.rs485ExternalDeviceAddr.get()
                .isEmpty()
        ) {
            showMessageDialog("请输入485外接设备地址!")
            return
        }

        val entity = M50SerialPortParamEntity(
            out_power = if (mStates.isExternalPower.get()) "1" else "0",
            rs232_mode = rs232ExternalDeviceList.indexOf(mStates.rs232ExternalDevice.get())
                .toString(),
            cam_module = if (mStates.rs232ExternalDevice.get() == "无") IOTConstants.NULL_KEY else captureFrequencyMinList[captureFrequencyList.indexOf(
                mStates.captureFrequency.get()
            )],
            pixx = if (mStates.rs232ExternalDevice.get() == "无") IOTConstants.NULL_KEY else imageResolutionList.indexOf(
                mStates.imageResolution.get()
            ).toString(),
            pixy = if (mStates.rs232ExternalDevice.get() == "无") IOTConstants.NULL_KEY else imageResolutionList.indexOf(
                mStates.imageResolution.get()
            ).toString(),
            rs485_mode = rs485ExternalDeviceList.indexOf(mStates.rs485ExternalDevice.get())
                .toString(),
            rs485_baud = rs485BaudRateList.indexOf(mStates.rs485BaudRate.get()).toString(),
            rs485_addr = mStates.rs485ExternalDeviceAddr.get()
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.M50_MD_SET_SERIAL_PORT,
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
        val command = IOTCommandUtil.getCommand(IOTCommandType.M50_MD_SET_SERIAL_PORT,"method=0")
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.M50_MD_SET_SERIAL_PORT -> {//
                val result = if (cmdStr.contains("method=0"))
                    iotParseManager.parse<M50SerialPortParam>(
                        cmdStr,
                        IOTCommandType.M50_MD_SET_SERIAL_PORT
                    )
                else iotParseManager.parse<CommonSettingCmdResult>(cmdStr)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg =
                            if (cmdStr.contains("method=0")) "查询信息出错: ${result.message}" else "设置参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        if (cmdStr.contains("method=0")) {
                            initParamData(result.data as M50SerialPortParam)
                        } else {
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

    private fun initParamData(info: M50SerialPortParam) {
        try {
            mStates.isExternalPower.set(info.out_power == "1")
            info.rs232_mode.toIntOrNull()?.let {
                if (it in rs232ExternalDeviceList.indices) {
                    mStates.rs232ExternalDevice.set(rs232ExternalDeviceList[it])
                }
            }

            captureFrequencyMinList.indexOf(info.cam_module)
                .let { index ->
                    if (index in captureFrequencyList.indices) {
                        mStates.captureFrequency.set(captureFrequencyList[index])
                    }
                }

            mStates.imageResolution.set("${info.pixx}x${info.pixy}")

            info.rs485_mode.toIntOrNull()?.let {
                if (it in rs485ExternalDeviceList.indices) {
                    mStates.rs485ExternalDevice.set(rs485ExternalDeviceList[it])
                }
            }

            info.rs485_baud.toIntOrNull()?.let {
                if (it in rs485BaudRateList.indices) {
                    mStates.rs485BaudRate.set(rs485BaudRateList[it])
                }
            }

            mStates.rs485ExternalDeviceAddr.set(info.rs485_addr)
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}