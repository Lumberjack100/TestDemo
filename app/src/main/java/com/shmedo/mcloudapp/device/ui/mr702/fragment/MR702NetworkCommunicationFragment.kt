package com.shmedo.mcloudapp.device.ui.mr702.fragment

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.IOTCommandManager
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParseManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.dismissWaitDialog
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.showWaitDialog
import com.shmedo.mcloudapp.databinding.FragmentMR702NetworkCommunicationBinding
import com.shmedo.mcloudapp.device.BaseClickProxy
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.viewmodel.state.MR702NetworkCommunicationViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class MR702NetworkCommunicationFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentMR702NetworkCommunicationBinding by lazy { getBinding() as FragmentMR702NetworkCommunicationBinding }
    private val mStates: MR702NetworkCommunicationViewModel by viewModels()
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val iotParseManager: IOTParseManager by inject()

    private val ipModeList by lazy { Utils.getApp().resources.getStringArray(R.array.ip_mode) }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_m_r702_network_communication, BR.vm, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.llToolbar.toolbar.title = "网络与通信"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
                nav().navigateUp()
            }
        })
    }

    override fun initData() {
        super.initData()
        toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_device_param_edit)
        toolbarViewModel.toolbarTvActionText.set("取消")
        toolbarViewModel.toolbarIvActionVisible.set(true)
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onToolbarIvClick() {
            toolbarViewModel.toolbarIvActionVisible.set(false)
            toolbarViewModel.toolbarTvActionVisible.set(true)
        }

        override fun onToolbarTvClick() {
            toolbarViewModel.toolbarIvActionVisible.set(true)
            toolbarViewModel.toolbarTvActionVisible.set(false)
        }

        fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            when (button.id) {
                R.id.mobileCommunicationSB -> { //4G通信

                }
                R.id.ethernetAccessSB -> { //以太网接入

                }
            }
        }

        fun onIPModeSwitchClick(){
            val selectedIndex = ipModeList.indexOf(mStates.ipMode.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择状态", ipModeList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.ipMode.set(text)
                        mStates.isIPContentVisible.set(position == 1)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }
    }

    override fun lazyLoadData() {
        showWaitDialog(StringUtils.getString(R.string.loading))
        queryNetworkData()
    }

    private fun queryNetworkData() {
        val command: String =
            IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_MR_GET_DATA_NETWORK)
        if (communicateWay is NetPlatformConnect) {
            netIotCommandViewModel.batchDispatchRawCmd(command, listOf(deviceInfo.deviceToken))
        } else {
            bleViewModel.sendCommand(command, true, deviceInfo.apiKey, 1000)
        }
    }

    override fun doDispatchFailed(cmdStr: String, errorMsg: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_DATA_NETWORK -> Toaster.show("下发指令失败: $errorMsg")

            else -> {}
        }
    }

    override fun doDispatchSuccess(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_DATA_NETWORK -> {
                netIotCommandViewModel.processCmdResult()
            }

            else -> {}
        }
    }

    override fun setResultData(cmdStr: String) {
        dismissWaitDialog()
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_DATA_NETWORK -> {
                val result =
                    iotParseManager.parse<String>(cmdStr, IOTCommandType.QUERY_DEVICE_STATUS)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询设备状态出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        val content: String = result.data

                    }
                }
            }

            else -> {}
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        fun newBundleArguments(
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}