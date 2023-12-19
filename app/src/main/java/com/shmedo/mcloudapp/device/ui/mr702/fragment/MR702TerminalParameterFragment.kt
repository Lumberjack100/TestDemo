package com.shmedo.mcloudapp.device.ui.mr702.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.google.android.material.tabs.TabLayout
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr.MRReportMethodEntity
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr.MRScreenParamEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRReportMethod
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRScreenParam
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.databinding.FragmentMr702TerminalParameterBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.MR702TerminalParameterViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/27 <br></br>
 * 描述：   终端参数页面
 */
class MR702TerminalParameterFragment : BaseIOTDeviceFragment(), TabLayout.OnTabSelectedListener {
    private val binding: FragmentMr702TerminalParameterBinding by lazy { getBinding() as FragmentMr702TerminalParameterBinding }
    private val mStates: MR702TerminalParameterViewModel by viewModels()
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val activeColor: Int = ColorUtils.getColor(R.color.colorPrimary)
    private val normalColor: Int = ColorUtils.getColor(R.color.text_color_666666)
    private val activeSize: Float = 17f
    private val normalSize: Float = 15f
    private val tabs = arrayOf("上报方式", "本机屏幕")

    private val reportMethodList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_report_method) }
    private val reportStartTimeList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_report_start_time) }


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mr702_terminal_parameter, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
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
        toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_device_param_edit)
        toolbarViewModel.toolbarTvActionText.set("取消")
        toolbarViewModel.toolbarIvActionVisible.set(true)
    }

    override fun initData() {
        super.initData()
        initTabLayout()
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

    inner class ClickProxy : BaseClickProxy() {
        override fun onToolbarIvClick() {
            setEditable(true)
        }

        override fun onToolbarTvClick() {
            setEditable(false)
        }

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
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .hasShadowBg(false)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.4f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .atView(binding.llReportMethod.ivStartTime) // 依附于所点击的View，内部会自动判断在上方或者下方显示
                .asAttachList(reportStartTimeList, null, { position, text ->
                    mStates.startTime.set(text)
                }, 0, 0)
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

    private fun initSaveCommand() {
        commandItems.clear()
        if (mStates.isReportMethodVisible.get()) {
            if (mStates.reportMethod.get().contains("定时定点") && mStates.startTime.get()
                    .isEmpty()
            ) {
                Toaster.show("请选择上报起始时间")
                return
            }
            if (mStates.interval.get().isEmpty()) {
                Toaster.show("请输入上报间隔")
                return
            }
            val entity = MRReportMethodEntity(
                type = (reportMethodList.indexOf(mStates.reportMethod.get()) + 1).toString(),
                basis = reportStartTimeList.indexOf(mStates.startTime.get()).toString(),
                interval = mStates.interval.get()
            )
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MD_MR_SET_REPORT_METHOD,
                entity.toCommandString()
            )
            commandItems.add(command)
        } else {
            if (mStates.screenRefreshTime.get().isEmpty()) {
                Toaster.show("请输入屏幕更新周期")
                return
            }
            if (mStates.screenBrightTime.get().isEmpty()) {
                Toaster.show("请输入屏幕亮屏时间")
                return
            }
            if (mStates.screenPowerUpTime.get().isEmpty()) {
                Toaster.show("请输入屏幕通电时间")
                return
            }
            val entity = MRScreenParamEntity(
                interval = mStates.screenRefreshTime.get(),
                otime = mStates.screenBrightTime.get(),
                ptime = mStates.screenPowerUpTime.get(),
                bproport = mStates.lightness.get().toString()
            )
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.MD_MR_SET_SCREEN_PARAM,
                entity.toCommandString()
            )
            commandItems.add(command)
        }
        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun setEditable(editable: Boolean) {
        toolbarViewModel.toolbarIvActionVisible.set(!editable)
        toolbarViewModel.toolbarTvActionVisible.set(editable)
        mStates.isEditable.set(editable)
    }

    override fun lazyLoadData() {
        queryData()
    }

    private fun queryData() {
        commandItems.clear()

        var command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_REPORT_METHOD)
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_MR_GET_SCREEN_PARAM)
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.loading))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_REPORT_METHOD -> {
                val result = iotParseManager.parse<MRReportMethod>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_REPORT_METHOD
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询上报方式参数出错: ${result.message}"
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

            IOTCommandType.MD_MR_GET_SCREEN_PARAM -> {
                val result = iotParseManager.parse<MRScreenParam>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_SCREEN_PARAM
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询屏幕参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        initScreenParam(result.data)
                    }
                }
            }

            IOTCommandType.MD_MR_SET_REPORT_METHOD -> {
                setEditable(false)
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "上报方式设置出错: ${result.message}"
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

            IOTCommandType.MD_MR_SET_SCREEN_PARAM -> {
                setEditable(false)
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "屏幕参数设置出错: ${result.message}"
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

    private fun initReportMethod(info: MRReportMethod) {
        mStates.reportMethod.set(reportMethodList[info.type.toInt() - 1])
        mStates.startTime.set(reportStartTimeList[info.basis.toInt()])
        mStates.interval.set(info.interval)
    }

    private fun initScreenParam(info: MRScreenParam) {
        mStates.screenRefreshTime.set(info.interval)
        mStates.screenBrightTime.set(info.otime)
        mStates.screenPowerUpTime.set(info.ptime)
        mStates.lightness.set(info.bproport.toInt())
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}