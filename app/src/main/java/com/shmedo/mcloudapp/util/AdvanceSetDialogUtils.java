package com.shmedo.mcloudapp.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.*;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.das.das.cmd.CommandManager;
import com.shmedo.das.das.cmd.CommandType;
import com.shmedo.das.das.cmd.entity.RebootDeviceEntity;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.SyncPositionBean;
import com.shmedo.mcloudapp.entity.event.BluetoothStateEvent;
import com.shmedo.mcloudapp.ui.activity.ConfigDASActivity;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import timber.log.Timber;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util
 * 创建者:   dpc
 * 创建时间:  2019-12-24
 * 描述：    TODO
 */
public class AdvanceSetDialogUtils {
    @SuppressLint("StaticFieldLeak")
    private static MaterialDialog.Builder mBuilder;
    @SuppressLint("StaticFieldLeak")
    private static MaterialDialog mMaterialDialog;

    private static boolean isShowPrompt = true;


    /**
     * 固件升级 or 重启系统
     */
    public static void showReStartDialog(ConfigDASActivity activity, String title, String instructions, SwitchButton swFirmwareUpgrade) {
        mBuilder = new MaterialDialog.Builder(activity);
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
                    activity.sendCommonCommand(command);
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
                    activity.sendCommonCommand(command);
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
    public static void showModifyAuthorizationDialog(ConfigDASActivity activity) {
        mBuilder = new MaterialDialog.Builder(activity);
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
     * 恢复出厂设置
     */
    public static void showRestoreDataDialog(ConfigDASActivity activity) {
        mBuilder = new MaterialDialog.Builder(activity);
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
                activity.sendCommonCommand(baseInfoCommand);
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
}
