package com.shmedo.mcloudapp.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.*;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.OnClick;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.das.das.cmd.CommandManager;
import com.shmedo.das.das.cmd.CommandType;
import com.shmedo.das.das.cmd.entity.RebootDeviceEntity;
import com.shmedo.das.das.cmd.entity.SettingRemoteUpgradeEntity;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseFragment;
import com.shmedo.mcloudapp.ui.activity.ConfigDASActivity;
import com.shmedo.mcloudapp.ui.activity.device.senior.InstructionDebugActivity;
import com.shmedo.mcloudapp.ui.activity.device.senior.LogPrintActivity;
import com.shmedo.mcloudapp.ui.activity.device.senior.ProductRegistrationActivity;
import com.shmedo.mcloudapp.util.KeyBordUtils;
import com.shmedo.mcloudapp.util.StringUtil;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.fragment
 * 文件名:   AdvanceSetFragment
 * 创建者:   dpc
 * 创建时间:  2019/3/12 17:10
 * 描述：   高级设置
 */
public class AdvanceSetFragment extends BaseFragment {

    @BindView(R.id.sw_firmware_upgrade)
    SwitchButton swFirmwareUpgrade;

    private ConfigDASActivity configDASActivity;

    private MaterialDialog.Builder mBuilder;
    private MaterialDialog mMaterialDialog;
    private boolean isShowPrompt = true;


    @Override
    protected int initContentView() {
        return R.layout.fragment_advance_set;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        configDASActivity = (ConfigDASActivity) getActivity();
        initData();
        return view;
    }

