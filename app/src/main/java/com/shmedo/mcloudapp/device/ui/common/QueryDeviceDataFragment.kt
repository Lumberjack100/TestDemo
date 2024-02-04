package com.shmedo.mcloudapp.device.ui.common

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.brv.PageRefreshLayout
import com.drake.brv.utils.setup
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.common.ext.showMessageDialog
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.databinding.FragmentQueryDeviceDataBinding
import com.shmedo.mcloudapp.databinding.ItemDeviceDataBinding
import com.shmedo.mcloudapp.device.common.BaseClickProxy
import com.shmedo.mcloudapp.device.model.CloudDeviceData
import com.shmedo.mcloudapp.device.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.QueryDeviceDataViewModel
import com.shmedo.mcloudapp.device.viewmodel.state.ToolbarViewModel


class QueryDeviceDataFragment : BaseFragment() {
    private lateinit var binding: FragmentQueryDeviceDataBinding
    private lateinit var toolbarViewModel: ToolbarViewModel
    private lateinit var mHeadStates: QueryDeviceDataViewModel
    private lateinit var deviceRequestViewModel: DeviceRequestViewModel

    private val platformList: MutableList<String> =
        arrayListOf("物联网平台", "MDNET平台")
    private val dataTypeList: MutableList<String> =
        arrayListOf("全部数据类型", "传感器数据", "设备状态数据")
    private val periodDateList: MutableList<String> = arrayListOf(
        "当天",
        "两天内",
        "三天内",
        "一周内",
        "一个月内",
        "三个月内",
        "半年内",
        "一年内"
    )
    private var startTime: String = TimeUtils.millis2String(
        TimeUtils.getNowMills(),
        "yyyy-MM-dd"
    )
    private val endTime: String =
        TimeUtils.millis2String(TimeUtils.getNowMills(), "yyyy-MM-dd HH:mm:ss")

    private var statusBarColor = 0
    private var keyWord: String = ""


    override fun initViewModel() {
        toolbarViewModel = getFragmentScopeViewModel()
        mHeadStates = getFragmentScopeViewModel()
        deviceRequestViewModel = getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_query_device_data, BR.stateVM, mHeadStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentQueryDeviceDataBinding
        binding.llToolbar.toolbar.title = "数据查询"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        initRefresh()
        initAdapter()
    }

    private fun initRefresh() {
        PageRefreshLayout.startIndex = 1
        binding.refreshLayout.onRefresh {
            refreshData()
        }
    }

    private fun initAdapter() {
        binding.recyclerView.setup { rv ->
            addType<CloudDeviceData>(R.layout.item_device_data)
            onBind {
                val cloudDeviceData = getModel<CloudDeviceData>()
                val itemDeviceDataBinding = getBinding<ItemDeviceDataBinding>()
                try {
                    val gson = GsonBuilder()
                        .serializeNulls()
                        .setPrettyPrinting()
                        .disableHtmlEscaping()
                        .create()
                    var content = cloudDeviceData.content.replace("\u0000", "") // removes NUL chars
                    content = content.replace("\\u0000", "") // removes backslash+u0000
                    val prettyJson = gson.toJson(JsonParser.parseString(content))
                    itemDeviceDataBinding.tvDataContent.text = prettyJson
                } catch (ex: Exception) {
                    itemDeviceDataBinding.tvDataContent.text = cloudDeviceData.content
                    ex.printStackTrace()
                }
            }
        }
    }

    override fun initData() {
        arguments?.let {
            keyWord = it.getString(AppContants.Extras.DEVICE_SEARCH_KEYWORD, "")
            statusBarColor = it.getInt(AppContants.Extras.STATUS_BAR_COLOR)
        }
        mHeadStates.sn.set(keyWord)
        mHeadStates.platformType.set(platformList[0])
        mHeadStates.dataType.set(dataTypeList[0])
        mHeadStates.periodDate.set(periodDateList[0])
    }

