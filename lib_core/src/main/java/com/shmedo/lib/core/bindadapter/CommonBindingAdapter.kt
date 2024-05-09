/*
 * Copyright 2018-present KunMinX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shmedo.lib.core.bindadapter;

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.util.Pair
import android.util.SparseIntArray
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.BindingAdapter
import com.blankj.utilcode.util.ClickUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.shmedo.lib.core.base.model.LogLevel


/**
 * Create by KunMinX at 19/9/18
 */
object CommonBindingAdapter {
    private val mColors = SparseIntArray()

    init {
        mColors.put(LogLevel.fromPriority(Log.DEBUG), -0xff6322)
        mColors.put(LogLevel.fromPriority(Log.VERBOSE), -0x474faa)
        mColors.put(LogLevel.fromPriority(Log.INFO), Color.BLACK)
        mColors.put(LogLevel.fromPriority(Log.WARN), -0x2886da)
        mColors.put(LogLevel.fromPriority(Log.ERROR), Color.RED)
    }

    private val characterFilter = InputFilter { source, start, end, _, _, _ ->
        for (i in start until end) {
            if (!"_0123456789qwertzuiopasdfghjklyxcvbnmQWERTZUIOPASDFGHJKLYXCVBNM".contains(source[i].toString())) {
                return@InputFilter ""
            }
        }
        null
    }

    @JvmStatic
    @BindingAdapter(value = ["imageUrl", "placeHolder"], requireAll = false)
    fun imageUrl(view: ImageView, url: String?, placeHolder: Drawable?) {
        Glide.with(view.context).load(url).placeholder(placeHolder).into(view)
    }

    @JvmStatic
    @BindingAdapter(value = ["circleImageUrl", "placeHolder"], requireAll = false)
    fun circleImageUrl(view: ImageView, url: String?, placeHolder: Drawable?) {
        Glide.with(view.context)
            .load(url)
            .placeholder(placeHolder)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .transition(DrawableTransitionOptions.withCrossFade(500))
            .into(view)
    }

    @JvmStatic
    @BindingAdapter(value = ["imageResId"], requireAll = false)
    fun imageResId(view: ImageView, resId: Int) {
        view.setImageResource(resId)
    }

    @JvmStatic
    @BindingAdapter(value = ["bgResId"], requireAll = false)
    fun bgResId(view: View, resId: Int) {
        view.setBackgroundResource(resId)
    }

    @JvmStatic
    @BindingAdapter(value = ["visible"], requireAll = false)
    fun visible(view: View, visible: Boolean) {
        if (visible && view.visibility == View.GONE) {
            view.visibility = View.VISIBLE
        } else if (!visible && view.visibility == View.VISIBLE) {
            view.visibility = View.GONE
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["invisible"], requireAll = false)
    fun invisible(view: View, visible: Boolean) {
        if (visible && view.visibility == View.INVISIBLE) {
            view.visibility = View.VISIBLE
        } else if (!visible && view.visibility == View.VISIBLE) {
            view.visibility = View.INVISIBLE
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["enabled"], requireAll = false)
    fun enabled(view: View, enabled: Boolean) {
        view.isEnabled = enabled
    }

    @JvmStatic
    @BindingAdapter(value = ["size"], requireAll = false)
    fun size(view: View, size: Pair<Int, Int>) {
        val params = view.layoutParams as CoordinatorLayout.LayoutParams
        params.width = size.first
        params.height = size.second
        view.layoutParams = params
    }

    @JvmStatic
    @BindingAdapter(value = ["transX"], requireAll = false)
    fun translationX(view: View, translationX: Float) {
        view.translationX = translationX
    }

    @JvmStatic
    @BindingAdapter(value = ["transY"], requireAll = false)
    fun translationY(view: View, translationY: Float) {
        view.translationY = translationY
    }

    @JvmStatic
    @BindingAdapter(value = ["x"], requireAll = false)
    fun x(view: View, x: Float) {
        view.x = x
    }

    @JvmStatic
    @BindingAdapter(value = ["y"], requireAll = false)
    fun y(view: View, y: Float) {
        view.y = y
    }

    @JvmStatic
    @BindingAdapter(value = ["alpha"], requireAll = false)
    fun alpha(view: View, alpha: Float) {
        view.alpha = alpha
    }

    @JvmStatic
    @BindingAdapter(value = ["logLevelTag"], requireAll = false)
    fun setLogTextTag(textView: TextView, level: Int) {
        textView.setTextColor(mColors[LogLevel.fromPriority(level)])
        when (level) {
            Log.DEBUG -> textView.text = "D"
            Log.VERBOSE -> textView.text = "V"
            Log.INFO -> textView.text = "I"
            Log.WARN -> textView.text = "W"
            Log.ERROR -> textView.text = "E"
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["logLevelColor"], requireAll = false)
    fun setLogTextColor(textView: TextView, level: Int) {
        textView.setTextColor(mColors[LogLevel.fromPriority(level)])
    }

    @JvmStatic
    @BindingAdapter(value = ["textColor"], requireAll = false)
    fun setTextColor(textView: TextView, textColorRes: Int) {
        textView.setTextColor(textView.context.getColor(textColorRes))
    }

    @JvmStatic
    @BindingAdapter(value = ["textStyle"], requireAll = false)
    fun setTextStyle(textView: TextView, isBold: Boolean) {
        textView.setTypeface(null, if (isBold) Typeface.BOLD else Typeface.NORMAL)
    }

    @JvmStatic
    @BindingAdapter(value = ["selected"], requireAll = false)
    fun selected(view: View, select: Boolean) {
        view.isSelected = select
    }

    @JvmStatic
    @BindingAdapter(
        value = ["lengthFilter", "inputTypeFilter"],
        requireAll = false
    )
    fun setLengthFilter(
        editText: EditText,
        length: Int? = null,
        inputTypeFilter: String? = ""
    ) {
        //如果length为null，则使用-1作为默认值
        val lengthFilter = InputFilter.LengthFilter(length ?: 20)
        if (inputTypeFilter.isNullOrEmpty()) {
            editText.filters = arrayOf(lengthFilter)
            return
        }

        when (inputTypeFilter) {
            "number" -> {
                editText.filters = arrayOf(lengthFilter)
                // 允许输入整数
                editText.inputType = InputType.TYPE_CLASS_NUMBER
            }

            "numberDecimal" -> {
                editText.filters = arrayOf(lengthFilter)
                // 允许输入带符号的小数
                editText.inputType =
                    InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL
            }

            "character" -> editText.filters = arrayOf(lengthFilter, characterFilter)

            else -> { // 其他自定义的输入过滤
                val inputFilter = InputFilter { source, start, end, _, _, _ ->
                    for (i in start until end) {
                        if (!inputTypeFilter.contains(source[i].toString())) {
                            return@InputFilter ""
                        }
                    }
                    null
                }
                editText.filters = arrayOf(lengthFilter, inputFilter)
            }
        }
    }

    /**
     * 防止重复点击
     */
    @JvmStatic
    @BindingAdapter(value = ["onClickWithDebouncing"], requireAll = false)
    fun onClickWithDebouncing(view: View?, clickListener: View.OnClickListener?) {
        ClickUtils.applySingleDebouncing(view, clickListener)
    }
}
