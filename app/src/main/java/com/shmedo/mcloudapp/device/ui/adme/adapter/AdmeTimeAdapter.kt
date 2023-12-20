package com.shmedo.mcloudapp.device.ui.adme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.device.model.AdmeTimeItem

/**
 * 供选择、编辑的图片、视频列表
 * @author：luck
 * @date：2016-7-27 23:02
 * @describe：GridImageAdapter
 */
class AdmeTimeAdapter(context: Context, result: List<AdmeTimeItem>) :
    RecyclerView.Adapter<AdmeTimeAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater
    val data = ArrayList<AdmeTimeItem>()
    var itemMax = 8

    init {
        mInflater = LayoutInflater.from(context)
        data.addAll(result)
    }

    /**
     * 删除
     */
    fun delete(position: Int) {
        try {
            if (position != RecyclerView.NO_POSITION && data.size > position) {
                data.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, data.size)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mImg: ImageView
        val mIvDel: ImageView
        val tvTime: TextView

        init {
            mImg = view.findViewById(R.id.iv_add_time)
            mIvDel = view.findViewById(R.id.iv_del)
            tvTime = view.findViewById(R.id.tv_time)
        }
    }

    override fun getItemCount(): Int {
        return if (data.size < itemMax) {
            data.size + 1
        } else {
            data.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isShowAddItem(position)) {
            TYPE_ADD
        } else {
            TYPE_ITEM
        }
    }

    /**
     * 创建ViewHolder
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.item_adme_execute_time, viewGroup, false)
        return ViewHolder(view)
    }

    private fun isShowAddItem(position: Int): Boolean {
        return position == data.size
    }

    /**
     * 设置值
     */
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        //少于MaxSize张，显示继续添加的图标
        if (getItemViewType(position) == TYPE_ADD) {
            viewHolder.mImg.visibility = View.VISIBLE
            viewHolder.mIvDel.visibility = View.GONE
            viewHolder.tvTime.visibility = View.GONE
            viewHolder.mImg.setOnClickListener {
                mItemClickListener?.addItem()
            }

        } else {
            viewHolder.mImg.visibility = View.GONE
            viewHolder.mIvDel.visibility = View.VISIBLE
            viewHolder.tvTime.visibility = View.VISIBLE
            viewHolder.mIvDel.setOnClickListener {
                delete(viewHolder.layoutPosition)
            }
            viewHolder.tvTime.text = data[position].time
            //itemView 的点击事件
            mItemClickListener?.let {
                viewHolder.itemView.setOnClickListener { v: View? ->
                    it.onItemClick(v, viewHolder.layoutPosition)
                }
            }
            mItemLongClickListener?.let {
                viewHolder.itemView.setOnLongClickListener { v: View? ->
                    it.onItemLongClick(viewHolder, viewHolder.layoutPosition, v)
                    true
                }
            }
        }
    }

    private var mItemClickListener: OnItemClickListener? = null
    private var mItemLongClickListener: OnItemLongClickListener? = null

    fun setOnItemClickListener(l: OnItemClickListener?) {
        mItemClickListener = l
    }

    fun setItemLongClickListener(l: OnItemLongClickListener?) {
        mItemLongClickListener = l
    }

    interface OnItemClickListener {
        fun onItemClick(v: View?, position: Int)
        fun addItem()
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(holder: RecyclerView.ViewHolder?, position: Int, v: View?)
    }

    companion object {
        const val TYPE_ADD = 1
        const val TYPE_ITEM = 2
    }
}
