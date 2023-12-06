package com.shmedo.mcloudapp.device.ui.mr702.fragment.port

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr.MRRS485Port1CollectionParamEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRRS485Port1CollectionParam
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRSensorStatus
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.launchWithViewLifecycle
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.common.ext.showMessage
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs485Port1Binding
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.common.MRRS485Port1
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.MRSensorItem
import com.shmedo.mcloudapp.device.model.RVEmptyFooter
import com.shmedo.mcloudapp.device.model.SensorModel
import com.shmedo.mcloudapp.device.ui.mr702.fragment.dialog.MR702SensorSelectionPopupView
import com.shmedo.mcloudapp.device.viewmodel.state.MR702PortHomeViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.MR702RS485Port1ViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class MR702RS485Port1Fragment : BaseIOTDeviceFragment() {
    private val binding: FragmentMr702Rs485Port1Binding by lazy { getBinding() as FragmentMr702Rs485Port1Binding }
    private val mInterfaceHomeViewModel: MR702PortHomeViewModel by activityViewModels()
    private val mStates: MR702RS485Port1ViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private var deleteItemIndex = 0

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_rs485_port1, BR.stateVM, mStates)
            .addBindingParam(BR.homeVM, mInterfaceHomeViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        initRefresh()
        initSensorAdapter()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            queryInfo()
        }
    }

    private fun initSensorAdapter() {
        binding.rv.setup { rv ->
            rv.addItemDecoration(
                MyGridSpacingItemDecoration(
                    2,
                    ConvertUtils.dp2px(8f),
                    false
                )
            )
            addType<MRSensorItem>(R.layout.item_mr702_port_sensor)
            addType<RVEmptyFooter>(R.layout.item_mr702_port_sensor_rv_footer)
            R.id.item.onClick {
                when (itemViewType) {
                    R.layout.item_mr702_port_sensor -> {
                        val item = getModel<MRSensorItem>()
                        val bundle = MR702RS485Port1SensorParamFragment.newBundleArguments(
                            item,
                            communicateWay,
                            deviceInfo,
                            bleDevice
                        )
                        nav().navigate(
                            R.id.action_mR702PortHomeFragment_to_mR702RS485Port1SensorParamFragment,
                            bundle
                        )
                    }

                    else -> showAddSensorPopup()
                }
            }
            R.id.item_del.onClick {
                val item = getModel<MRSensorItem>()
                showMessage("确定删除此传感器吗？", "提示", "删除", {
                    deleteItemIndex = modelPosition
                    deleteSensorCommand(item.modelToken + "_" + item.addr)
                }, "取消")
            }
        }
    }

    private fun showAddSensorPopup() {
        val sensorList = mInterfaceHomeViewModel.portSensorsMap["485port1"] ?: listOf()
        val selectionPopupView = MR702SensorSelectionPopupView(requireContext())
        selectionPopupView.setData("请选择传感器类型", sensorList)
            .setSelectListener(object : MR702SensorSelectionPopupView.OnSelectListener {
                override fun onSelect(sensorModel: SensorModel) {
                    val bundle = MR702RS485Port1SensorAddParamFragment.newBundleArguments(
                        MRSensorItem(
                            sensorType = sensorModel.sensorType,
                            sensorName = sensorModel.sensorName,
                            modelToken = sensorModel.modelToken,
                            modelFieldList = sensorModel.modelFieldList.map { it.fieldName }
                        ),
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                    nav().navigate(
                        R.id.action_mR702PortHomeFragment_to_mR702RS485Port1SensorAddParamFragment,
                        bundle
                    )
                }
            })
        XPopup.Builder(context)
            .dismissOnBackPressed(false) // 按返回键是否关闭弹窗，默认为true
            .dismissOnTouchOutside(false)// 点击外部是否关闭弹窗，默认为true
            .enableDrag(false)
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .asCustom(selectionPopupView)
            .show()
    }

    override fun createObserver() {
        super.createObserver()
        mMessenger.mr702Rs485PortSensorRefresh.observe(viewLifecycleOwner) { port ->
            if (port is MRRS485Port1) {
                refreshSensorInfo()
            }
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        fun onSubmitClick() {
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
    private fun deleteSensorCommand(model: String) {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_DEL_RS485_PORT1_SENSOR,
            "model=$model&del=1"
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 保存采集参数
     */
    private fun initSaveCommand() {
        commandItems.clear()
        if (mStates.acquisitionFrequency.get().isEmpty()) {
            Toaster.show("请输入采集频率")
            return
        }
        if (mStates.collectionDuration.get().isEmpty()) {
            Toaster.show("请输入采集周期")
            return
        }
        if (mStates.collectionTimes.get().isEmpty()) {
            Toaster.show("请输入采集间隔")
            return
        }
        if (mStates.noResponseTimes.get().isEmpty()) {
            Toaster.show("请输入无响应次数")
            return
        }
        if (mStates.delayDuration.get().isEmpty()) {
            Toaster.show("请输入延时时间")
            return
        }
        val entity = MRRS485Port1CollectionParamEntity(
            collfreq = mStates.acquisitionFrequency.get(),
            collcycle = mStates.collectionDuration.get(),
            collround = mStates.collectionTimes.get(),
            noresp = mStates.noResponseTimes.get(),
            powerontimes = mStates.delayDuration.get()
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_SET_RS485_PORT1_COLL,
            entity.toCommandString()
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryInfo() {
        commandItems.clear()

        var command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_RS485_PORT1_COLL)
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_RS485_PORT1_SENSOR, "index=0")
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun refreshSensorInfo() {
        commandItems.clear()

        commandItems.add(
            IOTCommandUtil.getCommand(
                IOTCommandType.MD_MR_GET_RS485_PORT1_SENSOR,
                "index=0"
            )
        )
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        if (viewLifecycleOwner.lifecycle.currentState < androidx.lifecycle.Lifecycle.State.RESUMED) {
            return
        }
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_RS485_PORT1_COLL -> {
                val result = iotParseManager.parse<MRRS485Port1CollectionParam>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_RS485_PORT1_COLL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询采集参数出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        initCollectionData(result.data)
                    }
                }
            }

            IOTCommandType.MD_MR_GET_RS485_PORT1_SENSOR -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_RS485_PORT1_SENSOR
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        if (result.message.contains("index=0")) {
                            initEmptySensor()
                            return
                        }
                        initEmptySensor()
                        val errMsg = "查询传感器状态信息出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        binding.refreshLayout.finish()
                        initSensorData(result.data)
                    }
                }
            }

            IOTCommandType.MD_MR_DEL_RS485_PORT1_SENSOR -> {
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
                            binding.rv.bindingAdapter.mutable.removeAt(deleteItemIndex)
                            binding.rv.bindingAdapter.notifyItemRemoved(deleteItemIndex)
                            updateFooter()
                        }
                    }
                }
            }

            IOTCommandType.MD_MR_SET_RS485_PORT1_COLL -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置采集参数出错: ${result.message}"
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

    private fun initCollectionData(collectionParam: MRRS485Port1CollectionParam) {
        try {
            mStates.acquisitionFrequency.set(collectionParam.collfreq)
            mStates.collectionDuration.set(collectionParam.collcycle)
            mStates.collectionTimes.set(collectionParam.collround)
            mStates.noResponseTimes.set(collectionParam.noresp)
            mStates.delayDuration.set(collectionParam.powerontimes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initSensorData(content: String) {
        launchWithViewLifecycle {
            try {
                val sensorStatusList =
                    MoshiUtil.fromJson<List<MRSensorStatus>>(content)
                        ?: return@launchWithViewLifecycle
                val list = sensorStatusList.map { sensorStatus ->
                    val strs = sensorStatus.model.split("_").toTypedArray()
                    MRSensorItem(
                        isPlugin = sensorStatus.sta == "0",
                        addr = strs[1],
                        addrDesc = "地址-${strs[1]}",
                        sensorName = mInterfaceHomeViewModel.sensorMap[strs[0]]?.sensorName
                            ?: "未知类型",
                        modelToken = strs[0],
                        modelFieldList = mInterfaceHomeViewModel.sensorMap[strs[0]]?.modelFieldList?.map { it.fieldName }
                            ?: listOf()
                    )
                }
                binding.rv.models = list
                updateFooter()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initEmptySensor() {
        val list = arrayListOf<MRSensorItem>()
        binding.rv.models = list
        updateFooter()
    }

    private fun updateFooter() {
        if (binding.rv.bindingAdapter.modelCount < SENSOR_SIZE) {
            if (binding.rv.bindingAdapter.footerCount == 0)
                binding.rv.bindingAdapter.addFooter(RVEmptyFooter(), animation = true)
        } else {
            binding.rv.bindingAdapter.removeFooterAt(animation = true)
        }
    }

    companion object {
        fun newInstance() = MR702RS485Port1Fragment()
        const val SENSOR_SIZE = 32
        const val FRAGMENT_RESULT_REQUEST_KEY = "MR702RS485Port1Fragment"
    }

    private fun testData() {
        val content =
            "[{\"model\": \"214_1\",\"sta\": \"0\"},{\"model\": \"214_2\",\"sta\": \"0\"},{\"model\": \"214_3\",\"sta\": \"2\"},{\"model\": \"214_3\",\"sta\": \"2\"}]"
        initSensorData(content)
    }
}