    private void initData() {
        swFirmwareUpgrade.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    swFirmwareUpgrade.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                if (isChecked) {
                    showReStartDialog("固件升级","固件");
                } else {
                    configDASActivity.sendCommonCommand("##1200\r\n");
                    ToastUtils.show("关闭固件升级");
                }
            }
        });
    }

    @OnClick({R.id.ll_reset_data, R.id.ll_restart_system, R.id.rl_modify_authorization,
            R.id.rl_product_register, R.id.rl_instruction_debug, R.id.rl_log_print})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_reset_data://恢复出厂设置
                if (checkIsBluetoothConnected()) {
                    showRestoreDataDialog();
                }
                break;

            case R.id.ll_restart_system://重启系统
                if (checkIsBluetoothConnected()) {
                    showReStartDialog("重启系统","重启");
                }
                break;

            case R.id.rl_modify_authorization: //修改授权码
                if (checkIsBluetoothConnected()) {
                    showModifyAuthorizationDialog();
                }
                break;

            case R.id.rl_product_register://产品注册
                if (checkIsBluetoothConnected()) {
                    Intent intent = new Intent(getActivity(), ProductRegistrationActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.rl_instruction_debug://指令交互调试模式
                if (checkIsBluetoothConnected()) {
                    InstructionDebugActivity.startActivity(getActivity());
                }
                break;

            case R.id.rl_log_print://日志输出
//                ToastUtils.show("功能开发中...");
                if (checkIsBluetoothConnected()) {
                    LogPrintActivity.startActivity(getActivity());
                }
                break;
        }
    }

    /**
     * 恢复出厂设置
     */
    private void showRestoreDataDialog() {
        mBuilder = new MaterialDialog.Builder(getActivity());
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
                String baseInfoCommand = CommandManager.getInstance().getCommand(CommandType.RESTORE_FACTORY_SETTING, null);
                configDASActivity.sendCommonCommand(baseInfoCommand);
                ToastUtils.show("指令已发送，设备即将恢复出厂设置");

                mMaterialDialog.dismiss();
                mMaterialDialog = null;
                mBuilder = null;
            }
        });
        mBuilder.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                mMaterialDialog.dismiss();
                mMaterialDialog = null;
                mBuilder = null;
            }
        });
    }

    /**
     * 重启系统
     */
    private void showReStartDialog(String title,String instructions) {
        mBuilder = new MaterialDialog.Builder(getActivity());
        mBuilder.customView(R.layout.dialog_restart_system, false)
                .title(title)
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false);
        mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
        LinearLayout llTime = (LinearLayout) mMaterialDialog.findViewById(R.id.ll_time);
        LinearLayout llPort = (LinearLayout) mMaterialDialog.findViewById(R.id.ll_port);
        final EditText etRestartTime = (EditText) mMaterialDialog.findViewById(R.id.et_restart_time);
        final EditText etPortNumber = (EditText) mMaterialDialog.findViewById(R.id.et_port_number);
        Button btnCancelRestart = (Button) mMaterialDialog.findViewById(R.id.btn_cancel_restart);
        Button btnRestartSystem = (Button) mMaterialDialog.findViewById(R.id.btn_restart_system);
        modifyHintText("最大四位数", etRestartTime);
        modifyHintText("最长支持5位数字", etPortNumber);
        //设置最大长度
        etRestartTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        etPortNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        if (instructions.equals("重启")){
            llTime.setVisibility(View.VISIBLE);
            llPort.setVisibility(View.GONE);
        }else if (instructions.equals("固件")){
            llTime.setVisibility(View.GONE);
            llPort.setVisibility(View.VISIBLE);
        }
        btnRestartSystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (instructions.equals("重启")){
                    String time = etRestartTime.getText().toString().trim();
                    if (TextUtils.isEmpty(time)) {
                        ToastUtils.show("重启时间不能为空");
                        return;
                    }
                    if (!StringUtil.isNumeric(time)) {
                        ToastUtils.show("重启时间格式只能为数字");
                        return;
                    }
                    RebootDeviceEntity rebootDeviceEntity = new RebootDeviceEntity(Integer.valueOf(time));
                    String command = CommandManager.getInstance().getCommand(CommandType.REBOOT_DEVICE, rebootDeviceEntity);
                    configDASActivity.sendCommonCommand(command);
                    ToastUtils.show("指令已发送，设备将在 " + time + "s 后重启");

                    KeyBordUtils.hideSoftKeyboard(etRestartTime);
                }else if (instructions.equals("固件")){
                    //升级固件
                    String port = etPortNumber.getText().toString().trim();
                    if (TextUtils.isEmpty(port)) {
                        ToastUtils.show("端口号不能为空");
                        return;
                    }
                    if (!StringUtil.isNumeric(port)) {
                        ToastUtils.show("端口号格式只能为数字");
                        return;
                    }
                    String command = "##1201"+port+"\r\n";
                    configDASActivity.sendCommonCommand(command);
                    ToastUtils.show("指令已发送");
                    KeyBordUtils.hideSoftKeyboard(etPortNumber);
                }

                mMaterialDialog.dismiss();
                mMaterialDialog = null;
                mBuilder = null;
            }
        });
        btnCancelRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (instructions.equals("重启")){
                    KeyBordUtils.hideSoftKeyboard(etRestartTime);
                }else if (instructions.equals("固件")){
                    swFirmwareUpgrade.setCheckedImmediatelyNoEvent(false);
                    KeyBordUtils.hideSoftKeyboard(etPortNumber);
                }
                mMaterialDialog.dismiss();
                mMaterialDialog = null;
                mBuilder = null;
            }
        });
    }


    /**
     * 修改授权码
     */
    private void showModifyAuthorizationDialog() {
        mBuilder = new MaterialDialog.Builder(getActivity());
        mBuilder.customView(R.layout.dialog_modify_authorization, false)
                .title("修改授权码")
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
        modifyHintText("请输入原授权码", etOriginAuthor);
        modifyHintText("请输入新的授权码", etNewAuthor);
        modifyHintText("请再次确认授权码", etConfirmAuthor);

        ivQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvPrompt.setVisibility(isShowPrompt ? View.VISIBLE : View.GONE);
                isShowPrompt = !isShowPrompt;
            }
        });
        btnConfirmModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(etOriginAuthor.getText().toString().trim())) {
                    ToastUtils.show("原授权码不能为空");
                    return;
                }

                if (TextUtils.isEmpty(etNewAuthor.getText().toString().trim())) {
                    ToastUtils.show("新的授权码不能为空");
                    return;
                }

                if (TextUtils.isEmpty(etConfirmAuthor.getText().toString().trim())) {
                    ToastUtils.show("确认的授权码不能为空");
                    return;
                }

                //TODO 调用修改授权码接口
                ToastUtils.show("功能开发中...");
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyBordUtils.hideSoftKeyboard(etOriginAuthor);
                mMaterialDialog.dismiss();
                mMaterialDialog = null;
                mBuilder = null;
            }
        });
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

    private boolean checkIsBluetoothConnected() {
        if (!MCloudApp.isIsBluetoothDeviceConnected()) {
            ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
            return false;
        }

        return true;
    }


    @Override
    public boolean onBackPressed() {
        return false;
    }
}
