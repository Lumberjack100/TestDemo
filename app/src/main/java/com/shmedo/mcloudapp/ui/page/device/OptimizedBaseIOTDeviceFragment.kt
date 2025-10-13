package com.shmedo.mcloudapp.ui.page.device

import android.os.Bundle
import android.util.Log
import androidx.annotation.CallSuper
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.PageRefreshLayout
import com.hjq.toast.ToastParams
import com.hjq.toast.Toaster
import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.communication.manager.DeviceCommunicationManager
import com.shmedo.mcloudapp.communication.model.CommandSequenceCallbacks
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.DeviceConnectionState
import com.shmedo.mcloudapp.communication.model.DeviceError
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.extensions.getAppViewModel
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.BleViewModel
import com.shmedo.mcloudapp.ui.viewmodel.request.NetIOTCommandViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.PageMessenger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/7/25
 * @desc: 优化后的IOT设备基础Fragment，使用新的通信架构，提供更简洁、更可维护的API
 *
 * 主要优化：
 * 1. 职责分离：通信逻辑由DeviceCommunicationManager管理
 * 2. 统一错误处理：支持多种错误处理策略
 * 3. 响应驱动：严格按照"发送→等待响应→发送下一条"的业务逻辑
 * 4. 配置驱动：通过配置对象控制行为
 */
abstract class OptimizedBaseIOTDeviceFragment : BaseFragment() {

    protected lateinit var mMessenger: PageMessenger
    protected val netIotCommandViewModel: NetIOTCommandViewModel by viewModel()
    protected val bleViewModel: BleViewModel by activityViewModel()

    protected var refreshLayout: PageRefreshLayout? = null

    // 设备信息
    protected var productType = ProductType.UnKnown
    protected var statusBarColor = 0
    protected var communicateWay: CommunicateWay = NetPlatformConnect
    protected lateinit var deviceInfo: DeviceInfo
    protected var bleDevice: DiscoveredBluetoothDevice? = null

    // 通信管理器
    protected lateinit var communicationManager: DeviceCommunicationManager

    // 页面唯一标识，用于会话管理
    protected val fragmentId = "Fragment_${this::class.simpleName}_${System.currentTimeMillis()}"

    // 最后通信时间
    protected val lastCommunicationTime = MutableStateFlow(System.currentTimeMillis())

    @CallSuper
    override fun initViewModel() {
        mMessenger = getAppViewModel()
    }

    @CallSuper
    override fun initData() {
        // 从参数中获取设备信息
        arguments?.let {
            productType = it.getParcelable(AppContants.Extras.PRODUCT_TYPE)!!
            communicateWay = it.getParcelable(AppContants.Extras.COMMUNICATION_WAY)!!
            deviceInfo = it.getParcelable(AppContants.Extras.DEVICE_INFO)!!
            bleDevice = it.getParcelable(AppContants.Extras.BLE_DEVICE)
            statusBarColor = it.getInt(AppContants.Extras.STATUS_BAR_COLOR)
        }

        // 初始化通信管理器
        communicationManager = DeviceCommunicationManager(
            fragment = this,
            deviceInfo = deviceInfo,
            communicateWay = communicateWay,
            netViewModel = netIotCommandViewModel,
            bleViewModel = bleViewModel,
        )
    }

    @CallSuper
    override fun createObserver() {
        // 监听连接状态
        launchWithViewLifecycle {
            communicationManager.getConnectionState().collect { state ->
                when (state) {
                    is DeviceConnectionState.Connecting -> onDeviceConnecting()
                    is DeviceConnectionState.Connected -> onDeviceConnected()
                    is DeviceConnectionState.Disconnected -> onDeviceDisconnected()
                    is DeviceConnectionState.Error -> onDeviceConnectionError(state.error)
                }
            }
        }
    }

