package com.shmedo.mcloudapp.ui.dialog

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.extensions.showLoadingWithUUID
import com.shmedo.mcloudapp.extensions.updateLoadingMessage
import com.shmedo.mcloudapp.utils.LoadingDialogManager
import timber.log.Timber

/**
 * @author：gonghe
 * @time: 2025/1/7
 * @desc: LoadingDialog重构后的功能测试类
 * 
 * 用于验证重构后的LoadingDialog在各种场景下的稳定性
 */
object LoadingDialogTest {

    /**
     * 测试基本的显示和关闭功能
     */
    fun testBasicShowAndDismiss(activity: AppCompatActivity) {
        try {
            // 测试基本显示
            val loadingId1 = activity.showLoadingDialog("测试加载中...")
            Timber.d("显示LoadingDialog成功，ID: $loadingId1")
            
            // 延迟关闭
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                activity.dismissLoadingDialog(loadingId1)
                Timber.d("关闭LoadingDialog成功")
            }, 3000)
            
        } catch (e: Exception) {
            Timber.e(e, "基本测试失败")
        }
    }

    /**
     * 测试Fragment中的使用
     */
    fun testFragmentUsage(fragment: Fragment) {
        try {
            // 检查Fragment状态
            if (!fragment.isAdded || fragment.isDetached) {
                Timber.w("Fragment状态异常，跳过测试")
                return
            }
            
            // 测试UUID生成
            val loadingId = fragment.showLoadingWithUUID("Fragment测试加载...")
            Timber.d("Fragment显示LoadingDialog成功，ID: $loadingId")
            
            // 测试消息更新
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (fragment.isAdded && !fragment.isDetached) {
                    fragment.updateLoadingMessage(loadingId, "更新消息测试...")
                    Timber.d("更新消息成功")
                }
            }, 1500)
            
            // 测试关闭
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (fragment.isAdded && !fragment.isDetached) {
                    fragment.dismissLoadingDialog(loadingId)
                    Timber.d("Fragment关闭LoadingDialog成功")
                }
            }, 3000)
            
        } catch (e: Exception) {
            Timber.e(e, "Fragment测试失败")
        }
    }

    /**
     * 测试多个Loading同时存在的情况
     */
    fun testMultipleLoading(activity: AppCompatActivity) {
        try {
            val loadingId1 = activity.showLoadingDialog("第一个加载...")
            val loadingId2 = activity.showLoadingDialog("第二个加载...")
            val loadingId3 = activity.showLoadingDialog("第三个加载...")
            
            Timber.d("创建多个LoadingDialog: $loadingId1, $loadingId2, $loadingId3")
            
            // 分别在不同时间关闭
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                activity.dismissLoadingDialog(loadingId1)
            }, 2000)
            
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                activity.dismissLoadingDialog(loadingId2)
            }, 4000)
            
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                activity.dismissLoadingDialog(loadingId3)
            }, 6000)
            
        } catch (e: Exception) {
            Timber.e(e, "多Loading测试失败")
        }
    }

    /**
     * 测试异常情况处理
     */
    fun testExceptionHandling(activity: AppCompatActivity?) {
        try {
            // 测试null activity
            val loadingId = activity?.showLoadingDialog("异常测试") ?: "test_id"
            Timber.d("异常处理测试，ID: $loadingId")
            
            // 尝试关闭不存在的Dialog
            activity?.dismissLoadingDialog("non_exist_id")
            
            // 测试重复关闭
            activity?.dismissLoadingDialog(loadingId)
            activity?.dismissLoadingDialog(loadingId)
            
            Timber.d("异常处理测试完成")
            
        } catch (e: Exception) {
            Timber.e(e, "异常处理测试出错")
        }
    }

    /**
     * 测试生命周期相关的场景（模拟QuickConfigCommandParamFragment的使用）
     */
    fun testLifecycleScenario(fragment: Fragment) {
        if (!fragment.isAdded || fragment.isDetached || fragment.activity?.isFinishing == true) {
            Timber.w("Fragment生命周期异常，跳过测试")
            return
        }
        
        try {
            // 模拟长时间操作（如Excel导出）
            var loadingId: String? = null
            
            // 启动模拟操作
            loadingId = fragment.showLoadingWithUUID(
                message = "正在模拟导出Excel...",
                isCancelable = false
            )
            
            // 模拟异步操作
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    // 检查Fragment状态（模拟异步操作完成时的状态检查）
                    if (!fragment.isAdded || fragment.isDetached || fragment.activity?.isFinishing == true) {
                        Timber.w("操作完成时Fragment已销毁，跳过UI更新")
                        return@postDelayed
                    }
                    
                    // 模拟操作成功完成
                    Timber.d("模拟操作完成")
                    
                } catch (e: Exception) {
                    Timber.e(e, "模拟操作异常")
                } finally {
                    // 安全关闭Loading
                    loadingId?.let { id ->
                        try {
                            fragment.dismissLoadingDialog(id)
                            Timber.d("安全关闭LoadingDialog")
                        } catch (e: Exception) {
                            Timber.w(e, "关闭LoadingDialog失败")
                        }
                    }
                }
            }, 5000) // 模拟5秒的操作时间
            
        } catch (e: Exception) {
            Timber.e(e, "生命周期测试失败")
        }
    }

    /**
     * 性能测试：快速创建和销毁大量Dialog
     */
    fun testPerformance(activity: AppCompatActivity) {
        try {
            val startTime = System.currentTimeMillis()
            val loadingIds = mutableListOf<String>()
            
            // 快速创建100个Dialog（测试内存管理）
            repeat(100) { i ->
                val id = activity.showLoadingDialog("性能测试 $i")
                loadingIds.add(id)
            }
            
            val createTime = System.currentTimeMillis() - startTime
            Timber.d("创建100个LoadingDialog耗时: ${createTime}ms")
            
            // 快速销毁所有Dialog
            val destroyStart = System.currentTimeMillis()
            loadingIds.forEach { id ->
                activity.dismissLoadingDialog(id)
            }
            
            val destroyTime = System.currentTimeMillis() - destroyStart
            Timber.d("销毁100个LoadingDialog耗时: ${destroyTime}ms")
            
            // 触发清理
            LoadingDialogManager.performCleanup()
            
        } catch (e: Exception) {
            Timber.e(e, "性能测试失败")
        }
    }
}
