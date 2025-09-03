package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.setup
import com.hjq.toast.ToastParams
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
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
import com.shmedo.mcloudapp.databinding.FragmentMr702EquipmentOperationBinding
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showLoadingWithUUID
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.CommonModule
import com.shmedo.mcloudapp.model.DeviceFunctionModule
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
import com.shmedo.mcloudapp.model.UnifiedDeviceModule
import com.shmedo.mcloudapp.model.toUnified
import com.shmedo.mcloudapp.ui.dialog.TelemetryPopupView
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.collector_product.dialog.MR702DeviceDataUploadPopupView
import com.shmedo.mcloudapp.ui.page.device.collector_product.dialog.MR702ManualSettingPopupView
import com.shmedo.mcloudapp.ui.page.device.collector_product.dialog.MR702ParamExportPopupView
import com.shmedo.mcloudapp.ui.page.device.collector_product.dialog.MR702ParamImportPopupView
import com.shmedo.mcloudapp.ui.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702EquipmentOperationViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * @author：gonghe
 * @time: 2023/12/5
 * @desc: 设备操作
 *
 */
class MR702EquipmentOperationFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702EquipmentOperationBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: MR702EquipmentOperationViewModel by viewModels()
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModel()
    private val iotParseManager: IOTParserManager by inject()

    private val monitoringElementList = arrayListOf<MonitoringElement>()
    private var takePhotoLoadingDialogId = ""


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
            addType<UnifiedDeviceModule>(R.layout.item_device_config_module)
            R.id.item.onClick {
                val unifiedModule = getModel<UnifiedDeviceModule>()
                processItemClick(unifiedModule.module)
            }
        }.models = getModuleList()
    }

    private fun processItemClick(functionModule: DeviceFunctionModule) {
        if (!isDeviceConnected()) {
            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
            return
        }
        when (functionModule) {
            is TelemetryDataModule -> {//遥测数据
                val command =
                    IOTCommandUtil.getCommand(IOTCommandType.SAMPLE)

                mStates.isResponseLoading.set(true)
                mStates.isResponseSuccess.set(false)
                showTelemetryDataPopup()

                sendCommandSequence(
                    commands = listOf(command),
                    config = CommandSequenceConfig(
                        timeout = AppContants.Communication.DELAY_10000_MILLIS,//默认10秒超时
                        showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                        errorConfig = ErrorConfig.customConfig { error ->
                            if (error is DeviceError.Timeout) {
                                mStates.isResponseLoading.set(false)
                                mStates.isResponseSuccess.set(false)
                                mStates.responseContent.set("设备未响应")

                            } else {
                                mStates.isResponseLoading.set(false)
                                mStates.isResponseSuccess.set(false)
                                mStates.responseContent.set(error.message)
                            }
                        }
                    )
                )
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
                mStates.sn.set(deviceInfo.deviceToken)
                mStates.deviceId.set(deviceInfo.id.toString())
                mStates.isParamImporting.set(false)
                showParameterImportPopup()
            }

            is MR702ManualPhotoTakingModule -> {//手动拍照
                val command =
                    IOTCommandUtil.getCommand(IOTCommandType.MR_MD_TAKE_PHOTOS, "action=1&linkid=1")

                takePhotoLoadingDialogId =
                    showLoadingWithUUID(StringUtils.getString(R.string.processing))
                sendCommandSequence(
                    commands = listOf(command),
                    config = CommandSequenceConfig(
                        timeout = AppContants.Communication.DELAY_10000_MILLIS,//默认10秒超时
                        showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                        errorConfig = ErrorConfig.customConfig { error ->
                            dismissLoadingDialog(takePhotoLoadingDialogId)
                            showMessageDialog("拍照出错: ${error.message}")
                        }
                    )
                )
            }

            is MR702Remote485SilenceModule -> {//远程485静音
                showMessage(
                    "是否执行远程消警？",
                    "温馨提示",
                    "确定",
                    {
                        val command =
                            IOTCommandUtil.getCommand(IOTCommandType.MR_MD_RS485_CLEAR_ALARM)

                        takePhotoLoadingDialogId =
                            showLoadingWithUUID(StringUtils.getString(R.string.processing))
                        sendCommandSequence(
                            commands = listOf(command),
                            config = CommandSequenceConfig(
                                timeout = AppContants.Communication.DELAY_10000_MILLIS,//默认10秒超时
                                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                                errorConfig = ErrorConfig.customConfig { error ->
                                    dismissLoadingDialog(takePhotoLoadingDialogId)
                                    showMessageDialog("远程消警出错: ${error.message}")
                                }
                            )
                        )
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
                        val command =
                            IOTCommandUtil.getCommand(IOTCommandType.MR_MD_CLEAN_CLEAR_ALARM)

                        takePhotoLoadingDialogId =
                            showLoadingWithUUID(StringUtils.getString(R.string.processing))
                        sendCommandSequence(
                            commands = listOf(command),
                            config = CommandSequenceConfig(
                                timeout = AppContants.Communication.DELAY_10000_MILLIS,//默认10秒超时
                                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                                errorConfig = ErrorConfig.customConfig { error ->
                                    dismissLoadingDialog(takePhotoLoadingDialogId)
                                    showMessageDialog("清除消警出错: ${error.message}")
                                }
                            )
                        )
                    }, "取消"
                )
            }

            is MR702RainSetZeroModule -> {//雨量置零
                showMessage(
                    "是否执行雨量置零？",
                    "温馨提示",
                    "确定",
                    {
                        val command =
                            IOTCommandUtil.getCommand(IOTCommandType.MR_MD_RS485_CLEAR_RAIN_GAUGE)

                        takePhotoLoadingDialogId =
                            showLoadingWithUUID(StringUtils.getString(R.string.processing))
                        sendCommandSequence(
                            commands = listOf(command),
                            config = CommandSequenceConfig(
                                timeout = AppContants.Communication.DELAY_10000_MILLIS,//默认10秒超时
                                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                                errorConfig = ErrorConfig.customConfig { error ->
                                    dismissLoadingDialog(takePhotoLoadingDialogId)
                                    showMessageDialog("雨量置零出错: ${error.message}")
                                }
                            )
                        )
                    },
                    "取消"
                )
            }

            else -> {
                if (functionModule.navId != 0) {
                    val bundle = newBundleArguments(
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                    nav().safeNavigate(
                        functionModule.navId,
                        bundle
                    )
                }
            }
        }
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
                    val command = IOTCommandUtil.getCommand(
                        IOTCommandType.MR_MD_ARTIFICIAL,
                        "type=$type&data=$data&unit=$unit&otime=$otime"
                    )
                    sendManualSettingCommand(command)
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

    private fun sendManualSettingCommand(command:String){
        mStates.isManualSetting.set(true)
        mStates.isResponseLoading.set(true)
        mStates.isResponseSuccess.set(false)

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.customConfig { error ->
                    if (error is DeviceError.Timeout) {
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(false)
                        mStates.responseContent.set("设备未响应")

                    } else {
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(false)
                        mStates.responseContent.set(error.message)
                    }
                }
            )
        )
    }

    /**
     * 显示数据上传弹窗
     */
    private fun showDeviceDataUploadPopup() {
        val popupView = MR702DeviceDataUploadPopupView(requireContext())
        popupView.setTitle("文件上传", mStates)
            .setClickListener(object : MR702DeviceDataUploadPopupView.OnClickListener {
                override fun onConfirmClick(type: Int, time: String) {
                    val command = IOTCommandUtil.getCommand(
                        IOTCommandType.MR_MD_FILE_UPLOAD,
                        "type=$type&timeframe=$time"
                    )
                    sendDeviceDataUploadCommand(command)
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

    private fun sendDeviceDataUploadCommand(command:String){
        mStates.isDeviceDataUploading.set(true)
        mStates.isResponseLoading.set(true)
        mStates.isResponseSuccess.set(false)

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.customConfig { error ->
                    if (error is DeviceError.Timeout) {
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(false)
                        mStates.responseContent.set("设备未响应")

                    } else {
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(false)
                        mStates.responseContent.set(error.message)
                    }
                }
            )
        )
    }


    /**
     * 显示参数导出弹窗
     */
    private fun showParameterExportPopup() {
        val popupView = MR702ParamExportPopupView(requireContext())
        popupView.setTitle("参数导出", mStates)
            .setClickListener(object : MR702ParamExportPopupView.OnClickListener {
                override fun onExportClick() {
                    val command = IOTCommandUtil.getCommand(IOTCommandType.MD_BACKUP_CONFIG)
                    sendParameterExportCommand(command)
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

    private fun sendParameterExportCommand(command:String){
        mStates.isParamExporting.set(true)
        mStates.isResponseLoading.set(true)
        mStates.isResponseSuccess.set(false)

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.customConfig { error ->
                    if (error is DeviceError.Timeout) {
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(false)
                        mStates.responseContent.set("设备未响应")

                    } else {
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(false)
                        mStates.responseContent.set(error.message)
                    }
                }
            )
        )
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
            netIotCommandViewModel.queryCommandResultByMsgID("", arrayListOf(msgID))
        }
    }


    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.SAMPLE -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.SAMPLE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(false)
                        mStates.responseContent.set(result.message)
                        handleFailureResult(result.message, isShowErrMsg = false)
                    }

                    is IOTCommandResult.Success -> {
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
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(false)
                        mStates.responseContent.set(result.message)
                        handleFailureResult(result.message, isShowErrMsg = false)
                    }

                    else -> {
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(true)
                        mStates.responseContent.set("人工置数指令下发成功")
                    }
                }
            }

            IOTCommandType.MR_MD_FILE_UPLOAD -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(false)
                        mStates.responseContent.set(result.message)
                        handleFailureResult(result.message, isShowErrMsg = false)
                    }

                    else -> {
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(true)
                        mStates.responseContent.set("文件上传指令下发成功")
                    }
                }
            }

            IOTCommandType.MD_BACKUP_CONFIG -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(false)
                        mStates.responseContent.set(result.message)
                        handleFailureResult(result.message, isShowErrMsg = false)
                    }

                    else -> {
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(true)
                        mStates.responseContent.set("参数导出指令下发成功")
                    }
                }
            }

            IOTCommandType.MD_RESTORE_CONFIG -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        mStates.isResponseLoading.set(false)
                        mStates.isResponseSuccess.set(false)
                        mStates.responseContent.set(result.message)
                        handleFailureResult(result.message, isShowErrMsg = false)
                    }

                    else -> {
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
                    }

                    else -> {
                        if (!isCommunicationExecuting()) {
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
                    }

                    else -> {
                        if (!isCommunicationExecuting()) {
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
                    }

                    else -> {
                        if (!isCommunicationExecuting()) {
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
                    }

                    else -> {
                        if (!isCommunicationExecuting()) {
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
            }
        }
    }

    private fun getModuleList() =
        arrayListOf(
            TelemetryDataModule().toUnified(),
            MR702ManualSettingModule().toUnified(),
            DeviceLogUploadModule().toUnified(),
            MR702ParameterExportModule().toUnified(),
            MR702ParameterImportModule().toUnified(),
            MR702ManualPhotoTakingModule().toUnified(),
            CommonModule(
                name = "库容计算",
                desc = "采用线性插值法计算公式计算",
                resID = R.drawable.ic_sample,
                navId = R.id.action_global_to_mR702ReservoirCapacityFragment
            ).toUnified(),
            MR702Remote485SilenceModule().toUnified(),
            MR702CleanClearAlarmModule().toUnified(),
            MR702RainSetZeroModule().toUnified(),
        )

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}