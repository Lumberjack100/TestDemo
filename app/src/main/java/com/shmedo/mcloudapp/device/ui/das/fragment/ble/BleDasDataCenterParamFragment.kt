package com.shmedo.mcloudapp.device.ui.das.fragment.ble

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.lxj.xpopup.XPopup
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.lib.device.base.md_cmd.assemble.entity.common.RegistrationPlatformEntity
import com.shmedo.lib.device.base.md_cmd.assemble.entity.common.ServerAddressInfoEntity
import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.enums.SaveConfigMode
import com.shmedo.lib.device.base.md_cmd.model.common.BleDataCenterInfo
import com.shmedo.lib.device.base.md_cmd.model.common.ServerAddressInfo
import com.shmedo.lib.device.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.device.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.device.base.md_cmd.utils.MDCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentBleDasDataCenterParamBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.DataCenterStatusItem
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.BleDasDataCenterParamViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessage
import com.shmedo.mcloudapp.ext.showMessageDialog
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import timber.log.Timber

class BleDasDataCenterParamFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentBleDasDataCenterParamBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: BleDasDataCenterParamViewModel
    private val mdParseManager: MDParserManager by inject()

    private lateinit var statusItem: DataCenterStatusItem

    private val transferProtocolList by lazy { Utils.getApp().resources.getStringArray(R.array.register_protocol) }
    private val platformList by lazy { Utils.getApp().resources.getStringArray(R.array.data_center_register_platform) }


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_ble_das_data_center_param, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentBleDasDataCenterParamBinding
        binding.llToolbar.toolbar.title = "数据中心"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            processBack(true)
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                processBack(true)
            }
        })
