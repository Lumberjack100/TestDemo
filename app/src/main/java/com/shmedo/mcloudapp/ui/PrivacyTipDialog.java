package com.shmedo.mcloudapp.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.ui.activity.WebViewActivity;

import java.io.Serializable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/1 <br/>
 * 描述：    隐私权限提示
 */
public class PrivacyTipDialog extends DialogFragment {
    @BindView(R.id.tv_privacy_desc)
    TextView mTvPrivacyDesc;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setWindowStyle(Gravity.CENTER);
        View rootView = inflater.inflate(R.layout.fragment_privacy_dialog, container, false);
        ButterKnife.bind(this, rootView);
        initView();
        return rootView;
    }

    private void setWindowStyle(int gravity) {
        Dialog mDialog = getDialog();
        Window window = mDialog.getWindow();
        //无标题  必须放在setContextView之前调用
        window.requestFeature(Window.FEATURE_NO_TITLE);
        //Sets whether this dialog is canceled when touched outside the window's bounds.
        mDialog.setCanceledOnTouchOutside(false);
        //Sets whether this dialog is cancelable with the BACK key.
        setCancelable(false);
        window.setWindowAnimations(R.style.DialogFragmentAnimation);
        //window外可以点击,不拦截窗口外的事件
//        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        // 设置宽度为屏宽、靠近屏幕底部。
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = gravity;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
    }

    private void initView() {
        urlSpan();
    }


    private void urlSpan() {
        String text = getActivity().getResources().getString(R.string.privacy_agreement_desc);
        SpannableString spannableString = new SpannableString(text);
        int start1 = text.indexOf("《用户协议和免责条款》");
        int end1 = start1 + "《用户协议和免责条款》".length();
//        int start2 = text.indexOf("《隐私政策》");
//        int end2 = start2 + "《隐私政策》".length();
        spannableString.setSpan(new MyClickText(getActivity(), ContentType.USER_PROTOCOL), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        spannableString.setSpan(new MyClickText(getActivity(), ContentType.PRIVACY_POLICY), start2, end2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        //当然这里也可以通过setSpan来设置哪些位置的文本哪些颜色
        mTvPrivacyDesc.setText(spannableString);
        mTvPrivacyDesc.setMovementMethod(LinkMovementMethod.getInstance());//不设置 没有点击事件
        mTvPrivacyDesc.setHighlightColor(Color.TRANSPARENT); //设置点击后的颜色为透明
    }

    @OnClick({R.id.btn_agree, R.id.btn_deny})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_agree:
                doPositiveClick(view);
                break;

            case R.id.btn_deny:
                doNegativeClick(view);
                break;
        }
    }

    private void doPositiveClick(View view) {
        DialogFragmentClickListener listener = (DialogFragmentClickListener) getActivity();
        listener.onPositiveClick(view);
        dismiss();
    }

    private void doNegativeClick(View view) {
        DialogFragmentClickListener listener = (DialogFragmentClickListener) getActivity();
        listener.onNegativeClick(view);
        dismiss();
    }


    public interface DialogFragmentClickListener {
        void onPositiveClick(View view);

        void onNegativeClick(View view);
    }

    static class MyClickText extends ClickableSpan {
        private Context context;
        private ContentType contentType;

        public MyClickText(Context context, ContentType contentType) {
            this.context = context;
            this.contentType = contentType;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            //设置文本的颜色
            ds.setColor(context.getResources().getColor(R.color.colorPrimary));
            //超链接形式的下划线，false 表示不显示下划线，true表示显示下划线
            ds.setUnderlineText(false);
        }

        @Override
        public void onClick(View view) {
            if (contentType == ContentType.USER_PROTOCOL) {
                String url = "file:///android_asset/private/UserProtocol.html";
                WebViewActivity.startActivity(context, url);
            } else {
                String url = "file:///android_asset/private/PrivacyPolicy.html";
                WebViewActivity.startActivity(context, url);
            }
        }
    }

    public enum ContentType implements Serializable {
        USER_PROTOCOL,//用户协议
        PRIVACY_POLICY//隐私政策
    }
}
