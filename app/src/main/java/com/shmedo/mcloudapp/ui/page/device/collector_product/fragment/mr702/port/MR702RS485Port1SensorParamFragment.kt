package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.mr702.port

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.google.android.material.tabs.TabLayout
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.jsonhelper.MoshiUtil
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.mr.MRRS485Port1SensorParamEntity
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port1SensorParam
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port1SensorParamWrapper
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs485Port1SensorParamBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.MRSensorItem
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702PortHomeViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.MR702RS485Port1SensorParamViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber

@Deprecated("This class is deprecated", ReplaceWith("MR702RS485Port1SingleSensorAddParamFragment"))
class MR702RS485Port1SensorParamFragment : BaseIOTDeviceFragment(),
    TabLayout.OnTabSelectedListener {
    private lateinit var binding: FragmentMr702Rs485Port1SensorParamBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: MR702RS485Port1SensorParamViewModel by viewModels()
    private val portHomeViewModel: MR702PortHomeViewModel by activityViewModels()
    private val iotParseManager: IOTParserManager by inject()

    private lateinit var sensorItem: MRSensorItem
    private val dataBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_data_bit) }
    private val checkBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_check_bit) }
    private val stopBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_stop_bit) }
    private val dataFormatList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_rs485_sensor_data_format) }
    private val solutionMethodList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_rs485_sensor_solution_method) }

    private val activeColor: Int = ColorUtils.getColor(R.color.colorPrimary)
    private val normalColor: Int = ColorUtils.getColor(R.color.title_text_color)
    private val activeSize: Float = 17f
    private val normalSize: Float = 15f
    private var selectedFieldIndex = 0

    private val tabList: MutableList<String> = arrayListOf()


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_mr702_rs485_port1_sensor_param,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentMr702Rs485Port1SensorParamBinding
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            processBack(true)
        }
        registerOnBackPressedDispatcher {
            processBack(true)
        }
        toolbarViewModel.toolbarIvActionResId.set(R.drawable.ic_device_param_edit)
        toolbarViewModel.toolbarTvActionText.set("取消")
        toolbarViewModel.toolbarIvActionVisible.set(true)
        initRefresh()
    }

    private fun setEditable(editable: Boolean) {
        toolbarViewModel.toolbarIvActionVisible.set(!editable)
        toolbarViewModel.toolbarTvActionVisible.set(editable)
        mStates.isEditable.set(editable)
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_refresh_fail_warn))
                finishRefresh()
                return@onRefresh
            }
            queryData()
        }
    }

    override fun initData() {
        super.initData()
        arguments?.let {
            sensorItem = it.getParcelable(SENSOR_MODEL_ITEM)!!
        }
        binding.llToolbar.toolbar.title = sensorItem.sensorName

        portHomeViewModel.configPort4851SensorIDToSensorModelMap[sensorItem.modelToken]?.let {
            mStates.curSensorModel = it
        }
        if (mStates.curSensorModel.modelFieldList.isNotEmpty()) {
            tabList.clear()
            tabList.addAll(mStates.curSensorModel.modelFieldList.map { it.fieldName })
            initTabLayout()
        }
        mStates.modelName.set(sensorItem.sensorName)
        mStates.modelToken.set(sensorItem.modelToken)
        mStates.address.set("1")//传感器地址,默认1
        resetDefaultModelField()
    }

    /**
     * 重置采集项
     */
    private fun resetDefaultModelField() {
        mStates.baudRate.set("9600")  //默认波特率
        mStates.dataBit.set(dataBitList[3])//默认数据位 8
        mStates.checkBit.set(checkBitList[0])//默认校验位 无
        mStates.stopBit.set(stopBitList[0])//默认停止位 1

        mStates.hydrologicalIdentification.set("")//水文标识
        mStates.collectionInstructions.set("")//采集指令
        mStates.ratio.set("1")//默认倍率 1
        mStates.dataFormat.set(dataFormatList[0])
        mStates.solutionMethod.set(solutionMethodList[0])//默认解算方法 加权平均
        mStates.triggerValue.set("0")//默认触发值 0
        mStates.upperLimit.set("100")//默认上限 100
        mStates.lowerLimit.set("0")//默认下限 0
        mStates.correctValue.set("0")//默认修正值 0
        mStates.ngateval.set("3")//默认阈值次数 3
    }

    private fun initTabLayout() {
        val tabLayout = binding.tabs
        tabLayout.removeAllTabs()
        tabList.forEachIndexed { index, name ->
            val textView = TextView(requireContext())
            textView.text = name
            textView.textSize = if (index == 0) activeSize else normalSize
            textView.typeface = if (index == 0) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            textView.gravity = Gravity.CENTER
            textView.setTextColor(if (index == 0) activeColor else normalColor)
            tabLayout.addTab(tabLayout.newTab().setCustomView(textView))
        }
        tabLayout.addOnTabSelectedListener(this)
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        val textView = tab.customView as TextView?
        textView?.apply {
            textSize = activeSize
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(activeColor)
        }
        selectedFieldIndex = tab.position
        binding.refreshLayout.autoRefresh()
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

        fun onDataBitChooseClick() {
            val selectedIndex = dataBitList.indexOf(mStates.dataBit.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择数据位", dataBitList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.dataBit.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onCheckBitChooseClick() {
            val selectedIndex = checkBitList.indexOf(mStates.checkBit.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择校验位", checkBitList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.checkBit.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onStopBitChooseClick() {
            val selectedIndex = stopBitList.indexOf(mStates.stopBit.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择停止位", stopBitList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.stopBit.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onDataFormatChooseClick() {
            val selectedIndex = dataFormatList.indexOf(mStates.dataFormat.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择数据类型", dataFormatList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.dataFormat.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onSolutionMethodChooseClick() {
            val selectedIndex = solutionMethodList.indexOf(mStates.solutionMethod.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择解算", solutionMethodList,
                    null, selectedIndex,
                    { position, text ->
                        mStates.solutionMethod.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onSaveModelFieldClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }

        override fun onSubmitButtonClick() {
            processBack()
        }
    }

    private fun initSaveCommand() {
        commandItems.clear()
        if (mStates.modelToken.get().isEmpty()) {
            showMessageDialog("请输入物模型编号")
            return
        }
        if (mStates.address.get().isEmpty()) {
            showMessageDialog("请输入地址")
            return
        }
        if (mStates.baudRate.get().isEmpty()) {
            showMessageDialog("请输入波特率")
            return
        }
        if (mStates.hydrologicalIdentification.get().isEmpty()) {
            showMessageDialog("请输入水文标识")
            return
        }
        if (mStates.collectionInstructions.get().isEmpty()) {
            showMessageDialog("请输入采集指令")
            return
        }
        if (mStates.ratio.get().isEmpty()) {
            showMessageDialog("请输入倍率")
            return
        }
        if (mStates.triggerValue.get().isEmpty()) {
            showMessageDialog("请输入触发值")
            return
        }
        if (mStates.upperLimit.get().isEmpty()) {
            showMessageDialog("请输入上限值")
            return
        }
        if (mStates.lowerLimit.get().isEmpty()) {
            showMessageDialog("请输入下限值")
            return
        }
        if (mStates.correctValue.get().isEmpty()) {
            showMessageDialog("请输入修正值")
            return
        }
        if (mStates.ngateval.get().isEmpty()) {
            showMessageDialog("请输入阈值次数")
            return
        }
        val entity = MRRS485Port1SensorParamEntity(
            c_model = "0",
            num = selectedFieldIndex.toString(),
            model = mStates.modelToken.get() + "_" + mStates.address.get(),
            baud = mStates.baudRate.get(),
            databit = mStates.dataBit.get(),
            parity = (checkBitList.indexOf(mStates.checkBit.get())).toString(),
            stopbit = (stopBitList.indexOf(mStates.stopBit.get())).toString(),
            swtoken = mStates.hydrologicalIdentification.get(),
            cmd = mStates.collectionInstructions.get(),
            ratio = mStates.ratio.get(),
            dataformat = (dataFormatList.indexOf(mStates.dataFormat.get())).toString(),
            calctype = (solutionMethodList.indexOf(mStates.solutionMethod.get())).toString(),
            gateval = mStates.triggerValue.get(),
            uplimit = mStates.upperLimit.get(),
            lowlimit = mStates.lowerLimit.get(),
            corrvalue = mStates.correctValue.get(),
            ngateval = mStates.ngateval.get(),
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MR_MD_SET_RS485_PORT1_SENSOR_PARAM,
            entity.toCommandString()
        )
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
            IOTCommandType.MR_MD_GET_RS485_PORT1_SENSOR_PARAM,
            "model=${sensorItem.modelToken}_${sensorItem.addr}&index=${selectedFieldIndex}"
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MR_MD_GET_RS485_PORT1_SENSOR_PARAM -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MR_MD_GET_RS485_PORT1_SENSOR_PARAM
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        val errMsg = "查询参数出错: ${result.message}"
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

            IOTCommandType.MR_MD_SET_RS485_PORT1_SENSOR_PARAM -> {
                setEditable(false)
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

    private fun initParamData(content: String) {
        launchWithViewLifecycle {
            try {
                val sensorParamWrapper = withContext(Dispatchers.IO) {
                    MoshiUtil.fromJson<MRRS485Port1SensorParamWrapper>(content)
                } ?: return@launchWithViewLifecycle
                if (mStates.curSensorModel.modelFieldList.isEmpty() && selectedFieldIndex == 0) {
                    tabList.clear()
                    for (i in 0..sensorParamWrapper.indexnum.toInt()) {
                        tabList.add("采集项${i + 1}")
                    }
                    initTabLayout()
                }
                sensorParamWrapper.port1_param.let { portParam ->
                    val sensorParam: MRRS485Port1SensorParam = portParam[0]
                    mStates.sensorParamWrapper.set(sensorParam)

                    val strs = sensorParam.model.split("_")
                    mStates.address.set(strs[1])
                    mStates.modelToken.set(strs[0])
                    mStates.baudRate.set(sensorParam.baud)
                    mStates.dataBit.set(sensorParam.databit)
                    sensorParam.parity.toInt().let { value ->
                        if (value in checkBitList.indices) {
                            mStates.checkBit.set(checkBitList[value])
                        }
                    }
                    sensorParam.stopbit.toInt().let { value ->
                        if (value in stopBitList.indices) {
                            mStates.stopBit.set(stopBitList[value])
                        }
                    }
                    mStates.hydrologicalIdentification.set(sensorParam.swtoken)
                    mStates.collectionInstructions.set(sensorParam.cmd)
                    mStates.ratio.set(sensorParam.ratio)
                    sensorParam.dataformat.toInt().let { value ->
                        if (value in dataFormatList.indices) {
                            mStates.dataFormat.set(dataFormatList[value])
                        }
                    }
                    sensorParam.calctype.toInt().let { value ->
                        if (value in solutionMethodList.indices) {
                            mStates.solutionMethod.set(solutionMethodList[value])
                        }
                    }
                    mStates.triggerValue.set(sensorParam.gateval)
                    mStates.upperLimit.set(sensorParam.uplimit)
                    mStates.lowerLimit.set(sensorParam.lowlimit)
                    mStates.correctValue.set(sensorParam.corrvalue)
                    mStates.ngateval.set(sensorParam.ngateval)
                }
            } catch (e: Exception) {
                Timber.Forest.e(e)
                addDeviceLogItem(Log.ERROR, e.errorMsg)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    private fun processBack(isPressBackBtn: Boolean = false) {
        launchWithViewLifecycle {
            if (isPressBackBtn) {
                mMessenger.requestStatusBarColor(if (statusBarColor == 0) R.color.colorPrimary else statusBarColor)
                nav().navigateUp()
                return@launchWithViewLifecycle
            }
            delay(1000)
            //需要给上一级浏览页面传递最新的事件信息
//            mMessenger.requestMR702Rs485PortSensorRefresh(MRRS485Port1)
            nav().navigateUp()
        }
    }

    companion object {
        private const val SENSOR_MODEL_ITEM = "sensor_model_item"

        fun newBundleArguments(
            sensorItem: MRSensorItem,
            type: ProductType = ProductType.UnKnown,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putParcelable(SENSOR_MODEL_ITEM, sensorItem)
            putParcelable(AppContants.Extras.PRODUCT_TYPE, type)
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}