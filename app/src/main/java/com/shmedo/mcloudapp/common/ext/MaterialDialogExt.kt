package com.shmedo.mcloudapp.common.ext

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.blankj.utilcode.util.ColorUtils
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.utils.SettingUtil


/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/12/13 <br/>
 * 描述：     TODO
 */


/**
 * @param message 显示对话框的内容 必填项
 * @param title 显示对话框的标题 默认 温馨提示
 * @param positiveButtonText 确定按钮文字 默认确定
 * @param positiveAction 点击确定按钮触发的方法 默认空方法
 * @param negativeButtonText 取消按钮文字 默认空 不为空时显示该按钮
 * @param negativeAction 点击取消按钮触发的方法 默认空方法
 *
 */
fun AppCompatActivity.showMessage(
    message: String,
    title: String = "温馨提示",
    positiveButtonText: String = "确定",
    positiveAction: () -> Unit = {},
    negativeButtonText: String = "",
    negativeAction: () -> Unit = {}
) {
    MaterialDialog(this)
        .cancelable(true)
        .lifecycleOwner(this)
        .show {
            title(text = title)
            message(text = message)
            positiveButton(text = positiveButtonText) {
                positiveAction.invoke()
            }
            if (negativeButtonText.isNotEmpty()) {
                negativeButton(text = negativeButtonText) {
                    negativeAction.invoke()
                }
            }
            getActionButton(WhichButton.POSITIVE).updateTextColor(SettingUtil.getColor(this@showMessage))
            getActionButton(WhichButton.NEGATIVE).updateTextColor(ColorUtils.getColor(R.color.sub_title_text_color))
        }
}

/**
 * @param message 显示对话框的内容 必填项
 * @param title 显示对话框的标题 默认 温馨提示
 * @param positiveButtonText 确定按钮文字 默认确定
 * @param positiveAction 点击确定按钮触发的方法 默认空方法
 * @param negativeButtonText 取消按钮文字 默认空 不为空时显示该按钮
 * @param negativeAction 点击取消按钮触发的方法 默认空方法
 */
fun Fragment.showMessage(
    message: String,
    title: String = "温馨提示",
    positiveButtonText: String = "确定",
    positiveAction: () -> Unit = {},
    negativeButtonText: String = "",
    negativeAction: () -> Unit = {}
) {
    activity?.let {
        MaterialDialog(it)
            .cancelable(false)
            .lifecycleOwner(viewLifecycleOwner)
            .show {
                title(text = title)
                message(text = message)
                positiveButton(text = positiveButtonText) {
                    positiveAction.invoke()
                }
                if (negativeButtonText.isNotEmpty()) {
                    negativeButton(text = negativeButtonText) {
                        negativeAction.invoke()
                    }
                }
                getActionButton(WhichButton.POSITIVE).updateTextColor(SettingUtil.getColor(it))
                getActionButton(WhichButton.NEGATIVE).updateTextColor(ColorUtils.getColor(R.color.sub_title_text_color))
            }
    }
}

/**
 * 带确定按钮的单选对话框
 */
fun AppCompatActivity.showItemsSingleChoiceDialog(
    title: String = "温馨提示",
    positiveButtonText: String = "确定",
    items: List<String>,
    positiveAction: (String, Int) -> Unit,
    selection: Int
) {
    MaterialDialog(this)
        .lifecycleOwner(this)
        .show {
            title(text = title)
            listItemsSingleChoice(
                items = items,
                initialSelection = selection
            ) { _, index, text ->
                positiveAction.invoke("$text", index)
            }
            positiveButton(text = positiveButtonText)
        }

}

/**
 * 带确定按钮的单选对话框
 */
fun Fragment.showItemsSingleChoiceDialog(
    title: String = "温馨提示",
    positiveButtonText: String = "确定",
    items: List<String>,
    positiveAction: (String, Int) -> Unit,
    selection: Int
) {
    activity?.let {
        MaterialDialog(it)
            .lifecycleOwner(viewLifecycleOwner)
            .show {
                title(text = title)
                listItemsSingleChoice(
                    items = items,
                    initialSelection = selection
                ) { _, index, text ->
                    positiveAction.invoke("$text", index)
                }
                positiveButton(text = positiveButtonText)
            }
    }
}

/**
 * 带确定按钮的多选对话框
 */
fun Fragment.showItemsMultiChoiceDialog(
    title: String = "温馨提示",
    positiveButtonText: String = "确定",
    items: List<String>,
    positiveAction: (List<CharSequence>, IntArray) -> Unit,
    selection: IntArray
) {
    activity?.let {
        MaterialDialog(it)
            .lifecycleOwner(viewLifecycleOwner)
            .show {
                title(text = title)
                listItemsMultiChoice(
                    items = items,
                    initialSelection = selection
                ) { _, indices, selectedItems ->
                    positiveAction.invoke(selectedItems, indices)
                }
                positiveButton(text = positiveButtonText)
            }
    }
}