package com.shmedo.mcloudapp.ui.page.device

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.PageRefreshLayout
import com.drake.brv.utils.setup
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.core.commonlib.mmkv.AuthMMKVOwner
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.core.model.DeviceInfo
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentDeviceSelectBinding
import com.shmedo.mcloudapp.extensions.launchWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.DeviceRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.DeviceSelectViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.widget.recyclerview.MyGridSpacingItemDecoration
import kotlinx.coroutines.delay
import org.koin.androidx.viewmodel.ext.android.viewModel
/**
 * @author：gonghe
 * @time: 2025/8/11
 * @desc: 选择设备
 *
 */
class DeviceSelectFragment : BaseFragment() {
    private lateinit var binding: FragmentDeviceSelectBinding

    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: DeviceSelectViewModel by viewModels()
    private val deviceRequestViewModel: DeviceRequestViewModel by viewModel()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_device_select,
            BR.toolbarVM,
            toolbarViewModel
        )
            .addBindingParam(BR.stateVM, mStates)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDeviceSelectBinding
        binding.llToolbar.toolbar.title = "设备选择"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
        initRefresh()
        initDeviceInfoAdapter()
    }

    private fun initRefresh() {
        PageRefreshLayout.startIndex = 1
        binding.refreshLayout.onRefresh {
            if (mStates.deviceToken.get().isEmpty()) {
                Toaster.show("请输入设备SN号")
                binding.refreshLayout.finishRefresh()
                return@onRefresh
            }
            queryDeviceList()
        }
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

                launchWithViewLifecycle {
                    delay(500)
                    // 需要给上一级页面传递最新的信息
                    setFragmentResult(
                        AppContants.Extras.FRAGMENT_COMMON_RESULT_REQUEST_KEY,
                        bundleOf(AppContants.Extras.DEVICE_INFO to deviceInfo)
                    )
                    nav().navigateUp()
                }
            }
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
    }

    private fun queryDeviceList() {
        deviceRequestViewModel.getDeviceList(
            companyID = AuthMMKVOwner.companyID,
            deviceToken = mStates.deviceToken.get(),
            currentPage = binding.refreshLayout.index,
            pageSize = 20,
            isHasListSuperInfoPermission = AuthMMKVOwner.listSuperInfoPermission
        )
    }

    inner class ClickProxy : BaseClickProxy() {
        override fun onGoToSearch() {
            binding.refreshLayout.showLoading()
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar, isKeyboardEnable = true)
    }

}