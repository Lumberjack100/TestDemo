package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRDataReportTypeEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRDataReportType
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.communication.model.CommandSequenceConfig
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.communication.model.ErrorHandlingStrategy
import com.shmedo.mcloudapp.databinding.FragmentMr702ReportConfigBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702ReportConfigViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/6/5
 * @desc:  遥测终端机上报参数配置页面
 *
 */
class MR702ReportConfigFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentMr702ReportConfigBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: MR702ReportConfigViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val reportMethodList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_report_method) }
    private val reportStartTimeList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_report_start_time) }


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_report_config, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702ReportConfigBinding
        binding.llToolbar.toolbar.title = "上报配置"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            handleBackByCheckDataModified()
        }
        registerOnBackPressedDispatcher {
            handleBackByCheckDataModified()
        }
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_refresh_fail_warn))
                finishRefresh()
                return@onRefresh
            }
            queryData()
        }
    }

    override fun initData() {
        super.initData()
        resetDefaultParams()
        // 保存初始状态
        mStates.saveInitialState()
    }

    private fun resetDefaultParams() {
        mStates.reportMethod.set(reportMethodList[0])
        mStates.startTime.set(reportStartTimeList[0])
        mStates.interval.set("")
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
                    "请选择上报方式", reportMethodList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.reportMethod.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 上报起始时间
         */
        fun onReportingStartTimeClick() {
            XPopup.Builder(context)
                .hasShadowBg(false)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.4f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .atView(binding.tvStartTime) // 依附于所点击的View，内部会自动判断在上方或者下方显示
                .asAttachList(reportStartTimeList, null, { _, text ->
                    mStates.startTime.set(text)
                }, 0, 0)
                .show()
        }


        /**
         * 恢复默认配置
         */
        fun onResetClick() {
            resetDefaultParams()
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun initSaveCommand() {
        val commands = mutableListOf<String>()

        if (mStates.reportMethod.get().contains("定时定点") && mStates.startTime.get().isEmpty()) {
            showMessageDialog("请选择上报起始时间!")
            return
        }
        if (mStates.interval.get().isEmpty()) {
            showMessageDialog("请输入上报间隔!")
            return
        }
        val entity = MRDataReportTypeEntity(
            type = (reportMethodList.indexOf(mStates.reportMethod.get()) + 1).toString(),
            basis = reportStartTimeList.indexOf(mStates.startTime.get()).toString(),
            interval = mStates.interval.get()
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_SET_REPORT_TYPE,
            entity.toCommandString()
        )
        commands.add(command)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                loadingMessage = StringUtils.getString(R.string.processing),
                errorConfig = ErrorConfig.dialogConfig()
            )
        )
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        val commands = mutableListOf<String>()

        val command = IOTCommandUtil.getCommand(IOTCommandType.MR_MD_GET_REPORT_TYPE)
        commands.add(command)

        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                showLoadingDialog = false, // 使用刷新动画而不是加载动画弹窗
                errorConfig = ErrorConfig(
                    strategy = ErrorHandlingStrategy.Dialog
                )
            )
        )
    }


    override fun handleCommandResponse(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MR_MD_GET_REPORT_TYPE -> {
                val result = iotParseManager.parse<MRDataReportType>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_REPORT_TYPE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        initReportMethod(result.data)
                    }
                }
            }


            IOTCommandType.MR_MD_SET_REPORT_TYPE -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg, isMessageDialog = true)
                        return
                    }

                    else -> {
                        if (!isCommunicationExecuting()){
                            processNavigateUp()
                        }
                    }
                }
            }

            else -> {

            }
        }
    }

    private fun initReportMethod(info: MRDataReportType) {
        try {
            info.type.toInt().let {
                if (it in 1..reportMethodList.size) {
                    mStates.reportMethod.set(reportMethodList[it - 1])
                }
            }
            info.basis.toInt().let {
                if (it in reportStartTimeList.indices) {
                    mStates.startTime.set(reportStartTimeList[it])
                }
            }
            mStates.interval.set(info.interval)

            // 保存初始状态
            mStates.saveInitialState()
        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    override fun handleBackByCheckDataModified() {
        if (mStates.isDataModified.value == true) {
            showExitConfirmationDialog()
            return
        }
        nav().navigateUp()
    }

}