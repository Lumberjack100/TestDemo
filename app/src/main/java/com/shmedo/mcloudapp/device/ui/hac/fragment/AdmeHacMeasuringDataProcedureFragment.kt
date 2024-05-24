package com.shmedo.mcloudapp.device.ui.hac.fragment

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.animation.BounceInterpolator
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.blankj.utilcode.constant.RegexConstants
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.RegexUtils
import com.blankj.utilcode.util.StringUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.ext.launchWithViewLifecycle
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.hac.HacMeasuringDataEntity
import com.shmedo.lib.device.base.iot_cmd.enums.AdmeCTRMotionState
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.enums.ProductType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.model.hac.HacMotionState
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.FragmentAdmeHacMeasuringDataProcedureBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.AdmeHacMeasuringDataProcedureViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ext.nav
import com.shmedo.mcloudapp.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ext.showErrorProtectionTip
import com.shmedo.mcloudapp.ext.showLoadingDialog
import com.shmedo.mcloudapp.ext.showMessage
import com.shmedo.mcloudapp.utils.DeviceStatusInfoProcessor
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class AdmeHacMeasuringDataProcedureFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAdmeHacMeasuringDataProcedureBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: AdmeHacMeasuringDataProcedureViewModel
    private val iotParseManager: IOTParserManager by inject()

    private var soundPool: SoundPool? = null
    private var voiceMeasureFail = 0
    private var voiceMeasureSuccess = 0
    private var curCommandType = IOTCommandType.UNKNOWN_TYPE


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSoundPool()
    }

    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_adme_hac_measuring_data_procedure,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAdmeHacMeasuringDataProcedureBinding
        binding.llToolbar.toolbar.title = "数据测量"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            processBack()
        }
        registerOnBackPressedDispatcher {
            processBack()
        }
        toolbarViewModel.toolbarIvActionVisible.set(false)
    }

    override fun initData() {
        super.initData()
        arguments?.let {
            mStates.isCheckReverse.set(it.getBoolean(CHECK_REVERSE))
        }
    }

    private fun loadButtonAnimator() {
        binding.llMeasuringDataProcedureBottom.btnAction.animate()
            .scaleX(0.6f)
            .scaleY(0.6f)
            .setDuration(600)
            .setInterpolator(BounceInterpolator())
            .withEndAction {
                binding.llMeasuringDataProcedureBottom.btnAction.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(600)
                    .setInterpolator(BounceInterpolator())
                    .start()
            }
            .start()
    }

    /**
     * 获取电机的运行状态
     */
    private fun getMotorMotionData(timeMillis: Long = 0L) {
        if (mStates.isStopQueryMotorState.get()) return

        launchWithViewLifecycle {
            delay(timeMillis)

            commandItems.clear()
            curCommandType = IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE

            val command = IOTCommandUtil.getCommand(curCommandType)
            commandItems.add(command)
            sendCommandFromCmdList(
                isStartTimeoutJob = true,
                timeoutMillis = AppContants.Communication.DELAY_10000_MILLIS
            )
        }
    }

    /**
     * 停止测量
     */
    private fun stopMeasureAction() {
        //数据测量配置参数
        val entity = HacMeasuringDataEntity(
            equipmodel = "0",
        )
        commandItems.clear()
        curCommandType = IOTCommandType.ADME_HAC_MD_SET_DATA_MEASURE_PARAM
        val command = IOTCommandUtil.getCommand(
            curCommandType,
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
        mStates.isStopQueryMotorState.set(false)
        getMotorMotionData()
    }

    inner class ClickProxy : BaseClickProxy() {
        fun onActionClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            if (mStates.runButtonText.get() == "结束测量") {
                showStopWarnDialog()

            } else if (mStates.runButtonText.get() == "下一步") {
                if (mStates.motionStateWrapper.get().motorinfo == "8") {
                    processBack(false)
                    //等待下次测量,进入测量结果展示页面
                    nav().navigate(
                        R.id.action_global_to_admeHacMeasuringDataResultsFragment
                    )
                } else {
                    processBack(true)
                }
            }
        }
    }

    fun showStopWarnDialog() {
        showMessage("确定停止电机运动？", "温馨提示", "确定", {
            stopQueryMotorState()
            stopMeasureAction()
        }, "取消")
    }

    private fun stopQueryMotorState() {
        cancelNearbyCommunicationTimeoutJob()
        mStates.isStopQueryMotorState.set(true)
    }

    override fun showNearbyCommunicationTimeoutAlert(
        cmdStr: String,
        isDismissLoadingDialog: Boolean,
        isShowMsg: Boolean,
        msg: String
    ) {
        if (communicateWay is BleConnect && bleViewModel.isConnected())
            getMotorMotionData(DELAY_2000_MILLIS)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE -> {//获取ADME的运行状态
                val result = iotParseManager.parse<HacMotionState>(
                    cmdStr,
                    IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
//                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "获取设备的运行状态出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        getMotorMotionData(DELAY_5000_MILLIS)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        refreshMotionState(result.data)
                        sendCommandFromCmdList {
                            getMotorMotionData(DELAY_5000_MILLIS)
                        }
                    }
                }
            }

            IOTCommandType.ADME_HAC_MD_SET_DATA_MEASURE_PARAM -> {//设置HAC数据测量参数
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "停止电机出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            mStates.isRunButtonVisible.set(false)
                            mStates.motorInfo.set("本轮测量已停止,预计 ${getMinTime()} 分钟后可重新测量")
                        }
                    }
                }
            }

            IOTCommandType.LENGTH_INVALID -> {//接收的数据格式不符合物联网指令协议，进入此逻辑处理
                getMotorMotionData(DELAY_5000_MILLIS)
            }

            else -> {

            }
        }
    }

    /**
     * 实时刷新电机运动状态
     */
    private fun refreshMotionState(motionState: HacMotionState) {
        mStates.motionStateWrapper.set(motionState)
        if (mStates.isFirstQueryMotorState.get()) {
            mStates.isFirstQueryMotorState.set(false)
            mStates.measureMode.set(if (motionState.measmode == "0") "正向测量" else "反向测量")
        }

        //异常时，停止轮询电机运动状态，展示异常原因
        if (motionState.abndiasis != "0") {
            cancelNearbyCommunicationTimeoutJob()
            showErrorProtectionTip(motionState.abndiasis)
        }

        try {
            mStates.inclinometerBattery.set(motionState.incvoltage + "%")
            motionState.incvoltage.toDoubleOrNull()?.let {
                mStates.inclinometerBatteryColorRes.set(
                    if (it <= 20) ColorUtils.getColor(R.color.device_offline_platform) else ColorUtils.getColor(
                        R.color.device_online_platform
                    )
                )
            }
            mStates.deviceBattery.set(motionState.driveinputv + "%")
            motionState.driveinputv.toDoubleOrNull()?.let {
                mStates.deviceBatteryColorRes.set(
                    if (it <= 20) ColorUtils.getColor(R.color.device_offline_platform) else ColorUtils.getColor(
                        R.color.device_online_platform
                    )
                )
            }
            mStates.isVerticalProgressBarVisible.set(true)
            mStates.isWaitTimeVisible.set(false)
            when (AdmeCTRMotionState.valueByCode(motionState.motorinfo)) {
                AdmeCTRMotionState.NOZZLE_WAITING -> {//上拉至管口等待
                    mStates.isCurDepthVisible.set(false)
                    initVerticalProgress(motionState.measpoint)
                    mStates.motorInfo.set("上拉至管口...")
                }

                AdmeCTRMotionState.PAIR_SETTING_PARAM -> {//测斜仪配对
                    mStates.isCurDepthVisible.set(false)
                    initVerticalProgress(motionState.measpoint)
                    val msg =
                        if (motionState.measmode == "1" && mStates.isCheckReverse.get()) "测斜仪配对,反转自检..." else "测斜仪配对中..."
                    mStates.motorInfo.set(msg)
                }

                AdmeCTRMotionState.DOWN,//测斜仪下放
                AdmeCTRMotionState.BOTTOM_WAITING -> {//管底等待
                    mStates.isCurDepthVisible.set(false)
                    mStates.isWaitTimeVisible.set(true)
                    initVerticalProgress(motionState.measpoint)
                    mStates.motorInfo.set(
                        if (motionState.motorinfo == "2")
                            "测斜仪下放中..."
                        else
                            "管底等待中..."
                    )
                    mStates.waittimedesc.set(
                        if (motionState.motorinfo == "2")
                            "下放结束预计"
                        else
                            "距离开始测量预计"
                    )
                    mStates.waittime.set(String.format("%s分钟", getMinTime()))
                }

                AdmeCTRMotionState.POINT_MEASUREMENT -> {//测点测量
                    mStates.isCurDepthVisible.set(true)
                    mStates.isWaitTimeVisible.set(true)
                    updateVerticalProgress(motionState.measpoint)
                    mStates.motorInfo.set("测点测量中...")
                    mStates.waittimedesc.set("测量结束预计")
                    mStates.waittime.set(String.format("%s分钟", getMinTime()))
                }

                AdmeCTRMotionState.MEASUREMENT_OVER -> {//测点结束
                    mStates.verticalProgress.set(mStates.verticalMaxProgress.get())
                    mStates.motorInfo.set("准备读取数据...")
                }

                AdmeCTRMotionState.READ_DATA -> {//数据读取中
                    mStates.isVerticalProgressBarVisible.set(false)
                    mStates.isWaitTimeVisible.set(true)
                    mStates.isHorizontalProgressBarReadingData.set(true)//读取数据进度条颜色
                    updateHorizontalProgress(motionState.measpoint)
                    mStates.motorInfo.set("数据读取中...")
                    mStates.waittimedesc.set("数据读取结束预计")
                    mStates.waittime.set(String.format("%s分钟", getMinTime()))
                    mStates.isRunButtonVisible.set(false)
                }

                AdmeCTRMotionState.UPLOAD_DATA -> {//数据上传中
                    mStates.isVerticalProgressBarVisible.set(false)
                    mStates.isHorizontalProgressBarReadingData.set(false)//上传数据进度条颜色
                    updateHorizontalProgress(motionState.measpoint)
                    mStates.motorInfo.set("数据上传中...")
                    mStates.isRunButtonVisible.set(false)
                }

                AdmeCTRMotionState.WAITING_NEXT_TESTING,//等待下一次测量
                AdmeCTRMotionState.WAITING_BACK_TESTING -> {//等待反测
                    stopQueryMotorState()
                    mStates.isVerticalProgressBarVisible.set(false)
                    mStates.isHorizontalProgressBarReadingData.set(motionState.motorinfo == "9")//正反测模式下，正测阶段只有读取数据过程，没有上传数据，所以不展示上传数据进度框
                    setHorizontalMaxProgress()
                    mStates.motorInfo.set(if (motionState.motorinfo == "9") "测量完成,等待反向测量" else "测量完成")
                    mStates.isRunButtonVisible.set(true)
                    mStates.runButtonText.set("下一步")
                    //VibrateUtils.vibrate(100);
                    if (voiceMeasureSuccess != 0) {
                        playSound(voiceMeasureSuccess)
                    }
                    loadButtonAnimator()
                }

                AdmeCTRMotionState.FAILED -> {//测量失败
                    stopQueryMotorState()
                    mStates.motorInfo.set("测量失败")
                    mStates.isRunButtonVisible.set(true)
                    mStates.runButtonText.set("下一步")
                    if (voiceMeasureFail != 0) {
                        playSound(voiceMeasureFail)
                    }
                    loadButtonAnimator()
                }

                else -> {
                }
            }

        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun initVerticalProgress(measurePoint: String) {
        if (measurePoint.isEmpty() || !measurePoint.contains("|"))
            return

        try {
            val values = measurePoint.split("|")
            if (!TextUtils.isEmpty(values[1])) {
                val depth = values[1].toDouble()
                if (depth == 0.0) {
                    mStates.holeDepth.set("测斜管深度 -- 米")
                    return
                }
                mStates.holeDepthValue.set(depth)
                mStates.holeDepth.set(
                    "测斜管深度 ${
                        DeviceStatusInfoProcessor.formatDoubleValue(
                            depth.toString(),
                            "0",
                            1
                        )
                    } 米"
                )
                mStates.verticalMaxProgress.set((depth * 10).toInt())
            }
            mStates.verticalProgress.set(0)

        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun updateVerticalProgress(measurePoint: String) {
        if (measurePoint.isEmpty() || !measurePoint.contains("|"))
            return

        try {
            var depth = mStates.holeDepthValue.get()
            val values = measurePoint.split("|")
            if (mStates.holeDepthValue.get() <= 0.0) {
                if (!TextUtils.isEmpty(values[1])) {
                    depth = values[1].toDouble()
                    if (depth <= 0.0) {
                        mStates.holeDepth.set("测斜管深度 -- 米")
                        return
                    }
                    mStates.holeDepthValue.set(depth)
                    mStates.holeDepth.set(
                        "测斜管深度 ${
                            DeviceStatusInfoProcessor.formatDoubleValue(
                                depth.toString(),
                                "0",
                                1
                            )
                        } 米"
                    )
                    mStates.verticalMaxProgress.set((depth * 10).toInt())
                }
            }
            if (TextUtils.isEmpty(values[0])) {
                mStates.verticalProgress.set(0)
            } else {
                val value = values[0].toDouble()
                mStates.curDepth.set(
                    "当前测点位置 ${
                        DeviceStatusInfoProcessor.formatDoubleValue(
                            (depth - value).toString(),
                            "0",
                            1
                        )
                    } 米"
                )
                mStates.verticalProgress.set((value * 10).toInt())
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun updateHorizontalProgress(measurePoint: String) {
        if (measurePoint.isEmpty() || !measurePoint.contains("|"))
            return

        try {
            var progress = 0
            val values = measurePoint.split("|")
            if (!TextUtils.isEmpty(values[1]) && RegexUtils.isMatch(
                    RegexConstants.REGEX_INTEGER,
                    values[1]
                )
            ) {
                mStates.horizontalMaxProgress.set(values[1].toInt())
            }
            if (!TextUtils.isEmpty(values[0])) {
                progress = values[0].toInt()
                mStates.horizontalProgress.set(progress)
            }
            mStates.processDataNum.set("${values[0]}/${values[1]}")
            val result =
                if ((mStates.horizontalMaxProgress.get() == 0)) 0f else progress.toFloat() / mStates.horizontalMaxProgress.get()
            mStates.processDataPercent.set(
                "${
                    DeviceStatusInfoProcessor.formatDoubleValue(
                        (result * 100).toString(),
                        "0",
                        0
                    )
                } %"
            )
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun setHorizontalMaxProgress() {
        mStates.horizontalProgress.set(mStates.horizontalMaxProgress.get())
        mStates.processDataNum.set(
            "${mStates.horizontalMaxProgress.get()}/${mStates.horizontalMaxProgress.get()}"

        )
        mStates.processDataPercent.set("100%")
    }


    private fun getMinTime(): String {
        val decimalFormat = DecimalFormat("#", DecimalFormatSymbols(Locale.getDefault()))
        mStates.motionStateWrapper.get().let { motionState ->
            if (TextUtils.isEmpty(motionState.waittime)) return "--"
            try {
                val second = motionState.waittime.toInt()
                val min = second.toFloat() / 60 + 1

                return decimalFormat.format(min.toDouble())
            } catch (exception: Exception) {
                Timber.e(exception)
                return "--"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    private fun processBack(isNavUp: Boolean = true) {
        launchWithViewLifecycle {
            delay(500)
            //巡护事件需要给上一级浏览页面传递最新的事件信息
            setFragmentResult(
                AppContants.Extras.FRAGMENT_MEASURING_DATA_PROCEDURE_RESULT_REQUEST_KEY,
                bundleOf(AppContants.Extras.MOTOR_STATE to mStates.motionStateWrapper.get())
            )
            if (isNavUp)
                nav().navigateUp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseSoundPool()
    }

    /**
     * 测量完成或失败后播放的提示音初始化
     */
    private fun initSoundPool() {
        //AudioAttributes是一个封装音频各种属性的方法
        val audioAttrs = AudioAttributes.Builder()
            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttrs)
            .build()

        soundPool?.let {
            voiceMeasureFail = it.load(context, R.raw.measure_fail, 1)
            voiceMeasureSuccess = it.load(context, R.raw.measure_success, 1)
        }
    }

    private fun playSound(soundId: Int) {
        soundPool?.let {
            if (soundId != 0) {
                it.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
            }
        }
    }

    private fun releaseSoundPool() {
        soundPool?.release()
        soundPool = null
    }

    companion object {
        const val DELAY_2000_MILLIS = 2000L
        const val DELAY_5000_MILLIS = 5000L
        const val CHECK_REVERSE: String = "check_reverse"

        fun newBundleArguments(
            isCheckReverse: Boolean,
            type: ProductType = ProductType.UnKnown,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putBoolean(CHECK_REVERSE, isCheckReverse)
            putParcelable(AppContants.Extras.PRODUCT_TYPE, type)
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}