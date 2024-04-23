package com.shmedo.mcloudapp.device.ui.adme.fragment

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.adme.AdmeCalibrationProcessingEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeCalibrationProcessingInfo
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessageDialog
import com.shmedo.mcloudapp.databinding.FragmentAdmeSportsCalibrationProcessingBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.AdmeSportsCalibrationProcessingViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/2/26
 * @desc: 运动检校处理
 *
 */
class AdmeSportsCalibrationProcessingFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAdmeSportsCalibrationProcessingBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: AdmeSportsCalibrationProcessingViewModel
    private val iotParseManager: IOTParserManager by inject()


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_adme_sports_calibration_processing,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAdmeSportsCalibrationProcessingBinding
        binding.llToolbar.toolbar.title = "运动检校处理"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        toolbarViewModel.toolbarIvActionVisible.set(mMessenger.admeDeviceMode.get() == "0")
        toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_device_param_edit)
        toolbarViewModel.toolbarTvActionText.set("取消")
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryData()
        }
    }

    override fun initData() {
        super.initData()

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

        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
//            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
//                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
//                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
//                return
//            }
            when (button.id) {
                R.id.sportsCalibrationSB -> {
                    mStates.isSportsCalibration.set(isChecked)
                }

                R.id.kValueCalibrationSB -> {
                    mStates.isKValueCalibration.set(isChecked)
                }

                R.id.dataProcessingSB -> {
                    mStates.isDataProcessing.set(isChecked)
                }
            }
        }

        fun onSubmitClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun initSaveCommand() {
        if (mStates.isSportsCalibration.get()) {
            if (mStates.zeroDifference.get().isEmpty()) {
                showMessageDialog("请输入归零差!")
                return
            }
            try {
                val value = mStates.zeroDifference.get().toDouble()
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的归零差!")
                return
            }

            if (mStates.positioningErrorInterval.get().isEmpty()) {
                showMessageDialog("请输入定位误差区间!")
                return
            }
            try {
                val value = mStates.positioningErrorInterval.get().toDouble()
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的定位误差区间!")
                return
            }

            if (mStates.tryNumber.get().isEmpty()) {
                showMessageDialog("请输入运动检校尝试次数!")
                return
            }
        }
        if (mStates.isKValueCalibration.get()) {
            if (mStates.kValueThreshold.get().isEmpty()) {
                showMessageDialog("请输入K值阈值!")
                return
            }
            try {
                val value = mStates.kValueThreshold.get().toDouble()
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的K值阈值!")
                return
            }

            if (mStates.middleErrorThreshold.get().isEmpty()) {
                showMessageDialog("请输入中误差阈值!")
                return
            }
            try {
                val value = mStates.middleErrorThreshold.get().toDouble()
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的中误差阈值!")
                return
            }

            if (mStates.kTryNumber.get().isEmpty()) {
                showMessageDialog("请输入K值检校尝试次数!")
                return
            }
        }
        if (mStates.isDataProcessing.get()) {
            if (mStates.accumulatedDifference.get().isEmpty()) {
                showMessageDialog("请输入累积值差!")
                return
            }
            try {
                val value = mStates.accumulatedDifference.get().toDouble()
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的累积值差!")
                return
            }
        }
        val entity = AdmeCalibrationProcessingEntity(
            if (mStates.isSportsCalibration.get()) "1" else "0",
            if (mStates.isSportsCalibration.get()) mStates.zeroDifference.get() else IOTConstants.NULL_KEY,
            if (mStates.isSportsCalibration.get()) mStates.positioningErrorInterval.get() else IOTConstants.NULL_KEY,
            if (mStates.isSportsCalibration.get()) mStates.tryNumber.get() else IOTConstants.NULL_KEY,

            if (mStates.isKValueCalibration.get()) "1" else "0",
            if (mStates.isKValueCalibration.get()) mStates.kValueThreshold.get() else IOTConstants.NULL_KEY,
            if (mStates.isKValueCalibration.get()) mStates.middleErrorThreshold.get() else IOTConstants.NULL_KEY,
            if (mStates.isKValueCalibration.get()) mStates.kTryNumber.get() else IOTConstants.NULL_KEY,

            if (mStates.isDataProcessing.get()) "1" else "0",
            if (mStates.isDataProcessing.get()) mStates.accumulatedDifference.get() else IOTConstants.NULL_KEY,
        )
        commandItems.clear()
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_SET_CALIBRATION_PROCESSING,
            entity.toCommandString()
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_SAVE_CONFIG_PARAM)
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
            IOTCommandType.ADME_MD_GET_CALIBRATION_PROCESSING
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.ADME_MD_GET_CALIBRATION_PROCESSING -> {
                val result = iotParseManager.parse<AdmeCalibrationProcessingInfo>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_CALIBRATION_PROCESSING
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
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

            IOTCommandType.ADME_MD_SET_CALIBRATION_PROCESSING -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            IOTCommandType.MD_SAVE_CONFIG_PARAM -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "保存出错: ${result.message}"
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

    private fun initParamData(info: AdmeCalibrationProcessingInfo) {
        try {
            mStates.isSportsCalibration.set(info.rivswitch == "1")
            mStates.zeroDifference.set(info.zerodiffer)
            mStates.positioningErrorInterval.set(info.posterrorint)
            mStates.tryNumber.set(info.rtrynum)

            mStates.isKValueCalibration.set(info.kcswitch == "1")
            mStates.kValueThreshold.set(info.kthreshold)
            mStates.middleErrorThreshold.set(info.methreshold)
            mStates.kTryNumber.set(info.ktrynum)

            mStates.isDataProcessing.set(info.dpswitch == "1")
            mStates.accumulatedDifference.set(info.accudiff)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}