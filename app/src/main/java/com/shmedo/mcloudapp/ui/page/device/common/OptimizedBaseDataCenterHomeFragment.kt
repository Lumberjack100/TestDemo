package com.shmedo.mcloudapp.ui.page.device.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.drake.brv.BindingAdapter.BindingViewHolder
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.ble.scanner.model.DiscoveredBluetoothDevice
import com.shmedo.lib.cmd.base.iot_cmd.enums.ProductType
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParserManager
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentUniversalDataCenterHomeBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.safeNavigate
import com.shmedo.mcloudapp.model.CommunicateWay
import com.shmedo.mcloudapp.model.DataCenterStatusItem
import com.shmedo.mcloudapp.model.DeviceStatusInfoGroupItem
import com.shmedo.mcloudapp.model.GapItem
import com.shmedo.mcloudapp.model.NetPlatformConnect
import com.shmedo.mcloudapp.model.ParamSubmitButtonItem
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import org.koin.android.ext.android.inject

/**
 * @author：gonghe
 * @time: 2025/7/26
 * @desc: 使用优化架构的数据中心列表页面抽象基类
 *
 * 优化特点：
 * 1. 继承自 OptimizedBaseIOTDeviceFragment，使用新的通信架构
 * 2. 统一的错误处理策略
 * 3. 响应驱动的指令执行
 * 4. 支持4G和蓝牙两种通讯方式
 */
abstract class OptimizedBaseDataCenterHomeFragment : OptimizedBaseIOTDeviceFragment() {
    protected lateinit var binding: FragmentUniversalDataCenterHomeBinding
    protected val toolbarViewModel: ToolbarViewModel by viewModels()
    protected val iotParseManager: IOTParserManager by inject()
    protected var centerNum = 0 // 数据链路数量

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_universal_data_center_home,
            BR.toolbarVM,
            toolbarViewModel
        )
            .addBindingParam(BR.click, getClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUniversalDataCenterHomeBinding
        binding.llToolbar.toolbar.title = "数据链路"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
        initRefresh()
        initAdapter()
    }

    private fun initRefresh() {
        refreshLayout = binding.refreshLayout
        binding.refreshLayout.setEnableLoadMore(false)
        binding.refreshLayout.onRefresh {
            if (!isDeviceConnected()) {
                Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_refresh_fail_warn))
                return@onRefresh
            }
            queryData()
        }
    }

    private fun initAdapter() {
        binding.recyclerView.linear().setup { rv ->
            addType<DeviceStatusInfoGroupItem>(R.layout.item_device_status_info_group)
            addType<DataCenterStatusItem>(R.layout.data_center_status_item)
            addType<GapItem>(R.layout.item_device_status_info_gap)
            addType<ParamSubmitButtonItem>(R.layout.item_param_summit_button)
            onBind {
                processOtherItemViewBind(itemViewType)
            }

            R.id.item.onClick {
                when (itemViewType) {
                    R.layout.data_center_status_item -> {
                        val item = getModel<DataCenterStatusItem>()
                        val bundle = UniversalDataCenterParamFragment.newBundleArguments(
                            item,
                            productType,
                            communicateWay,
                            deviceInfo,
                            bleDevice
                        )
                        nav().safeNavigate(
                            getNavigationActionId(),
                            bundle
                        )
                    }

                    else -> {
                        processOtherItemViewClick(itemViewType)
                    }
                }
            }

            R.id.btn_submit.onClick {
                KeyboardUtils.hideSoftInput(binding.root)
                if (!isDeviceConnected() ) {
                    Toaster.show(StringUtils.getString(R.string.ble_config_disconnect_warn))
                    return@onClick
                }
                initSaveCommand()
            }
        }
    }

    override fun initData() {
        super.initData()
        arguments?.let {
            centerNum = it.getInt(CENTER_NUM, 1)
        }
        initRecyclerViewAdapterData()
    }

    protected open fun initRecyclerViewAdapterData() {
        binding.recyclerView.models = getAdapterData()
    }

    /**
     * 处理其他类型Item的视图绑定，子类可重写以处理扩展适配器
     */
    protected open fun BindingViewHolder.processOtherItemViewBind(itemViewType: Int) {
        // 基类默认不处理，子类可重写
    }

    protected open fun BindingViewHolder.processOtherItemViewClick(itemViewType: Int) {
        // 基类默认不处理，子类可重写
    }

    protected open fun initSaveCommand() {
        // 子类实现具体的保存逻辑
    }

    override fun createObserver() {
        super.createObserver()
        setFragmentResultListener(AppContants.Extras.FRAGMENT_DATA_CENTER_HOME_RESULT_REQUEST_KEY) { _, bundle ->
            handleFragmentResult(bundle)
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    protected fun getAdapterData(): MutableList<Any> {
        val groupList = mutableListOf<Any>()
        groupList.add(DeviceStatusInfoGroupItem("数据链路"))
        for (i in 1..centerNum) {
            groupList.add(
                DataCenterStatusItem(
                    centerid = i,
                    name = "数据链路$i",
                    status = "0",
                    bgResId = when (i) {
                        centerNum -> R.drawable.shape_common_click_item_bottom_corner_4
                        else -> R.drawable.layer_common_click_item_with_divider
                    }
                )
            )
        }
        return groupList
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    // 抽象方法，由子类实现
    /**
     * 查询数据，子类实现具体的查询逻辑
     */
    protected abstract fun queryData()

    /**
     * 获取导航动作ID
     */
    protected abstract fun getNavigationActionId(): Int

    /**
     * 处理从编辑页面返回的结果
     */
    protected open fun handleFragmentResult(bundle: Bundle) {
        binding.refreshLayout.autoRefresh()
    }

    /**
     * 获取点击代理，子类可以重写以提供自定义的点击处理逻辑
     */
    protected open fun getClickProxy(): BaseClickProxy {
        return BaseClickProxy()
    }


    companion object {
        const val CENTER_NUM = "center_num"

        fun newBundleArguments(
            centerNum: Int = 1,
            type: ProductType = ProductType.UnKnown,
            communicateWay: CommunicateWay = NetPlatformConnect,
            deviceInfo: DeviceInfo,
            bleDevice: DiscoveredBluetoothDevice? = null,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putInt(CENTER_NUM, centerNum)
            putParcelable(AppContants.Extras.PRODUCT_TYPE, type)
            putParcelable(AppContants.Extras.COMMUNICATION_WAY, communicateWay)
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
            putParcelable(AppContants.Extras.BLE_DEVICE, bleDevice)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
} 