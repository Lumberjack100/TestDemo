package com.shmedo.mcloudapp.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.shmedo.mcloudapp.R

class ProgressMaterialButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialButtonStyle
) : MaterialButton(context, attrs, defStyleAttr) {

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = context.resources.getDimension(R.dimen.progress_stroke_width)
        color = ContextCompat.getColor(context, R.color.colorPrimary)
    }

    private val progressPath = Path()
    private var progress = 0f
    private var isAnimating = false

    // 创建进度动画
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 6000
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener { animation ->
            progress = animation.animatedValue as Float
            invalidate()
        }
    }

    fun startProgressAnimation() {
        isAnimating = true
        animator.start()
    }

    fun stopProgressAnimation() {
        isAnimating = false
        animator.cancel()
        progress = 0f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isAnimating) {
            progressPath.reset()
            val width = width.toFloat()
            val height = height.toFloat()

            when {
                progress < 0.25f -> {
                    progressPath.moveTo(0f, height)
                    progressPath.lineTo(0f, height - height * progress * 4)
                }

                progress < 0.5f -> {
                    progressPath.moveTo(0f, 0f)
                    progressPath.lineTo(width * (progress - 0.25f) * 4, 0f)
                }

                progress < 0.75f -> {
                    progressPath.moveTo(width, 0f)
                    progressPath.lineTo(width, height * (progress - 0.5f) * 4)
                }

                else -> {
                    progressPath.moveTo(width, height)
                    progressPath.lineTo(width - width * (progress - 0.75f) * 4, height)
                }
            }

            canvas.drawPath(progressPath, progressPaint)
        }
    }
}