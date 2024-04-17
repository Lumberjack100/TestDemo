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
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.das.DasCollectorEntity
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
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
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
import com.shmedo.mcloudapp.device.ui.das.fragment.externalsensor.DasExternalDigitalSensorFragment
import com.shmedo.mcloudapp.device.ui.das.fragment.externalsensor.DasExternalVibratingSensorFragment
import com.shmedo.mcloudapp.device.viewmodel.state.DasExternalSensorListViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/4/17
 * 描述： TODO
 */
class BleDasExternalSensorListFragment : BaseMDDeviceFragment() {
    private lateinit var binding: FragmentDasExternalSensorListBinding
    private lateinit var mStates: DasExternalSensorListViewModel<MDDasExternalSensorInfo>
    private val mdParseManager: MDParserManager by inject()
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
            val collectorModel = it.getString(COLLECTOR_MODEL, "-1")
            //collectorModel 移除前缀 0
            mStates.collectorType.set(
                if (collectorModel.startsWith("0") && collectorModel.length > 1) collectorModel.substring(
                    1
                ) else collectorModel
            )
            mStates.isVibratingWireSensor.set(mStates.collectorType.get() == IOTSensorType.VW08.code)
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
                            val bundle = DasExternalDigitalSensorFragment.newBundleArguments(
                                communicateWay,
                                deviceInfo,
                                bleDevice,
                                modelPosition,
                                item.addr
                            )
                            nav().navigate(
                                R.id.action_dasSensorHomeFragment_to_dasExternalDigitalSensorFragment,
                                bundle
                            )
                        } else {
                            val bundle = DasExternalVibratingSensorFragment.newBundleArguments(
                                communicateWay,
                                deviceInfo,
                                bleDevice,
                                item.addr
                            )
                            nav().navigate(
                                R.id.action_dasSensorHomeFragment_to_dasExternalVibratingSensorFragment,
                                bundle
                            )
                        }
                    }

                    else -> {
                        if (!mStates.isVibratingWireSensor.get()) {
                            val bundle = DasExternalDigitalSensorFragment.newBundleArguments(
                                communicateWay,
                                deviceInfo,
                                bleDevice,
                                -1,
                                "-1"
                            )
                            nav().navigate(
                                R.id.action_dasSensorHomeFragment_to_dasExternalDigitalSensorFragment,
                                bundle
                            )
                        } else {
                            val bundle = DasExternalVibratingSensorFragment.newBundleArguments(
                                communicateWay,
                                deviceInfo,
                                bleDevice,
                                "-1"
                            )
                            nav().navigate(
                                R.id.action_dasSensorHomeFragment_to_dasExternalVibratingSensorFragment,
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


    private fun initSaveCommand() {
        commandItems.clear()

        //设置采集器参数
        val entity = DasCollectorEntity(
            type = mStates.collectorType.get(),
            sensornum = mStates.sensorModelMap.size.toString(),
        )
        val command = MDCommandUtil.getCommand(
            MDCommandType.SET_COLLECTOR_SENSOR,
            entity.toCommandString()
        )
        commandItems.add(command)

        //设置采集器接入的传感器配置信息
        initExtendSensorConfigInfoCommand()

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendMDCommandFromCmdList(
            isStartTimeoutJob = true,
            timeoutMillis = AppContants.Communication.DELAY_15000_MILLIS
        )
    }

    /**
     * 设置采集器接入的传感器配置信息
     */
    private fun initExtendSensorConfigInfoCommand() {
        mStates.sensorModelMap.keys.sortedBy { addr -> addr.toInt() }
            .forEachIndexed { mIndex, key ->
                val sensorInfo = mStates.sensorModelMap[key]!!
//                val entity = DasExternalSensorEntity().apply {
//                    index = mIndex.toString()
//                    type = sensorInfo.type
//                    addr = sensorInfo.addr
//                    threshold = sensorInfo.threshold
//                    corrval = sensorInfo.corrval
//                    when (IOTSensorType.value(sensorInfo.type)) {
//                        IOTSensorType.KANG_PERCOLATE -> {//基康渗压计
//                            tubealti = sensorInfo.tubealti
//                            ropelen = sensorInfo.ropelen
//                            poly_a = sensorInfo.poly_a
//                            poly_b = sensorInfo.poly_b
//                            poly_c = sensorInfo.poly_c
//                            temp_k = sensorInfo.temp_k
//                            temp_t0 = sensorInfo.temp_t0
//                        }
//
//                        IOTSensorType.GUDAN_PERCOLATE -> {//葛南渗压计
//                            tubealti = sensorInfo.tubealti
//                            ropelen = sensorInfo.ropelen
//                            sens_k = sensorInfo.sens_k
//                            temp_b = sensorInfo.temp_b
//                            temp_t0 = sensorInfo.temp_t0
//                            referval_f = sensorInfo.referval_f
//                        }
//
//                        IOTSensorType.GUDAN_SOIL_PRESSURE -> {//葛南土压力计
//                            sens_k = sensorInfo.sens_k
//                            temp_b = sensorInfo.temp_b
//                            temp_t0 = sensorInfo.temp_t0
//                            referval_f = sensorInfo.referval_f
//                        }
//
//                        IOTSensorType.GUDAN_STRESS -> {//葛南应力计
//                            sens_k = sensorInfo.sens_k
//                            temp_b = sensorInfo.temp_b
//                            temp_t0 = sensorInfo.temp_t0
//                            referval_f = sensorInfo.referval_f
//                            elastic_mod = sensorInfo.elastic_mod
//                        }
//
//                        IOTSensorType.JUNXING_ZLJ_300T -> {//轴力计
//                            sens_k = sensorInfo.sens_k
//                            temp_b = sensorInfo.temp_b
//                            temp_t0 = sensorInfo.temp_t0
//                            referval_f = sensorInfo.referval_f
//                        }
//
//                        IOTSensorType.INCLINOMETER -> {//固定测斜仪
//                            spacing = sensorInfo.spacing
//                            model_type = sensorInfo.model_type
//                        }
//
//                        IOTSensorType.LUYAN_INCLINOMETER -> {//倾角仪
//                            initvalx = sensorInfo.initvalx
//                            initvaly = sensorInfo.initvaly
//                            initvalz = sensorInfo.initvalz
//                        }
//
//                        IOTSensorType.WEIR -> {//量水堰计
//                            lsycsds = sensorInfo.lsycsds
//                            lsyysst = sensorInfo.lsyysst
//                        }
//
//                        IOTSensorType.STATIC_LEVEL,//静力水准
//                        IOTSensorType.SEDIMENTATION_METER -> {//沉降仪
//                            initval = sensorInfo.initval
//                        }
//
//                        IOTSensorType.VW08 -> {//MCU 振弦传感器
//                            sens_k = sensorInfo.sens_k
//                            temp_b = sensorInfo.temp_b
//                            temp_t0 = sensorInfo.temp_t0
//                            referval_f = sensorInfo.referval_f
//                        }
//
//                        IOTSensorType.DIGITAL_WATER_LEVEL_GAUGE,//数字式水位计
//                        IOTSensorType.WATER_LEVEL_GAUGE -> {//MCU 水位(液位)计
//                            tubealti = sensorInfo.tubealti
//                            ropelen = sensorInfo.ropelen
//                        }
//
//                        IOTSensorType.RADAR_LEVEL_GAUGE -> {//雷达液(物)位计 设置子雷达传感器型号
//                            child_type = sensorInfo.child_type
//                        }
//
//                        else -> {}
//                    }
//                }
//                val command = MDCommandUtil.getCommand(
//                    MDCommandType.DAS_MD_SET_EXTERNAL_SENSOR,
//                    entity.toCommandString()
//                )
//                commandItems.add(command)
            }
    }

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
                        val errMsg = "设置采集器参数出错: ${result.message}"
                        Timber.e(errMsg)
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
        fun newInstance() = BleDasExternalSensorListFragment()
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