//        toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_device_param_edit)
//        toolbarViewModel.toolbarTvActionText.set("取消")
        toolbarViewModel.toolbarIvActionVisible.set(false)
        mStates.isEditable.set(true)
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryData()
        }
    }

    override fun initData() {
        super.initData()
        arguments?.let {
            statusItem = it.getParcelable(AppContants.Extras.SERVER_NUMBER)!!
        }
        mStates.centerName.set(statusItem.name)
        mStates.centerStatus.set(statusItem.status)
        mStates.isCenterOpened.set(statusItem.status != "0")
        mStates.transferProtocol.set(transferProtocolList[0])
        mStates.transferProtocolCode.set("2")
        mStates.platformType.set(platformList[0])
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
                return
            }
            mStates.isCenterOpened.set(isChecked)
            if (!isChecked) {
                showMessage("确定要关闭数据中心吗？", "温馨提示", "确定", {
                    closeDataServer()
                }, "取消", {
                    mStates.isCenterOpened.set(true)
                    (button as SwitchButton).setCheckedImmediatelyNoEvent(true)
                })
            }
        }

        /**
         * 传输协议
         */
        fun onTransferProtocolChooseClick() {
            val selectedIndex = transferProtocolList.indexOf(mStates.transferProtocol.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择传输协议", transferProtocolList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.transferProtocol.set(text)
                        when (position) {
                            0 -> {//MDM协议
                                mStates.isMqttItemVisible.set(false)
                                mStates.transferProtocolCode.set("2")
                            }

                            1 -> {//MQTT自动注册
                                mStates.isMqttItemVisible.set(true)
                                mStates.isMqttAutoRegister.set(true)
                                mStates.transferProtocolCode.set("4")
                            }

                            2 -> {//MQTT手动注册
                                mStates.isMqttItemVisible.set(true)
                                mStates.isMqttAutoRegister.set(false)
                                mStates.transferProtocolCode.set("5")
                            }

                            else -> {
                            }
                        }
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 平台类型
         */
        fun onPlatformTypeChooseClick() {
            val selectedIndex = platformList.indexOf(mStates.platformType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择平台类型", platformList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.platformType.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onSubmitClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    /**
     * 关闭数据中心</br>
     * addr和port设置为空时，关闭该数据中心
     */
    private fun closeDataServer() {
        commandItems.clear()
        val command = MDCommandUtil.getCommand(
            MDCommandType.SET_SERVER_ADDRESS_PORT,
            statusItem.centerid.toString()
        )
        commandItems.add(command)
        Timber.d("关闭数据服务器%s指令===%s", statusItem.centerid.toString(), command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun initSaveCommand() {
        commandItems.clear()
        if (mStates.centerServerAddress.get().isEmpty()) {
            showMessageDialog("请输入数据中心地址!")
            return
        }
        if (mStates.centerServerPort.get().isEmpty()) {
            showMessageDialog("请输入数据中心端口号!")
            return
        }
        try {
            val port: Int = mStates.centerServerPort.get().toInt()
            if (port < 0 || port > 65535) {
                showMessageDialog("数据中心端口号数值范围[0,65535]!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("数据中心端口号数值范围[0,65535]!")
            return
        }
        if (mStates.transferProtocol.get().equals(transferProtocolList[1], true)) {
            if (mStates.registerAddress.get().isEmpty()) {
                showMessageDialog("请输入设备注册地址!")
                return
            }
            if (mStates.centerServerPort.get().isEmpty()) {
                showMessageDialog("请输入设备注册端口号!")
                return
            }
            if (mStates.registerPort.get().isNotEmpty()) {
                try {
                    val port: Int = mStates.centerServerPort.get().toInt()
                    if (port < 0 || port > 65535) {
                        showMessageDialog("设备注册端口号数值范围[0,65535]!")
                        return
                    }
                } catch (ex: Exception) {
                    showMessageDialog("设备注册端口号数值范围[0,65535]!")
                    return
                }
            }
            if (mStates.keepAlive.get().isEmpty()) {
                showMessageDialog("请输入心跳间隔!")
                return
            }
            try {
                val keepAlive: Int = mStates.keepAlive.get().toInt()
                if (keepAlive < 0 || keepAlive > 65535) {
                    showMessageDialog("心跳间隔数值范围[0,65535]!")
                    return
                }
            } catch (ex: Exception) {
                showMessageDialog("心跳间隔数值范围[0,65535]!")
                return
            }
            if (mStates.sn.get().isEmpty()) {
                showMessageDialog("请输入设备 SN!")
                return
            }
            if (mStates.productId.get().isEmpty()) {
                showMessageDialog("请输入产品 ID!")
                return
            }
            if (mStates.productId.get().isEmpty()) {
                showMessageDialog("请输入设备注册码!")
                return
            }
        }
        if (mStates.transferProtocol.get().equals(transferProtocolList[2], true)) {
            if (mStates.keepAlive.get().isEmpty()) {
                showMessageDialog("请输入心跳间隔!")
                return
            }
            try {
                val keepAlive: Int = mStates.keepAlive.get().toInt()
                if (keepAlive < 0 || keepAlive > 65535) {
                    showMessageDialog("心跳间隔数值范围[0,65535]!")
                    return
                }
            } catch (ex: Exception) {
                showMessageDialog("心跳间隔数值范围[0,65535]!")
                return
            }
            if (mStates.deviceId.get().isEmpty()) {
                showMessageDialog("请输入MQTT设备ID!")
                return
            }
            if (mStates.account.get().isEmpty()) {
                showMessageDialog("请输入MQTT用户名!")
                return
            }
            if (mStates.password.get().isEmpty()) {
                showMessageDialog("请输入MQTT密码!")
                return
            }
        }

        //网络中心通讯协议
        var command = MDCommandUtil.getCommand(
            MDCommandType.NET_LINK_COMMUN_PROTOCOL,
            "${statusItem.centerid}${mStates.transferProtocolCode.get()}"
        )
        Timber.d("设置网络中心通讯协议===%s", command)
        commandItems.add(command)

        //数据服务器地址、端口
        var entity = ServerAddressInfoEntity(
            statusItem.centerid.toString(),
            mStates.centerServerAddress.get(),
            mStates.centerServerPort.get()
        )
        command = MDCommandUtil.getCommand(
            MDCommandType.SET_SERVER_ADDRESS_PORT,
            entity.toCommandString()
        )
        Timber.d("设置数据服务器地址、端口===%s", command)
        commandItems.add(command)

        if (mStates.transferProtocolCode.get() == "4") {//MQTT自动注册
            //选择注册平台
            command = MDCommandUtil.getCommand(
                MDCommandType.AUTO_REGISTRATION_PLATFORM,
                "${statusItem.centerid}${platformList.indexOf(mStates.platformType.get())}"
            )
            Timber.d("选择平台配置===%s", command)
            commandItems.add(command)

            //自动注册平台地址端口
            entity = ServerAddressInfoEntity(
                statusItem.centerid.toString(),
                mStates.registerAddress.get(),
                mStates.registerPort.get()
            )
            command = MDCommandUtil.getCommand(
                MDCommandType.SET_AUTO_REGISTRATION_PLATFORM_SERVER_ADDRESS_PORT,
                entity.toCommandString()
            )
            Timber.d("自动注册平台地址、端口===%s", command)
            commandItems.add(command)

            //MQTT KeepAlive值
            command = MDCommandUtil.getCommand(
                MDCommandType.MQTT_KEEP_ALIVE,
                "${statusItem.centerid}${mStates.keepAlive.get()}"
            )
            Timber.d("设置KeepAlive===%s", command)
            commandItems.add(command)

            //自动注册平台参数：设备SN号+产品ID+注册码
            val platformEntity = RegistrationPlatformEntity(
                statusItem.centerid.toString(),
                mStates.sn.get(),
                mStates.productId.get(),
                mStates.registerCode.get()
            )
            command = MDCommandUtil.getCommand(
                MDCommandType.SET_AUTO_REGISTRATION_PLATFORM_PARAM,
                platformEntity.toCommandString()
            )
            Timber.d("自动注册平台参数===%s", command)
            commandItems.add(command)

        } else if (mStates.transferProtocolCode.get() == "5") {//MQTT手动注册
            //选择注册平台
            command = MDCommandUtil.getCommand(
                MDCommandType.AUTO_REGISTRATION_PLATFORM,
                "${statusItem.centerid}${platformList.indexOf(mStates.platformType.get())}"
            )
            Timber.d("选择平台配置===%s", command)
            commandItems.add(command)

            //MQTT KeepAlive值
            command = MDCommandUtil.getCommand(
                MDCommandType.MQTT_KEEP_ALIVE,
                "${statusItem.centerid}${mStates.keepAlive.get()}"
            )
            Timber.d("设置KeepAlive===%s", command)
            commandItems.add(command)

            //手动注册平台参数：产品ID+设备ID+设备KEY
            val platformEntity = RegistrationPlatformEntity(
                statusItem.centerid.toString(),
                mStates.account.get(),
                mStates.deviceId.get(),
                mStates.password.get()
            )
            command = MDCommandUtil.getCommand(
                MDCommandType.SET_MANUAL_REGISTRATION_PLATFORM_PARAM,
                platformEntity.toCommandString()
            )
            Timber.d("手动注册平台参数===%s", command)
            commandItems.add(command)
        }

        command = MDCommandUtil.getCommand(
            MDCommandType.SAVE_CONFIG_INFO,
            SaveConfigMode.SAVE_NO_REBOOT.toString()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(
            isStartTimeoutJob = true,
            timeoutMillis = AppContants.Communication.DELAY_20000_MILLIS
        )
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()

        //获取服务器地址,查询中心开启状态
//        var command = MDCommandUtil.getCommand(MDCommandType.SERVER_ADDRESS, statusItem.centerid.toString())
//        commandItems.add(command)
//        Timber.d("查询数据服务器%s的地址===%s", statusItem.centerid.toString(), command)

        //查询数据中心参数
        val command = MDCommandUtil.getCommand(
            MDCommandType.QUERY_DATA_CENTER_PARAM,
            statusItem.centerid.toString()
        )
        commandItems.add(command)
        Timber.d("查询数据中心%s的参数===%s", statusItem.centerid.toString(), command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.SERVER_ADDRESS -> {//获取服务器1、2、3 的地址
                val result = mdParseManager.parse<ServerAddressInfo>(
                    cmdStr,
                    MDCommandType.SERVER_ADDRESS
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询数据中心地址出错"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
//                        initDataCenterParam(result.data)
                    }
                }
            }

            MDCommandType.QUERY_DATA_CENTER_PARAM -> {//查询数据中心 1、2、3 参数
                val result = mdParseManager.parse<BleDataCenterInfo>(
                    cmdStr,
                    MDCommandType.QUERY_DATA_CENTER_PARAM
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询数据中心参数出错"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initDataCenterParam(result.data)
                    }
                }
            }

            MDCommandType.NET_LINK_COMMUN_PROTOCOL -> {//设置通讯协议应答
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "通讯协议配置错误!"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            MDCommandType.SET_SERVER_ADDRESS_PORT -> {//设置数据服务器地址、端口应答
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置数据中心地址、端口错误!"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            MDCommandType.AUTO_REGISTRATION_PLATFORM -> {//MQTT 自动注册设置通选择注册平台时应答
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "平台类型配置错误!"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            MDCommandType.SET_AUTO_REGISTRATION_PLATFORM_SERVER_ADDRESS_PORT -> {//MQTT 自动注册设置注册平台地址时应答
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "注册平台地址、端口配置错误!"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            MDCommandType.MQTT_KEEP_ALIVE -> {//设置KeepAlive值应答
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "心跳间隔配置错误!"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            MDCommandType.SET_AUTO_REGISTRATION_PLATFORM_PARAM -> {//MQTT 自动注册设置参数
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "自动注册平台参数配置错误!"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            MDCommandType.SET_MANUAL_REGISTRATION_PLATFORM_PARAM -> {//MQTT 手动注册设置参数
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "手动注册平台参数配置错误!"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            MDCommandType.SAVE_CONFIG_INFO -> {//
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "保存参数出错!"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("保存成功")
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initDataCenterParam(data: BleDataCenterInfo) {
        mStates.centerServerAddress.set(data.dataPlatformAddress)
        mStates.centerServerPort.set(data.dataPlatformPort)
        when (data.communicationProtocol) {
            "2" -> {//MDM协议
                mStates.transferProtocol.set(transferProtocolList[0])
                mStates.transferProtocolCode.set("2")
                mStates.isMqttItemVisible.set(false)
            }

            "4" -> {//MQTT自动注册
                mStates.transferProtocol.set(transferProtocolList[1])
                mStates.transferProtocolCode.set("4")
                mStates.isMqttItemVisible.set(true)
                mStates.isMqttAutoRegister.set(true)
            }

            "5" -> {//MQTT手动注册
                mStates.transferProtocol.set(transferProtocolList[2])
                mStates.transferProtocolCode.set("5")
                mStates.isMqttItemVisible.set(true)
                mStates.isMqttAutoRegister.set(false)
            }

            else -> {
            }
        }

        data.registerPlatform.toIntOrNull()?.let {
            if (it in platformList.indices) {
                mStates.platformType.set(platformList[it])
            }
        }
        //MQTT 协议参数
        mStates.registerAddress.set(data.registerPlatformAddress)
        mStates.registerPort.set(data.registerPlatformPort)
        mStates.keepAlive.set(data.keepAliveValue)

        mStates.sn.set(data.deviceSn)
        mStates.productId.set(data.productId)
        mStates.registerCode.set(data.registerCode)

        mStates.deviceId.set(data.mqttDeviceId)
        mStates.account.set(data.mqttUsername)
        mStates.password.set(data.mqttPassword)
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    private fun processBack(isPressBackBtn: Boolean = false) {
        launchWithViewLifecycle {
            if (isPressBackBtn) {
                mMessenger.requestStatusBarColor(if (statusBarColor == 0) R.color.colorPrimary else statusBarColor)
                nav().navigateUp()
                return@launchWithViewLifecycle
            }
            delay(1000)
            //巡护事件需要给上一级浏览页面传递最新的事件信息
            setFragmentResult(
                AppContants.Extras.FRAGMENT_DATA_CENTER_HOME_RESULT_REQUEST_KEY,
                bundleOf(AppContants.Extras.REFRESH_DATA_CENTER_STATUS to statusItem.centerid)
            )
            nav().navigateUp()
        }
    }

    companion object {
        fun newBundleArguments(
            item: DataCenterStatusItem,
            type: ProductType = ProductType.UnKnown,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putParcelable(AppContants.Extras.SERVER_NUMBER, item)
            putParcelable(AppContants.Extras.PRODUCT_TYPE, type)
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}