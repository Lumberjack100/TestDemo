package com.shmedo.mcloudapp.ui.page.device.das.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.das.DasCollectorEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasCollectorInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.databinding.FragmentDasCollectorSettingBinding
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.DasCollectorSettingViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * @author：gonghe
 * @time: 2024/1/8
 * @desc: DAS 采集器参数配置页面
 *
 */
class DasCollectorSettingFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasCollectorSettingBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: DasCollectorSettingViewModel
    private val iotParseManager: IOTParserManager by inject()
    val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.getDefault()))


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_das_collector_setting,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDasCollectorSettingBinding
        binding.llToolbar.toolbar.title = "采集器参数"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryData()
        }
    }

    private fun setEditable(editable: Boolean) {
        toolbarViewModel.toolbarIvActionVisible.set(!editable)
        toolbarViewModel.toolbarTvActionVisible.set(editable)
        mStates.isEditable.set(editable)
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onToolbarIvClick() {
            setEditable(true)
        }

        override fun onToolbarTvClick() {
            setEditable(false)
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

    private fun initSaveCommand() {
        if (mStates.collectorAddress.get().isEmpty()) {
            showMessageDialog("请输入采集器地址!")
            return
        }
        try {
            val value = mStates.collectorAddress.get().toDouble()
            if (value < 0 || value > 255) {
                showMessageDialog("采集器地址数值范围[0,255]!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的采集器地址!")
            return
        }
        if (mStates.solvingInterval.get().isEmpty()) {
            showMessageDialog("请输入解算间隔!")
            return
        }
        if (mStates.standbyTime.get().isEmpty()) {
            showMessageDialog("请输入待机时长!")
            return
        }
        if (mStates.collectionInterval.get().isEmpty()) {
            showMessageDialog("请输入采集间隔!")
            return
        }
        if (mStates.isShowSensitivity.get()) {
            if (mStates.sensitivity.get().isEmpty()) {
                showMessageDialog("请输入灵敏度!")
                return
            }
            try {
                val value = mStates.sensitivity.get().toDouble()
                if (value < 30 || value > 150) {
                    showMessageDialog("灵敏度数值范围[30,150]!")
                    return
                }
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的灵敏度!")
                return
            }
        }

        val entity = DasCollectorEntity(
            type = mStates.infoWrapper.get().type,
            addr = mStates.collectorAddress.get(),
            collgap = mStates.collectionInterval.get(),
            calcgap = mStates.solvingInterval.get(),
            standbygap = mStates.standbyTime.get(),
            sensitivity = if (mStates.isShowSensitivity.get()) mStates.sensitivity.get() else IOTConstants.NULL_KEY
        )
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_SET_COLLECTOR_CONTROL,
            entity.toCommandString()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }


    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_GET_COLLECTOR_CONTROL
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.DAS_MD_GET_COLLECTOR_CONTROL -> {
                val result = iotParseManager.parse<DasCollectorInfo>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_COLLECTOR_CONTROL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询采集器参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        //设备版本不支持，隐藏编辑按钮
                        toolbarViewModel.toolbarIvActionVisible.set(!errMsg.contains("设备版本不支持"))
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initParamData(result.data)
                    }
                }
            }

            IOTCommandType.DAS_MD_SET_COLLECTOR_CONTROL -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置采集器参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("数据保存成功")
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initParamData(collectorInfo: DasCollectorInfo) {
        try {
            mStates.infoWrapper.set(collectorInfo)
            mStates.collectorAddress.set(collectorInfo.addr)
            mStates.solvingInterval.set(collectorInfo.calcgap)
            mStates.standbyTime.set(collectorInfo.standbygap)
            mStates.collectionInterval.set(collectorInfo.collgap)
            mStates.sensitivity.set(collectorInfo.sensitivity)
            mStates.isShowSensitivity.set(collectorInfo.sensitivity != IOTConstants.NULL_KEY)
            if (mStates.isShowSensitivity.get()) {
                collectorInfo.sensitivity.toDoubleOrNull()?.let {
                    mStates.sensitivity.set(decimalFormat.format(it))
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}