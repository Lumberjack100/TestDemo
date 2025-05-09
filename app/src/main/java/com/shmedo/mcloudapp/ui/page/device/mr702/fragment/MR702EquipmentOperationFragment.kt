package com.shmedo.mcloudapp.ui.page.device.mr702.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.brv.utils.setup
import com.hjq.toast.ToastParams
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentMr702EquipmentOperationBinding
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showLoadingWithUUID
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.ConfigModule
import com.shmedo.mcloudapp.model.DeviceLogUploadModule
import com.shmedo.mcloudapp.model.MR702CleanClearAlarmModule
import com.shmedo.mcloudapp.model.MR702ManualPhotoTakingModule
import com.shmedo.mcloudapp.model.MR702ManualSettingModule
import com.shmedo.mcloudapp.model.MR702ParameterExportModule
import com.shmedo.mcloudapp.model.MR702ParameterImportModule
import com.shmedo.mcloudapp.model.MR702RainSetZeroModule
import com.shmedo.mcloudapp.model.MR702Remote485SilenceModule
import com.shmedo.mcloudapp.model.MonitoringElement
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
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import org.koin.android.ext.android.inject

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
    private var takePhotoLoadingDialogId = ""


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
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
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
        when (module.functionModule) {
            is TimeCalibrationModule -> {//时间校准
                commandItems.clear()
                val command =
                    IOTCommandUtil.getCommand(IOTCommandType.QUERY_TERMINAL_TIME)
                commandItems.add(command)

                if (communicateWay is BleConnect) {
                    mStates.isResponseLoading.set(true)
                    showTimeCalibrationPopup()
                }
                sendCommandFromCmdList(isStartTimeoutJob = true)
            }

            is TelemetryDataModule -> {//遥测数据
                commandItems.clear()
                val command =
                    IOTCommandUtil.getCommand(IOTCommandType.SAMPLE)
                commandItems.add(command)

                if (communicateWay is BleConnect) {
                    mStates.isResponseLoading.set(true)
                    showTelemetryDataPopup()
                }
                sendCommandFromCmdList(isStartTimeoutJob = true)
            }

            is MR702ManualSettingModule -> {//人工置数
                mStates.isManualSetting.set(false)
                showManualSettingPopup()
            }

            is DeviceLogUploadModule -> {//数据上传
                mStates.isDeviceDataUploading.set(false)
                showDeviceDataUploadPopup()
            }

            is MR702ParameterExportModule -> {//参数导出
                mStates.isParamExporting.set(false)
                showParameterExportPopup()
            }

            is MR702ParameterImportModule -> {//参数导入
                mStates.isParamImporting.set(false)
                mStates.sn.set(deviceInfo.deviceToken)
                mStates.deviceId.set(deviceInfo.id.toString())
                showParameterImportPopup()
            }

            is MR702ManualPhotoTakingModule -> {//手动拍照
                commandItems.clear()
                val command =
                    IOTCommandUtil.getCommand(IOTCommandType.MR_MD_TAKE_PHOTOS, "action=1&linkid=1")
                commandItems.add(command)

                takePhotoLoadingDialogId =
                    showLoadingWithUUID(StringUtils.getString(R.string.processing))
                sendCommandFromCmdList(isStartTimeoutJob = true)
            }

            is MR702Remote485SilenceModule -> {//远程485静音
                showMessage(
                    "是否执行远程消警？",
                    "温馨提示",
                    "确定",
                    {
                        commandItems.clear()
                        val command =
                            IOTCommandUtil.getCommand(IOTCommandType.MR_MD_RS485_CLEAR_ALARM)
                        commandItems.add(command)

                        takePhotoLoadingDialogId =
                            showLoadingWithUUID(StringUtils.getString(R.string.processing))
                        sendCommandFromCmdList(isStartTimeoutJob = true)
                    },
                    "取消"
                )
            }

            is MR702CleanClearAlarmModule -> {//清除消警
                showMessage(
                    "是否执行清除消警？",
                    "温馨提示",
                    "确定",
                    {
                        commandItems.clear()
                        val command =
                            IOTCommandUtil.getCommand(IOTCommandType.MR_MD_CLEAN_CLEAR_ALARM)
                        commandItems.add(command)

                        takePhotoLoadingDialogId =
                            showLoadingWithUUID(StringUtils.getString(R.string.processing))
                        sendCommandFromCmdList(isStartTimeoutJob = true)
                    }
                )
            }

            is MR702RainSetZeroModule -> {//雨量置零
                showMessage(
                    "是否执行雨量置零？",
                    "温馨提示",
                    "确定",
                    {
                        commandItems.clear()
                        val command =
                            IOTCommandUtil.getCommand(IOTCommandType.MR_MD_RS485_CLEAR_RAIN_GAUGE)
                        commandItems.add(command)

                        takePhotoLoadingDialogId =
                            showLoadingWithUUID(StringUtils.getString(R.string.processing))
                        sendCommandFromCmdList(isStartTimeoutJob = true)
                    },
                    "取消"
                )
            }

            else -> {
                if (module.functionModule.navId != 0) {
                    val bundle = BaseIOTDeviceFragment.newBundleArguments(
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                    nav().safeNavigate(
                        module.functionModule.navId,
                        bundle
                    )
                }
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
                        IOTCommandType.MR_MD_ARTIFICIAL,
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
                        IOTCommandType.MR_MD_FILE_UPLOAD,
                        "type=$type&timeframe=$time"
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

            IOTCommandType.SAMPLE -> {
                mStates.isResponseLoading.set(true)
                mStates.isResponseSuccess.set(false)
                showTelemetryDataPopup()
            }

            IOTCommandType.MR_MD_ARTIFICIAL -> {
                mStates.isManualSetting.set(true)
                mStates.isResponseLoading.set(true)
                mStates.isResponseSuccess.set(false)
            }

            IOTCommandType.MR_MD_FILE_UPLOAD -> {
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

    /**
     * 4G 下发指令响应失败
     */
    override fun doCmdResponseResultError(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MR_MD_TAKE_PHOTOS,
            IOTCommandType.MR_MD_RS485_CLEAR_ALARM,
            IOTCommandType.MR_MD_CLEAN_CLEAR_ALARM,
            IOTCommandType.MR_MD_RS485_CLEAR_RAIN_GAUGE -> {
                dismissLoadingDialog(takePhotoLoadingDialogId)
                super.doCmdResponseResultError(
                    cmdStr = cmdStr,
                    errMsg = errMsg,
                    isShowErrMsg = true,
                    isMessageDialog = true
                )
            }

            else -> {
            }
        }
        mStates.isResponseLoading.set(false)
        mStates.isResponseSuccess.set(false)
        mStates.responseContent.set(errMsg)
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
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MR_MD_TAKE_PHOTOS,
            IOTCommandType.MR_MD_RS485_CLEAR_ALARM,
            IOTCommandType.MR_MD_CLEAN_CLEAR_ALARM,
            IOTCommandType.MR_MD_RS485_CLEAR_RAIN_GAUGE -> {
                dismissLoadingDialog(takePhotoLoadingDialogId)
                super.doCmdResponseResultTimeOut(
                    cmdStr = cmdStr,
                    errMsg = errMsg,
                    isShowErrMsg = true,
                    isMessageDialog = true
                )
            }

            else -> {
            }
        }
        mStates.isResponseLoading.set(false)
        mStates.isResponseSuccess.set(false)
        mStates.responseContent.set("设备未响应")
    }

    override fun showNearbyCommunicationTimeoutAlert(
        cmdStr: String,
        isDismissLoadingDialog: Boolean,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean,
        errMsg: String
    ) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MR_MD_TAKE_PHOTOS,
            IOTCommandType.MR_MD_RS485_CLEAR_ALARM,
            IOTCommandType.MR_MD_CLEAN_CLEAR_ALARM,
            IOTCommandType.MR_MD_RS485_CLEAR_RAIN_GAUGE -> {
                dismissLoadingDialog(takePhotoLoadingDialogId)
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr = cmdStr,
                    isDismissLoadingDialog = isDismissLoadingDialog,
                    isShowErrMsg = true,
                    isMessageDialog = true,
                    errMsg = "设备未响应"
                )
            }

            else -> {
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr = cmdStr,
                    isDismissLoadingDialog = isDismissLoadingDialog,
                    isShowErrMsg = false,
                    isMessageDialog = isMessageDialog,
                    errMsg = errMsg
                )
            }
        }
        mStates.isResponseLoading.set(false)
        mStates.isResponseSuccess.set(false)
        mStates.responseContent.set("设备未响应")
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
                        handleFailureResult(result.message, isShowErrMsg = false)
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
                        handleFailureResult(result.message, isShowErrMsg = false)
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

            IOTCommandType.SAMPLE -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.SAMPLE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        handleFailureResult(result.message, isShowErrMsg = false)
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

            IOTCommandType.MR_MD_ARTIFICIAL -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        handleFailureResult(result.message, isShowErrMsg = false)
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

            IOTCommandType.MR_MD_FILE_UPLOAD -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        handleFailureResult(result.message, isShowErrMsg = false)
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
                        handleFailureResult(result.message, isShowErrMsg = false)

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
                        handleFailureResult(result.message, isShowErrMsg = false)
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

            IOTCommandType.MR_MD_TAKE_PHOTOS -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        dismissLoadingDialog(takePhotoLoadingDialogId)
                        val errMsg = "抓拍失败: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            dismissLoadingDialog(takePhotoLoadingDialogId)
                            Toaster.show(ToastParams().apply {
                                text = "抓拍成功"
                                duration = 1000
                            })
                        }
                    }
                }
            }

            IOTCommandType.MR_MD_RS485_CLEAR_ALARM -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        dismissLoadingDialog(takePhotoLoadingDialogId)
                        val errMsg = "消警失败: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            dismissLoadingDialog(takePhotoLoadingDialogId)
                            Toaster.show(ToastParams().apply {
                                text = "消警成功"
                                duration = 1000
                            })
                        }
                    }
                }
            }

            IOTCommandType.MR_MD_CLEAN_CLEAR_ALARM -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        dismissLoadingDialog(takePhotoLoadingDialogId)
                        val errMsg = "清除消警失败: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            dismissLoadingDialog(takePhotoLoadingDialogId)
                            Toaster.show(ToastParams().apply {
                                text = "清除消警成功"
                                duration = 1000
                            })
                        }
                    }
                }
            }
                        

            IOTCommandType.MR_MD_RS485_CLEAR_RAIN_GAUGE -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        dismissLoadingDialog(takePhotoLoadingDialogId)
                        val errMsg = "雨量置零失败: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            dismissLoadingDialog(takePhotoLoadingDialogId)
                            Toaster.show(ToastParams().apply {
                                text = "雨量置零成功"
                                duration = 1000
                            })
                        }
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
            ConfigModule(MR702ManualSettingModule()),
            ConfigModule(DeviceLogUploadModule()),
            ConfigModule(MR702ParameterExportModule()),
            ConfigModule(MR702ParameterImportModule()),
            ConfigModule(MR702ManualPhotoTakingModule()),
            ConfigModule(
                CommonModule(
                    name = "库容计算",
                    desc = "采用线性插值法计算公式计算",
                    resID = R.drawable.ic_sample,
                    navId = R.id.action_global_to_mR702ReservoirCapacityFragment
                )
            ),
            ConfigModule(MR702Remote485SilenceModule()),
            ConfigModule(MR702CleanClearAlarmModule()),
            ConfigModule(MR702RainSetZeroModule()),
        )

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}