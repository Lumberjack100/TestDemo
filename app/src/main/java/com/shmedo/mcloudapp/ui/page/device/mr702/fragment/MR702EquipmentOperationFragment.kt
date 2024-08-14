package com.shmedo.mcloudapp.ui.page.device.mr702.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentMr702EquipmentOperationBinding
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.ConfigModule
import com.shmedo.mcloudapp.model.DeviceLogUploadModule
import com.shmedo.mcloudapp.model.ManualPhotoTakingModule
import com.shmedo.mcloudapp.model.ManualSettingModule
import com.shmedo.mcloudapp.model.MonitoringElement
import com.shmedo.mcloudapp.model.ParameterExportModule
import com.shmedo.mcloudapp.model.ParameterImportModule
import com.shmedo.mcloudapp.model.TelemetryDataModule
import com.shmedo.mcloudapp.model.TimeCalibrationModule
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.mr702.dialog.MR702DeviceDataUploadPopupView
import com.shmedo.mcloudapp.ui.page.device.mr702.dialog.MR702ManualSettingPopupView
import com.shmedo.mcloudapp.ui.page.device.mr702.dialog.MR702ParamExportPopupView
import com.shmedo.mcloudapp.ui.page.device.mr702.dialog.MR702ParamImportPopupView
import com.shmedo.mcloudapp.ui.page.device.mr702.dialog.TelemetryPopupView
import com.shmedo.mcloudapp.ui.page.device.mr702.dialog.TimeCalibrationPopupView

import com.shmedo.mcloudapp.ui.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702EquipmentOperationViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2023/12/5
 * @desc: 设备操作
 *
 */
