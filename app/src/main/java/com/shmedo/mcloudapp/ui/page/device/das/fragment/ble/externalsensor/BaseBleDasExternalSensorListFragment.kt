package com.shmedo.mcloudapp.ui.page.device.das.fragment.ble.externalsensor

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasCollectorInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasExternalSensorInfo
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.cmd.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentDasExternalSensorListBinding
import com.shmedo.mcloudapp.extensions.getActivityScopeViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.DASSensorItem
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.model.RVEmptyFooter
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.das.fragment.ble.BleDasSensorHomeFragment
import com.shmedo.mcloudapp.ui.page.device.das.fragment.externalsensor.DasExternalDigitalSensorFragment
import com.shmedo.mcloudapp.ui.page.device.das.fragment.externalsensor.DasExternalVibratingSensorFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.DasExternalSensorListViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/4/18
 * 描述： TODO
 */
abstract class BaseBleDasExternalSensorListFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasExternalSensorListBinding
    lateinit var mStates: DasExternalSensorListViewModel<DasExternalSensorInfo>
    val mdParseManager: MDParserManager by inject()
    lateinit var iotSensorType: IOTSensorType
    private var deleteItemIndex = 0

    override fun initViewModel() {
        super.initViewModel()
        mStates = getActivityScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_das_external_sensor_list,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDasExternalSensorListBinding
        initRefresh()
        initSensorAdapter()
    }

    override fun initData() {
        super.initData()
        arguments?.let {
            val model = it.getString(COLLECTOR_MODEL, "-1")
            //collectorModel 移除前缀 0
            mStates.collectorType.set(
                if (model.startsWith("0") && model.length > 1) model.substring(
                    1
                ) else model
            )
            mStates.isVibratingWireSensor.set(mStates.collectorType.get() == IOTSensorType.VIBRATING_SENSOR.code)
            iotSensorType = IOTSensorType.value(mStates.collectorType.get())
        }
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            resetData()
            queryCollectorInfo()
        }
    }

    private fun initSensorAdapter() {
        binding.rv.setup { rv ->
            rv.addItemDecoration(
                MyGridSpacingItemDecoration(
                    2,
                    ConvertUtils.dp2px(10f),
                    false
                )
            )
            addType<DASSensorItem>(R.layout.item_das_sensor)
            addType<RVEmptyFooter>(R.layout.item_sensor_add_footer)
            R.id.item.onClick {
                if (isBleDisconnected()) {
                    Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                    return@onClick
                }
                if (mStates.collectorType.get().isEmpty()) {
                    showMessageDialog("未获取到采集器信息，请尝试刷新后再试!")
                    return@onClick
                }
                when (itemViewType) {
                    R.layout.item_das_sensor -> {
                        val item = getModel<DASSensorItem>()
                        if (!mStates.isVibratingWireSensor.get()) {
                            val bundle = DasExternalDigitalSensorFragment.newBundleArguments(
                                index = modelPosition,
                                sensorAddr = item.addr,
                                productType,
                                communicateWay,
                                deviceInfo,
                                bleDevice
                            )
                            nav().navigate(
                                R.id.action_bleDasSensorHomeFragment_to_bleDasExternalDigitalSensorFragment,
                                bundle
                            )
                        } else {
                            val bundle = DasExternalVibratingSensorFragment.newBundleArguments(
                                sensorChannel = item.addr,
                                productType,
                                communicateWay,
                                deviceInfo,
                                bleDevice,
                            )
                            nav().navigate(
                                R.id.action_bleDasSensorHomeFragment_to_bleDasExternalVibratingSensorFragment,
                                bundle
                            )
                        }
                    }

                    else -> {//添加传感器
                        if (!mStates.isVibratingWireSensor.get()) {
                            val bundle = DasExternalDigitalSensorFragment.newBundleArguments(
                                index = -1,
                                sensorAddr = "-1",
                                productType,
                                communicateWay,
                                deviceInfo,
                                bleDevice,
                            )
                            nav().navigate(
                                R.id.action_bleDasSensorHomeFragment_to_bleDasExternalDigitalSensorFragment,
                                bundle
                            )
                        } else {
                            val bundle = DasExternalVibratingSensorFragment.newBundleArguments(
                                sensorChannel = "-1",
                                productType,
                                communicateWay,
                                deviceInfo,
                                bleDevice,
                            )
                            nav().navigate(
                                R.id.action_bleDasSensorHomeFragment_to_bleDasExternalVibratingSensorFragment,
                                bundle
                            )
                        }
                    }
                }
            }
            R.id.item_del.onClick {
                if (isBleDisconnected()) {
                    Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                    return@onClick
                }
                //最少保留一个传感器
                if (binding.rv.models!!.size <= 1) {
                    Toaster.show("至少保留一个传感器")
                    return@onClick
                }
                showMessage("确定删除此传感器吗？", "提示", "删除", {
                    deleteItemIndex = modelPosition
                    binding.rv.bindingAdapter.mutable[deleteItemIndex].let {
                        if (it is DASSensorItem) {
                            mStates.sensorModelMap.remove(it.addr)
                        }
                    }
                    binding.rv.bindingAdapter.mutable.removeAt(deleteItemIndex)
                    binding.rv.bindingAdapter.notifyItemRemoved(deleteItemIndex)
                    updateFooter()
                }, "取消")
            }
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onSubmitButtonClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    abstract fun initSaveCommand()

    override fun createObserver() {
        super.createObserver()
        mStates.isRefreshSensorList.observe(viewLifecycleOwner) { flag ->
            if (flag) {
                binding.rv.mutable.clear()
                mStates.sensorModelMap.keys.sortedBy { addr -> addr.toInt() }.forEach { key ->
                    val sensorInfo = mStates.sensorModelMap[key]!!
                    val item = DASSensorItem(
                        isPlugin = true,
                        addr = sensorInfo.addr,
                        addrDesc = if (mStates.isVibratingWireSensor.get()) "通道-${sensorInfo.addr.toInt() + 1}" else "地址-${sensorInfo.addr}",
                        sensorType = sensorInfo.type,
                        sensorName = IOTSensorType.value(sensorInfo.type).description,
                    )
                    binding.rv.mutable.add(item)
                }
                binding.rv.bindingAdapter.notifyDataSetChanged()
                updateFooter()
            }
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 查询采集器配置信息
     */
    private fun queryCollectorInfo() {
        commandItems.clear()
        val command = MDCommandUtil.getCommand(
            MDCommandType.COLLECTOR_CONFIG,
            MDCommandUtil.formatStringTwo(mStates.collectorType.get())
        )
        commandItems.add(command)
        Timber.d("查询采集器配置信息===%s", command)
        sendCommandFromCmdList(
            isStartTimeoutJob = true,
            timeoutMillis = com.shmedo.core.commonlib.utils.AppContants.Communication.DELAY_15000_MILLIS
        )
    }

    /**
     * 查询采集器接入的传感器配置信息
     */
    private fun queryExtendSensorConfigInfo(sensorNum: Int) {
        commandItems.clear()
        for (index in 0 until sensorNum) {
            val model = MDCommandUtil.formatStringTwo(mStates.collectorType.get())
            val address = MDCommandUtil.formatStringTwo(index.toString())
            val command =
                MDCommandUtil.getCommand(
                    MDCommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER,
                    "$model$address"
                )
            commandItems.add(command)
            Timber.d(
                "获取 %s 采集器 %s 通道的传感器参数===%s",
                IOTSensorType.value(mStates.collectorType.get()),
                address,
                command
            )
        }

        sendCommandFromCmdList()
    }

    override fun setResultData(cmdStr: String) {
        when (MDCommandUtil.extractCommandType(cmdStr)) {
            MDCommandType.COLLECTOR_CONFIG -> {
                val result = mdParseManager.parse<DasCollectorInfo>(
                    cmdStr,
                    MDCommandType.COLLECTOR_CONFIG
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "查询采集器参数出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        initCollectorInfo(result.data)
                    }
                }
            }

            MDCommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER -> {//获取XX采集器YY通道的传感器参数 ##101
                val result = mdParseManager.parse<DasExternalSensorInfo>(
                    cmdStr,
                    MDCommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        initEmptySensor()
                        val errMsg = "查询传感器参数出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        //处理此通道的传感器配置参数
                        processSensorParamsInfo(result.data)
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                            updateFooter()
                            mStates.isSubmitBtnVisible.set(mStates.sensorModelMap.isNotEmpty())
                        }
                    }
                }
            }

            MDCommandType.SET_COLLECTOR_SENSOR -> {//
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "接入传感器设置出错"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {}
                    }
                }
            }

            MDCommandType.SAVE_CONFIG_INFO -> {//
                when (val result = mdParseManager.parse<String>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        val errMsg = "保存出错!"
                        handleFailureResult(errMsg)
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

    /**
     * 初始化采集器信息
     */
    private fun initCollectorInfo(collectorInfo: DasCollectorInfo) {
        try {
            //采集器地址为 0 时，表示采集器未启用，不允许配置传感器，退出页面
            if (collectorInfo.addr == "0") {
                cancelNearbyCommunicationTimeoutJob()
                showMessageDialog("采集器地址为0,无法配置扩展传感器,请先修改采集器地址!")
                return
            }
            if (collectorInfo.sensornum.isEmpty() || collectorInfo.sensornum.toInt() == 0) {
                cancelNearbyCommunicationTimeoutJob()
                return
            }
            queryExtendSensorConfigInfo(collectorInfo.sensornum.toInt())
        } catch (e: Exception) {
            cancelNearbyCommunicationTimeoutJob()
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     *  处理获取到的单个传感器参数信息
     */
    private fun processSensorParamsInfo(sensorInfo: DasExternalSensorInfo) {
        if (mStates.sensorModelMap.containsKey(sensorInfo.addr)) {
            mStates.sensorModelMap[sensorInfo.addr] = sensorInfo
            return
        }
        mStates.sensorModelMap[sensorInfo.addr] = sensorInfo
        val item = DASSensorItem(
            isPlugin = true,
            addr = sensorInfo.addr,
            addrDesc = if (mStates.isVibratingWireSensor.get()) "通道-${sensorInfo.addr.toInt() + 1}" else "地址-${sensorInfo.addr}",
            sensorType = sensorInfo.type,
            sensorName = IOTSensorType.value(sensorInfo.type).description,
        )
        if (binding.rv.models.isNullOrEmpty())
            binding.rv.models = arrayListOf()
        binding.rv.mutable.add(item)
        binding.rv.bindingAdapter.notifyItemInserted(binding.rv.bindingAdapter.modelCount)
    }

    private fun initEmptySensor() {
        binding.rv.models = arrayListOf<DASSensorItem>()
        updateFooter()
    }

    private fun updateFooter() {
        if (binding.rv.bindingAdapter.modelCount < MAX_SENSOR_COUNT) {
            if (binding.rv.bindingAdapter.footerCount == 0)
                binding.rv.bindingAdapter.addFooter(RVEmptyFooter())
        } else {
            binding.rv.bindingAdapter.removeFooterAt()
        }
    }

    private fun resetData() {
        deleteItemIndex = 0
        mStates.sensorModelMap.clear()
        binding.rv.bindingAdapter.clearFooter()
        binding.rv.models = arrayListOf<DASSensorItem>()
        mStates.isSubmitBtnVisible.set(false)
    }

    companion object {
        const val MAX_SENSOR_COUNT = 16
        const val COLLECTOR_MODEL = "collector_model"
        fun newBundleArguments(
            collectorModel: String,
            type: ProductType = ProductType.UnKnown,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putString(BleDasSensorHomeFragment.COLLECTOR_MODEL, collectorModel)
            putParcelable(com.shmedo.core.commonlib.utils.AppContants.Extras.PRODUCT_TYPE, type)
            putParcelable(com.shmedo.core.commonlib.utils.AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(com.shmedo.core.commonlib.utils.AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(com.shmedo.core.commonlib.utils.AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(com.shmedo.core.commonlib.utils.AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}