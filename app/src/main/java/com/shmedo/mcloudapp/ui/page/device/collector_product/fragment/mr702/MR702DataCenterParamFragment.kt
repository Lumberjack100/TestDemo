package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.CenterNumberEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRDataCenterParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.StationCode
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRDataCenterParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentMr702DataCenterParamBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.DataCenterStatusItem
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702DataCenterParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject

/**
 * @author：gonghe
 * @time: 2025/6/19
 * @desc: 数据中心参数配置页面 - 支持4G和蓝牙两种通讯方式
 *
 */
class MR702DataCenterParamFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702DataCenterParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: MR702DataCenterParamViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private lateinit var statusItem: DataCenterStatusItem

    private val communicateWayList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_communicate_way) }
    private val ipLevelList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_ip_level) }
    private val transferProtocolList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_transfer_protocol) }

    private val dataProtocolList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_data_protocol) }
    private val allPlatformList by lazy { Utils.getApp().resources.getStringArray(R.array.data_center_register_platform) }
    private val guangdongWaterPlatformStationTypeList by lazy {
        Utils.getApp().resources.getStringArray(
            R.array.guangdong_water_platform_station_type
        )
    }

    override fun initViewModel() {
        super.initViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_data_center_param, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702DataCenterParamBinding
        binding.llToolbar.toolbar.title = "链路配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (isBleDisconnected()) {
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
        initDefaultParam()
    }

    /**
     * 初始化默认参数
     */
    private fun initDefaultParam() {
        mStates.centerName.set(statusItem.name)
        mStates.centerStatus.set(statusItem.status)
        mStates.isCenterOpened.set(statusItem.status != "0")

        mStates.centerServerAddress.set("")//
        mStates.centerServerPort.set("")//
        mStates.communicateWay.set(communicateWayList[0])//默认选择4G
        mStates.ipLeve.set(ipLevelList[0])//默认选择IPV4
        mStates.transferProtocol.set(transferProtocolList[0])//默认选择TCP
        mStates.dataProtocol.set("MQTT")//默认选择
        mStates.platformType.set(allPlatformList[2])//默认选择米度物联平台
        mStates.guangdongWaterPlatformStationType.set(guangdongWaterPlatformStationTypeList[0])//默认选择山洪灾害监测站

        mStates.isMqttItemVisible.set(true)
        mStates.productId.set("")//
        mStates.deviceId.set("")//
        mStates.deviceKey.set("")//
        mStates.registerCode.set("")//
        mStates.registerAddress.set("")//
        mStates.registerPort.set("")//

        mStates.stationType.set(StationCode.RESERVOIR.description)//默认选择水库(湖泊)
        mStates.timingReport.set(true)
        mStates.maintainReport.set(true)
        mStates.maintainReportInterval.set("30")//维持上报间隔（秒）
        mStates.reissuingDataValidDays.set("180")//数据补发有效天数
        mStates.reissuingDataInterval.set("30")//数据补发间隔（分钟）
    }

    inner class ClickProxy : BaseClickProxy() {

        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
                return
            }
            mStates.isCenterOpened.set(isChecked)
            if (!isChecked) {
                showMessage("确定要关闭链路吗？", "温馨提示", "确定", {
                    closeDataServer()
                }, "取消", {
                    mStates.isCenterOpened.set(true)
                    (button as SwitchButton).setCheckedImmediatelyNoEvent(true)
                })
            }
        }

        /**
         * 通信方式
         */
        fun onCommunicateWayChooseClick() {
            val selectedIndex = communicateWayList.indexOf(mStates.communicateWay.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择通信方式", communicateWayList,
                    null, selectedIndex,
                    { position, text ->
                        if (position == 0 && !mStates.isDataNetOpened.get()) {
                            showMessage(
                                "请先到网络与通信页面中开启4G后再选择此项！",
                                "提示",
                                "我已知晓"
                            )
                            return@asBottomList
                        } else if (position == 1 && !mStates.isWiredNetOpened.get()) {
                            showMessage(
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
         * 网络协议
         */
        fun onIPTypeChooseClick() {
            val selectedIndex = ipLevelList.indexOf(mStates.ipLeve.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
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
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 数据协议
         */
        fun onDataProtocolChooseClick() {
            val selectedIndex = dataProtocolList.indexOf(mStates.dataProtocol.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择数据协议", dataProtocolList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.dataProtocol.set(text)
                        when (position) {
                            0, 4 -> {//MQTT/MQTTS
                                mStates.isMqttItemVisible.set(true)
                                mStates.isSL651ItemVisible.set(false)
                            }

                            1 -> {//TCP-C
                                mStates.isMqttItemVisible.set(false)
                                mStates.isSL651ItemVisible.set(false)
                            }

                            else -> {//SL651、SZY206
                                mStates.isMqttItemVisible.set(false)
                                mStates.isSL651ItemVisible.set(true)
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
            val selectedIndex = allPlatformList.indexOf(mStates.platformType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择平台类型", allPlatformList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.platformType.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /** 广东水利平台测站类型 */
        fun onGuangdongWaterPlatformStationTypeChooseClick() {
            val selectedIndex =
                guangdongWaterPlatformStationTypeList.indexOf(mStates.guangdongWaterPlatformStationType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择测站类型", guangdongWaterPlatformStationTypeList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.guangdongWaterPlatformStationType.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 测站分类
         */
        fun onStationClassificationChooseClick() {
            val codeList = StationCode.entries.map { it.description }
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

        fun onSubmitClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun closeDataServer() {
        commandItems.clear()
        val entity = MRDataCenterParamEntity(
            centerid = statusItem.centerid.toString(),
            switch = "0"
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_SET_DATA_CENTER,
            entity.toCommandString()
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun initSaveCommand() {
        commandItems.clear()
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

        val entity = MRDataCenterParamEntity(
            centerid = statusItem.centerid.toString(),
            switch = "1",
            addr = mStates.centerServerAddress.get(),
            port = mStates.centerServerPort.get(),
            line = (communicateWayList.indexOf(mStates.communicateWay.get()) + 1).toString(),
            level = (ipLevelList.indexOf(mStates.ipLeve.get()) + 1).toString(),
            type = (transferProtocolList.indexOf(mStates.transferProtocol.get()) + 1).toString(),
            datatype = (dataProtocolList.indexOf(mStates.dataProtocol.get()) + 1).toString(),
            plattype = allPlatformList.indexOf(mStates.platformType.get()).toString(),
            packtype = if (mStates.platformType.get().contains("广东水利")) {
                when (mStates.guangdongWaterPlatformStationType.get()) {
                    "山洪灾害监测站" -> "0"
                    "河道水情监测站" -> "1"
                    "沉降监测站" -> "3"
                    "水质监测站" -> "4"
                    "雨量监测站" -> "5"
                    "流量监测站" -> "6"
                    else -> IOTConstants.NULL_KEY
                }
            } else IOTConstants.NULL_KEY
        )
        if (mStates.dataProtocol.get() == "MQTT" || mStates.dataProtocol.get() == "MQTTS") {//MQTT
            //当产品 ID、设备 ID 为空时，需要填写设备注册码、设备注册地址、设备注册端口号
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

            if (mStates.registerPort.get().isNotEmpty()) {
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
            entity.regcode = mStates.registerCode.get()
            entity.httpaddr = mStates.registerAddress.get()
            entity.httpport = mStates.registerPort.get()

            if (mStates.dataProtocol.get() == "MQTTS")
                entity.taddress = mStates.telemetryStationAddr.get()

        } else if (mStates.dataProtocol.get() == "SL651" || mStates.dataProtocol.get() == "SZY206") {//SL651/SZY206
            entity.type_code =
                if (mStates.dataProtocol.get() == "SL651") StationCode.Companion.valueByDescription(
                    mStates.stationType.get()
                ).code else IOTConstants.NULL_KEY
            entity.co_address =
                if (mStates.dataProtocol.get() == "SL651") mStates.centerStationAddr.get() else IOTConstants.NULL_KEY
            entity.password = mStates.password.get()
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
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()

        val entity = CenterNumberEntity(statusItem.centerid.toString())
        val command = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_DATA_CENTER, entity)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MR_MD_GET_DATA_CENTER -> {
                val result = iotParseManager.parse<MRDataCenterParam>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_DATA_CENTER
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询数据链路参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initDataCenterParam(result.data)
                    }
                }
            }

            IOTCommandType.MR_MD_SET_DATA_CENTER -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("数据保存成功")
                            processNavigateUp()
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initDataCenterParam(data: MRDataCenterParam) {
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
        mStates.dataProtocol.set(
            when (data.datatype) {
                "1", "5" -> {//MQTT, MQTTS
                    mStates.isMqttItemVisible.set(true)
                    mStates.isSL651ItemVisible.set(false)
                    if (data.datatype == "1") dataProtocolList[0] else
                        dataProtocolList[4]
                }

                "2" -> {//TCP-C
                    mStates.isMqttItemVisible.set(false)
                    mStates.isSL651ItemVisible.set(false)
                    dataProtocolList[1]
                }

                "3" -> {//SL651
                    mStates.isMqttItemVisible.set(false)
                    mStates.isSL651ItemVisible.set(true)
                    dataProtocolList[2]
                }

                else -> {//SZY206
                    mStates.isMqttItemVisible.set(false)
                    mStates.isSL651ItemVisible.set(true)
                    dataProtocolList[3]
                }
            }
        )
        data.plattype.toIntOrNull()?.let {
            if (it in allPlatformList.indices) {
                mStates.platformType.set(allPlatformList[it])
            }
        }

        // Handle packtype for Guangdong Water Platform station types
        // The mapping is: 0=山洪灾害监测站, 1=河道水情监测站, 3=沉降监测站, 4=水质监测站, 5=雨量监测站, 6=流量监测站
        when (data.packtype) {
            "0" -> mStates.guangdongWaterPlatformStationType.set("山洪灾害监测站")
            "1" -> mStates.guangdongWaterPlatformStationType.set("河道水情监测站")
            "3" -> mStates.guangdongWaterPlatformStationType.set("沉降监测站")
            "4" -> mStates.guangdongWaterPlatformStationType.set("水质监测站")
            "5" -> mStates.guangdongWaterPlatformStationType.set("雨量监测站")
            "6" -> mStates.guangdongWaterPlatformStationType.set("流量监测站")
        }

        //MQTT 协议参数
        mStates.productId.set(data.projid)
        mStates.deviceId.set(data.deviceid)
        mStates.deviceKey.set(data.devicekey)
        mStates.registerCode.set(data.regcode)
        mStates.registerAddress.set(data.httpaddr)
        mStates.registerPort.set(data.httpport)

        //SL651/SZY206 协议参数
        if (mStates.dataProtocol.get() == dataProtocolList[2]) {
            mStates.stationType.set(StationCode.Companion.valueByCode(data.type_code).description)//测站分类编码
            mStates.centerStationAddr.set(data.co_address)
        }
        mStates.password.set(data.password)
        mStates.telemetryStationAddr.set(data.taddress)//遥测站地址/测站编码
        mStates.hourlyReport.set(data.hour_report == "1")
        mStates.timingReport.set(data.timed_report == "1")
        mStates.addReport.set(data.add_report == "1")
        mStates.maintainReport.set(data.maintain_report == "1")
        mStates.maintainReportInterval.set(data.keepalive)
        mStates.reissuingDataValidDays.set(data.valid_day)
        mStates.reissuingDataInterval.set(data.reissue_time)
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    override fun processNavigateUp(toastMsg: String, isShowToast: Boolean) {
        launchWithViewLifecycle {
            delay(1000)
            //需要给上一级页面传递最新的信息
            setFragmentResult(
                AppContants.Extras.FRAGMENT_DATA_CENTER_HOME_RESULT_REQUEST_KEY,
                bundleOf(MR702DataCenterHomeFragment.REFRESH_DATA to true)
            )
            nav().navigateUp()
        }
    }
}