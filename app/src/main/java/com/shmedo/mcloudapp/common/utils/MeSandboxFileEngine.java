package com.shmedo.mcloudapp.common.utils;

import android.content.Context;

import com.luck.picture.lib.engine.UriToFileTransformEngine;
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener;
import com.luck.picture.lib.utils.SandboxTransformUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/1/2 <br/>
 * 描述：     自定义沙盒文件处理
 */
public class MeSandboxFileEngine implements UriToFileTransformEngine {
    @Override
    public void onUriToFileAsyncTransform(Context context, String srcPath, String mineType, OnKeyValueResultCallbackListener call) {
        if (call != null) {
            call.onCallback(srcPath, SandboxTransformUtils.copyPathToSandbox(context, srcPath, mineType));
        }
    }
}
