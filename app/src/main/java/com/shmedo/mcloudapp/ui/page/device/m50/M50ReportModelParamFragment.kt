package com.shmedo.mcloudapp.ui.page.device.m50

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.AlarmTriggerValueEntity
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.u_product.UDAlarmReportModeEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.AlarmTriggerValueInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.common.UDCommonCurrentStateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentM50ReportModelParamBinding
import com.shmedo.mcloudapp.extensions.formatDoubleValue
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.M50ReportModelParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2024/9/10
 * @desc:M50上报工作模式参数设置
 *
 */
class M50ReportModelParamFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentM50ReportModelParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: M50ReportModelParamViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()
    private val workModelList = arrayListOf("测站", "基准站")
    private val reportModelList = arrayListOf("低功耗", "正常")


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
        when (productType) {
            ProductType.U_D_1,
            ProductType.U_D_2
            -> {
                mStates.firstAlarmThresholdTitle.set("一级报警阈值(毫米)")
                mStates.secondAlarmThresholdTitle.set("二级报警阈值(毫米)")
                mStates.thirdAlarmThresholdTitle.set("三级报警阈值(毫米)")
                mStates.fourthAlarmThresholdTitle.set("四级报警阈值(毫米)")
            }

            else -> {}
        }
    }

    private fun resetDefaultParams() {
        mStates.workModel.set(workModelList[0])
        mStates.reportModel.set(reportModelList[0])
        mStates.memsThreshold.set("0")
        mStates.alarmEnable.set(true)
        //一级报警阈值
        mStates.firstAlarmThreshold.set("20")
        //二级报警阈值
        mStates.secondAlarmThreshold.set("50")
        //三级报警阈值
        mStates.thirdAlarmThreshold.set("100")
        //四级报警阈值
        mStates.fourthAlarmThreshold.set("200")
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

        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
                return
            }
            mStates.alarmEnable.set(isChecked)
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

        //四级预警未启用
        if (!mStates.alarmEnable.get()) {
            val reportModeEntity = UDAlarmReportModeEntity(
                rept_mode = "0",
                warning_switch = "0",
            )
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MD_SET_MUD_LEVEL_METER_SENSOR,
                reportModeEntity.toCommandString()
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

        val reportModeEntity = UDAlarmReportModeEntity(
            rept_mode = "0",
            warning_switch = "1",
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_MUD_LEVEL_METER_SENSOR,
            reportModeEntity.toCommandString()
        )
        commandItems.add(command)

        val triggerValueEntity = AlarmTriggerValueEntity(
            level1 = mStates.firstAlarmThreshold.get(),
            level2 = mStates.secondAlarmThreshold.get(),
            level3 = mStates.thirdAlarmThreshold.get(),
            level4 = mStates.fourthAlarmThreshold.get()
        )
        val triggerValueCommand = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_ALRAM_BROADCAST_TRIGGER_VALUE,
            triggerValueEntity.toCommandString()
        )
        commandItems.add(triggerValueCommand)



        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()

        var command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_DEVICE_STATUS, "value=0"
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_ALRAM_BROADCAST_TRIGGER_VALUE
        )
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
                        val errMsg = "查询上报模式出错: ${result.message}"
                        handleFailureResult(errMsg)
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

            IOTCommandType.MD_GET_ALRAM_BROADCAST_TRIGGER_VALUE -> {
                val result = iotParseManager.parse<AlarmTriggerValueInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_ALRAM_BROADCAST_TRIGGER_VALUE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询报警阈值出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initAlarmTriggerValueData(result.data)
                    }
                }
            }

            IOTCommandType.MD_SET_MUD_LEVEL_METER_SENSOR -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置上报模式出错: ${result.message}"
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

            IOTCommandType.MD_SET_ALRAM_BROADCAST_TRIGGER_VALUE -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置报警阈值出错: ${result.message}"
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

    private fun initStatusInfo(content: String) {
        launchWithViewLifecycle {
            try {
                val udCommonCurrentStateInfo = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<UDCommonCurrentStateInfo>(content)
                } ?: return@launchWithViewLifecycle

                mStates.reportModel.set(if (udCommonCurrentStateInfo.reportMode == "0") "自动" else "手动")
                mStates.alarmEnable.set(udCommonCurrentStateInfo.levelFourWarningEnabled == "1")

            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    /**
     * 初始化报警阈值数据
     */
    private fun initAlarmTriggerValueData(info: AlarmTriggerValueInfo) {
        mStates.firstAlarmThreshold.set(info.level1.formatDoubleValue("", 1))
        mStates.secondAlarmThreshold.set(info.level2.formatDoubleValue("", 1))
        mStates.thirdAlarmThreshold.set(info.level3.formatDoubleValue("", 1))
        mStates.fourthAlarmThreshold.set(info.level4.formatDoubleValue("", 1))
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}