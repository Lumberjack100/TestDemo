package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.cmd.entity.InstallLocationEntity;
import com.shmedo.configlibrary.ble.cmd.entity.WorkModeEntity;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.SetRemoteUpgrade;
import com.shmedo.configlibrary.ble.enums.WorkModel;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.advanced.InstructionDebugActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.advanced.LogPrintActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.advanced.ProductRegistrationActivity;
import com.shmedo.mcloudapp.entity.SyncPositionBean;
import com.shmedo.core.event.BluetoothConnectStateEvent;
import com.shmedo.mcloudapp.util.AdvanceSetDialogUtils;
import com.shmedo.mcloudapp.util.FileUtils;
import com.shmedo.mcloudapp.util.KeyBordUtils;
import com.shmedo.mcloudapp.util.LocationUtils;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class DeviceAdvanceSettingActivity extends BaseDeviceConnectActivity {
    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.sw_firmware_upgrade)
    SwitchButton mSbFirmwareUpgrade;

    @BindView(R.id.sp_debug_mode)
    Spinner mSpDebugMode;

    private int debugModeCheck = 0;//标志位，Avoid onItemSelected calls during initialization

    public static void startActivity(Context context, int mode) {
        Intent intent = new Intent(context, DeviceAdvanceSettingActivity.class);
        intent.putExtra(AppContants.Extras.DEBUG_MODE, mode);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_device_advance_setting;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("高级配置");
        setSwitchViewListener();
        initSpinnerAdapter();
        initData();
    }

    private void initData() {
        int mode = getIntent().getIntExtra(AppContants.Extras.DEBUG_MODE, 1);
        //设备调试模式
        switch (WorkModel.valueOf(mode)) {
            case INITIALZE:
                mSpDebugMode.setSelection(0);
                break;

            case WORK:
                mSpDebugMode.setSelection(1);
                break;

            case DEBUG:
                mSpDebugMode.setSelection(2);
                break;

            case INFO:
                mSpDebugMode.setSelection(3);
                break;

            default:
                mSpDebugMode.setSelection(1);
                break;
        }
    }

    /**
     * switch按钮事件
     */
    private void setSwitchViewListener() {
        mSbFirmwareUpgrade.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    mSbFirmwareUpgrade.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                if (isChecked) {
                    AdvanceSetDialogUtils.showReStartDialog(DeviceAdvanceSettingActivity.this, "固件升级", "固件", mSbFirmwareUpgrade);
                } else {
                    setSetRemoteUpgrade(SetRemoteUpgrade.CLOSE_UPGRADE_MODEL, null, 0);
                    ToastUtils.show("关闭固件升级");
                }
            }
        });
    }

    private void initSpinnerAdapter() {
        //工作模式
        String[] workMode = getResources().getStringArray(R.array.das_work_mode);
        ArrayAdapter<String> debugModeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, workMode);
        debugModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpDebugMode.setAdapter(debugModeAdapter);
        mSpDebugMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                debugModeCheck++;
                if (debugModeCheck >= 2) {
                    String status = parent.getSelectedItem().toString();
                    WorkModel workModel;
                    switch (status) {
                        case "初始化":
                            workModel = WorkModel.INITIALZE;
                            break;

                        case "工作":
                            workModel = WorkModel.WORK;
                            break;

                        case "DEBUG":
                            workModel = WorkModel.DEBUG;
                            break;

                        case "INFO":
                            workModel = WorkModel.INFO;
                            break;

                        default:
                            workModel = WorkModel.WORK;
                            break;
                    }
                    WorkModeEntity workModeEntity = new WorkModeEntity(workModel.toInt());
                    String command = CommandManager.getInstance().getCommand(CommandType.WORK_MODE, workModeEntity);
                    sendCommonCommandImmediately(command);
                    Timber.d("设置工作模式指令==%s", command);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    @OnClick({R.id.back, R.id.ll_reset_data, R.id.ll_restart_system, R.id.rl_modify_authorization,
            R.id.rl_product_register, R.id.rl_instruction_debug, R.id.rl_log_print, R.id.rl_sync_position})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                break;

            case R.id.ll_reset_data://恢复出厂设置
                if (checkIsBluetoothConnected()) {
                    AdvanceSetDialogUtils.showRestoreDataDialog(this);
                }
                break;

            case R.id.ll_restart_system://重启系统
                if (checkIsBluetoothConnected()) {
                    AdvanceSetDialogUtils.showReStartDialog(this, "重启系统", "重启", null);
                }
                break;

            case R.id.rl_modify_authorization: //修改授权码
                if (checkIsBluetoothConnected()) {
                    AdvanceSetDialogUtils.showModifyAuthorizationDialog(this);
                }
                break;

            case R.id.rl_product_register://产品注册
                if (checkIsBluetoothConnected()) {
                    ProductRegistrationActivity.startActivity(DeviceAdvanceSettingActivity.this);
                }
                break;

            case R.id.rl_instruction_debug://指令交互调试模式
                if (checkIsBluetoothConnected()) {
                    InstructionDebugActivity.startActivity(DeviceAdvanceSettingActivity.this);
                }
                break;

            case R.id.rl_log_print://日志输出
                if (checkIsBluetoothConnected()) {
                    requestPermissionForSaveLog();
                }
                break;

            case R.id.rl_sync_position: //同步安装位置
                if (checkIsBluetoothConnected()) {
                    showSyncPositionDialog(DeviceAdvanceSettingActivity.this);
                }
                break;
        }
    }

    private boolean checkIsBluetoothConnected() {
        if (!MCloudApp.isIsBluetoothDeviceConnected()) {
            ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
            return false;
        }

        return true;
    }

    private void requestPermissionForSaveLog() {
        if (!FileUtils.externalAvailable()) {
            ToastUtils.show(getString(R.string.operation_failed_without_sdcard));
            return;
        }

        XPermissionUtils.requestPermissionsResult(this, 200, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        LogPrintActivity.startActivity(DeviceAdvanceSettingActivity.this);
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        boolean allNeverAskAgain = XPermissionUtils.isAllNeverAskAgain(DeviceAdvanceSettingActivity.this, deniedPermissions);
                        // 所有的权限都被勾上不再询问时，跳转到应用设置界面，引导用户手动打开权限
                        if (allNeverAskAgain) {
                            XPermissionUtils.showRefusePermissionDialog(DeviceAdvanceSettingActivity.this, GlobalUtil.getString(R.string.message_permission_storage_rationale));
                        } else {
                            ToastUtils.show(GlobalUtil.getString(R.string.message_permission_storage_denied));
                        }
                    }
                });
    }

    private MaterialDialog.Builder mBuilder;
    private MaterialDialog mMaterialDialog;
    private String address;
    private String latLong;
    private EditText etPositionInfo;
    private TextView tvLatLong;

    private void showSyncPositionDialog(BaseDeviceConnectActivity activity) {
        mBuilder = new MaterialDialog.Builder(activity);
        mBuilder.customView(R.layout.dialog_sync_position, false)
                .title("同步安装位置")
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false);
        mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();

        etPositionInfo = (EditText) mMaterialDialog.findViewById(R.id.et_position_info);
        ImageView imgPosition = (ImageView) mMaterialDialog.findViewById(R.id.img_position);
        tvLatLong = (TextView) mMaterialDialog.findViewById(R.id.lat_long);
        Button btnCancelRestart = (Button) mMaterialDialog.findViewById(R.id.btn_cancel_restart);
        Button btnRestartSystem = (Button) mMaterialDialog.findViewById(R.id.btn_restart_system);

        imgPosition.setOnClickListener(view -> {
            LocationUtils.getInstance().getPositionPermission(activity);
        });

        btnRestartSystem.setOnClickListener(view -> {
            String result = etPositionInfo.getText().toString().trim();
            if (!TextUtils.isEmpty(result)) {
                try {
                    InstallLocationEntity installLocationEntity = new InstallLocationEntity(1);
                    String command = CommandManager.getInstance().getCommand(CommandType.INSTALL_LOCATION, installLocationEntity);
                    activity.sendCommonCommandImmediately(command);
                    Timber.i("查询安装位置：%s", command);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                ToastUtils.show("位置信息不能为空");
                return;
            }
            ToastUtils.show("位置信息同步成功");
            KeyBordUtils.hideSoftKeyboard(etPositionInfo);
            mMaterialDialog.dismiss();
            mMaterialDialog = null;
            mBuilder = null;
            LocationUtils.getInstance().stopLocalService();
        });
        btnCancelRestart.setOnClickListener(view -> {
            KeyBordUtils.hideSoftKeyboard(etPositionInfo);
            mMaterialDialog.dismiss();
            mMaterialDialog = null;
            mBuilder = null;
            LocationUtils.getInstance().stopLocalService();
        });
    }


    /**
     * 设置显示数据
     */
    private void setResultData(String cmdStr) {
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType cmdType = StringUtil.extractCommandType(cmdStr);
        if (cmdType == CommandType.REBOOT_DEVICE) {
            if (tempStr.endsWith(CommandResult.ERROR_END)) {
                ToastUtils.show("发送重启指令错误!");
                stopProgressRunnable();
                return;
            }
            stopProgressRunnable();
            hander.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 3000);
            return;
        }

        if (cmdType == CommandType.SETTING_REMOTE_UPGRADE) {
            if (tempStr.endsWith(CommandResult.ERROR_END)) {
                ToastUtils.show("发送远程升级指令错误!");
                stopProgressRunnable();
                return;
            }
            stopProgressRunnable();
            ToastUtils.show("远程升级指令已发送");
            hander.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 3000);
            return;
        }
        if (cmdType == CommandType.RESTORE_FACTORY_SETTING) {
            if (tempStr.endsWith(CommandResult.ERROR_END)) {
                ToastUtils.show("发送恢复出厂设置指令错误!");
                stopProgressRunnable();
                return;
            }
            stopProgressRunnable();
            ToastUtils.show("指令已发送，设备即将恢复出厂设置");
            hander.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 3000);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (!TextUtils.isEmpty(messageEvent) && messageEvent.startsWith("$$")) {
            setResultData(messageEvent);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SyncPositionBean event) {
        Timber.i("==位置来了==%s", event.toString());
        if (event.getType().equals("location")) {
            address = event.getAddress();
            latLong = String.format(Locale.getDefault(), "%.6f", event.getLongitude()) + "," + String.format(Locale.getDefault(), "%.6f", event.getLatitude());
            etPositionInfo.setText(latLong);
            tvLatLong.setText(address);
            LocationUtils.getInstance().stopLocalService();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BluetoothConnectStateEvent bluetoothConnectStateEvent) {
        mSpDebugMode.setEnabled(bluetoothConnectStateEvent.isConnected);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
