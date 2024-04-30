package com.shmedo.mcloudapp.device.ui.common

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.IntentUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.UriUtils
import com.blankj.utilcode.util.Utils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DebugCmdLogInfo
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.enums.MDLogOutputStatus
import com.shmedo.lib.device.base.md_cmd.enums.MDWorkModel
import com.shmedo.lib.device.base.md_cmd.utils.MDCommandUtil
import com.shmedo.lib.device.base.md_cmd.utils.MDConstants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentBleCustomCommandLogPrintBinding
import com.shmedo.mcloudapp.device.common.BaseCommandLogPrintClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.BleCustomCommandLogPrintViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ext.nav
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BleCustomCommandLogPrintFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentBleCustomCommandLogPrintBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: BleCustomCommandLogPrintViewModel
    private var isIotCmd = true

    private val debugModelList: MutableList<String> =
        arrayListOf("关", "debug模式", "info模式")

    private val cmdTypeList = mutableListOf<String>()


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

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
        //设置menu 关键代码
        mActivity.setSupportActionBar(binding.toolbar)
        addMenu()
        binding.toolbar.title = "指令调试"
        binding.toolbar.setNavigationOnClickListener { v: View? ->
            closeDebugMode()
            //mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                closeDebugMode()
                //mMessenger.requestStatusBarColor(R.color.colorPrimary)
                nav().navigateUp()
            }
        })
        initAdapter()
    }

    private fun initAdapter() {
        binding.recyclerview.setup { rv ->
            addType<DebugCmdLogInfo>(R.layout.item_debug_cmd_log)
        }.models = mutableListOf<DebugCmdLogInfo>()
    }

    override fun initData() {
        super.initData()
        arguments?.let {
            isIotCmd = it.getBoolean(IOT_CMD)
        }
        mStates.debugMode.set(debugModelList[0])
        cmdTypeList.clear()
        if (communicateWay is BleConnect) {
            cmdTypeList.addAll(listOf("物联网自定义指令", "##指令", "米度透传指令"))
        } else {
            cmdTypeList.addAll(listOf("物联网自定义指令", "米度透传指令"))
        }
        mStates.command.set(cmdTypeList[0])
    }

    inner class ClickProxy : BaseCommandLogPrintClickProxy() {
        override fun onDebugModeChooseClick() {
            val selectedIndex = debugModelList.indexOf(mStates.debugMode.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", debugModelList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.debugMode.set(text)
                        when (position) {
                            0 -> {//关
                                closeDebugMode()
                            }

                            1 -> {//debug模式
                                setDebugMode()
                            }

                            2 -> {//info模式
                                setInfoMode()
                            }
                        }
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        override fun onSwitchCmdTypeClick() {
            XPopup.Builder(context)
                .hasShadowBg(false)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .isDarkTheme(false)
                .popupAnimation(PopupAnimation.TranslateFromRight) //NoAnimation表示禁用动画
                .atView(binding.ivSwitchCmdType)  // 依附于所点击的View，内部会自动判断在上方或者下方显示
                .asAttachList(
                    cmdTypeList.toTypedArray(),
                    null
                ) { position, text ->
                    when (text) {
                        "米度透传指令" -> {
                            mStates.command.set("\$cmd=md_raw&content=")
                        }

                        "物联网自定义指令" -> {
                            mStates.command.set("\$cmd=")
                        }

                        "##指令" -> {
                            mStates.command.set("##")
                        }
                    }
                }
                .show()
        }

        override fun onSendClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            sendCmd()
        }
    }

    private fun sendCmd() {
        if (mStates.command.get().isEmpty())
            return

        val input = mStates.command.get()
        val cmdStr = if (input.startsWith("##"))
            input.plus(MDConstants.COMMAND_FOOTER)
        else
            input

        commandItems.clear()
        commandItems.add(cmdStr)
        addLog(input)
        sendCommandFromCmdList()
    }

    private fun closeDebugMode() {
        commandItems.clear()
        if (!isIotCmd) {
            var command = MDCommandUtil.getCommand(
                MDCommandType.LOG_OUTPUT_STATUS,
                MDLogOutputStatus.CLOSE.toString()
            )
            addLog(command)
            commandItems.add(command)

            command = MDCommandUtil.getCommand(
                MDCommandType.WORK_MODE,
                MDWorkModel.WORK.toString()
            )
            addLog(command)
            commandItems.add(command)
        } else {
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.SET_LOG_OUTPUT_MODE_LEVEL,
                "level=off&type=bt"
            )
            addLog(command)
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
            addLog(command)
            commandItems.add(command)

            command = MDCommandUtil.getCommand(
                MDCommandType.WORK_MODE,
                MDWorkModel.DEBUG.toString()
            )
            addLog(command)
            commandItems.add(command)
        } else {
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.SET_LOG_OUTPUT_MODE_LEVEL,
                "level=debug&type=bt"
            )
            addLog(command)
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
            addLog(command)
            commandItems.add(command)

            command = MDCommandUtil.getCommand(
                MDCommandType.WORK_MODE,
                MDWorkModel.INFO.toString()
            )
            addLog(command)
            commandItems.add(command)
        } else {
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.SET_LOG_OUTPUT_MODE_LEVEL,
                "level=info&type=bt"
            )
            addLog(command)
            commandItems.add(command)
        }
        sendCommandFromCmdList()
    }

    override fun setResultData(cmdStr: String) {
        addLog(cmdStr, ColorUtils.getColor(R.color.colorPrimaryDark))
        sendCommandFromCmdList()
    }

    private fun addLog(
        cmdStr: String,
        colorRes: Int = ColorUtils.getColor(R.color.title_text_color)
    ) {
        val logInfo = DebugCmdLogInfo(
            logTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm:ss.SSS")),
            content = cmdStr.replace(MDConstants.COMMAND_FOOTER, ""),
            colorRes = colorRes
        )
        binding.recyclerview.bindingAdapter.apply {
            mutable.add(logInfo)
            notifyItemInserted(itemCount)
        }
        binding.recyclerview.scrollToPosition(binding.recyclerview.bindingAdapter.itemCount - 1)
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
                        //分享
                        shareLogToFile()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    /**
     * 分享日志
     */
    private fun shareLogText() {
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
                startActivity(IntentUtils.getShareTextIntent(logContent.toString()))
            }
        }
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
        initImmersionBar(binding.toolbar, isKeyboardEnable = true)
//        val windowInsetsController = WindowCompat.getInsetsController(mActivity.window, mActivity.window.decorView)
//        windowInsetsController.isAppearanceLightStatusBars = true
    }

    override fun onDestroy() {
        closeDebugMode()
        super.onDestroy()
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