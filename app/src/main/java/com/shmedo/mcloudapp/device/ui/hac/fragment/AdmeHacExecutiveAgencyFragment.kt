package com.shmedo.mcloudapp.device.ui.hac.fragment

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.hjq.toast.Toaster
import com.kongzue.dialogx.dialogs.PopTip
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.adme.AdmeExecutiveAgencyInfoEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeExecutiveAgencyInfo
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.device.base.iot_cmd.utils.IOTConstants
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentAdmeExecutiveAgencyBinding
import com.shmedo.mcloudapp.device.common.BaseAdmeExecutiveAgencyClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.AdmeExecutiveAgencyViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessageDialog
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * 创建者：gonghe
 * 创建时间：2024/5/9
 * 描述： TODO
 */
class AdmeHacExecutiveAgencyFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAdmeExecutiveAgencyBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: AdmeExecutiveAgencyViewModel
    private val iotParseManager: IOTParserManager by inject()

    private val settlementMethodList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_settlement_method) }
    private val dataResponseTypeList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_data_response_type) }

    private val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.getDefault()))


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_adme_executive_agency,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAdmeExecutiveAgencyBinding
        binding.llToolbar.toolbar.title = "执行机构参数"
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
        mStates.measureMethod.set(0)
        mStates.dataSettlementMethod.set(settlementMethodList[1])
        mStates.dataResponse.set(dataResponseTypeList[0])
    }

    private fun setEditable(editable: Boolean) {
        toolbarViewModel.toolbarIvActionVisible.set(!editable)
        toolbarViewModel.toolbarTvActionVisible.set(editable)
        mStates.isEditable.set(editable)
    }

    inner class ClickProxy : BaseAdmeExecutiveAgencyClickProxy() {
        override fun onToolbarIvClick() {
            setEditable(true)
        }

        override fun onToolbarTvClick() {
            setEditable(false)
        }

        /**
         * 数据解算方式
         */
        override fun onDataSettlementMethodClick() {
            val selectedIndex = settlementMethodList.indexOf(mStates.dataSettlementMethod.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", settlementMethodList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.dataSettlementMethod.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 数据应答方式
         */
        override fun onDataResponseClick() {
            val selectedIndex = dataResponseTypeList.indexOf(mStates.dataResponse.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", dataResponseTypeList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.dataResponse.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        override fun onSubmitButtonClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }
    }

    private fun initSaveCommand() {
        if (mStates.dataReadingInterval.get().isEmpty()) {
            showMessageDialog("请输入数据读取间隔!")
            return
        }
        try {
            val value = mStates.dataReadingInterval.get().toDouble()
            if (value < 1) {
                showMessageDialog("请输入正确的数据读取间隔!")
                return
            }
        } catch (ex: Exception) {
            Toaster.show("请输入正确的数据读取间隔!")
            return
        }

        if (mStates.measurementCompensationTime.get().isEmpty()) {
            Toaster.show("请输入测量补偿时间!")
            return
        }
        try {
            val value = mStates.measurementCompensationTime.get().toDouble()
            if (value < 1) {
                Toaster.show("请输入正确的测量补偿时间!")
                return
            }
        } catch (ex: Exception) {
            Toaster.show("请输入正确的测量补偿时间!")
            return
        }

        if (mStates.motorDriveAddress.get().isEmpty()) {
            Toaster.show("请输入电机驱动器地址!")
            return
        }
        try {
            val value = mStates.motorDriveAddress.get().toDouble()
            if (value < 0 || value > 99) {
                showMessageDialog("电机驱动器地址数值范围[0,99]!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("电机驱动器地址数值范围[0,99]!")
            return
        }

        if (mStates.decentralizationSpeed.get().isEmpty()) {
            Toaster.show("请输入电机下放速度!")
            return
        }
        try {
            val value = mStates.decentralizationSpeed.get().toDouble()
            if (value < 1 || value > 180) {
                showMessageDialog("电机下放速度数值范围[1,180]!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("电机下放速度数值范围[1,180]!")
            return
        }

        if (mStates.decentralizationWaitingTime.get().isEmpty()) {
            Toaster.show("请输入下放等待时间!")
            return
        }
        try {
            val value = mStates.decentralizationWaitingTime.get().toDouble()
            if (value < 1 || value > 32) {
                showMessageDialog("下放等待时间数值范围[1,32]!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("下放等待时间数值范围[1,32]!")
            return
        }

        if (mStates.pullUpSpeed.get().isEmpty()) {
            Toaster.show("请输入电机上拉速度!")
            return
        }
        try {
            val value = mStates.pullUpSpeed.get().toDouble()
            if (value < 1 || value > 180) {
                showMessageDialog("电机上拉速度数值范围[1,180]!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("电机上拉速度数值范围[1,180]!")
            return
        }
        if (mStates.pullUpZeroSpeed.get() != IOTConstants.NULL_KEY) {
            if (mStates.pullUpZeroSpeed.get().isEmpty()) {
                Toaster.show("请输入上拉归零速度!")
                return
            }
            try {
                val value = mStates.pullUpZeroSpeed.get().toDouble()
                if (value < 1 || value > 10) {
                    showMessageDialog("上拉归零速度数值范围[1,10]!")
                    return
                }
            } catch (ex: Exception) {
                showMessageDialog("上拉归零速度数值范围[1,10]!")
                return
            }
        }

        if (mStates.measuringDistance.get().isEmpty()) {
            Toaster.show("请输入测量间距!")
            return
        }
        try {
            val value = mStates.measuringDistance.get().toDouble()
            if (value < 1) {
                showMessageDialog("请输入正确的测量间距!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的测量间距!")
            return
        }

        if (mStates.measurementIntervalTime.get().isEmpty()) {
            Toaster.show("请输入测量间隔时间!")
            return
        }
        try {
            val value = mStates.measurementIntervalTime.get().toDouble()
            if (value < 1) {
                showMessageDialog("请输入正确的测量间隔时间!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的测量间隔时间!")
            return
        }

        if (mStates.intervalCompensation.get().isEmpty()) {
            Toaster.show("请输入管口安全距离!")
            return
        }
        try {
            val value = mStates.intervalCompensation.get().toDouble()
            if (value < -10 || value > 10) {
                showMessageDialog("管口安全距离数值范围[-10,10]!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("管口安全距离数值范围[-10,10]!")
            return
        }

        if (mStates.bottomSafetyDistance.get().isEmpty()) {
            Toaster.show("请输入管底安全距离!")
            return
        }
        try {
            val value = mStates.bottomSafetyDistance.get().toDouble()
            if (value < -10 || value > 10) {
                showMessageDialog("管底安全距离数值范围[-10,10]!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("管底安全距离数值范围[-10,10]!")
            return
        }

        if (mStates.intervalFitting.get().isEmpty()) {
            Toaster.show("请输入数据拟合区间!")
            return
        }
        try {
            val value = mStates.intervalFitting.get().toDouble()
            if (value < 0 || value > 10) {
                showMessageDialog("数据拟合区间数值范围[0,10]!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("数据拟合区间数值范围[0,10]!")
            return
        }

        if (mStates.pointOffset.get().isEmpty()) {
            Toaster.show("请输入测点偏移距离!")
            return
        }
        try {
            val value = mStates.pointOffset.get().toDouble()
            if (value < 0 || value > 0.5) {
                showMessageDialog("测点偏移距离数值范围[0,0.5]!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("测点偏移距离数值范围[0,0.5]!")
            return
        }
        val entity = AdmeExecutiveAgencyInfoEntity(
            meastype =  IOTConstants.NULL_KEY,
            datatype = if (mStates.dataSettlementMethod.get() == settlementMethodList[0]) "0" else "1",
            datareply = if (mStates.dataResponse.get() == dataResponseTypeList[0]) "0" else "1",
            roundwaitetime =  IOTConstants.NULL_KEY,
            roundmeasinval = IOTConstants.NULL_KEY,
            invalday = IOTConstants.NULL_KEY,
            roundmeasstart = IOTConstants.NULL_KEY,
            datainval = mStates.dataReadingInterval.get(),
            compensatetime = mStates.measurementCompensationTime.get(),
            interdeep =  IOTConstants.NULL_KEY,
            driveaddress = mStates.motorDriveAddress.get(),
            downspeed = mStates.decentralizationSpeed.get(),
            downwaitetime = mStates.decentralizationWaitingTime.get(),
            upspeed = mStates.pullUpSpeed.get(),
            pzspeed = mStates.pullUpZeroSpeed.get(),
            measpacing = mStates.measuringDistance.get(),
            meaintertime = mStates.measurementIntervalTime.get(),
            meabaseth =  IOTConstants.NULL_KEY,
            interval_compensation = mStates.intervalCompensation.get(),
            bottom_safe_distance = mStates.bottomSafetyDistance.get(),
            interval_fitting = mStates.intervalFitting.get(),
            point_offset = mStates.pointOffset.get()
        )

        commandItems.clear()
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_HAC_MD_SET_EXECUTIVE_AGENCY,
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
            IOTCommandType.ADME_HAC_MD_GET_EXECUTIVE_AGENCY
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.ADME_HAC_MD_GET_EXECUTIVE_AGENCY -> {
                val result = iotParseManager.parse<AdmeExecutiveAgencyInfo>(
                    cmdStr,
                    IOTCommandType.ADME_HAC_MD_GET_EXECUTIVE_AGENCY
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询执行机构参数出错: ${result.message}"
                        Timber.e(errMsg)
                        PopTip.show(errMsg).autoDismiss(4500).iconError()
                        //设备版本不支持，隐藏编辑按钮
                        toolbarViewModel.toolbarIvActionVisible.set(!errMsg.contains("设备版本不支持"))
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

            IOTCommandType.ADME_HAC_MD_SET_EXECUTIVE_AGENCY -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        var errMsg = "设置执行机构参数出错: ${result.message}"
                        Timber.e(errMsg)
                        if (errMsg.contains("time_err"))
                            errMsg = errMsg.replaceFirst(
                                "(time_err)(:?)".toRegex(),
                                "一轮测量时间不能少于"
                            ) + "小时"
                        showMessageDialog(errMsg)
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

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initParamData(info: AdmeExecutiveAgencyInfo) {
        mStates.wrapInfo.set(info)
        mStates.wrapInfo.notifyChange()

        try {
            mStates.dataSettlementMethod.set(if (info.datatype == "0") settlementMethodList[0] else settlementMethodList[1])
            mStates.dataResponse.set(if (info.datareply == "0") dataResponseTypeList[0] else dataResponseTypeList[1])
            mStates.dataReadingInterval.set(info.datainval)
            mStates.measurementCompensationTime.set(info.compensatetime)
            mStates.motorDriveAddress.set(info.driveaddress)
            mStates.decentralizationSpeed.set(info.downspeed)
            mStates.decentralizationWaitingTime.set(info.downwaitetime)
            mStates.pullUpSpeed.set(info.upspeed)
            mStates.pullUpZeroSpeed.set(info.pzspeed)

            decimalFormat.applyPattern("#.##")
            mStates.measuringDistance.set(info.measpacing.toDoubleOrNull()?.let {
                decimalFormat.format(it)
            } ?: "")
            mStates.measurementIntervalTime.set(info.meaintertime)

            decimalFormat.applyPattern("#.###")
            mStates.intervalCompensation.set(info.interval_compensation.toDoubleOrNull()?.let {
                decimalFormat.format(it)
            } ?: "")

            decimalFormat.applyPattern("#.###")
            mStates.bottomSafetyDistance.set(info.bottom_safe_distance.toDoubleOrNull()?.let {
                decimalFormat.format(it)
            } ?: "")

            decimalFormat.applyPattern("#.#")
            mStates.intervalFitting.set(info.interval_fitting.toDoubleOrNull()?.let {
                decimalFormat.format(it)
            } ?: "")

            decimalFormat.applyPattern("#.###")
            mStates.pointOffset.set(info.point_offset.toDoubleOrNull()?.let {
                decimalFormat.format(it)
            } ?: "")
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}