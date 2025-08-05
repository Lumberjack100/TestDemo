package com.shmedo.mcloudapp.ui.page.device.adme.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.kyleduo.switchbutton.SwitchButton
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.adme.AdmeLockedRotorDetectionEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeLockedRotorDetectionInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentAdmeLockedRotorDetectionBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.AdmeLockedRotorDetectionViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class AdmeLockedRotorDetectionFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAdmeLockedRotorDetectionBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: AdmeLockedRotorDetectionViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private var admeLockedRotorDetectionInfo: AdmeLockedRotorDetectionInfo? = null
    private var holedepth = 0f//下放距离
    private var measpacing = 0f//上拉测量间距
    val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.getDefault()))


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_adme_locked_rotor_detection,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAdmeLockedRotorDetectionBinding
        binding.llToolbar.toolbar.title = "堵转缓停参数"
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
        initTextChangedListener()
        initSeekBarListener()
    }

    private fun initTextChangedListener() {
        KeyboardVisibilityEvent.setEventListener(
            mActivity,
            viewLifecycleOwner,
            KeyboardVisibilityEventListener { isOpen -> // some code depending on keyboard visiblity status
                if (!isOpen) {
                    binding.etDownSlowStartInterval.clearFocus()
                    binding.etDownSlowStopInterval.clearFocus()
                    binding.etPullUpSlowStartInterval.clearFocus()
                    binding.etPullUpSlowStopInterval.clearFocus()
                }
            })
        //下放加速距离
        binding.etDownSlowStartInterval.setOutSideFocusChangeListener { v, hasFocus ->
            //失去焦点时
            if (!hasFocus) {
                if (mStates.downSlowStartIntervalEndValue.get().isEmpty()) {
                    showMessageDialog("请输入下放加速距离")
                    return@setOutSideFocusChangeListener
                }
                try {
                    val leftValue = mStates.downSlowStartIntervalEndValue.get().toFloat()
                    val rightValue = if (mStates.downSlowStopIntervalStartValue.get().isEmpty()) 0f
                    else mStates.downSlowStopIntervalStartValue.get().toFloat()
                    Timber.d("leftValue:$leftValue,rightValue:$rightValue,holedepth:$holedepth")
                    if ((leftValue + rightValue) > holedepth) {
                        showMessageDialog("下放加速距离与下放减速距离之和不能超过下放总距离(" + holedepth + "毫米)")
                        return@setOutSideFocusChangeListener
                    }
                    if (leftValue > holedepth / 2) {
                        showMessageDialog("下放加速距离不能超过下放总距离(" + holedepth + "毫米) 的 50%")
                        return@setOutSideFocusChangeListener
                    }
                    decimalFormat.applyPattern("#.#")
                    val leftProgress = decimalFormat.format((leftValue / holedepth) * 100).toFloat()
                    binding.seekBarDownInterval.setProgress(
                        leftProgress,
                        binding.seekBarDownInterval.rightSeekBar.progress
                    )
                } catch (e: Exception) {
                    Timber.e(e)
                    addDeviceLogItem(Log.ERROR, e.errorMsg)
                }
            }
        }
        //下放减速距离
        binding.etDownSlowStopInterval.setOutSideFocusChangeListener { v, hasFocus ->
            //失去焦点时
            if (!hasFocus) {
                if (mStates.downSlowStopIntervalStartValue.get().isEmpty()) {
                    showMessageDialog("请输入下放减速距离")
                    return@setOutSideFocusChangeListener
                }
                try {
                    val leftValue = if (mStates.downSlowStartIntervalEndValue.get().isEmpty()) 0f
                    else mStates.downSlowStartIntervalEndValue.get().toFloat()
                    val rightValue = mStates.downSlowStopIntervalStartValue.get().toFloat()
                    if (leftValue + rightValue > holedepth) {
                        showMessageDialog("下放加速距离与下放减速距离之和不能超过下放总距离(" + holedepth + "毫米)")
                        return@setOutSideFocusChangeListener
                    }
                    if (rightValue > holedepth / 2) {
                        showMessageDialog("下放减速距离不能小于下放总距离(" + holedepth + "毫米) 的 50%")
                        return@setOutSideFocusChangeListener
                    }
                    decimalFormat.applyPattern("0")
                    val rightProgress =
                        decimalFormat.format((1 - rightValue / holedepth) * 100).toFloat()
                    binding.seekBarDownInterval.setProgress(
                        binding.seekBarDownInterval.leftSeekBar.progress,
                        rightProgress
                    )

                    //堵转检测区间右边进度条大于下放减速距离的值
                    if (binding.seekBarDownStallDetectionInterval.rightSeekBar.progress >= rightProgress) {
                        binding.seekBarDownStallDetectionInterval.setProgress(
                            binding.seekBarDownStallDetectionInterval.leftSeekBar.progress,
                            if (rightProgress < 50) 50f else rightProgress - 1
                        )
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                    addDeviceLogItem(Log.ERROR, e.errorMsg)
                }
            }
        }
        //上拉加速距离
        binding.etPullUpSlowStartInterval.setOutSideFocusChangeListener { v, hasFocus ->
            //失去焦点时
            if (!hasFocus) {
                if (mStates.pullUpSlowStartIntervalEndValue.get().isEmpty()) {
                    showMessageDialog("请输入上拉加速距离")
                    return@setOutSideFocusChangeListener
                }
                try {
                    val leftValue = mStates.pullUpSlowStartIntervalEndValue.get().toFloat()
                    val rightValue =
                        if (mStates.pullUpSlowStopIntervalStartValue.get().isEmpty()) 0f
                        else mStates.pullUpSlowStopIntervalStartValue.get().toFloat()
                    if (leftValue + rightValue > measpacing) {
                        showMessageDialog("上拉加速距离与上拉减速距离之和不能超过上拉测量间距(" + measpacing + "毫米)")
                        return@setOutSideFocusChangeListener
                    }
                    if (leftValue > measpacing / 2) {
                        showMessageDialog("上拉加速距离不能超过上拉测量间距(" + measpacing + "毫米) 的 50%")
                        return@setOutSideFocusChangeListener
                    }
                    decimalFormat.applyPattern("0")
                    val leftProgress =
                        decimalFormat.format((leftValue / measpacing) * 100).toFloat()
                    binding.seekBarPullUpInterval.setProgress(
                        leftProgress,
                        binding.seekBarPullUpInterval.rightSeekBar.progress
                    )
                } catch (e: Exception) {
                    Timber.e(e)
                    addDeviceLogItem(Log.ERROR, e.errorMsg)
                }
            }
        }
        //上拉减速距离
        binding.etPullUpSlowStopInterval.setOutSideFocusChangeListener { v, hasFocus ->
            //失去焦点时
            if (!hasFocus) {
                if (mStates.pullUpSlowStopIntervalStartValue.get().isEmpty()) {
                    showMessageDialog("请输入上拉减速距离")
                    return@setOutSideFocusChangeListener
                }
                try {
                    val leftValue = if (mStates.pullUpSlowStartIntervalEndValue.get().isEmpty()) 0f
                    else mStates.pullUpSlowStartIntervalEndValue.get().toFloat()
                    val rightValue = mStates.pullUpSlowStopIntervalStartValue.get().toFloat()
                    if (leftValue + rightValue > measpacing) {
                        showMessageDialog("上拉加速距离与上拉减速距离之和不能超过上拉测量间距(" + measpacing + "毫米)")
                        return@setOutSideFocusChangeListener
                    }
                    if (rightValue > measpacing / 2) {
                        showMessageDialog("上拉减速距离不能小于上拉测量间距(" + measpacing + "毫米) 的 50%")
                        return@setOutSideFocusChangeListener
                    }
                    decimalFormat.applyPattern("0")
                    val rightProgress =
                        decimalFormat.format((1 - rightValue / measpacing) * 100).toFloat()
                    binding.seekBarPullUpInterval.setProgress(
                        binding.seekBarPullUpInterval.leftSeekBar.progress,
                        rightProgress
                    )
                } catch (e: Exception) {
                    Timber.e(e)
                    addDeviceLogItem(Log.ERROR, e.errorMsg)
                }
            }
        }
    }

    private fun initSeekBarListener() {
        //下放缓起缓停区间
        binding.seekBarDownInterval.setIndicatorTextDecimalFormat("0");
        binding.seekBarDownInterval.setIndicatorTextStringFormat("%s%%")
        binding.seekBarDownInterval.isEnabled = false

        //堵转检测区间
        binding.seekBarDownStallDetectionInterval.setIndicatorTextDecimalFormat("0");
        binding.seekBarDownStallDetectionInterval.setIndicatorTextStringFormat("%s%%")
        binding.seekBarDownStallDetectionInterval.setOnRangeChangedListener(object :
            OnRangeChangedListener {
            override fun onRangeChanged(
                view: RangeSeekBar,
                leftValue: Float,
                rightValue: Float,
                isFromUser: Boolean
            ) {
                val indicatorTextDecimalFormat =
                    DecimalFormat("0", DecimalFormatSymbols(Locale.getDefault()))
                mStates.downStallDetectionIntervalStartValue.set(
                    indicatorTextDecimalFormat.format(
                        leftValue.toDouble()
                    )
                )
                mStates.downStallDetectionIntervalEndValue.set(
                    indicatorTextDecimalFormat.format(
                        rightValue.toDouble()
                    )
                )
                mStates.downStallDetectionInterval.set(
                    String.format(
                        "%s%%-%s%%",
                        mStates.downStallDetectionIntervalStartValue.get(),
                        mStates.downStallDetectionIntervalEndValue.get()
                    )
                )
            }

            override fun onStartTrackingTouch(view: RangeSeekBar, isLeft: Boolean) {}

            override fun onStopTrackingTouch(view: RangeSeekBar, isLeft: Boolean) {
                val leftValue = view.leftSeekBar.progress
                val rightValue = view.rightSeekBar.progress
                val downSlowStopStartValue =
                    binding.seekBarDownInterval.rightSeekBar.progress
                if (leftValue >= 50) {
                    showMessageDialog("下放堵转检测区间起始值不能大于50%")
                    view.setProgress(49f, rightValue)
                }
                if (rightValue < 50) {
                    showMessageDialog("下放堵转检测区间终值不能小于50%")
                    view.setProgress(leftValue, 50f)
                }
                if (rightValue >= downSlowStopStartValue) {
                    view.setProgress(
                        leftValue,
                        if (binding.seekBarDownInterval.rightSeekBar.progress - 1 < 50) 50f
                        else binding.seekBarDownInterval.rightSeekBar.progress - 1
                    )
                }
            }
        })

        //上拉缓起缓停区间
        binding.seekBarPullUpInterval.setIndicatorTextDecimalFormat("0");
        binding.seekBarPullUpInterval.setIndicatorTextStringFormat("%s%%")
        binding.seekBarPullUpInterval.isEnabled = false
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_refresh_fail_warn))
                return@onRefresh
            }
            queryData()
        }
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

        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                (button as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
                return
            }
            when (button.id) {
                R.id.downEnableSBtn -> { //下放使能
                    mStates.downEnable.set(isChecked)
                    if (!isChecked) {
                        showMessage("确定使下放计米堵转检测不生效？", "温馨提示", "确定", {
                            closeDownOrPullUpEnable()
                        }, "取消", {
                            mStates.downEnable.set(true)
                            (button as SwitchButton).setCheckedImmediatelyNoEvent(true)
                        })
                    }
                }

                R.id.pullUpEnableSBtn -> { //上拉使能
                    mStates.pullUpEnable.set(isChecked)
                    if (!isChecked) {
                        showMessage("确定使上拉计米堵转检测不生效？", "温馨提示", "确定", {
                            closeDownOrPullUpEnable()
                        }, "取消", {
                            mStates.pullUpEnable.set(true)
                            (button as SwitchButton).setCheckedImmediatelyNoEvent(true)
                        })
                    }
                }
            }
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

    private fun closeDownOrPullUpEnable() {
        val entity = AdmeLockedRotorDetectionEntity(
            lowtbtss = if (mStates.downEnable.get()) "1" else "0",//下放堵转缓停（0:关闭，1:开启）
            numpput = admeLockedRotorDetectionInfo!!.numpput,//单位时间脉冲数
            pdajtime = admeLockedRotorDetectionInfo!!.pdajtime,//脉冲检测判断时间
            lowsusranb = admeLockedRotorDetectionInfo!!.lowsusranb,//下放缓起区间终值(加速阶段)
            lowsusrana = admeLockedRotorDetectionInfo!!.lowsusrana,//下放缓停区间起始值(减速阶段)
            detintiona = admeLockedRotorDetectionInfo!!.detintiona,//堵转检测区间起始值
            detintionb = admeLockedRotorDetectionInfo!!.detintionb,//堵转检测区间终值
            lowtorblothr = admeLockedRotorDetectionInfo!!.lowtorblothr,//下放力矩堵转阈值
            lowtordetime = admeLockedRotorDetectionInfo!!.lowtordetime,//下放力矩检测判断时间
            uptbtss = if (mStates.pullUpEnable.get()) "1" else "0",//上拉堵转缓停（0:关闭，1:开启）
            upsusranb = admeLockedRotorDetectionInfo!!.upsusranb,//上拉缓起区间终值(加速阶段)
            upsusrana = admeLockedRotorDetectionInfo!!.upsusrana,//上拉缓停区间起始值(减速阶段)
            uptorblothr = admeLockedRotorDetectionInfo!!.uptorblothr,//下放力矩堵转阈值
            uptordetime = admeLockedRotorDetectionInfo!!.uptordetime,//下放力矩检测判断时间
        )
        commandItems.clear()
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_SET_LOCKED_ROTOR_DETECTION,
            entity.toCommandString()
        )
        commandItems.add(command)

        command = IOTCommandUtil.getCommand(IOTCommandType.MD_SAVE_CONFIG_PARAM)
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    private fun initSaveCommand() {
        if (mStates.downEnable.get()) {
            if (mStates.downPulsesPerUnitTime.get().isEmpty()) {
                showMessageDialog("请输入下放单位时间脉冲数!")
                return
            }
            try {
                val value = mStates.downPulsesPerUnitTime.get().toInt()
                if (value < 1 || value > 10000) {
                    showMessageDialog("下放单位时间脉冲数不能小于1或大于10000!")
                    return
                }
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的下放单位时间脉冲数!")
                return
            }

            if (mStates.downPulseDetectionTime.get().isEmpty()) {
                showMessageDialog("请输入下放脉冲检测判断时间!")
                return
            }
            try {
                val value = mStates.downPulseDetectionTime.get().toDouble()
                if (value < 0.1 || value > 10) {
                    showMessageDialog("下放脉冲检测判断时间范围[0.1,10]!")
                    return
                }
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的下放脉冲检测判断时间!")
                return
            }

            if (mStates.downSlowStartIntervalEndValue.get().isEmpty()) {
                showMessageDialog("请输入下放加速距离!")
                return
            }

            if (mStates.downSlowStopIntervalStartValue.get().isEmpty()) {
                showMessageDialog("请输入下放减速距离!")
                return
            }
        }

        if (mStates.downTorqueStallThreshold.get().isEmpty()) {
            showMessageDialog("请输入下放力矩堵转阈值!")
            return
        }
        try {
            val value = mStates.downTorqueStallThreshold.get().toDouble()
            if (value < 0.00 || value > 2.00) {
                showMessageDialog("下放力矩堵转阈值不能小于0或大于2!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的下放力矩堵转阈值!")
            return
        }

        if (mStates.downTorqueDetectionTime.get().isEmpty()) {
            showMessageDialog("请输入下放力矩检测判断时间!")
            return
        }
        try {
            val value = mStates.downTorqueDetectionTime.get().toDouble()
            if (value < 0.01 || value > 5.00) {
                showMessageDialog("下放力矩检测判断时间不能小于0.01或大于5!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的下放力矩检测判断时间!")
            return
        }

        if (mStates.pullUpEnable.get()) {
            if (mStates.pullUpSlowStartIntervalEndValue.get().isEmpty()) {
                showMessageDialog("请输入上拉加速距离!")
                return
            }
            if (mStates.pullUpSlowStopIntervalStartValue.get().isEmpty()) {
                showMessageDialog("请输入上拉减速距离!")
                return
            }
        }
        if (mStates.pullUpTorqueStallThreshold.get().isEmpty()) {
            showMessageDialog("请输入上拉力矩堵转阈值!")
            return
        }
        try {
            val value = mStates.pullUpTorqueStallThreshold.get().toDouble()
            if (value < 1.00 || value > 8.00) {
                showMessageDialog("上拉力矩堵转阈值不能小于1或大于8!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的上拉力矩堵转阈值!")
            return
        }
        if (mStates.pullUpTorqueDetectionTime.get().isEmpty()) {
            showMessageDialog("请输入上拉力矩检测判断时间!")
            return
        }
        try {
            val value = mStates.pullUpTorqueDetectionTime.get().toDouble()
            if (value < 0.01 || value > 5.00) {
                showMessageDialog("上拉力矩检测判断时间不能小于0.01或大于5!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("请输入正确的上拉力矩检测判断时间!")
            return
        }

        val entity = AdmeLockedRotorDetectionEntity(
            lowtbtss = if (mStates.downEnable.get()) "1" else "0",//下放堵转缓停（0:关闭，1:开启）
            numpput = mStates.downPulsesPerUnitTime.get(),//单位时间脉冲数
            pdajtime = mStates.downPulseDetectionTime.get(),//脉冲检测判断时间
            lowsusranb = mStates.downSlowStartIntervalEndValue.get(),//下放缓起区间终值(加速阶段)
            lowsusrana = mStates.downSlowStopIntervalStartValue.get(),//下放缓停区间起始值(减速阶段)
            detintiona = mStates.downStallDetectionIntervalStartValue.get(),//堵转检测区间起始值
            detintionb = mStates.downStallDetectionIntervalEndValue.get(),//堵转检测区间终值
            lowtorblothr = mStates.downTorqueStallThreshold.get(),//下放力矩堵转阈值
            lowtordetime = mStates.downTorqueDetectionTime.get(),//下放力矩检测判断时间
            uptbtss = if (mStates.pullUpEnable.get()) "1" else "0",//上拉堵转缓停（0:关闭，1:开启）
            upsusranb = mStates.pullUpSlowStartIntervalEndValue.get(),//上拉缓起区间终值(加速阶段)
            upsusrana = mStates.pullUpSlowStopIntervalStartValue.get(),//上拉缓停区间起始值(减速阶段)
            uptorblothr = mStates.pullUpTorqueStallThreshold.get(),//下放力矩堵转阈值
            uptordetime = mStates.pullUpTorqueDetectionTime.get(),//下放力矩检测判断时间
        )
        commandItems.clear()
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_SET_LOCKED_ROTOR_DETECTION,
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
            IOTCommandType.ADME_MD_GET_LOCKED_ROTOR_DETECTION
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }


    override fun showNearbyCommunicationTimeoutAlert(
        cmdStr: String,
        isDismissLoadingDialog: Boolean,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean,
        errMsg: String
    ) {
        super.showNearbyCommunicationTimeoutAlert(
            cmdStr = cmdStr,
            isDismissLoadingDialog = isDismissLoadingDialog,
            isShowErrMsg = isShowErrMsg,
            isMessageDialog = isMessageDialog,
            errMsg = errMsg
        )
        if (IOTCommandUtil.extractCommandType(cmdStr) == IOTCommandType.ADME_MD_GET_LOCKED_ROTOR_DETECTION) {
            //隐藏编辑按钮
            toolbarViewModel.toolbarIvActionVisible.set(false)
        }
    }

    override fun doCmdResponseResultError(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        super.doCmdResponseResultError(
            cmdStr = cmdStr,
            errMsg = errMsg,
            isShowErrMsg = isShowErrMsg,
            isMessageDialog = isMessageDialog
        )
        if (IOTCommandUtil.extractCommandType(cmdStr) == IOTCommandType.ADME_MD_GET_LOCKED_ROTOR_DETECTION) {
            //隐藏编辑按钮
            toolbarViewModel.toolbarIvActionVisible.set(false)
        }
    }

    override fun doCmdResponseResultTimeOut(
        cmdStr: String,
        errMsg: String,
        isShowErrMsg: Boolean,
        isMessageDialog: Boolean
    ) {
        super.doCmdResponseResultTimeOut(
            cmdStr = cmdStr,
            errMsg = errMsg,
            isShowErrMsg = isShowErrMsg,
            isMessageDialog = isMessageDialog
        )
        if (IOTCommandUtil.extractCommandType(cmdStr) == IOTCommandType.ADME_MD_GET_LOCKED_ROTOR_DETECTION) {
            //隐藏编辑按钮
            toolbarViewModel.toolbarIvActionVisible.set(false)
        }
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.ADME_MD_GET_LOCKED_ROTOR_DETECTION -> {
                val result = iotParseManager.parse<AdmeLockedRotorDetectionInfo>(
                    cmdStr,
                    IOTCommandType.ADME_MD_GET_LOCKED_ROTOR_DETECTION
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询堵转参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        //设备版本不支持，隐藏编辑按钮
                        toolbarViewModel.toolbarIvActionVisible.set(!errMsg.contains("设备版本不支持"))
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        admeLockedRotorDetectionInfo = result.data
                        initParamData(result.data)
                    }
                }
            }

            IOTCommandType.ADME_MD_SET_LOCKED_ROTOR_DETECTION -> {
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置堵转参数出错: ${result.message}"
                        handleFailureResult(errMsg)
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
                        val errMsg = "保存出错: ${result.message}"
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

    private fun initParamData(info: AdmeLockedRotorDetectionInfo) {
        try {
            holedepth = info.holedepth.toFloat()//下放距离
            measpacing = info.measpacing.toFloat()//上拉测量间距

            mStates.downEnable.set(info.lowtbtss == "1")
            //下放单位时间脉冲数
            mStates.downPulsesPerUnitTime.set(info.numpput)
            //下放脉冲检测判断时间
            mStates.downPulseDetectionTime.set(
                DeviceStatusInfoProcessor.formatDoubleValue(
                    info.pdajtime,
                    "",
                    1
                )
            )

            val downSlowStartValue = info.lowsusranb.toFloat()
            val downSlowStopValue = info.lowsusrana.toFloat()
            //下放缓起区间终值(加速阶段)
            mStates.downSlowStartIntervalEndValue.set(downSlowStartValue.toString())
            //下放缓停区间起始值(减速阶段)
            mStates.downSlowStopIntervalStartValue.set(downSlowStopValue.toString())

            val leftProgress1 = if (holedepth == 0f) 0f else (downSlowStartValue / holedepth) * 100f
            val rightProgress1 =
                if (holedepth == 0f) 0f else (1 - downSlowStopValue / holedepth) * 100f

            decimalFormat.applyPattern("0")
            val formattedLeftProgress =
                decimalFormat.format(leftProgress1.coerceAtMost(49f)).toFloat()
            val formattedRightProgress =
                decimalFormat.format(rightProgress1.coerceAtLeast(50f)).toFloat()
            binding.seekBarDownInterval.setProgress(formattedLeftProgress, formattedRightProgress)

            //堵转检测区间起始值
            mStates.downStallDetectionIntervalStartValue.set(info.detintiona)
            //堵转检测区间终值
            mStates.downStallDetectionIntervalEndValue.set(info.detintionb)
            decimalFormat.applyPattern("0")
            val leftProgress2 = decimalFormat.format(info.detintiona.toFloat()).toFloat()
            var rightProgress2 = decimalFormat.format(info.detintionb.toFloat()).toFloat()
            //TODO 堵转检测区间右边进度条必须小于下放缓停区间的右边进度条数值
            rightProgress2 = rightProgress2.coerceAtMost(rightProgress1 - 1)
            //堵转检测区间
            binding.seekBarDownStallDetectionInterval.setProgress(
                leftProgress2,
                rightProgress2
            )

            //下放力矩堵转阈值
            mStates.downTorqueStallThreshold.set(
                DeviceStatusInfoProcessor.formatDoubleValue(
                    info.lowtorblothr,
                    "",
                    2
                )
            )
            //下放力矩检测判断时间
            mStates.downTorqueDetectionTime.set(
                DeviceStatusInfoProcessor.formatDoubleValue(
                    info.lowtordetime,
                    "",
                    1
                )
            )

            mStates.pullUpEnable.set(info.uptbtss == "1")
            //上拉缓起区间终值(加速阶段)
            mStates.pullUpSlowStartIntervalEndValue.set(
                info.upsusranb.replace(
                    "-",
                    ""
                )
            )
            //上拉缓停区间起始值(减速阶段)
            mStates.pullUpSlowStopIntervalStartValue.set(
                info.upsusrana.replace(
                    "-",
                    ""
                )
            )
            var leftProgress3 = measpacing.takeIf { it != 0f }
                ?.let { (mStates.pullUpSlowStartIntervalEndValue.get().toFloat() / it) * 100f }
                ?: 0f
            var rightProgress3 = measpacing.takeIf { it != 0f }
                ?.let { (1 - mStates.pullUpSlowStopIntervalStartValue.get().toFloat() / it) * 100f }
                ?: 0f
            decimalFormat.applyPattern("0")
            leftProgress3 = decimalFormat.format(leftProgress3.coerceAtMost(49f)).toFloat()
            rightProgress3 = decimalFormat.format(rightProgress3.coerceAtLeast(50f)).toFloat()
            binding.seekBarPullUpInterval.setProgress(
                leftProgress3,
                rightProgress3
            )

            //上拉力矩堵转阈值
            mStates.pullUpTorqueStallThreshold.set(
                DeviceStatusInfoProcessor.formatDoubleValue(
                    info.uptorblothr,
                    "",
                    2
                )
            )
            //上拉力矩检测判断时间
            mStates.pullUpTorqueDetectionTime.set(
                DeviceStatusInfoProcessor.formatDoubleValue(
                    info.uptordetime,
                    "",
                    1
                )
            )

        } catch (e: Exception) {
            Timber.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}