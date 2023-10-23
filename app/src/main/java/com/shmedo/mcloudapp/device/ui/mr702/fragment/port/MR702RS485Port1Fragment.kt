package com.shmedo.mcloudapp.device.ui.mr702.fragment.port

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRCollectionParam
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRSensorStatus
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.common.ext.showMessage
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs485Port1Binding
import com.shmedo.mcloudapp.device.BaseClickProxy
import com.shmedo.mcloudapp.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.MRSensorItem
import com.shmedo.mcloudapp.device.model.RVEmptyFooter
import com.shmedo.mcloudapp.device.model.SensorModel
import com.shmedo.mcloudapp.device.ui.mr702.fragment.MR702SensorSelectionPopupView
import com.shmedo.mcloudapp.device.viewmodel.state.MR702PortHomeViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.MR702RS485Port1ViewModel
import kotlinx.coroutines.launch
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

                    }
                    else -> showAddSensorPopup()
                }
            }
            R.id.item_del.onClick {
                val item = getModel<MRSensorItem>()
                showMessage("确定删除此传感器吗？", "提示", "删除", {
                    deleteItemIndex = adapterPosition
                    deleteSensorCommand(item.model)
                }, "取消")
            }
        }
    }

    override fun createObserver() {
        super.createObserver()
        mInterfaceHomeViewModel.isEditable.observe(viewLifecycleOwner) { editable ->
            val list = binding.rv.bindingAdapter.models ?: return@observe
            list.forEach { item ->
                (item as MRSensorItem).isShowDel = editable
            }
            binding.rv.models = list
//            if (editable) {
//                binding.rv.bindingAdapter.addFooter(RVEmptyFooter(), animation = true)
//            } else {
//                binding.rv.bindingAdapter.removeFooterAt(animation = true)
//            }
        }
    }

    private fun showAddSensorPopup() {
        val sensorList = mInterfaceHomeViewModel.sensorConfig["485port1"] ?: listOf()
        val selectionPopupView = MR702SensorSelectionPopupView(requireContext())
        selectionPopupView.setData("请选择传感器类型", sensorList)
            .setSelectListener(object : MR702SensorSelectionPopupView.OnSelectListener {
                override fun onSelect(sensorModel: SensorModel) {
                   Toaster.show("选择了${sensorModel.modelName}")
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
            "mode=$model&del=1"
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
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
        testData()
    }

    private fun queryInfo() {
        commandItems.clear()

        var command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_RS485_PORT1_COLL)
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_RS485_PORT1_SENSOR, "index=0")
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        if (viewLifecycleOwner.lifecycle.currentState < androidx.lifecycle.Lifecycle.State.RESUMED) {
            return
        }
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_RS485_PORT1_COLL -> {
                val result = iotParseManager.parse<MRCollectionParam>(
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
                        val errMsg = "查询传感器出错: ${result.message}"
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
                            if (binding.rv.bindingAdapter.modelCount < SENSOR_SIZE) {
                                binding.rv.bindingAdapter.addFooter(
                                    RVEmptyFooter(),
                                    animation = true
                                )
                            } else {
                                binding.rv.bindingAdapter.removeFooterAt(animation = true)
                            }
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
    }

    private fun initSensorData(content: String) {
        lifecycleScope.launch {
            try {
                val sensorStatusList =
                    MoshiUtil.fromJson<List<MRSensorStatus>>(content) ?: return@launch
                val list = sensorStatusList.map { sensorStatus ->
                    val strs = sensorStatus.model.split("_").toTypedArray()
                    MRSensorItem(
                        sensorStatus.sta == "0",
                        mInterfaceHomeViewModel.sensorModelMap[strs[0]]?.modelName ?: "未知类型",
                        strs[0],
                        strs[1],
                    )
                }
                binding.rv.models = list
                if (binding.rv.bindingAdapter.modelCount < SENSOR_SIZE) {
                    binding.rv.bindingAdapter.addFooter(RVEmptyFooter(), animation = true)
                } else {
                    binding.rv.bindingAdapter.removeFooterAt(animation = true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        fun newInstance() = MR702RS485Port1Fragment()
        const val SENSOR_SIZE = 32
    }

    private fun testData() {
        val content =
            "[{\"model\": \"214_1\",\"sta\": \"0\"},{\"model\": \"214_2\",\"sta\": \"0\"},{\"model\": \"214_3\",\"sta\": \"2\"},{\"model\": \"214_3\",\"sta\": \"2\"}]"
        initSensorData(content)
    }
}