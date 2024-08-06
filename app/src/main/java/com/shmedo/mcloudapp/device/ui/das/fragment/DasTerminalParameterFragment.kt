package com.shmedo.mcloudapp.device.ui.das.fragment

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.das.DasReportEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasReportInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessageDialog
import com.shmedo.mcloudapp.databinding.FragmentDasTerminalParameterBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.DasTerminalParameterViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import java.util.Locale

class DasTerminalParameterFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasTerminalParameterBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: DasTerminalParameterViewModel
    private val iotParseManager: IOTParserManager by inject()
    private val reportMethodList: MutableList<String> = arrayListOf("固定间隔上报", "定时定点上报")


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_das_terminal_parameter, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDasTerminalParameterBinding
        binding.llToolbar.toolbar.title = "上报方式"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
                nav().navigateUp()
            }
        })
    }

    override fun initData() {
        super.initData()
        mStates.reportMethod.set(reportMethodList[0])
        mStates.startTime.set("0")
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 上报方式
         */
        fun onReportingMethodClick() {
            val selectedIndex = reportMethodList.indexOf(mStates.reportMethod.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", reportMethodList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.reportMethod.set(text)
                        mStates.isStartTimeItemVisible.set(position == 1)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 上报起始时间
         */
        fun onReportingStartTimeClick() {
            TimePickerDialog(
                mActivity,
                { view, hourOfDay, minute ->
                    val time = String.format(Locale.getDefault(), "%2d", hourOfDay)
                    mStates.startTime.set(time)
                }, 0, 0, true
            ).show()
        }

        fun onSubmitClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun initSaveCommand() {
        commandItems.clear()
        if (mStates.reportMethod.get().contains("定时定点") && mStates.startTime.get()
                .isEmpty()
        ) {
            showMessageDialog("请选择上报起始时间!")
            return
        }

        if (mStates.interval.get().isEmpty()) {
            showMessageDialog("请输入上报时间间隔!")
            return
        }
        val entity = DasReportEntity(
            type = (reportMethodList.indexOf(mStates.reportMethod.get())).toString(),
            timepoint = mStates.startTime.get(),
            timegap = mStates.interval.get(),
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_SET_DATA_REPORT_TYPE,
            entity.toCommandString()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        queryData()
    }

    private fun queryData() {
        commandItems.clear()

        val command = IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_DATA_REPORT_TYPE)
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.loading))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.DAS_MD_GET_DATA_REPORT_TYPE -> {
                val result = iotParseManager.parse<DasReportInfo>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_DATA_REPORT_TYPE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询上报参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        initReportMethod(result.data)
                    }
                }
            }

            IOTCommandType.DAS_MD_SET_DATA_REPORT_TYPE -> {
//                setEditable(false)
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "上报参数设置出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            Toaster.show("保存成功")
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initReportMethod(info: DasReportInfo) {
        mStates.isStartTimeItemVisible.set(info.type.toInt() == 1)
        mStates.reportMethod.set(
            if (info.type.toInt() == 0) {
                reportMethodList[0]
            } else {
                reportMethodList[1]
            }
        )
        mStates.startTime.set(info.timepoint)
        mStates.interval.set(info.timegap)
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}