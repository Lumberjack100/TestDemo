package com.shmedo.mcloudapp.device.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.core.ext.getAppViewModel
import com.shmedo.lib.core.util.AppContants
import com.shmedo.lib.core.util.MmkvCacheUtil
import com.shmedo.lib.core.util.MoshiUtil
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.ext.initClose
import com.shmedo.mcloudapp.common.ext.nav
import com.shmedo.mcloudapp.common.ext.showMessage
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.common.viewmodel.state.EmptyViewModel
import com.shmedo.mcloudapp.common.viewmodel.state.PageMessenger
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
    private val binding: FragmentDeviceSearchBinding by lazy { getBinding() as FragmentDeviceSearchBinding }
    private val mMessenger: PageMessenger by lazy { getAppViewModel() }
    private val mStates: EmptyViewModel by viewModels()
    private val requestSearchViewModel: RequestSearchViewModel by viewModels()


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_device_search, BR.vm, mStates)
    }

    override fun initView(savedInstanceState: Bundle?) {
        setMenu()
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
            rv.layoutManager = LinearLayoutManager(context)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        val searchView = menu.findItem(R.id.action_search)?.actionView as SearchView
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
        super.onCreateOptionsMenu(menu, inflater)
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
        requestSearchViewModel.historyData.value?.let {
            if (it.contains(keyStr)) {
                //当搜索历史中包含该数据时 删除
                it.remove(keyStr)
            } else if (it.size >= 10) {
                //如果集合的size 有10个以上了，删除最后一个
                it.removeAt(it.size - 1)
            }
            //添加新数据到第一条
            it.add(0, keyStr)
            requestSearchViewModel.historyData.value = it
        }
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar, false)
        //当该Fragment重新获得视图时，重新设置Menu，防止退出WebFragment ActionBar被清空后，导致该界面的ActionBar无法显示bug
        setMenu()
    }

    private fun setMenu() {
        setHasOptionsMenu(true)
        binding.llToolbar.toolbar.run {
            //设置menu 关键代码
            mActivity.setSupportActionBar(this)
            initClose {
                nav().navigateUp()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mActivity.setSupportActionBar(null)
    }

    companion object {
        fun newBundleArguments(
            statusBarColor: Int = R.color.white
        ): Bundle = Bundle().apply {
            putInt(AppContants.Extras.STATUS_BAR_COLOR, statusBarColor)
        }
    }

}