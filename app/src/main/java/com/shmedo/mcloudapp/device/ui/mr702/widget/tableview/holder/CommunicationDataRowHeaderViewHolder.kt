package com.shmedo.mcloudapp.device.ui.mr702.widget.tableview.holder

import android.view.View
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.shmedo.mcloudapp.databinding.CommunicationDataTableviewRowHeaderLayoutBinding
import com.shmedo.mcloudapp.device.ui.mr702.widget.tableview.model.CommunicationDataCellModel

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/11 <br/>
 * 描述：     TODO
 */
class CommunicationDataRowHeaderViewHolder(itemView: View) : AbstractViewHolder(itemView) {
    private var binding: CommunicationDataTableviewRowHeaderLayoutBinding

    init {
        binding = CommunicationDataTableviewRowHeaderLayoutBinding.bind(itemView)
    }

    fun setCellModel(cellModel: CommunicationDataCellModel) {
        binding.rowHeaderTextView.text = cellModel.mData
    }

//    override fun setBackgroundColor(p_nColor: Int) {
//        super.setBackgroundColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.connect_ble_foreground))
//    }

}