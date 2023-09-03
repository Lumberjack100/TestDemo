package com.shmedo.mcloudapp.device.ui.m20.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.base.model.UserInfo
import com.shmedo.lib.core.ext.getAppViewModel
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.lib.device.base.iot_cmd.IOTCommandManager
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.utils.IOTStringUtil
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.common.viewmodel.state.PageMessenger
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentM20HomeBinding
import com.shmedo.mcloudapp.device.BaseNetIotCommunicateFragment
import com.shmedo.mcloudapp.device.model.ConfigModule
import com.shmedo.mcloudapp.device.model.DispatchCmdItem
import com.shmedo.mcloudapp.device.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.M20HomeViewModel

class NetM20HomeFragment : BaseNetIotCommunicateFragment() {
    private val binding: FragmentM20HomeBinding by lazy { getBinding() as FragmentM20HomeBinding }
    private val mMessenger: PageMessenger by lazy { getAppViewModel() }
    private val mStates: M20HomeViewModel by viewModels()
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModels()
    private val userInfo: UserInfo by lazy { MmkvCacheUtil.getUser()!! }

    private lateinit var deviceInfo: DeviceInfo
    private var statusBarColor = 0

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_m20_home, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.llToolbar.toolbar.title = "设备配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            mActivity.finish()
        }
        registerOnBackPressedDispatcher {
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            mActivity.finish()
        }
        initModuleAdapter()
    }

    private fun initModuleAdapter() {
        binding.recyclerview.setup { rv ->
            rv.addItemDecoration(
                MyGridSpacingItemDecoration(
                    2,
                    ConvertUtils.dp2px(15f),
                    false
                )
            )
            addType<ConfigModule>(R.layout.item_device_config_module)
            R.id.item.onClick {
                val module = getModel<ConfigModule>()
                processItemClick(module.name)
            }
        }.models = getModuleList()
    }

    override fun initData() {
        arguments?.let {
            deviceInfo = it.getSerializable(AppContants.Extras.DEVICE_INFO) as DeviceInfo
            statusBarColor = it.getInt(AppContants.Extras.STATUS_BAR_COLOR)
        }
        mStates.deviceName.set(deviceInfo.deviceName.ifEmpty { deviceInfo.deviceToken })
        mStates.deviceToken.set(String.format("设备SN号：%s", deviceInfo.deviceToken))
        mStates.productName.set(String.format("所属产品：%s", deviceInfo.productName))
        mStates.firmwareVersion.set(String.format("固件版本：%s", deviceInfo.firmwareVersion))
        mStates.isOnline.set(deviceInfo.onlineStatus)
        mStates.isExtendedField3Visible.set(false)
        mStates.isPlatformConnectionStateVisible.set(false)
        mStates.isConnectOperateVisible.set(false)
    }

    override fun createObserver() {
        deviceRequestViewModel.batchDispatchRawCmdResult.observe(viewLifecycleOwner) { dataResult: DataResult<List<DispatchCmdItem>> ->
            if (!dataResult.responseStatus.isSuccess) {
                Toaster.show(dataResult.responseStatus.errorMessage)
                return@observe
            }
            dataResult.result?.let { tempList ->

            }
        }
    }

    /**
     * 获取设备的基本信息
     */
    private fun queryBaseInfo() {
        val command: String =
            IOTCommandManager.getInstance().getCommand(IOTCommandType.M20_MD_GET_BASE_INFO)
        deviceRequestViewModel.batchDispatchRawCmd(command, listOf(deviceInfo.deviceToken))
    }

    private fun processItemClick(moduleName: String) {
        Toaster.show("点击了$moduleName")
        when (moduleName) {
            "设置向导" -> {

            }

            "状态" -> {

            }

            "数据中心" -> {

            }

            "设置" -> {

            }
        }
    }

    inner class ClickProxy {

    }

    override fun doDispatchFailed(cmdStr: String, errorMsg: String) {
        when (IOTStringUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS -> Toaster.show("下发指令失败")
            IOTCommandType.M20_MD_LEVEL_INITIAL -> {

            }

            else -> {}
        }
    }

    override fun doDispatchSuccess(cmdStr: String) {
        when (IOTStringUtil.extractCommandType(cmdStr)) {
            IOTCommandType.QUERY_DEVICE_STATUS -> Toaster.show("下发指令失败")
            IOTCommandType.M20_MD_LEVEL_INITIAL -> {

            }

            else -> {}
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    private fun getModuleList() =
        arrayListOf<ConfigModule>().apply {
            add(
                ConfigModule(
                    R.drawable.ic_setup_wizard,
                    "设置向导",
                    "一键配置"
                )
            )
            add(
                ConfigModule(
                    R.drawable.ic_device_current_state,
                    "状态",
                    "获取当前设备状态"
                )
            )
            add(
                ConfigModule(
                    R.drawable.ic_device_data_center,
                    "数据中心",
                    "基础参数配置"
                )
            )
            add(
                ConfigModule(
                    R.drawable.ic_device_setting,
                    "设置",
                    "高级设置"
                )
            )
        }


    companion object {
        fun newBundleArguments(
            deviceInfo: DeviceInfo,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putSerializable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }

}