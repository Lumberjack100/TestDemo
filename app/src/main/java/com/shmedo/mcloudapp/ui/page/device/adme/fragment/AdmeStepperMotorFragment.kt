package com.shmedo.mcloudapp.ui.page.device.adme.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.adme.AdmeStepperMotorEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeStepperMotorInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentAdmeStepperMotorBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.AdmeStepperMotorViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * @author：gonghe
 * @time: 2023/12/14
 * @desc:  ADME 步进电机参数配置页面
 *
 */
class AdmeStepperMotorFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAdmeStepperMotorBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: AdmeStepperMotorViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_adme_stepper_motor,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAdmeStepperMotorBinding
        binding.llToolbar.toolbar.title = "步进电机参数"
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
        if (mStates.accuracyCorrectionValue.get().isEmpty()) {
            showMessageDialog("请输入绝对精度修正值!")
            return
        }
        try {
            val value = mStates.accuracyCorrectionValue.get().toDouble()
            if (value < 0) {
                showMessageDialog("请输入正确的绝对精度修正值!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的绝对精度修正值!")
            return
        }

        if (mStates.movementSpeed.get().isEmpty()) {
            showMessageDialog("请输入步进电机运动速度!")
            return
        }
        try {
            val value: Int = mStates.movementSpeed.get().toInt()
            if (value < 1 || value > 600) {
                showMessageDialog("步进电机运动速度范围[1,600]!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("步进电机运动速度范围[1,600]!")
            return
        }

        if (mStates.motorTorque.get().isEmpty()) {
            showMessageDialog("请输入步进电机力矩!")
            return
        }
        try {
            val value = mStates.motorTorque.get().toInt()
            if (value < 0) {
                showMessageDialog("请输入正确的步进电机力矩!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的步进电机力矩!")
            return
        }
        val entity = AdmeStepperMotorEntity(
            posnegtest = "1",
            absprsion = mStates.accuracyCorrectionValue.get(),
            movspeed = mStates.movementSpeed.get(),
            movesm = mStates.motorTorque.get()
        )

        commandItems.clear()
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_SET_STEPPER_MOTOR,
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
            IOTCommandType.ADME_MD_GET_STEPPER_MOTOR
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.ADME_MD_GET_STEPPER_MOTOR -> {
                val result = iotParseManager.parse<AdmeStepperMotorInfo>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_STEPPER_MOTOR
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询步进电机参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList{
                            binding.refreshLayout.finish()
                        }
                        initParamData(result.data)
                    }
                }
            }

            IOTCommandType.ADME_MD_SET_STEPPER_MOTOR -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置步进电机参数出错: ${result.message}"
                        handleFailureResult(errMsg)
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
                        val errMsg = "保存出错: ${result.message}"
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

    private fun initParamData(info: AdmeStepperMotorInfo) {
        val decimalFormat = DecimalFormat("#.##", DecimalFormatSymbols(Locale.getDefault()))
        try {
            mStates.accuracyCorrectionValue.set(decimalFormat.format(info.absprsion.toDouble()))
            mStates.movementSpeed.set(info.movspeed)
            mStates.motorTorque.set(info.movesm)
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