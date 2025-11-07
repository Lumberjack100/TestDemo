package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.enums.DataCenterPlatform
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.md_cmd.assemble.entity.common.RegistrationPlatformEntity
import com.shmedo.lib.cmd.base.md_cmd.assemble.entity.common.ServerAddressInfoEntity
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.enums.SaveConfigMode
import com.shmedo.lib.cmd.base.md_cmd.model.common.BleDataCenterInfo
import com.shmedo.lib.cmd.base.md_cmd.model.common.ServerAddressInfo
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.FragmentBleDasDataCenterParamBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.DataCenterStatusItem
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.BleDasDataCenterParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/6/17
 * @desc: 物联网采集器(DAS)数据中心参数配置页面 - 支持蓝牙通讯方式
 *
 */
class BleDasDataCenterParamFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentBleDasDataCenterParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: BleDasDataCenterParamViewModel by viewModels()
    private val mdParseManager: MDParserManager by inject()

    private lateinit var statusItem: DataCenterStatusItem

    private val transferProtocolList by lazy { Utils.getApp().resources.getStringArray(R.array.register_protocol) }



    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_ble_das_data_center_param, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentBleDasDataCenterParamBinding
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            processBack(true)
        }
        registerOnBackPressedDispatcher {
            processBack(true)
        }
        toolbarViewModel.toolbarIvActionVisible.set(false)
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_refresh_fail_warn))
                finishRefresh()
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
        binding.llToolbar.toolbar.title = statusItem.name.replace("数据", "") + "配置"
        resetDefaultParams()
    }

    /**
     * 初始化默认参数
     */
    private fun resetDefaultParams() {
        mStates.isCenterOpened.set(statusItem.status != "0")
        mStates.centerName.set(statusItem.name)
        mStates.centerStatus.set(statusItem.status)
        mStates.transferProtocol.set(transferProtocolList[0])
        mStates.transferProtocolCode.set("2")
        mStates.platformType.set(DataCenterPlatform.MEDO_IOT_PLATFORM.getPlatName())//默认选择米度物联平台
    }

    inner class ClickProxy : BaseClickProxy() {

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
            val allPlatformNames = DataCenterPlatform.platNames.toTypedArray()
            val selectedIndex = allPlatformNames.indexOf(mStates.platformType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择平台类型", allPlatformNames,
                    null, selectedIndex,
                    { position, text ->
                        mStates.platformType.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            if (!mStates.isCenterOpened.get()) {
                closeDataServer()
                return
            }
            initSaveCommand()
        }
    }

    /**
     * 关闭数据链路</br>
     * addr和port设置为空时，关闭该数据链路
     */
    private fun closeDataServer() {
        val command = MDCommandUtil.getCommand(
            MDCommandType.SET_SERVER_ADDRESS_PORT,
            statusItem.centerid.toString()
        )
        Timber.d("关闭数据服务器%s指令===%s", statusItem.centerid.toString(), command)

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    private fun initSaveCommand() {
        if (mStates.centerServerAddress.get().isEmpty()) {
            showMessageDialog("请输入链路地址!")
            return
        }
        if (mStates.centerServerPort.get().isEmpty()) {
            showMessageDialog("请输入链路端口号!")
            return
        }
        try {
            val port: Int = mStates.centerServerPort.get().toInt()
            if (port < 0 || port > 65535) {
                showMessageDialog("链路端口号数值范围[0,65535]!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("链路端口号数值范围[0,65535]!")
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

        val commands = mutableListOf<String>()

        //网络中心通讯协议
        var command = MDCommandUtil.getCommand(
            MDCommandType.NET_LINK_COMMUN_PROTOCOL,
            "${statusItem.centerid}${mStates.transferProtocolCode.get()}"
        )
        Timber.d("设置网络中心通讯协议===%s", command)
        commands.add(command)

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
        commands.add(command)

        if (mStates.transferProtocolCode.get() == "4") {//MQTT自动注册
            //选择注册平台
            command = MDCommandUtil.getCommand(
                MDCommandType.AUTO_REGISTRATION_PLATFORM,
                "${statusItem.centerid}${DataCenterPlatform.valueByPlatformName(mStates.platformType.get()).getCmdValue()}"
            )
            Timber.d("选择平台配置===%s", command)
            commands.add(command)

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
            commands.add(command)

            //MQTT KeepAlive值
            command = MDCommandUtil.getCommand(
                MDCommandType.MQTT_KEEP_ALIVE,
                "${statusItem.centerid}${mStates.keepAlive.get()}"
            )
            Timber.d("设置KeepAlive===%s", command)
            commands.add(command)

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
            commands.add(command)

        } else if (mStates.transferProtocolCode.get() == "5") {//MQTT手动注册
            //选择注册平台
            command = MDCommandUtil.getCommand(
                MDCommandType.AUTO_REGISTRATION_PLATFORM,
                "${statusItem.centerid}${DataCenterPlatform.valueByPlatformName(mStates.platformType.get()).getCmdValue()}"
            )
            Timber.d("选择平台配置===%s", command)
            commands.add(command)

            //MQTT KeepAlive值
            command = MDCommandUtil.getCommand(
                MDCommandType.MQTT_KEEP_ALIVE,
                "${statusItem.centerid}${mStates.keepAlive.get()}"
            )
            Timber.d("设置KeepAlive===%s", command)
            commands.add(command)

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
            commands.add(command)
        }

        command = MDCommandUtil.getCommand(
            MDCommandType.SAVE_CONFIG_INFO,
            SaveConfigMode.SAVE_NO_REBOOT.toString()
        )
        commands.add(command)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        //查询数据链路参数
        val command = MDCommandUtil.getCommand(
            MDCommandType.QUERY_DATA_CENTER_PARAM,
            statusItem.centerid.toString()
        )
        Timber.d("查询数据链路%s的参数===%s", statusItem.centerid.toString(), command)

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig() // 查询失败显示Dialog
            )
        )
    }

    override fun handleCommandResponse(cmdStr: String) {
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.SERVER_ADDRESS -> {//获取服务器1、2、3 的地址
                val result = mdParseManager.parse<ServerAddressInfo>(
                    cmdStr,
                    MDCommandType.SERVER_ADDRESS
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询数据链路地址出错"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is MDCommandResult.Success -> {
                    }
                }
            }

            MDCommandType.QUERY_DATA_CENTER_PARAM -> {//查询数据链路 1、2、3 参数
                val result = mdParseManager.parse<BleDataCenterInfo>(
                    cmdStr,
                    MDCommandType.QUERY_DATA_CENTER_PARAM
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询数据链路参数出错"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is MDCommandResult.Success -> {
                        initDataCenterParam(result.data)
                    }
                }
            }

            MDCommandType.NET_LINK_COMMUN_PROTOCOL -> {//设置通讯协议应答
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "通讯协议配置错误!"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {

                    }
                }
            }

            MDCommandType.SET_SERVER_ADDRESS_PORT -> {//设置数据服务器地址、端口应答
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "设置数据链路地址、端口错误!"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {

                    }
                }
            }

            MDCommandType.AUTO_REGISTRATION_PLATFORM -> {//MQTT 自动注册设置通选择注册平台时应答
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "平台类型配置错误!"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {

                    }
                }
            }

            MDCommandType.SET_AUTO_REGISTRATION_PLATFORM_SERVER_ADDRESS_PORT -> {//MQTT 自动注册设置注册平台地址时应答
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "注册平台地址、端口配置错误!"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {

                    }
                }
            }

            MDCommandType.MQTT_KEEP_ALIVE -> {//设置KeepAlive值应答
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "心跳间隔配置错误!"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {

                    }
                }
            }

            MDCommandType.SET_AUTO_REGISTRATION_PLATFORM_PARAM -> {//MQTT 自动注册设置参数
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "自动注册平台参数配置错误!"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {

                    }
                }
            }

            MDCommandType.SET_MANUAL_REGISTRATION_PLATFORM_PARAM -> {//MQTT 手动注册设置参数
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "手动注册平台参数配置错误!"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {

                    }
                }
            }

            MDCommandType.SAVE_CONFIG_INFO -> {//
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "保存参数出错!"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        if (!isCommunicationExecuting())
                            processNavigateUp()
                    }
                }
            }

            else -> {

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

        // 使用 DataCenterPlatform 枚举类处理 plattype
        val platform = DataCenterPlatform.valueByCmdValue(data.registerPlatform)
        mStates.platformType.set(platform.getPlatName())

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


    private fun processBack(isPressBackBtn: Boolean = false) {
        launchWithViewLifecycle {
//            if (isPressBackBtn) {
//                mMessenger.requestStatusBarColor(if (statusBarColor == 0) R.color.colorPrimary else statusBarColor)
//                nav().navigateUp()
//                return@launchWithViewLifecycle
//            }
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