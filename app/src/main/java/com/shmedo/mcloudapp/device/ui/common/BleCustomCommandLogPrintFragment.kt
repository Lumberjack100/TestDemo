package com.shmedo.mcloudapp.device.ui.common

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.base.model.DebugCmdLogInfo
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.databinding.FragmentBleCustomCommandLogPrintBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.BleCustomCommandLogPrintViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import java.util.UUID

class BleCustomCommandLogPrintFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentBleCustomCommandLogPrintBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: BleCustomCommandLogPrintViewModel
    private val iotParseManager: IOTParserManager by inject()

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
        binding.toolbar.title = "指令调试"
        binding.toolbar.setNavigationOnClickListener { v: View? ->
            //            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //            mMessenger.requestStatusBarColor(R.color.colorPrimary)
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

    inner class ClickProxy : BaseClickProxy() {
        fun onDebugModeChooseClick() {
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

        fun onSendClick() {
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
            "\$cmd=md_raw&content=$input".plus(
                "&apikey=${deviceInfo.apikey.ifEmpty { "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9" }}&msgid=${
                    UUID.randomUUID().toString().substring(30)
                }"
            )
        else if (input.startsWith("\$cmd"))
            input.plus(
                "&apikey=${deviceInfo.apikey.ifEmpty { "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9" }}&msgid=${
                    UUID.randomUUID().toString().substring(30)
                }"
            )
        else
            input

        addLog(cmdStr)
        sendDebugCommand(cmdStr)
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

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.toolbar)
    }
}