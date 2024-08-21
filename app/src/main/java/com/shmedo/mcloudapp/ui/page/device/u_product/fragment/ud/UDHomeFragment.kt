package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

import android.os.Bundle
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
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
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentUDHomeBinding
import com.shmedo.mcloudapp.databinding.ItemSubConfigModuleBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.ConfigModule
import com.shmedo.mcloudapp.model.ConfigModuleTree
import com.shmedo.mcloudapp.model.DasSensorSubMonitorStatusItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoBasicItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.common.QueryDeviceDataFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.UDHomeViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import kotlinx.coroutines.flow.debounce
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/8/21
 * @desc: 一体化雷达水位计首页
 *
 */
class UDHomeFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUDHomeBinding
    private val toolbarViewModel: ToolbarViewModel by inject()
    private val mHeadStates: UDHomeViewModel by inject()
    private val iotParseManager: IOTParserManager by inject()

    override fun initViewModel() {
        super.initViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_u_d_home, BR.stateVM, mHeadStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUDHomeBinding
        binding.llToolbar.toolbar.title = "设备配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener {
            if (bleViewModel.isConnected()) {
                showMessage(
                    StringUtils.getString(R.string.disconnect_device_warn),
                    "温馨提示",
                    "确定",
                    {
                        bleViewModel.disconnect()
                        mActivity.finish()
                    },
                    "取消"
                )
            } else
                mActivity.finish()
        }
        registerOnBackPressedDispatcher {
            if (bleViewModel.isConnected()) {
                showMessage(
                    StringUtils.getString(R.string.disconnect_device_warn),
                    "温馨提示",
                    "确定",
                    {
                        bleViewModel.disconnect()
                        mActivity.finish()
                    },
                    "取消"
                )
            } else
                mActivity.finish()
        }
        initModuleAdapter()
    }

    override fun initData() {
        super.initData()
        toolbarViewModel.toolbarIvActionVisible.set(true)
        mHeadStates.productLightResId.set(R.drawable.ic_mr702)
        mHeadStates.productGrayResId.set(R.drawable.ic_mr702)
        mHeadStates.productLogoResId.set(mHeadStates.productLightResId.get())
        mHeadStates.productName.set(deviceInfo.productName)
        mHeadStates.deviceToken.set(deviceInfo.deviceToken)
        mHeadStates.deviceName.set(if (deviceInfo.deviceName == deviceInfo.deviceToken) deviceInfo.productToken else deviceInfo.deviceName.ifEmpty { deviceInfo.deviceToken })
        mHeadStates.firmwareVersion.set(deviceInfo.firmwareVersion.ifEmpty { "--" })
        mHeadStates.isRunningStateVisible.set(false)
        mHeadStates.isPlatformsVisible.set(false)

        when (communicateWay) {
            NetPlatformConnect -> {
                mHeadStates.isDeviceStateTagHighLight.set(deviceInfo.onlineStatus)
                mHeadStates.deviceStateTagText.set(if (deviceInfo.onlineStatus) "在线" else "离线")
                mHeadStates.isConnectOperateVisible.set(false)
                mHeadStates.isIOTPlatformStateVisible.set(false)
            }

            BleConnect -> {
                mHeadStates.isDeviceStateTagHighLight.set(false)
                mHeadStates.deviceStateTagText.set("未连接")
                mHeadStates.isConnectOperateVisible.set(true)
                mHeadStates.connectOperateText.set("蓝牙连接")
                mHeadStates.isIOTPlatformStateVisible.set(true)
                mHeadStates.iotPlatformStateText.set(if (deviceInfo.onlineStatus) "在线" else "离线")
            }

            else -> {}
        }
    }

    override fun onConnectionStateChanged(isConnected: Boolean) {
        mHeadStates.isConnected.set(isConnected)
        mHeadStates.isDeviceStateTagHighLight.set(isConnected)
        if (isConnected) {
            mHeadStates.deviceStateTagText.set("已连接")
            mHeadStates.connectOperateText.set("断开连接")
            mHeadStates.productLogoResId.set(mHeadStates.productLightResId.get())
        } else {
            mHeadStates.deviceStateTagText.set("未连接")
            mHeadStates.connectOperateText.set("蓝牙连接")
            mHeadStates.productLogoResId.set(mHeadStates.productGrayResId.get())
        }
        //刷新模块状态
        binding.rvModule.models?.forEach {
            if (it is ConfigModule) {
                it.configModule.refreshStatus(isConnected)
            }
        }
    }

    private fun initModuleAdapter() {
        binding.rvModule.linear().setup { rv ->
            addType<DeviceStatusInfoGroupItem>(R.layout.item_device_status_info_group)
            addType<DeviceStatusInfoBasicItem>(R.layout.item_device_status_info_basic)
            addType<ConfigModuleTree>(R.layout.item_sub_config_module)
            addType<GapItem>(R.layout.item_device_status_info_gap)
            onCreate {
                when (itemViewType) {
                    R.layout.item_sub_config_module -> {
                        val itemBinding = getBinding<ItemSubConfigModuleBinding>()
                        itemBinding.rvSubModule.setup { subRv ->
                            subRv.addItemDecoration(
                                MyGridSpacingItemDecoration(
                                    2,
                                    ConvertUtils.dp2px(8f), false
                                )
                            )
                            addType<DasSensorSubMonitorStatusItem>(R.layout.item_das_sensor_sub_monitor_status)
                        }
                    }

                    else -> {}
                }
            }
            onBind {

            }
            R.id.item.onClick {
                when (itemViewType) {
                    R.layout.item_device_status_info_basic -> {
                        val item = getModel<DeviceStatusInfoBasicItem>()
//                        processItemClick(item)
                    }

                    else -> {

                    }
                }
            }
        }
    }


    inner class ClickProxy : BaseClickProxy() {
        override fun onToolbarIvClick() {
            val bundle = QueryDeviceDataFragment.newBundleArguments(
                deviceInfo.deviceToken
            )
            nav(binding.llToolbar.ivAction).navigate(
                R.id.action_global_to_queryDeviceDataFragment, bundle
            )
        }

        override fun onConnectOperateClick() {
            if (bleViewModel.isConnected()) {
                showMessage(
                    StringUtils.getString(R.string.disconnect_device_warn),
                    "温馨提示",
                    "确定",
                    {
                        bleViewModel.disconnect()
                    },
                    "取消"
                )
            } else {
                bleViewModel.launch(bleDevice!!)
            }
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

    }

    override fun onBleDeviceReady() {
        super.onBleDeviceReady()
        launchWithViewLifecycle {
//            delay(2000) //延迟 timeMillis 秒后，提示超时
            //蓝牙模式下，等蓝牙建立连接后查询设备工作模式
//            queryData()
        }
    }

    override fun setResultData(cmdStr: String) {
        updateLastCommunicationTime()
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.REBOOT -> {//重启设备
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = StringUtils.getString(R.string.reboot_failed) + result.message
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show(StringUtils.getString(R.string.device_reboot_tip))
                        }
                    }
                }
            }

            else -> {

            }
        }
    }

    override fun createObserver() {
        super.createObserver()
        setupHeartbeat()
    }

    /**
     * 设置心跳检查
     */
    private fun setupHeartbeat() {
        launchWithViewLifecycle {
            lastCommunicationTime
                .debounce(AppContants.Communication.DELAY_10000_MILLIS)  // 30秒无更新触发
                .collect { lastUpdateTime ->
                    val updateTime = TimeUtils.millis2String(lastUpdateTime, "yyyy-MM-dd HH:mm:ss")
                    Timber.d("startTime: ${TimeUtils.getNowString()}，lastUpdateTime：$updateTime")
                    // 仅当设备连接并且需要发送心跳时，才发送心跳包
                    if (mHeadStates.isConnected.get() && isNearbyCommunicationTimeout(lastUpdateTime)) {
                        Timber.d("bingo startTime: ${TimeUtils.getNowString()}，lastUpdateTime：$updateTime")
                        val command = IOTCommandUtil.getCommand(IOTCommandType.HEART_BEAT)
                        Timber.d("发送心跳包指令: $command")
                        sendBleCommand(command)
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}