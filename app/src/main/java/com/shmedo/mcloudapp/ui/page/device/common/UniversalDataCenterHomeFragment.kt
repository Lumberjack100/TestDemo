package com.shmedo.mcloudapp.ui.page.device.common

import android.os.Bundle
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.BindingAdapter.BindingViewHolder
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.CenterNumberEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.das.DasDataReportEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ServerFive
import com.shmedo.lib.cmd.base.iot_cmd.enums.ServerFour
import com.shmedo.lib.cmd.base.iot_cmd.enums.ServerOne
import com.shmedo.lib.cmd.base.iot_cmd.enums.ServerThree
import com.shmedo.lib.cmd.base.iot_cmd.enums.ServerTwo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.common.DataCenterStatus
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasDataReportInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.ItemBeidouDataTransmissionBinding
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.BeidouDataTransmissionItem
import com.shmedo.mcloudapp.model.DataCenterStatusItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.ParamSubmitButtonItem

/**
 * @author：gonghe
 * @time: 2025/6/19
 * @desc: 通用数据中心列表页面 - 支持4G和蓝牙两种通讯方式
 *
 */
class UniversalDataCenterHomeFragment : BaseDataCenterHomeFragment() {
    private var beidouDataTransmissionItem: BeidouDataTransmissionItem =
        BeidouDataTransmissionItem()

    /**
     * 是否需要配置上报间隔
     */
    private fun isNeedReportInterval(): Boolean {
        return productType == ProductType.GNSS_M_1
                || productType == ProductType.GNSS_M_2
                || productType == ProductType.U_I_1
                || productType == ProductType.U_R_1
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        // 扩展适配器支持 BeidouDataTransmissionItem
        binding.recyclerView.bindingAdapter.addType<BeidouDataTransmissionItem>(R.layout.item_beidou_data_transmission)
    }

    override fun initRecyclerViewAdapterData() {
        val groupList = mutableListOf<Any>()

        // 如果支持上报间隔，在数据链路列表前添加北斗数传配置项
        if (isNeedReportInterval()) {
            groupList.add(DeviceStatusInfoGroupItem("上报周期"))
            groupList.add(beidouDataTransmissionItem)
            groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        }
        groupList.addAll(getAdapterData())

        if (isNeedReportInterval()) {
            groupList.add(
                GapItem(
                    height = ConvertUtils.dp2px(60f)
                )
            )
            groupList.add(
                ParamSubmitButtonItem(
                    btnText = "确定",
                )
            )
        }

        binding.recyclerView.bindingAdapter.models = groupList
    }

    override fun BindingViewHolder.processOtherItemViewBind(itemViewType: Int) {
        if (itemViewType == R.layout.item_beidou_data_transmission) {
            val binding = getBinding<ItemBeidouDataTransmissionBinding>()
            val beidouItem = getModel<BeidouDataTransmissionItem>()

            // 设置数据绑定参数
            binding.setVariable(BR.m, beidouItem)
            binding.executePendingBindings()
        }
    }

    override fun isTargetCommandType(commandType: IOTCommandType): Boolean =
        super.isTargetCommandType(commandType)
                || (commandType == IOTCommandType.MD_GET_DATA_REPORT_TIME)
                || (commandType == IOTCommandType.MD_SET_DATA_REPORT_TIME)


    override fun getNavigationActionId(): Int {
        return R.id.action_global_dataCenterParamFragment
    }

    override fun queryData() {
        commandItems.clear()

        //获取上报时间信息
        if (isNeedReportInterval()) {
            val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DATA_REPORT_TIME)
            commandItems.add(command)
        }

        for (i in 1..centerNum) {
            val entity = CenterNumberEntity(i.toString())
            val command =
                IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DATA_CENTER_STATUS, entity)
            commandItems.add(command)
        }

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun handleFragmentResult(bundle: Bundle) {
        val centerNumber =
            bundle.getInt(AppContants.Extras.REFRESH_DATA_CENTER_STATUS, ServerOne.centerId)

        commandItems.clear()
        val entity = CenterNumberEntity(centerNumber.toString())
        val command =
            IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DATA_CENTER_STATUS, entity)
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.loading))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun initSaveCommand() {
        if (beidouDataTransmissionItem.getReportIntervalStr().isEmpty()) {
            showMessageDialog("请输入上报间隔!")
            return
        }
        commandItems.clear()

        val entity = DasDataReportEntity(
            report_intv = beidouDataTransmissionItem.getReportIntervalStr()
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_DATA_REPORT_TIME,
            entity.toCommandString()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DATA_REPORT_TIME -> {
                val result = iotParseManager.parse<DasDataReportInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_DATA_REPORT_TIME
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询上报间隔出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initDataReportTime(result.data)
                    }
                }
            }

            IOTCommandType.MD_GET_DATA_CENTER_STATUS -> {
                val result = iotParseManager.parse<DataCenterStatus>(
                    cmdStr,
                    IOTCommandType.MD_GET_DATA_CENTER_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询数据链路状态出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initDataCenterStatus(result.data)
                    }
                }
            }

            IOTCommandType.MD_SET_DATA_REPORT_TIME -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            processNavigateUp()
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initDataReportTime(dataReportInfo: DasDataReportInfo) {
        beidouDataTransmissionItem.setReportInterval(dataReportInfo.report_intv)
    }

    private fun initDataCenterStatus(dataCenterStatus: DataCenterStatus) {
        when (dataCenterStatus.centerid) {
            ServerOne.centerId -> {
                binding.recyclerView.models?.filterIsInstance<DataCenterStatusItem>()
                    ?.findLast { it.name.contains("数据链路1") }
                    ?.refreshStatus(dataCenterStatus.status)
            }

            ServerTwo.centerId -> {
                binding.recyclerView.models?.filterIsInstance<DataCenterStatusItem>()
                    ?.findLast { it.name.contains("数据链路2") }
                    ?.refreshStatus(dataCenterStatus.status)
            }

            ServerThree.centerId -> {
                binding.recyclerView.models?.filterIsInstance<DataCenterStatusItem>()
                    ?.findLast { it.name.contains("数据链路3") }
                    ?.refreshStatus(dataCenterStatus.status)
            }

            ServerFour.centerId -> {
                binding.recyclerView.models?.filterIsInstance<DataCenterStatusItem>()
                    ?.findLast { it.name.contains("数据链路4") }
                    ?.refreshStatus(dataCenterStatus.status)
            }

            ServerFive.centerId -> {
                binding.recyclerView.models?.filterIsInstance<DataCenterStatusItem>()
                    ?.findLast { it.name.contains("数据链路5") }
                    ?.refreshStatus(dataCenterStatus.status)
            }
        }
    }
}