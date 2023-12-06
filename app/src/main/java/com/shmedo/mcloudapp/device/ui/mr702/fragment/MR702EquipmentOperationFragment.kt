package com.shmedo.mcloudapp.device.ui.mr702.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.setup
import com.google.gson.GsonBuilder
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentMr702EquipmentOperationBinding
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.ConfigModule
import com.shmedo.mcloudapp.device.model.DeviceLogUploadModule
import com.shmedo.mcloudapp.device.model.ManualPhotoTakingModule
import com.shmedo.mcloudapp.device.model.ManualSettingModule
import com.shmedo.mcloudapp.device.model.ParameterExportModule
import com.shmedo.mcloudapp.device.model.ParameterImportModule
import com.shmedo.mcloudapp.device.model.TelemetryDataModule
import com.shmedo.mcloudapp.device.model.TimeCalibrationModule
import com.shmedo.mcloudapp.device.ui.mr702.fragment.dialog.MR702ParamExportPopupView
import com.shmedo.mcloudapp.device.ui.mr702.fragment.dialog.TelemetryPopupView
import com.shmedo.mcloudapp.device.viewmodel.state.MR702EquipmentOperationViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2023/12/5
 * @desc: 设备操作
 *
 */
class MR702EquipmentOperationFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentMr702EquipmentOperationBinding by lazy { getBinding() as FragmentMr702EquipmentOperationBinding }
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: MR702EquipmentOperationViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_mr702_equipment_operation,
            BR.toolbarVM,
            toolbarViewModel
        )
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.llToolbar.toolbar.title = "设备操作"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        initModuleAdapter()
    }

    private fun initModuleAdapter() {
        binding.rvModule.setup { rv ->
            rv.addItemDecoration(
                MyGridSpacingItemDecoration(
                    2,
                    ConvertUtils.dp2px(15f),
                    false
                )
            )
            addType<ConfigModule>(R.layout.item_device_config_module)
            R.id.item.onClick {
                val module = getModel<ConfigModule>()
                processItemClick(module)
            }
        }.models = getModuleList()
    }

    private fun processItemClick(module: ConfigModule) {
        if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
            return
        }
        when (module.configModule) {
            is TimeCalibrationModule -> {
                commandItems.clear()

                val command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_SYSTEM_TIME)
                commandItems.add(command)
                sendCommandFromCmdList(isStartTimeoutJob = true)
            }

            is TelemetryDataModule -> {
                commandItems.clear()

                val command =
                    IOTCommandUtil.getCommand(IOTCommandType.QUERY_SAMPLE)//QUERY_SAMPLE  MD_MR_TELEMETRY
                commandItems.add(command)
                sendCommandFromCmdList(isStartTimeoutJob = true)
            }

            is ManualSettingModule -> {

            }

            is DeviceLogUploadModule -> {

            }

            is ParameterExportModule -> {
                showParameterExportPopup()
            }

            is ParameterImportModule -> {

            }

            else -> {

            }
        }
    }

    private fun showTelemetryDataPopup() {
        val popupView = TelemetryPopupView(requireContext())
        popupView.setTitle("召测", mStates)
        XPopup.Builder(context)
            .dismissOnBackPressed(false) // 按返回键是否关闭弹窗，默认为true
            .dismissOnTouchOutside(false)// 点击外部是否关闭弹窗，默认为true
            .enableDrag(false)
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .asCustom(popupView)
            .show()
    }

    private fun showParameterExportPopup() {
        val popupView = MR702ParamExportPopupView(requireContext())
        popupView.setTitle("参数导出")
            .setClickListener(object : MR702ParamExportPopupView.OnClickListener {
                override fun onExportClick() {
                    sendParameterExportCmd()
                }
            })
        XPopup.Builder(context)
            .dismissOnBackPressed(false) // 按返回键是否关闭弹窗，默认为true
            .dismissOnTouchOutside(false)// 点击外部是否关闭弹窗，默认为true
            .enableDrag(false)
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .asCustom(popupView)
            .show()
    }

    /**
     * 参数导出下发指令
     */
    private fun sendParameterExportCmd() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_UPLOAD_CONFIG)
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun doNetDispatchSuccess(cmdStr: String) {
        super.doNetDispatchSuccess(cmdStr)
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_SAMPLE -> {
                mStates.isResponseLoading.set(true)
                showTelemetryDataPopup()
            }

            else -> {}
        }
    }

    override fun doCmdResponseResultError(errorMsg: String) {
        super.doCmdResponseResultError(errorMsg)
        mStates.isResponseLoading.set(false)
        mStates.isResponseSuccess.set(false)
        mStates.responseContent.set(errorMsg)
    }

    override fun doCmdResponseResultTimeOut(errorMsg: String) {
        super.doCmdResponseResultTimeOut(errorMsg)
        mStates.isResponseLoading.set(false)
        mStates.isResponseSuccess.set(false)
        mStates.responseContent.set("指令响应超时")
    }

    override fun setResultData(cmdStr: String) {
        if (viewLifecycleOwner.lifecycle.currentState < androidx.lifecycle.Lifecycle.State.RESUMED) {
            return
        }
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_SAMPLE -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.QUERY_SAMPLE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        Timber.e(result.message)
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(false)
                        mStates.responseContent.set(result.message)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(true)
                        try {
                            //String content = "{\"20001_1\":{\"device_id\":\"13230\",\"time\":\"2022-07-20 17:32:05\",\"level\":\"warn\",\"msg\":\"iot_cmd:$cmd=md_getworkmode \"}}\u0000\u0000\u0000";
                            val gson = GsonBuilder()
                                .setPrettyPrinting()
                                .disableHtmlEscaping()
                                .create()
//                            var content: String = result.data
//                            content = content.replace("\u0000", "") // removes NUL chars
//                            content = content.replace("\\u0000", "") // removes backslash+u0000
//                            val prettyJson = gson.toJson(JsonParser.parseString(content))
                            mStates.responseContent.set(result.data)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
            }

            IOTCommandType.MD_MR_UPLOAD_CONFIG -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "参数导出指令下发出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("参数导出指令下发成功")
                        }
                    }
                }
            }

            else -> {}
        }
    }

    private fun getModuleList() =
        arrayListOf<ConfigModule>(
            ConfigModule(TimeCalibrationModule()),
            ConfigModule(TelemetryDataModule()),
            ConfigModule(ManualSettingModule()),
            ConfigModule(DeviceLogUploadModule()),
            ConfigModule(ParameterExportModule()),
            ConfigModule(ParameterImportModule()),
            ConfigModule(ManualPhotoTakingModule()),
        )

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}