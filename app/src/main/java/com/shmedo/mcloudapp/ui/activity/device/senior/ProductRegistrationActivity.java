package com.shmedo.mcloudapp.ui.activity.device.senior;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.ToastUtil;
import java.util.Objects;

import static com.shmedo.mcloudapp.ui.fragment.AdvanceSetFragment.modityHint;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device.config
 * 文件名:   ProductRegistrationActivity
 * 创建者:   dpc
 * 创建时间:  2019/2/18 16:32
 * 描述：    产品注册页面
 */
public class ProductRegistrationActivity extends BaseActivity {

    @BindView(R.id.toolbar_title) TextView mToolbarTitle;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.tv_date) TextView mTvDate;
    @BindView(R.id.et_origin_author) EditText mEtOriginAuthor;
    @BindView(R.id.et_new_author) EditText mEtNewAuthor;
    @BindView(R.id.btn_confirm_registered) Button mBtnConfirmRegistered;
    @BindView(R.id.iv_question) ImageView mIvQuestion;
    private MaterialDialog.Builder mBuilder;
    private MaterialDialog mMaterialDialog;


    @Override protected int initContentView() {
        return R.layout.activity_product_registration;
    }


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }


    private void initView() {
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        mToolbarTitle.setText("产品注册");
    }


    @OnClick({ R.id.iv_question, R.id.btn_confirm_registered })
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_question:
                mBuilder = new MaterialDialog.Builder(this);
                mBuilder.customView(R.layout.dialog_registered_apply,false)
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
                modityHint("请输入单位名称",etEntityName);
                modityHint("请输入设备SN号",etDeviceSN);
                modityHint("请输入姓名",etName);
                modityHint("请输入联系电话",etContactPhone);

                btnConfirmSend.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        if (StringUtil.isNullOrEmpty(etEntityName.getText().toString().trim())){
                            ToastUtil.showSToast("单位名称不能为空");
                        }else if (StringUtil.isNullOrEmpty(etDeviceSN.getText().toString().trim())){
                            ToastUtil.showSToast("设备SN号不能为空");
                        }else if (StringUtil.isNullOrEmpty(etName.getText().toString().trim())){
                            ToastUtil.showSToast("姓名不能为空");
                        }else if (StringUtil.isNullOrEmpty(etContactPhone.getText().toString().trim())){
                            ToastUtil.showSToast("联系电话不能为空");
                        }else {
                            ToastUtil.showSToast("确认发送");
                        }
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        mMaterialDialog.dismiss();
                    }
                });
                break;
            case R.id.btn_confirm_registered:
                if (StringUtil.isNullOrEmpty(mEtNewAuthor.getText().toString().trim())) {
                    ToastUtil.showSToast("注册码不能为空");
                } else if (StringUtil.isNullOrEmpty(mEtOriginAuthor.getText().toString().trim())) {
                    ToastUtil.showSToast("确认注册码不能为空");
                } else {
                    if (mEtNewAuthor.getText()
                        .toString().trim().equals(mEtOriginAuthor.getText().toString().trim())) {
                            finish();
                    } else {
                        ToastUtil.showSToast("输入的两次注册码不一样");
                    }
                }
                break;
        }
    }
}
