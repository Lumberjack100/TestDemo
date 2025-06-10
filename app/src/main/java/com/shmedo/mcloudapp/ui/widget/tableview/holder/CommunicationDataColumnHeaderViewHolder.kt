package com.shmedo.mcloudapp.ui.widget.tableview.holder

import android.view.View
import android.widget.LinearLayout
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.shmedo.mcloudapp.databinding.CommunicationDataTableviewColumnHeaderLayoutBinding
import com.shmedo.mcloudapp.ui.widget.tableview.model.CommunicationDataCellModel

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/11 <br/>
 * 描述：     TODO
 */
class CommunicationDataColumnHeaderViewHolder(itemView: View) : AbstractViewHolder(itemView) {
    private var binding: CommunicationDataTableviewColumnHeaderLayoutBinding

    init {
        binding = CommunicationDataTableviewColumnHeaderLayoutBinding.bind(itemView)
    }

    fun setCellModel(cellModel: CommunicationDataCellModel, pColumnPosition: Int) {
        // Set text
        binding.columnHeaderTextView.text = cellModel.mData
        // It is necessary to remeasure itself.
        binding.columnHeaderContainer.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        binding.columnHeaderTextView.requestLayout()
    }
}