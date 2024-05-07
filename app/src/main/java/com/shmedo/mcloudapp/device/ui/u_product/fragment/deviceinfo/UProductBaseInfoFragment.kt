package com.shmedo.mcloudapp.device.ui.u_product.fragment.deviceinfo

import android.os.Bundle
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonCurrentStateInfo2
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.FragmentUProductBaseInfoBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.MRRunningDataItem
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.UProductBaseInfoViewModel
import com.shmedo.mcloudapp.utils.DeviceStatusHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class UProductBaseInfoFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUProductBaseInfoBinding
    private lateinit var mStates: UProductBaseInfoViewModel
    private val iotParseManager: IOTParserManager by inject()
    private val decimalFormat = DecimalFormat("#.###", DecimalFormatSymbols(Locale.getDefault()))
    private val deviceAbnormalList: ArrayList<String> = ArrayList()

    override fun initViewModel() {
        super.initViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_u_product_base_info, BR.stateVM, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUProductBaseInfoBinding
        initRefresh()
        initRunningDataAdapter()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryBaseInfo()
        }
    }

    private fun initRunningDataAdapter() {
        binding.rvRunningData.setup { rv ->
            rv.addItemDecoration(
                MyGridSpacingItemDecoration(
                    2,
                    ConvertUtils.dp2px(10f),
                    false
                )
            )
            addType<MRRunningDataItem>(R.layout.item_mr702_device_info_running_data)
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        fun onShowErrorModulesInfoClick() {
            showErrorModulesInfoDialog()
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 获取设备的基本信息
     */
    private fun queryBaseInfo() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS)
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_GET_DEVICE_STATUS -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MD_GET_DEVICE_STATUS
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询基本信息出错: ${result.message}"
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        val content: String = result.data
                        initStatusInfo(content)
                    }
                }
            }

            else -> {}
        }
    }

    private fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val commonCurrentStateInfoList = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<List<CommonCurrentStateInfo2>>(content)
                } ?: return@launchWithViewLifecycle

                if (commonCurrentStateInfoList.isEmpty()) {
                    Toaster.show("数据为空")
                    return@launchWithViewLifecycle
                }
                val info = commonCurrentStateInfoList[0]
                mStates.wrapStateInfo.set(info)
                mStates.wrapStateInfo.notifyChange()

                checkDeviceIsNormal(info)
                initRunningData(info)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun checkDeviceIsNormal(currentStateInfo: CommonCurrentStateInfo2) {
        deviceAbnormalList.clear()
        deviceAbnormalList.addAll(DeviceStatusHelper.checkDeviceAbnormal(currentStateInfo))
        mStates.deviceNormal.set(deviceAbnormalList.isEmpty())
    }

    private fun initRunningData(commonCurrentStateInfo: CommonCurrentStateInfo2) {
        try {
            val list = mutableListOf<MRRunningDataItem>()
            if (commonCurrentStateInfo.worktime != IOTConstants.NULL_KEY && commonCurrentStateInfo.worktime.isNotEmpty()) {
                list.add(
                    MRRunningDataItem(
                        "运行时间(小时)",
                        decimalFormat.format(commonCurrentStateInfo.worktime.toDouble() / 3600)
                    )
                )
            }
            if (commonCurrentStateInfo.emmcStorage != IOTConstants.NULL_KEY && commonCurrentStateInfo.emmcFree != IOTConstants.NULL_KEY
                && commonCurrentStateInfo.emmcStorage.isNotEmpty() && commonCurrentStateInfo.emmcFree.isNotEmpty()
            ) {
                decimalFormat.applyPattern("#.#")
                val free = commonCurrentStateInfo.emmcFree.replace("MB", "").toDoubleOrNull()?.let {
                    decimalFormat.format(it)
                } ?: ""

                val total =
                    commonCurrentStateInfo.emmcStorage.replace("MB", "").toDoubleOrNull()?.let {
                        decimalFormat.format(it)
                    } ?: ""

                list.add(
                    MRRunningDataItem(
                        "存储状态",
                        "${free}/${total}MB",
                    )
                )
            }

            binding.rvRunningData.models = list
            if (list.isNotEmpty())
                mStates.isRunningDataVisible.set(true)

        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun showErrorModulesInfoDialog() {
        if (deviceAbnormalList.isEmpty()) {
            Toaster.show("设备异常信息为空")
            return
        }
        XPopup.Builder(context)
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .enableDrag(false)
            .asCenterList(
                "异常信息", deviceAbnormalList.toTypedArray(),
                null, -1,
                null, 0, R.layout.custom_xpopup_adapter_abnormal_info
            )
            .show()
    }

    companion object {
        fun newInstance() = UProductBaseInfoFragment()
    }
}