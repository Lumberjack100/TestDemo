package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

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
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.common.AlarmTriggerValueEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.AlarmSwitchInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.AlarmTriggerValueInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentUdWorkmodelParamBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.UDWorkModelParamViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * @author：gonghe
 * @time: 2024/8/23
 * @desc: 一体化雷达泥位计工作模式参数设置
 *
 */
class UDWorkModelParamFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentUdWorkmodelParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: UDWorkModelParamViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val modelList = arrayListOf("自动", "手动")
    private val reportFrequencyList =
        arrayListOf("15分钟/次", "30分钟/次", "1小时/次", " 2 小时/次")//上报频率
    private val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.getDefault()))


    override fun initViewModel() {
        super.initViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_ud_workmodel_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUdWorkmodelParamBinding
        binding.llToolbar.toolbar.title = "工作模式"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
        initRefresh()
    }

    override fun initData() {
        super.initData()
        initTitles()
        resetParams()
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

    private fun initTitles() {
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

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 选择模式
         */
        fun onModelChooseClick() {
            val selectedIndex = modelList.indexOf(mStates.model.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", modelList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.model.set(text)
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
            disableOrEnable(if (isChecked) "1" else "0")
        }

        /**
         * 选择上报频率
         */
        fun onReportFrequencyChooseClick() {
            val selectedIndex = reportFrequencyList.indexOf(mStates.reportFrequency.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", reportFrequencyList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.reportFrequency.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 恢复默认配置
         */
        fun onResetClick() {
            resetParams()
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

    private fun resetParams() {
        mStates.model.set("自动")
        //一级报警阈值
        mStates.firstAlarmThreshold.set("20")
        //二级报警阈值
        mStates.secondAlarmThreshold.set("50")
        //三级报警阈值
        mStates.thirdAlarmThreshold.set("100")
        //四级报警阈值
        mStates.fourthAlarmThreshold.set("200")
        mStates.reportFrequency.set(reportFrequencyList[0])
    }

    /**
     * 关闭或者打开
     */
    private fun disableOrEnable(sw: String = "1") {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SET_ALRAM_BROADCAST_SWITCH,
            "sw=$sw"
        )
        commandItems.add(command)

//        showLoadingDialog(StringUtils.getString(R.string.processing))
//        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun initSaveCommand() {
        commandItems.clear()

        if (mStates.model.get() == "自动") {
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
        } else {

        }


        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryData() {
        commandItems.clear()

        var command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_GET_ALRAM_BROADCAST_SWITCH
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
            IOTCommandType.MD_GET_ALRAM_BROADCAST_SWITCH -> {
                val result = iotParseManager.parse<AlarmSwitchInfo>(
                    cmdStr,
                    IOTCommandType.MD_GET_ALRAM_BROADCAST_SWITCH
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initEnableAlarmData(result.data)
                    }
                }
            }

            IOTCommandType.MD_SET_ALRAM_BROADCAST_SWITCH -> {//
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg =
                            if (cmdStr.contains("sw=0")) "关闭出错: ${result.message}" else "打开出错: ${result.message}"
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

    private fun initEnableAlarmData(info: AlarmSwitchInfo) {
        try {
            mStates.alarmEnable.set(info.sw == "1")
        } catch (e: Exception) {
            Timber.e(e)
            addLogItem(Log.ERROR, e.errorMsg)
        }
    }

    /**
     * 初始化报警阈值数据
     */
    private fun initAlarmTriggerValueData(info: AlarmTriggerValueInfo) {
        decimalFormat.applyPattern("#.###")

        info.level1.toDoubleOrNull()?.let {
            mStates.firstAlarmThreshold.set(decimalFormat.format(it))
        }
        info.level2.toDoubleOrNull()?.let {
            mStates.secondAlarmThreshold.set(decimalFormat.format(it))
        }
        info.level3.toDoubleOrNull()?.let {
            mStates.thirdAlarmThreshold.set(decimalFormat.format(it))
        }
        info.level4.toDoubleOrNull()?.let {
            mStates.fourthAlarmThreshold.set(decimalFormat.format(it))
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}