package com.shmedo.mcloudapp.ui.page.device.collector_product.fragment.das.externalsensor

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTSensorType
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasCollectorInfo
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasExternalSensorInfo
import com.shmedo.lib.network.ext.errorMsg
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentDasExternalSensorListBinding
import com.shmedo.mcloudapp.extensions.getActivityScopeViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.extensions.showMessage
import com.shmedo.mcloudapp.extensions.showMessageDialog
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.DASSensorItem
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.model.RVEmptyFooter
import com.shmedo.mcloudapp.ui.page.device.BaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.DasExternalSensorListViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2024/4/18
 * 描述：DAS扩展传感器列表页面基类，支持不同通讯协议
 */
abstract class BaseDasExternalSensorListFragment : BaseIOTDeviceFragment() {
    private lateinit var binding: FragmentDasExternalSensorListBinding
    protected lateinit var mStates: DasExternalSensorListViewModel<DasExternalSensorInfo>
    protected lateinit var iotSensorType: IOTSensorType
    protected var deleteItemIndex = 0

    override fun initViewModel() {
        super.initViewModel()
        mStates = getActivityScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_das_external_sensor_list,
            BR.stateVM,
            mStates
        ).addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDasExternalSensorListBinding
        initRefresh()
        initSensorAdapter()
    }

    override fun initData() {
        super.initData()
        arguments?.let {
            val model = it.getString(COLLECTOR_MODEL, "-1")
            // 处理采集器模型
            processCollectorModel(model)
        }
    }

    /**
     * 处理采集器模型，子类可以重写以适配不同的协议
     */
    protected open fun processCollectorModel(model: String) {
        // collectorModel 移除前缀 0
        val processedModel = if (model.startsWith("0") && model.length > 1) {
            model.substring(1)
        } else {
            model
        }
        mStates.collectorType.set(processedModel)
        mStates.isVibratingWireSensor.set(processedModel == IOTSensorType.VIBRATING_SENSOR.code)
        iotSensorType = IOTSensorType.Companion.value(processedModel)
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return@onRefresh
            }
            resetDefaultData()
            queryCollectorInfo()
        }
    }

    private fun initSensorAdapter() {
        binding.rv.setup { rv ->
            rv.addItemDecoration(
                MyGridSpacingItemDecoration(2, ConvertUtils.dp2px(8f), false)
            )
            addType<DASSensorItem>(R.layout.item_das_sensor)
            addType<RVEmptyFooter>(R.layout.item_sensor_add_footer)

            R.id.item.onClick {
                if (isBleDisconnected()) {
                    Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                    return@onClick
                }
                if (mStates.collectorType.get().isEmpty()) {
                    showMessageDialog("未获取到采集器信息，请尝试刷新后再试!")
                    return@onClick
                }

                when (itemViewType) {
                    R.layout.item_das_sensor -> {
                        val item = getModel<DASSensorItem>()
                        navigateToSensorEdit(item, modelPosition)
                    }

                    else -> {
                        navigateToSensorAdd()
                    }
                }
            }

            R.id.item_del.onClick {
                if (isBleDisconnected()) {
                    Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                    return@onClick
                }

                showMessage("确定移除此传感器吗？", "提示", "删除", {
                    deleteItemIndex = modelPosition
                    onDeleteSensor()
                }, "取消")
            }
        }
    }

    /**
     * 导航到传感器编辑页面
     */
    private fun navigateToSensorEdit(item: DASSensorItem, position: Int) {
        if (!mStates.isVibratingWireSensor.get()) {
            // 编辑数字式传感器
            val bundle = BaseDasExternalDigitalSensorFragment.newBundleArguments(
                sensorEditMode = true,
                index = position,
                sensorAddress = item.addr,
                productType,
                communicateWay,
                deviceInfo,
                bleDevice
            )
            nav().safeNavigate(
                R.id.action_global_to_dasExternalDigitalSensorFragment,
                bundle
            )
        } else {
            // 编辑振弦式传感器
            val bundle = DasExternalVibratingSensorFragment.newBundleArguments(
                sensorChannel = item.addr,
                productType,
                communicateWay,
                deviceInfo,
                bleDevice,
            )
            nav().safeNavigate(
                R.id.action_global_to_dasExternalVibratingSensorFragment,
                bundle
            )
        }
    }

    /**
     * 导航到传感器新增页面
     */
    private fun navigateToSensorAdd() {
        if (!mStates.isVibratingWireSensor.get()) {
            // 新增数字式传感器
            val bundle = BaseDasExternalDigitalSensorFragment.newBundleArguments(
                sensorEditMode = false,
                index = -1,
                type = productType,
                communicateWay = communicateWay,
                deviceInfo = deviceInfo,
                bleDevice = bleDevice,
            )
            nav().safeNavigate(
                R.id.action_global_to_dasExternalDigitalSensorFragment,
                bundle
            )
        } else {
            // 新增振弦式传感器
            val bundle = DasExternalVibratingSensorFragment.newBundleArguments(
                sensorChannel = "-1",
                productType,
                communicateWay,
                deviceInfo,
                bleDevice,
            )
            nav().safeNavigate(
                R.id.action_global_to_dasExternalVibratingSensorFragment,
                bundle
            )
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onSubmitButtonClick() {
            if (isBleDisconnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                return
            }

            if (mStates.sensorModelMap.isEmpty()) {
                showMessage(
                    "确定将采集器接入的传感器个数设置为0吗？",
                    "温馨提示",
                    "确定",
                    { closeCollector() },
                    "取消"
                )
                return
            }
            initSaveCommand()
        }
    }

    override fun createObserver() {
        super.createObserver()
        mStates.isRefreshSensorList.observe(viewLifecycleOwner) { flag ->
            if (flag) {
                binding.rv.models = arrayListOf()
                mStates.sensorModelMap.keys.sortedBy { addr -> addr.toInt() }.forEach { key ->
                    val sensorInfo = mStates.sensorModelMap[key]!!
                    val item = DASSensorItem(
                        isPlugin = true,
                        addr = sensorInfo.addr,
                        addrDesc = if (mStates.isVibratingWireSensor.get()) {
                            "通道-${sensorInfo.addr.toInt() + 1}"
                        } else {
                            "地址-${sensorInfo.addr}"
                        },
                        sensorType = sensorInfo.type,
                        sensorName = IOTSensorType.Companion.value(sensorInfo.type).description,
                    )
                    binding.rv.mutable.add(item)
                }
                updateFooter()
            }
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 处理获取到的单个传感器参数信息
     */
    protected fun processSensorParamsInfo(sensorInfo: DasExternalSensorInfo) {
        mStates.sensorModelMap[sensorInfo.addr] = sensorInfo
        val item = DASSensorItem(
            isPlugin = true,
            addr = sensorInfo.addr,
            addrDesc = if (mStates.isVibratingWireSensor.get()) {
                "通道-${sensorInfo.addr.toInt() + 1}"
            } else {
                "地址-${sensorInfo.addr}"
            },
            sensorType = sensorInfo.type,
            sensorName = IOTSensorType.Companion.value(sensorInfo.type).description,
        )

        if (binding.rv.models.isNullOrEmpty()) {
            binding.rv.models = arrayListOf()
        }
        binding.rv.mutable.add(item)
        binding.rv.bindingAdapter.notifyItemInserted(binding.rv.bindingAdapter.modelCount)
    }

    /**
     * 处理采集器信息初始化
     */
    protected fun handleCollectorInfo(collectorInfo: DasCollectorInfo) {
        try {
            // 采集器地址为 0 时，表示采集器未启用
            if (collectorInfo.addr == "0") {
                cancelNearbyCommunicationTimeoutJob()
                showMessageDialog("采集器地址为0,无法配置扩展传感器,请先修改采集器地址!")
                return
            }

            // 处理采集器类型（子类可能需要不同的处理逻辑）
            onCollectorTypeInitialized(collectorInfo.type)

            if (collectorInfo.sensornum.isEmpty() || collectorInfo.sensornum.toInt() == 0) {
                cancelNearbyCommunicationTimeoutJob()
                initEmptySensor()
                return
            }

            // 查询采集器接入的传感器配置信息
            queryExtendSensorConfigInfo(collectorInfo.sensornum.toInt())
        } catch (e: Exception) {
            cancelNearbyCommunicationTimeoutJob()
            Timber.Forest.e(e)
            addDeviceLogItem(Log.ERROR, e.errorMsg)
        }
    }

    protected fun updateAdapterRemoveSensorItem() {
        binding.rv.bindingAdapter.mutable[deleteItemIndex].let {
            if (it is DASSensorItem) {
                mStates.sensorModelMap.remove(it.addr)
            }
        }
        binding.rv.bindingAdapter.mutable.removeAt(deleteItemIndex)
        binding.rv.bindingAdapter.notifyItemRemoved(deleteItemIndex)
        updateFooter()
    }

    protected fun initEmptySensor() {
        binding.rv.models = arrayListOf<DASSensorItem>()
        updateFooter()
    }

    protected fun updateFooter() {
        if (binding.rv.bindingAdapter.modelCount < MAX_SENSOR_COUNT) {
            if (binding.rv.bindingAdapter.footerCount == 0) {
                binding.rv.bindingAdapter.addFooter(RVEmptyFooter())
            }
        } else {
            binding.rv.bindingAdapter.clearFooter()
        }
        mStates.isSubmitBtnVisible.set(mStates.sensorModelMap.isNotEmpty())
    }

    private fun resetDefaultData() {
        deleteItemIndex = 0
        mStates.sensorModelMap.clear()
        binding.rv.bindingAdapter.clearFooter()
        binding.rv.models = arrayListOf<DASSensorItem>()
        mStates.isSubmitBtnVisible.set(false)
    }

    // ========================== 抽象方法，子类必须实现 ==========================
    /**
     * 查询采集器配置信息
     */
    protected abstract fun queryCollectorInfo()

    /**
     * 查询采集器接入的传感器配置信息
     */
    protected abstract fun queryExtendSensorConfigInfo(sensorNum: Int)

    /**
     * 关闭采集器
     */
    protected abstract fun closeCollector()

    /**
     * 初始化保存命令
     */
    protected abstract fun initSaveCommand()

    /**
     * 删除传感器处理
     */
    protected abstract fun onDeleteSensor()

    /**
     * 采集器类型初始化后的回调
     */
    protected open fun onCollectorTypeInitialized(collectorType: String) {
        // 默认实现：设置到状态中
        mStates.collectorType.set(collectorType)
        mStates.isVibratingWireSensor.set(collectorType == "0")
    }

    companion object {
        const val MAX_SENSOR_COUNT = 16
        const val COLLECTOR_MODEL = "collector_model"

        fun newBundleArguments(
            collectorModel: String,
            type: ProductType = ProductType.UnKnown,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putString(COLLECTOR_MODEL, collectorModel)
            putParcelable(AppContants.Extras.PRODUCT_TYPE, type)
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}