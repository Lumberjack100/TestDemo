package com.shmedo.mcloudapp.ui.dialog

import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import com.blankj.utilcode.util.KeyboardUtils
import com.hjq.toast.Toaster
import com.lxj.xpopup.core.CenterPopupView
import com.shmedo.core.model.BuiltinCommandInfo
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.DialogBuiltinCommandEditBinding
import java.util.UUID

/**
 * 创建者：gonghe
 * 创建时间：2025/1/13
 * 描述：内置指令编辑弹框
 */
class BuiltinCommandEditDialog(
    context: Context,
    private val command: BuiltinCommandInfo?,
    private val onSave: (BuiltinCommandInfo) -> Unit
) : CenterPopupView(context) {
    private lateinit var binding: DialogBuiltinCommandEditBinding

    // 指定泛型类型为 String
    val cmdName = ObservableField<String>("")
    val cmdContent = ObservableField<String>("")

    // 指令分类选项
    private val categoryList = listOf(
        "通用指令",
        "专用指令"
    )

    override fun getImplLayoutId(): Int = R.layout.dialog_builtin_command_edit

    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind(popupImplView)!!
        binding.cmdName = cmdName
        binding.cmdContent = cmdContent
        initData()
        setClickListeners()
    }

    private fun initData() {
        if (command != null) {
            // 编辑模式，填充现有数据
            cmdName.set(command.name)
            cmdContent.set(command.content)
            binding.popupHead.tvTitle.text = "编辑指令"
        } else {
            // 新增模式
            binding.popupHead.tvTitle.text = "添加指令"
        }

        binding.etCommandName.apply {
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    setSelection(text?.length ?: 0)
                }
            }
        }
    }

    private fun setClickListeners() {
        binding.popupHead.ivClose.setOnClickListener {
            KeyboardUtils.hideSoftInput(binding.root)
            dismiss()
        }
        binding.tvCancel.setOnClickListener {
            KeyboardUtils.hideSoftInput(binding.root)
            dismiss()
        }
        binding.tvConfirm.setOnClickListener {
            KeyboardUtils.hideSoftInput(binding.root)
            saveCommand()
        }
    }

    private fun saveCommand() {
        val name = cmdName.get().toString()
        val content = cmdContent.get().toString()

        // 输入验证
        if (name.isEmpty()) {
            Toaster.show("请输入指令名称")
            return
        }

        if (content.isEmpty()) {
            Toaster.show("请输入指令内容")
            return
        }

        // 创建或更新指令实体
        val editedCommand = command?.copy(
            name = name,
            content = content
        )
            ?: // 新增模式
            BuiltinCommandInfo(
                id = UUID.randomUUID().toString(),
                name = name,
                content = content,
                remark = "",
                category = "通用指令",
                createTime = System.currentTimeMillis(),
                updateTime = System.currentTimeMillis()
            )

        // 回调保存
        onSave(editedCommand)
        dismiss()
    }
} 