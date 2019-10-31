package com.shmedo.mcloudapp.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.das.das.cmd.CommandManager;
import com.shmedo.das.das.cmd.CommandType;
import com.shmedo.das.das.cmd.entity.RebootDeviceEntity;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseFragment;
import com.shmedo.mcloudapp.bluetooth.Message;
import com.shmedo.mcloudapp.ui.activity.device.senior.InstructionDebugActivity;
import com.shmedo.mcloudapp.ui.activity.device.senior.ProductRegistrationActivity;
import com.shmedo.mcloudapp.util.KeyBordUtils;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.ToastUtil;

import java.util.UUID;

import butterknife.OnClick;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.fragment
 * 文件名:   AdvanceSetFragment
 * 创建者:   dpc
 * 创建时间:  2019/3/12 17:10
 * 描述：   高级设置
 */
public class AdvanceSetFragment extends BaseFragment {

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
        return view;
    }

    @OnClick({R.id.ll_reset_data, R.id.ll_restart_system, R.id.rl_modify_authorization, R.id.rl_product_register, R.id.rl_instruction_debug})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_reset_data:  //恢复出厂设置
                showResetDataDialog();
                break;

            case R.id.ll_restart_system://重启系统
                showReStartDialog();
                break;

            case R.id.rl_modify_authorization: //修改授权码
                showModifyAuthorizationDialog();
                break;

            case R.id.rl_product_register://产品注册
                Intent intent = new Intent(getActivity(), ProductRegistrationActivity.class);
                startActivity(intent);
                break;

            case R.id.rl_instruction_debug://指令交互调试模式
                InstructionDebugActivity.startActivity(getActivity());
                break;
        }
    }

    /**
     * 恢复出厂设置
     */
    private void showResetDataDialog() {
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
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtil.showShortToast("蓝牙未连接");
                    return;
                }

                String baseInfoCommand = CommandManager.getInstance().getCommand(CommandType.RESTORE_FACTORY_SETTING, null);
                Message msg = new Message(UUID.randomUUID().toString(), baseInfoCommand, true);
                if (DeviceFragment.mdBluetoothManager != null) {
                    DeviceFragment.mdBluetoothManager.writeMessage(msg);
                    ToastUtil.showLongToast("指令已发送，设备即将恢复出厂设置");
                }

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
    private void showReStartDialog() {
        mBuilder = new MaterialDialog.Builder(getActivity());
        mBuilder.customView(R.layout.dialog_restart_system, false)
                .title("重启系统")
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false);
        mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();

        final EditText etRestartTime = (EditText) mMaterialDialog.findViewById(R.id.et_restart_time);
        Button btnCancelRestart = (Button) mMaterialDialog.findViewById(R.id.btn_cancel_restart);
        Button btnRestartSystem = (Button) mMaterialDialog.findViewById(R.id.btn_restart_system);
        modifyHintText("最大四位数", etRestartTime);
        //设置最大长度
        etRestartTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        btnRestartSystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String time = etRestartTime.getText().toString().trim();
                if (TextUtils.isEmpty(time)) {
                    ToastUtil.showShortToast("重启时间不能为空");
                    return;
                }

                if (!StringUtil.isNumeric(time)) {
                    ToastUtil.showShortToast("重启时间格式不正确");
                    return;
                }

                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtil.showShortToast("蓝牙未连接");
                    return;
                }

                RebootDeviceEntity rebootDeviceEntity = new RebootDeviceEntity(Integer.valueOf(time));
                String command = CommandManager.getInstance().getCommand(CommandType.REBOOT_DEVICE, rebootDeviceEntity);

                Message msg = new Message(UUID.randomUUID().toString(), command, true);
                if (DeviceFragment.mdBluetoothManager != null) {
                    DeviceFragment.mdBluetoothManager.writeMessage(msg);
                    ToastUtil.showLongToast("指令已发送，设备将在 " + time + "s 后重启");
                }

                KeyBordUtils.hideSoftKeyboard(etRestartTime);
                mMaterialDialog.dismiss();
                mMaterialDialog = null;
                mBuilder = null;
            }
        });
        btnCancelRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyBordUtils.hideSoftKeyboard(etRestartTime);

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
                    ToastUtil.showShortToast("原授权码不能为空");
                    return;
                }

                if (TextUtils.isEmpty(etNewAuthor.getText().toString().trim())) {
                    ToastUtil.showShortToast("新的授权码不能为空");
                    return;
                }

                if (TextUtils.isEmpty(etConfirmAuthor.getText().toString().trim())) {
                    ToastUtil.showShortToast("确认的授权码不能为空");
                    return;
                }

                //TODO 调用修改授权码接口
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


    @Override
    public boolean onBackPressed() {
        return false;
    }
}
