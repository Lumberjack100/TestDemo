package com.shmedo.mcloudapp.ui.page.device.common

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.UriUtils
import com.blankj.utilcode.util.Utils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DebugCmdLogInfo
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseCommandLogPrintClickProxy
import com.shmedo.mcloudapp.databinding.FragmentBleCustomCommandLogPrintBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.base.activity.BaseActivity
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.BleCustomCommandLogPrintViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * @author：gonghe
 * @time: 2025/6/26
 * @desc: 蓝牙通讯下自定义指令调试打印输出
 *
 */
class BleCustomCommandLogPrintFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentBleCustomCommandLogPrintBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: BleCustomCommandLogPrintViewModel by viewModels()
    private var isIotCmd = true

    private val cmdTypeList = mutableListOf<String>()


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_ble_custom_command_log_print,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentBleCustomCommandLogPrintBinding
        setupToolbar()
        setupRecyclerView()
        observeLogItems()
    }

    private fun setupToolbar() {
        (mActivity as BaseActivity).setToolBar(binding.llToolbar.toolbar)
        addMenu()
        binding.llToolbar.toolbar.title = "指令下发"
        binding.llToolbar.toolbar.setNavigationOnClickListener {
            handleBackPressed()
        }
        registerOnBackPressedDispatcher {
            handleBackPressed()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerview.setup { rv ->
            addType<DebugCmdLogInfo>(R.layout.item_debug_cmd_log)
        }
    }

    private fun observeLogItems() {
        // 观察日志数据变化
        mStates.logItems.observe(viewLifecycleOwner) { logItems ->
            try {
                binding.recyclerview.bindingAdapter.addModels(logItems,true)
                if (logItems.isNotEmpty()) {
                    binding.recyclerview.smoothScrollToPosition(binding.recyclerview.mutable.size - 1)
                }
            } catch (e: Exception) {
                Timber.e(e, "更新日志列表失败")
            }
        }

        // 观察需要发送的指令
        mStates.commandsToSend.observe(viewLifecycleOwner) { commands ->
            if (commands.isNotEmpty()) {
                commandItems.clear()
                commandItems.addAll(commands)
                sendCommandFromCmdList()
            }
        }
    }

    override fun initData() {
        super.initData()
        try {
            enableCommandDebugMode()
            parseArguments()
            initializeCommandTypes()
            resetCommandInput()
        } catch (e: Exception) {
            Timber.e(e, "初始化数据失败")
            Toaster.show("初始化失败")
        }
    }

    private fun enableCommandDebugMode() {
        CommonMMKVOwner.isCommandDebugMode = true
    }

    private fun parseArguments() {
        arguments?.let {
            isIotCmd = it.getBoolean(IOT_CMD, true)
        }
    }

    private fun initializeCommandTypes() {
        mStates.setDebugMode(BleCustomCommandLogPrintViewModel.DEBUG_MODES[0])
        cmdTypeList.clear()

        if (communicateWay is BleConnect) {
            cmdTypeList.addAll(listOf("物联网指令","物联网透传指令", "##指令", "自定义指令"))
        } else {
            cmdTypeList.addAll(listOf("物联网指令", "物联网透传指令"))
        }
    }

    private fun resetCommandInput() {
        mStates.updateCommand(BleCustomCommandLogPrintViewModel.COMMAND_PREFIX_IOT)
        binding.etCustomCommand.clearFocus()
    }

    inner class ClickProxy : BaseCommandLogPrintClickProxy() {

        override fun onDebugModeChooseClick() {
            showDebugModeSelector()
        }

        override fun onSwitchCmdTypeClick() {
            showCommandTypeSelector()
        }

        override fun onSendClick() {
            handleSendCommand()
        }
    }

    private fun showDebugModeSelector() {
        try {
            val selectedIndex =
                BleCustomCommandLogPrintViewModel.DEBUG_MODES.indexOf(mStates.debugMode.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "",
                    BleCustomCommandLogPrintViewModel.DEBUG_MODES.toTypedArray(),
                    null,
                    selectedIndex
                ) { position, text ->
                    handleDebugModeSelection(position, text)
                }
                .show()
        } catch (e: Exception) {
            Timber.e(e, "显示调试模式选择器失败")
            Toaster.show("操作失败")
        }
    }

    private fun handleDebugModeSelection(position: Int, text: String) {
        try {
            mStates.setDebugMode(text)
            val debugMode = when (position) {
                0 -> BleCustomCommandLogPrintViewModel.DebugMode.CLOSE
                1 -> BleCustomCommandLogPrintViewModel.DebugMode.DEBUG
                2 -> BleCustomCommandLogPrintViewModel.DebugMode.INFO
                else -> return
            }
            updateDebugMode(debugMode)
        } catch (e: Exception) {
            Timber.e(e, "处理调试模式选择失败")
            Toaster.show("设置调试模式失败")
        }
    }

    private fun showCommandTypeSelector() {
        try {
            XPopup.Builder(context)
                .hasShadowBg(false)
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .isDarkTheme(false)
                .atView(binding.ivSwitchCmdType)
                .asAttachList(
                    cmdTypeList.toTypedArray(),
                    null
                ) { position, text ->
                    handleCommandTypeSelection(text)
                }
                .show()
        } catch (e: Exception) {
            Timber.e(e, "显示指令类型选择器失败")
        }
    }

    private fun handleCommandTypeSelection(commandType: String) {
        try {
            val command = when (commandType) {
                "物联网指令" -> BleCustomCommandLogPrintViewModel.COMMAND_PREFIX_IOT
                "物联网透传指令" -> BleCustomCommandLogPrintViewModel.COMMAND_PREFIX_IOT_RAW
                "##指令" -> BleCustomCommandLogPrintViewModel.COMMAND_PREFIX_MDM
                else -> BleCustomCommandLogPrintViewModel.COMMAND_PREFIX_CUSTOM
            }
            mStates.updateCommand(command)
            binding.etCustomCommand.clearFocus()
        } catch (e: Exception) {
            Timber.e(e, "处理指令类型选择失败")
        }
    }

    private fun handleSendCommand() {
        try {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }

            val command = mStates.command.get()
            if (!validateAndSendCommand(command)) {
                return
            }

            executeCommand(command)
        } catch (e: Exception) {
            Timber.e(e, "发送指令失败")
            Toaster.show("发送指令失败: ${e.message}")
        }
    }

    private fun validateAndSendCommand(command: String): Boolean {
        if (command.isEmpty()) {
            Toaster.show("请输入指令")
            return false
        }

//        if (!mStates.validateCommand(command)) {
//            Toaster.show("指令格式不正确，请以 \$cmd=、## 开头或包含 md_raw")
//            return false
//        }

        return true
    }

    private fun executeCommand(command: String) {
        mStates.addLog(command)
        sendBleCommand(command)
    }

    private fun updateDebugMode(mode: BleCustomCommandLogPrintViewModel.DebugMode) {
        try {
            val commands = mStates.generateDebugModeCommands(mode, isIotCmd)

            // 使用批量添加优化性能，避免多次触发 DiffUtil 计算
            val logsToAdd = commands.map { command ->
                Pair(command, ColorUtils.getColor(R.color.send_data_color))
            }
            mStates.addLogBatch(logsToAdd)

            mStates.requestSendCommands(commands)
        } catch (e: Exception) {
            Timber.e(e, "更新调试模式失败")
            Toaster.show("设置调试模式失败")
        }
    }

    override fun setResultData(cmdStr: String) {
        try {
            mStates.addLog(cmdStr, ColorUtils.getColor(R.color.receive_data_color))
            sendCommandFromCmdList()
        } catch (e: Exception) {
            Timber.e(e, "处理返回数据失败")
        }
    }

    private fun addMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.debug_cmd_log_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_share -> {
                        shareLogToFile()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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
            val fileName = "${getString(R.string.app_name)}_ble_realtime_log_${
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
            val uri = UriUtils.file2Uri(file)
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                // 设置剪贴板数据以授予接收应用对URI的访问权限
                val clip = ClipData.newRawUri("", uri)
                clipData = clip
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "分享到"))
        } catch (e: Exception) {
            Timber.e(e, "分享文件失败")
            Toaster.show("分享失败")
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar, isKeyboardEnable = true)
    }

    private fun handleBackPressed() {
        updateDebugMode(BleCustomCommandLogPrintViewModel.DebugMode.CLOSE)
        nav().navigateUp()
    }

    override fun onDestroy() {
        try {
            // 关闭指令调试模式
            CommonMMKVOwner.isCommandDebugMode = false
            updateDebugMode(BleCustomCommandLogPrintViewModel.DebugMode.CLOSE)
        } catch (e: Exception) {
            Timber.e(e, "销毁时清理资源失败")
        } finally {
            super.onDestroy()
        }
    }

    companion object {
        private const val IOT_CMD = "com.shmedo.mcloudapp.iot.IOT_CMD"

        fun newBundleArguments(
            isIotCmd: Boolean = true,
            type: ProductType = ProductType.UnKnown,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putBoolean(IOT_CMD, isIotCmd)
            putParcelable(AppContants.Extras.PRODUCT_TYPE, type)
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}