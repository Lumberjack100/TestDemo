package com.shmedo.mcloudapp.ext

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.blankj.utilcode.util.ColorUtils
import com.kongzue.dialogx.dialogs.MessageDialog
import com.lxj.xpopup.XPopup
import com.shmedo.lib.device.base.iot_cmd.enums.AdmeModuleErrorType
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.common.fragment.BaseFragment
import com.shmedo.mcloudapp.device.ui.hac.fragment.HacErrorProtectionTip
import com.shmedo.mcloudapp.utils.SettingUtil


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


/**
 * 提示对话框
 */
fun AppCompatActivity.showMessageDialog(
    message: String,
    title: String = "提示",
    positiveButtonText: String = "我已知晓"
) {
    MessageDialog.show(
        title,
        message,
        positiveButtonText
    )
}

/**
 * 提示对话框
 */
fun Fragment.showMessageDialog(
    message: String,
    title: String = "提示",
    positiveButtonText: String = "我已知晓"
) {
    MessageDialog.show(
        title,
        message,
        positiveButtonText
    )
}

fun BaseFragment.showAdmeErrorProtectionDialog(abndiasis: String) {
    val errorMsg = getAdmeErrorMsg(abndiasis)
    if (errorMsg.isEmpty()) {
        return
    }

    val popupView = HacErrorProtectionTip(requireContext())
    popupView.setData(errorMsg)
    XPopup.Builder(context)
        .dismissOnBackPressed(false) // 按返回键是否关闭弹窗，默认为true
        .dismissOnTouchOutside(false)// 点击外部是否关闭弹窗，默认为true
        .enableDrag(false)
        .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
        .asCustom(popupView)
        .show()
}

fun BaseFragment.getAdmeErrorMsg(abndiasis: String, delimiters: String = "\n"): String {
    if (abndiasis.isEmpty()) {
        return ""
    }

    //列出异常原因
    val stringBuilder = StringBuilder()
    // | 分割
    abndiasis.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
        .forEach { code ->
            val errorType = AdmeModuleErrorType.valueByCode(code)
            stringBuilder.append(errorType.description)
            stringBuilder.append(delimiters)
        }
    //移除最后一个分号
    if (stringBuilder.isNotEmpty()) {
        stringBuilder.deleteCharAt(stringBuilder.length - 1)
    }

    if (stringBuilder.toString().contains(AdmeModuleErrorType.EMPTY_ERROR.description)) {
        return ""
    }

    if (stringBuilder.toString().contains(AdmeModuleErrorType.UNKNOWN_ERROR.description)) {
        stringBuilder.clear()
        stringBuilder.append("异常代码: $abndiasis")
    }

    return stringBuilder.toString()
}
