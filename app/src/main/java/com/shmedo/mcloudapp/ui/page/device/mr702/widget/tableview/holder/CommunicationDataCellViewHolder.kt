package com.shmedo.mcloudapp.ui.page.device.mr702.widget.tableview.holder

import android.view.View
import android.widget.LinearLayout
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.shmedo.mcloudapp.databinding.CommunicationDataTableviewCellLayoutBinding
import com.shmedo.mcloudapp.ui.page.device.mr702.widget.tableview.model.CommunicationDataCellModel

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/11 <br/>
 * 描述：     TODO
 */
class CommunicationDataCellViewHolder(itemView: View) : AbstractViewHolder(itemView) {
    private var binding: CommunicationDataTableviewCellLayoutBinding

    init {
        binding = CommunicationDataTableviewCellLayoutBinding.bind(itemView)
    }

    fun setCellModel(cellModel: CommunicationDataCellModel, pColumnPosition: Int) {
        // Set text
        binding.cellData.text = cellModel.mData
        binding.cellData.setTextColor(binding.cellData.context.getColor(cellModel.textColorResId))
        // It is necessary to remeasure itself.
        binding.cellContainer.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        binding.cellData.requestLayout()
    }
}