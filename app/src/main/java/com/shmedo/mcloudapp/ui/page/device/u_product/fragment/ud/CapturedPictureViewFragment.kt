package com.shmedo.mcloudapp.ui.page.device.u_product.fragment.ud

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentCapturedPictureViewBinding
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.CapturedPictureViewViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CapturedPictureViewFragment : BaseFragment() {
    private lateinit var binding: FragmentCapturedPictureViewBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: CapturedPictureViewViewModel by viewModels()

    private var currentBitmap: Bitmap? = null
    private var originalBitmap: Bitmap? = null
    private var rotateAngle = 0f
    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    override fun initViewModel() {

    }

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
        binding.llToolbar.toolbar.title = "抓拍图片"
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
//            mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
            nav().navigateUp()
        }

        loadImage()
    }


    inner class ClickProxy : BaseClickProxy() {
        /**
         *
         */
        fun onRotateClick() {
            rotateImage(-90f)
        }

        /**
         *
         */
        fun onResetRotationClick() {
            resetImage()
        }

        /**
         *
         */
        fun onDownloadImageClick() {
            saveImage()

        }
    }


    private fun loadImage() {
        uiScope.launch(Dispatchers.IO) {
            // 假设图片资源为 R.drawable.sample_image
            val bitmap = Glide.with(requireContext())
                .asBitmap()
                .load(R.drawable.capture1) //mStates.imageUrl.get()
                .submit()
                .get()

            withContext(Dispatchers.Main) {
                originalBitmap = bitmap
                currentBitmap = bitmap
                binding.imageView.setImageBitmap(bitmap)
            }
        }
    }

    private fun rotateImage(degrees: Float) {
//        rotateAngle += degrees
//        val newDegrees = if (rotateAngle < 0) {
//            rotateAngle % 360 + 360
//        } else {
//            rotateAngle % 360
//        }
//        Timber.d("rotateAngle: $rotateAngle")
//        Timber.d("newDegrees: $newDegrees")

        currentBitmap?.let {
            val matrix = Matrix()
//            rotateAngle += degrees
            matrix.postRotate(degrees)
            val rotatedBitmap = Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true)
            currentBitmap = rotatedBitmap
            binding.imageView.setImageBitmap(rotatedBitmap)
        }
    }

    private fun resetImage() {
        currentBitmap = originalBitmap
        rotateAngle = 0f
        binding.imageView.setImageBitmap(originalBitmap)
    }

    private fun saveImage() {
        currentBitmap?.let { bitmap ->
            uiScope.launch(Dispatchers.IO) {
                try {
                    val savedImageURL = MediaStore.Images.Media.insertImage(
                        requireContext().contentResolver,
                        bitmap,
                        "Rotated Image",
                        "Image rotated by 90 degrees"
                    )
                    withContext(Dispatchers.Main) {
                        // 可以提示用户图片保存成功
                        Toaster.show("图片保存成功")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toaster.show("保存图片失败")
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
    }

    companion object {
        fun newInstance() = CapturedPictureViewFragment()
    }
}