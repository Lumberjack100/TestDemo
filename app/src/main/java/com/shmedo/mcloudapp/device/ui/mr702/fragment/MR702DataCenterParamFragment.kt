package com.shmedo.mcloudapp.device.ui.mr702.fragment

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
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.common.CenterNumberEntity
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr.MRDataCenterParamEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.lib.device.base.iot_cmd.enums.StationCode
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRDataCenterParam
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentMr702DataCenterParamBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.DataCenterStatusItem
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.MR702DataCenterParamViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessage
import com.shmedo.mcloudapp.ext.showMessageDialog
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import timber.log.Timber

class MR702DataCenterParamFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702DataCenterParamBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: MR702DataCenterParamViewModel
    private val iotParseManager: IOTParserManager by inject()

    private lateinit var statusItem: DataCenterStatusItem

    private val communicateWayList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_communicate_way) }
    private val ipLevelList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_ip_level) }
    private val transferProtocolList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_ip_protocol) }
    private val dataProtocolList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_data_protocol) }
    private val platformList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_register_platform) }


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_data_center_param, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702DataCenterParamBinding
        binding.llToolbar.toolbar.title = "数据中心"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            processBack(true)
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                processBack(true)
            }
        })
        toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_device_param_edit)
        toolbarViewModel.toolbarTvActionText.set("取消")
        toolbarViewModel.toolbarIvActionVisible.set(true)
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
        mStates.centerName.set(statusItem.name)
        mStates.centerStatus.set(statusItem.status)
        mStates.isCenterOpened.set(statusItem.status != "0")
        binding.llDataCenterParamHead.statusSB.setCheckedImmediatelyNoEvent(statusItem.status != "0")
    }

    private fun setEditable(editable: Boolean) {
        toolbarViewModel.toolbarIvActionVisible.set(!editable)
        toolbarViewModel.toolbarTvActionVisible.set(editable)
        mStates.isEditable.set(editable)
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onToolbarIvClick() {
            setEditable(true)
        }

        override fun onToolbarTvClick() {
            setEditable(false)
        }

        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (isBleDisconnected()) {
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
            val selectedIndex = ipLevelList.indexOf(mStates.ipType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择网络协议", ipLevelList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.ipType.set(text)
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
                            0 -> {//MQTT
                                mStates.isMqttItemVisible.set(true)
                                mStates.isSL651ItemVisible.set(false)
                            }

                            1 -> {//TCP-C
                                mStates.isMqttItemVisible.set(false)
                                mStates.isSL651ItemVisible.set(false)
                            }

                            else -> {//SL651
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
            IOTCommandType.MD_MR_SET_DATA_CENTER,
            entity.toCommandString()
        )
        commandItems.add(command)
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

        val entity = MRDataCenterParamEntity(
            centerid = statusItem.centerid.toString(),
            switch = "1",
            addr = mStates.centerServerAddress.get(),
            port = mStates.centerServerPort.get(),
            line = if (mStates.communicateWay.get() == communicateWayList[0]) "1" else "2",
            level = if (mStates.ipType.get() == ipLevelList[0]) "1" else "2",
            type = if (mStates.transferProtocol.get() == transferProtocolList[0]) "1" else "2",
            datatype = when (mStates.dataProtocol.get()) {
                dataProtocolList[0] -> "1"
                dataProtocolList[1] -> "2"
                else -> "3"
            },
            plattype = when (mStates.platformType.get()) {
                platformList[0] -> "0"
                platformList[1] -> "1"
                platformList[2] -> "2"
                platformList[3] -> "3"
                platformList[4] -> "4"
                else -> "5"
            }
        )
        if (mStates.dataProtocol.get() == dataProtocolList[0]) {//MQTT
            //当设备 ID、产品 ID 为空时，需要填写设备注册码、设备注册地址、设备注册端口号
            if (mStates.deviceId.get().isEmpty() && mStates.deviceKey.get().isEmpty()) {
                if (mStates.registerCode.get().isEmpty()) {
                    showMessageDialog("请输入设备注册码!")
                    return
                }
                if (mStates.registerAddress.get().isEmpty()) {
                    showMessageDialog("请输入设备注册地址!")
                    return
                }
                if (mStates.centerServerPort.get().isEmpty()) {
                    showMessageDialog("请输入设备注册端口号!")
                    return
                }
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
            entity.projid = mStates.productId.get()
            entity.deviceid = mStates.deviceId.get()
            entity.devicekey = mStates.deviceKey.get()
            entity.regcode = mStates.registerCode.get()
            entity.httpaddr = mStates.registerAddress.get()
            entity.httpport = mStates.registerPort.get()
        } else if (mStates.dataProtocol.get() == dataProtocolList[2]) {//SL651
            entity.type_code = StationCode.valueByDescription(mStates.stationType.get()).code
            entity.co_address = mStates.centerStationAddr.get()
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
            IOTCommandType.MD_MR_SET_DATA_CENTER,
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
        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_DATA_CENTER, entity)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_DATA_CENTER -> {
                val result = iotParseManager.parse<MRDataCenterParam>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_DATA_CENTER
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询数据中心参数出错: ${result.message}"
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

            IOTCommandType.MD_MR_SET_DATA_CENTER -> {
                setEditable(false)
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("保存成功")
                            processBack()
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
        mStates.communicateWay.set(if (data.line == "1") communicateWayList[0] else communicateWayList[1])
        mStates.ipType.set(if (data.level == "1") ipLevelList[0] else ipLevelList[1])

        mStates.transferProtocol.set(if (data.type == "1") transferProtocolList[0] else transferProtocolList[1])
        mStates.dataProtocol.set(
            when (data.datatype) {
                "1" -> {//MQTT
                    mStates.isMqttItemVisible.set(true)
                    mStates.isSL651ItemVisible.set(false)
                    dataProtocolList[0]
                }

                "2" -> {//TCP-C
                    mStates.isMqttItemVisible.set(false)
                    mStates.isSL651ItemVisible.set(false)
                    dataProtocolList[1]
                }

                else -> {//SL651
                    mStates.isMqttItemVisible.set(false)
                    mStates.isSL651ItemVisible.set(true)
                    dataProtocolList[2]
                }
            }
        )
        mStates.platformType.set(
            when (data.plattype) {
                "0" -> platformList[0]
                "1" -> platformList[1]
                "2" -> platformList[2]
                "3" -> platformList[3]
                "4" -> platformList[4]
                else -> platformList[5]
            }
        )
        //MQTT 协议参数
        mStates.productId.set(data.projid)
        mStates.deviceId.set(data.deviceid)
        mStates.deviceKey.set(data.devicekey)
        mStates.registerCode.set(data.regcode)
        mStates.registerAddress.set(data.httpaddr)
        mStates.registerPort.set(data.httpport)

        //SL651 水文协议参数
        mStates.stationType.set(StationCode.valueByCode(data.type_code).description)
        mStates.centerStationAddr.set(data.co_address)
        mStates.password.set(data.password)
        mStates.telemetryStationAddr.set(data.taddress)
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
                MR702DataCenterHomeFragment.FRAGMENT_RESULT_REQUEST_KEY,
                bundleOf(MR702DataCenterHomeFragment.REFRESH_DATA to true)
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