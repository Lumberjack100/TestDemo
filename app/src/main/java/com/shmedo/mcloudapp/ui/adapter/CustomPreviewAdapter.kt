package com.shmedo.mcloudapp.ui.adapter

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
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
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.ps_custom_preview_image, parent, false)
            CustomPreviewImageHolder(itemView)
        } else {
            super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun onViewRecycled(holder: BasePreviewHolder) {
        super.onViewRecycled(holder)
        if (holder is CustomPreviewImageHolder) {
            holder.clear()
        }
    }

    class CustomPreviewImageHolder(itemView: View) : BasePreviewHolder(itemView) {
        private lateinit var subsamplingScaleImageView: SubsamplingScaleImageView
        private lateinit var progressBar: ProgressBar
        private var currentGlideTarget: CustomTarget<Bitmap>? = null

        override fun findViews(itemView: View) {
            subsamplingScaleImageView = itemView.findViewById(R.id.big_preview_image)
            progressBar = itemView.findViewById(R.id.progress_bar)
        }

        override fun loadImage(media: LocalMedia, maxWidth: Int, maxHeight: Int) {
            if (!ActivityCompatHelper.assertValidRequest(itemView.context)) {
                return
            }
            //取消之前的 Glide 请求
            clear()
            //显示 ProgressBar
            progressBar.visibility = View.VISIBLE
            //隐藏 SubsamplingScaleImageView
            subsamplingScaleImageView.visibility = View.INVISIBLE

            //创建新的 Glide 请求
            val glideTarget = object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    //隐藏 ProgressBar
                    progressBar.visibility = View.GONE

                    if (MediaUtils.isLongImage(resource.width, resource.height)) {
                        subsamplingScaleImageView.visibility = View.VISIBLE
                        val scale = maxOf(
                            screenWidth / resource.width.toFloat(),
                            screenHeight / resource.height.toFloat()
                        )
                        subsamplingScaleImageView.setImage(
                            ImageSource.cachedBitmap(resource),
                            ImageViewState(scale, PointF(0f, 0f), 0)
                        )
                    } else {
                        subsamplingScaleImageView.visibility = View.GONE
                        coverImageView.setImageBitmap(resource)
                    }
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    //隐藏 ProgressBar
                    progressBar.visibility = View.GONE
                    //设置默认错误占位图（可选）
                    coverImageView.setImageResource(R.drawable.bg_error)
                    //显示错误提示
//                    Toaster.show("图片加载失败")
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // 隐藏 ProgressBar
                    progressBar.visibility = View.GONE
                }
            }
            currentGlideTarget = glideTarget

            Glide.with(itemView.context)
                .asBitmap()
                .load(media.availablePath)
                .into(glideTarget)
        }

        /**
         * 清理方法，用于在 ViewHolder 被回收时取消 Glide 请求
         */
        fun clear() {
            currentGlideTarget?.let {
                Glide.with(itemView.context).clear(it)
            }
            currentGlideTarget = null
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