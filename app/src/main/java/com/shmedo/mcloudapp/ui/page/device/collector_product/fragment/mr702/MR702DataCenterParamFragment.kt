package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702

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
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.extensions.decimalStringToHexString
import com.shmedo.core.commonlib.extensions.hexStringToDecimalString
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.CenterNumberEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRDataCenterParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.GuangdongWaterPlatformStationType
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.NewDataCenterPlatform
import com.shmedo.lib.cmd.base.iot_cmd.enums.PlatformDataProtocol
import com.shmedo.lib.cmd.base.iot_cmd.enums.SL651StationType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRDataCenterParam
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
import com.shmedo.mcloudapp.databinding.FragmentMr702DataCenterParamBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.DataCenterStatusItem
import com.shmedo.mcloudapp.model.DefaultPlatformConfigManager
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702DataCenterParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/8/5
 * @desc: 优化后的MR702数据中心参数配置页面
 *
 * 优化特点：
 * 1. 使用新的通信架构，代码更简洁
 * 2. 统一的错误处理策略
 * 3. 响应驱动的指令执行
 * 4. 保持原有的复杂业务逻辑不变
 * 5. 支持多种数据协议配置（MQTT、TCP-C、SL651、SZY206、MQTTS）
 * 6. 支持多种平台类型选择
 */
