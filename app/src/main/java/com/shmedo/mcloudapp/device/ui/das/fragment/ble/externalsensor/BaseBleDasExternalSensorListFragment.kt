package com.shmedo.mcloudapp.device.ui.das.fragment.ble.externalsensor

import android.os.Bundle
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.ext.getActivityScopeViewModel
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.model.das.DasCollectorInfo
import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.model.das.MDDasExternalSensorInfo
import com.shmedo.lib.device.base.md_cmd.parser.MDCommandResult
import com.shmedo.lib.device.base.md_cmd.parser.MDParserManager
import com.shmedo.lib.device.base.md_cmd.utils.MDCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.showMessage
import com.shmedo.mcloudapp.common.ext.showMessageDialog
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentDasExternalSensorListBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.DASSensorItem
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.model.RVEmptyFooter
import com.shmedo.mcloudapp.device.ui.das.fragment.ble.BaseMDDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.DasExternalSensorListViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/4/18
 * 描述： TODO
 */
abstract class BaseBleDasExternalSensorListFragment : BaseMDDeviceFragment() {
    private lateinit var binding: FragmentDasExternalSensorListBinding
    lateinit var mStates: DasExternalSensorListViewModel<MDDasExternalSensorInfo>
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
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
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
                if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
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
                            val bundle = BleDasExternalDigitalSensorFragment.newBundleArguments(
                                communicateWay,
                                deviceInfo,
                                bleDevice,
                                modelPosition,
                                item.addr
                            )
                            nav().navigate(
                                R.id.action_bleDasSensorHomeFragment_to_bleDasExternalDigitalSensorFragment,
                                bundle
                            )
                        } else {
                            val bundle = BleDasExternalVibratingSensorFragment.newBundleArguments(
                                communicateWay,
                                deviceInfo,
                                bleDevice,
                                item.addr
                            )
                            nav().navigate(
                                R.id.action_bleDasSensorHomeFragment_to_bleDasExternalVibratingSensorFragment,
                                bundle
                            )
                        }
                    }

                    else -> {//添加传感器
                        if (!mStates.isVibratingWireSensor.get()) {
                            val bundle = BleDasExternalDigitalSensorFragment.newBundleArguments(
                                communicateWay,
                                deviceInfo,
                                bleDevice,
                                -1,
                                "-1"
                            )
                            nav().navigate(
                                R.id.action_bleDasSensorHomeFragment_to_bleDasExternalDigitalSensorFragment,
                                bundle
                            )
                        } else {
                            val bundle = BleDasExternalVibratingSensorFragment.newBundleArguments(
                                communicateWay,
                                deviceInfo,
                                bleDevice,
                                "-1"
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
                if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
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
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
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
                        addr = sensorInfo.sensorAddress,
                        addrDesc = if (mStates.isVibratingWireSensor.get()) "通道-${sensorInfo.sensorAddress.toInt() + 1}" else "地址-${sensorInfo.sensorAddress}",
                        sensorType = sensorInfo.sensorType,
                        sensorName = IOTSensorType.value(sensorInfo.sensorType).description,
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
        sendMDCommandFromCmdList(
            isStartTimeoutJob = true,
            timeoutMillis = AppContants.Communication.DELAY_15000_MILLIS
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

        sendMDCommandFromCmdList()
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
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询采集器参数出错"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        initCollectorInfo(result.data)
                    }
                }
            }

            MDCommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER -> {//获取XX采集器YY通道的传感器参数 ##101
                val result = mdParseManager.parse<MDDasExternalSensorInfo>(
                    cmdStr,
                    MDCommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER
                )
                when (result) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        initEmptySensor()
                        val errMsg = "查询传感器参数出错"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    is MDCommandResult.Success -> {
                        //处理此通道的传感器配置参数
                        processSensorParamsInfo(result.data)
                        sendMDCommandFromCmdList {
                            binding.refreshLayout.finish()
                            updateFooter()
                            mStates.isSubmitBtnVisible.set(mStates.sensorModelMap.isNotEmpty())
                        }
                    }
                }
            }

            MDCommandType.SET_COLLECTOR_SENSOR -> {//
                when (val result = mdParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "接入传感器设置出错"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendMDCommandFromCmdList {}
                    }
                }
            }

            MDCommandType.SAVE_CONFIG_INFO -> {//
                when (val result = mdParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is MDCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "保存出错!"
                        Timber.e("$errMsg: ${result.message}")
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendMDCommandFromCmdList {
                            Toaster.show("保存成功")
                        }
                    }
                }
            }


            else -> {}
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
            e.printStackTrace()
        }
    }

    /**
     *  处理获取到的单个传感器参数信息
     */
    private fun processSensorParamsInfo(sensorInfo: MDDasExternalSensorInfo) {
        if (mStates.sensorModelMap.containsKey(sensorInfo.sensorAddress)) {
            mStates.sensorModelMap[sensorInfo.sensorAddress] = sensorInfo
            return
        }
        mStates.sensorModelMap[sensorInfo.sensorAddress] = sensorInfo
        val item = DASSensorItem(
            isPlugin = true,
            addr = sensorInfo.sensorAddress,
            addrDesc = if (mStates.isVibratingWireSensor.get()) "通道-${sensorInfo.sensorAddress.toInt() + 1}" else "地址-${sensorInfo.sensorAddress}",
            sensorType = sensorInfo.sensorType,
            sensorName = IOTSensorType.value(sensorInfo.sensorType).description,
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
        private const val COLLECTOR_MODEL = "collector_model"
        fun newBundleArguments(
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            collectorModel: String,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putString(COLLECTOR_MODEL, collectorModel)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}