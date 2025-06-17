package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import androidx.activity.OnBackPressedCallback
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.lxj.xpopup.XPopup
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.das.AudibleAlarmEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.das.AudibleAlarm
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentDasAudibleAlarmBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.DasAudibleAlarmViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat

class DasAudibleAlarmFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasAudibleAlarmBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: DasAudibleAlarmViewModel
    private val iotParseManager: IOTParserManager by inject()
    private val alarmTypeList = arrayOf("降雨量", "水位")

    private val decimalFormat = DecimalFormat("#.#")


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_das_audible_alarm,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDasAudibleAlarmBinding
        binding.llToolbar.toolbar.title = "声光报警器"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            //            mMessenger.requestStatusBarColor(R.color.colorPrimary)
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
            queryAlarmData()
        }
    }

    override fun initData() {
        super.initData()
        mStates.alarmType.set(alarmTypeList[0])
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
                return
            }
            when (button.id) {
                R.id.alarmSB -> {
                    mStates.isAudibleOpened.set(isChecked)
                }

                R.id.screenSB -> {
                    mStates.isScreenOpened.set(isChecked)
                }
            }
        }

        fun onAlarmTypeChooseClick() {
            val selectedIndex = alarmTypeList.indexOf(mStates.alarmType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", alarmTypeList,
                    null, selectedIndex,
                    { _, text ->
                        mStates.alarmType.set(text)
                        showLoadingDialog(StringUtils.getString(R.string.loading))
                        queryAlarmData(alarmTypeList.indexOf(text).toString())
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onSubmitClick() {
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
        if (mStates.isAudibleOpened.get()) {
            if (mStates.alarmAddress.get().isEmpty()) {
                showMessageDialog("请输入报警器地址")
                return
            }
            if (mStates.duration.get().isEmpty()) {
                showMessageDialog("请输入播放时长")
                return
            }
            //播放时长不能小于10秒或大于300秒
            if (mStates.duration.get().toInt() < 10 || mStates.duration.get().toInt() > 300) {
                showMessageDialog("播放时长不能小于10秒或大于300秒")
                return
            }
            if (mStates.interval.get().isEmpty()) {
                showMessageDialog("请输入切换间隔")
                return
            }
            //切换间隙不能小于5秒或大于300秒，并且不能超过播放时长
            if (mStates.interval.get().toInt() < 5 || mStates.interval.get()
                    .toInt() > 300 || mStates.interval.get()
                    .toInt() > mStates.duration.get().toInt()
            ) {
                showMessageDialog("切换间隙不能小于5秒或大于300秒，并且不能超过播放时长")
                return
            }

            if (mStates.triggerValueLevel1.get().isEmpty()) {
                showMessageDialog("请输入一级报警阈值")
                return
            }
            if (mStates.triggerValueLevel2.get().isEmpty()) {
                showMessageDialog("请输入二级报警阈值")
                return
            }
            //二级报警阈值不能小于一级报警阈值
            if (mStates.triggerValueLevel2.get().toDouble() <= mStates.triggerValueLevel1.get()
                    .toDouble()
            ) {
                showMessageDialog("二级报警阈值不能小于一级报警阈值")
                return
            }

            if (mStates.triggerValueLevel3.get().isEmpty()) {
                showMessageDialog("请输入三级报警阈值")
                return
            }
            //三级报警阈值不能小于二级报警阈值
            if (mStates.triggerValueLevel3.get().toDouble() <= mStates.triggerValueLevel2.get()
                    .toDouble()
            ) {
                showMessageDialog("三级报警阈值不能小于二级报警阈值")
                return
            }
        }

        if (mStates.isScreenOpened.get()) {
            if (mStates.screenAddress.get().isEmpty()) {
                showMessageDialog("请输入LED屏地址")
                return
            }
            if (mStates.showTime.get().isEmpty()) {
                showMessageDialog("请输入显示时长")
                return
            }
            if (mStates.showGap.get().isEmpty()) {
                showMessageDialog("请输入显示间隔")
                return
            }
        }

        val entity = AudibleAlarmEntity(
            alarmstatus = if (mStates.isAudibleOpened.get()) "1" else "0",
            screenstatus = if (mStates.isScreenOpened.get()) "1" else "0",
            alarmtype = if (mStates.alarmType.get() == alarmTypeList[0]) "0" else "1",
            alarmaddr = mStates.alarmAddress.get(),
            level1 = mStates.triggerValueLevel1.get(),
            level2 = mStates.triggerValueLevel2.get(),
            level3 = mStates.triggerValueLevel3.get(),
            playtime = mStates.duration.get(),
            playgap = mStates.interval.get(),
            volume = mStates.volume.get().toString(),
            screenaddr = mStates.screenAddress.get(),
            showtime = mStates.showTime.get(),
            showgap = mStates.showGap.get()
        )

        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_SET_AUDIBLE_ALARM,
            entity.toCommandString()
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryAlarmData(type: String = "0") {
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_GET_AUDIBLE_ALARM, "alarmtype=$type"
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.DAS_MD_GET_AUDIBLE_ALARM -> {
                val result = iotParseManager.parse<AudibleAlarm>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_AUDIBLE_ALARM
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询报警控制参数出错: ${result.message}"
                        handleFailureResult(errMsg)
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

            IOTCommandType.DAS_MD_SET_AUDIBLE_ALARM -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "数据保存出错: ${result.message}"
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

    private fun initParamData(audibleAlarm: AudibleAlarm) {
        try {
            mStates.isAudibleOpened.set(audibleAlarm.alarmstatus == "1")
            mStates.alarmType.set(if (audibleAlarm.alarmtype == "0") alarmTypeList[0] else alarmTypeList[1])
            mStates.alarmAddress.set(audibleAlarm.alarmaddr)
            mStates.duration.set(audibleAlarm.playtime)
            mStates.interval.set(audibleAlarm.playgap)
            mStates.volume.set(audibleAlarm.volume.toInt())
            audibleAlarm.level1.toDoubleOrNull()?.let {
                mStates.triggerValueLevel1.set(decimalFormat.format(it))
            }
            audibleAlarm.level2.toDoubleOrNull()?.let {
                mStates.triggerValueLevel2.set(decimalFormat.format(it))
            }
            audibleAlarm.level3.toDoubleOrNull()?.let {
                mStates.triggerValueLevel3.set(decimalFormat.format(it))
            }

            mStates.isScreenOpened.set(audibleAlarm.screenstatus == "1")
            mStates.screenAddress.set(audibleAlarm.screenaddr)
            mStates.showTime.set(audibleAlarm.showtime)
            mStates.showGap.set(audibleAlarm.showgap)
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