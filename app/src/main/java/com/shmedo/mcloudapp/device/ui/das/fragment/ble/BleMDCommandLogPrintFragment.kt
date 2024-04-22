package com.shmedo.mcloudapp.device.ui.das.fragment.ble

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
import com.shmedo.lib.core.base.model.DebugCmdLogInfo
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.device.base.md_cmd.utils.MDConstants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.databinding.FragmentBleCustomCommandLogPrintBinding
import com.shmedo.mcloudapp.device.common.BaseCommandLogPrintClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.viewmodel.state.BleCustomCommandLogPrintViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 创建者：gonghe
 * 创建时间：2024/4/18
 * 描述： TODO
 */
class BleMDCommandLogPrintFragment : BaseMDDeviceFragment() {
    private lateinit var binding: FragmentBleCustomCommandLogPrintBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: BleCustomCommandLogPrintViewModel

    private val debugModelList: MutableList<String> =
        arrayListOf("关", "debug模式", "info模式")


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
            //mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
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
        mStates.debugMode.set(debugModelList[0])
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
                    { _, text ->
                        mStates.debugMode.set(text)
                        showLoadingDialog(StringUtils.getString(R.string.loading))
//                        queryAlarmData(alarmTypeList.indexOf(text).toString())

                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
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

        val cmdStr = mStates.command.get()
        addLog(cmdStr)
        sendDebugCommand(cmdStr + MDConstants.COMMAND_FOOTER)
    }

    override fun setResultData(cmdStr: String) {
        addLog(cmdStr)
    }

    private fun addLog(cmdStr: String) {
        val logInfo = DebugCmdLogInfo(
            logTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm:ss.SSS")),
            content = cmdStr
        )
        binding.recyclerview.bindingAdapter.apply {
            mutable.add(logInfo)
            notifyItemInserted(itemCount)
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
                if (FileIOUtils.writeFileFromString(file, logContent.toString())) {
                    // 分享文件
                    shareFile(file)
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


}