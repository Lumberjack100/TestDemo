package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702.port

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.model.SensorModel
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRRS485Port1CollectionParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port1CollectionParam
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port1SensorParam
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port1SensorParamWrapper
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRSensorStatus
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs485Port1Binding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.MRRS485Port1
import com.shmedo.mcloudapp.model.MRSensorItem
import com.shmedo.mcloudapp.model.RVEmptyFooter
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.collector_product.dialog.MR702SensorSelectionPopupView
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702PortHomeViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702RS485Port1ViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber

class MR702RS485Port1Fragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702Rs485Port1Binding
    private val portHomeViewModel: MR702PortHomeViewModel by activityViewModels()
    private val mStates: MR702RS485Port1ViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val sensorStatusMap = mutableMapOf<String, String>()

    private var deleteItemIndex = 0


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_rs485_port1, BR.stateVM, mStates)
            .addBindingParam(BR.homeVM, portHomeViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702Rs485Port1Binding
        initRefresh()
        initSensorAdapter()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryCollectorAndSensorList()
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
        mStates.acquisitionFrequency.set("5000")//采集频率
        mStates.collectionDuration.set("57")//采集周期
        mStates.collectionTimes.set("3")//采集次数
        mStates.noResponseTimes.set("3")//无应答次数
        mStates.delayDuration.set("10")//延时时间
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
                            MR702RS485Port1SingleSensorAddParamFragment.Companion.newBundleArguments(
                                item,
                                productType,
                                communicateWay,
                                deviceInfo,
                                bleDevice
                            )
                        portHomeViewModel.configPort4851SensorIDToSensorModelMap[item.sensorID]?.modelFieldList?.size?.let { fieldSize ->
                            nav().safeNavigate(
                                if (fieldSize > 1) R.id.action_global_to_mR702RS485Port1TwoSensorParamFragment
                                else R.id.action_global_to_mR702RS485Port1SingleSensorParamFragment,
                                bundle
                            )
                        }
                    }

                    else -> showAddSensorPopup()
                }
            }
            R.id.item_del.onClick {
                val item = getModel<MRSensorItem>()
                showMessage("确定删除此传感器吗？", "提示", "删除", {
                    deleteItemIndex = modelPosition
                    val model = if (item.modelToken.isEmpty() || item.addr.isEmpty()) ""
                    else item.modelToken + "_" + item.addr
                    deleteSensorCommand(model)
                }, "取消")
            }
        }
    }

    override fun createObserver() {
        super.createObserver()
        mMessenger.mr702Rs485PortSensorRefresh.observe(viewLifecycleOwner) { port ->
            if (port is MRRS485Port1) {
                refreshSensorList()
            }
        }
    }

    inner class ClickProxy : BaseClickProxy() {
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
            initSaveCommand()
        }
    }

    /**
     * 显示添加传感器弹窗
     */
    private fun showAddSensorPopup() {
        val sensorList = portHomeViewModel.configPort4851SensorIDToSensorModelMap.values.toList()
        val selectionPopupView = MR702SensorSelectionPopupView(requireContext())
        selectionPopupView.setData("请选择传感器", sensorList)
            .setSelectListener(object : MR702SensorSelectionPopupView.OnSelectListener {
                override fun onSelect(sensorModel: SensorModel) {
                    val bundle =
                        MR702RS485Port1SingleSensorAddParamFragment.Companion.newBundleArguments(
                            MRSensorItem(
                                sensorID = sensorModel.sensorID,
                                sensorName = sensorModel.sensorName,
                                modelToken = sensorModel.modelToken,
                            ),
                            productType,
                            communicateWay,
                            deviceInfo,
                            bleDevice
                        )
                    nav().safeNavigate(
                        if (sensorModel.modelFieldList.size > 1) R.id.action_global_to_mR702RS485Port1TwoSensorAddParamFragment
                        else R.id.action_global_to_mR702RS485Port1SingleSensorAddParamFragment,
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

    /**
     * 删除传感器
     */
    private fun deleteSensorCommand(model: String) {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_DEL_RS485_PORT1_SENSOR,
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
            showMessageDialog("请输入采集频率")
            return
        }
        if (mStates.collectionDuration.get().isEmpty()) {
            showMessageDialog("请输入采集周期")
            return
        }
        if (mStates.collectionTimes.get().isEmpty()) {
            showMessageDialog("请输入采集次数")
            return
        }
        if (mStates.noResponseTimes.get().isEmpty()) {
            showMessageDialog("请输入无应答次数")
            return
        }
        if (mStates.delayDuration.get().isEmpty()) {
            showMessageDialog("请输入延时时间")
            return
        }
        val entity = MRRS485Port1CollectionParamEntity(
            collfreq = mStates.acquisitionFrequency.get(),
            collcycle = mStates.collectionDuration.get(),
            collround = mStates.collectionTimes.get(),
            noresp = mStates.noResponseTimes.get(),
            powerontimes = mStates.delayDuration.get(),
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_SET_RS485_PORT1_COLL,
            entity.toCommandString()
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryCollectorAndSensorList() {
        initEmptySensor()
        commandItems.clear()

        var command = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_RS485_PORT1_COLL)
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_RS485_PORT1_SENSOR, "index=0")
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun refreshSensorList() {
        initEmptySensor()
        commandItems.clear()

        commandItems.add(
            IOTCommandUtil.getCommand(
                IOTCommandType.MR_MD_GET_RS485_PORT1_SENSOR,
                "index=0"
            )
        )
        showLoadingDialog(StringUtils.getString(R.string.loading))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    /**
     * 查询传感器参数信息
     */
    private fun querySensorParamInfo(modelToken: String, addr: String) {
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_GET_RS485_PORT1_SENSOR_PARAM,
            "model=${modelToken}_${addr}&index=0"
        )
        commandItems.add(command)
    }

    private fun isTargetCommandType(commandType: IOTCommandType): Boolean =
        (commandType == IOTCommandType.MR_MD_GET_RS485_PORT1_COLL)
                || (commandType == IOTCommandType.MR_MD_GET_RS485_PORT1_SENSOR)
                || (commandType == IOTCommandType.MR_MD_GET_RS485_PORT1_SENSOR_PARAM)
                || (commandType == IOTCommandType.MR_MD_DEL_RS485_PORT1_SENSOR)
                || (commandType == IOTCommandType.MR_MD_SET_RS485_PORT1_COLL)

    /**
     * 4G 下发指令响应失败
     */
    override fun doCmdResponseResultError(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        val isShowMessage = isTargetCommandType(IOTCommandUtil.extractCommandType(cmdStr))
        super.doCmdResponseResultError(
            cmdStr = cmdStr,
            errMsg = errMsg,
            isShowErrMsg = isShowMessage,
            isMessageDialog = isShowMessage
        )
    }

    /**
     * 4G 下发指令响应超时
     */
    override fun doCmdResponseResultTimeOut(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        val isShowMessage = isTargetCommandType(IOTCommandUtil.extractCommandType(cmdStr))
        super.doCmdResponseResultTimeOut(
            cmdStr = cmdStr,
            errMsg = errMsg,
            isShowErrMsg = isShowMessage,
            isMessageDialog = isShowMessage
        )
    }

    /**
     * 蓝牙下发指令响应超时
     */
    override fun showNearbyCommunicationTimeoutAlert(
        cmdStr: String,
        isDismissLoadingDialog: Boolean,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean,
        errMsg: String
    ) {
        val isShowMessage = isTargetCommandType(IOTCommandUtil.extractCommandType(cmdStr))
        super.showNearbyCommunicationTimeoutAlert(
            cmdStr = cmdStr,
            isDismissLoadingDialog = isDismissLoadingDialog,
            isShowErrMsg = isShowMessage,
            isMessageDialog = isShowMessage,
            errMsg = errMsg
        )
    }

    override fun setResultData(cmdStr: String) {
        if (isRestrictHiddenMode() && isHidden) {
            return
        }
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MR_MD_GET_RS485_PORT1_COLL -> {
                val result = iotParseManager.parse<MRRS485Port1CollectionParam>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_RS485_PORT1_COLL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询采集参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initCollectionData(result.data)
                    }
                }
            }

            IOTCommandType.MR_MD_GET_RS485_PORT1_SENSOR -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_RS485_PORT1_SENSOR
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询传感器状态信息出错: ${result.message}"
                        handleFailureResult(
                            errMsg,
                            isShowErrMsg = !result.message.contains("index")
                        )
                        initEmptySensor()
                        return
                    }

                    is IOTCommandResult.Success -> {
                        initSensorListData(result.data)
                    }
                }
            }

            IOTCommandType.MR_MD_GET_RS485_PORT1_SENSOR_PARAM -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_RS485_PORT1_SENSOR_PARAM
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询传感器参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        if (binding.rv.mutable.isEmpty())
                            initEmptySensor()
                        return
                    }

                    is IOTCommandResult.Success -> {
                        //处理此通道的传感器配置参数
                        processSensorParamsInfo(result.data)
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                            updateFooter()
                        }
                    }
                }
            }

            IOTCommandType.MR_MD_DEL_RS485_PORT1_SENSOR -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "移除传感器出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("移除成功")
                            updateAdapterRemoveSensorItem()
                        }
                    }
                }
            }

            IOTCommandType.MR_MD_SET_RS485_PORT1_COLL -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置采集参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("数据保存成功")
                            // 保存初始状态
                            mStates.saveInitialState()
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initCollectionData(collectionParam: MRRS485Port1CollectionParam) {
        try {
            mStates.acquisitionFrequency.set(collectionParam.collfreq)
            mStates.collectionDuration.set(collectionParam.collcycle)
            mStates.collectionTimes.set(collectionParam.collround)
            mStates.noResponseTimes.set(collectionParam.noresp)
            mStates.delayDuration.set(collectionParam.powerontimes)

            // 保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.Forest.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    private fun initSensorListData(content: String) {
        launchWithViewLifecycle(Dispatchers.IO) {
            try {
                val sensorStatusList = MoshiUtil.fromJson<List<MRSensorStatus>>(content)
                    ?: return@launchWithViewLifecycle
                if (sensorStatusList.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        binding.refreshLayout.finish()
                    }
                    return@launchWithViewLifecycle
                }

                commandItems.clear()
                sensorStatusMap.clear()
                sensorStatusList.map { sensorStatus ->
                    sensorStatusMap[sensorStatus.model] = sensorStatus.sta

                    val modelAddr = sensorStatus.model.split("_")
                    val modelToken = if (modelAddr.isNotEmpty()) modelAddr[0] else ""
                    val address = if (modelAddr.size > 1) modelAddr[1] else ""
                    querySensorParamInfo(modelToken, address)
                }

                sendCommandFromCmdList()
            } catch (e: Exception) {
                Timber.Forest.e(e)
                withContext(Dispatchers.Main) {
                    addDeviceLogItem(Log.ERROR, e.errorMsg)
                    binding.refreshLayout.finish()
                }
            }
        }
    }

    /**
     * 处理获取到的单个传感器参数信息
     */
    private fun processSensorParamsInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val sensorParamWrapper = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<MRRS485Port1SensorParamWrapper>(content)
                } ?: return@launchWithViewLifecycle

                val sensorParam: MRRS485Port1SensorParam = sensorParamWrapper.port1_param[0]
                val modelAddr = sensorParam.model.split("_")
                val modelToken = if (modelAddr.isNotEmpty()) modelAddr[0] else ""
                val address = if (modelAddr.size > 1) modelAddr[1] else ""

                val item = MRSensorItem(
                    isPlugin = sensorStatusMap[sensorParam.model] == "0",
                    addr = address,
                    addrDesc = "地址-$address",
                    sensorID = sensorParam.sensorId,
                    sensorName = portHomeViewModel.configPort4851SensorIDToSensorModelMap[sensorParam.sensorId]?.sensorName
                        ?: "自定义传感器",
                    modelToken = modelToken,
                )
                binding.rv.mutable.add(item)
                binding.rv.bindingAdapter.notifyItemInserted(binding.rv.bindingAdapter.modelCount)

            } catch (e: Exception) {
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
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
        if (binding.rv.bindingAdapter.modelCount < MAX_SENSOR_COUNT) {
            if (binding.rv.bindingAdapter.footerCount == 0)
                binding.rv.bindingAdapter.addFooter(RVEmptyFooter(), animation = true)
        } else {
            binding.rv.bindingAdapter.clearFooter()
        }
    }

    companion object {
        fun newInstance() = MR702RS485Port1Fragment()
        const val MAX_SENSOR_COUNT = 32
    }
}