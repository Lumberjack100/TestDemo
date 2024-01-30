package com.shmedo.mcloudapp.device.ui.adme.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.RegexUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.util.IOTRegexContants
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.adme.AdmeInclinometerEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeInclinometerInfo
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.common.ext.showMessageDialog
import com.shmedo.mcloudapp.databinding.FragmentAdmeInclinometerBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.AdmeInclinometerViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * @author：gonghe
 * @time: 2023/12/14
 * @desc: ADME 测斜仪参数配置页面
 *
 */
class AdmeInclinometerFragment : BaseIOTDeviceFragment() {
    private val binding: FragmentAdmeInclinometerBinding by lazy { getBinding() as FragmentAdmeInclinometerBinding }
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: AdmeInclinometerViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private val versionList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_inclinometer_version) }
    private val modeList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_inclinometer_work_mode) }
    private val communicateWayList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_inclinometer_twist_angle_compensation) }


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_adme_inclinometer,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.llToolbar.toolbar.title = "测斜仪参数"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        toolbarViewModel.toolbarIvActionVisible.set(mMessenger.admeDeviceMode.get() == "0")
        toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_device_param_edit)
        toolbarViewModel.toolbarTvActionText.set("取消")
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            queryData()
        }
    }

    override fun initData() {
        super.initData()
        mStates.inclinometerVersion.set(versionList[0])
        mStates.mode.set(modeList[0])
        mStates.compensateWay.set(communicateWayList[0])
    }

    private fun setEditable(editable: Boolean) {
        toolbarViewModel.toolbarIvActionVisible.set(!editable)
        toolbarViewModel.toolbarTvActionVisible.set(editable)
        mStates.isEditable.set(editable)
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onToolbarIvClick() {
            setEditable(true)
        }

        override fun onToolbarTvClick() {
            setEditable(false)
        }

        /**
         * 测斜仪版本
         */
        fun onVersionChooseClick() {
            val selectedIndex = versionList.indexOf(mStates.inclinometerVersion.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", versionList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.inclinometerVersion.set(text)
                        mStates.isCompensateWayVisible.set(position == 1)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onModeChooseClick() {
            val selectedIndex = modeList.indexOf(mStates.mode.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", modeList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.mode.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onCompensateWayChooseClick() {
            val selectedIndex = communicateWayList.indexOf(mStates.compensateWay.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", communicateWayList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.compensateWay.set(text)
                        mStates.isTorsionAngleVisible.set(position == 1)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onSubmitClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun initSaveCommand() {
        if (mStates.torsionAngle.get().isEmpty()) {
            showMessageDialog("请输入扭转角γ!")
            return
        }
        try {
            val value = mStates.torsionAngle.get().toDouble()
            if (value < -90 || value > 90) {
                showMessageDialog("扭转角γ数值范围[-90,90]!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的扭转角γ!")
            return
        }

        if (mStates.address.get().isEmpty()) {
            showMessageDialog("请输入Mac地址!")
            return
        }
        if (!RegexUtils.isMatch(
                IOTRegexContants.REGEX_MAC_ADDRESS_NO_COLON,
                mStates.address.get()
            )
        ) {
            showMessageDialog("请输入正确的Mac地址!")
            return
        }

        if (mStates.collectionInterval.get().isEmpty()) {
            showMessageDialog("请输入采集器采集间隔!")
            return
        }
        try {
            val value = mStates.collectionInterval.get().toInt()
            if (value < 1) {
                showMessageDialog("请输入正确的采集器采集间隔!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的采集器采集间隔!")
            return
        }

        if (mStates.solvingInterval.get().isEmpty()) {
            showMessageDialog("请输入采集器解算间隔!")
            return
        }
        try {
            val value = mStates.solvingInterval.get().toInt()
            if (value < 1) {
                showMessageDialog("请输入正确的采集器解算间隔!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的采集器解算间隔!")
            return
        }

        if (mStates.sleepTime.get().isEmpty()) {
            showMessageDialog("请输入休眠时间!")
            return
        }
        try {
            val value = mStates.sleepTime.get().toInt()
            if (value < 1) {
                showMessageDialog("请输入正确的休眠时间!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的休眠时间!")
            return
        }

        if (mStates.correctionValue.get().isEmpty()) {
            showMessageDialog("请输入测斜仪修正值!")
            return
        }
        try {
            val value = mStates.correctionValue.get().toDouble()
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的测斜仪修正值!")
            return
        }

        val entity = AdmeInclinometerEntity(
            inctype = "1",
            incversion = if (mStates.inclinometerVersion.get() == versionList[0]) "0" else "1",
            address = mStates.address.get(),
            collinval = mStates.collectionInterval.get(),
            calcinval = mStates.solvingInterval.get(),
            dormancytime = mStates.sleepTime.get(),
            interupdate = mStates.correctionValue.get(),
            mode = when (mStates.mode.get()) {
                modeList[0] -> "5"
                modeList[1] -> "7"
                modeList[2] -> "8"
                modeList[3] -> "9"
                else -> "5"
            },
            compenway = if (mStates.compensateWay.get() == communicateWayList[0]) "0" else "1",
            torangle = mStates.torsionAngle.get()
        )
        commandItems.clear()
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_SET_INCLINOMETER,
            entity.toCommandString()
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_SAVE_CONFIG_PARAM)
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
            IOTCommandType.ADME_MD_GET_INCLINOMETER
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.ADME_MD_GET_INCLINOMETER -> {
                val result = iotParseManager.parse<AdmeInclinometerInfo>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_INCLINOMETER
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询测斜仪参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        //设备版本不支持，隐藏编辑按钮
                        toolbarViewModel.toolbarIvActionVisible.set(!errMsg.contains("设备版本不支持"))
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList{
                            binding.refreshLayout.finish()
                        }
                        initParamData(result.data)
                    }
                }
            }

            IOTCommandType.ADME_MD_SET_INCLINOMETER -> {//设置ADME的计米轮配置参数
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置测斜仪参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            IOTCommandType.MD_SAVE_CONFIG_PARAM -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "保存出错: ${result.message}"
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

    private fun initParamData(info: AdmeInclinometerInfo) {
        val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.getDefault()))
        try {
            mStates.inclinometerVersion.set(if (info.incversion == "1") versionList[1] else versionList[0])
            mStates.mode.set(
                when (info.mode) {
                    "5" -> modeList[0]
                    "7" -> modeList[1]
                    "8" -> modeList[2]
                    "9" -> modeList[3]
                    else -> modeList[0]
                }
            )
            mStates.isCompensateWayVisible.set(info.incversion == "1")
            mStates.isTorsionAngleVisible.set(info.compenway == "1")
            mStates.compensateWay.set(if (info.compenway == "1") communicateWayList[1] else communicateWayList[0])
            decimalFormat.applyPattern("#.##")
            mStates.torsionAngle.set(decimalFormat.format(info.torangle.toDouble()))
            mStates.address.set(info.address)
            mStates.collectionInterval.set(info.collinval)
            mStates.solvingInterval.set(info.calcinval)
            mStates.sleepTime.set(info.dormancytime)
            decimalFormat.applyPattern("#.###")
            mStates.correctionValue.set(decimalFormat.format(info.interupdate.toDouble()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}