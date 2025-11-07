package com.shmedo.mcloudapp.extensions

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.shmedo.mcloudapp.extensions.InsetsManager.overrideGlobalBottomInsets
import kotlin.math.max

/**
 * Edge-to-edge 辅助工具：
 * - 全局 NavHost systemBars.bottom（可启用/禁用/自定义）
 * - 页面仅叠加 IME delta（避免 systemBars 与 IME 重复）
 * - 覆盖层/贴底视图的上移
 * - 全屏页对全局 bottom 的按需“覆盖”（真正吃掉全局 padding）
 *
 *  For example:
 *
 *  ```
 * // DeviceHomeActivity.kt
 * override fun onCreate(savedInstanceState: Bundle?) {
 *     super.onCreate(savedInstanceState)
 *     setContentView(R.layout.activity_device_home)
 *     val navHost = findViewById<View>(R.id.nav_host_fragment)
 *     InsetsManager.applyGlobalSystemBarsBottom(navHost)
 * }
 *
 * // FullscreenFragment.kt
 * override fun onResume() {
 *     super.onResume()
 *     val navHost = requireActivity().findViewById<View>(R.id.nav_host_fragment)
 *     InsetsManager.overrideGlobalBottomInsets(navHost, InsetsManager.GlobalBottomMode.Disabled)
 * }
 *
 * override fun onPause() {
 *     super.onPause()
 *     val navHost = requireActivity().findViewById<View>(R.id.nav_host_fragment)
 *     InsetsManager.overrideGlobalBottomInsets(navHost, InsetsManager.GlobalBottomMode.Enabled)
 * }
 *
 * // 有输入框的页面：
 * InsetsManager.applyImeDeltaBottom(binding.root, binding.refreshLayout)
 *  ```
 */
object InsetsManager {

    // ==========================
    // 工具与扩展
    // ==========================

    private data class InitialPadding(val left: Int, val top: Int, val right: Int, val bottom: Int)
    private data class InitialMargin(val left: Int, val top: Int, val right: Int, val bottom: Int)

