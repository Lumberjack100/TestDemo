package com.shmedo.mcloudapp.device.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getFragmentScopeViewModel
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.showMessage
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.common.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.databinding.FragmentDeviceSearchBinding
import com.shmedo.mcloudapp.device.viewmodel.request.RequestSearchViewModel

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/8/31
 *
 * 描述： 设备搜索关键字页面
 *
 *
 */
class DeviceSearchFragment : BaseFragment() {
    private lateinit var binding: FragmentDeviceSearchBinding
    private lateinit var mStates: EmptyViewModel
    private lateinit var requestSearchViewModel: RequestSearchViewModel


    override fun initViewModel() {
        mStates =  getFragmentScopeViewModel()
        requestSearchViewModel =  getFragmentScopeViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_device_search, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentDeviceSearchBinding
        //设置menu 关键代码
        mActivity.setSupportActionBar(binding.llToolbar.toolbar)
        addMenu()
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        initHistoryAdapter()
        binding.searchClear.setOnClickListener {
            showMessage("确定清空吗", "温馨提示", "清空", {
                //清空
                requestSearchViewModel.historyData.value = arrayListOf()
            }, "取消")
        }
    }

    private fun initHistoryAdapter() {
        binding.searchHistoryRv.setup { rv ->
            addType<String>(R.layout.search_item_history)
            R.id.item.onClick {
                val queryStr = getModel<String>()
                goToSearchResultPage(queryStr)
            }
            R.id.item_history_del.onClick {
                requestSearchViewModel.historyData.value?.let {
                    it.removeAt(modelPosition)
                    requestSearchViewModel.historyData.value = it
                }
            }
        }
    }

    override fun createObserver() {
        requestSearchViewModel.historyData.observe(viewLifecycleOwner) { data ->
            binding.searchHistoryRv.models = data
            MmkvCacheUtil.setSearchHistoryData(MoshiUtil.toJson(data.toList()))
        }
    }

    override fun lazyLoadData() {
        //获取历史搜索词数据
        requestSearchViewModel.getHistoryData()
    }

    private fun addMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_search, menu)
                val menuItem = menu.findItem(R.id.action_search)
                (menuItem?.actionView as SearchView).let { searchView ->
                    searchView.run {
                        maxWidth = Integer.MAX_VALUE
                        onActionViewExpanded()
                        queryHint = "输入设备 SN 关键字搜索"
                        setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                            //SearchView的监听
                            override fun onQueryTextSubmit(query: String?): Boolean {
                                //当点击搜索时 输入法的搜索，和右边的搜索都会触发
                                query?.let { queryStr ->
                                    goToSearchResultPage(queryStr)
                                }
                                return false
                            }

                            override fun onQueryTextChange(newText: String?): Boolean {
                                return false
                            }
                        })
                        isSubmitButtonEnabled = true //右边是否展示搜索图标
                        val field = javaClass.getDeclaredField("mGoButton")
                        field.run {
                            isAccessible = true
                            val mGoButton = get(searchView) as ImageView
                            mGoButton.setImageResource(R.drawable.ic_search)
                        }
                    }
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun goToSearchResultPage(queryStr: String) {
        updateKey(queryStr)
        val bundle = DeviceSearchResultFragment.newBundleArguments(
            queryStr,
            R.color.white
        )
        nav().navigate(
            R.id.action_deviceSearchFragment_to_deviceSearchResultFragment,
            bundle
        )
    }

    /**
     * 更新搜索词
     */
    private fun updateKey(keyStr: String) {
        requestSearchViewModel.historyData.value?.let { dataList ->
            if (dataList.contains(keyStr)) {
                //当搜索历史中包含该数据时 删除
                dataList.remove(keyStr)
            } else if (dataList.size >= 10) {
                //如果集合的size 有10个以上了，删除最后一个
                dataList.removeAt(dataList.size - 1)
            }
            //添加新数据到第一条
            dataList.add(0, keyStr)
            requestSearchViewModel.historyData.value = dataList
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar, false)
    }

    companion object {
        fun newBundleArguments(
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }
}