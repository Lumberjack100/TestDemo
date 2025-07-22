package com.shmedo.mcloudapp.ui.page.device.gnss_product.fragment.m50

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.drake.brv.BindingAdapter.BindingViewHolder
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.CenterNumberEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.DataReportTypeEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ServerFive
import com.shmedo.lib.cmd.base.iot_cmd.enums.ServerFour
import com.shmedo.lib.cmd.base.iot_cmd.enums.ServerOne
import com.shmedo.lib.cmd.base.iot_cmd.enums.ServerThree
import com.shmedo.lib.cmd.base.iot_cmd.enums.ServerTwo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.common.DataCenterStatus
import com.shmedo.lib.cmd.base.iot_cmd.model.common.DataReportType
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.ItemDataReportingPeriodBinding
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.DataCenterStatusItem
import com.shmedo.mcloudapp.model.DataReportingPeriodItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.ParamSubmitButtonItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDataCenterHomeFragment
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/7/21
 * @desc: 一体式自供电 GNSS 接收机(M50)数据中心列表页面 - 支持4G和蓝牙两种通讯方式
 *
 */
class M50DataCenterHomeFragment : BaseDataCenterHomeFragment() {
    private val reportMethodList = listOf("固定间隔上报", "定时定点上报")
    private val reportStartTimeList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_report_start_time) }

    private var dataReportingPeriodItem: DataReportingPeriodItem = DataReportingPeriodItem()


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        // 扩展适配器支持 DataReportingPeriodItem
        binding.recyclerView.bindingAdapter.addType<DataReportingPeriodItem>(R.layout.item_data_reporting_period)
    }

    override fun initRecyclerViewAdapterData() {
        val groupList = mutableListOf<Any>()

        groupList.add(DeviceStatusInfoGroupItem("上报周期"))
        groupList.add(dataReportingPeriodItem)
        groupList.add(GapItem(height = ConvertUtils.dp2px(12f)))
        groupList.addAll(getAdapterData())

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

        binding.recyclerView.bindingAdapter.models = groupList
    }

    override fun BindingViewHolder.processOtherItemViewBind(itemViewType: Int) {
        if (itemViewType == R.layout.item_data_reporting_period) {
            val binding = getBinding<ItemDataReportingPeriodBinding>()
            val item = getModel<DataReportingPeriodItem>()

            // 设置数据绑定参数
            binding.setVariable(BR.m, item)
            binding.setVariable(BR.click, M50ClickProxy())
            binding.executePendingBindings()
        }
    }

    override fun isTargetCommandType(commandType: IOTCommandType): Boolean =
        super.isTargetCommandType(commandType)
                || (commandType == IOTCommandType.MD_GET_DATA_REPORT_TYPE)
                || (commandType == IOTCommandType.MD_SET_DATA_REPORT_TYPE)


    override fun getNavigationActionId(): Int {
        return R.id.action_global_dataCenterParamFragment
    }

    override fun queryData() {
        commandItems.clear()

        //获取上报周期信息
        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DATA_REPORT_TYPE)
        commandItems.add(command)

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

    inner class M50ClickProxy {
        /**
         * 上报方式
         */
        fun onReportingMethodClick() {
            val selectedIndex =
                reportMethodList.indexOf(dataReportingPeriodItem.getReportMethodStr())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择上报方式", reportMethodList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        dataReportingPeriodItem.setReportMethod(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 上报起始时间
         */
        fun onReportingStartTimeClick() {
//            XPopup.Builder(context)
//                .hasShadowBg(false)
//                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.4f).toInt())
//                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
//                .atView(binding.tvStartTime) // 依附于所点击的View，内部会自动判断在上方或者下方显示
//                .asAttachList(reportStartTimeList, null, { _, text ->
//                    dataReportingPeriodItem.setReportStartTimeHour(text)
//                }, 0, 0)
//                .show()

            val selectedIndex =
                reportStartTimeList.indexOf(dataReportingPeriodItem.getReportStartTimeHourStr())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择起始时间", reportStartTimeList,
                    null, selectedIndex,
                    { position, text ->
                        dataReportingPeriodItem.setReportStartTimeHour(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }
    }

    override fun initSaveCommand() {
        if (dataReportingPeriodItem.getReportMethodStr().contains("定时定点")) {
            if (dataReportingPeriodItem.getReportStartTimeHourStr().isEmpty()) {
                showMessageDialog("请选择起始时间（小时）!")
                return
            }
            if (dataReportingPeriodItem.getReportStartTimeMinuteStr().isEmpty()) {
                showMessageDialog("请输入起始时间（分钟）!")
                return
            }
            try {
                val value = dataReportingPeriodItem.getReportStartTimeMinuteStr().toDouble()
                if (value < 5 || value > 60) {
                    showMessageDialog("起始时间（分钟）数值范围[5,60]!")
                    return
                }
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的起始时间（分钟）!")
                return
            }

        } else {
            if (dataReportingPeriodItem.getReportIntervalStr().isEmpty()) {
                showMessageDialog("请输入时间间隔（分钟）!")
                return
            }
            try {
                val value = dataReportingPeriodItem.getReportIntervalStr().toDouble()
                if (value < 5 || value > 1440) {
                    showMessageDialog("时间间隔（分钟）数值范围[5,1440]!")
                    return
                }
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的时间间隔（分钟）!")
                return
            }
        }

        commandItems.clear()

        // 优化 timehour 参数：从时间字符串中提取小时数值，而不是使用索引
        val timehourValue = if (dataReportingPeriodItem.getReportMethodStr().contains("定时定点")) {
            try {
                // 从时间字符串（如 "08:00"）中提取小时数值（如 8）
                val timeStr = dataReportingPeriodItem.getReportStartTimeHourStr()
                val hourStr = timeStr.split(":")[0]
                hourStr.toInt().toString()
            } catch (ex: Exception) {
                Timber.e(ex, "解析起始时间小时失败")
                IOTConstants.NULL_KEY
            }
        } else {
            IOTConstants.NULL_KEY
        }

        val entity = DataReportTypeEntity(
            type = reportMethodList.indexOf(dataReportingPeriodItem.getReportMethodStr())
                .toString(),
            timehour = timehourValue,
            timemin = if (dataReportingPeriodItem.getReportMethodStr()
                    .contains("定时定点")
            ) dataReportingPeriodItem.getReportStartTimeMinuteStr() else IOTConstants.NULL_KEY,
            timegap = if (dataReportingPeriodItem.getReportMethodStr()
                    .contains("定时定点")
            ) IOTConstants.NULL_KEY else dataReportingPeriodItem.getReportIntervalStr()
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_DATA_REPORT_TYPE,
            entity.toCommandString()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DATA_REPORT_TYPE -> {
                val result = iotParseManager.parse<DataReportType>(
                    cmdStr,
                    IOTCommandType.MD_GET_DATA_REPORT_TYPE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询上报周期出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initDataReportInfo(result.data)
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

            IOTCommandType.MD_SET_DATA_REPORT_TYPE -> {
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

    private fun initDataReportInfo(dataReportType: DataReportType) {
        try {
            dataReportType.type.toIntOrNull()?.let {
                if (it in reportMethodList.indices) {
                    dataReportingPeriodItem.setReportMethod(reportMethodList[it])
                }
            }
            dataReportType.timehour.toIntOrNull()?.let { hourValue ->
                // 根据小时数值查找对应的时间字符串（如 8 -> "08:00"）
                val timeStr = String.format("%02d:00", hourValue)
                if (timeStr in reportStartTimeList) {
                    dataReportingPeriodItem.setReportStartTimeHour(timeStr)
                }
            }
            dataReportingPeriodItem.setReportStartTimeMinute(dataReportType.timemin)
            dataReportingPeriodItem.setReportInterval(dataReportType.timegap)
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
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