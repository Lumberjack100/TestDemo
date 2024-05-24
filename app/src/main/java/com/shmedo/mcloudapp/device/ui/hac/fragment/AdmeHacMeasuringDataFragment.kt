package com.shmedo.mcloudapp.device.ui.hac.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.RegexUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.blankj.utilcode.util.VibrateUtils
import com.hjq.toast.Toaster
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.core.util.IOTRegexContants
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.hac.HacMeasuringDataEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.model.hac.HacHoleAreaDepthInfo
import com.shmedo.lib.device.base.iot_cmd.model.hac.HacMeasuringDataInfo
import com.shmedo.lib.device.base.iot_cmd.model.hac.HacMotionState
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentAdmeHacMeasuringDataBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.AdmeHacMeasuringDataViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ext.showAdmeErrorProtectionDialog
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessageDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber

class AdmeHacMeasuringDataFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAdmeHacMeasuringDataBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: AdmeHacMeasuringDataViewModel
    private val iotParseManager: IOTParserManager by inject()

    private val settlementMethodList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_settlement_method) }

    private val holeNumList = ArrayList<String>()
    private val holeAreaDepthInfoArrayList = ArrayList<HacHoleAreaDepthInfo>()


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_adme_hac_measuring_data,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAdmeHacMeasuringDataBinding
        binding.llToolbar.toolbar.title = "数据测量"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        toolbarViewModel.toolbarIvActionVisible.set(false)
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
            queryParamData()
        }
    }

    override fun initData() {
        super.initData()
        mStates.dataSettlementMethod.set(settlementMethodList[0])
    }

    override fun createObserver() {
        super.createObserver()
        //从编辑页面返回需要刷新事件详情页面
        setFragmentResultListener(AppContants.Extras.FRAGMENT_MEASURING_DATA_PROCEDURE_RESULT_REQUEST_KEY) { key, bundle ->
            (bundle.getParcelable(AppContants.Extras.MOTOR_STATE) as HacMotionState?)?.let { motionState ->
                Timber.d("onActivityResult %s", motionState.toString())
                mStates.isRunButtonEnable.set(motionState.motorinfo == "8" || motionState.motorinfo == "9" || motionState.motorinfo == "10")
                mStates.runButtonText.set(
                    if (binding.switchSingleWay.isChecked || motionState.motorinfo == "8")
                        "正向测量"
                    else if (motionState.measmode == "1") "反向测量" else "正向测量"
                )
            }
        }
    }

    inner class ClickProxy : BaseClickProxy() {

        /**
         * 选择孔号
         */
        fun onHoleChooseClick() {
            val selectedIndex = holeNumList.indexOf(mStates.holeno.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", holeNumList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        updateHoleDepth(position)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 数据解算方式
         */
        fun onDataSettlementMethodClick() {
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

        fun onRunClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            if (mStates.runButtonText.get() == "反向测量") {
                VibrateUtils.vibrate(300)
                MessageDialog.show(
                    "提示", "请确认测斜仪是否反向旋转180°", "确认"
                ).setOkButtonClickListener { dialog, v ->
                    initSaveCommand()
                    false
                }.show()
            } else {
                initSaveCommand()
            }
        }
    }

    private fun updateHoleDepth(position: Int) {
        val holeAreaDepthInfo = holeAreaDepthInfoArrayList[position]

        mStates.holeno.set(holeAreaDepthInfo.holeno)
        mStates.areano.set(holeAreaDepthInfo.areano)
        mStates.realHoleDepth.set(holeAreaDepthInfo.holedepth)
        mStates.recommendHoleDepth.set(holeAreaDepthInfo.measdepth)
    }

    private fun initSaveCommand() {
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
        if (mStates.holeno.get().isEmpty()) {
            showMessageDialog("请选择孔号!")
            return
        }
        if (mStates.recommendHoleDepth.get().isEmpty()) {
            showMessageDialog("请输入推荐测斜管孔深!")
            return
        }
        try {
            if (mStates.recommendHoleDepth.get().toDouble() > mStates.realHoleDepth.get()
                    .toDouble()
            ) {
                showMessageDialog("推荐测斜管孔深必须小于实测测斜管孔深!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("推荐测斜管孔深必须小于实测测斜管孔深!")
            return
        }
        if (mStates.decentralizationWaitingTime.get().isEmpty()) {
            showMessageDialog("请输入下放等待时间!")
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

        //数据测量配置参数
        val entity = HacMeasuringDataEntity(
            equipmodel = "1",
            address = mStates.address.get(),
            holeno = mStates.holeno.get(),
            areano = mStates.areano.get(),
            downwaitetime = mStates.decentralizationWaitingTime.get(),
            holedepth = mStates.recommendHoleDepth.get(),
            datatype = settlementMethodList.indexOf(mStates.dataSettlementMethod.get()).toString(),
            onewaytest = if (binding.switchSingleWay.isChecked) "1" else "0",
            checkreverse = if (binding.switchReverse.isChecked) "1" else "0"
        )
        commandItems.clear()
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_HAC_MD_SET_DATA_MEASURE_PARAM,
            entity.toCommandString()
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(
            isStartTimeoutJob = true,
            timeoutMillis = AppContants.Communication.DELAY_15000_MILLIS
        )
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    private fun queryParamData() {
        commandItems.clear()

        //获取数据测量配置参数
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_HAC_MD_GET_DATA_MEASURE_PARAM
        )
        commandItems.add(command)

        //查询电机当前运动状态
        command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        if (isRestrictHiddenMode() && isHidden) {
            return
        }
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.ADME_HAC_MD_GET_DATA_MEASURE_PARAM -> {
                val result = iotParseManager.parse<HacMeasuringDataInfo>(
                    cmdStr,
                    IOTCommandType.ADME_HAC_MD_GET_DATA_MEASURE_PARAM
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "获取数据测量配置参数出错: ${result.message}"
                        Timber.e(errMsg)
                        PopTip.show(errMsg).autoDismiss(3500).iconError()
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initMeasuringDataInfoParam(result.data)
                    }
                }
            }

            IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE -> {//获取ADME的运行状态
                val result = iotParseManager.parse<HacMotionState>(
                    cmdStr,
                    IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "获取电机当前运动状态出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        updateMotionState(result.data)
                    }
                }
            }

            IOTCommandType.ADME_HAC_MD_SET_DATA_MEASURE_PARAM -> {//设置HAC数据测量参数,开始测量
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置数据测量参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            val bundle = AdmeHacMeasuringDataProcedureFragment.newBundleArguments(
                                binding.switchReverse.isChecked,
                                productType,
                                communicateWay,
                                deviceInfo,
                                bleDevice
                            )
                            nav().navigate(
                                R.id.action_global_to_admeHacMeasuringDataProcedureFragment,
                                bundle
                            )
                        }
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    /**
     * 初始化测量配置参数
     */
    private fun initMeasuringDataInfoParam(info: HacMeasuringDataInfo) {
        launchWithViewLifecycle {
            try {
                mStates.equipmodel.set(info.equipmodel)
                mStates.address.set(info.address)
                mStates.decentralizationWaitingTime.set(info.downwaitetime)
                info.datatype.toIntOrNull()?.let {
                    mStates.dataSettlementMethod.set(if (it in settlementMethodList.indices) settlementMethodList[it] else settlementMethodList[0])
                }
                mStates.isSingleWayTest.set(info.onewaytest == "1")
                mStates.isCheckReverse.set(info.checkreverse == "1")

                val tempHoleList = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<List<HacHoleAreaDepthInfo>>(info.holelist)
                }
                if (tempHoleList.isNullOrEmpty()) {
                    showMessageDialog("还没有测孔信息,请先进行孔深测量")
                    mStates.isRunButtonEnable.set(false)
                    return@launchWithViewLifecycle
                }

                holeAreaDepthInfoArrayList.clear()
                holeAreaDepthInfoArrayList.addAll(tempHoleList)

                holeNumList.clear()
                holeAreaDepthInfoArrayList.forEach {
                    holeNumList.add(it.holeno)
                }
                updateHoleDepth(0)
            } catch (e: Exception) {
                Timber.e(e)
                addLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    /**
     * 刷新电机运动状态
     */
    private fun updateMotionState(motionState: HacMotionState) {
        /**
         * 逻辑处理
         * 进入数据测量页面时，查询 md_hac_getdatameasparame 和 md_hac_getmotionstate 指令，先判断 equipmodel
        1.1 equipmodel =1(正常测量状态)：
        根据 motorinfo 控制跳转页面，motorinfo=2|3|4 进入数据测量页面状态；motorinfo=5|6 进入数据读取页面状态；motorinfo=7 进入数据上传页面状态。
        1.2 equipmodel =0 (停止状态)：
        单测时按钮显示正向测量；正反测时，motorinfo=8，按钮显示正向测量；motorinfo=9，按钮显示反向测量。
        1.3 equipmodel =2(异常状态)，弹框提示异常信息，点击按钮开始测量时，设备自动清除异常状态标志。
         */
        if (mStates.equipmodel.get() == "1") {//表示在测量 然后根据 motorinfo 控制跳转页面
            val bundle = AdmeHacMeasuringDataProcedureFragment.newBundleArguments(
                binding.switchReverse.isChecked,
                productType,
                communicateWay,
                deviceInfo,
                bleDevice
            )
            nav().navigate(
                R.id.action_global_to_admeHacMeasuringDataProcedureFragment,
                bundle
            )
            return
        }
        //停止或异常状态下,判断是否单测模式，单测模式下显示正向测量；正反测模式下，根据 motorinfo 处理操作按钮
        mStates.runButtonText.set(
            if (binding.switchSingleWay.isChecked)
                "正向测量"
            else if (motionState.measmode == "1") "反向测量" else "正向测量"
        )
        //表示异常，展示异常原因
        if (motionState.measmode == "2" && motionState.abndiasis != "0") {
            showAdmeErrorProtectionDialog(motionState.abndiasis)
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}