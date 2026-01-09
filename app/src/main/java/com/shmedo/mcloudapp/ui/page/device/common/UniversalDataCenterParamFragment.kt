package com.shmedo.mcloudapp.ui.page.device.common

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.CenterNumberEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.DataCenterParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.NewDataCenterPlatform
import com.shmedo.lib.cmd.base.iot_cmd.enums.PlatformDataProtocol
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.mcloudapp.extensions.isLB20S
import com.shmedo.lib.cmd.base.iot_cmd.enums.SL651StationType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.common.DataCenterInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.databinding.FragmentDataCenterParamBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.DataCenterStatusItem
import com.shmedo.mcloudapp.model.DefaultPlatformConfigManager
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.DataCenterParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/6/20
 * @desc: 优化后的通用数据中心参数配置页面 - 支持4G和蓝牙两种通讯方式
 *
 * 优化特点：
 * 1. 使用新的通信架构，代码更简洁
 * 2. 统一的错误处理策略
 * 3. 响应驱动的指令执行
 * 4. 保持原有的复杂业务逻辑不变
 * 5. 支持多种数据协议配置（MQTT、TCP-C、SL651、NTRIP、HTTP）
 * 6. 支持多种平台类型选择
 */
class UniversalDataCenterParamFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDataCenterParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: DataCenterParamViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private lateinit var statusItem: DataCenterStatusItem


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_data_center_param, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDataCenterParamBinding
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            handleBackByCheckDataModified()
        }
        registerOnBackPressedDispatcher {
            handleBackByCheckDataModified()
        }
        initRefresh()
        mStates.isEditable.set(true)
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
        //添加这行来保存初始状态
        mStates.saveInitialState()
    }

    /**
     * 初始化默认参数
     */
    private fun resetDefaultParams() {
        mStates.isCenterOpened.set(statusItem.status != "0")

        // 加载默认平台配置（米度物联平台）
        loadPlatformDefaultParameters(NewDataCenterPlatform.MEDO_IOT_PLATFORM.getPlatFormName())
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        val entity = CenterNumberEntity(statusItem.centerid.toString())
        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DATA_CENTER_PARAM, entity)

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig.dialogConfig() // 查询失败显示Dialog
            )
        )
    }

    /**
     * 关闭数据中心服务
     */
    private fun closeDataServer() {
        val entity = DataCenterParamEntity(
            centerid = statusItem.centerid.toString(),
            addr = "",
            port = "",
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_DATA_CENTER_PARAM,
            entity.toCommandString()
        )

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }


    /**
     * 保存配置
     */
    private fun initSaveCommand() {
        // 数据验证
        if (!validateInputData()) {
            return
        }

        val entity = DataCenterParamEntity(
            centerid = statusItem.centerid.toString(),
            addr = mStates.centerServerAddress.get(),
            port = mStates.centerServerPort.get(),
            protocol = mStates.dataProtocol.get(),
            plattype = NewDataCenterPlatform.valueByPlatformName(mStates.platformType.get())
                .getPlatType()
        )

        // MQTT/MQTTS 协议特有配置参数
        if (mStates.dataProtocol.get() == PlatformDataProtocol.MQTT.getCmdValue()) {
            if (mStates.isRegisterVisible.get() && mStates.registerPort.get().isNotEmpty()) {
                try {
                    val port: Int = mStates.registerPort.get().toInt()
                    if (port < 0 || port > 65535) {
                        showMessageDialog("设备注册端口号数值范围[0,65535]!")
                        return
                    }
                } catch (ex: Exception) {
                    showMessageDialog("设备注册端口号数值范围[0,65535]!")
                    return
                }
            }
            entity.projid = mStates.productId.get()
            entity.deviceid = mStates.deviceId.get()
            entity.devicekey = mStates.deviceKey.get()
            entity.regcode = if (mStates.isRegisterCodeVisible.get()) mStates.registerCode.get() else ""
            entity.httpaddr =
                if (mStates.isRegisterVisible.get()) mStates.registerAddress.get() else ""
            entity.httpport =
                if (mStates.isRegisterVisible.get()) mStates.registerPort.get() else ""

        } else if (mStates.dataProtocol.get() == PlatformDataProtocol.SL651.getCmdValue()) {//SL651
            // SL651/SZY206 协议特有配置参数
            entity.type_code =
                SL651StationType.valueByStationName(mStates.stationType.get()).getCode()
            entity.co_address = mStates.centerStationAddr.get()
            entity.password = mStates.password.get()
            entity.taddress = mStates.telemetryStationAddr.get()
            entity.hour_report = if (mStates.hourlyReport.get()) "1" else "0"
            entity.data_link = mStates.maintainReportInterval.get()
            entity.valid_day = mStates.reissuingDataValidDays.get()
            entity.reissue_time = mStates.reissuingDataInterval.get()

        } else if (mStates.isNtripProtocol.get()) {//NTRIP 系列协议
            entity.projid = mStates.productId.get()     //站点信息
            //NTRIP_S 协议基站不需要填写用户名
            entity.deviceid =
                if (mStates.dataProtocol.get() == PlatformDataProtocol.NTRIP_S.getCmdValue()) IOTConstants.NULL_KEY else mStates.deviceId.get() //用户名
            entity.devicekey = mStates.deviceKey.get()  //密码

        } else if (mStates.dataProtocol.get() == PlatformDataProtocol.HTTP.getCmdValue()) {
            entity.taddress = mStates.telemetryStationAddr.get()
        }

        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_DATA_CENTER_PARAM,
            entity.toCommandString()
        )

        sendCommandSequence(
            commands = listOf(command),
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    /**
     * 数据验证逻辑
     */
    private fun validateInputData(): Boolean {
        if (mStates.centerServerAddress.get().isEmpty()) {
            showMessageDialog("请输入链路地址!")
            return false
        }
        if (mStates.centerServerPort.get().isEmpty()) {
            showMessageDialog("请输入链路端口号!")
            return false
        }
        try {
            val port = mStates.centerServerPort.get().toInt()
            if (port < 0 || port > 65535) {
                showMessageDialog("链路端口号数值范围[0,65535]!")
                return false
            }
        } catch (ex: Exception) {
            showMessageDialog("链路端口号数值范围[0,65535]!")
            return false
        }

        return true
    }

    /**
     * 处理指令响应
     */
    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DATA_CENTER_PARAM -> {
                val result = iotParseManager.parse<DataCenterInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_DATA_CENTER_PARAM
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询链路参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is IOTCommandResult.Success -> {
                        try {
                            initDataCenterParam(result.data)
                        } catch (e: Exception) {
                            Timber.e(e)
                            addDeviceLogItem(Log.ERROR, e.errorMsg)
                        }
                    }
                }
            }

            IOTCommandType.MD_SET_DATA_CENTER_PARAM -> {
                val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置链路参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    else -> {
                        if (!isCommunicationExecuting())
                            processNavigateUp()
                    }
                }
            }

            else -> {
                Timber.d("未处理的指令类型: ${IOTCommandUtil.extractCommandType(cmdStr)}")
            }
        }
    }

    /**
     * 初始化数据中心参数
     */
    private fun initDataCenterParam(data: DataCenterInfo) {
        try {
            // 使用 NewDataCenterPlatform 枚举类处理 plattype
            val platform =
                NewDataCenterPlatform.valueByPlatType(data.plattype, data.addr, data.port)
            mStates.platformType.set(platform.getPlatFormName())//平台类型

            mStates.centerServerAddress.set(data.addr)//链路地址
            mStates.centerServerPort.set(data.port)//链路端口
            mStates.dataProtocol.set(data.protocol)//数据协议

            //MQTT/NTRIP 协议参数
            mStates.productId.set(data.projid)//产品ID
            mStates.deviceId.set(data.deviceid)//设备 Id
            mStates.deviceKey.set(data.devicekey)//设备Key
            mStates.registerCode.set(data.regcode)//注册码
            mStates.registerAddress.set(data.httpaddr)//注册地址
            mStates.registerPort.set(data.httpport)//注册端口

            // 根据平台类型更新 UI 显示状态
            updatePlatformUIState(platform.getPlatFormName())

            //SL651 水文协议参数
            mStates.stationType.set(
                SL651StationType.valueByCode(data.type_code).getStationName()
            )//测站分类
            mStates.centerStationAddr.set(data.co_address)// 中心站地址
            mStates.password.set(data.password)// 密码
            mStates.telemetryStationAddr.set(data.taddress)// 测站编码(遥测站地址)

            mStates.hourlyReport.set(data.hour_report == "1")// 小时报开启标识
            mStates.maintainReportInterval.set(data.data_link)// 维持上报间隔（秒）
            mStates.reissuingDataValidDays.set(data.valid_day)// 数据补发有效天数
            mStates.reissuingDataInterval.set(data.reissue_time)// 数据补发间隔（分钟）


            if (communicateWay is NetPlatformConnect && mStates.platformType.get() == NewDataCenterPlatform.MEDO_IOT_PLATFORM.getPlatFormName() && statusItem.status == "1") {
                mStates.isEditable.set(false)
                showMessageDialog("4G模式下，米度物联平台链路不允许修改，以免设备离线")
            }

            //添加这行来保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 加载平台默认参数
     */
    private fun loadPlatformDefaultParameters(platformName: String) {
        val defaultConfig = DefaultPlatformConfigManager.getDefaultConfigByName(platformName)
        defaultConfig?.let {
            DefaultPlatformConfigManager.applyDefaultConfig(it, mStates)
        }
        // 更新平台相关的 UI 状态
        updatePlatformUIState(platformName)
    }

    /**
     * 根据选中的平台类型更新 UI 显示状态
     * 处理云南地灾加密平台的特殊显示逻辑：
     * - 隐藏设备注册码
     * - "设备注册地址" 改为 "加密服务地址"
     * - "设备注册端口" 改为 "加密服务端口"
     */
    private fun updatePlatformUIState(platformName: String) {
        val isMqttProtocol =
            mStates.dataProtocol.get() == PlatformDataProtocol.MQTT.getCmdValue()
        val isMedoIotPlatform = platformName == NewDataCenterPlatform.MEDO_IOT_PLATFORM.getPlatFormName()
        val isYunnanEncryptPlatform =
            platformName == NewDataCenterPlatform.YUNNAN_DISASTER_ENCRYPT_PLATFORM.getPlatFormName()

        // 更新云南地灾加密平台标识
        mStates.isYunnanEncryptPlatform.set(isYunnanEncryptPlatform)

        // 更新注册相关字段可见性
        // 米度物联平台: 显示注册码、注册地址、注册端口
        // 云南地灾加密平台: 不显示注册码，但显示加密服务地址和端口
        mStates.isRegisterVisible.set(isMqttProtocol && (isMedoIotPlatform || isYunnanEncryptPlatform))

        // 设备注册码只对米度物联平台可见
        mStates.isRegisterCodeVisible.set(isMqttProtocol && isMedoIotPlatform)

        // 根据平台类型设置标签文本
        if (isYunnanEncryptPlatform) {
            mStates.registerAddressLabel.set("加密服务地址")
            mStates.registerPortLabel.set("加密服务端口")
        } else {
            mStates.registerAddressLabel.set("设备注册地址")
            mStates.registerPortLabel.set("设备注册端口")
        }
    }

    /**
     * 点击事件处理
     */
    inner class ClickProxy : BaseClickProxy() {
        /**
         * 平台类型
         */
        fun onPlatformTypeChooseClick() {
            val supportedPlatformNames =
                DefaultPlatformConfigManager.getSupportedPlatformNames(productType).toTypedArray()
            val selectedIndex = supportedPlatformNames.indexOf(mStates.platformType.get())

            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择平台类型", supportedPlatformNames,
                    null, selectedIndex,
                    { position, text ->
                        mStates.platformType.set(text)
                        // 加载选中平台的默认参数，并更新 UI 状态
                        loadPlatformDefaultParameters(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 数据协议
         */
        fun onDataProtocolChooseClick() {
            val supportedDataProtocolNames =
                DefaultPlatformConfigManager.getSupportedDataProtocolNames(
                    mStates.platformType.get(),
                    productType
                ).toTypedArray()
            val selectedIndex = supportedDataProtocolNames.indexOf(mStates.dataProtocol.get())

            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择数据协议", supportedDataProtocolNames,
                    null, selectedIndex,
                    { position, text ->
                        mStates.dataProtocol.set(text)
                        // 更新 UI 状态以反映协议变化
                        updatePlatformUIState(mStates.platformType.get())
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 测站分类
         */
        fun onStationClassificationChooseClick() {
            val codeList = SL651StationType.entries.map { it.getStationName() }
            val selectedIndex = codeList.indexOf(mStates.stationType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择测站分类", codeList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.stationType.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 高级设置展开、折叠
         */
        fun onToggleAdvancedClick() {
            mStates.isAdvancedItemVisible.set(!mStates.isAdvancedItemVisible.get())
        }

        /**
         * 恢复默认配置
         */
        override fun onResetButtonClick() {
            resetDefaultParams()
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


    override fun handleBackByCheckDataModified() {
        if (mStates.isDataModified.value == true) {
            showExitConfirmationDialog()
            return
        }
        nav().navigateUp()
    }

    override fun processNavigateUp(toastMsg: String, isShowToast: Boolean) {
        launchWithViewLifecycle {
            delay(1000)
            //需要给上一级页面传递最新的信息
            if (isNeedRefreshDataCenterStatus()) {
                setFragmentResult(
                    AppContants.Extras.FRAGMENT_DATA_CENTER_HOME_RESULT_REQUEST_KEY,
                    bundleOf(AppContants.Extras.REFRESH_DATA_CENTER_STATUS to statusItem.centerid)
                )
            }
            nav().navigateUp()
        }
    }

    /**
     * 是否需要给上一级页面传递最新的信息
     */
    private fun isNeedRefreshDataCenterStatus(): Boolean {
        return communicateWay is BleConnect &&
                (!productType.isLB20S())
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