class MR702EquipmentOperationFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702EquipmentOperationBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: MR702EquipmentOperationViewModel
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val monitoringElementList = arrayListOf<MonitoringElement>()


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_mr702_equipment_operation,
            BR.toolbarVM,
            toolbarViewModel
        )
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702EquipmentOperationBinding
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

    override fun initData() {
        super.initData()
        monitoringElementList.apply {
            add(MonitoringElement(32, "当前降水量", "mm"))
            add(MonitoringElement(31, "日降水量", "mm"))
            add(MonitoringElement(59, "库(闸、站)上水位", "mm"))
            add(MonitoringElement(250, "渗流", "mm"))
            add(MonitoringElement(251, "渗压", "KPa"))
        }
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
        if (isBleDisconnected()) {
            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
            return
        }
        when (module.configModule) {
            is TimeCalibrationModule -> {
                commandItems.clear()
                val command =
                    IOTCommandUtil.getCommand(IOTCommandType.QUERY_TERMINAL_TIME)//QUERY_TERMINAL_TIME  MD_MR_GET_SYSTEM_TIME
                commandItems.add(command)

                if (communicateWay is BleConnect) {
                    mStates.isResponseLoading.set(true)
                    showTimeCalibrationPopup()
                }
                sendCommandFromCmdList(isStartTimeoutJob = true)
            }

            is TelemetryDataModule -> {
                commandItems.clear()
                val command =
                    IOTCommandUtil.getCommand(IOTCommandType.QUERY_SAMPLE)//QUERY_SAMPLE  MD_MR_TELEMETRY
                commandItems.add(command)

                if (communicateWay is BleConnect) {
                    mStates.isResponseLoading.set(true)
                    showTelemetryDataPopup()
                }
                sendCommandFromCmdList(isStartTimeoutJob = true)
            }

            is ManualSettingModule -> {
                mStates.isManualSetting.set(false)
                showManualSettingPopup()
            }

            is DeviceLogUploadModule -> {
                mStates.isDeviceDataUploading.set(false)
                showDeviceDataUploadPopup()
            }

            is ParameterExportModule -> {
                mStates.isParamExporting.set(false)
                showParameterExportPopup()
            }

            is ParameterImportModule -> {
                mStates.isParamImporting.set(false)
                mStates.sn.set(deviceInfo.deviceToken)
                mStates.deviceId.set(deviceInfo.id.toString())
                showParameterImportPopup()
            }

            else -> {

            }
        }
    }

    /**
     * 显示时间校准弹窗
     */
    private fun showTimeCalibrationPopup() {
        val popupView = TimeCalibrationPopupView(requireContext())
        popupView.setTitle("时间校准", mStates)
            .setClickListener(object : TimeCalibrationPopupView.OnClickListener {
                override fun onSettingClick() {
                    commandItems.clear()
                    val command = IOTCommandUtil.getCommand(
                        IOTCommandType.SET_TERMINAL_TIME,
                        "time=${TimeUtils.getNowString()}"
                    )
                    commandItems.add(command)
                    sendCommandFromCmdList(isStartTimeoutJob = true)
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
     * 显示遥测数据弹窗
     */
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

    /**
     * 显示人工置数弹窗
     */
    private fun showManualSettingPopup() {
        val popupView = MR702ManualSettingPopupView(requireContext())
        popupView.setTitle("人工置数", monitoringElementList, mStates)
            .setClickListener(object : MR702ManualSettingPopupView.OnClickListener {
                override fun onConfirmClick(otime: Long, type: Int, data: String, unit: String) {
                    commandItems.clear()
                    val command = IOTCommandUtil.getCommand(
                        IOTCommandType.MD_MR_ARTIFICIAL,
                        "type=$type&data=$data&unit=$unit&otime=$otime"
                    )
                    commandItems.add(command)
                    sendCommandFromCmdList(isStartTimeoutJob = true)
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
     * 显示数据上传弹窗
     */
    private fun showDeviceDataUploadPopup() {
        val popupView = MR702DeviceDataUploadPopupView(requireContext())
        popupView.setTitle("文件上传", mStates)
            .setClickListener(object : MR702DeviceDataUploadPopupView.OnClickListener {
                override fun onConfirmClick(type: Int, time: String) {
                    commandItems.clear()
                    val command = IOTCommandUtil.getCommand(
                        IOTCommandType.MD_MR_UPLOAD_FILE,
                        "typre=$type&timeframe=$time"
                    )
                    commandItems.add(command)
                    sendCommandFromCmdList(isStartTimeoutJob = true)
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
     * 显示参数导出弹窗
     */
    private fun showParameterExportPopup() {
        val popupView = MR702ParamExportPopupView(requireContext())
        popupView.setTitle("参数导出", mStates)
            .setClickListener(object : MR702ParamExportPopupView.OnClickListener {
                override fun onExportClick() {
                    commandItems.clear()
                    val command = IOTCommandUtil.getCommand(IOTCommandType.MD_BACKUP_CONFIG)
                    commandItems.add(command)
                    sendCommandFromCmdList(isStartTimeoutJob = true)
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
     * 显示参数导入弹窗
     */
    private fun showParameterImportPopup() {
        val popupView = MR702ParamImportPopupView(requireContext())
        popupView.setTitle("参数导入", mStates)
            .setClickListener(object : MR702ParamImportPopupView.OnClickListener {
                override fun onImportClick(backupID: String) {
                    applyBackup(backupID)
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

    private fun applyBackup(backupID: String = "") {
        launchWithViewLifecycle {
            val msgID = deviceRequestViewModel.applyBackup(
                backupID,
                deviceInfo.id.toString()
            ) { error: Throwable ->
                mStates.isResponseLoading.set(false)
                mStates.isResponseSuccess.set(false)
                mStates.responseContent.set(error.errorMsg)
            } ?: return@launchWithViewLifecycle

            mStates.isParamImporting.set(true)
            mStates.isResponseLoading.set(true)
            mStates.isResponseSuccess.set(false)
            netIotCommandViewModel.processCmdResult("", arrayListOf(msgID))
        }
    }

    override fun doNetDispatchSuccess(cmdStr: String) {
        super.doNetDispatchSuccess(cmdStr)
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_TERMINAL_TIME -> {
                mStates.isResponseLoading.set(true)
                mStates.isResponseSuccess.set(false)
                showTimeCalibrationPopup()
            }

            IOTCommandType.SET_TERMINAL_TIME -> {
                mStates.isResponseLoading.set(true)
                mStates.isResponseSuccess.set(false)
            }

            IOTCommandType.QUERY_SAMPLE -> {
                mStates.isResponseLoading.set(true)
                mStates.isResponseSuccess.set(false)
                showTelemetryDataPopup()
            }

            IOTCommandType.MD_MR_ARTIFICIAL -> {
                mStates.isManualSetting.set(true)
                mStates.isResponseLoading.set(true)
                mStates.isResponseSuccess.set(false)
            }

            IOTCommandType.MD_MR_UPLOAD_FILE -> {
                mStates.isDeviceDataUploading.set(true)
                mStates.isResponseLoading.set(true)
                mStates.isResponseSuccess.set(false)
            }

            IOTCommandType.MD_BACKUP_CONFIG -> {
                mStates.isParamExporting.set(true)
                mStates.isResponseLoading.set(true)
                mStates.isResponseSuccess.set(false)
            }

            else -> {}
        }
    }

    override fun doCmdResponseResultError(cmdStr: String, errorMsg: String) {
//        super.doCmdResponseResultError(errorMsg)
        mStates.isResponseLoading.set(false)
        mStates.isResponseSuccess.set(false)
        mStates.responseContent.set(errorMsg)
    }

    override fun doCmdResponseResultTimeOut(cmdStr: String, errorMsg: String) {
//        super.doCmdResponseResultTimeOut(errorMsg)
        mStates.isResponseLoading.set(false)
        mStates.isResponseSuccess.set(false)
        mStates.responseContent.set("指令响应超时")
    }

    override fun showNearbyCommunicationTimeoutAlert(
        cmdStr: String,
        isDismissLoadingDialog: Boolean,
        isShowMsg: Boolean,
        msg: String
    ) {
        super.showNearbyCommunicationTimeoutAlert(cmdStr, isDismissLoadingDialog, false, msg)
        mStates.isResponseLoading.set(false)
        mStates.isResponseSuccess.set(false)
        mStates.responseContent.set("指令响应超时")
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_TERMINAL_TIME -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.QUERY_TERMINAL_TIME
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
                        mStates.isCalibratingSuccess.set(false)
                        mStates.deviceTime.set(result.data)
                        mStates.systemTime.set(TimeUtils.getNowString())
                    }
                }
            }

            IOTCommandType.SET_TERMINAL_TIME -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        Timber.e(result.message)
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(false)
                        mStates.responseContent.set(result.message)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(true)
                        mStates.isCalibratingSuccess.set(true)
                    }
                }
            }

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
//                            val gson = GsonBuilder()
//                                .setPrettyPrinting()
//                                .disableHtmlEscaping()
//                                .create()
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

            IOTCommandType.MD_MR_ARTIFICIAL -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(false)
                        mStates.responseContent.set(result.message)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(true)
                        mStates.responseContent.set("人工置数指令下发成功")
                    }
                }
            }

            IOTCommandType.MD_MR_UPLOAD_FILE -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(false)
                        mStates.responseContent.set(result.message)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(true)
                        mStates.responseContent.set("文件上传指令下发成功")
                    }
                }
            }

            IOTCommandType.MD_BACKUP_CONFIG -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(false)
                        mStates.responseContent.set(result.message)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(true)
                        mStates.responseContent.set("参数导出指令下发成功")
                    }
                }
            }

            IOTCommandType.MD_RESTORE_CONFIG -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(false)
                        mStates.responseContent.set(result.message)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(true)
                        mStates.responseContent.set("参数导入指令下发成功")
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
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