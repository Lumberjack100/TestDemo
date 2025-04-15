package com.shmedo.mcloudapp.ui.page.device.lb20s.fragment

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.BroadcastEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentLb20sCustomAlarmTestBinding
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.LB20SCustomAlarmTestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessage
import org.koin.android.ext.android.inject

/**
 * @author：gonghe
 * @time: 2024/5/11
 * @desc: 无线预警广播自定义报警语音测试
 *
 */
class LB20SCustomAlarmTestFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentLb20sCustomAlarmTestBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: LB20SCustomAlarmTestViewModel
    private val iotParseManager: IOTParserManager by inject()

    private val broadcastNumList = (1..10).map { it.toString() }

    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_lb20s_custom_alarm_test,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentLb20sCustomAlarmTestBinding
        binding.llToolbar.toolbar.title = "自定义播报内容"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
    }

    override fun initData() {
        super.initData()
        mStates.broadcastNum.set(broadcastNumList[2])
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 选择播报遍数
         */
        fun onBroadcastNumChooseClick() {
            val selectedIndex = broadcastNumList.indexOf(mStates.broadcastNum.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", broadcastNumList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.broadcastNum.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            showMessage(
                "是否发送报警语音测试？",
                "温馨提示",
                "确定",
                {
                    initSaveCommand()
                },
                "取消"
            )
        }
    }


    private fun initSaveCommand() {
        commandItems.clear()
        val entity = BroadcastEntity(
            b_num = mStates.broadcastNum.get(),
            b_size = "99",
            b_content = mStates.broadcastContent.get()
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.BROADCAST,
            entity.toCommandString()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.BROADCAST -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "播报出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("播报成功")
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