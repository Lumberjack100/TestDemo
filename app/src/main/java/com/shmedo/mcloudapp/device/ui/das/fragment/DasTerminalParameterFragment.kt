package com.shmedo.mcloudapp.device.ui.das.fragment

import android.app.TimePickerDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.google.android.material.tabs.TabLayout
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.das.DasBdTerminalEntity
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.das.DasReportEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.model.das.DasBdTerminalInfo
import com.shmedo.lib.device.base.iot_cmd.model.das.DasReportInfo
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.common.ext.showMessage
import com.shmedo.mcloudapp.common.ext.showMessageDialog
import com.shmedo.mcloudapp.databinding.FragmentDasTerminalParameterBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.DasTerminalParameterViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.Locale

class DasTerminalParameterFragment : BaseIOTDeviceFragment(), TabLayout.OnTabSelectedListener {
    private lateinit var binding: FragmentDasTerminalParameterBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: DasTerminalParameterViewModel
    private val iotParseManager: IOTParserManager by inject()

    private val activeColor: Int = ColorUtils.getColor(R.color.colorPrimary)
    private val normalColor: Int = ColorUtils.getColor(R.color.text_color_666666)
    private val activeSize: Float = 17f
    private val normalSize: Float = 15f
    private val tabs = arrayOf("上报方式", "北斗终端")

    private val reportMethodList: MutableList<String> = arrayListOf("固定间隔上报", "定时定点上报")
    private val baudRateList: MutableList<String> = arrayListOf("9600", "115200")


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
        binding.llToolbar.toolbar.title = "终端参数"
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
//        toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_device_param_edit)
//        toolbarViewModel.toolbarTvActionText.set("取消")
        toolbarViewModel.toolbarIvActionVisible.set(false)
    }

    override fun initData() {
        super.initData()
        initTabLayout()
        mStates.reportMethod.set(reportMethodList[0])
        mStates.startTime.set("0")
        mStates.baudRate.set(baudRateList[0])
    }

    private fun initTabLayout() {
        // add custom tab items
        val tabLayout = binding.tabs
        var textView = TextView(requireContext())
        textView.text = tabs[0]
        textView.textSize = activeSize
        textView.typeface = Typeface.DEFAULT_BOLD
        textView.gravity = Gravity.CENTER
        textView.setTextColor(activeColor)
        tabLayout.addTab(tabLayout.newTab().setCustomView(textView))

        textView = TextView(requireContext())
        textView.text = tabs[1]
        textView.textSize = normalSize
        textView.typeface = Typeface.DEFAULT
        textView.gravity = Gravity.CENTER
        textView.setTextColor(normalColor)
        tabLayout.addTab(tabLayout.newTab().setCustomView(textView))

        tabLayout.addOnTabSelectedListener(this)
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        mStates.isReportMethodVisible.set(tab.position == 0)
        val textView = tab.customView as TextView?
        textView?.apply {
            textSize = activeSize
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(activeColor)
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        val textView = tab.customView as TextView?
        textView?.apply {
            textSize = normalSize
            typeface = Typeface.DEFAULT
            setTextColor(normalColor)
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab) {}

    /* private fun setEditable(editable: Boolean) {
         toolbarViewModel.toolbarIvActionVisible.set(!editable)
         toolbarViewModel.toolbarTvActionVisible.set(editable)
         mStates.isEditable.set(editable)
     }*/

    inner class ClickProxy : BaseClickProxy() {
//        override fun onToolbarIvClick() {
//            setEditable(true)
//        }
//
//        override fun onToolbarTvClick() {
//            setEditable(false)
//        }

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

        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
                return
            }
            mStates.isBdOpened.set(isChecked)
            if (!isChecked) {
                showMessage("确定要关闭吗？", "温馨提示", "确定", {
                    closeBdTerminal()
                }, "取消", {
                    mStates.isBdOpened.set(true)
                    (button as SwitchButton).setCheckedImmediatelyNoEvent(true)
                })
            }
        }

        fun onBaudRateChooseClick() {
            val selectedIndex = baudRateList.indexOf(mStates.baudRate.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", baudRateList.toTypedArray(),
                    null, selectedIndex,
                    { _, text ->
                        mStates.baudRate.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onSubmitClick() {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun closeBdTerminal() {
        commandItems.clear()
        val entity = DasBdTerminalEntity(
            sw = "0"
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.DAS_MD_SET_BD_TERMINAL,
            entity.toCommandString()
        )
        commandItems.add(command)
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun initSaveCommand() {
        commandItems.clear()
        if (mStates.isReportMethodVisible.get()) {
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

        } else {
            if (mStates.address.get().isEmpty()) {
                showMessageDialog("请输入目标地址!")
                return
            }
            val entity = DasBdTerminalEntity(
                sw = "1",
                dstaddr = mStates.address.get(),
                baud = mStates.baudRate.get()
            )
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.DAS_MD_SET_BD_TERMINAL,
                entity.toCommandString()
            )
            commandItems.add(command)
        }

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun lazyLoadData() {
        queryData()
    }

    private fun queryData() {
        commandItems.clear()

        var command = IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_DATA_REPORT_TYPE)
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.DAS_MD_GET_BD_TERMINAL)
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
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询上报参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        initReportMethod(result.data)
                    }
                }
            }

            IOTCommandType.DAS_MD_GET_BD_TERMINAL -> {
                val result = iotParseManager.parse<DasBdTerminalInfo>(
                    cmdStr,
                    IOTCommandType.DAS_MD_GET_BD_TERMINAL
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询北斗参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        initBdTerminal(result.data)
                    }
                }
            }

            IOTCommandType.DAS_MD_SET_DATA_REPORT_TYPE -> {
//                setEditable(false)
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "上报参数设置出错: ${result.message}"
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

            IOTCommandType.DAS_MD_SET_BD_TERMINAL -> {
//                setEditable(false)
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "北斗参数设置出错: ${result.message}"
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

    private fun initBdTerminal(info: DasBdTerminalInfo) {
        mStates.isBdOpened.set(info.sw.toInt() == 1)
        mStates.address.set(info.dstaddr)
        mStates.baudRate.set(
            if (info.baud.isEmpty()) {
                baudRateList[0]
            } else {
                baudRateList[baudRateList.indexOf(info.baud)]
            }
        )
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}