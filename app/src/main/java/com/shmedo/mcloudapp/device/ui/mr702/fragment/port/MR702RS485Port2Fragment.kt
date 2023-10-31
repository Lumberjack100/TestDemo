package com.shmedo.mcloudapp.device.ui.mr702.fragment.port

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr.MRCollectionParamEntity
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr.MRSerialPortParamEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRCollectionParam
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRSensorStatus
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRSerialPortParam
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
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs485Port2Binding
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.common.MRRS485Port2
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.MRSensorItem
import com.shmedo.mcloudapp.device.model.RVEmptyFooter
import com.shmedo.mcloudapp.device.model.SensorModel
import com.shmedo.mcloudapp.device.ui.mr702.fragment.MR702SensorSelectionPopupView
import com.shmedo.mcloudapp.device.viewmodel.state.MR702PortHomeViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.MR702RS485Port2ViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class MR702RS485Port2Fragment : BaseIOTDeviceFragment() {
    private val binding: FragmentMr702Rs485Port2Binding by lazy { getBinding() as FragmentMr702Rs485Port2Binding }
    private val mInterfaceHomeViewModel: MR702PortHomeViewModel by activityViewModels()
    private val mStates: MR702RS485Port2ViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val collectorTypeList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_rs485_port2_collector_type) }
    private val dataBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_data_bit) }
    private val checkBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_check_bit) }
    private val stopBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_stop_bit) }

    private var deleteItemIndex = 0

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_rs485_port2, BR.stateVM, mStates)
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
                        val bundle = MR702RS485Port2SensorParamFragment.newBundleArguments(
                            item,
                            false,
                            communicateWay,
                            deviceInfo,
                            bleDevice
                        )
                        nav().navigate(
                            R.id.action_mR702PortHomeFragment_to_mR702RS485Port2SensorParamFragment,
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
                    deleteSensorCommand(item.addr)
                }, "取消")
            }
        }
    }

    private fun showAddSensorPopup() {
        val sensorList = mInterfaceHomeViewModel.portSensorsMap["485port2"] ?: listOf()
        val selectionPopupView = MR702SensorSelectionPopupView(requireContext())
        selectionPopupView.setData("请选择传感器类型", sensorList)
            .setSelectListener(object : MR702SensorSelectionPopupView.OnSelectListener {
                override fun onSelect(sensorModel: SensorModel) {
                    val bundle = MR702RS485Port2SensorParamFragment.newBundleArguments(
                        MRSensorItem(
                            sensorType = sensorModel.sensorType,
                            sensorName = sensorModel.sensorName,
                            modelToken = sensorModel.modelToken,
                            modelFieldList = sensorModel.modelFieldList.map { it.fieldName }
                        ),
                        true,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                    nav().navigate(
                        R.id.action_mR702PortHomeFragment_to_mR702RS485Port2SensorParamFragment,
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
        mInterfaceHomeViewModel.isEditable.observe(viewLifecycleOwner) { editable ->
            val list = binding.rv.bindingAdapter.models ?: return@observe
            list.forEach { item ->
                (item as MRSensorItem).isShowDel = editable
            }
            binding.rv.models = list
        }
        mMessenger.mr702Rs485PortSensorRefresh.observe(viewLifecycleOwner) { port ->
            if (port is MRRS485Port2) {
                refreshSensorInfo()
            }
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        fun onCollectorTypeChooseClick() {
            val selectedIndex = collectorTypeList.indexOf(mStates.collectorType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择采集器类型", collectorTypeList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.collectorType.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onDataBitChooseClick() {
            val selectedIndex = dataBitList.indexOf(mStates.dataBit.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择数据位", dataBitList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.dataBit.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onCheckBitChooseClick() {
            val selectedIndex = checkBitList.indexOf(mStates.checkBit.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择校验位", checkBitList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.checkBit.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onStopBitChooseClick() {
            val selectedIndex = stopBitList.indexOf(mStates.stopBit.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择停止位", stopBitList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.stopBit.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

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
    private fun deleteSensorCommand(chl: String) {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_DEL_RS485_PORT2_SENSOR,
            "chl=$chl"
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
            Toaster.show("请输入采集时长")
            return
        }
        if (mStates.collectionInterval.get().isEmpty()) {
            Toaster.show("请输入采集间隔")
            return
        }
        if (mStates.noResponseTimes.get().isEmpty()) {
            Toaster.show("请输入无响应次数")
            return
        }
        if (mStates.collectorAddress.get().isEmpty()) {
            Toaster.show("请输入采集器地址")
            return
        }
        val entity = MRCollectionParamEntity(
            collfreq = mStates.acquisitionFrequency.get(),
            collcycle = mStates.collectionDuration.get(),
            collgap = mStates.collectionInterval.get(),
            noresp = mStates.noResponseTimes.get(),
            colltype = (collectorTypeList.indexOf(mStates.collectorType.get()) + 1).toString(),
            colladdr = mStates.collectorAddress.get()
        )
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_SET_RS485_PORT2_COLL,
            entity.toCommandString()
        )
        commandItems.add(command)

        if (mStates.baudRate.get().isEmpty()) {
            Toaster.show("请选择波特率")
            return
        }
        val entity2 = MRSerialPortParamEntity(
            baud = mStates.baudRate.get(),
            databit = (dataBitList.indexOf(mStates.dataBit.get())).toString(),
            parity = (checkBitList.indexOf(mStates.checkBit.get())).toString(),
            stopbit = (stopBitList.indexOf(mStates.stopBit.get())).toString(),
        )
        command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_SET_RS485_PORT2_UART,
            entity2.toCommandString()
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

        var command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_RS485_PORT2_COLL)
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_RS485_PORT2_UART)
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_RS485_PORT2_SENSOR, "index=0")
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun refreshSensorInfo() {
        commandItems.clear()

        commandItems.add(
            IOTCommandUtil.getCommand(
                IOTCommandType.MD_MR_GET_RS485_PORT2_SENSOR,
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
            IOTCommandType.MD_MR_GET_RS485_PORT2_COLL -> {
                val result = iotParseManager.parse<MRCollectionParam>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_RS485_PORT2_COLL
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

            IOTCommandType.MD_MR_GET_RS485_PORT2_UART -> {
                val result = iotParseManager.parse<MRSerialPortParam>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_RS485_PORT2_UART
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
                        sendCommandFromCmdList()
                        initUartData(result.data)
                    }
                }
            }

            IOTCommandType.MD_MR_GET_RS485_PORT2_SENSOR -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_RS485_PORT2_SENSOR
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        if (result.message.contains("index")) {
                            initEmptySensor()
                            return
                        }
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

            IOTCommandType.MD_MR_DEL_RS485_PORT2_SENSOR -> {
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

            IOTCommandType.MD_MR_SET_RS485_PORT2_COLL -> {
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

            IOTCommandType.MD_MR_SET_RS485_PORT2_UART -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置采集器参数出错: ${result.message}"
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

    private fun initCollectionData(collectionParam: MRCollectionParam) {
        mStates.acquisitionFrequency.set(collectionParam.collfreq)
        mStates.collectionDuration.set(collectionParam.collcycle)
        mStates.collectionInterval.set(collectionParam.collgap)
        mStates.noResponseTimes.set(collectionParam.noresp)
        mStates.collectorType.set(collectorTypeList[collectionParam.colltype.toInt() - 1])
        mStates.collectorAddress.set(collectionParam.colladdr)
    }

    private fun initUartData(serialPortParam: MRSerialPortParam) {
        mStates.baudRate.set(serialPortParam.baud)
        mStates.dataBit.set(dataBitList[serialPortParam.databit.toInt()])
        mStates.checkBit.set(checkBitList[serialPortParam.parity.toInt()])
        mStates.stopBit.set(stopBitList[serialPortParam.stopbit.toInt()])
    }

    private fun initSensorData(content: String) {
        launchWithViewLifecycle{
            try {
                val sensorStatusList =
                    MoshiUtil.fromJson<List<MRSensorStatus>>(content) ?: return@launchWithViewLifecycle
                val list = sensorStatusList.map { sensorStatus ->
                    MRSensorItem(
                        isPlugin = sensorStatus.sta == "0",
                        chl = sensorStatus.chl,
                        addrDesc = "通道-${sensorStatus.chl}",
                        sensorName = mInterfaceHomeViewModel.sensorMap[sensorStatus.sensortype]?.sensorName
                            ?: "未知类型",
                        sensorType = sensorStatus.sensortype,
                        modelToken = mInterfaceHomeViewModel.sensorMap[sensorStatus.sensortype]?.modelToken
                            ?: "",
                        modelFieldList = mInterfaceHomeViewModel.sensorMap[sensorStatus.sensortype]?.modelFieldList?.map { it.fieldName }
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
        fun newInstance() = MR702RS485Port2Fragment()
        const val SENSOR_SIZE = 32
    }
}