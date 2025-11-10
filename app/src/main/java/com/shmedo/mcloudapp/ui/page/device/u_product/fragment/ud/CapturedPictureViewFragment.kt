package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.ConvertUtils
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.luck.picture.lib.adapter.PicturePreviewAdapter
import com.luck.picture.lib.basic.PictureMediaScannerConnection
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.utils.DownloadFileUtils
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentCapturedPictureViewBinding
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.ui.adapter.CustomPreviewAdapter
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.CapturedPictureViewViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.utils.permission.PermissionDescription
import com.shmedo.mcloudapp.utils.permission.PermissionInterceptor
import kotlin.math.abs

class CapturedPictureViewFragment : BaseFragment() {
    private lateinit var binding: FragmentCapturedPictureViewBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: CapturedPictureViewViewModel by activityViewModels()

    private lateinit var viewPager: ViewPager2
    private lateinit var viewPageAdapter: PicturePreviewAdapter
    private var curPosition: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            curPosition = it.getInt(CURRENT_POSITION)
        }
    }

    override fun initViewModel() {}

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_captured_picture_view,
            BR.stateVM,
            mStates
        )
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentCapturedPictureViewBinding
        initToolbar()
        initViewPage()
    }

    private fun initToolbar() {
        binding.llToolbar.toolbar.title = "返回"
        binding.llToolbar.toolbar.apply {
            setNavigationOnClickListener { nav().navigateUp() }
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }
    }

    private fun initViewPage() {
        viewPager = ViewPager2(requireContext()).apply {
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            // 预加载数量
            offscreenPageLimit = 1
        }
        binding.magical.setMagicalContent(viewPager)

        viewPageAdapter = CustomPreviewAdapter().apply {
            setData(mStates.mData)
        }
        viewPager.apply {
            adapter = viewPageAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                }

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    curPosition = position
                }
            })

            // 添加页面切换动画
            setPageTransformer(CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(ConvertUtils.dp2px(3f)))
                addTransformer { page, position ->
                    val r = 1 - abs(position)
                    page.scaleY = 0.85f + r * 0.15f
                }
            })

            setCurrentItem(curPosition, false)
        }
    }

    inner class ClickProxy : BaseClickProxy() {
        /**
         * 点击查看上一张图片
         */
        fun onViewPreImageClick() {
            if (curPosition > 0) {
                viewPager.setCurrentItem(curPosition - 1, true)
            } else {
                Toaster.show("已经是第一张了")
            }
        }

        /**
         * 点击查看下一张图片
         */
        fun onViewNextImageClick() {
            val total = mStates.mData.size
            if (curPosition < total - 1) {
                viewPager.setCurrentItem(curPosition + 1, true)
            } else {
                Toaster.show("已经是最后一张了")
            }
        }

        /**
         *
         */
        fun onDownloadImageClick() {
            //申请存储权限
            XXPermissions.with(this@CapturedPictureViewFragment)
                .permission(PermissionLists.getWriteExternalStoragePermission())
                // 设置权限请求拦截器（局部设置）
                .interceptor(PermissionInterceptor())
                .description(PermissionDescription())
                .request(OnPermissionCallback { grantedList, deniedList ->
                    val allGranted = deniedList.isEmpty()
                    if (!allGranted) {
                        return@OnPermissionCallback
                    }

                    saveImage()
                })
        }
    }

    private fun saveImage() {
        val media = mStates.mData.getOrNull(curPosition) ?: return

        try {
            val path = media.availablePath
            if (PictureMimeType.isHasHttp(path)) {
                showLoadingDialog("下载中...")
            }
            DownloadFileUtils.saveLocalFile(
                context, path, media.mimeType
            ) { realPath: String? ->
                dismissLoadingDialog()
                if (realPath.isNullOrEmpty()) {
                    val errorMsg = when {
                        PictureMimeType.isHasAudio(media.mimeType) -> getString(R.string.ps_save_audio_error)
                        PictureMimeType.isHasVideo(media.mimeType) -> getString(R.string.ps_save_video_error)
                        else -> getString(R.string.ps_save_image_error)
                    }
                    Toaster.show(errorMsg)
                } else {
                    PictureMediaScannerConnection(activity, realPath)
                    Toaster.show(getString(R.string.ps_save_success) + "\n" + realPath)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMsg = when {
                PictureMimeType.isHasAudio(media.mimeType) -> getString(R.string.ps_save_audio_error)
                PictureMimeType.isHasVideo(media.mimeType) -> getString(R.string.ps_save_video_error)
                else -> getString(R.string.ps_save_image_error)
            }
            Toaster.show(errorMsg)
        }
    }

    override fun onDestroyView() {
        viewPager.unregisterOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {})
        super.onDestroyView()
    }



    companion object {
        const val CURRENT_POSITION = "current_position"

        fun newBundleArguments(
            position: Int = 0,
        ): Bundle = Bundle().apply {
            putInt(CURRENT_POSITION, position)
        }
    }
}