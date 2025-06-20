package com.shmedo.mcloudapp.ui.page.device.common

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.brv.BindingAdapter.BindingViewHolder
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
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
import com.shmedo.mcloudapp.baseclickproxy.DoubleClickListener
import com.shmedo.mcloudapp.databinding.FragmentUniversalDeviceHomeNewBinding
import com.shmedo.mcloudapp.databinding.ItemSubConfigModuleBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showDialogFragment
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.ConfigModule
import com.shmedo.mcloudapp.model.ConfigModuleTree
import com.shmedo.mcloudapp.model.DeviceFunctionModule
import com.shmedo.mcloudapp.model.DeviceStatusEnum
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.u_product.dialog.FindDeviceBeepDialog
import com.shmedo.mcloudapp.ui.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.CommonDeviceHomeViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.isActive
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2025/6/9
 * 描述： TODO
 */
abstract class NewUniversalBaseDeviceHomeFragment : BaseIOTDeviceFragment() {
    protected lateinit var binding: FragmentUniversalDeviceHomeNewBinding
    protected val toolbarViewModel: ToolbarViewModel by viewModels()
    protected val mHeadStates: CommonDeviceHomeViewModel by viewModels()
    protected val deviceRequestViewModel: DeviceRequestViewModel by viewModel()

    protected val iotParseManager: IOTParserManager by inject()

    private var lastOnlineStatus: Boolean = false//在线状态
    private var deviceStatusCheckJob: Job? = null


    override fun initViewModel() {
        super.initViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_universal_device_home_new,
            BR.stateVM,
            mHeadStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUniversalDeviceHomeNewBinding
        binding.llToolbar.toolbar.title = "返回"
        binding.llToolbar.toolbar.setNavigationOnClickListener {
            if (bleViewModel.isConnected()) {
                bleViewModel.disconnect()
            }
            mActivity.finish()
        }
        registerOnBackPressedDispatcher {
            if (bleViewModel.isConnected()) {
                bleViewModel.disconnect()
            }
            mActivity.finish()
        }
        initDeviceLogoDoubleClickListener()
        initModuleAdapter()
    }

    private fun initDeviceLogoDoubleClickListener() {
        binding.llDeviceInfo.ivDeviceLogo.setOnClickListener(object : DoubleClickListener() {
            override fun onDoubleClick(v: View) {
                if (isBleDisconnected() || isNetDisconnected()) {
                    return
                }
                searchDevice()
            }
        })
    }

    override fun initData() {
        super.initData()
        toolbarViewModel.toolbarIvActionVisible.set(communicateWay is BleConnect)

        mHeadStates.productName.set(productType.productName.ifEmpty { deviceInfo.productName })
        val deviceName =
            if (deviceInfo.deviceName == deviceInfo.deviceToken) deviceInfo.productToken else deviceInfo.deviceName.ifEmpty { deviceInfo.deviceToken }
        mHeadStates.productToken.set(productType.productToken.ifEmpty { deviceName })
        mHeadStates.deviceToken.set(deviceInfo.deviceToken)

        initModuleData()
    }

    override fun onConnectionStateChanged(isConnected: Boolean) {
        mHeadStates.isConnected.set(isConnected)
        if (isConnected) {
            mHeadStates.productLogoResId.set(mHeadStates.productNormalResId.get())
            mHeadStates.iotPlatformStateText.set("蓝牙已连接")
            toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_ble_disconnect)

        } else {
            mHeadStates.productLogoResId.set(mHeadStates.productOfflineResId.get())
            mHeadStates.iotPlatformStateText.set("蓝牙已断开")
            toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_ble_connect)

