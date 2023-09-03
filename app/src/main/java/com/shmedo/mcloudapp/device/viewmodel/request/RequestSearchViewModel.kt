package com.shmedo.mcloudapp.device.viewmodel.request


import com.kunminx.architecture.domain.message.MutableResult
import com.shmedo.lib.core.base.viewmodel.BaseViewModel
import com.shmedo.lib.core.ext.launch
import com.shmedo.lib.core.util.MmkvCacheUtil

/**
 * 作者　: hegaojian
 * 时间　: 2020/2/29
 * 描述　:
 */
class RequestSearchViewModel : BaseViewModel() {
    //搜索历史词数据
    val historyData: MutableResult<ArrayList<String>> = MutableResult()

    /**
     * 获取历史数据
     */
    fun getHistoryData() {
        launch({
            MmkvCacheUtil.getSearchHistoryData()
        }, {
            val tempList = arrayListOf<String>()
            tempList.addAll(it)
            historyData.value = tempList
        }, {
            //获取本地历史数据出异常了
        })
    }
}