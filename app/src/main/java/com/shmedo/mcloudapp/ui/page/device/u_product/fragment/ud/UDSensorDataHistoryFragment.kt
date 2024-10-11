package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.brv.PageRefreshLayout
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnExternalPreviewEventListener
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.extensions.compareAndReturn
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceInfo
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
import com.shmedo.mcloudapp.model.HoverHeaderModel
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.UDSensorDataHistoryViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.RecycleViewDivider
import com.shmedo.mcloudapp.utils.image.GlideEngine
import org.koin.androidx.viewmodel.ext.android.viewModel

class UDSensorDataHistoryFragment : BaseFragment() {
    private lateinit var binding: FragmentUdSensorDataHistoryBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: UDSensorDataHistoryViewModel by viewModels()
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModel()

    private lateinit var deviceInfo: DeviceInfo

    private val modelNameList = arrayListOf("液面高程", "安装角度", "抓拍图片")
    private val modelTokenList = arrayListOf("904", "206", "10001")
    private val modelValueDescList = arrayListOf("高度(m)", "角度(°)", "操作")
    private val modelFieldJsonPathList = arrayListOf("liquid_surface_alt", "z")

    private val sensorIDList: MutableList<String> = arrayListOf()
    private val mImageData: ArrayList<LocalMedia> = ArrayList()


