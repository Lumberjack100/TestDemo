package com.shmedo.mcloudapp.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.widget.NestedScrollView
import com.amap.api.maps.TextureMapView
import com.shmedo.mcloudapp.R


/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/8 <br/>
 * 描述：     解决嵌套地图滑动冲突
 */

class CustomNestedScrollView : NestedScrollView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (isTouchWithinMapView(ev))
            false // 不拦截事件，让子控件处理
        else super.onInterceptTouchEvent(ev)
    }


    /**
     * 检查触摸点是否在 TextureMapView 的范围内
     */
    private fun isTouchWithinMapView(ev: MotionEvent): Boolean {
        val textureMapView = findViewById<TextureMapView>(R.id.textureMapView) ?: return false

        val location = IntArray(2)
        textureMapView.getLocationOnScreen(location)
        val left = location[0]
        val top = location[1]
        val right: Int = left + textureMapView.width
        val bottom: Int = top + textureMapView.height
        val x = ev.rawX // 使用 getRawX 和 getRawY 而不是 getX 和 getY 来获取触摸事件的屏幕坐标
        val y = ev.rawY
        return x >= left && x <= right && y >= top && y <= bottom
    }

}