            mHeadStates.deviceStatusCode.set(DeviceStatusEnum.UNKNOWN.code)
        }

        //刷新模块状态
        binding.rvModule.models?.forEach {
            if (it is ConfigModuleTree) {
                it.configModules.forEach { configModule ->
                    configModule.functionModule.refreshStatus(isConnected)
                }
            }
        }
    }

    private fun initModuleAdapter() {
        binding.rvModule.linear().setup { rv ->
            addType<DeviceStatusInfoGroupItem>(R.layout.item_device_status_info_group2)
            addType<ConfigModuleTree>(R.layout.item_sub_config_module)
            addType<GapItem>(R.layout.item_device_status_info_gap)
            onCreate {
                when (itemViewType) {
                    R.layout.item_sub_config_module -> {
                        val itemBinding = getBinding<ItemSubConfigModuleBinding>()
                        itemBinding.rvSubModule.setup { subRv ->
                            subRv.addItemDecoration(
                                MyGridSpacingItemDecoration(
                                    4,
                                    ConvertUtils.dp2px(10f), false
                                )
                            )
                            addType<ConfigModule>(R.layout.item_device_config_module_ud)
                            R.id.item.onClick {
                                val configModule = getModel<ConfigModule>()
                                processSubModuleItemClick(configModule.functionModule)
                            }
                        }
                    }

                    else -> {}
                }
            }
            onBind {
                when (itemViewType) {
                    R.layout.item_sub_config_module -> {
                        val configModuleTree = getModel<ConfigModuleTree>()
                        val itemBinding = getBinding<ItemSubConfigModuleBinding>()
                        itemBinding.rvSubModule.models = configModuleTree.configModules
                    }

                    else -> {
                        processOtherItemViewBind(itemViewType)
                    }
                }
            }
        }
    }

    protected open fun BindingViewHolder.processOtherItemViewBind(viewId: Int) {}

    protected abstract fun initModuleData()

    inner class ClickProxy : BaseClickProxy() {
        override fun onToolbarIvClick() {
            if (bleViewModel.isConnected()) {
                bleViewModel.disconnect()
            } else {
                bleViewModel.launch(bleDevice!!)
            }
        }
    }

    private fun processSubModuleItemClick(module: DeviceFunctionModule) {
        if (isBleDisconnected()) {
            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
            return
        }
        when (module) {
//            is CommandDebugConfigModule -> {//指令下发
//                val bundle = BleCustomCommandLogPrintFragment.newBundleArguments(
//                    true,
//                    productType,
//                    communicateWay,
//                    deviceInfo,
//                    bleDevice
//                )
//                nav().safeNavigate(module.navId, bundle)
//            }

            else -> {
                processOtherItemClick(module)
            }
        }
    }

    protected open fun processOtherItemClick(configModule: DeviceFunctionModule) {
        if (configModule.navId != 0) {
            val bundle = BaseIOTDeviceFragment.newBundleArguments(
                productType,
                communicateWay,
                deviceInfo,
                bleDevice
            )
            nav().safeNavigate(
                configModule.navId,
                bundle
            )
        } else {
            Toaster.show("正在开发中")
        }
    }

    override fun lazyLoadData() {
        //4G 模式下，直接查询设备工作模式
        if (communicateWay is NetPlatformConnect) {
            onNetPlatformReady()
        } else {
            bleViewModel.launch(bleDevice!!)
        }
    }

    private fun onNetPlatformReady() {
        lastOnlineStatus = deviceInfo.onlineStatus
        if (deviceInfo.onlineStatus) {
            mHeadStates.productLogoResId.set(mHeadStates.productNormalResId.get())
            mHeadStates.iotPlatformStateText.set("米度平台在线")
            queryStatusInfo()
        } else {
            mHeadStates.productLogoResId.set(mHeadStates.productOfflineResId.get())
            mHeadStates.iotPlatformStateText.set("米度平台离线")

            mHeadStates.deviceStatusCode.set(DeviceStatusEnum.UNKNOWN.code)
        }
        //刷新模块状态
        binding.rvModule.models?.forEach {
            if (it is ConfigModuleTree) {
                it.configModules.forEach { configModule ->
                    configModule.functionModule.refreshStatus(deviceInfo.onlineStatus)
                }
            }
        }
    }

    override fun onBleDeviceReady() {
        super.onBleDeviceReady()
        queryStatusInfo()
    }

    open fun queryStatusInfo() {

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
            IOTCommandType.MD_SEARCH_DEVICE -> {
                super.doCmdResponseResultError(
                    cmdStr = cmdStr,
                    errMsg = "设备查找出错: $errMsg",
                    isShowErrMsg = true,
                    isMessageDialog = true
                )
            }

            else -> {
                super.doCmdResponseResultError(
                    cmdStr = cmdStr,
                    errMsg = errMsg,
                    isShowErrMsg = isShowErrMsg,
                    isMessageDialog = isMessageDialog
                )
            }
        }
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
            IOTCommandType.MD_SEARCH_DEVICE -> {
                super.doCmdResponseResultTimeOut(
                    cmdStr = cmdStr,
                    errMsg = errMsg,
                    isShowErrMsg = true,
                    isMessageDialog = true
                )
            }

            else -> {
                super.doCmdResponseResultTimeOut(
                    cmdStr = cmdStr,
                    errMsg = errMsg,
                    isShowErrMsg = isShowErrMsg,
                    isMessageDialog = isMessageDialog
                )
            }
        }
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
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_SEARCH_DEVICE -> {
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr = cmdStr,
                    isDismissLoadingDialog = isDismissLoadingDialog,
                    isShowErrMsg = true,
                    isMessageDialog = true,
                    errMsg = errMsg
                )
            }

            else -> {
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr = cmdStr,
                    isDismissLoadingDialog = isDismissLoadingDialog,
                    isShowErrMsg = isShowErrMsg,
                    isMessageDialog = isMessageDialog,
                    errMsg = errMsg
                )
            }
        }
    }

    override fun setResultData(cmdStr: String) {
        updateLastCommunicationTime()

        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_SEARCH_DEVICE -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设备查找出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            showDialogFragment(FindDeviceBeepDialog.TAG) {
                                FindDeviceBeepDialog.newInstance(productType)
                            }
                        }
                    }
                }
            }

            else -> {
                processOtherCmdResult(
                    IOTCommandUtil.extractCommandType(cmdStr),
                    cmdStr
                )
            }
        }
    }

    protected open fun processOtherCmdResult(commandType: IOTCommandType, cmdStr: String) {}

    override fun createObserver() {
        super.createObserver()
        setupHeartbeat()
        if (communicateWay is NetPlatformConnect) {
            checkDeviceOnlineStatus()
        }
    }

    /**
     * 设置心跳检查
     */
    protected open fun setupHeartbeat() {
        launchWithViewLifecycle {
            lastCommunicationTime
                .debounce(AppContants.Communication.DELAY_20000_MILLIS)  //20秒无更新触发
                .collect { lastUpdateTime ->
                    val updateTime =
                        TimeUtils.millis2String(lastUpdateTime, "yyyy-MM-dd HH:mm:ss")
                    //仅当设备连接并且需要发送心跳时，才发送心跳包
                    if (mHeadStates.isConnected.get()) {
                        Timber.Forest.d("发送心跳包指令 startTime: ${TimeUtils.getNowString()}，lastUpdateTime：$updateTime")
                        val command = IOTCommandUtil.getCommand(IOTCommandType.HEART_BEAT)
                        Timber.Forest.d("发送心跳包指令: $command")
                        sendBleCommand(command)
                    }
                }
        }
    }

    private fun checkDeviceOnlineStatus() {
        // 取消现有的job
        deviceStatusCheckJob?.cancel()

        // 创建新的job，每30秒执行一次
        deviceStatusCheckJob = launchWithViewLifecycle {
            while (isActive) {
                try {
                    deviceRequestViewModel.getDeviceDetailInfo(deviceInfo.deviceToken) { error: Throwable ->
                        addDeviceLogItem(Log.ERROR, error.errorMsg)
                    }?.let { deviceDetailInfo ->
                        // 如果设备在线状态发生变化，更新UI
                        if (deviceDetailInfo.deviceInfo.onlineStatus != lastOnlineStatus) {
                            onNetPlatformReady()
                        }
                    }
                } catch (e: Exception) {
                    Timber.Forest.e(e)
                }
                delay(30000) // 延迟30秒
            }
        }
    }

    // 在 onDestroy 中取消 job
    override fun onDestroy() {
        super.onDestroy()
        deviceStatusCheckJob?.cancel()
        deviceStatusCheckJob = null
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

}