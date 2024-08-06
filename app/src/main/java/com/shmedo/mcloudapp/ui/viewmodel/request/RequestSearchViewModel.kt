package com.shmedo.mcloudapp.ui.viewmodel.request


import com.kunminx.architecture.domain.message.MutableResult
import com.shmedo.core.commonlib.utils.MmkvCacheUtil
import com.shmedo.core.data.repository.LoggerRepositoryImp
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseRequestViewModel
import com.shmedo.mcloudapp.extensions.launch

/**
 * 作者　:
 * 时间　:
 * 描述　:
 */
class RequestSearchViewModel(private val loggerRepositoryImp: LoggerRepositoryImp) :
    BaseRequestViewModel(loggerRepositoryImp) {

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