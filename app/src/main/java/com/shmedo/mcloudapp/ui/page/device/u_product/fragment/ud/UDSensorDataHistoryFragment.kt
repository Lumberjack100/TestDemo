package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceFileInfo
import com.shmedo.core.model.DeviceInfo
import com.shmedo.core.model.EmptyInfo
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentUdSensorDataHistoryBinding
import com.shmedo.mcloudapp.databinding.ItemUdSensorDataBinding
import com.shmedo.mcloudapp.databinding.ItemUdSensorDataHeaderBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.showDatePickerDialog
import com.shmedo.mcloudapp.model.DeviceSensorDataHeaderItem
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.UDSensorDataHistoryViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.RecycleViewDivider
import org.koin.androidx.viewmodel.ext.android.viewModel

class UDSensorDataHistoryFragment : BaseFragment() {
    private lateinit var binding: FragmentUdSensorDataHistoryBinding
    private val mStates: UDSensorDataHistoryViewModel by viewModels()
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModel()

    private lateinit var deviceInfo: DeviceInfo
    private val modelNameList = arrayListOf("水面距离", "垂直方向角度", "抓拍图片")
    private val modelTokenList = arrayListOf("104", "206", "10001")
    private val modelFieldList = arrayListOf("高度(m)", "角度(°)", "操作")


    override fun initViewModel() {}

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_ud_sensor_data_history, BR.stateVM, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        mActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        binding = getBinding() as FragmentUdSensorDataHistoryBinding
        binding.llToolbar.toolbar.title = "历史数据"
        binding.llToolbar.toolbar.setNavigationOnClickListener {
            nav().navigateUp()
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                nav().navigateUp()
            }
        })
        initRefresh()
        initAdapter()
    }

    private fun initRefresh() {
        binding.refreshLayout.setEnableRefresh(false)
        binding.refreshLayout.onRefresh {
            refreshData()
        }
    }

    private fun initAdapter() {
        binding.recyclerview.linear().setup { rv ->
            rv.addItemDecoration(
                RecycleViewDivider(
                    LinearLayoutManager.VERTICAL, ConvertUtils.dp2px(1f), ColorUtils.getColor(
                        R.color.divider_line_bg
                    )
                )
            )
            addType<DeviceSensorDataHeaderItem>(R.layout.item_ud_sensor_data_header)
            addType<EmptyInfo>(R.layout.item_ud_sensor_data)
            onBind {
                try {
                    when (itemViewType) {
                        R.layout.item_ud_sensor_data_header -> {
                            val itemBinding = getBinding<ItemUdSensorDataHeaderBinding>()
                            itemBinding.tvValueName.text =
                                modelFieldList[modelNameList.indexOf(mStates.modelName.get())]
                        }

                        R.layout.item_ud_sensor_data -> {
                            val itemBinding = getBinding<ItemUdSensorDataBinding>()
                            itemBinding.tvIndex.text = modelPosition.toString()
                            itemBinding.tvValue.visibility =
                                if (mStates.modelName.get() == "抓拍图片") View.GONE else View.VISIBLE
                            itemBinding.tvOpt.visibility =
                                if (mStates.modelName.get() == "抓拍图片") View.VISIBLE else View.GONE

                            val item = getModel<EmptyInfo>()
                            if (item is DeviceFileInfo) {
                                itemBinding.tvTime.text =
                                    TimeUtils.date2String(TimeUtils.string2Date(item.uploadTime))
                                itemBinding.tvName.text = mStates.modelName.get()
                            }
                        }

                        else -> {}
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    override fun initData() {
        arguments?.let {
            deviceInfo = it.getParcelable(AppContants.Extras.DEVICE_INFO)!!
        }
        resetDefaultParams()
    }

    private fun resetDefaultParams() {
        //一周前
        mStates.startTimeMills.set(TimeUtils.getNowMills() - 7 * 24 * 3600 * 1000)
        mStates.endTimeMills.set(TimeUtils.getNowMills())
        mStates.startTime.set(
            TimeUtils.millis2String(
                mStates.startTimeMills.get(),
                "yyyy-MM-dd HH:mm"
            ) + ":00"
        )
        mStates.endTime.set(
            TimeUtils.millis2String(
                mStates.endTimeMills.get(),
                "yyyy-MM-dd HH:mm"
            ) + ":00"
        )
        mStates.modelName.set(modelNameList[2])
    }

    override fun createObserver() {
        deviceRequestViewModel.sensorDataListResult.observe(viewLifecycleOwner) { listDataResult: DataResult<List<EmptyInfo>> ->
            if (!listDataResult.responseStatus.isSuccess) {
                Toaster.show(listDataResult.responseStatus.errorMessage)
                return@observe
            }
            listDataResult.result?.let {
                binding.refreshLayout.addData(it, isEmpty = {
                    binding.refreshLayout.index == 1 && it.isEmpty()
                }, hasMore = {
                    binding.refreshLayout.index < listDataResult.totalPage
                })
            }
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        fun onChooseStartTimeClick() {
            requireContext().showDatePickerDialog(defaultDate = if (mStates.startTimeMills.get() == 0L) TimeUtils.getNowMills() else mStates.startTimeMills.get()) { time: Long ->
                if (TimeUtils.millis2String(
                        time, "yyyy-MM-dd HH:mm"
                    ) != mStates.endTime.get() && time > mStates.endTimeMills.get() && mStates.endTimeMills.get() != 0L
                ) {
                    Toaster.show("开始时间不能大于结束时间")
                    return@showDatePickerDialog
                }
                mStates.startTimeMills.set(time)
                mStates.startTime.set(TimeUtils.millis2String(time, "yyyy-MM-dd HH:mm") + ":00")
            }
        }

        fun onChooseEndTimeClick() {
            requireContext().showDatePickerDialog(defaultDate = if (mStates.endTimeMills.get() == 0L) TimeUtils.getNowMills() else mStates.endTimeMills.get()) { time: Long ->
                if (mStates.startTimeMills.get() > time) {
                    Toaster.show("结束时间不能小于开始时间")
                    return@showDatePickerDialog
                }
                mStates.endTimeMills.set(time)
                mStates.endTime.set(TimeUtils.millis2String(time, "yyyy-MM-dd HH:mm") + ":00")
            }
        }

        /**
         * 选择监测类型
         */
        fun onModelChooseClick() {
            val selectedIndex = modelNameList.indexOf(mStates.modelName.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "", modelNameList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mStates.modelName.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }
    }

    override fun lazyLoadData() {
        launchWithViewLifecycle {
            val sensorList =
                deviceRequestViewModel.queryDeviceSensor(deviceToken = deviceInfo.deviceToken)
            mStates.modelTokenMap.clear()
            sensorList.forEach {
                mStates.modelTokenMap[it.iotSensorType] = it
            }
            binding.refreshLayout.showLoading()
        }
    }

    private fun refreshData() {
        deviceRequestViewModel.queryDeviceFileListWithPage(
            deviceToken = deviceInfo.deviceToken,
            begin = mStates.startTime.get(),
            end = mStates.endTime.get(),
            currentPage = binding.refreshLayout.index,
            pageSize = PAGE_SIZE
        )
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    override fun onDestroyView() {
        mActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onDestroyView()
    }

    companion object {
        private const val PAGE_SIZE = 100
        fun newBundleArguments(
            deviceInfo: DeviceInfo,
        ): Bundle = Bundle().apply {
            putParcelable(AppContants.Extras.DEVICE_INFO, deviceInfo)
        }
    }
}