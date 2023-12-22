package com.shmedo.mcloudapp.device.ui.mr702.fragment.port

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
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.core.base.model.DeviceInfo
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.lib.device.base.iot_cmd.assemble.entity.mr.MRRS485Port1SensorParamEntity
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonSettingCmdResult
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRRS485Port1SensorParam
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRRS485Port1SensorParamWrapper
import com.shmedo.lib.device.base.iot_cmd.parser.IOTCommandResult
import com.shmedo.lib.device.base.iot_cmd.parser.IOTParserManager
import com.shmedo.lib.device.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.launchWithViewLifecycle
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.showLoadingDialog
import com.shmedo.mcloudapp.databinding.FragmentMr702Rs485Port1SensorParamBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.common.MRRS485Port1
import com.shmedo.mcloudapp.device.model.BleConnect
import com.shmedo.mcloudapp.device.model.CommunicateWay
import com.shmedo.mcloudapp.device.model.MRSensorItem
import com.shmedo.mcloudapp.device.model.NetPlatformConnect
import com.shmedo.mcloudapp.device.ui.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.device.viewmodel.state.MR702RS485Port1SensorParamViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DecimalFormat

class MR702RS485Port1SensorParamFragment : BaseIOTDeviceFragment(),
    TabLayout.OnTabSelectedListener {
    private val binding: FragmentMr702Rs485Port1SensorParamBinding by lazy { getBinding() as FragmentMr702Rs485Port1SensorParamBinding }
    private val mStates: MR702RS485Port1SensorParamViewModel by viewModels()
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val iotParseManager: IOTParserManager by inject()

    private lateinit var sensorItem: MRSensorItem
    private val dataBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_data_bit) }
    private val checkBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_check_bit) }
    private val stopBitList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_stop_bit) }
    private val dataFormatList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_rs232_port1_sensor_data_format) }
    private val solutionMethodList by lazy { Utils.getApp().resources.getStringArray(R.array.mr_rs232_port1_sensor_solution_method) }

    private val activeColor: Int = ColorUtils.getColor(R.color.colorPrimary)
    private val normalColor: Int = ColorUtils.getColor(R.color.title_text_color)
    private val activeSize: Float = 18f
    private val normalSize: Float = 15f
    private var selectedFieldIndex = 0

    private val tabList: MutableList<String> = arrayListOf()
    private val decimalFormat = DecimalFormat("#.#")


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
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            processBack(true)
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                processBack(true)
            }
        })
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
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
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
        mStates.sensorName.set(sensorItem.sensorName)
        mStates.modelToken.set(sensorItem.modelToken)
        binding.llToolbar.toolbar.title = "RS485-1-${sensorItem.sensorName}"
        if (sensorItem.modelFieldList.isNotEmpty()) {
            tabList.clear()
            tabList.addAll(sensorItem.modelFieldList)
            initTabLayout()
        }
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
            if (communicateWay is BleConnect && !bleViewModel.isConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }
            initSaveCommand()
        }

        fun onSubmitClick() {
            processBack()
        }
    }

    private fun initSaveCommand() {
        commandItems.clear()
        if (mStates.sensorAddress.get().isEmpty()) {
            Toaster.show("请输入传感器地址")
            return
        }
        if (mStates.modelToken.get().isEmpty()) {
            Toaster.show("请输入物模型")
            return
        }
        if (mStates.baudRate.get().isEmpty()) {
            Toaster.show("请输入波特率")
            return
        }
        if (mStates.hydrologicalIdentification.get().isEmpty()) {
            Toaster.show("请输入水文标识")
            return
        }
        if (mStates.collectionInstructions.get().isEmpty()) {
            Toaster.show("请输入采集指令")
            return
        }
        if (mStates.ratio.get().isEmpty()) {
            Toaster.show("请输入倍率")
            return
        }
        if (mStates.triggerValue.get().isEmpty()) {
            Toaster.show("请输入触发值")
            return
        }
        if (mStates.upperLimit.get().isEmpty()) {
            Toaster.show("请输入上限值")
            return
        }
        if (mStates.lowerLimit.get().isEmpty()) {
            Toaster.show("请输入下限值")
            return
        }
        if (mStates.correctValue.get().isEmpty()) {
            Toaster.show("请输入修正值")
            return
        }
        val entity = MRRS485Port1SensorParamEntity(
            c_model = "0",
            num = selectedFieldIndex.toString(),
            model = mStates.modelToken.get() + "_" + mStates.sensorAddress.get(),
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
            corrvalue = mStates.correctValue.get()
        )
        val command = IOTCommandUtil.getCommand(
            IOTCommandType.MD_MR_SET_RS485_PORT1_SENSOR_PARAM,
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
            IOTCommandType.MD_MR_GET_RS485_PORT1_SENSOR_PARAM,
            "model=${sensorItem.modelToken}_${sensorItem.addr}&index=${selectedFieldIndex}"
        )
        commandItems.add(command)
        sendCommandFromCmdList(isStartTimeoutJob = true)
    }

    override fun setResultData(cmdStr: String) {
        when (IOTCommandUtil.extractCommandType(cmdStr)) {
            IOTCommandType.MD_MR_GET_RS485_PORT1_SENSOR_PARAM -> {
                val result = iotParseManager.parse<String>(
                    cmdStr,
                    IOTCommandType.MD_MR_GET_RS485_PORT1_SENSOR_PARAM
                )
                when (result) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "查询参数出错: ${result.message}"
                        Timber.e(errMsg)
                        Toaster.show(errMsg)
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

            IOTCommandType.MD_MR_SET_RS485_PORT1_SENSOR_PARAM -> {
                setEditable(false)
                when (val result = iotParseManager.parse<CommonSettingCmdResult>(cmdStr)) {
                    is IOTCommandResult.Failure -> {
                        cancelNearbyCommunicationTimeoutJob()
                        val errMsg = "设置参数出错: ${result.message}"
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

    private fun initParamData(content: String) {
        launchWithViewLifecycle {
            try {
                val sensorParamWrapper = MoshiUtil.fromJson<MRRS485Port1SensorParamWrapper>(content)
                    ?: return@launchWithViewLifecycle
                if (sensorItem.modelFieldList.isEmpty() && selectedFieldIndex == 0) {
                    tabList.clear()
                    for (i in 0..sensorParamWrapper.indexnum.toInt()) {
                        tabList.add("采集项${i + 1}")
                    }
                    initTabLayout()
                }
                sensorParamWrapper.port1_param.let {
                    val sensorParam: MRRS485Port1SensorParam = it[0]
                    val strs = sensorParam.model.split("_").toTypedArray()
                    mStates.sensorAddress.set(strs[1])
                    mStates.modelToken.set(strs[0])
                    mStates.baudRate.set(sensorParam.baud)
                    mStates.dataBit.set(sensorParam.databit)
                    mStates.checkBit.set(checkBitList[sensorParam.parity.toInt()])
                    mStates.stopBit.set(stopBitList[sensorParam.stopbit.toInt()])
                    mStates.hydrologicalIdentification.set(sensorParam.swtoken)
                    mStates.collectionInstructions.set(sensorParam.cmd)
                    mStates.ratio.set(sensorParam.ratio)
                    mStates.dataFormat.set(dataFormatList[sensorParam.dataformat.toInt()])
                    mStates.solutionMethod.set(solutionMethodList[sensorParam.calctype.toInt()])
                    mStates.triggerValue.set(sensorParam.gateval)
                    mStates.upperLimit.set(sensorParam.uplimit)
                    mStates.lowerLimit.set(sensorParam.lowlimit)
                    mStates.correctValue.set(sensorParam.corrvalue)
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
            mMessenger.requestMR702Rs485PortSensorRefresh(MRRS485Port1)
            nav().navigateUp()
        }
    }

    companion object {
        fun newInstance() = MR702RS485Port1SensorParamFragment()
        private const val SENSOR_MODEL_ITEM = "sensor_model_item"

        fun newBundleArguments(
            sensorItem: MRSensorItem,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putParcelable(SENSOR_MODEL_ITEM, sensorItem)
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}