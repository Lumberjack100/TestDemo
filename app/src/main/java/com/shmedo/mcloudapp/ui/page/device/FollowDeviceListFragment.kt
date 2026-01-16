package com.shmedo.mcloudapp.ui.page.device

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.PageRefreshLayout
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.lxj.xpopup.XPopup
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentFollowDeviceListBinding
import com.shmedo.mcloudapp.extensions.InsetsManager
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * @author：gonghe
 * @time: 2025/6/23
 * @desc: 收藏的设备列表页面
 *
 */
class FollowDeviceListFragment : BaseFragment() {
    private lateinit var binding: FragmentFollowDeviceListBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModel()

    private var deleteItemIndex = 0


    override fun initViewModel() {
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_follow_device_list,
            BR.toolbarVM,
            toolbarViewModel
        )
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentFollowDeviceListBinding
        binding.llToolbar.toolbar.title = "我的收藏"
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
                val deviceInfo = getModel<DeviceInfo>().apply {
                    deviceToken = deviceToken.ifEmpty { deviceSn }
                }
                DeviceHomeActivity.start(mActivity, deviceInfo)
            }
            R.id.item.onLongClick {
                val deviceInfo = getModel<DeviceInfo>().apply {
                    deviceToken = deviceToken.ifEmpty { deviceSn }
                }
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

                        "取消收藏" -> {
                            deleteItemIndex = modelPosition
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

    override fun createObserver() {
        deviceRequestViewModel.followDeviceListResult.observe(viewLifecycleOwner) { listDataResult: DataResult<List<DeviceInfo>> ->
            if (!listDataResult.responseStatus.isSuccess) {
                Toaster.show(listDataResult.responseStatus.errorMessage)
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
        deviceRequestViewModel.cancelFollowDeviceResult.observe(viewLifecycleOwner) { dataResult: DataResult<String> ->
            if (!dataResult.responseStatus.isSuccess) {
                Toaster.show(dataResult.responseStatus.errorMessage)
                return@observe
            }
            Toaster.show("已取消收藏")
            binding.recyclerviewDevice.bindingAdapter.mutable.removeAt(deleteItemIndex)
            binding.recyclerviewDevice.bindingAdapter.notifyItemRemoved(deleteItemIndex)
        }
    }

    override fun lazyLoadData() {
        binding.refreshLayout.showLoading()
    }

    private fun queryDeviceList() {
        deviceRequestViewModel.getFollowDeviceList(
            currentPage = binding.refreshLayout.index,
            pageSize = PAGE_SIZE,
        )
    }

    private fun unFollowDevice(deviceSn: String) {
        deviceRequestViewModel.cancelUserFollowDevice(deviceSn)
    }


    companion object {
        private const val PAGE_SIZE = 20
    }
}