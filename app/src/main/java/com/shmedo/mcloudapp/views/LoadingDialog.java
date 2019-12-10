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
 *
 */

public class LoadingDialog {
    private Context mContext;
    private MaterialDialog materialDialog = null;

    public LoadingDialog(Context context) {
        this.mContext = context;
    }

    public MaterialDialog getLoadingDialog() {
        return materialDialog;
    }


    public void show() {
        show(null);
    }

    public void showNoCancelDialog() {
        showNoCancelDialog(null);
    }



    public void show(String tip) {
        if (materialDialog != null && materialDialog.isShowing()) {
            return;
        }

        if (materialDialog == null) {
            materialDialog = new MaterialDialog.Builder(mContext)
                    .content(TextUtils.isEmpty(tip) ? "正在加载..." : tip)
                    .progress(true, 0)
                    .progressIndeterminateStyle(false)
                    .build();
            materialDialog.setCanceledOnTouchOutside(true);
        }

        if (!materialDialog.isShowing()) {
            materialDialog.show();
        }
    }


    public void showNoCancelDialog(String tip) {
        if (materialDialog != null && materialDialog.isShowing()) {
            return;
        }

        if (materialDialog == null) {
            materialDialog = new MaterialDialog.Builder(mContext)
                    .content(TextUtils.isEmpty(tip) ? "正在加载..." : tip)
                    .progress(true, 0)
                    .progressIndeterminateStyle(false)
                    .build();
            materialDialog.setCancelable(false);
            materialDialog.setCanceledOnTouchOutside(false);
        }

        if (!materialDialog.isShowing()) {
            materialDialog.show();
        }
    }



    public void dismiss() {
        if (materialDialog != null && materialDialog.isShowing()) {
            materialDialog.dismiss();
            materialDialog = null;
        }
    }

    public boolean isShowing() {
        return materialDialog.isShowing();
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
