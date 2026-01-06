package com.shmedo.mcloudapp.ui.page.device

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.PageRefreshLayout
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.commonlib.mmkv.AuthMMKVOwner
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentDeviceSearchResultBinding
import com.shmedo.mcloudapp.extensions.InsetsManager
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/8/31
 *
 * 描述： 设备搜索结果展示页面
 *
 *
 */
class DeviceSearchResultFragment : BaseFragment() {
    private lateinit var binding: FragmentDeviceSearchResultBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModel()

    private var statusBarColor = 0
    private lateinit var keyWord: String


    override fun initViewModel() {
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_device_search_result,
            BR.toolbarVM,
            toolbarViewModel
        )
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDeviceSearchResultBinding
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
        initDeviceInfoAdapter()
        initRefresh()

        InsetsManager.liftSpecificBottomView(
            binding.root,
            binding.recyclerviewDevice,
            applyTo = InsetsManager.ApplyTo.Padding,
            extraBottomPaddingDp = 25
        )
    }

    private fun initDeviceInfoAdapter() {
        binding.recyclerviewDevice.setup { rv ->
            rv.addItemDecoration(
                MyGridSpacingItemDecoration(
                    2,
                    ConvertUtils.dp2px(10f),
                    false
                )
            )
            addType<DeviceInfo>(R.layout.item_device_info)
            R.id.item.onClick {
                val deviceInfo = getModel<DeviceInfo>()
                DeviceHomeActivity.start(mActivity, deviceInfo)
            }
            R.id.item.onLongClick {
                val deviceInfo = getModel<DeviceInfo>()
                val dataList = if (deviceInfo.followTime.isNullOrEmpty()) arrayListOf(
                    "查看数据",
                    "收藏"
                ) else arrayListOf("查看数据", "取消收藏")

                val builder = XPopup.Builder(context)
                    .hasShadowBg(true)
                    .atView(itemView)
                builder.asAttachList(dataList.toTypedArray(), null)
                { _, text ->
                    when (text) {
                        "查看数据" -> {
                            QuickFunctionActivity.start(mActivity, deviceInfo)
                        }

                        "收藏" -> {
                            followDevice(deviceInfo.deviceToken)
                        }

                        "取消收藏" -> {
                            unFollowDevice(deviceInfo.deviceToken)
                        }
                    }
                }
                    .show()
                true
            }
        }
    }

    private fun initRefresh() {
        PageRefreshLayout.startIndex = 1
        binding.refreshLayout.onRefresh {
            queryDeviceList()
        }
    }

    override fun initData() {
        arguments?.let {
            keyWord = it.getString(AppContants.Extras.DEVICE_SEARCH_KEYWORD, "")
            statusBarColor = it.getInt(AppContants.Extras.STATUS_BAR_COLOR)
            binding.llToolbar.toolbar.title = keyWord
        }
    }

    override fun createObserver() {
        deviceRequestViewModel.deviceListResult.observe(viewLifecycleOwner) { listDataResult: DataResult<List<DeviceInfo>> ->
            if (!listDataResult.responseStatus.isSuccess) {
                Toaster.show(listDataResult.responseStatus.errorMessage)
                binding.refreshLayout.showError()
                return@observe
            }
            if (listDataResult.result.isNullOrEmpty()) {
                binding.refreshLayout.showEmpty()
                return@observe
            }
            binding.refreshLayout.addData(listDataResult.result, hasMore = {
                binding.refreshLayout.index < listDataResult.totalPage
            })
        }
        deviceRequestViewModel.followDeviceResult.observe(viewLifecycleOwner) { dataResult: DataResult<String> ->
            if (!dataResult.responseStatus.isSuccess) {
                Toaster.show(dataResult.responseStatus.errorMessage)
                return@observe
            }
            Toaster.show("已收藏")
        }
        deviceRequestViewModel.cancelFollowDeviceResult.observe(viewLifecycleOwner) { dataResult: DataResult<String> ->
            if (!dataResult.responseStatus.isSuccess) {
                Toaster.show(dataResult.responseStatus.errorMessage)
                return@observe
            }
            Toaster.show("已取消收藏")
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.showLoading()
    }

    private fun queryDeviceList() {
        deviceRequestViewModel.getDeviceList(
            companyID = AuthMMKVOwner.companyID,
            deviceToken = keyWord,
            currentPage = binding.refreshLayout.index,
            pageSize = PAGE_SIZE,
            isHasListSuperInfoPermission = AuthMMKVOwner.listSuperInfoPermission
        )
    }

    private fun followDevice(deviceSn: String) {
        deviceRequestViewModel.addUserFollowDevice(deviceSn)
    }

    private fun unFollowDevice(deviceSn: String) {
        deviceRequestViewModel.cancelUserFollowDevice(deviceSn)
    }


    companion object {
        private const val PAGE_SIZE = 20
        fun newBundleArguments(
            keyWord: String,
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putString(AppContants.Extras.DEVICE_SEARCH_KEYWORD, keyWord)
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}