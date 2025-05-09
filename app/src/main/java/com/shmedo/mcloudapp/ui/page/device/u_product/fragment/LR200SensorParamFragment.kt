package com.shmedo.mcloudapp.ui.page.device.u_product.fragment

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentLr200SensorParamBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.u_product.dialog.LR200ZeroValueCalibrationPopupView
import com.shmedo.mcloudapp.ui.viewmodel.state.LR200SensorParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.LR200ZeroValueCalibrationViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject

/**
 * @author：gonghe
 * @time: 2024/5/11
 * @desc: LR200 一体式裂缝计传感器参数设置
 *
 */
class LR200SensorParamFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentLr200SensorParamBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: LR200SensorParamViewModel
    private lateinit var mCommandResponseStates: LR200ZeroValueCalibrationViewModel
    private val iotParseManager: IOTParserManager by inject()

    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
        mCommandResponseStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_lr200_sensor_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentLr200SensorParamBinding
        binding.llToolbar.toolbar.title = "传感设置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 零位校准
         */
        fun onZeroCalibrationClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            commandItems.clear()
            //先发送遥测指令
            var command = IOTCommandUtil.getCommand(
                IOTCommandType.SAMPLE
            )
            commandItems.add(command)

            //发送零位预设值的查询指令
            command = IOTCommandUtil.getCommand(
                IOTCommandType.MD_GET_LF_ZERO_VALUE
            )
            commandItems.add(command)

            showLoadingDialog(StringUtils.getString(R.string.loading))
            sendCommandFromCmdList(isStartTimeoutJob = true)
        }

        /**
         * 初始化
         */
        fun onSetInitialValueClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            val bundle = BaseIOTDeviceFragment.newBundleArguments(
                productType,
                communicateWay,
                deviceInfo,
                bleDevice
            )
            nav().safeNavigate(
                R.id.action_lR200SensorParamFragment_to_lR200InitialValueFragment,
                bundle
            )
        }
    }

    /**
     * 显示零值校准弹窗
     */
    private fun showZeroValueCalibrationPopup() {
        val popupView = LR200ZeroValueCalibrationPopupView(requireContext())
        popupView.setTitle("数据校准", mCommandResponseStates)
            .setClickListener(object : LR200ZeroValueCalibrationPopupView.OnClickListener {
                override fun onSettingClick() {
                    commandItems.clear()
                    val command = IOTCommandUtil.getCommand(
                        IOTCommandType.MD_SET_LF_ZERO_VALUE,
                        "datastreams=${mCommandResponseStates.zeroValueMeasured.get()}"
                    )
                    commandItems.add(command)
                    showLoadingDialog(StringUtils.getString(R.string.processing))
                    sendCommandFromCmdList(isStartTimeoutJob = true)
                }
            })
        XPopup.Builder(context)
            .dismissOnBackPressed(false) // 按返回键是否关闭弹窗，默认为true
            .dismissOnTouchOutside(false)// 点击外部是否关闭弹窗，默认为true
            .enableDrag(false)
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .asCustom(popupView)
            .show()
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.SAMPLE -> {//
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.SAMPLE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        //{"103_1":{"x":-1.32,"y":-4.06,"z":85.73},"203_1":0.00,"105_1":0.00}
                        //下面取出 203_1 对应的值赋值给 zeroValueMeasured：
                        val zeroValueMeasured =
                            MoshiUtil.fromJson<Map<String, Any>>(result.data)?.get("203_1")
                        mCommandResponseStates.zeroValueMeasured.set(zeroValueMeasured.toString())
                    }
                }
            }

            IOTCommandType.MD_GET_LF_ZERO_VALUE -> {//
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MD_GET_LF_ZERO_VALUE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            mCommandResponseStates.isResponseLoading.set(false)
                            mCommandResponseStates.isResponseSuccess.set(true)
                            mCommandResponseStates.isCalibratingSuccess.set(false)
                            mCommandResponseStates.zeroValue.set(result.data)
                            showZeroValueCalibrationPopup()
                        }
                    }
                }
            }


            IOTCommandType.MD_SET_LF_ZERO_VALUE -> {//设置
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "校准出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            mCommandResponseStates.isResponseLoading.set(false)
                            mCommandResponseStates.isResponseSuccess.set(true)
                            mCommandResponseStates.isCalibratingSuccess.set(true)
                        }
                    }
                }
            }


            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}