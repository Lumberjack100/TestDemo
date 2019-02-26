package com.shmedo.mcloudapp.ui.activity.device.config;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.util.UserConfig;
import java.util.Objects;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device.config
 * 文件名:   DisplacementConfigActivity
 * 创建者:   dpc
 * 创建时间:  2019/2/20 09:45
 * 描述：    传感器参数配置
 */
public class DisplacementConfigActivity extends BaseActivity {
    @BindView(R.id.toolbar_title) TextView mToolbarTitle;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.iv_stay1) ImageView mIvStay1;
    @BindView(R.id.sw_stay1) Switch mSwStay1;
    @BindView(R.id.tv_stay1) TextView mTvStay1;
    @BindView(R.id.iv_stay2) ImageView mIvStay2;
    @BindView(R.id.sw_stay2) Switch mSwStay2;
    @BindView(R.id.tv_stay2) TextView mTvStay2;
    @BindView(R.id.iv_stay3) ImageView mIvStay3;
    @BindView(R.id.sw_stay3) Switch mSwStay3;
    @BindView(R.id.tv_stay3) TextView mTvStay3;
    @BindView(R.id.iv_stay4) ImageView mIvStay4;
    @BindView(R.id.sw_stay4) Switch mSwStay4;
    @BindView(R.id.tv_stay4) TextView mTvStay4;
    @BindView(R.id.iv_stay5) ImageView mIvStay5;
    @BindView(R.id.sw_stay5) Switch mSwStay5;
    @BindView(R.id.tv_stay5) TextView mTvStay5;
    @BindView(R.id.iv_stay6) ImageView mIvStay6;
    @BindView(R.id.sw_stay6) Switch mSwStay6;
    @BindView(R.id.tv_stay6) TextView mTvStay6;
    @BindView(R.id.iv_stay7) ImageView mIvStay7;
    @BindView(R.id.sw_stay7) Switch mSwStay7;
    @BindView(R.id.tv_stay7) TextView mTvStay7;
    @BindView(R.id.iv_stay8) ImageView mIvStay8;
    @BindView(R.id.sw_stay8) Switch mSwStay8;
    @BindView(R.id.tv_stay8) TextView mTvStay8;
    private MaterialDialog.Builder mBuilder;
    private MaterialDialog mMaterialDialog;
    private Dialog mDialog;
    private UserConfig uc;


    @Override protected int initContentView() {
        return R.layout.activity_displacement_config;
    }


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }
    private void initView() {
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        mToolbarTitle.setText("拉线位移计配置");
        uc = UserConfig.getConfig(this, CommonVariable.USER_CONFIG_NAME);
    }

    private void initData(){
        checkSwitchColor(mTvStay1,mSwStay1);
        checkSwitchColor(mTvStay2,mSwStay2);
        checkSwitchColor(mTvStay3,mSwStay3);
        checkSwitchColor(mTvStay4,mSwStay4);
        checkSwitchColor(mTvStay5,mSwStay5);
        checkSwitchColor(mTvStay6,mSwStay6);
        checkSwitchColor(mTvStay7,mSwStay7);
        checkSwitchColor(mTvStay8,mSwStay8);
        mSwStay1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkSwitchColorbg(b,mTvStay1,mSwStay1);
            }
        });
        mSwStay2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkSwitchColorbg(b,mTvStay2,mSwStay2);
            }
        });
        mSwStay3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkSwitchColorbg(b,mTvStay3,mSwStay3);
            }
        });
        mSwStay4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkSwitchColorbg(b,mTvStay4,mSwStay4);
            }
        });
        mSwStay5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkSwitchColorbg(b,mTvStay5,mSwStay5);
            }
        });
        mSwStay6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkSwitchColorbg(b,mTvStay6,mSwStay6);
            }
        });
        mSwStay7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkSwitchColorbg(b,mTvStay7,mSwStay7);
            }
        });
        mSwStay8.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkSwitchColorbg(b,mTvStay8,mSwStay8);
            }
        });
    }

    @OnClick({ R.id.iv_stay1, R.id.iv_stay2, R.id.iv_stay3, R.id.iv_stay4, R.id.iv_stay5,
                 R.id.iv_stay6, R.id.iv_stay7, R.id.iv_stay8 })
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_stay1:
                showDialog("stay1");
                break;
            case R.id.iv_stay2:
                showDialog("stay2");
                break;
            case R.id.iv_stay3:
                showDialog("stay3");
                break;
            case R.id.iv_stay4:
                showDialog("stay4");
                break;
            case R.id.iv_stay5:
                showDialog("stay5");
                break;
            case R.id.iv_stay6:
                showDialog("stay6");
                break;
            case R.id.iv_stay7:
                showDialog("stay7");
                break;
            case R.id.iv_stay8:
                showDialog("stay8");
                break;
        }
    }

    private void checkSwitchColor(TextView mTvStay,Switch mSwStay){
        if (mSwStay.isChecked()){
            mTvStay.setText("已启用");
            mTvStay.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }else {
            mTvStay.setText("已停用");
            mTvStay.setBackgroundColor(getResources().getColor(R.color.secondary_text));
        }
    }

    private void checkSwitchColorbg(boolean b, final TextView mTvStay, final Switch mSwStay){
        if (b){
            mSwStay.setChecked(true);
            mTvStay.setText("已启用");
            mTvStay.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }else {
            mBuilder = new MaterialDialog.Builder(this);
            mBuilder.title("温馨提示：")
                .content("确认要停用该传感器吗？")
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消");
            mMaterialDialog = mBuilder.build();
            mMaterialDialog.show();
            mBuilder.onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    mSwStay.setChecked(true);
                    mTvStay.setText("已启用");
                    mTvStay.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                }
            });
            mBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    mSwStay.setChecked(false);
                    mTvStay.setText("已停用");
                    mTvStay.setBackgroundColor(getResources().getColor(R.color.secondary_text));
                }
            });

        }
    }




    private void showDialog( String tag) {
        if (mDialog == null) {
            initShareDialog(tag);
        }
        mDialog.show();
    }

    /**
     * 初始化分享弹出框
     */
    private void initShareDialog(final String tag) {
        mDialog = new Dialog(this, R.style.dialog_bottom_full);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setCancelable(false);
        Window window = mDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.share_animation);
        View view = View.inflate(this, R.layout.dialog_displacement_config, null);
        final EditText modbusAddress = view.findViewById(R.id.et_modbus_address);
        final EditText warnValue = view.findViewById(R.id.et_warn_value);
        final EditText revised = view.findViewById(R.id.et_revised);
        final EditText noteInformation = view.findViewById(R.id.et_note_information);



        String modbusAddress1 = null;
        String warnValue1 = null;
        String revised1 = null;
        String noteInformation1 = null;
        switch (tag){
            case "stay1":
                modbusAddress1 = uc.readString("modbusAddress1");
                warnValue1=uc.readString("warnValue1");
                revised1=uc.readString("revised1");
                noteInformation1 = uc.readString("noteInformation1");
                break;
            case "stay2":
                modbusAddress1 = uc.readString("modbusAddress2");
                warnValue1=uc.readString("warnValue2");
                revised1=uc.readString("revised2");
                noteInformation1 = uc.readString("noteInformation2");
                break;
            case "stay3":
                modbusAddress1 = uc.readString("modbusAddress3");
                warnValue1=uc.readString("warnValue3");
                revised1=uc.readString("revised3");
                noteInformation1 = uc.readString("noteInformation3");
                break;
            case "stay4":
                modbusAddress1 = uc.readString("modbusAddress4");
                warnValue1=uc.readString("warnValue4");
                revised1=uc.readString("revised4");
                noteInformation1 = uc.readString("noteInformation4");
                break;
            case "stay5":
                modbusAddress1 = uc.readString("modbusAddress5");
                warnValue1=uc.readString("warnValue5");
                revised1=uc.readString("revised5");
                noteInformation1 = uc.readString("noteInformation5");
                break;
            case "stay6":
                modbusAddress1 = uc.readString("modbusAddress6");
                warnValue1=uc.readString("warnValue6");
                revised1=uc.readString("revised6");
                noteInformation1 = uc.readString("noteInformation6");
                break;
            case "stay7":
                modbusAddress1 = uc.readString("modbusAddress7");
                warnValue1=uc.readString("warnValue7");
                revised1=uc.readString("revised7");
                noteInformation1 = uc.readString("noteInformation7");
                break;
            case "stay8":
                modbusAddress1 = uc.readString("modbusAddress8");
                warnValue1=uc.readString("warnValue8");
                revised1=uc.readString("revised8");
                noteInformation1 = uc.readString("noteInformation8");
                break;

        }
        modbusAddress.setText( modbusAddress1);
        warnValue.setText(warnValue1);
        revised.setText(revised1);
        noteInformation.setText(noteInformation1);
        view.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                    mDialog = null;
                }
            }
        });


        view.findViewById(R.id.tv_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (StringUtil.isNullOrEmpty(modbusAddress.getText().toString().trim())) {
                    ToastUtil.showSToast("Modbus地址不能为空");
                }else if(StringUtil.isNullOrEmpty(warnValue.getText().toString().trim())){
                    ToastUtil.showSToast("报警值不能为空");
                }else if(StringUtil.isNullOrEmpty(revised.getText().toString().trim())){
                    ToastUtil.showSToast("修正值不能为空");
                }else if(StringUtil.isNullOrEmpty(noteInformation.getText().toString().trim())){
                    ToastUtil.showSToast("备注信息不能为空");
                }else {
                    final String modbusAddress2 = modbusAddress.getText().toString().trim();
                    final String warnValue2 = warnValue.getText().toString().trim();
                    final String revised2 = revised.getText().toString().trim();
                    final String noteInformation2 = noteInformation.getText().toString().trim();
                    switch (tag){
                        case "stay1":
                            uc.writeString("modbusAddress1",modbusAddress2);
                            uc.writeString("warnValue1",warnValue2);
                            uc.writeString("revised1",revised2);
                            uc.writeString("noteInformation1",noteInformation2);
                            checkSwitchColorbg(true,mTvStay1,mSwStay1);
                            break;
                        case "stay2":
                            uc.writeString("modbusAddress2",modbusAddress.getText().toString());
                            uc.writeString("warnValue2",warnValue.getText().toString());
                            uc.writeString("revised2",revised.getText().toString());
                            uc.writeString("noteInformation2",noteInformation.getText().toString());
                            checkSwitchColorbg(true,mTvStay2,mSwStay2);
                            break;
                        case "stay3":
                            uc.writeString("modbusAddress3",modbusAddress.getText().toString());
                            uc.writeString("warnValue3",warnValue.getText().toString());
                            uc.writeString("revised3",revised.getText().toString());
                            uc.writeString("noteInformation3",noteInformation.getText().toString());
                            checkSwitchColorbg(true,mTvStay3,mSwStay3);
                            break;
                        case "stay4":
                            uc.writeString("modbusAddress4",modbusAddress.getText().toString());
                            uc.writeString("warnValue4",warnValue.getText().toString());
                            uc.writeString("revised4",revised.getText().toString());
                            uc.writeString("noteInformation4",noteInformation.getText().toString());
                            checkSwitchColorbg(true,mTvStay4,mSwStay4);
                            break;
                        case "stay5":
                            uc.writeString("modbusAddress5",modbusAddress.getText().toString());
                            uc.writeString("warnValue5",warnValue.getText().toString());
                            uc.writeString("revised5",revised.getText().toString());
                            uc.writeString("noteInformation5",noteInformation.getText().toString());
                            checkSwitchColorbg(true,mTvStay5,mSwStay5);
                            break;
                        case "stay6":
                            uc.writeString("modbusAddress6",modbusAddress.getText().toString());
                            uc.writeString("warnValue6",warnValue.getText().toString());
                            uc.writeString("revised6",revised.getText().toString());
                            uc.writeString("noteInformation6",noteInformation.getText().toString());
                            checkSwitchColorbg(true,mTvStay6,mSwStay6);
                            break;
                        case "stay7":
                            uc.writeString("modbusAddress7",modbusAddress.getText().toString());
                            uc.writeString("warnValue7",warnValue.getText().toString());
                            uc.writeString("revised7",revised.getText().toString());
                            uc.writeString("noteInformation7",noteInformation.getText().toString());
                            checkSwitchColorbg(true,mTvStay7,mSwStay7);
                            break;
                        case "stay8":
                            uc.writeString("modbusAddress8",modbusAddress.getText().toString());
                            uc.writeString("warnValue8",warnValue.getText().toString());
                            uc.writeString("revised8",revised.getText().toString());
                            uc.writeString("noteInformation8",noteInformation.getText().toString());
                            checkSwitchColorbg(true,mTvStay8,mSwStay8);
                            break;
                    }
                    if (mDialog != null && mDialog.isShowing()) {
                        mDialog.dismiss();
                        mDialog = null;
                        modbusAddress.setText("");
                        warnValue.setText("");
                        revised.setText("");
                        noteInformation.setText("");
                    }
                }

            }
        });
        window.setContentView(view);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
    }



}
