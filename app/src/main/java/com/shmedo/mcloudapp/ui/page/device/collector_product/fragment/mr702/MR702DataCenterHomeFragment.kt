package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702

import com.drake.brv.utils.models
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRDataCenterStatus
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.model.DataCenterStatusItem
import com.shmedo.mcloudapp.ui.page.device.common.BaseDataCenterHomeFragment

/**
 * @author：gonghe
 * @time: 2025/6/19
 * @desc: 遥测终端机(MR702)数据中心列表页面 - 支持4G和蓝牙两种通讯方式
 *
 */
class MR702DataCenterHomeFragment : BaseDataCenterHomeFragment() {
    override fun getNavigationActionId(): Int {
        return R.id.action_global_to_mR702DataCenterParamFragment
    }

    override fun queryData() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_DATA_CENTER_STATUS)
        commandItems.add(command)

        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MR_MD_GET_DATA_CENTER_STATUS -> {
                val result = iotParseManager.parse<MRDataCenterStatus>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_DATA_CENTER_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询数据链路状态出错: ${result.message}"
                        handleFailureResult(errMsg)
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

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initDataCenterStatus(dataCenterStatus: MRDataCenterStatus) {
        binding.recyclerView.models?.filterIsInstance<DataCenterStatusItem>()
            ?.findLast { it.name.contains("数据链路1") }?.refreshStatus(dataCenterStatus.status1)

        binding.recyclerView.models?.filterIsInstance<DataCenterStatusItem>()
            ?.findLast { it.name.contains("数据链路2") }?.refreshStatus(dataCenterStatus.status2)

        binding.recyclerView.models?.filterIsInstance<DataCenterStatusItem>()
            ?.findLast { it.name.contains("数据链路3") }?.refreshStatus(dataCenterStatus.status3)

        binding.recyclerView.models?.filterIsInstance<DataCenterStatusItem>()
            ?.findLast { it.name.contains("数据链路4") }?.refreshStatus(dataCenterStatus.status4)

        binding.recyclerView.models?.filterIsInstance<DataCenterStatusItem>()
            ?.findLast { it.name.contains("数据链路5") }?.refreshStatus(dataCenterStatus.status5)
    }

    companion object {
        const val REFRESH_DATA = "refresh_data"
    }
}