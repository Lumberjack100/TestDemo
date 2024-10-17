package com.shmedo.mcloudapp.ui.page.userprofile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextUtils
import android.util.Base64
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ThreadUtils
import com.hjq.toast.Toaster
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.utils.MediaUtils
import com.luck.picture.lib.utils.PictureFileUtils
import com.shmedo.core.commonlib.mmkv.AuthMMKVOwner
import com.shmedo.core.model.UserInfo
import com.shmedo.lib.network.response.DataResult
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentUserInfoHomeBinding
import com.shmedo.mcloudapp.extensions.dismissLoadingDialog
import com.shmedo.mcloudapp.extensions.getFragmentScopeViewModel
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.showLoadingDialog
import com.shmedo.mcloudapp.ui.page.base.fragment.BaseFragment
import com.shmedo.mcloudapp.ui.viewmodel.request.LoginRequestViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.UserInfoHomeViewModel
import com.shmedo.mcloudapp.utils.image.GlideEngine
import com.shmedo.mcloudapp.utils.image.ImageFileCompressEngine
import com.shmedo.mcloudapp.utils.image.MeOnCameraInterceptListener
import com.shmedo.mcloudapp.utils.image.MeSandboxFileEngine
import org.json.JSONException
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.getViewModel
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException

class UserInfoHomeFragment : BaseFragment() {
    private lateinit var binding: FragmentUserInfoHomeBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private lateinit var mStates: UserInfoHomeViewModel
    private lateinit var loginRequestViewModel: LoginRequestViewModel
    private val userInfo: UserInfo by lazy { AuthMMKVOwner.userInfo!! }


