package com.shmedo.mcloudapp.device.ui.das.fragment.externalsensor

import android.os.Bundle
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getActivityScopeViewModel
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.das.DasCollectorEntity
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.das.DasExternalSensorEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.model.das.DasCollectorInfo
import com.shmedo.lib.device.base.iot_cmd.model.das.DasExternalSensorInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentDasExternalSensorListBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.DASSensorItem
import com.shmedo.mcloudapp.device.model.RVEmptyFooter
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.DasExternalSensorListViewModel
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessage
import com.shmedo.mcloudapp.ext.showMessageDialog
import org.koin.android.ext.android.inject
import timber.log.Timber

class DasExternalSensorListFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasExternalSensorListBinding
    private lateinit var mStates: DasExternalSensorListViewModel<DasExternalSensorInfo>
    private val iotParseManager: IOTParserManager by inject()

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
                //最少保留一个传感器
                if (binding.rv.models!!.size <= 1) {
                    Toaster.show("至少保留一个传感器")
                    return@onClick
                }
                showMessage("确定删除此传感器吗？", "提示", "删除", {
                    deleteItemIndex = modelPosition
                    deleteSensorCommand()
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

    /**
     * 删除传感器
     */
    private fun deleteSensorCommand() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_DEL_EXTERNAL_SENSOR,
            "index=$deleteItemIndex"
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun initSaveCommand() {
        commandItems.clear()

        //设置采集器参数
        val entity = DasCollectorEntity(
            type = mStates.collectorType.get(),
            sensornum = mStates.sensorModelMap.size.toString(),
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_SET_COLLECTOR_CONTROL,
            entity.toCommandString()
        )
        commandItems.add(command)

        //设置采集器接入的传感器配置信息
        initExtendSensorConfigInfoCommand()

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(
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
                val entity = DasExternalSensorEntity().apply {
                    index = mIndex.toString()
                    type = sensorInfo.type
                    addr = sensorInfo.addr
                    threshold = sensorInfo.threshold
                    corrval = sensorInfo.corrval
                    when (IOTSensorType.value(sensorInfo.type)) {
                        IOTSensorType.KANG_PERCOLATE -> {//基康渗压计
                            tubealti = sensorInfo.tubealti
                            ropelen = sensorInfo.ropelen
                            poly_a = sensorInfo.poly_a
                            poly_b = sensorInfo.poly_b
                            poly_c = sensorInfo.poly_c
                            temp_k = sensorInfo.temp_k
                            temp_t0 = sensorInfo.temp_t0
                        }

                        IOTSensorType.GUDAN_PERCOLATE -> {//葛南渗压计
                            tubealti = sensorInfo.tubealti
                            ropelen = sensorInfo.ropelen
                            sens_k = sensorInfo.sens_k
                            temp_b = sensorInfo.temp_b
                            temp_t0 = sensorInfo.temp_t0
                            referval_f = sensorInfo.referval_f
                        }

                        IOTSensorType.GUDAN_SOIL_PRESSURE -> {//葛南土压力计
                            sens_k = sensorInfo.sens_k
                            temp_b = sensorInfo.temp_b
                            temp_t0 = sensorInfo.temp_t0
                            referval_f = sensorInfo.referval_f
                        }

                        IOTSensorType.GUDAN_STRESS -> {//葛南应力计
                            sens_k = sensorInfo.sens_k
                            temp_b = sensorInfo.temp_b
                            temp_t0 = sensorInfo.temp_t0
                            referval_f = sensorInfo.referval_f
                            elastic_mod = sensorInfo.elastic_mod
                        }

                        IOTSensorType.JUNXING_ZLJ_300T -> {//轴力计
                            sens_k = sensorInfo.sens_k
                            temp_b = sensorInfo.temp_b
                            temp_t0 = sensorInfo.temp_t0
                            referval_f = sensorInfo.referval_f
                        }

                        IOTSensorType.INCLINOMETER -> {//固定测斜仪
                            spacing = sensorInfo.spacing
                            model_type = sensorInfo.model_type
                        }

                        IOTSensorType.LUYAN_INCLINOMETER -> {//倾角仪
                            initvalx = sensorInfo.initvalx
                            initvaly = sensorInfo.initvaly
                            initvalz = sensorInfo.initvalz
                        }

                        IOTSensorType.WEIR -> {//量水堰计
                            lsycsds = sensorInfo.lsycsds
                            lsyysst = sensorInfo.lsyysst
                            caddr = sensorInfo.caddr
                        }

                        IOTSensorType.STATIC_LEVEL,//静力水准
                        IOTSensorType.SEDIMENTATION_METER -> {//沉降仪
                            initval = sensorInfo.initval
                        }

                        IOTSensorType.VIBRATING_SENSOR -> {//MCU_振弦传感器
                            sens_k = sensorInfo.sens_k
                            temp_b = sensorInfo.temp_b
                            temp_t0 = sensorInfo.temp_t0
                            referval_f = sensorInfo.referval_f
                            caddr = sensorInfo.caddr
                        }

                        IOTSensorType.DIGITAL_WATER_LEVEL_GAUGE,//数字式水位计
                        IOTSensorType.WATER_LEVEL_GAUGE -> {//MCU_水位(液位)计
                            tubealti = sensorInfo.tubealti
                            ropelen = sensorInfo.ropelen
                        }

                        IOTSensorType.RADAR_LEVEL_GAUGE -> {//雷达液(物)位计 设置子雷达传感器型号
                            child_type = sensorInfo.child_type
                        }

                        else -> {}
                    }
                }
                val command = IOTCommandUtil.getCommand(
                    IOTCommandType.DAS_MD_SET_EXTERNAL_SENSOR,
                    entity.toCommandString()
                )
                commandItems.add(command)
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
        val command = IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_COLLECTOR_CONTROL)
        commandItems.add(command)

        sendCommandFromCmdList(
            isStartTimeoutJob = true,
            timeoutMillis = AppContants.Communication.DELAY_15000_MILLIS
        )
    }

    /**
     * 查询采集器接入的传感器配置信息
     */
    private fun queryExtendSensorConfigInfo(sensorNum: Int) {
        commandItems.clear()
        for (i in 0 until sensorNum) {
            val command =
                IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_EXTERNAL_SENSOR, "index=$i")
            commandItems.add(command)
        }
        sendCommandFromCmdList()
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.DAS_MD_GET_COLLECTOR_CONTROL -> {
                val result = iotParseManager.parse<DasCollectorInfo>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_COLLECTOR_CONTROL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询采集器参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        initCollectorInfo(result.data)
                    }
                }
            }

            IOTCommandType.DAS_MD_GET_EXTERNAL_SENSOR -> {
                val result = iotParseManager.parse<DasExternalSensorInfo>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_EXTERNAL_SENSOR
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        initEmptySensor()
                        val errMsg = "查询传感器参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
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

            IOTCommandType.DAS_MD_DEL_EXTERNAL_SENSOR -> {//移除传感器
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "移除传感器出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("移除成功")
                            binding.rv.bindingAdapter.mutable[deleteItemIndex].let {
                                if (it is DASSensorItem) {
                                    mStates.sensorModelMap.remove(it.addr)
                                }
                            }
                            binding.rv.bindingAdapter.mutable.removeAt(deleteItemIndex)
                            binding.rv.bindingAdapter.notifyItemRemoved(deleteItemIndex)
                            updateFooter()
                        }
                    }
                }
            }

            IOTCommandType.DAS_MD_SET_COLLECTOR_CONTROL -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置采集器参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {}
                    }
                }
            }

            IOTCommandType.DAS_MD_SET_EXTERNAL_SENSOR -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "保存传感器参数出错: ${result.message}"
                        Timber.e(errMsg)
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
            mStates.collectorType.set(collectorInfo.type)
            mStates.isVibratingWireSensor.set(collectorInfo.type == "0")
            if (collectorInfo.sensornum.isEmpty() || collectorInfo.sensornum.toInt() == 0) {
                cancelNearbyCommunicationTimeoutJob()
                return
            }
            queryExtendSensorConfigInfo(collectorInfo.sensornum.toInt())
        } catch (e: Exception) {
            cancelNearbyCommunicationTimeoutJob()
            Timber.e(e)
        }
    }

    /**
     *  处理获取到的单个传感器参数信息
     */
    private fun processSensorParamsInfo(sensorInfo: DasExternalSensorInfo) {
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
        fun newInstance() = DasExternalSensorListFragment()
    }
}