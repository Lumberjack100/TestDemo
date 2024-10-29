package com.shmedo.mcloudapp.ui.page.device.image_preview

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.ImageViewState
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.luck.picture.lib.adapter.PicturePreviewAdapter
import com.luck.picture.lib.adapter.holder.BasePreviewHolder
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.utils.ActivityCompatHelper
import com.luck.picture.lib.utils.MediaUtils
import com.shmedo.mcloudapp.R

/**
 * @author：luck
 * @date：2022/2/21 4:17 下午
 * @describe：CustomPreviewAdapter
 */
class CustomPreviewAdapter : PicturePreviewAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BasePreviewHolder {
        return if (viewType == BasePreviewHolder.ADAPTER_TYPE_IMAGE) {
            // 这里以重写自定义图片预览为例
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.ps_custom_preview_image, parent, false)
            CustomPreviewImageHolder(itemView)
        } else {
            super.onCreateViewHolder(parent, viewType)
        }
    }

    class CustomPreviewImageHolder(itemView: View) : BasePreviewHolder(itemView) {
        private lateinit var subsamplingScaleImageView: SubsamplingScaleImageView

        override fun findViews(itemView: View) {
            subsamplingScaleImageView = itemView.findViewById(R.id.big_preview_image)
        }

        override fun loadImage(media: LocalMedia, maxWidth: Int, maxHeight: Int) {
            if (!ActivityCompatHelper.assertValidRequest(itemView.context)) {
                return
            }

            Glide.with(itemView.context)
                .asBitmap()
                .load(media.availablePath)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        if (MediaUtils.isLongImage(resource.width, resource.height)) {
                            subsamplingScaleImageView.visibility = View.VISIBLE
                            val scale = maxOf(screenWidth / resource.width.toFloat(),
                                screenHeight / resource.height.toFloat())
                            subsamplingScaleImageView.setImage(
                                ImageSource.cachedBitmap(resource),
                                ImageViewState(scale, PointF(0f, 0f), 0)
                            )
                        } else {
                            subsamplingScaleImageView.visibility = View.GONE
                            coverImageView.setImageBitmap(resource)
                        }
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {}

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }

        override fun onClickBackPressed() {
            if (MediaUtils.isLongImage(media.width, media.height)) {
                subsamplingScaleImageView.setOnClickListener {
                    mPreviewEventListener?.onBackPressed()
                }
            } else {
                coverImageView.setOnViewTapListener { _, _, _ ->
                    mPreviewEventListener?.onBackPressed()
                }
            }
        }

        override fun onLongPressDownload(media: LocalMedia) {
            if (MediaUtils.isLongImage(media.width, media.height)) {
                subsamplingScaleImageView.setOnLongClickListener {
                    mPreviewEventListener?.onLongPressDownload(media)
                    false
                }
            } else {
                coverImageView.setOnLongClickListener {
                    mPreviewEventListener?.onLongPressDownload(media)
                    false
                }
            }
        }
    }
}