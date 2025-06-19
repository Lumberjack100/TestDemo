package com.shmedo.mcloudapp.ui.page.device.common

import android.os.Bundle
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.bindingAdapter
import com.hjq.toast.Toaster
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.CenterNumberEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.das.DasDataReportEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
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
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.DataCenterStatusItem

/**
 * @author：gonghe
 * @time: 2025/6/19
 * @desc: 数据中心列表页面 - 支持4G和蓝牙两种通讯方式
 *
 */
class UniversalDataCenterHomeFragment : BaseDataCenterHomeFragment() {

    override fun getNavigationActionId(): Int {
        return R.id.action_global_dataCenterParamFragment
    }

    override fun queryData() {
        commandItems.clear()

        //获取上报时间信息
        if (mStates.isSupportedReportInterval.get()) {
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

    override fun getClickProxy(): BaseClickProxy {
        return ClickProxy()
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun initSaveCommand() {
        if (mStates.reportInterval.get().isEmpty()) {
            showMessageDialog("请输入上报间隔!")
            return
        }
        commandItems.clear()
        val entity = DasDataReportEntity(
            report_intv = mStates.reportInterval.get()
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
        mStates.reportInterval.set(dataReportInfo.report_intv)
        //添加这行来保存初始状态
        mStates.saveInitialState()
    }

    private fun initDataCenterStatus(dataCenterStatus: DataCenterStatus) {
        when (dataCenterStatus.centerid) {
            ServerOne.centerId -> {
                binding.recyclerView.bindingAdapter.getModel<DataCenterStatusItem>(0)
                    .refreshStatus(dataCenterStatus.status)
            }

            ServerTwo.centerId -> {
                binding.recyclerView.bindingAdapter.getModel<DataCenterStatusItem>(1)
                    .refreshStatus(dataCenterStatus.status)
            }

            ServerThree.centerId -> {
                binding.recyclerView.bindingAdapter.getModel<DataCenterStatusItem>(2)
                    .refreshStatus(dataCenterStatus.status)
            }

            ServerFour.centerId -> {
                binding.recyclerView.bindingAdapter.getModel<DataCenterStatusItem>(3)
                    .refreshStatus(dataCenterStatus.status)
            }

            ServerFive.centerId -> {
                binding.recyclerView.bindingAdapter.getModel<DataCenterStatusItem>(4)
                    .refreshStatus(dataCenterStatus.status)
            }
        }
    }
}