package com.shmedo.mcloudapp.ui.page.device.common

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import com.shmedo.core.model.DebugCmdLogInfo
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.enums.MDLogOutputStatus
import com.shmedo.lib.cmd.base.md_cmd.enums.MDWorkModel
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.lib.cmd.base.md_cmd.utils.MDConstants
import com.shmedo.lib.tcp.TcpConnectClosed
import com.shmedo.lib.tcp.TcpConnectError
import com.shmedo.lib.tcp.TcpConnectedResult
import com.shmedo.lib.tcp.TcpIdleResult
import com.shmedo.lib.tcp.TcpSuccessDataResult
import com.shmedo.lib.tcp.TcpSuccessRawDataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.BuildConfig
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseCommandLogPrintClickProxy
import com.shmedo.mcloudapp.databinding.FragmentTcpDebugBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.ProductConfigViewModel
import com.shmedo.mcloudapp.ui.viewmodel.request.TcpViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.TcpDebugViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * @author：gonghe
 * @time: 2025/6/20
 * @desc: 设备远程连接调试页面
 *
 */
class TcpDebugFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentTcpDebugBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: TcpDebugViewModel by viewModels()
    private val tcpViewModel: TcpViewModel by activityViewModel()
    private val productConfigViewModel: ProductConfigViewModel by viewModel()

    private var isIotCmd = true
    private var isRawMode = true // 是否为原始模式（不使用分隔符）


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_tcp_debug,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentTcpDebugBinding
        setupToolbar()
        setupRecyclerView()
    }

    private fun setupToolbar() {
        binding.toolbar.title = "远程调试"
        binding.toolbar.setNavigationOnClickListener {
            handleBackPressed()
        }
        registerOnBackPressedDispatcher {
            handleBackPressed()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerview.setup {
            addType<DebugCmdLogInfo>(R.layout.item_debug_cmd_log)
        }
    }

    private fun observeLogItems() {
        // 观察日志数据变化
        mStates.logItems.observe(viewLifecycleOwner) { logItems ->
            try {
                binding.recyclerview.bindingAdapter.addModels(logItems, true)
                if (logItems.isNotEmpty()) {
                    binding.recyclerview.smoothScrollToPosition(binding.recyclerview.mutable.size - 1)
                }
            } catch (e: Exception) {
                Timber.e(e, "更新日志列表失败")
            }
        }
    }

    override fun initData() {
        super.initData()
        try {
            enableCommandDebugMode()
            parseArguments()
            clearLogs()
        } catch (e: Exception) {
            Timber.e(e, "初始化数据失败")
            Toaster.show("初始化失败")
        }
    }

    private fun enableCommandDebugMode() {
        //开启指令调试模式
        CommonMMKVOwner.isCommandDebugMode = true
    }

    private fun parseArguments() {
        isIotCmd = productType != ProductType.COLLECTOR_R_1 &&
                productType != ProductType.DAS &&
                productType != ProductType.BHY
    }

    override fun lazyLoadData() {
        establishTcpConnection()
    }

    private fun establishTcpConnection() {
        try {
            addLog("正在建立远程 TCP 连接...", ColorUtils.getColor(R.color.title_text_color))

            launchWithViewLifecycle {
                try {
                    val deviceDebugAddress = productConfigViewModel.getRemoteDeviceLogin(
                        deviceSn = deviceInfo.deviceToken,
                        deviceKey = deviceInfo.apikey.ifEmpty { "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9" },
                    ) { error: Throwable ->
                        handleConnectionError("获取远程服务器地址和端口信息失败", error)
                    } ?: return@launchWithViewLifecycle

                    updateConnectionInfo(
                        deviceDebugAddress.deviceServerInfo.serverAddr,
                        deviceDebugAddress.deviceServerInfo.serverPort
                    )

                    initializeTcpClient(
                        deviceDebugAddress.deviceServerInfo.serverAddr,
                        deviceDebugAddress.deviceServerInfo.serverPort
                    )
                } catch (e: Exception) {
                    handleConnectionError("建立TCP连接失败", e)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "建立TCP连接异常")
            Toaster.show("连接失败")
        }
    }

    private fun updateConnectionInfo(serverAddr: String, serverPort: Int) {
        binding.toolbar.subtitle = "$serverAddr:$serverPort"
    }

    private fun initializeTcpClient(serverAddr: String, serverPort: Int) {
        try {
            if (isRawMode) {
                addLog("已启用原始模式（无分隔符）", ColorUtils.getColor(R.color.title_text_color))
            } else {
                // 传统模式：使用分隔符
                addLog("已启用传统模式（使用分隔符）", ColorUtils.getColor(R.color.title_text_color))
            }

            tcpViewModel.connectToDevice(
                serverAddr,
                serverPort,
                false,
                if (isRawMode) null else MDConstants.COMMAND_FOOTER
            )

        } catch (e: Exception) {
            handleConnectionError("初始化TCP客户端失败", e)
        }
    }

    private fun handleConnectionError(message: String, error: Throwable) {
        val errorMsg = "$message：${error.message}"
        addLog(errorMsg, ColorUtils.getColor(R.color.error_FF4400))
        Timber.e(error, message)
    }

    override fun createObserver() {
        super.createObserver()
        launchWithViewLifecycle {
            try {
                collectTcpData()
            } catch (e: Exception) {
                Timber.e(e, "观察TCP数据失败")
            }
        }
        observeLogItems()
    }

    private suspend fun collectTcpData() {
        tcpViewModel.data.collect { state ->
            Timber.v("${javaClass.simpleName} Tcp State: $state")
            try {
                when (state) {
                    is TcpIdleResult -> {
                        // 空闲状态，无需处理
                    }

                    is TcpConnectedResult -> {
                        handleTcpConnected()
                    }

                    is TcpSuccessDataResult -> {
                        val data = state.data
                        if (data.isNotEmpty()) {
                            handleReceiveMsgFromTCPServer(data)
                        }
                    }

                    is TcpSuccessRawDataResult -> {
                        val data = state.data
                        if (data.isNotEmpty()) {
                            handleReceiveRawDataFromTCPServer(data)
                        }
                    }

                    is TcpConnectClosed -> {
                        handleTcpDisconnected("TCP 连接断开")
                    }

                    is TcpConnectError -> {
                        handleTcpDisconnected("TCP 连接异常")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "处理TCP状态失败")
            }
        }
    }

    private fun handleTcpConnected() {
        mStates.tcpConnected.set(true)
        addLog("TCP 连接成功", ColorUtils.getColor(R.color.online_colorPrimary))
    }

    private fun handleTcpDisconnected(message: String) {
        mStates.tcpConnected.set(false)
        addLog(message, ColorUtils.getColor(R.color.error_FF4400))
    }

    override suspend fun collectBleConnectionState() {
        // 监听连接状态变化
        bleViewModel.connectionState.collect { state ->
            try {
                handleBleConnectionState(state)
            } catch (e: Exception) {
                Timber.e(e, "处理蓝牙状态失败")
            }
        }
    }

    override suspend fun collectBleCommandData() {
        bleViewModel.commandData.collect { data ->
            try {
                handleBleResponseContentFromDevice(data.response)
            } catch (e: Exception) {
                Timber.e(e, "处理蓝牙数据失败")
            }
        }
    }

    private fun handleBleConnectionState(state: ConnectionState) {
        when (state) {
            ConnectionState.Connecting -> {
                addLog(
                    "正在连接蓝牙(${bleDevice?.address})...",
                    ColorUtils.getColor(R.color.title_text_color)
                )
            }

            is ConnectionState.Initializing -> {
                addLog("蓝牙连接成功", ColorUtils.getColor(R.color.online_colorPrimary))
            }

            is ConnectionState.Ready -> {
                addLog("蓝牙已就绪", ColorUtils.getColor(R.color.title_text_color))
            }

            is ConnectionState.Disconnected -> {
                when (state.reason) {
                    ConnectionState.Disconnected.Reason.LINK_LOSS -> {
                        addLog(
                            "蓝牙连接断开, reason: device link loss",
                            ColorUtils.getColor(R.color.error_FF4400)
                        )
                    }

                    ConnectionState.Disconnected.Reason.NOT_SUPPORTED -> {
                        addLog(
                            "蓝牙连接失败, reason: device missing service",
                            ColorUtils.getColor(R.color.error_FF4400)
                        )
                    }

                    else -> {
                        addLog(
                            "蓝牙连接断开, reason: ${state.reason}",
                            ColorUtils.getColor(R.color.error_FF4400)
                        )
                    }
                }
            }

            else -> {}
        }
    }

    inner class ClickProxy : BaseCommandLogPrintClickProxy() {
        override fun onConnectOperateClick() {
            handleTcpConnectionToggle()
        }

        override fun onToolbarIvClick() {
            showMoreMenu()
        }
    }

    private fun handleTcpConnectionToggle() {
        try {
            if (tcpViewModel.isConnected()) {
                tcpViewModel.disconnect()
            } else {
                tcpViewModel.connect()
            }
        } catch (e: Exception) {
            Timber.e(e, "切换TCP连接状态失败")
            Toaster.show("操作失败")
        }
    }

    /**
     * 处理接收的 TCP 消息，通过蓝牙转发给设备
     */
    private fun handleReceiveMsgFromTCPServer(cmdStr: String) {
        try {
            if (cmdStr.isEmpty()) return

            val cleanedMsg = cmdStr.replace(MDConstants.COMMAND_FOOTER, "")
            addLog(cleanedMsg, ColorUtils.getColor(R.color.receive_data_color))

            if (isBleDisconnected()) {
                addLog("蓝牙连接已断开", ColorUtils.getColor(R.color.error_FF4400))
                return
            }

            sendBleCommand(cmdStr)
        } catch (e: Exception) {
            Timber.e(e, "处理TCP消息失败")
        }
    }

    /**
     * 处理原始 TCP 数据，通过蓝牙转发给设备
     */
    private fun handleReceiveRawDataFromTCPServer(data: ByteArray) {
        try {
            if (data.isEmpty()) return

            val dataStr = String(data, Charsets.UTF_8)
            addLog("Raw TCP: $dataStr", ColorUtils.getColor(R.color.receive_data_color))

            if (isBleDisconnected()) {
                addLog("蓝牙连接已断开", ColorUtils.getColor(R.color.error_FF4400))
                return
            }

            // 直接转发原始字节数据到 BLE
            sendBleCommand(dataStr)
        } catch (e: Exception) {
            Timber.e(e, "处理原始TCP数据失败")
        }
    }

    /**
     * 处理设备响应内容，通过 TCP 转发给远程调试客户端
     */
    private fun handleBleResponseContentFromDevice(cmdStr: String) {
        try {
            addLog(cmdStr, ColorUtils.getColor(R.color.send_data_color))

            if (!tcpViewModel.isConnected()) {
                addLog("TCP 连接已断开", ColorUtils.getColor(R.color.error_FF4400))
                return
            }

            if (isRawMode) {
                // 原始模式：发送字节数据
                val data = cmdStr.toByteArray(Charsets.UTF_8)
                tcpViewModel.sendRawDataToServer(data)
            } else {
                // 传统模式：发送字符串
                tcpViewModel.sendMsgToServer(cmdStr)
            }
        } catch (e: Exception) {
            Timber.e(e, "转发蓝牙响应失败")
        }
    }

    override fun setResultData(cmdStr: String) {
    }

    private fun showMoreMenu() {
        try {
            val rawModeText = if (isRawMode) "切换到分隔符模式" else "切换到原始模式"
            val menuItems = if (bleViewModel.isConnected()) {
                arrayOf<String>(
                    "打开debug模式",
                    "打开info模式",
                    "关闭debug模式",
                    "清空日志",
                    "分享日志",
                    rawModeText,
                )
            } else {
                arrayOf<String>("蓝牙重连", "清空日志", "分享日志", rawModeText)
            }

            BottomMenu.show(menuItems)
                .setMessage("")
                .setOnMenuItemClickListener { _, text, _ ->
                    handleMenuItemClick(text as String)
                    false
                }
        } catch (e: Exception) {
            Timber.e(e, "显示菜单失败")
        }
    }

    private fun handleMenuItemClick(text: String) {
        try {
            when (text) {
                "蓝牙重连" -> {
                    reconnectBle()
                }

                "清空日志" -> {
                    clearLogs()
                }

                "分享日志" -> {
                    shareLogToFile()
                }

                "切换到原始模式", "切换到分隔符模式" -> {
                    toggleRawMode()
                }

                "打开debug模式" -> {
                    setDebugMode()
                }

                "打开info模式" -> {
                    setInfoMode()
                }

                "关闭debug模式" -> {
                    closeDebugMode()
                }

                "测试" -> {
                    testConnection()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "处理菜单点击失败")
            Toaster.show("操作失败")
        }
    }

    /**
     * 切换原始模式
     */
    private fun toggleRawMode() {
        try {
            isRawMode = !isRawMode
            val modeText = if (isRawMode) "原始模式" else "分隔符模式"
            addLog("已切换到$modeText", ColorUtils.getColor(R.color.title_text_color))

            // 如果TCP已连接，需要断开重连以应用新模式
            if (tcpViewModel.isConnected()) {
                addLog("正在重连以应用新模式...", ColorUtils.getColor(R.color.title_text_color))
                launchWithViewLifecycle {
                    tcpViewModel.disconnect()
                    // 等待断开
                    kotlinx.coroutines.delay(1000)
                    establishTcpConnection()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "切换模式失败")
            Toaster.show("切换模式失败")
        }
    }

    private fun reconnectBle() {
        try {
            bleDevice?.let { device ->
                bleViewModel.launch(device)
            }
        } catch (e: Exception) {
            Timber.e(e, "重连蓝牙失败")
            Toaster.show("重连失败")
        }
    }

    private fun clearLogs() {
        try {
            mStates.clearLogs()
            binding.recyclerview.bindingAdapter.models = emptyList()
        } catch (e: Exception) {
            Timber.e(e, "清空日志失败")
        }
    }

    private fun testConnection() {
        try {
            val testMsg = if (!isIotCmd) "##000\r\n" else "\$cmd=sample"
            handleReceiveMsgFromTCPServer(testMsg)
        } catch (e: Exception) {
            Timber.e(e, "测试连接失败")
        }
    }

    private fun closeDebugMode() {
        try {
            val commands = generateDebugModeCommands(false)
            executeCommandBatch(commands)
        } catch (e: Exception) {
            Timber.e(e, "关闭调试模式失败")
            Toaster.show("关闭调试模式失败")
        }
    }

    private fun setDebugMode() {
        try {
            val commands = generateDebugModeCommands(true, "debug")
            executeCommandBatch(commands)
        } catch (e: Exception) {
            Timber.e(e, "设置调试模式失败")
            Toaster.show("设置调试模式失败")
        }
    }

    private fun setInfoMode() {
        try {
            val commands = generateDebugModeCommands(true, "info")
            executeCommandBatch(commands)
        } catch (e: Exception) {
            Timber.e(e, "设置info模式失败")
            Toaster.show("设置info模式失败")
        }
    }

    private fun generateDebugModeCommands(enable: Boolean, level: String = "off"): List<String> {
        val commands = mutableListOf<String>()

        if (!isIotCmd) {
            if (enable) {
                commands.add(
                    MDCommandUtil.getCommand(
                        MDCommandType.LOG_OUTPUT_STATUS,
                        MDLogOutputStatus.OPEN.toString()
                    )
                )
                val workMode = if (level == "debug") MDWorkModel.DEBUG else MDWorkModel.INFO
                commands.add(
                    MDCommandUtil.getCommand(
                        MDCommandType.WORK_MODE,
                        workMode.toString()
                    )
                )
            } else {
                commands.add(
                    MDCommandUtil.getCommand(
                        MDCommandType.LOG_OUTPUT_STATUS,
                        MDLogOutputStatus.CLOSE.toString()
                    )
                )
                commands.add(
                    MDCommandUtil.getCommand(
                        MDCommandType.WORK_MODE,
                        MDWorkModel.WORK.toString()
                    )
                )
            }
        } else {
            val logLevel = if (enable) level else "off"
            commands.add(
                IOTCommandUtil.getCommand(
                    IOTCommandType.MD_SET_LOG_OUTPUT_MODE_LEVEL,
                    "level=$logLevel&type=bt"
                )
            )
        }

        return commands
    }

    private fun executeCommandBatch(commands: List<String>) {
        try {
            commandItems.clear()
            commands.forEach { command ->
                addLog(command)
                commandItems.add(command)
            }
            sendCommandFromCmdList()
        } catch (e: Exception) {
            Timber.e(e, "执行指令批次失败")
        }
    }

    private fun addLog(
        cmdStr: String,
        colorRes: Int = ColorUtils.getColor(R.color.send_data_color)
    ) {
        try {
            mStates.addLog(cmdStr, colorRes)
        } catch (e: Exception) {
            Timber.e(e, "添加日志失败")
        }
    }

    /**
     * 分享日志到文件
     */
    private fun shareLogToFile() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val logContent = withContext(Dispatchers.Default) {
                    mStates.generateLogContent()
                }

                if (logContent.isEmpty()) {
                    Toaster.show("暂无日志内容")
                    return@launch
                }

                val file = withContext(Dispatchers.IO) {
                    createLogFile(logContent)
                }

                if (file != null) {
                    shareFile(file)
                } else {
                    Toaster.show("导出日志失败")
                }
            } catch (e: Exception) {
                Timber.e(e, "分享日志失败")
                Toaster.show("分享日志失败: ${e.message}")
            }
        }
    }

    private suspend fun createLogFile(logContent: String): File? {
        return try {
            val fileName = "${getString(R.string.app_name)}_tcp_debug_log_${
                SimpleDateFormat(
                    "yyyyMMddHHmmss",
                    Locale.getDefault(Locale.Category.FORMAT)
                ).format(Date())
            }.txt"

            val file = File(Utils.getApp().cacheDir.path, fileName)
            if (FileIOUtils.writeFileFromString(file, logContent)) {
                file
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "创建日志文件失败")
            null
        }
    }

    /**
     * 分享文件
     */
    private fun shareFile(file: File) {
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                file
            )
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "调试日志")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                // 设置剪贴板数据以授予接收应用对URI的访问权限
                clipData = ClipData.newRawUri("", uri)
            }
            startActivity(Intent.createChooser(shareIntent, "分享到"))
        } catch (e: Exception) {
            Timber.e(e, "分享文件失败")
            Toaster.show("分享失败")
        }
    }

    private fun handleBackPressed() {
        try {
            if (tcpViewModel.isConnected()) {
                showDisconnectConfirmDialog()
            } else {
                navigateUp()
            }
        } catch (e: Exception) {
            Timber.e(e, "处理返回按键失败")
            navigateUp()
        }
    }

    private fun showDisconnectConfirmDialog() {
        showMessage(
            StringUtils.getString(R.string.finish_activity_disconnect_tcp_device),
            "温馨提示",
            "确定",
            {
                disconnectAndExit()
            },
            "取消"
        )
    }

    private fun disconnectAndExit() {
        try {
            tcpViewModel.disconnect()
            navigateUp()
        } catch (e: Exception) {
            Timber.e(e, "断开连接并退出失败")
            navigateUp()
        }
    }

    private fun navigateUp() {
        try {
            nav().navigateUp()
        } catch (e: Exception) {
            Timber.e(e, "导航返回失败")
        }
    }

    override fun onDestroy() {
        try {
            // 关闭指令调试模式
            CommonMMKVOwner.isCommandDebugMode = false
            closeDebugMode()
        } catch (e: Exception) {
            Timber.e(e, "销毁时清理资源失败")
        } finally {
            super.onDestroy()
        }
    }
}