    // ==================== 新的API方法 ====================
    /**
     * 发送指令序列 (支持实时回调和会话隔离)
     * @param commands 指令列表
     * @param config 执行配置
     * @param callbacks 回调配置
     */
    protected fun sendCommandSequence(
        commands: List<String>,
        config: CommandSequenceConfig = CommandSequenceConfig(),
        callbacks: CommandSequenceCallbacks = CommandSequenceCallbacks(),
    ) {
        // 自动设置会话所有者ID和优先级
        val enhancedConfig = config.copy(
            ownerId = config.ownerId ?: fragmentId,
            priority = config.priority
        )

        Timber.d("页面 $fragmentId 发送指令序列: ${commands.size} 条指令")
        addDeviceLogItem(
            Log.DEBUG,
            "页面 ${this::class.simpleName}，发送指令序列: ${commands.size} 条指令"
        )

        communicationManager.executeCommandSequence(
            commands = commands,
            config = enhancedConfig,
            callbacks = CommandSequenceCallbacks(
                onSuccess = { successResult ->
                    // 根据配置决定是否启用业务层指令响应内容解析失败中断功能
                    if (enhancedConfig.enableBusinessParseFailureInterrupt) {
                        // 启用了业务层解析失败中断功能
                        val shouldContinue = try {
                            handleCommandResponse(successResult.responseData)
                            // 如果handleCommandResponse没有抛出异常，表示响应内容解析成功，继续指令序列执行
                            true
                        } catch (e: Exception) {
                            // 如果handleCommandResponse抛出异常，表示响应内容解析失败，中断指令序列执行
                            Timber.e(e, "页面 $fragmentId 业务解析失败，中断指令序列执行")
                            false
                        }

                        // 调用原始回调（如果有）
                        val originalResult = callbacks.onSuccess?.invoke(successResult)

                        // 返回最终的控制决策：优先使用原始回调的结果，其次使用业务解析结果
                        originalResult ?: shouldContinue

                    } else {
                        // 保持原有逻辑：直接处理响应，不进行中断控制
                        try {
                            handleCommandResponse(successResult.responseData)
                        } catch (e: Exception) {
                            Timber.e(e, "页面 $fragmentId 业务解析失败，继续指令序列执行")
                        }
                        // 调用原始回调
                        callbacks.onSuccess?.invoke(successResult)
                        // 返回null表示使用默认行为（继续执行）
                        null
                    }
                },
                onComplete = { results ->
                    if (enhancedConfig.autoFinishRefreshLayoutOnComplete) {
                        finishRefresh()
                    }
                    Timber.d("页面 $fragmentId 指令序列执行完成")
                    callbacks.onComplete(results)
                },
                onError = { error, command ->
                    if (enhancedConfig.stopOnFirstCmdError) {
                        // 统一的清理逻辑
                        cancelCommunication()
                    }
                    callbacks.onError(error, command)
                }
            )
        )
    }


    // ==================== 便捷方法 ====================

    /**
     * 重启设备 (便捷方法)
     */
    protected fun rebootDevice(
        config: CommandSequenceConfig = CommandSequenceConfig(
            errorConfig = ErrorConfig.dialogConfig()
        ),
        callbacks: CommandSequenceCallbacks = CommandSequenceCallbacks()
    ) {
        val command = IOTCommandUtil.getCommand(IOTCommandType.REBOOT)
        sendCommandSequence(
            commands = listOf(command),
            config = config,
            callbacks = callbacks
        )
    }

    /**
     * 恢复出厂设置指令
     */
    protected open fun restoreFactory(
        config: CommandSequenceConfig = CommandSequenceConfig(
            errorConfig = ErrorConfig.dialogConfig()
        ),
        callbacks: CommandSequenceCallbacks = CommandSequenceCallbacks()
    ) {
        val command = IOTCommandUtil.getCommand(IOTCommandType.RESET)
        sendCommandSequence(
            commands = listOf(command),
            config = config,
            callbacks = callbacks
        )
    }

