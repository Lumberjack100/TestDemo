package com.shmedo.mcloudapp.device.ui.mr702.widget.tableview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.shmedo.mcloudapp.databinding.CommunicationDataTableviewCellLayoutBinding
import com.shmedo.mcloudapp.databinding.CommunicationDataTableviewColumnHeaderLayoutBinding
import com.shmedo.mcloudapp.databinding.CommunicationDataTableviewCornerLayoutBinding
import com.shmedo.mcloudapp.databinding.CommunicationDataTableviewRowHeaderLayoutBinding
import com.shmedo.mcloudapp.device.ui.mr702.widget.tableview.holder.CommunicationDataCellViewHolder
import com.shmedo.mcloudapp.device.ui.mr702.widget.tableview.holder.CommunicationDataColumnHeaderViewHolder
import com.shmedo.mcloudapp.device.ui.mr702.widget.tableview.holder.CommunicationDataRowHeaderViewHolder
import com.shmedo.mcloudapp.device.ui.mr702.widget.tableview.model.CommunicationDataCellModel

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/11 <br/>
 * 描述：     TODO
 */
class CommunicationDataTableAdapter :
    AbstractTableAdapter<CommunicationDataCellModel, CommunicationDataCellModel, CommunicationDataCellModel>() {


    override fun getCellItemViewType(position: Int): Int {
        return 0
    }

    override fun onCreateCellViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        val binding = CommunicationDataTableviewCellLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommunicationDataCellViewHolder(binding.root)
    }

    override fun onBindCellViewHolder(
        holder: AbstractViewHolder,
        cellItemModel: CommunicationDataCellModel?,
        columnPosition: Int,
        rowPosition: Int
    ) {
        if (holder is CommunicationDataCellViewHolder) {
            holder.setCellModel(cellItemModel!!, columnPosition)
        }
    }

    override fun getColumnHeaderItemViewType(position: Int): Int {
        // The unique ID for this type of column header item
        // If you have different items for Cell View by X (Column) position,
        // then you should fill this method to be able create different
        // type of CellViewHolder on "onCreateCellViewHolder"
        return 0
    }

    override fun onCreateColumnHeaderViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractViewHolder {
        val binding = CommunicationDataTableviewColumnHeaderLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommunicationDataColumnHeaderViewHolder(binding.root)
    }

    override fun onBindColumnHeaderViewHolder(
        holder: AbstractViewHolder,
        columnHeaderItemModel: CommunicationDataCellModel?,
        columnPosition: Int
    ) {
        if (holder is CommunicationDataColumnHeaderViewHolder) {
            holder.setCellModel(columnHeaderItemModel!!, columnPosition)
        }
    }


    override fun getRowHeaderItemViewType(position: Int): Int {
        // The unique ID for this type of row header item
        // If you have different items for Row Header View by Y (Row) position,
        // then you should fill this method to be able create different
        // type of RowHeaderViewHolder on "onCreateRowHeaderViewHolder"
        return 0
    }

    override fun onCreateRowHeaderViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {
        val binding = CommunicationDataTableviewRowHeaderLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommunicationDataRowHeaderViewHolder(binding.root)
    }

    override fun onBindRowHeaderViewHolder(
        holder: AbstractViewHolder,
        rowHeaderItemModel: CommunicationDataCellModel?,
        rowPosition: Int
    ) {
        if (holder is CommunicationDataRowHeaderViewHolder) {
            holder.setCellModel(rowHeaderItemModel!!)
        }
    }

    override fun onCreateCornerView(parent: ViewGroup): View {
        val binding = CommunicationDataTableviewCornerLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.cornerTextView.text = "数据中心"

        return binding.root
    }

}