class MR702DataCenterParamFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702DataCenterParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: MR702DataCenterParamViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private lateinit var statusItem: DataCenterStatusItem

    private val communicateWayList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_communicate_way) }
    private val ipLevelList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_ip_level) }
    private val transferProtocolList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_transfer_protocol) }

    private val dataProtocolList: MutableList<String> = arrayListOf()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_mr702_data_center_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702DataCenterParamBinding
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            handleBackByCheckDataModified()
        }
        registerOnBackPressedDispatcher { handleBackByCheckDataModified() }
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
        // 保存初始状态
        mStates.saveInitialState()
    }

    private fun resetDefaultParams() {
        dataProtocolList.clear()
        dataProtocolList.addAll(PlatformDataProtocol.getDataProtocolNamesByProduct(productType))

        mStates.isCenterOpened.set(statusItem.status != "0")
        mStates.communicateWay.set(communicateWayList[0]) //通信方式 默认选择4G
        mStates.ipLeve.set(ipLevelList[0]) //网络协议 默认选择IPV4
        mStates.transferProtocol.set(transferProtocolList[0]) //传输协议 默认选择TCP

        // 加载默认平台配置（米度物联平台）
        loadPlatformDefaultParameters(NewDataCenterPlatform.MEDO_IOT_PLATFORM.getPlatFormName())
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 查询数据
     */
    private fun queryData() {
        val entity = CenterNumberEntity(statusItem.centerid.toString())
        val command = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_DATA_CENTER, entity)

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
        val entity = MRDataCenterParamEntity(
            centerid = statusItem.centerid.toString(),
            switch = "0"
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_SET_DATA_CENTER,
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
    private fun saveConfiguration() {
        // 数据验证
        if (!validateInputData()) {
            return
        }

        val entity = MRDataCenterParamEntity(
            centerid = statusItem.centerid.toString(),
            switch = "1",
            addr = mStates.centerServerAddress.get(),
            port = mStates.centerServerPort.get(),
            line = (communicateWayList.indexOf(mStates.communicateWay.get()) + 1).toString(),// 通信方式
            level = (ipLevelList.indexOf(mStates.ipLeve.get()) + 1).toString(),// IP 网络协议
            type = (transferProtocolList.indexOf(mStates.transferProtocol.get()) + 1).toString(),//传输协议
            datatype = (dataProtocolList.indexOf(mStates.dataProtocol.get()) + 1).toString(),//数据协议
            plattype = NewDataCenterPlatform.valueByPlatformName(mStates.platformType.get())//平台类型
                .getPlatType(),
            packtype = if (mStates.platformType.get() == NewDataCenterPlatform.GUANGDONG_WATER_PLATFORM.getPlatFormName()) // 测站类型
                GuangdongWaterPlatformStationType.valueByStationName(mStates.guangdongWaterPlatformStationType.get())
                    .getCode()
            else IOTConstants.NULL_KEY
        )

        // MQTT/MQTTS 协议特有配置参数
        if (mStates.dataProtocol.get() == PlatformDataProtocol.MQTT.getCmdValue() || mStates.dataProtocol.get() == PlatformDataProtocol.MQTTS.getCmdValue()) {
            // 当产品 ID、设备 ID 为空时，需要填写设备注册码、设备注册地址、设备注册端口号
            if (mStates.productId.get().isEmpty() && mStates.deviceId.get().isEmpty()) {
                if (mStates.registerCode.get().isEmpty()) {
                    showMessageDialog("请输入设备注册码!")
                    return
                }
                if (mStates.registerAddress.get().isEmpty()) {
                    showMessageDialog("请输入设备注册地址!")
                    return
                }
                if (mStates.registerPort.get().isEmpty()) {
                    showMessageDialog("请输入设备注册端口号!")
                    return
                }
            }
            // MQTT/MQTTS 协议注册端口验证
            if (mStates.registerPort.get().isNotEmpty()) {
                try {
                    val port = mStates.registerPort.get().toInt()
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
            entity.regcode = mStates.registerCode.get()
            entity.httpaddr = mStates.registerAddress.get()
            entity.httpport = mStates.registerPort.get()

            if (mStates.dataProtocol.get() == PlatformDataProtocol.MQTTS.getCmdValue())
                entity.taddress = mStates.telemetryStationAddr.get() //测站编码

        } else if (mStates.dataProtocol.get() == PlatformDataProtocol.SL651.getCmdValue() || mStates.dataProtocol.get() == PlatformDataProtocol.SZY206.getCmdValue()) {
            // SL651/SZY206 协议特有配置参数
            entity.type_code =
                if (mStates.dataProtocol.get() == PlatformDataProtocol.SL651.getCmdValue())
                    SL651StationType.valueByStationName(mStates.stationType.get())
                        .getCode() else IOTConstants.NULL_KEY
            entity.co_address =
                if (mStates.dataProtocol.get() == PlatformDataProtocol.SL651.getCmdValue())
                    mStates.centerStationAddr.get() else IOTConstants.NULL_KEY
            entity.password =
                if (mStates.platformType.get() == NewDataCenterPlatform.HUBEI_WATER_PLATFORM.getPlatFormName()) {
                    mStates.password.get().hexStringToDecimalString()
                } else {
                    mStates.password.get()
                }
            entity.taddress = mStates.telemetryStationAddr.get()
            entity.hour_report = if (mStates.hourlyReport.get()) "1" else "0"
            entity.timed_report = if (mStates.timingReport.get()) "1" else "0"
            entity.add_report = if (mStates.addReport.get()) "1" else "0"
            entity.maintain_report = if (mStates.maintainReport.get()) "1" else "0"
            entity.keepalive = mStates.maintainReportInterval.get()
            entity.valid_day = mStates.reissuingDataValidDays.get()
            entity.reissue_time = mStates.reissuingDataInterval.get()
        }

        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_SET_DATA_CENTER,
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
            IOTCommandType.MR_MD_GET_DATA_CENTER -> {
                val result = iotParseManager.parse<MRDataCenterParam>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_DATA_CENTER
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询链路参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                    }

                    is IOTCommandResult.Success -> {
                        initDataCenterParam(result.data)
                    }
                }
            }

            IOTCommandType.MR_MD_SET_DATA_CENTER -> {
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
    private fun initDataCenterParam(data: MRDataCenterParam) {
        try {
            mStates.isDataNetOpened.set(data.datanet == "1")
            mStates.isWiredNetOpened.set(data.wirednet == "1")

            mStates.centerServerAddress.set(data.addr)
            mStates.centerServerPort.set(data.port)

            data.line.toIntOrNull()?.let {
                if (it in 1..communicateWayList.size) {
                    mStates.communicateWay.set(communicateWayList[it - 1])
                }
            }
            data.level.toIntOrNull()?.let {
                if (it in 1..ipLevelList.size) {
                    mStates.ipLeve.set(ipLevelList[it - 1])
                }
            }

            data.type.toIntOrNull()?.let {
                if (it in 1..transferProtocolList.size) {
                    mStates.transferProtocol.set(transferProtocolList[it - 1])
                }
            }

            // 使用 DataCenterPlatform 枚举类处理 plattype
            val platform = NewDataCenterPlatform.valueByPlatType(data.plattype)
            mStates.platformType.set(platform.getPlatFormName())

            mStates.dataProtocol.set(
                when (data.datatype) {
                    "1", "5" -> { // MQTT, MQTTS
                        if (data.datatype == "1") dataProtocolList[0] else dataProtocolList[4]
                    }

                    "2" -> { // TCP-C
                        dataProtocolList[1]
                    }

                    "3" -> { // SL651
                        dataProtocolList[2]
                    }

                    else -> { // SZY206
                        dataProtocolList[3]
                    }
                }
            )

            // Handle packtype for Guangdong Water Platform station types
            // The mapping is: 0=山洪灾害监测站, 1=河道水情监测站, 3=沉降监测站, 4=水质监测站, 5=雨量监测站, 6=流量监测站
            mStates.guangdongWaterPlatformStationType.set(
                GuangdongWaterPlatformStationType.valueByCode(
                    data.packtype
                ).getStationName()
            )

            // MQTT 协议参数
            mStates.productId.set(data.projid)
            mStates.deviceId.set(data.deviceid)
            mStates.deviceKey.set(data.devicekey)
            mStates.registerCode.set(data.regcode)
            mStates.registerAddress.set(data.httpaddr)
            mStates.registerPort.set(data.httpport)

            // SL651/SZY206 协议参数
            if (mStates.dataProtocol.get() == dataProtocolList[2]) {
                mStates.stationType.set(
                    SL651StationType.valueByCode(data.type_code).getStationName()
                )//测站分类
                mStates.centerStationAddr.set(data.co_address)//中心站地址
            }

            // 根据平台类型设置密码显示格式
            mStates.password.set(
                if (platform == NewDataCenterPlatform.HUBEI_WATER_PLATFORM) {
                    data.password.decimalStringToHexString()//湖北水文平台密码需要转成 16 进制字符展示
                } else {
                    data.password
                }
            )
            mStates.telemetryStationAddr.set(data.taddress)//遥测站地址/测站编码
            mStates.hourlyReport.set(data.hour_report == "1")
            mStates.timingReport.set(data.timed_report == "1")
            mStates.addReport.set(data.add_report == "1")
            mStates.maintainReport.set(data.maintain_report == "1")
            mStates.maintainReportInterval.set(data.keepalive)
            mStates.reissuingDataValidDays.set(data.valid_day)
            mStates.reissuingDataInterval.set(data.reissue_time)

            if (communicateWay is NetPlatformConnect && mStates.platformType.get() == NewDataCenterPlatform.MEDO_IOT_PLATFORM.getPlatFormName() && statusItem.status == "1") {
                mStates.isEditable.set(false)
                showMessageDialog("4G模式下，米度物联平台链路不允许修改，以免设备离线")
            }

            // 保存初始状态
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
    }

    /**
     * 点击事件处理
     */
    inner class ClickProxy : BaseClickProxy() {
        /**
         * 平台类型选择
         */
        fun onPlatformTypeChooseClick() {
            val supportedPlatformNames =
                DefaultPlatformConfigManager.getSupportedPlatformNames(productType).toTypedArray()
            val selectedIndex = supportedPlatformNames.indexOf(mStates.platformType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "请选择平台类型", supportedPlatformNames,
                    null, selectedIndex,
                    { position, text ->
                        mStates.platformType.set(text)
                        // 加载选中平台的默认参数
                        loadPlatformDefaultParameters(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 通信方式选择
         */
        fun onCommunicateWayChooseClick() {
            val selectedIndex = communicateWayList.indexOf(mStates.communicateWay.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "请选择通信方式", communicateWayList,
                    null, selectedIndex,
                    { position, text ->
                        if (position == 0 && !mStates.isDataNetOpened.get()) {
                            showMessageDialog(
                                "请先到网络与通信页面中开启4G后再选择此项！",
                                "提示",
                                "我已知晓"
                            )
                            return@asBottomList
                        } else if (position == 1 && !mStates.isWiredNetOpened.get()) {
                            showMessageDialog(
                                "请先到网络与通信页面中开启以太网功能后再选择此项！",
                                "提示",
                                "我已知晓"
                            )
                            return@asBottomList
                        }
                        mStates.communicateWay.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 网络协议选择
         */
        fun onIPTypeChooseClick() {
            val selectedIndex = ipLevelList.indexOf(mStates.ipLeve.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "请选择网络协议", ipLevelList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.ipLeve.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 传输协议选择
         */
        fun onTransferProtocolChooseClick() {
            val selectedIndex = transferProtocolList.indexOf(mStates.transferProtocol.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "请选择传输协议", transferProtocolList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.transferProtocol.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 数据协议选择
         */
        fun onDataProtocolChooseClick() {
            val selectedIndex = dataProtocolList.indexOf(mStates.dataProtocol.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "请选择数据协议", dataProtocolList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.dataProtocol.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 广东水文平台测站类型选择
         */
        fun onGuangdongWaterPlatformStationTypeChooseClick() {
            val stationList = GuangdongWaterPlatformStationType.stationNames
            val selectedIndex = stationList.indexOf(mStates.guangdongWaterPlatformStationType.get())

            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "请选择测站类型", stationList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.guangdongWaterPlatformStationType.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 测站分类选择
         */
        fun onStationClassificationChooseClick() {
            val stationList = SL651StationType.stationNames
            val selectedIndex = stationList.indexOf(mStates.stationType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true)
                .enableDrag(false)
                .asBottomList(
                    "请选择测站分类", stationList.toTypedArray(),
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
            saveConfiguration()
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
            // 需要给上一级页面传递最新的信息
            setFragmentResult(
                AppContants.Extras.FRAGMENT_DATA_CENTER_HOME_RESULT_REQUEST_KEY,
                bundleOf(MR702DataCenterHomeFragment.REFRESH_DATA to true)
            )
            nav().navigateUp()
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}