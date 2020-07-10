package com.shmedo.mcloudapp.deviceconfig.ui.activity.advanced;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.util.KeyBordUtils;
import com.shmedo.mcloudapp.util.TimeUtil;
import com.shmedo.mcloudapp.views.TimePickerDialog;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device.config
 * 文件名:   ProductRegistrationActivity
 * 创建者:   dpc
 * 创建时间:  2019/2/18 16:32
 * 描述：    产品注册页面
 */
public class ProductRegistrationActivity extends BaseActivity {

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.tv_date)
    TextView mTvDate;

    @BindView(R.id.et_origin_author)
    EditText mEtOriginAuthor;

    @BindView(R.id.et_new_author)
    EditText mEtNewAuthor;

    @BindView(R.id.btn_confirm_registered)
    Button mBtnConfirmRegistered;

    private MaterialDialog.Builder mBuilder;
    private MaterialDialog mMaterialDialog;

    private TimePickerDialog timeDialog;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ProductRegistrationActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_product_registration;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        initView();
    }


    private void initView() {
        mToolbarTitle.setText("产品注册");
        mTvDate.setText(TimeUtil.getCurrentTime());
    }


    @OnClick({R.id.iv_question, R.id.btn_confirm_registered,R.id.tv_date})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_question:
                showQuestionDialog();
                break;

            case R.id.btn_confirm_registered:
                processRegister();
                break;
            case R.id.tv_date:
                timeDialog = null ;
                timeDialog = new TimePickerDialog(this);
                timeDialog.setTimeLisinter(mTvDate);
                timeDialog.build();
                break;
        }
    }


    /**
     * 修改hint字体大小
     *
     * @param content
     * @param editText
     */
    public static void modifyHintText(String content, EditText editText) {
        if (editText == null) {
            return;
        }
        SpannableString spannableString = new SpannableString(content);
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(13, true);
        spannableString.setSpan(absoluteSizeSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        editText.setHint(new SpannedString(spannableString));
    }

    private void showQuestionDialog() {
        mBuilder = new MaterialDialog.Builder(this);
        mBuilder.customView(R.layout.dialog_registered_apply, false)
                .title("注册码申请表：")
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false);
        mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();

        final EditText etEntityName = (EditText) mMaterialDialog.findViewById(R.id.et_entity_name);
        final EditText etDeviceSN = (EditText) mMaterialDialog.findViewById(R.id.et_device_sn);
        final EditText etName = (EditText) mMaterialDialog.findViewById(R.id.et_name);
        final EditText etContactPhone = (EditText) mMaterialDialog.findViewById(R.id.et_contact_phone);

        Button btnCancel = (Button) mMaterialDialog.findViewById(R.id.btn_cancel);
        Button btnConfirmSend = (Button) mMaterialDialog.findViewById(R.id.btn_confirm_send);
        modifyHintText("请输入单位名称", etEntityName);
        modifyHintText("请输入设备SN号", etDeviceSN);
        modifyHintText("请输入姓名", etName);
        modifyHintText("请输入联系电话", etContactPhone);

        btnConfirmSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(etEntityName.getText().toString().trim())) {
                    ToastUtils.show("单位名称不能为空");
                    return;
                }

                if (TextUtils.isEmpty(etDeviceSN.getText().toString().trim())) {
                    ToastUtils.show("设备SN号不能为空");
                    return;
                }

                if (TextUtils.isEmpty(etName.getText().toString().trim())) {
                    ToastUtils.show("姓名不能为空");
                    return;
                }

                if (TextUtils.isEmpty(etContactPhone.getText().toString().trim())) {
                    ToastUtils.show("联系电话不能为空");
                    return;
                }

                //TODO  调用注册码申请接口
                ToastUtils.show("功能开发中...");
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                KeyBordUtils.hideSoftKeyboard(etEntityName);
                mMaterialDialog.dismiss();
                mMaterialDialog = null;
                mBuilder = null;
            }
        });
    }

    private void processRegister() {
        if (TextUtils.isEmpty(mEtNewAuthor.getText().toString().trim())) {
            ToastUtils.show("注册码不能为空");
            return;
        }

        if (TextUtils.isEmpty(mEtOriginAuthor.getText().toString().trim())) {
            ToastUtils.show("确认注册码不能为空");
            return;
        }

        if (!mEtNewAuthor.getText().toString().trim().equals(mEtOriginAuthor.getText().toString().trim())) {
            ToastUtils.show("输入的两次注册码不一样");
            return;
        }

       //TODO  调用注册接口
    }
}
