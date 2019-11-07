package com.shmedo.mcloudapp.views;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * 项目名：  das-config-app
 * 包名：    com.example.medoDas.widget
 * 文件名:   LoadingDialog
 * 创建者:   dpc
 * 创建时间:  2018/3/14 16:05
 * 描述：    TODO
 */

public class LoadingDialog {
    private Context mContext;
    private MaterialDialog loadingDialog = null;

    public LoadingDialog(Context context) {
        this.mContext = context;
    }

    public MaterialDialog getLoadingDialog() {
        return loadingDialog;
    }


    public void show() {
        show(null);
    }

    public void showNoCancelDialog() {
        showNoCancelDialog(null);
    }



    public void show(String tip) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            return;
        }

        if (loadingDialog == null) {
            loadingDialog = new MaterialDialog.Builder(mContext)
                    .content(TextUtils.isEmpty(tip) ? "正在加载..." : tip)
                    .progress(true, 0)
                    .progressIndeterminateStyle(false)
                    .build();
            loadingDialog.setCanceledOnTouchOutside(true);
        }

        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }


    public void showNoCancelDialog(String tip) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            return;
        }

        if (loadingDialog == null) {
            loadingDialog = new MaterialDialog.Builder(mContext)
                    .content(TextUtils.isEmpty(tip) ? "正在加载..." : tip)
                    .progress(true, 0)
                    .progressIndeterminateStyle(false)
                    .build();
            loadingDialog.setCancelable(false);
            loadingDialog.setCanceledOnTouchOutside(false);
        }

        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }



    public void dismiss() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    public boolean isShowing() {
        return loadingDialog.isShowing();
    }


    public static void showScanResultDialog(Context context, String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(context);
        mBuilder.title("温馨提示：")
                .content(content)
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false)
                .positiveText("确定");
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }
}