    /**
     * 设备查找 (便捷方法)
     */
    protected fun searchDevice() {
        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_SEARCH_DEVICE, "switch=1")
        sendCommandSequence(
            commands = listOf(command), config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.device_searching),
                errorConfig = ErrorConfig.toastConfig()
            )
        )
    }

    // ==================== 抽象方法 ====================

    /**
     * 处理指令响应 (由子类实现)
     * 这是唯一需要子类实现的方法，用于处理具体的指令响应逻辑
     */
    abstract fun handleCommandResponse(cmdStr: String)

    // ==================== 状态回调方法 (可重写) ====================

    /**
     * 设备连接中回调
     */
    protected open fun onDeviceConnecting() {}

    /**
     * 设备已连接回调
     */
    protected open fun onDeviceConnected() {}

    /**
     * 设备已断开回调
     */
    protected open fun onDeviceDisconnected() {}

    /**
     * 设备连接错误回调
     */
    protected open fun onDeviceConnectionError(error: DeviceError) {}

    // ==================== 工具方法 ====================

    /**
     * 更新最后通信时间
     */
    protected fun updateLastCommunicationTime() {
        lastCommunicationTime.value = System.currentTimeMillis()
    }

    /**
     * 添加设备日志
     */
    protected fun addDeviceLogItem(priority: Int, data: String) {
        bleViewModel.addLogItem(
            sessionId = CommonMMKVOwner.iotDeviceLogSessionId,
            priority = priority,
            data = data
        )
    }

    /**
     * 结束刷新
     */
    protected fun finishRefresh() {
        refreshLayout?.finish(false)
    }

    /**
     * 检查设备是否已连接
     */
    protected fun isDeviceConnected(): Boolean = communicationManager.isConnected()

    /**
     * 检查是否正在执行指令
     */
    protected fun isCommunicationExecuting(): Boolean = communicationManager.isExecuting()

    protected fun handleFailureResult(
        errMsg: String,
        isShowErrMsg: Boolean = true,
        isMessageDialog: Boolean = false
    ) {
        Timber.e(errMsg)
        if (isShowErrMsg) {
            if (isMessageDialog) showMessageDialog(errMsg)
            else Toaster.show(errMsg)
        }
        // 当启用enableBusinessParseFailureInterrupt时，抛出异常会中断后续指令执行
        throw IllegalStateException(errMsg)
    }

    // ==================== 导航和UI工具方法 ====================

    protected open fun processNavigateUp(toastMsg: String = "", isShowToast: Boolean = true) {
        if (isShowToast) Toaster.show(ToastParams().apply {
            text = toastMsg.ifEmpty { "保存成功" }
            duration = 1000
        })
        launchWithViewLifecycle {
            delay(1000)
            nav().navigateUp()
        }
    }

    protected open fun handleBackByCheckDataModified() {
        // 子类可重写此方法实现数据修改检查
        nav().navigateUp()
    }

    protected open fun showExitConfirmationDialog() {
        showMessage(
            StringUtils.getString(R.string.data_modified_warn),
            "提示",
            "确定",
            { nav().navigateUp() },
            "取消"
        )
    }

    // ==================== 兼容性方法 ====================

    /**
     * 检查是否是蓝牙DAS设备
     */
    protected fun isBleDas(): Boolean {
        return communicateWay == BleConnect && (
                productType == ProductType.DAS ||
                        productType == ProductType.BHY ||
                        productType == ProductType.COLLECTOR_R_1
                )
    }

    override fun isRestrictHiddenMode(): Boolean = true

    // ==================== 会话管理便捷方法 ====================
    /**
     * 取消当前通信
     */
    protected fun cancelCommunication(isFinishRefresh: Boolean = true) {
        if (isFinishRefresh) {
            finishRefresh()
        }

        if (communicateWay == BleConnect) {
            bleViewModel.cancelSession(fragmentId)
        }
        communicationManager.cancelExecution()
    }

    /**
     * 取消当前页面的所有指令
     */
    protected fun cancelAllPageCommands() {
        if (communicateWay == BleConnect) {
            bleViewModel.cancelSession(fragmentId)
        }
        communicationManager.cleanup()
        Timber.i("页面 $fragmentId 取消所有指令")
    }

    override fun onDestroy() {
        // 页面销毁时取消所有相关指令
        Timber.d("页面 $fragmentId 销毁，清理相关会话")
        cancelAllPageCommands()
        super.onDestroy()
    }

    companion object {
        /**
         * 创建Bundle参数 (兼容原有接口)
         */
        fun newBundleArguments(
            type: ProductType = ProductType.UnKnown,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putParcelable(AppContants.Extras.PRODUCT_TYPE, type)
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
} 