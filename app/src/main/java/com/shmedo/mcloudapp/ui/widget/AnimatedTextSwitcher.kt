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

    private var _textSize: Float =
        context.resources.getDimension(R.dimen.page_content_item_text_size_14)
    private var errorTextColor: Int = ContextCompat.getColor(context, R.color.error_FF4400)
    private var warnTextColor: Int = ContextCompat.getColor(context, R.color.warn_FF9D00)
    private var textGravity: Int = Gravity.CENTER
    private var _maxEms: Int = 7
    private var needBackground: Boolean = true
    private var errorBackground: Int = R.drawable.bg_label_error_corner_1dp
    private var warnBackground: Int = R.drawable.bg_label_warn_corner_1dp
    private var paddingHorizontal: Int = resources.getDimensionPixelSize(R.dimen.dimen_size_7)
    private var paddingVertical: Int = resources.getDimensionPixelSize(R.dimen.dimen_size_2)

    init {
        initAttributes(attrs)
        setFactory { createTextView() }
        setupAnimations()
    }

    private fun initAttributes(attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.AnimatedTextSwitcher).apply {
            try {
                _textSize =
                    getDimension(R.styleable.AnimatedTextSwitcher_defaultTextSize, _textSize)
                errorTextColor =
                    getColor(R.styleable.AnimatedTextSwitcher_errorTextColor, errorTextColor)
                warnTextColor =
                    getColor(R.styleable.AnimatedTextSwitcher_warnTextColor, warnTextColor)
                textGravity = getInt(R.styleable.AnimatedTextSwitcher_textGravity, textGravity)
                _maxEms = getInt(R.styleable.AnimatedTextSwitcher_maxEms, _maxEms)
                needBackground =
                    getBoolean(R.styleable.AnimatedTextSwitcher_needBackground, needBackground)
                errorBackground =
                    getResourceId(R.styleable.AnimatedTextSwitcher_errorBackground, errorBackground)
                warnBackground =
                    getResourceId(R.styleable.AnimatedTextSwitcher_warnBackground, warnBackground)
                paddingHorizontal = getDimensionPixelSize(
                    R.styleable.AnimatedTextSwitcher_paddingHorizontal,
                    paddingHorizontal
                )
                paddingVertical = getDimensionPixelSize(
                    R.styleable.AnimatedTextSwitcher_paddingVertical,
                    paddingVertical
                )
            } finally {
                recycle()
            }
        }
    }

    private fun createTextView() = MaterialTextView(context).apply {
        gravity = textGravity
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
        setEms(_maxEms)
        ellipsize = TextUtils.TruncateAt.END
        maxLines = 1
    }

    private fun setupAnimations() {
        inAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_in_up)
        outAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_out_up)
    }

    private fun setTextStyle(
        isError: Boolean = false,
        textSize: Float = this._textSize,
        needBackground: Boolean = this.needBackground
    ) {
        val color = if (isError) errorTextColor else warnTextColor
        val backgroundRes = if (isError) errorBackground else warnBackground

        (0..1).forEach { index ->
            (getChildAt(index) as? TextView)?.apply {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                setTextColor(color)
                background = if (needBackground) {
                    ContextCompat.getDrawable(context, backgroundRes)
                } else null
            }
        }
    }

    fun setText(text: String, isError: Boolean = false) {
        setTextStyle(isError)
        super.setText(text)
    }

}