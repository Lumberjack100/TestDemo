package com.shmedo.mcloudapp.ui.activity.device.config;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.ui.activity.device.config.senior.InstructionDebugActivity;
import com.shmedo.mcloudapp.ui.activity.device.config.senior.ProductRegistrationActivity;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.ToastUtil;
import java.util.Objects;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device.config
 * 文件名:   SeniorSettingActivity
 * 创建者:   dpc
 * 创建时间:  2019/2/15 15:23
 * 描述：    高级设置
 */
public class SeniorSettingActivity extends BaseActivity {
    @BindView(R.id.toolbar_title) TextView mToolbarTitle;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.ll_reset_data) LinearLayout mLlResetData;
    @BindView(R.id.ll_restart_system) LinearLayout mLlRestartSystem;
    @BindView(R.id.rl_modify_authorization) RelativeLayout mRlModifyAuthorization;
    @BindView(R.id.rl_product_register) RelativeLayout mRlProductRegister;
    @BindView(R.id.rl_instruction_debug) RelativeLayout mRlInstructionDebug;
    private MaterialDialog.Builder mBuilder;
    private MaterialDialog mMaterialDialog;
    private boolean prompt = true;
    @Override protected int initContentView() {
        return R.layout.activity_senior_setting;
    }


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }


    private void initView() {
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        mToolbarTitle.setText("高级设置");

    }


    @OnClick({ R.id.ll_reset_data, R.id.ll_restart_system, R.id.rl_modify_authorization,
                 R.id.rl_product_register, R.id.rl_instruction_debug })
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_reset_data:
                mBuilder = new MaterialDialog.Builder(this);
                mBuilder.title("温馨提示：")
                    .content("产品将恢复出厂设置状态，请确认是否继续？")
                    .contentColor(Color.parseColor("#000000"))
                    .canceledOnTouchOutside(false)
                    .positiveText("确定")
                    .negativeText("取消");
                mMaterialDialog = mBuilder.build();
                mMaterialDialog.show();
                mBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ToastUtil.showSToast("----");
                    }
                });
                break;
            case R.id.ll_restart_system:
                mBuilder = new MaterialDialog.Builder(this);
                mBuilder.title("温馨提示：")
                    .content("产品将重启，请确认是否继续？")
                    .contentColor(Color.parseColor("#000000"))
                    .canceledOnTouchOutside(false)
                    .positiveText("确定")
                    .negativeText("取消");
                mMaterialDialog = mBuilder.build();
                mMaterialDialog.show();
                mBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ToastUtil.showSToast("重启");
                    }
                });
                break;
            case R.id.rl_modify_authorization:

                mBuilder = new MaterialDialog.Builder(this);
                mBuilder.customView(R.layout.dialog_modify_authorization,false)
                    .title("修改授权码：")
                    .contentColor(Color.parseColor("#000000"))
                    .canceledOnTouchOutside(false);
                mMaterialDialog = mBuilder.build();
                mMaterialDialog.show();
                ImageView ivQuestion = (ImageView) mMaterialDialog.findViewById(R.id.iv_question);
                final EditText etOriginAuthor = (EditText) mMaterialDialog.findViewById(R.id.et_origin_author);
                final EditText etNewAuthor = (EditText) mMaterialDialog.findViewById(R.id.et_new_author);
                final EditText etConfirmAuthor = (EditText) mMaterialDialog.findViewById(R.id.et_confirm_author);
                final TextView tvPrompt = (TextView) mMaterialDialog.findViewById(R.id.tv_prompt);
                Button btnCancel = (Button) mMaterialDialog.findViewById(R.id.btn_cancel);
                Button btnConfirmModify = (Button) mMaterialDialog.findViewById(R.id.btn_confirm_modify);
                modityHint("请输入原授权码",etOriginAuthor);
                modityHint("请输入新的授权码",etNewAuthor);
                modityHint("请再次确认授权码",etConfirmAuthor);

                ivQuestion.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        if (prompt) {
                            tvPrompt.setVisibility(View.VISIBLE);
                            prompt = false;
                        }else {
                            tvPrompt.setVisibility(View.GONE);
                            prompt = true;
                        }
                    }
                });
                 btnConfirmModify.setOnClickListener(new View.OnClickListener() {
                     @Override public void onClick(View view) {
                         if (StringUtil.isNullOrEmpty(etOriginAuthor.getText().toString().trim())){
                             ToastUtil.showSToast("原授权码不能为空");
                         }else if (StringUtil.isNullOrEmpty(etNewAuthor.getText().toString().trim())){
                             ToastUtil.showSToast("新的授权码不能为空");
                         }else if (StringUtil.isNullOrEmpty(etConfirmAuthor.getText().toString().trim())){
                             ToastUtil.showSToast("确认的授权码不能为空");

                         }else {
                             ToastUtil.showSToast("确认修改");
                         }
                     }
                 });
                 btnCancel.setOnClickListener(new View.OnClickListener() {
                     @Override public void onClick(View view) {
                         mMaterialDialog.dismiss();
                     }
                 });
                break;
            case R.id.rl_product_register://产品注册
                Intent intent = new Intent(SeniorSettingActivity.this,ProductRegistrationActivity.class);
                startActivity(intent);

                break;
            case R.id.rl_instruction_debug://指令交互调试模式
                Intent in = new Intent(SeniorSettingActivity.this,InstructionDebugActivity.class);
                startActivity(in);
                break;
        }
    }


    /**
     * 修改hint字体大小
     * @param content
     * @param editText
     */
    public static void modityHint(String content,EditText editText){
        SpannableString ss = new SpannableString(content);
        AbsoluteSizeSpan ass = new AbsoluteSizeSpan(13,true);
        ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        editText.setHint(new SpannedString(ss));
    }
}
