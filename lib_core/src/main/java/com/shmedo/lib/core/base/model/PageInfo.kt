package com.shmedo.lib.core.base.model

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/21 <br></br>
 * 描述：     数据分页信息
 */
class PageInfo(private var firstPage: Int = 0) {
    var page = firstPage
        private set

    fun nextPage() {
        page++
    }

    fun reset() {
        page = firstPage
    }

    fun isFirstPage(): Boolean {
        return page == firstPage
    }

}
