package com.shmedo.mcloudapp.ui.page.device.common

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.UriUtils
import com.blankj.utilcode.util.Utils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import com.shmedo.core.model.DebugCmdLogInfo
import com.shmedo.lib.ble.communicate.service.base.ConnectedResult
import com.shmedo.lib.ble.communicate.service.base.ConnectingResult
import com.shmedo.lib.ble.communicate.service.base.DisconnectedResult
import com.shmedo.lib.ble.communicate.service.base.IdleResult
import com.shmedo.lib.ble.communicate.service.base.LinkLossResult
import com.shmedo.lib.ble.communicate.service.base.MissingServiceResult
import com.shmedo.lib.ble.communicate.service.base.ReadyResult
import com.shmedo.lib.ble.communicate.service.base.SuccessResult
import com.shmedo.lib.ble.communicate.service.base.UnknownErrorResult
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
import com.shmedo.lib.tcp.TcpSuccessResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseCommandLogPrintClickProxy
import com.shmedo.mcloudapp.databinding.FragmentTcpDebugBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.model.NoDeviceState
import com.shmedo.mcloudapp.model.WorkingState
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.request.TcpViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.TcpDebugViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.getViewModel
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
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: TcpDebugViewModel
    private lateinit var tcpViewModel: TcpViewModel
    private lateinit var deviceRequestViewModel: DeviceRequestViewModel

    private var isIotCmd = true

    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
        tcpViewModel = getViewModel()
        deviceRequestViewModel = getViewModel()
    }

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
        binding.toolbar.title = "远程调试"
        binding.toolbar.setNavigationOnClickListener { v: View? ->
            processBackPress()
        }
        registerOnBackPressedDispatcher {
            processBackPress()
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                processBackPress()
            }
        })
        initLogAdapter()
    }

    private fun initLogAdapter() {
        binding.recyclerview.setup { rv ->
            addType<DebugCmdLogInfo>(R.layout.item_debug_cmd_log)
        }.models = mutableListOf<DebugCmdLogInfo>()
    }

    override fun initData() {
        super.initData()
        isIotCmd =
            productType != ProductType.COLLECTOR_R_1 && productType != ProductType.DAS && productType != ProductType.BHY
        //开启命令调试模式
        CommonMMKVOwner.isCommandDebugMode = true
    }

    override fun lazyLoadData() {
        printLog("正在建立远程 TCP 连接...", ColorUtils.getColor(R.color.title_text_color))
        launchWithViewLifecycle {
            val deviceDebugAddress = deviceRequestViewModel.getRemoteDeviceLogin(
                deviceSn = deviceInfo.deviceToken,
                deviceKey = deviceInfo.apikey.ifEmpty { "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9" },
            ) { error: Throwable ->
                printLog(
                    "TCP 连接异常，获取远程服务器地址和端口信息失败：${error.message}",
                    ColorUtils.getColor(R.color.error_FF4400)
                )

            } ?: return@launchWithViewLifecycle

            binding.toolbar.subtitle =
                "${deviceDebugAddress.deviceServerInfo.serverAddr}:${deviceDebugAddress.deviceServerInfo.serverPort}"

            tcpViewModel.initTcpClient(
                deviceDebugAddress.deviceServerInfo.serverAddr,
                deviceDebugAddress.deviceServerInfo.serverPort,
                false,
                MDConstants.COMMAND_FOOTER
            )
            withContext(Dispatchers.IO) {
                tcpViewModel.connect()
            }
        }
    }

    override fun createObserver() {
        super.createObserver()
        launchWithViewLifecycle {
            try {
                collectTcpData()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private suspend fun collectTcpData() {
        tcpViewModel.data.collect { state ->
            Timber.v("${javaClass.simpleName} Tcp State: $state")
            when (state) {
                is TcpIdleResult -> {
                    //do nothing
                }

                is TcpConnectedResult -> {
                    mStates.tcpConnected.set(true)
                    printLog("TCP 连接成功", ColorUtils.getColor(R.color.online_colorPrimary))
                }

                is TcpSuccessResult -> {
                    val data = state.data
                    if (data.isNotEmpty()) {
                        Timber.d("Received TCP Message:$data")
                        handleReceiveMsgFromTCPServer(data)
                    }
                }

                is TcpConnectClosed -> {
                    mStates.tcpConnected.set(false)
                    printLog("TCP 连接断开", ColorUtils.getColor(R.color.error_FF4400))
                }

                is TcpConnectError -> {
                    mStates.tcpConnected.set(false)
                    printLog("TCP 连接异常", ColorUtils.getColor(R.color.error_FF4400))
                }
            }
        }
    }

    override suspend fun collectBleData() {
        bleViewModel.state.collect { state ->
            Timber.v("${javaClass.simpleName} MedoBle: $state")
            when (state) {
                NoDeviceState -> {}
                is WorkingState -> when (state.result) {
                    is IdleResult,
                    is ConnectingResult -> {
                        printLog(
                            "正在连接蓝牙(${bleDevice?.address})...",
                            ColorUtils.getColor(R.color.title_text_color)
                        )
                    }

                    is ConnectedResult -> {
                        printLog(
                            "蓝牙连接成功",
                            ColorUtils.getColor(R.color.online_colorPrimary)
                        )
                    }

                    is ReadyResult -> {
                        printLog(
                            "蓝牙已就绪",
                            ColorUtils.getColor(R.color.title_text_color)
                        )
                    }

                    is SuccessResult -> {
                        handleBleResponseContentFromDevice(state.result.data.response)
                    }

                    is DisconnectedResult -> {
                        printLog(
                            "蓝牙连接断开, reason: ${state.result.reason}",
                            ColorUtils.getColor(R.color.error_FF4400)
                        )
                    }

                    is LinkLossResult -> {
                        printLog(
                            "蓝牙连接断开, reason: device link loss",
                            ColorUtils.getColor(R.color.error_FF4400)
                        )
                    }

                    is MissingServiceResult -> {
                        printLog(
                            "蓝牙连接失败, reason: device missing service",
                            ColorUtils.getColor(R.color.error_FF4400)
                        )
                    }

                    is UnknownErrorResult -> {
                        printLog(
                            "蓝牙连接失败, reason: device unknown error",
                            ColorUtils.getColor(R.color.error_FF4400)
                        )
                    }
                }
            }
        }
    }

    inner class ClickProxy : BaseCommandLogPrintClickProxy() {
        override fun onConnectOperateClick() {
            if (tcpViewModel.isConnected()) {
                tcpViewModel.disconnect()
            } else {
                tcpViewModel.connect()
            }
        }

        override fun onToolbarIvClick() {
            showMoreMenu()
        }
    }

    /**
     * 处理接收的 TCP 消息，通过蓝牙转发给设备
     */
    private fun handleReceiveMsgFromTCPServer(msg: String) {
        if (msg.isEmpty())
            return

        printLog(
            msg.replace(MDConstants.COMMAND_FOOTER, ""),
            ColorUtils.getColor(R.color.receive_data_color)
        )

        if (isBleDisconnected()) {
            printLog(
                "蓝牙连接已断开",
                ColorUtils.getColor(R.color.error_FF4400)
            )
            return
        }
        sendBleCommand(msg)
    }

    override fun setResultData(cmdStr: String) {
        printLog(cmdStr, ColorUtils.getColor(R.color.send_data_color))
        handleBleResponseContentFromDevice(cmdStr)
    }

    /**
     * 处理设备响应内容，通过 TCP 转发给远程调试客户端
     */
    private fun handleBleResponseContentFromDevice(cmdStr: String) {
        if (!tcpViewModel.isConnected()) {
            printLog(
                "TCP 连接已断开",
                ColorUtils.getColor(R.color.error_FF4400)
            )
            return
        }
        tcpViewModel.sendMsgToServer(cmdStr)
    }

    private fun showMoreMenu() {
        val menuItems = if (bleViewModel.isConnected()) arrayOf<String>(
            "清空日志",
            "分享日志",
            "打开debug模式",
            "打开info模式",
            "测试"
        ) else arrayOf<String>("蓝牙重连", "清空日志", "分享日志")
        BottomMenu.show(menuItems)
            .setMessage("")
            .setOnMenuItemClickListener { dialog, text, index ->
                when (text) {
                    "蓝牙重连" -> {
                        bleViewModel.launch(bleDevice!!)
                    }

                    "清空日志" -> {
                        binding.recyclerview.bindingAdapter.models = mutableListOf()
                    }

                    "分享日志" -> {
                        shareLogToFile()
                    }

                    "打开debug模式" -> {
                        setDebugMode()
                    }

                    "打开info模式" -> {
                        setInfoMode()
                    }

                    "测试" -> {
                        handleReceiveMsgFromTCPServer(if (!isIotCmd) "##000\r\n" else "\$cmd=sample")
                    }
                }
                false
            }
    }

    private fun closeDebugMode() {
        commandItems.clear()
        if (!isIotCmd) {
            var command = MDCommandUtil.getCommand(
                MDCommandType.LOG_OUTPUT_STATUS,
                MDLogOutputStatus.CLOSE.toString()
            )
            printLog(command)
            commandItems.add(command)

            command = MDCommandUtil.getCommand(
                MDCommandType.WORK_MODE,
                MDWorkModel.WORK.toString()
            )
            printLog(command)
            commandItems.add(command)
        } else {
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_LOG_OUTPUT_MODE_LEVEL,
                "level=off&type=bt"
            )
            printLog(command)
            commandItems.add(command)
        }
        sendCommandFromCmdList()
    }

    private fun setDebugMode() {
        commandItems.clear()
        if (!isIotCmd) {
            var command = MDCommandUtil.getCommand(
                MDCommandType.LOG_OUTPUT_STATUS,
                MDLogOutputStatus.OPEN.toString()
            )
            printLog(command)
            commandItems.add(command)

            command = MDCommandUtil.getCommand(
                MDCommandType.WORK_MODE,
                MDWorkModel.DEBUG.toString()
            )
            printLog(command)
            commandItems.add(command)
        } else {
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_LOG_OUTPUT_MODE_LEVEL,
                "level=debug&type=bt"
            )
            printLog(command)
            commandItems.add(command)
        }
        sendCommandFromCmdList()
    }

    private fun setInfoMode() {
        commandItems.clear()
        if (!isIotCmd) {
            var command = MDCommandUtil.getCommand(
                MDCommandType.LOG_OUTPUT_STATUS,
                MDLogOutputStatus.OPEN.toString()
            )
            printLog(command)
            commandItems.add(command)

            command = MDCommandUtil.getCommand(
                MDCommandType.WORK_MODE,
                MDWorkModel.INFO.toString()
            )
            printLog(command)
            commandItems.add(command)
        } else {
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_LOG_OUTPUT_MODE_LEVEL,
                "level=info&type=bt"
            )
            printLog(command)
            commandItems.add(command)
        }
        sendCommandFromCmdList()
    }

    private fun printLog(
        cmdStr: String,
        colorRes: Int = ColorUtils.getColor(R.color.send_data_color)
    ) {
        val logInfo = DebugCmdLogInfo(
            logTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm:ss.SSS")),
            content = cmdStr.replace(MDConstants.COMMAND_FOOTER, ""),
            colorRes = colorRes,
            byteCount = cmdStr.length
        )
        binding.recyclerview.bindingAdapter.apply {
            mutable.add(logInfo)
            notifyItemInserted(itemCount)
        }
//        binding.recyclerview.scrollToPosition(binding.recyclerview.bindingAdapter.itemCount - 1)
    }

    /**
     * 分享日志到文件
     */
    private fun shareLogToFile() {
        launchWithViewLifecycle {
            binding.recyclerview.models?.let { logList ->
                val logContent = StringBuilder()
                logList.forEach { logInfo ->
                    (logInfo as DebugCmdLogInfo).apply {
                        logContent.append(logTime)
                        logContent.append(" ")
                        logContent.append(content)
                        logContent.append("\n")
                    }
                }

                // 创建文件并写入日志内容
                val fileName = "${getString(R.string.app_name)}_ble_realtime_log_${
                    SimpleDateFormat(
                        "yyyyMMddHHmmss",
                        Locale.getDefault(Locale.Category.FORMAT)
                    ).format(
                        Date()
                    )
                }.txt"
                val file = File(Utils.getApp().cacheDir.path, fileName)
                try {
                    // 在IO线程进行文件写入操作
                    withContext(Dispatchers.IO) {
                        if (FileIOUtils.writeFileFromString(file, logContent.toString())) {
                            // 切换回主线程进行文件分享
                            withContext(Dispatchers.Main) {
                                shareFile(file)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                    // 异常处理，显示错误信息等
                    withContext(Dispatchers.Main) {
                        Toaster.show("Error sharing file: ${e.localizedMessage}")
                    }
                }
            }
        }
    }

    /**
     * 分享文件
     */
    private fun shareFile(file: File) {
        val uri = UriUtils.file2Uri(file)
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            //设置剪贴板数据以授予接收应用对URI的访问权限
            val clip = ClipData.newRawUri("", uri)
            clipData = clip
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "分享到"))
    }

    override fun onResume() {
        super.onResume()
        // 开启屏幕长亮
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        initImmersionBar(binding.toolbar, isKeyboardEnable = true)
    }

    override fun onStop() {
        super.onStop()
        // 禁用屏幕长亮
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onDestroy() {
        //关闭命令调试模式
        CommonMMKVOwner.isCommandDebugMode = false
        closeDebugMode()
        super.onDestroy()
    }

    private fun processBackPress() {
        if (tcpViewModel.isConnected()) {
            showMessage(
                StringUtils.getString(R.string.finish_activity_disconnect_tcp_device),
                "温馨提示",
                "确定",
                {
                    tcpViewModel.disconnect()
                    nav().navigateUp()
                },
                "取消"
            )
        } else {
            nav().navigateUp()
        }
    }
}