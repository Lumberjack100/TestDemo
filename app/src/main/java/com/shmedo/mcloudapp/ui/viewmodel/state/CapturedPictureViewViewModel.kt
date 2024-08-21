package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.blankj.utilcode.util.ResourceUtils
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class CapturedPictureViewViewModel : ViewModel() {
    val imageUrl = NonNullObservableField("https://mdnet-normal.oss-cn-hangzhou.aliyuncs.com/ali-oss-eee9438c-d6a9-47cd-8f7e-623ae05e3a76?Expires=1724144504&OSSAccessKeyId=LTAI5DRlaZP1R7Kr&Signature=mXhrmDnUFpv4hs1D12Fz2tghmRY%3D")
    val placeHolder = NonNullObservableField(ResourceUtils.getDrawable(R.drawable.ic_account))
}