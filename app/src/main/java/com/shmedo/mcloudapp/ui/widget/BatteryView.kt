package com.shmedo.mcloudapp.ui.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.shmedo.mcloudapp.R

/**
 * @Author      : gonghe
 * @Email       : xxxxx@qq.com
 * @Date        : on 2023-11-13 16:02.
 * @Description :描述
 */

class BatteryView : View {
    private var orientation = 0
    private var mPower = 100
    private var mColor = 0
    private var mBorderColor = 0
    private var width = 0
    private var height = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.BatteryView)
        orientation = typedArray.getInt(R.styleable.BatteryView_batteryOrientation, 0)
        mColor = typedArray.getColor(R.styleable.BatteryView_batteryColor, -0x1)
        mBorderColor =
            typedArray.getColor(R.styleable.BatteryView_borderColor, Color.parseColor("#DCDCDC"))
        mPower = typedArray.getInt(R.styleable.BatteryView_batteryPower, 100)
        width = measuredWidth
        height = measuredHeight

        /**
         * recycle() :官方的解释是：回收TypedArray，以便后面重用。在调用这个函数后，你就不能再使用这个TypedArray。
         * 在TypedArray后调用recycle主要是为了缓存。当recycle被调用后，这就说明这个对象从现在可以被重用了。
         * TypedArray 内部持有部分数组，它们缓存在Resources类中的静态字段中，这样就不用每次使用前都需要分配内存。
         */
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //对View上的內容进行测量后得到的View內容占据的宽度
        width = measuredWidth
        //对View上的內容进行测量后得到的View內容占据的高度
        height = measuredHeight
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //判断电池方向    horizontal: 0   vertical: 1
        if (orientation == 0) {
            drawHorizontalBattery(canvas)
        } else {
            drawVerticalBattery(canvas)
        }
    }

    /**
     * 绘制水平电池
     *
     * @param canvas
     */
    private fun drawHorizontalBattery(canvas: Canvas) {
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        val strokeWidth = width / 20f
        paint.strokeWidth = strokeWidth
        val r1 = RectF(
            strokeWidth / 2,
            strokeWidth / 2,
            width - strokeWidth - strokeWidth / 2,
            height - strokeWidth / 2
        )
        //设置外边框颜色
        paint.setColor(mBorderColor)
        canvas.drawRect(r1, paint)

        paint.strokeWidth = 0f
        paint.style = Paint.Style.FILL
        //画电池内矩形电量
        val offset = (width - strokeWidth * 2) * mPower / 100f
        val r2 = RectF(strokeWidth, strokeWidth, offset, height - strokeWidth)
        //根据电池电量决定电池内矩形电量颜色
        if (mPower < 30) {
            paint.setColor(Color.RED)
        }
        if (mPower >= 30) {
            paint.setColor(mColor)
        }
        canvas.drawRect(r2, paint)

        //画电池头
        val r3 = RectF(width - strokeWidth, height * 0.25f, width.toFloat(), height * 0.75f)
        //设置电池头颜色
        paint.setColor(mBorderColor)
        canvas.drawRect(r3, paint)
    }

    /**
     * 绘制垂直电池
     *
     * @param canvas
     */
    private fun drawVerticalBattery(canvas: Canvas) {
        val paint = Paint()
        paint.setColor(mColor)
        paint.style = Paint.Style.STROKE
        val strokeWidth = height / 20.0f
        paint.strokeWidth = strokeWidth
        val headHeight = (strokeWidth + 0.5f).toInt()
        val rect = RectF(
            strokeWidth / 2,
            headHeight + strokeWidth / 2,
            width - strokeWidth / 2,
            height - strokeWidth / 2
        )
        canvas.drawRect(rect, paint)

        paint.strokeWidth = 0f
        val topOffset = (height - headHeight - strokeWidth) * (100 - mPower) / 100.0f
        val rect2 = RectF(
            strokeWidth,
            headHeight + strokeWidth + topOffset,
            width - strokeWidth,
            height - strokeWidth
        )
        paint.style = Paint.Style.FILL
        canvas.drawRect(rect2, paint)

        val headRect = RectF(width / 4.0f, 0f, width * 0.75f, headHeight.toFloat())
        canvas.drawRect(headRect, paint)
    }

    /**
     * 设置电池电量
     *
     * @param power
     */
    fun setPower(power: Int) {
        mPower = power
        if (mPower < 0) {
            mPower = 100
        }
        invalidate() //刷新VIEW
    }

    /**
     * 设置电池颜色
     *
     * @param color
     */
    fun setColor(color: Int) {
        mColor = color
        invalidate()
    }

    /**
     * 设置电池边框颜色
     *
     * @param color
     */
    fun setBorderColor(color: Int) {
        mBorderColor = color
        invalidate()
    }

    /**
     * 获取电池电量
     *
     * @return
     */
    fun getPower(): Int {
        return mPower
    }
}