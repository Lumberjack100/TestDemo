package com.shmedo.mcloudapp.ui.page.device.m50.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.RtkParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.common.RtkParamInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentM50ReportModelParamBinding
import com.shmedo.mcloudapp.extensions.formatDoubleValue
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.M50ReportModelParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/9/10
 * @desc: M50上报工作模式参数设置
 *
 */
class M50ReportModelParamFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentM50ReportModelParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: M50ReportModelParamViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()
    private val workModelList = arrayListOf("基站", "测站", "PPP-B2b", "CORS接入")
    private val reportModelList = arrayListOf("常在线", "低功耗", "自适应")
    private val networkModelList = arrayListOf("4G传输", "电台传输", "自动")


    override fun initViewModel() {
        super.initViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_m50_report_model_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentM50ReportModelParamBinding
        binding.llToolbar.toolbar.title = "工作模式"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
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

    override fun initData() {
        super.initData()
        initThresholdTitles()
        resetDefaultParams()
    }

    private fun initThresholdTitles() {
        mStates.firstAlarmThresholdTitle.set("一级报警阈值（毫米）")
        mStates.secondAlarmThresholdTitle.set("二级报警阈值（毫米）")
        mStates.thirdAlarmThresholdTitle.set("三级报警阈值（毫米）")
        mStates.fourthAlarmThresholdTitle.set("四级报警阈值（毫米）")
    }

    private fun resetDefaultParams() {
        mStates.workModel.set(workModelList[1])//默认测站
        mStates.reportModel.set(reportModelList[2])//默认自适应
        mStates.networkModel.set(networkModelList[2])//默认自动
        mStates.memsThreshold.set("5")//MEMS阈值
        mStates.alarmEnable.set(false)//是否开启报警
        mStates.firstAlarmThreshold.set("20")//一级报警阈值
        mStates.secondAlarmThreshold.set("50")//二级报警阈值
        mStates.thirdAlarmThreshold.set("100")//三级报警阈值
        mStates.fourthAlarmThreshold.set("200")//四级报警阈值
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 选择工作模式
         */
        fun onWorkModelChooseClick() {
            val selectedIndex = workModelList.indexOf(mStates.workModel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", workModelList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.workModel.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 选择上报模式
         */
        fun onReportModelChooseClick() {
            val selectedIndex = reportModelList.indexOf(mStates.reportModel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", reportModelList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.reportModel.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 选择网络模式
         */
        fun onNetworkModelChooseClick() {
            val selectedIndex = networkModelList.indexOf(mStates.networkModel.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", networkModelList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.networkModel.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
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
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun initSaveCommand() {
        commandItems.clear()

        if (mStates.memsThreshold.get().isEmpty()) {
            showMessageDialog("请输入MEMS 触发阈值!")
            return
        }
        try {
            val value = mStates.memsThreshold.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的MEMS 触发阈值!")
            return
        }

        //四级预警未启用
        if (!mStates.alarmEnable.get()) {
            val entity = RtkParamEntity(
                mode = (workModelList.indexOf(mStates.workModel.get()) + 1).toString(),
                reportMode = reportModelList.indexOf(mStates.reportModel.get()).toString(),
                networkMode = networkModelList.indexOf(mStates.networkModel.get()).toString(),
                gateAngleVal1 = mStates.memsThreshold.get(),
                alarmSwitch = "0"
            )
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MD_CFG_RTK,
                entity.toCommandString()
            )
            commandItems.add(command)

            showLoadingDialog(StringUtils.getString(R.string.processing))
            sendCommandFromCmdList(isStartTimeoutJob = true)
            return
        }

        //四级预警启用
        if (mStates.firstAlarmThreshold.get().isEmpty()) {
            showMessageDialog("请输入一级报警阈值!")
            return
        }
        try {
            val value = mStates.firstAlarmThreshold.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的一级报警阈值!")
            return
        }

        if (mStates.secondAlarmThreshold.get().isEmpty()) {
            showMessageDialog("请输入二级报警阈值!")
            return
        }
        try {
            val value = mStates.secondAlarmThreshold.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的二级报警阈值!")
            return
        }

        if (mStates.thirdAlarmThreshold.get().isEmpty()) {
            showMessageDialog("请输入三级报警阈值!")
            return
        }
        try {
            val value = mStates.thirdAlarmThreshold.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的三级报警阈值!")
            return
        }

        if (mStates.fourthAlarmThreshold.get().isEmpty()) {
            showMessageDialog("请输入四级报警阈值!")
            return
        }
        try {
            val value = mStates.fourthAlarmThreshold.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的四级报警阈值!")
            return
        }
        val entity = RtkParamEntity(
            mode = (workModelList.indexOf(mStates.workModel.get()) + 1).toString(),
            reportMode = reportModelList.indexOf(mStates.reportModel.get()).toString(),
            networkMode = networkModelList.indexOf(mStates.networkModel.get()).toString(),
            gateAngleVal1 = mStates.memsThreshold.get(),
            alarmSwitch = "1",
            gateDevVal1 = mStates.firstAlarmThreshold.get(),
            gateDevVal2 = mStates.secondAlarmThreshold.get(),
            gateDevVal3 = mStates.thirdAlarmThreshold.get(),
            gateDevVal4 = mStates.fourthAlarmThreshold.get()
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_CFG_RTK,
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
            IOTCommandType.MD_CFG_RTK,
            "method=0"
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_CFG_RTK -> {
                val result = if (cmdStr.contains("method=0"))
                    iotParseManager.parse<RtkParamInfo>(
                        cmdStr,
                        IOTCommandType.MD_CFG_RTK
                    )
                else iotParseManager.parse<CommonSettingCmdResult>(cmdStr)
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg =
                            if (cmdStr.contains("method=0")) "查询信息出错: ${result.message}" else "数据保存出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        if (cmdStr.contains("method=0")) {
                            initParamData(result.data as RtkParamInfo)
                        } else {
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

    private fun initParamData(info: RtkParamInfo) {
        try {
            info.mode.toIntOrNull()?.let {
                if (it in 1..workModelList.size) {
                    mStates.workModel.set(workModelList[it - 1])
                }
            }
            info.reportMode.toIntOrNull()?.let {
                if (it in reportModelList.indices) {
                    mStates.reportModel.set(reportModelList[it])
                }
            }
            info.networkMode.toIntOrNull()?.let {
                if (it in networkModelList.indices) {
                    mStates.networkModel.set(networkModelList[it])
                }
            }
            mStates.memsThreshold.set(info.gateAngleVal1.formatDoubleValue("", 1))
            mStates.alarmEnable.set(info.alarmSwitch == "1")
            mStates.firstAlarmThreshold.set(info.gateDevVal1.formatDoubleValue("", 1))
            mStates.secondAlarmThreshold.set(info.gateDevVal2.formatDoubleValue("", 1))
            mStates.thirdAlarmThreshold.set(info.gateDevVal3.formatDoubleValue("", 1))
            mStates.fourthAlarmThreshold.set(info.gateDevVal4.formatDoubleValue("", 1))
        } catch (e: Exception) {
            Timber.Forest.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}