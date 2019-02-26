package com.shmedo.mcloudapp.views;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.afollestad.materialdialogs.DialogAction;
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
    private MaterialDialog dialog;

    public LoadingDialog(Context context) {
        this.mContext = context;
    }

    public MaterialDialog getDialog() {
        return dialog;
    }

    public void show(String tip) {
        if (dialog == null) {
            dialog = new MaterialDialog.Builder(mContext)
                .content(TextUtils.isEmpty(tip) ? "正在加载..." : tip)
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .build();
            dialog.setCanceledOnTouchOutside(true);
        }
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    public void show() {
        show(null);
    }

    public void showCancelDialog(String tip) {
        if (dialog == null) {
            dialog = new MaterialDialog.Builder(mContext)
                .content(TextUtils.isEmpty(tip) ? "正在加载..." : tip)
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .build();
            dialog.setCanceledOnTouchOutside(false);
        }
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    public void showNoCancelDialog(String tip) {
        if (dialog == null) {
            dialog = new MaterialDialog.Builder(mContext)
                .content(TextUtils.isEmpty(tip) ? "正在加载..." : tip)
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .build();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
        }
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    public void showNoCancel() {
        showNoCancelDialog(null);
    }


    public void showCancelDialog() {
        showCancelDialog(null);
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    public boolean isShowing() {
        return dialog.isShowing();
    }

    public static void showRefusePermissionDialog(final Context context, String message) {
        MaterialDialog.Builder builderRefuse = new MaterialDialog.Builder(context)
             .title("权限申请").content(message).negativeText("稍后再试").positiveText("现在设置")
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.dismiss();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
                    context.startActivity(intent);
                }
            });
        builderRefuse.show();
    }
    public static void showScanResultDialog( Context context,String content){
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(context);
        mBuilder.title("温馨提示：")
            .content(content)
            .contentColor(Color.parseColor("#000000"))
            .canceledOnTouchOutside(false)
            .positiveText("确定");
        //.negativeText("取消");
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }
    //private  void showStringsDialog(int strings, Context context){
    //    MaterialDialog.Builder mBuilder;
    //    MaterialDialog mMaterialDialog;
    //    mBuilder = new MaterialDialog.Builder(context);
    //    mBuilder.title("温馨提示：")
    //        .content(getResources().getString(strings))
    //        .contentColor(Color.parseColor("#000000"))
    //        .canceledOnTouchOutside(false)
    //        .positiveText("确定");
    //    //.negativeText("取消");
    //    mMaterialDialog = mBuilder.build();
    //    mMaterialDialog.show();
    //}
}
