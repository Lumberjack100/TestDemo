package com.shmedo.core.model

import androidx.databinding.BaseObservable

/**
 * 创建者：gonghe
 * 创建时间：2025/1/13
 * 描述：内置指令实体类
 */
data class BuiltinCommandInfo(
    val id: String,
    val category: String,                                    // 指令分类
    val name: String,                                        // 指令名称
    val content: String,                                     // 指令内容
    val remark: String = "",                                 // 备注
    val createTime: Long = System.currentTimeMillis(),  // 创建时间
    val updateTime: Long = System.currentTimeMillis(),   // 更新时间
    var checked: Boolean = false,
    var cbVisibility: Boolean = false
) : BaseObservable(){
    fun refreshChecked(value: Boolean) {
        if (checked != value) {
            this.checked = value
            notifyChange()
        }
    }

    fun refreshCbVisibility(value: Boolean) {
        if (cbVisibility != value) {
            this.cbVisibility = value
            notifyChange()
        }
    }
}