package com.shmedo.mcloudapp.ui.widget.tableview.model

import com.shmedo.mcloudapp.R

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/11 <br/>
 * 描述：     通讯数据表格单元格数据
 */
data class CommunicationDataCellModel(
    val mData: String,
    val textColorResId: Int = R.color.title_text_color
)
