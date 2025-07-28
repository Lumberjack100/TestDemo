package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.m50

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.RtkParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.common.RadioCommunicateInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.RtkParamInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentM50ReportModelParamBinding
import com.shmedo.mcloudapp.extensions.formatDoubleValue
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.M50ReportModelParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/9/10
 * @desc: M50上报工作模式参数设置
 *
 */
class M50ReportModelParamFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentM50ReportModelParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: M50ReportModelParamViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val reportModelList = arrayListOf("常在线", "低功耗", "自适应")
    private val networkModelList = arrayListOf("4G传输", "电台传输", "自动")
    private val workModelList = arrayListOf("基站", "测站")

    // 新增配置项选项列表
    private val coordinateInitializationList = arrayListOf("是", "否")
    private val initializationModeList = arrayListOf("手动", "自动")

    private val initializationTimeList =
        arrayListOf("15分钟", "30分钟", "60分钟", "120分钟", "360分钟", "12小时", "24小时")
    private val calculationIntervalTimeList = arrayListOf(
        "5分钟",
        "10分钟",
        "15分钟",
        "30分钟",
        "60分钟",
        "120分钟",
        "360分钟",
        "12小时",
        "24小时"
    )

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_m50_report_model_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentM50ReportModelParamBinding
        binding.llToolbar.toolbar.title = "工作模式"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            handleBackByCheckDataModified()
        }
        registerOnBackPressedDispatcher {
            handleBackByCheckDataModified()
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
        // 保存初始状态
        mStates.saveInitialState()
    }

    private fun resetDefaultParams() {
        mStates.reportModel.set(reportModelList[0])//默认自适应
        mStates.networkModel.set(networkModelList[2])//默认自动
        mStates.workModel.set(workModelList[1])//默认测站
        mStates.coordinateInitialization.set(coordinateInitializationList[1])//默认否
        mStates.initializationMode.set(initializationModeList[1])//默认自动
        mStates.longitude.set("")//经度
        mStates.latitude.set("")//纬度
        mStates.altitude.set("")//高度

        mStates.initializationTime.set(initializationTimeList[0])//默认15分钟
        mStates.calculationIntervalTime.set(calculationIntervalTimeList[0])//默认5分钟
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 选择上报模式
         */
        fun onReportModelChooseClick() {
            val selectedIndex = reportModelList.indexOf(mStates.reportModel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", reportModelList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.reportModel.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 选择网络模式
         */
        fun onNetworkModelChooseClick() {
            val availableNetworkModelList = arrayListOf<String>()
            availableNetworkModelList.addAll(networkModelList)

            // 当电台未启用时，不能选择电台传输
            if (!mStates.isRadioEnable.get()) {
                // 如果当前已经选择了电台传输，自动切换为4G传输
                if (mStates.networkModel.get() == "电台传输") {
                    mStates.networkModel.set("4G传输")
                    showMessageDialog("电台未启用，网络模式已自动切换为4G传输")
                    return
                }
                // 显示仅包含4G传输和自动的网络模式选项
                availableNetworkModelList.remove("电台传输")
            }

            val selectedIndex = availableNetworkModelList.indexOf(mStates.networkModel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", availableNetworkModelList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.networkModel.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 选择工作模式
         */
        fun onWorkModelChooseClick() {
            val selectedIndex = workModelList.indexOf(mStates.workModel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", workModelList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.workModel.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 选择坐标初始化
         */
        fun onCoordinateInitializationChooseClick() {
            val selectedIndex =
                coordinateInitializationList.indexOf(mStates.coordinateInitialization.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "", coordinateInitializationList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.coordinateInitialization.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 选择初始化模式
         */
        fun onInitializationModeChooseClick() {
            val selectedIndex = initializationModeList.indexOf(mStates.initializationMode.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "", initializationModeList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.initializationMode.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 选择初始化时间
         */
        fun onInitializationTimeChooseClick() {
            val selectedIndex = initializationTimeList.indexOf(mStates.initializationTime.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "", initializationTimeList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.initializationTime.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 选择解算间隔时间
         */
        fun onCalculationIntervalTimeChooseClick() {
            val selectedIndex =
                calculationIntervalTimeList.indexOf(mStates.calculationIntervalTime.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "", calculationIntervalTimeList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.calculationIntervalTime.set(text)
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
        // 检查电台未启用时网络模式是否为电台传输
        if (!mStates.isRadioEnable.get() && mStates.networkModel.get() == "电台传输") {
            showMessageDialog("电台未启用，网络模式不能选择电台传输!")
            return
        }

        // 验证坐标初始化相关配置
        if (mStates.coordinateInitialization.get() == "是") {
            if (mStates.workModel.get() == "基站" && mStates.initializationMode.get() == "手动") {
                // 验证手动模式下的坐标输入
                if (mStates.longitude.get().isEmpty()) {
                    showMessageDialog("请输入经度!")
                    return
                }
                if (mStates.latitude.get().isEmpty()) {
                    showMessageDialog("请输入纬度!")
                    return
                }
                if (mStates.altitude.get().isEmpty()) {
                    showMessageDialog("请输入高度!")
                    return
                }

                // 验证输入格式
                try {
                    mStates.longitude.get().toDouble()
                    mStates.latitude.get().toDouble()
                    mStates.altitude.get().toDouble()
                } catch (ex: Exception) {
                    showMessageDialog("请输入正确的坐标数值!")
                    return
                }
            }
        }

        commandItems.clear()

        if (mStates.coordinateInitialization.get() == "是") {
            if (mStates.workModel.get() == "基站") {
                // 构建保存命令（这里需要根据实际命令协议调整）
                val entity = RtkParamEntity(
                    reportMode = reportModelList.indexOf(mStates.reportModel.get()).toString(),
                    networkMode = networkModelList.indexOf(mStates.networkModel.get()).toString(),
                    mode = (workModelList.indexOf(mStates.workModel.get()) + 1).toString(),
                    baseStationMode = initializationModeList.indexOf(mStates.initializationMode.get())
                        .toString(),
                    latitude = if (mStates.initializationMode.get() == "手动") mStates.latitude.get() else "0",
                    longitude = if (mStates.initializationMode.get() == "手动") mStates.longitude.get() else "0",
                    height = if (mStates.initializationMode.get() == "手动") mStates.altitude.get() else "0",
                )
                val command = IOTCommandUtil.getCommand(
                    IOTCommandType.GM_MD_CFG_RTK,
                    entity.toCommandString()
                )
                commandItems.add(command)
            }

            if (mStates.workModel.get() == "测站") {
                val entity = RtkParamEntity(
                    reportMode = reportModelList.indexOf(mStates.reportModel.get()).toString(),
                    networkMode = networkModelList.indexOf(mStates.networkModel.get()).toString(),
                    mode = (workModelList.indexOf(mStates.workModel.get()) + 1).toString(),
                    basearc = (initializationTimeList.indexOf(mStates.initializationTime.get()) + 1).toString(),
                    arc = (calculationIntervalTimeList.indexOf(mStates.calculationIntervalTime.get()) + 1).toString(),
                )
                var command = IOTCommandUtil.getCommand(
                    IOTCommandType.GM_MD_CFG_RTK,
                    entity.toCommandString()
                )
                commandItems.add(command)

                command =
                    IOTCommandUtil.getCommand(IOTCommandType.MD_FORMAT_DATA_STORAGE, "type=4")
                commandItems.add(command)
            }
        } else {
            val entity = RtkParamEntity(
                reportMode = reportModelList.indexOf(mStates.reportModel.get()).toString(),
                networkMode = networkModelList.indexOf(mStates.networkModel.get()).toString(),
                mode = (workModelList.indexOf(mStates.workModel.get()) + 1).toString(),
            )
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.GM_MD_CFG_RTK,
                entity.toCommandString()
            )
            commandItems.add(command)
        }

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(
            isStartTimeoutJob = true,
            timeoutMillis = AppContants.Communication.DELAY_15000_MILLIS
        )
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()

        //查询电台参数
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_RADIO_CTRL
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(
            IOTCommandType.GM_MD_CFG_RTK,
            "method=0"
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun isTargetCommandType(commandType: IOTCommandType): Boolean =
        (commandType == IOTCommandType.MD_GET_RADIO_CTRL)
                || (commandType == IOTCommandType.GM_MD_CFG_RTK)
                || (commandType == IOTCommandType.MD_FORMAT_DATA_STORAGE)

    /**
     * 4G 下发指令响应失败
     */
    override fun doCmdResponseResultError(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        val isShowMessage = isTargetCommandType(IOTCommandUtil.extractCommandType(cmdStr))
        super.doCmdResponseResultError(
            cmdStr = cmdStr,
            errMsg = errMsg,
            isShowErrMsg = isShowMessage,
            isMessageDialog = isShowMessage
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
        val isShowMessage = isTargetCommandType(IOTCommandUtil.extractCommandType(cmdStr))
        super.doCmdResponseResultTimeOut(
            cmdStr = cmdStr,
            errMsg = errMsg,
            isShowErrMsg = isShowMessage,
            isMessageDialog = isShowMessage
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
        val isShowMessage = isTargetCommandType(IOTCommandUtil.extractCommandType(cmdStr))
        super.showNearbyCommunicationTimeoutAlert(
            cmdStr = cmdStr,
            isDismissLoadingDialog = isDismissLoadingDialog,
            isShowErrMsg = isShowMessage,
            isMessageDialog = isShowMessage,
            errMsg = errMsg
        )
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_RADIO_CTRL -> {
                val result = iotParseManager.parse<RadioCommunicateInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_RADIO_CTRL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询电台参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initRadioData(result.data)
                    }
                }
            }

            IOTCommandType.GM_MD_CFG_RTK -> {
                val result = if (cmdStr.contains("method=0"))
                    iotParseManager.parse<RtkParamInfo>(
                        cmdStr,
                        IOTCommandType.GM_MD_CFG_RTK
                    )
                else iotParseManager.parse<CommonSettingCmdResult>(cmdStr)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg =
                            if (cmdStr.contains("method=0")) "查询信息出错: ${result.message}" else "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        if (cmdStr.contains("method=0")) {
                            initParamData(result.data as RtkParamInfo)
                        } else {
                            processNavigateUp()
                        }
                    }
                }
            }

            IOTCommandType.MD_FORMAT_DATA_STORAGE -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            processNavigateUp()
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initRadioData(data: RadioCommunicateInfo) {
        mStates.isRadioEnable.set(data.sw == "1")

        // 当电台未启用时，如果当前网络模式为"电台传输"，则自动切换为"4G传输"
        if (data.sw != "1" && mStates.networkModel.get() == "电台传输") {
            mStates.networkModel.set("4G传输")
        }
    }

    private fun initParamData(info: RtkParamInfo) {
        try {
            info.reportMode.toIntOrNull()?.let {
                if (it in reportModelList.indices) {
                    mStates.reportModel.set(reportModelList[it])
                }
            }
            info.networkMode.toIntOrNull()?.let {
                if (it in networkModelList.indices) {
                    // 如果电台未启用且网络模式为电台传输，则强制设置为4G传输
                    if (!mStates.isRadioEnable.get() && networkModelList[it] == "电台传输") {
                        mStates.networkModel.set("4G传输")
                    } else {
                        mStates.networkModel.set(networkModelList[it])
                    }
                }
            }
            info.mode.toIntOrNull()?.let {
                if (it in 1..workModelList.size) {
                    mStates.workModel.set(workModelList[it - 1])
                }
            }

            info.baseStationMode.toIntOrNull()?.let {
                if (it in initializationModeList.indices) {
                    mStates.initializationMode.set(initializationModeList[it])
                }
            }
            mStates.longitude.set(info.longitude)//info.longitude.formatDoubleValue("", 1)
            mStates.latitude.set(info.latitude)//info.latitude.formatDoubleValue("", 1)
            mStates.altitude.set(info.height.formatDoubleValue("", 3))

            info.basearc.toIntOrNull()?.let {
                if (it in 1..initializationTimeList.size) {
                    mStates.initializationTime.set(initializationTimeList[it - 1])
                }
            }
            info.arc.toIntOrNull()?.let {
                if (it in 1..calculationIntervalTimeList.size) {
                    mStates.calculationIntervalTime.set(calculationIntervalTimeList[it - 1])
                }
            }

            // 保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    override fun handleBackByCheckDataModified() {
        if (mStates.isDataModified.value == true) {
            showExitConfirmationDialog()
            return
        }
        nav().navigateUp()
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}