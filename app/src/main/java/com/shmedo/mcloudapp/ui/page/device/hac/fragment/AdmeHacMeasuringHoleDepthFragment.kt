package com.shmedo.mcloudapp.ui.page.device.hac.fragment

import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Filter
import androidx.core.widget.doAfterTextChanged
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.RegexUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.mmkv.MmkvCacheUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.AdmeConfigInfo
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.hac.HacMeasuringHoleDepthInfoEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.hac.HacHoleAreaDepthInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.hac.HacMeasuringHoleDepthInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.hac.HacMotorMotionDistanceInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentAdmeHacMeasuringHoleDepthBinding
import com.shmedo.mcloudapp.extensions.getAdmeErrorMsg
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.isViewLifecycleActive
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.BleConnect
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.hac.dialog.AdmeHacAutoMeasuringHoleDepthBottomDialog
import com.shmedo.mcloudapp.ui.page.device.hac.dialog.AdmeHacManualMeasuringHoleDepthBottomDialog
import com.shmedo.mcloudapp.ui.page.device.hac.dialog.HacConfigNumberBottomDialog
import com.shmedo.mcloudapp.ui.viewmodel.request.AdmeConfigViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.AdmeHacMeasuringHoleDepthViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.utils.IOTRegexContants
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import kotlin.math.abs

class AdmeHacMeasuringHoleDepthFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAdmeHacMeasuringHoleDepthBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private val admeConfigViewModel: AdmeConfigViewModel by viewModel()
    private lateinit var mStates: AdmeHacMeasuringHoleDepthViewModel
    private val iotParseManager: IOTParserManager by inject()

    private val measureWayList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_measure_hole_depth_method) }
    private val motionTypeList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_measure_hole_depth_motor_motion_type) }

    private val projectNumList = arrayListOf<String>()
    private val areaNumList = arrayListOf<String>()
    private val holeNumList = arrayListOf<String>()
    private val holeAreaDepthInfoArrayList = ArrayList<HacHoleAreaDepthInfo>()
    private var configProjectNum: String = "" //之前配置的项目编号
    private var configAreaNum: String = "" //之前配置的区域编号
    private var configHoleNum: String = "" //之前配置的孔编号

    private var safeDistance: String = "" //安全距离补偿

    private var autoMeasuringHoleDepthBottomDialog: AdmeHacAutoMeasuringHoleDepthBottomDialog? =
        null
    private var manualMeasuringHoleDepthBottomDialog: AdmeHacManualMeasuringHoleDepthBottomDialog? =
        null

    private var queryMotionStateJob: Job? = null


    override fun initViewModel() {
        super.initViewModel()
        toolbarViewModel = getFragmentScopeViewModel()
        mStates = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_adme_hac_measuring_hole_depth,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentAdmeHacMeasuringHoleDepthBinding
        binding.llToolbar.toolbar.title = "孔深测量"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
        toolbarViewModel.toolbarIvActionVisible.set(false)
        initRefresh()
        initOtherListener()
        setProjectNumberAdapter()
        setAreaNumberAdapter()
        setHoleNumberAdapter()
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

    // 扩展函数：设置不过滤的下拉适配器
    private fun MaterialAutoCompleteTextView.setUnfilteredAdapter(
        items: List<String>,
        onItemSelected: (String) -> Unit
    ) {
        val adapter = object : ArrayAdapter<String>(
            context,
            R.layout.simple_dropdown_item_line,
            items
        ) {
            override fun getFilter(): Filter = object : Filter() {
                override fun performFiltering(prefix: CharSequence?): FilterResults {
                    return FilterResults().apply {
                        values = items
                        count = items.size
                    }
                }

                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    notifyDataSetChanged()
                }
            }
        }
        setAdapter(adapter)
        onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            onItemSelected(items[position])
        }
    }

    private fun setProjectNumberAdapter() {
        binding.projectNum.setUnfilteredAdapter(projectNumList) { projectNum ->
            if (mStates.projectNum.get() == projectNum) {
                return@setUnfilteredAdapter
            }
            mStates.projectNum.set(projectNum)
            //清空下级选项
            mStates.areaNum.set("")
            mStates.holeNum.set("")

            launchWithViewLifecycle {
                try {
                    admeConfigViewModel.queryAllAreaID(projectNum)?.let { areaList ->
                        areaNumList.clear()
                        areaNumList.addAll(areaList)
//                        setAreaNumberAdapter()
                    }
                } catch (ex: Exception) {
                    Timber.e(ex, "Failed to load area IDs")
                }
            }
        }
    }

    private fun setAreaNumberAdapter() {
        binding.areaNum.setUnfilteredAdapter(areaNumList) { areaNum ->
            if (mStates.areaNum.get() == areaNum) {
                return@setUnfilteredAdapter
            }
            mStates.areaNum.set(areaNum)
            mStates.holeNum.set("")

            launchWithViewLifecycle {
                try {
                    admeConfigViewModel.queryAllHoleNumber(
                        mStates.projectNum.get(),
                        areaNum
                    )?.let { holeList ->
                        holeNumList.clear()
                        holeNumList.addAll(holeList)
//                        setHoleNumberAdapter()
                    }
                } catch (ex: Exception) {
                    Timber.e(ex, "Failed to load hole numbers")
                }
            }
        }
    }

    private fun setHoleNumberAdapter() {
        binding.holeNum.setUnfilteredAdapter(holeNumList) { holeNum ->
            if (mStates.holeNum.get() == holeNum) {
                return@setUnfilteredAdapter
            }
            mStates.holeNum.set(holeNum)
            launchWithViewLifecycle {
                try {
                    //加载孔号配置信息
                    admeConfigViewModel.queryConfigByHoleNumber(
                        mStates.projectNum.get(),
                        mStates.areaNum.get(),
                        holeNum
                    )
                        ?.let { configInfo ->
                            handleConfigInfo(configInfo)
                        }
                } catch (ex: Exception) {
                    Timber.e(ex, "Failed to load area IDs")
                }
            }
        }
    }

    private fun initOtherListener() {
        binding.etMotorSpeed.doAfterTextChanged { text: Editable? ->
            if (text.isNullOrEmpty()) {
                return@doAfterTextChanged
            }
            if (mStates.motionType.get() == motionTypeList[0]) {
                val speed = text.toString().toInt()
                if (speed > 10) {
                    showMessageDialog("上拉触发磁开关最大速度为 10！")
                }
            }
        }
    }

    override fun initData() {
        super.initData()
        mStates.isAutoMeasuringMode.set(true) //默认自动测量模式
        mStates.measureWay.set(measureWayList[0])//自动测孔深
        mStates.motionType.set(motionTypeList[0])//上拉
        loadAutoLastHistoryData()
    }

    /**
     * 自动测孔深模式加载本地缓存的参数
     */
    private fun loadAutoLastHistoryData() {
        mStates.downSpeed.set(MmkvCacheUtil.getAdmeAutoLastMotorDropSpeed())
    }

    /**
     * 手动测孔深模式加载本地缓存的参数
     */
    private fun loadManualLastHistoryData() {
        if (mStates.motionType.get() == motionTypeList[0]) {//上拉
            mStates.speed.set(MmkvCacheUtil.getAdmeManualLastMotorPullUpSpeed())
            mStates.distanceGoal.set(MmkvCacheUtil.getAdmeManualLastMotorPullUpDistance())
        } else {
            mStates.speed.set(MmkvCacheUtil.getAdmeManualLastMotorDropSpeed())
            mStates.distanceGoal.set(MmkvCacheUtil.getAdmeManualLastMotorDropDistance())
        }
    }

    private fun loadAdmeConfigData() {
        launchWithViewLifecycle {
            try {
                //1. 加载项目编号列表
                admeConfigViewModel.queryAllProjectID()?.let { projectIds ->
                    projectNumList.clear()
                    projectNumList.addAll(projectIds)
//                    setProjectNumberAdapter()
                }

                //2. 加载设备配置信息
                admeConfigViewModel.queryConfigByDeviceSN(deviceInfo.deviceToken)
                    ?.let { configInfo ->
                        handleConfigInfo(configInfo)
                    }

                //3. 如果有已配置的项目号，加载对应的区域号列表
                if (configProjectNum.isNotEmpty() && projectNumList.contains(configProjectNum)) {
                    mStates.projectNum.set(configProjectNum)
                    admeConfigViewModel.queryAllAreaID(configProjectNum)?.let { areaList ->
                        areaNumList.clear()
                        areaNumList.addAll(areaList)
//                        setAreaNumberAdapter()
                    }
                }

                //4. 如果有已配置的区域号，加载对应的孔号列表
                if (configAreaNum.isNotEmpty() && areaNumList.contains(configAreaNum)) {
                    mStates.areaNum.set(configAreaNum)
                    admeConfigViewModel.queryAllHoleNumber(configProjectNum, configAreaNum)
                        ?.let { holeList ->
                            holeNumList.clear()
                            holeNumList.addAll(holeList)
//                            setHoleNumberAdapter()

                            if (holeNumList.contains(configHoleNum)) {
                                mStates.holeNum.set(configHoleNum)
                                mStates.runButtonText.set("重测孔深")
                            }
                        }
                }

                //添加这行来保存初始状态
                mStates.saveInitialState()
            } catch (ex: Exception) {
                Timber.e(ex, "Failed to load config data")
                ex.printStackTrace()
            }
        }
    }

    //处理配置信息
    private fun handleConfigInfo(configInfo: AdmeConfigInfo) {
        try {
            configProjectNum = configInfo.projectID
            configAreaNum = configInfo.areaNumber
            configHoleNum = configInfo.holeNumber

            val configMap = if (configInfo.config.isEmpty()) {
                mapOf()
            } else {
                MoshiUtil.fromJson<Map<String, String>>(configInfo.config) ?: mapOf()
            }

            mStates.realHoleDepth.set(configMap["realHoleDepth"].orEmpty())
            mStates.recommendHoleDepth.set(configMap["recommendHoleDepth"].orEmpty())
            //添加这行来保存初始状态
            mStates.saveInitialState()

        } catch (ex: Exception) {
            Timber.e(ex, "Failed to parse config info")
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 测量孔深模式
         */
        fun onMeasureModeClick() {
            val selectedIndex = measureWayList.indexOf(mStates.measureWay.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", measureWayList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.measureWay.set(text)
                        mStates.isAutoMeasuringMode.set(position == 0)
                        if (position == 0)
                            loadAutoLastHistoryData()
                        else
                            loadManualLastHistoryData()

                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        /**
         * 选择运动方式
         */
        fun onMotionTypeClick() {
            val selectedIndex = motionTypeList.indexOf(mStates.motionType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", motionTypeList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.motionType.set(text)
                        loadManualLastHistoryData()
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            mStates.decentralizedEnable.set(isChecked)
        }

        fun onRunClick() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }

        fun onEditNumberClick(view: View) {
            when (view.id) {
                R.id.btn_edit_project_num -> {
                    showHacConfigNumberBottomDialog(
                        "项目号",
                        mStates.projectNum.get()
                    ) { newNumber ->
                        mStates.projectNum.set(newNumber)
                    }
                }

                R.id.btn_edit_area_num -> {
                    showHacConfigNumberBottomDialog(
                        "区号",
                        mStates.areaNum.get()
                    ) { newNumber ->
                        mStates.areaNum.set(newNumber)
                    }
                }

                R.id.btn_edit_hole_num -> {
                    showHacConfigNumberBottomDialog(
                        "孔号",
                        mStates.holeNum.get()
                    ) { newNumber ->
                        mStates.holeNum.set(newNumber)
                    }
                }
            }
        }

        /**
         * 保存孔深测量配置
         */
        fun onSaveHoleConfig() {
            KeyboardUtils.hideSoftInput(binding.root)
            if (mStates.projectNum.get().isEmpty()) {
                showMessageDialog("请设置项目号!")
                return
            }
            if (mStates.areaNum.get().isEmpty()) {
                showMessageDialog("请设置区号!")
                return
            }
            if (mStates.holeNum.get().isEmpty()) {
                showMessageDialog("请设置孔号!")
                return
            }
            launchWithViewLifecycle {
                try {
                    val configMap = mapOf(
                        "realHoleDepth" to mStates.realHoleDepth.get(),
                        "recommendHoleDepth" to mStates.recommendHoleDepth.get(),
                    )
                    val configJson = MoshiUtil.toJson(configMap)
                    admeConfigViewModel.manageConfig(
                        deviceInfo.deviceToken,
                        mStates.projectNum.get(),
                        mStates.areaNum.get(),
                        mStates.holeNum.get(),
                        configJson
                    ) { error: Throwable ->
                        showMessage("保存失败")
                        Timber.e(error, "Failed to save config")
                    } ?: return@launchWithViewLifecycle

                    //添加这行来保存初始状态
                    mStates.saveInitialState()
                    showMessage("保存成功")
                } catch (ex: Exception) {
                    showMessage("保存失败")
                    Timber.e(ex, "Failed to save config")
                }
            }
        }
    }

    /**
     * 显示设置调查编号底部弹窗
     */
    private fun showHacConfigNumberBottomDialog(
        title: String,
        currentNumber: String,
        onConfirm: (String) -> Unit
    ) {
        val popupView = HacConfigNumberBottomDialog.create(mActivity) {
            setTitle(title)
            setNumber(currentNumber)
            setOnConfirm { newNumber, callback ->
                val isValid = checkHacNumberIsValid(
                    title,
                    newNumber
                )
                if (isValid) {
                    onConfirm(newNumber)
                    callback.dismiss()
                }
            }
        }

        XPopup.Builder(context)
            .isViewMode(true)
            .autoOpenSoftInput(true)
            .enableDrag(false)
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .asCustom(popupView)
            .show()
    }

    /**
     * 检查编号是否有效
     */
    private fun checkHacNumberIsValid(
        title: String,
        number: String,
    ): Boolean {
        when (title) {
            "项目号" -> {
                if (number.isEmpty()) {
                    Toaster.show("请选择项目号")
                    return false
                }
                if (projectNumList.contains(number)) {
                    Toaster.show("此项目号已存在，换一个试试")
                    return false
                }
            }

            "区号" -> {
                if (number.isEmpty()) {
                    Toaster.show("请选择区号")
                    return false
                }
                if (areaNumList.contains(number)) {
                    Toaster.show("此区号已存在，换一个试试")
                    return false
                }
            }

            "孔号" -> {
                if (number.isEmpty()) {
                    Toaster.show("请选择孔号")
                    return false
                }
                if (holeNumList.contains(number)) {
                    Toaster.show("此孔号已存在，换一个试试")
                    return false
                }
            }
        }
        return true
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
        if (mStates.projectNum.get().isEmpty()) {
            showMessageDialog("请选择项目号!")
            return
        }
        if (mStates.areaNum.get().isEmpty()) {
            showMessageDialog("请选择区号!")
            return
        }
        if (mStates.holeNum.get().isEmpty()) {
            showMessageDialog("请选择孔号!")
            return
        }
        if (mStates.isAutoMeasuringMode.get()) {
            if (mStates.downSpeed.get().isEmpty()) {
                showMessageDialog("请输入下放速度!")
                return
            }
            try {
                val value = mStates.downSpeed.get().toDouble()
                if (value < 1 || value > 100) {
                    showMessageDialog("电机下放速度数值范围[1,100]!")
                    return
                }
            } catch (ex: Exception) {
                showMessageDialog("电机下放速度数值范围[1,100]!")
                return
            }
        } else {//手动测量孔深模式
            if (mStates.speed.get().isEmpty()) {
                showMessageDialog("请输入电机速度!")
                return
            }
            try {
                val value = mStates.speed.get().toDouble()
                if (value < 1 || value > 200) {
                    showMessageDialog("电机速度数值范围[1,200]!")
                    return
                }
            } catch (ex: Exception) {
                showMessageDialog("电机速度数值范围[1,200]!")
                return
            }
            if (mStates.distanceGoal.get().isEmpty()) {
                showMessageDialog("请输入设定运动距离!")
                return
            }
            try {
                val value = mStates.distanceGoal.get().toDouble()
                if (value < 0) {
                    showMessageDialog("设定运动距离不能小于 0!")
                    return
                }
            } catch (ex: Exception) {
                showMessageDialog("请输入正确的设定运动距离!")
                return
            }
        }
        setMeasuringHoleDepth()
    }

    private fun setMeasuringHoleDepth() {
        commandItems.clear()

        //清空电机运动脉冲数据记录
        var command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_MD_CLEAR_MEASURING_HOLEDEPTH_DATA
        )
        commandItems.add(command)

        if (mStates.isAutoMeasuringMode.get())
            setAutoMeasuringHoleDepth()
        else
            setManualMeasuringHoleDepth()

        //保存参数指令
        command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_SAVE_CONFIG_PARAM
        )
        commandItems.add(command)

        showLoadingDialog(StringUtils.getString(R.string.processing))
        sendCommandFromCmdList(
            isStartTimeoutJob = true,
            timeoutMillis = AppContants.Communication.DELAY_15000_MILLIS
        )
    }

    /**
     * 自动测孔深配置参数
     */
    private fun setAutoMeasuringHoleDepth() {
        //持久化保存电机速度
        MmkvCacheUtil.setAdmeAutoLastMotorDropSpeed(mStates.downSpeed.get())
        mStates.realHoleDepth.set("0")
        mStates.recommendHoleDepth.set("0")

        val entity = HacMeasuringHoleDepthInfoEntity(
            model = "0",
            address = mStates.address.get(),
            projectno = mStates.projectNum.get(),
            areano = mStates.areaNum.get(),
            holeno = mStates.holeNum.get(),
            lowtbtss = "1",//自动测孔深，默认打开下放堵转检测
            motorspeed = mStates.downSpeed.get(),
            measway = "0",
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_HAC_MD_SET_HOLE_MEASURE_PARAM,
            entity.toCommandString()
        )
        commandItems.add(command)
    }

    /**
     * 手动测孔深配置参数
     */
    private fun setManualMeasuringHoleDepth() {
        if (mStates.motionType.get() == motionTypeList[0]) {//上拉
            MmkvCacheUtil.setAdmeManualLastMotorPullUpSpeed(mStates.speed.get())
            MmkvCacheUtil.setAdmeManualLastMotorPullUpDistance(mStates.distanceGoal.get())
        } else {
            MmkvCacheUtil.setAdmeManualLastMotorDropSpeed(mStates.speed.get())
            MmkvCacheUtil.setAdmeManualLastMotorDropDistance(mStates.distanceGoal.get())
        }

        val entity = HacMeasuringHoleDepthInfoEntity(
            model = "0",
            address = mStates.address.get(),
            projectno = mStates.projectNum.get(),
            areano = mStates.areaNum.get(),
            holeno = mStates.holeNum.get(),
            lowtbtss = if (mStates.decentralizedEnable.get()) "1" else "0",
            motorspeed = mStates.speed.get(),
            measway = "1",
            movementway = if (mStates.motionType.get() == motionTypeList[0]) "0" else "1",
            movedistance = mStates.distanceGoal.get()
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_HAC_MD_SET_HOLE_MEASURE_PARAM,
            entity.toCommandString()
        )
        commandItems.add(command)
    }

    /**
     * 查询ADME测孔深运动的脉冲数、运动距离
     */
    private fun getMotorMotionData(timeMillis: Long = 0L) {
        if (mStates.isStopQueryMotorState.get()) return

        // 启动一个新的协程作为超时Job
        queryMotionStateJob?.cancel()
        queryMotionStateJob = launchWithViewLifecycle {
            delay(timeMillis)

            commandItems.clear()
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.ADME_HAC_MD_GET_HOLE_MEASURE_PULSE
            )
            commandItems.add(command)
            sendCommandFromCmdList(isStartTimeoutJob = true)
        }
    }

    /**
     * 继续电机运动
     */
    private fun continueMotorMotion() {
        try {
            commandItems.clear()
            val entity = HacMeasuringHoleDepthInfoEntity(
                model = "1",
                movementway = if (mStates.motionType.get() == motionTypeList[0]) "0" else "1",
            )
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.ADME_HAC_MD_SET_HOLE_MEASURE_PARAM,
                entity.toCommandString()
            )
            commandItems.add(command)
            sendCommandFromCmdList(isStartTimeoutJob = true)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * 停止或者暂停电机运动
     */
    private fun stopMotorMotion() {
        stopQueryMotorState()

        launchWithViewLifecycle {
            delay(500)

            commandItems.clear()
            val command = IOTCommandUtil.getCommand(
                IOTCommandType.ADME_MD_STOP_MEASURING_HOLEDEPTH
            )
            commandItems.add(command)
            sendCommandFromCmdList(isStartTimeoutJob = true)
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
        loadAdmeConfigData()
    }

    private fun queryParamData() {
        commandItems.clear()

        //获取HAC的孔深测量配置参数
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.ADME_HAC_MD_GET_HOLE_MEASURE_PARAM
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

        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.ADME_HAC_MD_GET_HOLE_MEASURE_PULSE,
                -> {
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr = cmdStr,
                    isDismissLoadingDialog = isDismissLoadingDialog,
                    isShowErrMsg = false,
                    isMessageDialog = isMessageDialog,
                    errMsg = errMsg
                )
                getMotorMotionData(DELAY_2000_MILLIS)
            }

            else -> {
                super.showNearbyCommunicationTimeoutAlert(
                    cmdStr = cmdStr,
                    isDismissLoadingDialog = isDismissLoadingDialog,
                    isShowErrMsg = isShowErrMsg,
                    isMessageDialog = isMessageDialog,
                    errMsg = errMsg
                )
            }
        }
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.ADME_HAC_MD_GET_HOLE_MEASURE_PARAM -> {//获取孔深测量配置参数
                val result = iotParseManager.parse<HacMeasuringHoleDepthInfo>(
                    cmdStr,
                    IOTCommandType.ADME_HAC_MD_GET_HOLE_MEASURE_PARAM
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "获取测量孔深配置参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList {
                            binding.refreshLayout.finish()
                        }
                        initMeasuringHoleDepthInfoParams(result.data)
                    }
                }
            }

            IOTCommandType.ADME_MD_CLEAR_MEASURING_HOLEDEPTH_DATA -> {//清空脉冲数记录
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "清空脉冲数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        safeDistance = ""
                        sendCommandFromCmdList()
                    }
                }
            }

            IOTCommandType.ADME_HAC_MD_SET_HOLE_MEASURE_PARAM -> {//设置孔深测量参数,开始测量
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "设置孔深测量参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            if (mStates.isAutoMeasuringMode.get()) {
                                showAutoMotorMotionBottomDialog()
                            } else {
                                showManualMotorMotionBottomDialog()
                            }
                        }
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
                            if (mStates.isAutoMeasuringMode.get()) {
                                showAutoMotorMotionBottomDialog()
                            } else {
                                showManualMotorMotionBottomDialog()
                            }
                        }
                    }
                }
            }

            IOTCommandType.ADME_HAC_MD_GET_HOLE_MEASURE_PULSE -> {//查询ADME测孔深运动的脉冲数、运动距离
                val result = iotParseManager.parse<HacMotorMotionDistanceInfo>(
                    cmdStr,
                    IOTCommandType.ADME_HAC_MD_GET_HOLE_MEASURE_PULSE
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        //cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "获取电机的实时运动数据出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
                        getMotorMotionData(DELAY_2000_MILLIS)
                        return
                    }

                    is IOTCommandResult.Success -> {
                        sendCommandFromCmdList()
                        if (mStates.isAutoMeasuringMode.get()) {
                            updateMotorMotionDistance(result.data)
                            autoMeasuringHoleDepthBottomDialog?.let { dialog ->
                                if (dialog.isViewLifecycleActive()) {
                                    updateMotionData(result.data)
                                }
                            }
                        } else {
                            manualMeasuringHoleDepthBottomDialog?.let { dialog ->
                                if (dialog.isViewLifecycleActive()) {
                                    updateMotionData(result.data)
                                }
                            }
                        }
                    }
                }
            }

            IOTCommandType.ADME_MD_STOP_MEASURING_HOLEDEPTH -> {//停止电机运动
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "停止电机出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList()
                    }
                }
            }

            else -> {
                cancelNearbyCommunicationTimeoutJob()
            }
        }
    }

    private fun initMeasuringHoleDepthInfoParams(hacMeasuringHoleDepthInfo: HacMeasuringHoleDepthInfo) {
        try {
            mStates.address.set(hacMeasuringHoleDepthInfo.address)
            mStates.decentralizedEnable.set(hacMeasuringHoleDepthInfo.lowtbtss == "1")

//            holeNumList.clear()
//            holeAreaDepthInfoArrayList.clear()
//            if (hacMeasuringHoleDepthInfo.holelist.isNotEmpty()) {
//                holeAreaDepthInfoArrayList.addAll(hacMeasuringHoleDepthInfo.holelist)
//                holeAreaDepthInfoArrayList.forEach {
//                    holeNumList.add(it.holeno)
//                }
//            }
//            binding.holeNum.setDatas(holeNumList)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun showAutoMotorMotionBottomDialog() {
        autoMeasuringHoleDepthBottomDialog =
            AdmeHacAutoMeasuringHoleDepthBottomDialog.newInstance().apply {
                setOnDialogFragmentClickListener(object :
                    AdmeHacAutoMeasuringHoleDepthBottomDialog.OnDialogFragmentClickListener {
                    override fun onCloseClick() {
                        if (!bleViewModel.isConnected() || mStates.isExitButtonVisible.get()) {
                            resetPulseData()
                            dismiss()
                            return
                        }
                        showMessage("确认退出数据运行吗?", "温馨提示", "确定", {
                            resetPulseData()
                            //蓝牙未断开时先发送停止电机指令，再关闭运行页面
                            if (bleViewModel.isConnected()) {
                                stopMotorMotion()
                            }
                            dismiss()
                        }, "取消")
                    }

                    override fun onRefresh() {
                        if (communicateWay is BleConnect && bleViewModel.isConnected() && !mStates.isStopQueryMotorState.get()) {
                            getMotorMotionData(DELAY_2000_MILLIS)
                        }
                    }

                    override fun onStopClick() {
                        if (isBleDisconnected()) {
                            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                            return
                        }
                        mStates.isExitButtonVisible.set(true)
                        stopMotorMotion()
                    }

                    override fun onExitClick() {
                        resetPulseData()
                        dismiss()
                    }
                })
            }
        autoMeasuringHoleDepthBottomDialog?.show(childFragmentManager, "dialog")
        mStates.isStopQueryMotorState.set(false)
        getMotorMotionData(DELAY_2000_MILLIS)
    }

    private fun showManualMotorMotionBottomDialog() {
        //数据运行弹框已经显示了
        if (manualMeasuringHoleDepthBottomDialog != null && manualMeasuringHoleDepthBottomDialog!!.isVisible) {
            Timber.d(
                "Continue Motion:curDistance=%s,curPulse=%s",
                mStates.motionDistance.get(),
                mStates.motionPulse.get()
            )
            mStates.isStopQueryMotorState.set(false)
            getMotorMotionData(DELAY_2000_MILLIS)
            return
        }
        manualMeasuringHoleDepthBottomDialog =
            AdmeHacManualMeasuringHoleDepthBottomDialog.newInstance().apply {
                setOnDialogFragmentClickListener(object :
                    AdmeHacManualMeasuringHoleDepthBottomDialog.OnDialogFragmentClickListener {
                    override fun onCloseClick() {
                        if (!bleViewModel.isConnected() || mStates.isExitButtonVisible.get()) {
                            resetPulseData()
                            dismiss()
                            return
                        }
                        showMessage("确认退出数据运行吗?", "温馨提示", "确定", {
                            resetPulseData()
                            //蓝牙未断开时先发送停止电机指令，再关闭运行页面
                            if (bleViewModel.isConnected()) {
                                stopMotorMotion()
                            }
                            dismiss()
                        }, "取消")
                    }

                    override fun onRefresh() {
                        if (communicateWay is BleConnect && bleViewModel.isConnected() && !mStates.isStopQueryMotorState.get()) {
                            getMotorMotionData(DELAY_2000_MILLIS)
                        }
                    }

                    override fun onStopClick() {
                        if (isBleDisconnected()) {
                            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                            return
                        }
                        mStates.isExitButtonVisible.set(mStates.isDoManualStopAction.get())
                        stopMotorMotion()
                    }

                    override fun onPauseClick() {
                        if (isBleDisconnected()) {
                            Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                            return
                        }
                        if (mStates.pauseButtonText.get() == "继续") {
                            mStates.pauseButtonText.set("暂停")
                            continueMotorMotion()
                        } else {
                            mStates.pauseButtonText.set("继续")
                            stopMotorMotion()
                        }
                    }

                    override fun onExitClick() {
                        resetPulseData()
                        dismiss()
                    }
                })
            }
        manualMeasuringHoleDepthBottomDialog?.show(childFragmentManager, "dialog")
        mStates.isStopQueryMotorState.set(false)
        getMotorMotionData(DELAY_2000_MILLIS)
    }

    private fun updateMotorMotionDistance(motorMotionDistanceInfo: HacMotorMotionDistanceInfo) {
        try {
            if (safeDistance.isEmpty()) {
                safeDistance = motorMotionDistanceInfo.realholedepth
                Timber.d("safeDistance=$safeDistance")
            }
            if (motorMotionDistanceInfo.realholedepth.isNotEmpty() && safeDistance.isNotEmpty()) {
                val holeValue = abs(motorMotionDistanceInfo.realholedepth.toDouble())
                val safeValue = abs(safeDistance.toDouble())
//                Timber.d("safeDistance=$safeDistance，holeValue=$holeValue")
                //测孔深值不等于安全补偿距离表示测孔深值有效
                if (holeValue != safeValue) {
                    mStates.realHoleDepth.set(motorMotionDistanceInfo.realholedepth)
                    mStates.recommendHoleDepth.set(motorMotionDistanceInfo.recoholedepth)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * 实时刷新脉冲和运动距离
     */
    private fun updateMotionData(motorMotionDistanceInfo: HacMotorMotionDistanceInfo) {
        //异常码 99 表示上拉到管口，测量结束，停止轮询脉冲数并发送停止电机运动指令
        if (motorMotionDistanceInfo.abndiasis.contains("99")) {
            mStates.isDoManualStopAction.set(true)
            stopMotorMotion()
            return
        }
        try {
            mStates.motionPulse.set(motorMotionDistanceInfo.pulsenumber)
            mStates.motionDistance.set(motorMotionDistanceInfo.realmovedistance)
            //CTR 工作异常
            if (motorMotionDistanceInfo.abndiasis.isNotEmpty() && motorMotionDistanceInfo.abndiasis != "0") {
                val errorMsg = getAdmeErrorMsg(motorMotionDistanceInfo.abndiasis, delimiters = ";")
                if (errorMsg.isEmpty()) {
                    return
                }
                mStates.isMotorInfoNormal.set(false)
                mStates.motorInfo.set(errorMsg)
            } else {
                mStates.isMotorInfoNormal.set(true)
                mStates.motorInfo.set("正常")
            }
            //继续轮询电机脉冲数据
            getMotorMotionData(DELAY_2000_MILLIS)

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun resetPulseData() {
        autoMeasuringHoleDepthBottomDialog = null
        manualMeasuringHoleDepthBottomDialog = null
        safeDistance = ""
    }

    private fun stopQueryMotorState() {
        queryMotionStateJob?.cancel()
        mStates.isStopQueryMotorState.set(true)
        cancelNearbyCommunicationTimeoutJob()
    }

    override fun onResume() {
        super.onResume()
        // 开启屏幕长亮
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        initImmersionBar(binding.llToolbar.toolbar)
    }

    override fun onPause() {
        super.onPause()
        // 禁用屏幕长亮
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    companion object {
        const val DELAY_2000_MILLIS = 2000L
    }
}