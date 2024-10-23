package com.shmedo.mcloudapp.ui.widget

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.animation.AnimationUtils
import android.widget.TextSwitcher
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.textview.MaterialTextView
import com.shmedo.mcloudapp.R


class AnimatedTextSwitcher @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : TextSwitcher(context, attrs) {

    init {
        setFactory {
            MaterialTextView(context).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                gravity = Gravity.CENTER
                setPadding(
                    resources.getDimensionPixelSize(R.dimen.dimen_size_7),
                    resources.getDimensionPixelSize(R.dimen.dimen_size_2),
                    resources.getDimensionPixelSize(R.dimen.dimen_size_7),
                    resources.getDimensionPixelSize(R.dimen.dimen_size_2)
                )
                setEms(7) // 设置宽度为10个字符宽
                ellipsize = TextUtils.TruncateAt.END // 如果文本超出，在末尾显示省略号
                maxLines = 1 // 确保只显示一行
            }
        }

        inAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_in_up)
        outAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_out_up)
    }

    fun setText(text: String, isError: Boolean, needBackground: Boolean = true) {
        (getChildAt(0) as? TextView)?.apply {
            setTextColor(
                ContextCompat.getColor(
                    context,
                    if (isError) R.color.error_FF4400 else R.color.warn_FF9D00
                )
            )
            if (needBackground)
                background = ContextCompat.getDrawable(
                    context,
                    if (isError) R.drawable.bg_label_error_corner_1dp else R.drawable.bg_label_warn_corner_1dp
                )
        }
        (getChildAt(1) as? TextView)?.apply {
            setTextColor(
                ContextCompat.getColor(
                    context,
                    if (isError) R.color.error_FF4400 else R.color.warn_FF9D00
                )
            )
            if (needBackground)
                background = ContextCompat.getDrawable(
                    context,
                    if (isError) R.drawable.bg_label_error_corner_1dp else R.drawable.bg_label_warn_corner_1dp
                )
        }
        super.setText(text)
    }
}