    override fun createObserver() {
        deviceRequestViewModel.cloudDeviceDataListResult.observe(viewLifecycleOwner) { listDataResult: DataResult<List<CloudDeviceData>> ->
            if (!listDataResult.responseStatus.isSuccess) {
                Toaster.show(listDataResult.responseStatus.errorMessage)
                if (binding.refreshLayout.state == RefreshState.Refreshing)
                    binding.refreshLayout.finishRefresh(false)
                else
                    binding.refreshLayout.finishLoadMore(false)

                return@observe
            }
            binding.refreshLayout.addData(listDataResult.result, hasMore = {
                binding.refreshLayout.index < listDataResult.totalPage
            })
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.autoRefresh()
    }

    inner class ClickProxy : BaseClickProxy() {
        fun onChoosePlatformClick() {
            val selectedIndex = platformList.indexOf(mHeadStates.platformType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择平台", platformList.toTypedArray(),
                    null, selectedIndex,
                    { _, text ->
                        mHeadStates.platformType.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onChooseDataTypeClick() {
            val selectedIndex = dataTypeList.indexOf(mHeadStates.dataType.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择数据类型", dataTypeList.toTypedArray(),
                    null, selectedIndex,
                    { _, text ->
                        mHeadStates.dataType.set(text)
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onChoosePeriodDateClick() {
            val selectedIndex = periodDateList.indexOf(mHeadStates.periodDate.get())
            XPopup.setPrimaryColor(ColorUtils.getColor(R.color.colorPrimary))
            XPopup.Builder(context)
                .maxHeight((ScreenUtils.getAppScreenHeight() * 0.6f).toInt())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .enableDrag(false)
                .asBottomList(
                    "请选择时间范围", periodDateList.toTypedArray(),
                    null, selectedIndex,
                    { position, text ->
                        mHeadStates.periodDate.set(text)
                        when (position) {
                            0 -> {//当天
                                startTime = TimeUtils.millis2String(
                                    TimeUtils.getNowMills(),
                                    "yyyy-MM-dd"
                                )
                            }

                            1 -> {//两天内
                                startTime = TimeUtils.millis2String(
                                    TimeUtils.getNowMills() - 2L * 24 * 3600 * 1000,
                                    "yyyy-MM-dd"
                                )
                            }

                            2 -> {//三天内
                                startTime = TimeUtils.millis2String(
                                    TimeUtils.getNowMills() - 3L * 24 * 3600 * 1000,
                                    "yyyy-MM-dd"
                                )
                            }

                            3 -> {//一周内
                                startTime = TimeUtils.millis2String(
                                    TimeUtils.getNowMills() - 7L * 24 * 3600 * 1000,
                                    "yyyy-MM-dd"
                                )
                            }

                            4 -> {//一个月内
                                startTime = TimeUtils.millis2String(
                                    TimeUtils.getNowMills() - 30L * 24 * 3600 * 1000,
                                    "yyyy-MM-dd"
                                )
                            }

                            5 -> {//三个月内
                                startTime = TimeUtils.millis2String(
                                    TimeUtils.getNowMills() - 90L * 24 * 3600 * 1000,
                                    "yyyy-MM-dd"
                                )
                            }

                            6 -> {//半年内
                                startTime = TimeUtils.millis2String(
                                    TimeUtils.getNowMills() - 180L * 24 * 3600 * 1000,
                                    "yyyy-MM-dd"
                                )
                            }

                            7 -> {//一年内
                                startTime = TimeUtils.millis2String(
                                    TimeUtils.getNowMills() - 365L * 24 * 3600 * 1000,
                                    "yyyy-MM-dd"
                                )
                            }
                        }
                    }, 0, R.layout.custom_xpopup_adapter_text_center
                )
                .show()
        }

        fun onSearchClick() {
            if (mHeadStates.sn.get().isEmpty()) {
                showMessageDialog("请输入 SN 号")
                return
            }
            binding.refreshLayout.autoRefresh()
        }
    }

    private fun refreshData() {
        deviceRequestViewModel.queryCloudDataExWithPage(
            sn = mHeadStates.sn.get(),
            condition = mHeadStates.regexContent.get(),
            iotData = mHeadStates.platformType.get() == platformList[0],
            dataType = if (mHeadStates.dataType.get() == dataTypeList[0]) ""
            else if (mHeadStates.dataType.get() == dataTypeList[1]) "0"
            else "1",
            timeSort = false,
            begin = "$startTime 00:00:00",
            end = endTime,
            currentPage = binding.refreshLayout.index,
            pageSize = PAGE_SIZE
        )
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        private const val PAGE_SIZE = 50
        fun newBundleArguments(
            keyWord: String,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putString(AppContants.Extras.DEVICE_SEARCH_KEYWORD, keyWord)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}