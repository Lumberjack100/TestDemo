package com.shmedo.mcloudapp.device.ui.mr702.fragment

import android.content.Context
import android.graphics.Color
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.lxj.xpopup.impl.PartShadowPopupView
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.widget.recyclerview.MyGridSpacingItemDecoration
import com.shmedo.mcloudapp.databinding.CustomMr702InterfaceSelectionPartShadowPopupBinding
import com.shmedo.mcloudapp.databinding.ItemMr702InterfaceBinding

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/10/19 <br/>
 * 描述：     TODO
 */
class MR702InterfaceSelectionPartShadowPopupView(context: Context) : PartShadowPopupView(context) {
    private lateinit var binding: CustomMr702InterfaceSelectionPartShadowPopupBinding
    private lateinit var data: List<String>

    private var selectedIndex: Int = 0
    private lateinit var selectListener: OnSelectListener
    private val activeColor: Int = ColorUtils.getColor(R.color.white)
    private val normalColor: Int = Color.parseColor("#65A2CD")

    fun setData(
        list: List<String>,
        selectedIndex: Int = 0
    ): MR702InterfaceSelectionPartShadowPopupView {
        this.data = list.toList()
        this.selectedIndex = selectedIndex
        return this
    }

    fun setSelectListener(selectListener: OnSelectListener): MR702InterfaceSelectionPartShadowPopupView {
        this.selectListener = selectListener
        return this
    }

    override fun getImplLayoutId(): Int {
        return R.layout.custom_mr702_interface_selection_part_shadow_popup
    }

    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind(popupImplView)!!
        initAdapter()
        binding.tvCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun initAdapter() {
        binding.rv.setup { rv ->
            rv.addItemDecoration(
                MyGridSpacingItemDecoration(
                    4,
                    ConvertUtils.dp2px(8f),
                    false
                )
            )
            addType<String>(R.layout.item_mr702_interface)
            onBind {
                if (selectedIndex == modelPosition) {
                    getBinding<ItemMr702InterfaceBinding>().item.setBackgroundResource(R.drawable.bg_mr702_interface_tab_checked)
                    getBinding<ItemMr702InterfaceBinding>().tabText.setTextColor(activeColor)
                } else {
                    getBinding<ItemMr702InterfaceBinding>().item.setBackgroundResource(R.drawable.bg_mr702_interface_tab_normal)
                    getBinding<ItemMr702InterfaceBinding>().tabText.setTextColor(normalColor)
                }
            }
            R.id.item.onClick {
                val name = getModel<String>()
                selectListener.onSelect(name)
                dismiss()
            }
        }
        if (data.isNotEmpty())
            binding.rv.models = data
    }

    interface OnSelectListener {
        fun onSelect(name: String)
    }
}