    private fun View.captureInitialPadding() =
        InitialPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)

    private fun View.captureInitialMargin(): InitialMargin {
        val lp = layoutParams
        return if (lp is MarginLayoutParams) {
            InitialMargin(lp.leftMargin, lp.topMargin, lp.rightMargin, lp.bottomMargin)
        } else {
            InitialMargin(0, 0, 0, 0)
        }
    }

    private val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    // 为了“可覆盖”，我们在 View 上保存一次初始 padding 与当前模式
    private const val TAG_INIT_PADDING = -7_000_000     // 任意负数避免资源冲突
    private const val TAG_GLOBAL_MODE = -7_000_001
    private const val TAG_LISTENER_SET = -7_000_002

    private fun View.storeInitialPaddingOnce() {
        if (getTag(TAG_INIT_PADDING) == null) {
            setTag(TAG_INIT_PADDING, captureInitialPadding())
        }
    }

    private fun View.getStoredInitialPadding(): InitialPadding =
        (getTag(TAG_INIT_PADDING) as? InitialPadding) ?: captureInitialPadding()

    // ==========================
    // 一、全局 NavHost systemBars.bottom（可覆盖）
    // ==========================

    sealed class GlobalBottomMode {
        /** 启用全局 bottom（= systemBars.bottom） */
        data object Enabled : GlobalBottomMode()

        /** 禁用全局 bottom（= 0）——全屏页使用 */
        data object Disabled : GlobalBottomMode()

        /** 自定义 bottom 像素（比如给底栏容器） */
        data class CustomPx(val bottomPx: Int) : GlobalBottomMode()
    }

    /**
     * 全局 bottom：在 NavHost（或全局内容根）上一次性应用 systemBars.bottom。
     * 之后可用 [overrideGlobalBottomInsets] 在目的页启用/禁用/自定义该 bottom。
     */
    fun applyGlobalSystemBarsBottom(navHost: View) {
        navHost.storeInitialPaddingOnce()

        // 标记已安装，避免重复装多个 listener
        if (navHost.getTag(TAG_LISTENER_SET) == true) return
        navHost.setTag(TAG_LISTENER_SET, true)

        ViewCompat.setOnApplyWindowInsetsListener(navHost) { v, insets ->
            val init = v.getStoredInitialPadding()
            // 根据当前模式计算 bottom
            val mode = v.getTag(TAG_GLOBAL_MODE) as? GlobalBottomMode ?: GlobalBottomMode.Enabled
            val sys = insets.getInsets(Type.systemBars())
            val bottom = when (mode) {
                is GlobalBottomMode.Enabled -> sys.bottom
                is GlobalBottomMode.Disabled -> 0
                is GlobalBottomMode.CustomPx -> mode.bottomPx
            }
            v.updatePadding(
                left = init.left,
                top = init.top,
                right = init.right,
                bottom = init.bottom + bottom
            )
            insets // 不消费
        }

        ViewCompat.requestApplyInsets(navHost)
    }

    /**
     * 覆盖（切换）全局 bottom 模式：
     * - 全屏页： Disabled
     * - 普通页： Enabled
     * - 底栏容器： CustomPx(系统栏高度)（如果你把 NavHost 的 bottom 转移给底栏）
     */
    fun overrideGlobalBottomInsets(navHost: View, mode: GlobalBottomMode) {
        navHost.setTag(TAG_GLOBAL_MODE, mode)
        // 触发重新计算
        ViewCompat.requestApplyInsets(navHost)
    }

    // ==========================
    // 二、页面仅叠加 IME delta（避免双重填充）
    // ==========================

    enum class ApplyTo { Padding, Margin, Translation }

    /**
     * 在页面内对 [target] 仅叠加 IME 相对 systemBars 的**额外差值**（delta）。
     * 这样全局的 systemBars.bottom 与页面的 ime 不会相加重复。
     * imeDelta = max(ime.bottom - systemBars.bottom, 0)
     * @param root 建议传本页面的根 View（用于监听 insets）
     * @param target 需要跟随键盘上移的容器（如刷新容器、底部按钮栏）
     * @param applyTo What: paddingBottom / marginBottom / translationY
     */
    fun applyImeDeltaBottom(
        root: View,
        target: View,
        applyTo: ApplyTo = ApplyTo.Padding
    ) {
        when (applyTo) {
            ApplyTo.Padding -> {
                val init = target.captureInitialPadding()
                ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
                    val sys = insets.getInsets(Type.systemBars())
                    val ime = insets.getInsets(Type.ime())
                    val delta = max(ime.bottom - sys.bottom, 0)
                    target.updatePadding(
                        left = init.left,
                        top = init.top,
                        right = init.right,
                        bottom = init.bottom + delta
                    )
                    insets
                }
            }

            ApplyTo.Margin -> {
                val init = target.captureInitialMargin()
                ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
                    val sys = insets.getInsets(Type.systemBars())
                    val ime = insets.getInsets(Type.ime())
                    val delta = max(ime.bottom - sys.bottom, 0)
                    target.updateLayoutParams<MarginLayoutParams> {
                        leftMargin = init.left
                        topMargin = init.top
                        rightMargin = init.right
                        bottomMargin = init.bottom + delta
                    }
                    insets
                }
            }

            ApplyTo.Translation -> {
                val initialTransY = target.translationY
                ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
                    val sys = insets.getInsets(Type.systemBars())
                    val ime = insets.getInsets(Type.ime())
                    val delta = max(ime.bottom - sys.bottom, 0)
                    target.translationY = initialTransY - delta
                    insets
                }
            }
        }
        ViewCompat.requestApplyInsets(root)
    }

    // ==========================
    // 三、让“贴底 overlay”跟随系统栏/IME 上移（可选）
    // ==========================

    /**
     * 精确上移某个“贴底”视图（已知 ID 或直接引用）。
     * @param followIme 是否跟随 IME；true 时按 max(systemBars.bottom, ime.bottom)
     * @param applyTo Padding/Margin/Translation 三选一
     */
    fun liftSpecificBottomView(
        root: View,
        view: View,
        followIme: Boolean = false,
        applyTo: ApplyTo = ApplyTo.Padding,
        extraBottomPaddingDp: Int = 0
    ) {
        when (applyTo) {
            ApplyTo.Padding -> {
                val init = view.captureInitialPadding()
                ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
                    val sys = insets.getInsets(Type.systemBars())
                    val ime = if (followIme) insets.getInsets(Type.ime()) else null
                    val bottom = max(sys.bottom, ime?.bottom ?: 0) + extraBottomPaddingDp.dp
                    view.updatePadding(init.left, init.top, init.right, init.bottom + bottom)
                    insets
                }
            }

            ApplyTo.Margin -> {
                val init = view.captureInitialMargin()
                ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
                    val sys = insets.getInsets(Type.systemBars())
                    val ime = if (followIme) insets.getInsets(Type.ime()) else null
                    val bottom = max(sys.bottom, ime?.bottom ?: 0) + extraBottomPaddingDp.dp
                    view.updateLayoutParams<MarginLayoutParams> {
                        leftMargin = init.left; topMargin = init.top; rightMargin = init.right
                        bottomMargin = init.bottom + bottom
                    }
                    insets
                }
            }

            ApplyTo.Translation -> {
                val initTransY = view.translationY
                ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
                    val sys = insets.getInsets(Type.systemBars())
                    val ime = if (followIme) insets.getInsets(Type.ime()) else null
                    val bottom = max(sys.bottom, ime?.bottom ?: 0) + extraBottomPaddingDp.dp
                    view.translationY = initTransY - bottom
                    insets
                }
            }
        }
        ViewCompat.requestApplyInsets(root)
    }
}