    override fun initViewModel() {}

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_ud_sensor_data_history, BR.stateVM, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
//        mActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        binding = getBinding() as FragmentUdSensorDataHistoryBinding
        toolbarViewModel.toolbarTitleText.set("历史数据")
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
        PageRefreshLayout.startIndex = 1
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
            addType<HoverHeaderModel>(R.layout.item_ud_sensor_data_header)
            addType<Map<String, String>>(R.layout.item_ud_sensor_data)
            onBind {
                try {
                    when (itemViewType) {
                        R.layout.item_ud_sensor_data_header -> {
                            val itemBinding = getBinding<ItemUdSensorDataHeaderBinding>()
                            itemBinding.tvValueName.text =
                                modelValueDescList[modelNameList.indexOf(mStates.modelName.get())]
                        }

                        R.layout.item_ud_sensor_data -> {
                            val itemBinding = getBinding<ItemUdSensorDataBinding>()
                            itemBinding.tvIndex.text = modelPosition.toString()
                            itemBinding.tvValue.visibility = mStates.modelName.get()
                                .compareAndReturn("抓拍图片", View.GONE, View.VISIBLE)
                            itemBinding.llOpt.visibility = mStates.modelName.get()
                                .compareAndReturn("抓拍图片", View.VISIBLE, View.GONE)

                            val itemMap = getModel<Map<String, String>>()
                            when (mStates.modelName.get()) {
                                "液面高程" -> {
                                    itemBinding.tvTime.text =
                                        TimeUtils.date2String(
                                            TimeUtils.string2Date(itemMap["time"]),
                                            "yy.MM.dd HH:mm:ss"
                                        )
                                    itemBinding.tvName.text = mStates.modelName.get()
                                    itemBinding.tvValue.text = itemMap[modelFieldJsonPathList[0]]
                                }

                                "安装角度" -> {
                                    itemBinding.tvTime.text =
                                        TimeUtils.date2String(
                                            TimeUtils.string2Date(itemMap["time"]),
                                            "yy.MM.dd HH:mm:ss"
                                        )
                                    itemBinding.tvName.text = mStates.modelName.get()
                                    itemBinding.tvValue.text = itemMap[modelFieldJsonPathList[1]]
                                }

                                "抓拍图片" -> {
                                    itemBinding.tvTime.text =
                                        TimeUtils.date2String(
                                            TimeUtils.string2Date(itemMap["uploadTime"]),
                                            "yy.MM.dd HH:mm:ss"
                                        )
                                    itemBinding.tvName.text = mStates.modelName.get()
                                }

                                else -> {}
                            }
                        }

                        else -> {}
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
            R.id.tv_opt.onClick {
                val itemMap = getModel<Map<String, String>>()
                if (itemMap.containsKey("filePath")) {
                    val filePath = itemMap["filePath"]
                    val shortFileName = itemMap["fileName"]

                    mImageData.clear()
                    mImageData.add(LocalMedia.generateHttpAsLocalMedia(filePath)
                        .apply {
                            fileName = shortFileName
                        })
                    openPreview(0)
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
                "yy.MM.dd HH:mm"
            )
        )
        mStates.endTime.set(
            TimeUtils.millis2String(
                mStates.endTimeMills.get(),
                "yy.MM.dd HH:mm"
            )
        )
        mStates.modelName.set(modelNameList[0])
    }

    override fun createObserver() {
        deviceRequestViewModel.sensorDataListResult.observe(viewLifecycleOwner) { listDataResult: DataResult<List<Any>> ->
            if (!listDataResult.responseStatus.isSuccess) {
                Toaster.show(listDataResult.responseStatus.errorMessage)
                binding.refreshLayout.showError()
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
                        time, "yy.MM.dd HH:mm"
                    ) != mStates.endTime.get() && time > mStates.endTimeMills.get() && mStates.endTimeMills.get() != 0L
                ) {
                    Toaster.show("开始时间不能大于结束时间")
                    return@showDatePickerDialog
                }
                mStates.startTimeMills.set(time)
                mStates.startTime.set(TimeUtils.millis2String(time, "yy.MM.dd HH:mm"))
                if (mStates.endTimeMills.get() == 0L) {
                    binding.refreshLayout.showLoading()
                }
            }
        }

        fun onChooseEndTimeClick() {
            requireContext().showDatePickerDialog(defaultDate = if (mStates.endTimeMills.get() == 0L) TimeUtils.getNowMills() else mStates.endTimeMills.get()) { time: Long ->
                if (mStates.startTimeMills.get() > time) {
                    Toaster.show("结束时间不能小于开始时间")
                    return@showDatePickerDialog
                }
                mStates.endTimeMills.set(time)
                mStates.endTime.set(TimeUtils.millis2String(time, "yy.MM.dd HH:mm"))
                if (mStates.startTimeMills.get() == 0L) {
                    binding.refreshLayout.showLoading()
                }
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
                        if (mStates.modelName.get() == "抓拍图片") {
                            sensorIDList.clear()
                        } else {
                            if (position in modelTokenList.indices) {
                                mStates.modelTokenMap[modelTokenList[position]]?.let { sensorBasicInfo ->
                                    sensorIDList.clear()
                                    sensorIDList.add(sensorBasicInfo.id)
                                }
                            }
                        }
                        binding.refreshLayout.showLoading()
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

            sensorIDList.clear()
            val position = modelNameList.indexOf(mStates.modelName.get())
            mStates.modelTokenMap[modelTokenList[position]]?.let { sensorBasicInfo ->
                sensorIDList.add(sensorBasicInfo.id)
            }
            binding.refreshLayout.showLoading()
        }
    }

    private fun refreshData() {
        deviceRequestViewModel.queryMonitorDataListWithPage(
            deviceToken = deviceInfo.deviceToken,
            sensorIDList = sensorIDList,
            begin = TimeUtils.millis2String(
                mStates.startTimeMills.get(),
                "yyyy-MM-dd HH:mm"
            ) + ":00",
            end = TimeUtils.millis2String(
                mStates.endTimeMills.get(),
                "yyyy-MM-dd HH:mm"
            ) + ":00",
            currentPage = binding.refreshLayout.index,
            pageSize = PAGE_SIZE
        )
    }

    /**
     * 打开预览
     */
    private fun openPreview(currentPosition: Int) {
        // 预览图片、视频、音频
        PictureSelector.create(requireContext()).openPreview()
            .setImageEngine(GlideEngine.createGlideEngine())
            .setExternalPreviewEventListener(ImagePreviewEventListener())
            .isHidePreviewDownload(false)
            .startActivityPreview(currentPosition, false, mImageData)
    }

    /**
     * 外部预览监听事件
     */
    private inner class ImagePreviewEventListener : OnExternalPreviewEventListener {
        override fun onPreviewDelete(position: Int) {

        }

        override fun onLongPressDownload(context: Context?, media: LocalMedia?): Boolean {
            return false
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    override fun onDestroyView() {
//        mActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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