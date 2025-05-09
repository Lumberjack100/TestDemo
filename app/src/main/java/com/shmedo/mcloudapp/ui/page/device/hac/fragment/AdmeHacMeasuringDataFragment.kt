package com.shmedo.mcloudapp.ui.page.device.hac.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Filter
import androidx.fragment.app.setFragmentResultListener
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.RegexUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.blankj.utilcode.util.VibrateUtils
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.hjq.toast.Toaster
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.AdmeConfigInfo
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.hac.HacMeasuringDataEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.hac.HacMeasuringDataInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.hac.HacMotionState
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentAdmeHacMeasuringDataBinding
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showAdmeErrorProtectionDialog
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.page.device.hac.dialog.HacConfigNumberBottomDialog
import com.shmedo.mcloudapp.ui.viewmodel.request.AdmeConfigViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.AdmeHacMeasuringDataViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.utils.IOTRegexContants
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class AdmeHacMeasuringDataFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentAdmeHacMeasuringDataBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mStates: AdmeHacMeasuringDataViewModel
    private val admeConfigViewModel: AdmeConfigViewModel by viewModel()
    private val iotParseManager: IOTParserManager by inject()

    private val settlementMethodList by lazy { Utils.getApp().resources.getStringArray(R.array.adme_settlement_method) }

    private val projectNumList = arrayListOf<String>()
    private val areaNumList = arrayListOf<String>()
    private val holeNumList = arrayListOf<String>()
    private var lastConfigProjectNum: String = "" //之前配置的项目编号
    private var lastConfigAreaNum: String = "" //之前配置的区域编号
    private var lastConfigHoleNum: String = "" //之前配置的孔编号


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
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
        toolbarViewModel.toolbarIvActionVisible.set(false)
        initRefresh()
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
                    }
                } catch (ex: Exception) {
                    Timber.e(ex, "Failed to load area numbers")
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
                    Timber.e(ex, "Failed to load hole config info")
                }
            }
        }
    }

    private fun loadAdmeConfigData() {
        launchWithViewLifecycle {
            try {
                //1、加载设备配置信息
                admeConfigViewModel.queryConfigByDeviceSN(deviceInfo.deviceToken)
                    ?.let { configInfo ->
                        handleConfigInfo(configInfo)
                    }

                //2、加载项目编号列表
                admeConfigViewModel.queryAllProjectID()?.let { projectIds ->
                    projectNumList.clear()
                    projectNumList.addAll(projectIds)
                }

                //3、如果有已配置的项目号，加载对应的区域号列表
                if (lastConfigProjectNum.isNotEmpty() && projectNumList.contains(
                        lastConfigProjectNum
                    )
                ) {
                    mStates.projectNum.set(lastConfigProjectNum)
                    admeConfigViewModel.queryAllAreaID(lastConfigProjectNum)?.let { areaList ->
                        areaNumList.clear()
                        areaNumList.addAll(areaList)
                    }
                }

                //4、如果有已配置的区域号，加载对应的孔号列表
                if (lastConfigAreaNum.isNotEmpty() && areaNumList.contains(lastConfigAreaNum)) {
                    mStates.areaNum.set(lastConfigAreaNum)
                    admeConfigViewModel.queryAllHoleNumber(lastConfigProjectNum, lastConfigAreaNum)
                        ?.let { holeList ->
                            holeNumList.clear()
                            holeNumList.addAll(holeList)

                            if (holeNumList.contains(lastConfigHoleNum)) {
                                mStates.holeNum.set(lastConfigHoleNum)
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
            lastConfigProjectNum = configInfo.projectID
            lastConfigAreaNum = configInfo.areaNumber
            lastConfigHoleNum = configInfo.holeNumber

            val configMap = if (configInfo.config.isEmpty()) {
                mapOf()
            } else {
                MoshiUtil.fromJson<Map<String, String>>(configInfo.config) ?: mapOf()
            }
            mStates.realHoleDepth.set(configMap["realHoleDepth"].orEmpty())
            mStates.recommendHoleDepth.set(configMap["recommendHoleDepth"].orEmpty())
            mStates.decentralizationWaitingTime.set(configMap["decentralizationWaitingTime"].orEmpty())

            //添加这行来保存初始状态
            mStates.saveInitialState()

        } catch (ex: Exception) {
            Timber.e(ex, "Failed to parse config info")
        }
    }

    override fun initData() {
        super.initData()
        mStates.dataSettlementMethod.set(settlementMethodList[0])
    }

    override fun createObserver() {
        super.createObserver()
        //从测量过程页面返回需要刷新运行状态信息
        setFragmentResultListener(AppContants.Extras.FRAGMENT_MEASURING_DATA_PROCEDURE_RESULT_REQUEST_KEY) { key, bundle ->
            (bundle.getParcelable(AppContants.Extras.MOTOR_STATE) as HacMotionState?)?.let { motionState ->
                Timber.d("onActivityResult %s", motionState.toString())
                mStates.isRunButtonEnable.set(motionState.motorinfo == "8" || motionState.motorinfo == "9" || motionState.motorinfo == "10")
                mStates.runButtonText.set(
                    if (mStates.isSingleWayTest.get() || motionState.motorinfo == "8")
                        "正向测量"
                    else if (motionState.measmode == "1") "反向测量" else "正向测量"
                )
            }
        }
    }

    inner class ClickProxy : BaseClickProxy() {
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

        override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
            when (button.id) {
                R.id.switch_single_way -> { //仅用单向测量
                    mStates.isSingleWayTest.set(isChecked)
                }

                R.id.switch_reverse -> { //测斜仪反转自检
                    mStates.isCheckReverse.set(isChecked)
                }
            }
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

    private fun saveHoleConfig() {
        launchWithViewLifecycle {
            try {
                val configMap = mapOf(
                    "realHoleDepth" to mStates.realHoleDepth.get(),
                    "recommendHoleDepth" to mStates.recommendHoleDepth.get(),
                    "decentralizationWaitingTime" to mStates.decentralizationWaitingTime.get()
                )
                val configJson = MoshiUtil.toJson(configMap)
                admeConfigViewModel.manageConfig(
                    deviceInfo.deviceToken,
                    mStates.projectNum.get(),
                    mStates.areaNum.get(),
                    mStates.holeNum.get(),
                    configJson
                ) ?: return@launchWithViewLifecycle

                //添加这行来保存初始状态
                mStates.saveInitialState()
            } catch (ex: Exception) {
                Timber.e(ex, "Failed to save hole config")
            }
        }
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
        if (mStates.recommendHoleDepth.get().isEmpty()) {
            showMessageDialog("请输入推荐测斜管孔深!")
            return
        }
        if (mStates.decentralizationWaitingTime.get().isEmpty()) {
            showMessageDialog("请输入下放等待时间!")
            return
        }
        try {
            val value = mStates.decentralizationWaitingTime.get().toDouble()
            if (value < 30 || value > 86400) {
                showMessageDialog("下放等待时间数值范围[30,86400]!")
                return
            }
        } catch (ex: Exception) {
            showMessageDialog("下放等待时间数值范围[30,86400]!")
            return
        }
        //保存孔号配置信息
        saveHoleConfig()

        //数据测量配置参数
        val entity = HacMeasuringDataEntity(
            equipmodel = "1",
            address = mStates.address.get(),
            projectno = mStates.projectNum.get(),
            areano = mStates.areaNum.get(),
            holeno = mStates.holeNum.get(),
            downwaitetime = mStates.decentralizationWaitingTime.get(),
            holedepth = mStates.recommendHoleDepth.get(),
            datatype = settlementMethodList.indexOf(mStates.dataSettlementMethod.get()).toString(),
            onewaytest = if (mStates.isSingleWayTest.get()) "1" else "0",
            checkreverse = if (mStates.isCheckReverse.get()) "1" else "0"
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
        loadAdmeConfigData()
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
        sendCommandFromCmdList(
            isStartTimeoutJob = true,
            timeoutMillis = AppContants.Communication.DELAY_15000_MILLIS
        )
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
                        val errMsg = "获取数据测量配置参数出错: ${result.message}"
                        handleFailureResult(errMsg)
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
                        val errMsg = "获取电机当前运动状态出错: ${result.message}"
                        handleFailureResult(errMsg)
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
                        val errMsg = "设置数据测量参数出错: ${result.message}"
                        handleFailureResult(errMsg)
                        return
                    }

                    else -> {
                        sendCommandFromCmdList {
                            val bundle = AdmeHacMeasuringDataProcedureFragment.newBundleArguments(
                                mStates.isCheckReverse.get(),
                                productType,
                                communicateWay,
                                deviceInfo,
                                bleDevice
                            )
                            nav().safeNavigate(
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
                mStates.decentralizationWaitingTime.set(
                    mStates.decentralizationWaitingTime.get().ifEmpty { info.downwaitetime })
                info.datatype.toIntOrNull()?.let {
                    mStates.dataSettlementMethod.set(if (it in settlementMethodList.indices) settlementMethodList[it] else settlementMethodList[0])
                }
                mStates.isSingleWayTest.set(info.onewaytest == "1")
                mStates.isCheckReverse.set(info.checkreverse == "1")
                if (mStates.equipmodel.get() == "1") {
                    return@launchWithViewLifecycle
                }
            } catch (e: Exception) {
                Timber.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
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
        1.1 equipmodel = 1(正常测量状态)：
        根据 motorinfo 控制跳转页面，motorinfo=2|3|4 进入数据测量页面状态；motorinfo=5|6 进入数据读取页面状态；motorinfo=7 进入数据上传页面状态。
        1.2 equipmodel = 0 (停止状态)：
        单测时按钮显示正向测量；正反测时，motorinfo=8，按钮显示正向测量；motorinfo=9，按钮显示反向测量。
        1.3 equipmodel = 2(异常状态)，弹框提示异常信息，点击按钮开始测量时，设备自动清除异常状态标志。
         */
        if (mStates.equipmodel.get() == "1") {//表示在测量 然后根据 motorinfo 控制跳转页面
            val bundle = AdmeHacMeasuringDataProcedureFragment.newBundleArguments(
                mStates.isCheckReverse.get(),
                productType,
                communicateWay,
                deviceInfo,
                bleDevice
            )
            nav().safeNavigate(
                R.id.action_global_to_admeHacMeasuringDataProcedureFragment,
                bundle
            )
            return
        }
        //停止或异常状态下,判断是否单测模式，单测模式下显示正向测量；正反测模式下，根据 motorinfo 处理操作按钮
        mStates.runButtonText.set(
            if (mStates.isSingleWayTest.get())
                "正向测量"
            else if (motionState.measmode == "1") "反向测量" else "正向测量"
        )
        //表示异常，展示异常原因
        if (mStates.equipmodel.get() == "2" && motionState.abndiasis.isNotEmpty() && motionState.abndiasis != "0") {
            showAdmeErrorProtectionDialog(motionState.abndiasis)
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }
}