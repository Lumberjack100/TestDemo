package com.shmedo.mcloudapp.utils.image

import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.luck.lib.camerax.SimpleCameraX
import com.luck.picture.lib.interfaces.OnCameraInterceptListener

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/8/24 <br/>
 * 描述：     TODO
 */
class MeOnCameraInterceptListener: OnCameraInterceptListener {
    override fun openCamera(fragment: Fragment, cameraMode: Int, requestCode: Int) {
        val camera: SimpleCameraX = SimpleCameraX.of()
        camera.isAutoRotation(true)
        camera.setCameraMode(cameraMode)
        camera.setVideoFrameRate(25)
        camera.setVideoBitRate(3 * 1024 * 1024)
        camera.isDisplayRecordChangeTime(true)
        camera.isManualFocusCameraPreview(true)
        camera.isZoomCameraPreview(true)
//        camera.setOutputPathDir(getSandboxCameraOutputPath())
//        camera.setPermissionDeniedListener(getSimpleXPermissionDeniedListener())
//        camera.setPermissionDescriptionListener(getSimpleXPermissionDescriptionListener())
        camera.setImageEngine { context, url, imageView ->
            Glide.with(context!!).load(url).into(imageView!!)
        }
        camera.start(fragment.requireActivity(), fragment, requestCode)
    }
}