package com.shmedo.mcloudapp.ui.page.device.common

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
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
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.CenterNumberEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.DataCenterParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.enums.StationCode
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
import com.shmedo.mcloudapp.databinding.FragmentDataCenterParamBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.DataCenterStatusItem
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.DataCenterParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import timber.log.Timber

class UniversalDataCenterParamFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDataCenterParamBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: DataCenterParamViewModel
    private val iotParseManager: IOTParserManager by inject()

    private lateinit var statusItem: DataCenterStatusItem

    private val dataTypeList =
        arrayListOf("CMD", "NMEA", "DIFF_IN", "DIFF_OUT", "RAW_OUT", "RES_OUT")
    private val dataProtocolList = arrayListOf("TCP-C", "TCP-S", "MQTT", "SL651")
    private val platformList by lazy { Utils.getApp().resources.getStringArray(R.array.data_center_register_platform) }


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_data_center_param, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDataCenterParamBinding
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            processBack(true)
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                processBack(true)
            }
        })
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
        mStates.isEditable.set(true)
        resetDefaultParams()
    }

    /**
     * 初始化默认参数
     */
    private fun resetDefaultParams() {
        toolbarViewModel.toolbarTitleText.set(statusItem.name.replace("数据", "") + "配置")
        mStates.isCenterOpened.set(statusItem.status != "0")
        mStates.isDataTypeVisible.set(
            productType == ProductType.GNSS_E_1 || productType == ProductType.GNSS_E_2 || productType == ProductType.GNSS_E_3
        )

        mStates.dataType.set(dataTypeList[5])
        mStates.dataProtocol.set(dataProtocolList[0])//默认选择TCP-C
        mStates.platformType.set(platformList[2])//默认选择米度物联平台

        mStates.stationType.set(StationCode.RESERVOIR.description)//默认选择水库(湖泊)
        mStates.maintainReport.set(true)
        mStates.maintainReportInterval.set("30")//维持上报间隔（秒）
        mStates.reissuingDataValidDays.set("180")//数据补发有效天数
        mStates.reissuingDataInterval.set("30")//数据补发间隔(分钟)
    }


    inner class ClickProxy : BaseClickProxy() {
        /**
         * 数据类型
         */
        fun onDataTypeChooseClick() {
            val selectedIndex = dataTypeList.indexOf(mStates.dataType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择数据类型", dataTypeList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.dataType.set(text)
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
                    "请选择数据协议", dataProtocolList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.dataProtocol.set(text)
                        when (text) {
                            "TCP-C" -> {//TCP-C
                                mStates.isMqttItemVisible.set(false)
                                mStates.isSL651ItemVisible.set(false)
                            }

                            "TCP-S" -> {//TCP-S
                                mStates.isMqttItemVisible.set(false)
                                mStates.isSL651ItemVisible.set(false)
                            }

                            "MQTT" -> {//MQTT
                                mStates.isMqttItemVisible.set(true)
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
                        mStates.isRigisterVisible.set(!text.contains("重庆地灾"))
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

        /**
         * 恢复默认配置
         */
        fun onResetClick() {
            resetDefaultParams()
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
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

    private fun closeDataServer() {
        commandItems.clear()
        val entity = DataCenterParamEntity(
            centerid = statusItem.centerid.toString(),
            addr = "",
            port = "",
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_DATA_CENTER,
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
        val entity = DataCenterParamEntity(
            centerid = statusItem.centerid.toString(),
            addr = mStates.centerServerAddress.get(),
            port = mStates.centerServerPort.get(),
            datatype = if (productType == ProductType.GNSS_E_1 || productType == ProductType.GNSS_E_2 || productType == ProductType.GNSS_E_3)
                (dataTypeList.indexOf(mStates.dataType.get()) + 1).toString()
            else IOTConstants.NULL_KEY,
            protocol = mStates.dataProtocol.get(),
            plattype = platformList.indexOf(mStates.platformType.get()).toString()
        )
        if (mStates.dataProtocol.get() == "MQTT") {//MQTT
            //当设备 ID、产品 ID 为空时，需要填写设备注册码、设备注册地址、设备注册端口号
            if (mStates.isRigisterVisible.get() && mStates.deviceId.get()
                    .isEmpty() && mStates.deviceKey.get().isEmpty()
            ) {
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
            if (mStates.isRigisterVisible.get() && mStates.registerPort.get().isNotEmpty()) {
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
            entity.regcode = if (mStates.isRigisterVisible.get()) mStates.registerCode.get() else ""
            entity.httpaddr =
                if (mStates.isRigisterVisible.get()) mStates.registerAddress.get() else ""
            entity.httpport =
                if (mStates.isRigisterVisible.get()) mStates.registerPort.get() else ""
        } else if (mStates.dataProtocol.get() == "SL651") {//SL651
            entity.type_code = StationCode.valueByDescription(mStates.stationType.get()).code
            entity.co_address = mStates.centerStationAddr.get()
            entity.password = mStates.password.get()
            entity.taddress = mStates.telemetryStationAddr.get()
            entity.hour_report = if (mStates.hourlyReport.get()) "1" else "0"
            entity.data_link = mStates.maintainReportInterval.get()
            entity.valid_day = mStates.reissuingDataValidDays.get()
            entity.reissue_time = mStates.reissuingDataInterval.get()
        }
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_DATA_CENTER,
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
        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DATA_CENTER, entity)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DATA_CENTER -> {
                val result = iotParseManager.parse<DataCenterInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_DATA_CENTER
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询链路参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        try {
                            initDataCenterParam(result.data)
                        } catch (e: Exception) {
                            Timber.e(e)
                            addLogItem(Log.ERROR, e.errorMsg)
                        }
                    }
                }
            }

            IOTCommandType.MD_SET_DATA_CENTER -> {
//                setEditable(false)
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置链路参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("数据保存成功")
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

    private fun initDataCenterParam(data: DataCenterInfo) {
        mStates.centerServerAddress.set(data.addr)
        mStates.centerServerPort.set(data.port)

        data.datatype.toIntOrNull()?.let {
            if (it in 1..dataTypeList.size) {
                mStates.dataType.set(dataTypeList[it - 1])
            }
        }
        mStates.dataProtocol.set(data.protocol)
        when (data.protocol) {
            "MQTT" -> {//MQTT
                mStates.isMqttItemVisible.set(true)
                mStates.isSL651ItemVisible.set(false)
            }

            "TCP-C" -> {//TCP-C
                mStates.isMqttItemVisible.set(false)
                mStates.isSL651ItemVisible.set(false)
            }

            "TCP-S" -> {//TCP-S
                mStates.isMqttItemVisible.set(false)
                mStates.isSL651ItemVisible.set(false)
            }

            else -> {//SL651
                mStates.isMqttItemVisible.set(false)
                mStates.isSL651ItemVisible.set(true)
            }
        }
        data.plattype.toIntOrNull()?.let {
            if (it in platformList.indices) {
                mStates.platformType.set(platformList[it])
            }
        }

        //MQTT 协议参数
        mStates.productId.set(data.projid)
        mStates.deviceId.set(data.deviceid)
        mStates.deviceKey.set(data.devicekey)
        mStates.registerCode.set(data.regcode)
        mStates.registerAddress.set(data.httpaddr)
        mStates.registerPort.set(data.httpport)
        //重庆地灾平台不显示注册码、注册地址、注册端口号
        mStates.isRigisterVisible.set(!mStates.platformType.get().contains("重庆地灾"))


        //SL651 水文协议参数
        mStates.stationType.set(StationCode.valueByCode(data.type_code).description)
        mStates.centerStationAddr.set(data.co_address)
        mStates.password.set(data.password)
        mStates.telemetryStationAddr.set(data.taddress)
        mStates.hourlyReport.set(data.hour_report == "1")
        mStates.maintainReportInterval.set(data.data_link)
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