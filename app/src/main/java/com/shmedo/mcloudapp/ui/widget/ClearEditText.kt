package com.shmedo.mcloudapp.ui.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.shmedo.mcloudapp.R

/**
 * 项目名：  eMeasApp
 * 包名：    com.shmedo.emeas.framwork
 * 文件名:   ClearEditText
 * 创建者:   dpc
 * 创建时间:  2017/9/6 15:12
 */

class ClearEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr), View.OnTouchListener,
    View.OnFocusChangeListener, TextWatcher {

    private lateinit var mClearTextIcon: Drawable
    private var mOnFocusChangeListener: OnFocusChangeListener? = null

    init {
        init(context)
        isFocusable = true
        isFocusableInTouchMode = true
    }

    private fun init(context: Context) {
        mClearTextIcon = compoundDrawables[2] ?: run {
            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_clear)!!
            val wrapDrawable = DrawableCompat.wrap(drawable)
            DrawableCompat.setTint(wrapDrawable, currentHintTextColor)
            wrapDrawable
        }

        mClearTextIcon.setBounds(
            0,
            0,
            mClearTextIcon.intrinsicHeight,
            mClearTextIcon.intrinsicHeight
        )
        setClearIconVisible(false)
        setOnTouchListener(this)
        onFocusChangeListener = this
        addTextChangedListener(this)
    }

    fun setOutSideFocusChangeListener(l: OnFocusChangeListener?) {
        mOnFocusChangeListener = l
    }

    override fun onFocusChange(view: View, hasFocus: Boolean) {
        if (hasFocus) {
            setClearIconVisible(text?.isNotEmpty() == true)
            post {
                text?.let {
                    setSelection(it.length)
                }
            }
        } else {
            setClearIconVisible(false)
        }
        mOnFocusChangeListener?.onFocusChange(view, hasFocus)
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        val x = motionEvent.x.toInt()
        if (mClearTextIcon.isVisible && x > width - paddingRight - mClearTextIcon.intrinsicWidth) {
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                error = null
                setText("")
            }
            return true
        }
        return super.onTouchEvent(motionEvent)
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        if (isFocused) {
            setClearIconVisible(text?.isNotEmpty() == true)
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // Not used
    }

    override fun afterTextChanged(s: Editable?) {
        // Not used
    }

    private fun setClearIconVisible(visible: Boolean) {
        mClearTextIcon.setVisible(visible, false)
        val compoundDrawables = compoundDrawables
        setCompoundDrawables(
            compoundDrawables[0],
            compoundDrawables[1],
            if (visible) mClearTextIcon else null,
            compoundDrawables[3]
        )
    }
}