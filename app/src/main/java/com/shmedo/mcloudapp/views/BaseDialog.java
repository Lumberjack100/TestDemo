package com.shmedo.mcloudapp.views;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.views
 * 文件名:   BaseDialog
 * 创建者:   dpc
 * 创建时间:  2019/2/12 17:34
 * 描述：     dialog工具类 用于普通dialog的快速创建和使用
 *  默认显示在屏幕中间点击外部消失
 */
public abstract class BaseDialog {
    public static final String CANCLE = "BaseDialogCancle"; //用于设置取消点击事件
    private int layoutid;
    private Dialog baseDialog;
    protected int gravity = Gravity.BOTTOM;
    protected boolean canCelable = true;

    public Dialog getBaseDialog() {
        return baseDialog;
    }


    private Context context;
    private OnChildClickLisinter onChildClickLisinter;


    public BaseDialog(int layoutid, Context context) {
        this.layoutid = layoutid;
        this.context = context;
    }

    public BaseDialog(int layoutid, Context context, int grivity) {
        this.layoutid = layoutid;
        this.context = context;
        this.gravity = grivity;
    }


    public void build() {
        if (baseDialog == null) {
            baseDialog = new Dialog(context);
            baseDialog.setCancelable(canCelable);
            baseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            baseDialog.setContentView(layoutid);
            Window window = baseDialog.getWindow();
            window.setGravity(gravity);
            WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            manager.getDefaultDisplay().getMetrics(dm);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = dm.widthPixels;
            window.setAttributes(lp);
            //            baseDialog.getWindow().setDimAmount(0f);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ViewHelp viewUtils = new ViewHelp(this);
            updataView(viewUtils);
        }
        baseDialog.show();
    }

    protected abstract void updataView(ViewHelp helper);


    public void dismiss() {
        if (baseDialog != null) {
            baseDialog.dismiss();
        }
    }

    public OnChildClickLisinter getOnChildClickLisinter() {
        return onChildClickLisinter;
    }

    public void setOnChildClickLisinter(OnChildClickLisinter onChildClickLisinter) {
        this.onChildClickLisinter = onChildClickLisinter;
    }

    public interface OnChildClickLisinter {
        void onChildChildClick(Dialog dialog, View view);
    }

    public class ViewHelp {
        private BaseDialog dialog;

        public ViewHelp(BaseDialog dialog) {
            this.dialog = dialog;
        }

        public <T extends View> T getView(int viewid) {
            View view = dialog.getBaseDialog().findViewById(viewid);
            if (view == null) {
                throw new RuntimeException("Check Whether Your Id Is Correct or Not ");
            }
            return (T) view;
        }

        public View setText(@IdRes int viewId, CharSequence value) {
            TextView view = getView(viewId);
            view.setText(value);
            return view;
        }

        public View setText(@IdRes int viewId, @StringRes int strId) {
            TextView view = getView(viewId);
            view.setText(strId);
            return view;
        }

        public void addOnClickLisinter(@IdRes int viewId) {
            final View view = getView(viewId);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dialog.getOnChildClickLisinter() != null) {
                        if (CANCLE.equals(view.getTag())) {
                            dismiss();
                        } else {

                            dialog.getOnChildClickLisinter().onChildChildClick(baseDialog, view);
                        }

                    }
                }
            });
        }

    }
}

