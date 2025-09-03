package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702.port

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.model.SensorModel
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRRS485Port2CollectionParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRSerialPortParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port2CollectionParam
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRSensorStatus
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRSerialPortParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.communication.model.ErrorHandlingStrategy
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs485Port2Binding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.MRSensorItem
import com.shmedo.mcloudapp.model.RVEmptyFooter
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.collector_product.dialog.MR702SensorSelectionPopupView
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702PortHomeViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702RS485Port2ViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.UUID

class MR702RS485Port2Fragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702Rs485Port2Binding
    private val mStates: MR702RS485Port2ViewModel by viewModels()
    private val portHomeViewModel: MR702PortHomeViewModel by activityViewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val collectorTypeList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_rs485_port2_collector_type) }
    private val dataBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_data_bit) }
    private val checkBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_check_bit) }
    private val stopBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_stop_bit) }

    private var deleteItemIndex = 0


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_rs485_port2, BR.stateVM, mStates)
            .addBindingParam(BR.homeVM, portHomeViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702Rs485Port2Binding
        initRefresh()
        initSensorAdapter()
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
            queryInfo()
        }
    }

    override fun initData() {
        super.initData()
        resetDefaultParams()
        // 保存初始状态
        mStates.saveInitialState()
    }

    /**
     * 初始化默认参数
     */
    private fun resetDefaultParams() {
        mStates.acquisitionFrequency.set("180")//采集频率
        mStates.collectionTimes.set("3")//采集次数
        mStates.noResponseTimes.set("3")//无应答次数
        mStates.delayDuration.set("10")//延时时间

        mStates.collectorType.set(collectorTypeList[0])
        mStates.collectorAddress.set("1")//采集器地址
        mStates.baudRate.set("9600")  //默认波特率
        mStates.dataBit.set(dataBitList[3])//默认数据位 8
        mStates.checkBit.set(checkBitList[0])//默认校验位 无
        mStates.stopBit.set(stopBitList[0])//默认停止位 1
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
            addType<RVEmptyFooter>(R.layout.item_sensor_add_footer)
            R.id.item.onClick {
                when (itemViewType) {
                    R.layout.item_mr702_port_sensor -> {
                        val item = getModel<MRSensorItem>()
                        val bundle =
                            MR702RS485Port2SensorParamFragment.Companion.newBundleArguments(
                                item,
                                false,
                                productType,
                                communicateWay,
                                deviceInfo,
                                bleDevice
                            )
                        nav().safeNavigate(
                            R.id.action_global_to_mR702RS485Port2SensorParamFragment,
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
                    deleteSensorCommand(item.chl)
                }, "取消")
            }
        }
    }

    private fun showAddSensorPopup() {
        val sensorList = portHomeViewModel.configPort4852SensorIDToSensorModelMap.values.toList()
        val selectionPopupView = MR702SensorSelectionPopupView(requireContext())
        selectionPopupView.setData("请选择传感器", sensorList)
            .setSelectListener(object : MR702SensorSelectionPopupView.OnSelectListener {
                override fun onSelect(sensorModel: SensorModel) {
                    val bundle = MR702RS485Port2SensorParamFragment.Companion.newBundleArguments(
                        MRSensorItem(
                            sensorID = sensorModel.sensorID,
                            sensorName = sensorModel.sensorName,
                            modelToken = sensorModel.modelToken,
                        ),
                        true,
                        productType,
                        communicateWay,
                        deviceInfo,
                        bleDevice
                    )
                    nav().safeNavigate(
                        R.id.action_global_to_mR702RS485Port2SensorParamFragment,
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
        portHomeViewModel.port2SensorUpdateEvent.observe(viewLifecycleOwner) { sensorItem ->
            Timber.e("接收到传感器更新事件: ${sensorItem.sensorName}")
            updateSensorInList(sensorItem)
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

        /**
         * 恢复默认配置
         */
        fun onResetClick() {
            resetDefaultParams()
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
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
        val commands = mutableListOf<String>()

        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_DEL_RS485_PORT2_SENSOR,
            "chl=$chl"
        )
        commands.add(command)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    /**
     * 保存采集参数
     */
    private fun initSaveCommand() {
        val commands = mutableListOf<String>()

        if (mStates.acquisitionFrequency.get().isEmpty()) {
            showMessageDialog("请输入采集频率")
            return
        }
        if (mStates.collectionTimes.get().isEmpty()) {
            showMessageDialog("请输入采集次数")
            return
        }
        if (mStates.noResponseTimes.get().isEmpty()) {
            showMessageDialog("请输入无响应次数")
            return
        }
        if (mStates.delayDuration.get().isEmpty()) {
            showMessageDialog("请输入延时时间")
            return
        }
        if (mStates.collectorAddress.get().isEmpty()) {
            showMessageDialog("请输入采集器地址")
            return
        }
        val entity = MRRS485Port2CollectionParamEntity(
            collfreq = mStates.acquisitionFrequency.get(),
            collround = mStates.collectionTimes.get(),
            noresp = mStates.noResponseTimes.get(),
            colltype = (collectorTypeList.indexOf(mStates.collectorType.get()) + 1).toString(),
            colladdr = mStates.collectorAddress.get(),
            powerontimes = mStates.delayDuration.get()
        )
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_SET_RS485_PORT2_COLL,
            entity.toCommandString()
        )
        commands.add(command)

        if (mStates.baudRate.get().isEmpty()) {
            Toaster.show("请选择波特率")
            return
        }
        val entity2 = MRSerialPortParamEntity(
            baud = mStates.baudRate.get(),
            databit = mStates.dataBit.get(),
            parity = (checkBitList.indexOf(mStates.checkBit.get())).toString(),
            stopbit = (stopBitList.indexOf(mStates.stopBit.get())).toString(),
        )
        command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_SET_RS485_PORT2_UART,
            entity2.toCommandString()
        )
        commands.add(command)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryInfo() {
        val commands = mutableListOf<String>()

        var command = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_RS485_PORT2_COLL)
        commands.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_RS485_PORT2_UART)
        commands.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_RS485_PORT2_SENSOR, "index=0")
        commands.add(command)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig(
                    strategy = ErrorHandlingStrategy.Dialog
                )
            )
        )
    }


    /**
     * 添加传感器到列表（增量更新）
     */
    private fun addSensorToList(sensorItem: MRSensorItem) {
        binding.rv.bindingAdapter.apply {
            mutable.add(sensorItem)
            notifyItemInserted(itemCount)
        }
        updateFooter()
    }

    /**
     * 更新列表中的传感器（增量更新）
     */
    private fun updateSensorInList(sensorItem: MRSensorItem) {
        val currentList = binding.rv.mutable.filterIsInstance<MRSensorItem>().toMutableList()
        val index =
            currentList.indexOfFirst { it.uuid == sensorItem.uuid }
        if (index >= 0) {
            currentList[index].refreshStatus(
                sensorItem.isPlugin,
                sensorItem.addr,
                sensorItem.addrDesc
            )
        } else {
            // 如果没找到，直接添加
            addSensorToList(sensorItem)
        }
    }


    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MR_MD_GET_RS485_PORT2_COLL -> {
                val result = iotParseManager.parse<MRRS485Port2CollectionParam>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_RS485_PORT2_COLL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询采集参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        initCollectionData(result.data)
                    }
                }
            }

            IOTCommandType.MR_MD_GET_RS485_PORT2_UART -> {
                val result = iotParseManager.parse<MRSerialPortParam>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_RS485_PORT2_UART
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询采集器参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        initUartData(result.data)
                    }
                }
            }

            IOTCommandType.MR_MD_GET_RS485_PORT2_SENSOR -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_RS485_PORT2_SENSOR
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        if (result.message.contains("index")) {
                            initEmptySensor()
                            return
                        }
                        val errMsg = "查询传感器状态信息出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        initSensorData(result.data)
                    }
                }
            }

            IOTCommandType.MR_MD_DEL_RS485_PORT2_SENSOR -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "移除传感器出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        if (!isCommunicationExecuting()) {
                            Toaster.show("移除成功")
                            updateAdapterRemoveSensorItem()
                        }
                    }
                }
            }

            IOTCommandType.MR_MD_SET_RS485_PORT2_COLL -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置采集参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        if (!isCommunicationExecuting()) {
                            Toaster.show("数据保存成功")
                            // 保存初始状态
                            mStates.saveInitialState()
                        }
                    }
                }
            }

            IOTCommandType.MR_MD_SET_RS485_PORT2_UART -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置采集器参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        if (!isCommunicationExecuting()) {
                            Toaster.show("数据保存成功")
                            // 保存初始状态
                            mStates.saveInitialState()
                        }
                    }
                }
            }

            else -> {

            }
        }
    }

    private fun initCollectionData(collectionParam: MRRS485Port2CollectionParam) {
        try {
            mStates.acquisitionFrequency.set(collectionParam.collfreq)
            mStates.collectionTimes.set(collectionParam.collround)
            mStates.noResponseTimes.set(collectionParam.noresp)
            collectionParam.colltype.toInt().let {
                if (it in 1..collectorTypeList.size) {
                    mStates.collectorType.set(collectorTypeList[it - 1])
                }
            }
            mStates.collectorAddress.set(collectionParam.colladdr)
            mStates.delayDuration.set(collectionParam.powerontimes)

            // 保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.Forest.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initUartData(serialPortParam: MRSerialPortParam) {
        try {
            mStates.baudRate.set(serialPortParam.baud)
            mStates.dataBit.set(serialPortParam.databit)
            serialPortParam.parity.toInt().let {
                if (it in checkBitList.indices) {
                    mStates.checkBit.set(checkBitList[it])
                }
            }
            serialPortParam.stopbit.toInt().let {
                if (it in stopBitList.indices) {
                    mStates.stopBit.set(stopBitList[it])
                }
            }

            // 保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.Forest.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initSensorData(content: String) {
        launchWithViewLifecycle(Dispatchers.IO) {
            try {
                val sensorStatusList = MoshiUtil.fromJson<List<MRSensorStatus>>(content)
                    ?: return@launchWithViewLifecycle

                val list = sensorStatusList.map { sensorStatus ->
                    val sensorType = sensorStatus.sensortype
                    MRSensorItem(
                        isPlugin = sensorStatus.sta == "0",
                        chl = sensorStatus.chl,
                        addrDesc = "通道-${sensorStatus.chl}",
                        sensorID = sensorType,
                        sensorName = portHomeViewModel.configPort4852SensorIDToSensorModelMap[sensorType]?.sensorName
                            ?: "未知类型",
                        modelToken = portHomeViewModel.configPort4852SensorIDToSensorModelMap[sensorType]?.modelToken
                            ?: "",
                        uuid = UUID.randomUUID().toString()
                    )
                }
                withContext(Dispatchers.Main) {
                    binding.rv.models = list
                    updateFooter()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initEmptySensor() {
        binding.rv.models = arrayListOf<MRSensorItem>()
        updateFooter()
    }

    private fun updateAdapterRemoveSensorItem() {
        binding.rv.bindingAdapter.mutable.removeAt(deleteItemIndex)
        binding.rv.bindingAdapter.notifyItemRemoved(deleteItemIndex)
        updateFooter()
    }

    private fun updateFooter() {
        if (binding.rv.bindingAdapter.modelCount < SENSOR_SIZE) {
            if (binding.rv.bindingAdapter.footerCount == 0)
                binding.rv.bindingAdapter.addFooter(RVEmptyFooter(), animation = true)
        } else {
            binding.rv.bindingAdapter.clearFooter()
        }
    }

    companion object {
        fun newInstance() = MR702RS485Port2Fragment()
        const val SENSOR_SIZE = 32
    }
}