    override fun initViewModel() {
        mStates = getFragmentScopeViewModel()
        loginRequestViewModel = getViewModel()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_user_info_home, BR.vm, mStates)
            .addBindingParam(BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, ClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentUserInfoHomeBinding
        toolbarViewModel.toolbarTitleText.set("个人资料")
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        mActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                nav().navigateUp()
            }
        })
        val filter = InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {
                if (!"0123456789".contains(source[i].toString())) {
                    return@InputFilter ""
                }
            }
            null
        }
        binding.phoneET.filters = arrayOf(LengthFilter(11), filter)
        binding.emailET.filters = arrayOf(LengthFilter(30))
    }

    override fun initData() {
        if (!TextUtils.isEmpty(userInfo.headPhotoPath))
            mStates.imageUrl.set(userInfo.headPhotoPath)
    }

    private fun updateView() {
        mStates.name.set(userInfo.name)
        mStates.post.set(userInfo.position)
        mStates.phone.set(userInfo.cellPhone)
        mStates.email.set(userInfo.email)
    }

    override fun createObserver() {
        loginRequestViewModel.uploadUserAvataResult.observe(viewLifecycleOwner) { dataResult: DataResult<String> ->
            dismissLoadingDialog()
            if (!dataResult.responseStatus.isSuccess) {
                Toaster.show(dataResult.responseStatus.errorMessage)
                return@observe
            }
            Toaster.show("头像已上传")
            setFragmentResult(
                MineFragment.requestKey,
                bundleOf(com.shmedo.core.commonlib.utils.AppContants.Extras.IS_REFRESH_USER_INFO to true)
            )
        }
        loginRequestViewModel.updateUserInfoResult.observe(viewLifecycleOwner) { dataResult: DataResult<String> ->
            dismissLoadingDialog()
            if (!dataResult.responseStatus.isSuccess) {
                Toaster.show(dataResult.responseStatus.errorMessage)
                return@observe
            }
            Toaster.show("数据保存成功")
            setFragmentResult(
                MineFragment.requestKey,
                bundleOf(com.shmedo.core.commonlib.utils.AppContants.Extras.IS_REFRESH_USER_INFO to true)
            )
            ThreadUtils.runOnUiThreadDelayed({
//                mMessenger.requestStatusBarColor(R.color.colorPrimary)
                nav().navigateUp()
            }, 1000)
        }
    }


    inner class ClickProxy : BaseClickProxy() {
        fun onChangeAvatar() {
            // 进入相册
            PictureSelector.create(context)
                .openGallery(SelectMimeType.ofAll())
                .setImageEngine(GlideEngine.createGlideEngine())
                .setCompressEngine(ImageFileCompressEngine())
                .setSandboxFileEngine(MeSandboxFileEngine())
                .setCameraInterceptListener(MeOnCameraInterceptListener())
                .isDisplayCamera(true)
                .isWithSelectVideoImage(false)
                .isMaxSelectEnabledMask(false)
                .setMaxSelectNum(1)
                .setMaxVideoSelectNum(0) //                    .setSelectedData(mAdapter.getData())
                .forResult(MeOnResultCallbackListener())
        }

        /**
         * 保存事件
         */
        fun onSubmitClick() {
            if (TextUtils.isEmpty(mStates.name.get())) {
                Toaster.show("请输入用户名")
                binding.nameET.requestFocus()
                return
            }
            val jsonObjectRequest = JSONObject()
            try {
                jsonObjectRequest.put("companyID", AuthMMKVOwner.companyID)
                jsonObjectRequest.put("userID", AuthMMKVOwner.userID)
                jsonObjectRequest.put("name", mStates.name.get())
                if (mStates.post.get().isNotEmpty())
                    jsonObjectRequest.put("position", mStates.post.get())
                if (mStates.email.get().isNotEmpty())
                    jsonObjectRequest.put("email", mStates.email.get())
            } catch (e: JSONException) {
                Timber.e(e)
            }
            showLoadingDialog("处理中...")
            loginRequestViewModel.requestUpdateUserInfo(jsonObjectRequest.toString())
        }
    }

    private fun uploadAvatar(filePath: String) {
        val fileContent: String? = getBase64ImageString(filePath)
        if (TextUtils.isEmpty(fileContent)) {
            Toaster.show("头像图片文件名或图片Base64字符串为空")
            return
        }
        val jsonObjectRequest = JSONObject()
        try {
            jsonObjectRequest.put("companyID", AuthMMKVOwner.companyID)
            jsonObjectRequest.put("userID", AuthMMKVOwner.userID)
            jsonObjectRequest.put("content", fileContent)
            jsonObjectRequest.put("extension", "png")
        } catch (e: JSONException) {
            Timber.e(e)
        }
        showLoadingDialog("正在上传...")
        loginRequestViewModel.uploadUserAvatar(jsonObjectRequest.toString())
    }

    private fun getBase64ImageString(filePath: String): String? {
        if (TextUtils.isEmpty(filePath)) {
            return null
        }
        val bitmap = BitmapFactory.decodeFile(filePath)
        return bitmapToBase64(bitmap)
    }

    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    private fun bitmapToBase64(bitmap: Bitmap?): String? {
        var result: String? = null
        var outputStream: ByteArrayOutputStream? = null
        try {
            if (bitmap != null) {
                outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                outputStream.flush()
                outputStream.close()
                val bitmapBytes = outputStream.toByteArray()
                val byteLength = bitmapBytes.size.toFloat() / 1024 / 1024
                Timber.i("bytes.length=  " + byteLength + "MB")
                result = Base64.encodeToString(bitmapBytes, Base64.NO_WRAP)
            }
        } catch (e: IOException) {
            Timber.e(e)
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush()
                    outputStream.close()
                }
            } catch (e: IOException) {
                Timber.e(e)
            }
        }
        return result
    }

    /**
     * 选择结果
     */
    private inner class MeOnResultCallbackListener : OnResultCallbackListener<LocalMedia> {
        override fun onResult(result: ArrayList<LocalMedia>) {
            analyticalSelectResults(result)
        }

        override fun onCancel() {
            Timber.i("PictureSelector Cancel")
        }
    }

    /**
     * 处理选择结果
     *
     * @param result
     */
    private fun analyticalSelectResults(result: ArrayList<LocalMedia>) {
        val media = result[0]
        if (media.width == 0 || media.height == 0) {
            if (PictureMimeType.isHasImage(media.mimeType)) {
                val imageExtraInfo = MediaUtils.getImageSize(context, media.path)
                media.width = imageExtraInfo.width
                media.height = imageExtraInfo.height
            } else if (PictureMimeType.isHasVideo(media.mimeType)) {
                val videoExtraInfo = MediaUtils.getVideoSize(context, media.path)
                media.width = videoExtraInfo.width
                media.height = videoExtraInfo.height
            }
        }
        Timber.i("文件名: %s", media.fileName)
        Timber.i("是否压缩:%s", media.isCompressed)
        Timber.i("压缩路径:%s", media.compressPath)
        Timber.i("初始路径(getPath):%s", media.path)
        Timber.i("绝对路径(getRealPath):%s", media.realPath)
        Timber.i("是否裁剪:%s", media.isCut)
        Timber.i("裁剪路径:%s", media.cutPath)
        Timber.i("是否开启原图:%s", media.isOriginal)
        Timber.i("原图路径:%s", media.originalPath)
        Timber.i("沙盒路径:%s", media.sandboxPath)
        Timber.i("水印路径:%s", media.watermarkPath)
        Timber.i("视频缩略图:%s", media.videoThumbnailPath)
        Timber.i("原始宽高: " + media.width + "x" + media.height)
        Timber.i("裁剪宽高: " + media.cropImageWidth + "x" + media.cropImageHeight)
        Timber.i("文件大小: %s", PictureFileUtils.formatAccurateUnitFileSize(media.size))
        Timber.i("文件时长: %s", media.duration)
        mStates.imageUrl.set(media.sandboxPath)
        val path =
            if (TextUtils.isEmpty(media.compressPath)) media.sandboxPath else media.compressPath

        uploadAvatar(path)
    }

    override fun onResume() {
        super.onResume()
        initImmersionBar(binding.llToolbar.toolbar)
        updateView()
    }
}