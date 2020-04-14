package com.shmedo.mcloudapp.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseFragment;
import com.shmedo.mcloudapp.entity.SyncPositionBean;
import com.shmedo.mcloudapp.inter.Extras;
import com.shmedo.mcloudapp.ui.activity.ConfigDASActivity;
import com.shmedo.mcloudapp.ui.activity.device.senior.InstructionDebugActivity;
import com.shmedo.mcloudapp.ui.activity.device.senior.ProductRegistrationActivity;
import com.shmedo.mcloudapp.util.AdvanceSetDialogUtils;
import com.shmedo.mcloudapp.util.KeyBordUtils;
import com.shmedo.mcloudapp.util.LocationUtils;
import com.shmedo.mcloudapp.util.LogToSDUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

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

    private String snNumber;

    @Override
    protected int initContentView() {
        return R.layout.fragment_advance_set;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        getIntentData();
        initData();
        return view;
    }

    private void getIntentData() {
        Intent intent = getActivity().getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey(Extras.CUR_DEVICE_NAME)) {
            String deviceInfo = intent.getStringExtra(Extras.CUR_DEVICE_NAME);
            String[] scanData = deviceInfo.split(",");
            snNumber = scanData[1];
        }
        configDASActivity = (ConfigDASActivity) getActivity();
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
                    AdvanceSetDialogUtils.showReStartDialog(configDASActivity, "固件升级", "固件", swFirmwareUpgrade);
                } else {
                    configDASActivity.sendCommonCommand("##1200\r\n");
                    ToastUtils.show("关闭固件升级");
                }
            }
        });
    }

    @OnClick({R.id.ll_reset_data, R.id.ll_restart_system, R.id.rl_modify_authorization,
            R.id.rl_product_register, R.id.rl_instruction_debug, R.id.rl_log_print, R.id.rl_sync_position})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_reset_data://恢复出厂设置
                if (checkIsBluetoothConnected()) {
                    AdvanceSetDialogUtils.showRestoreDataDialog(configDASActivity);
                }
                break;

            case R.id.ll_restart_system://重启系统
                if (checkIsBluetoothConnected()) {
                    AdvanceSetDialogUtils.showReStartDialog(configDASActivity, "重启系统", "重启", null);
                }
                break;

            case R.id.rl_modify_authorization: //修改授权码
                if (checkIsBluetoothConnected()) {
                    AdvanceSetDialogUtils.showModifyAuthorizationDialog(configDASActivity);
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
                if (checkIsBluetoothConnected()) {
                    LogToSDUtil.requestPermissionForSaveLog(configDASActivity, snNumber);
                }
                break;
            case R.id.rl_sync_position: //同步安装位置
//                ToastUtils.show("功能开发中...");
                if (checkIsBluetoothConnected()) {
                    showSyncPositionDialog(configDASActivity);
                }
                break;
        }
    }

    private static MaterialDialog.Builder mBuilder;
    private static MaterialDialog mMaterialDialog;
    private static String address;
    private static String latLong;
    private static EditText etPositionInfo;
    private static TextView tvLatLong;

    private static void showSyncPositionDialog(ConfigDASActivity activity) {
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
                    String command = "##9161" + result + "\r\n";
                    activity.sendCommonCommandImmediately(command);
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SyncPositionBean event) {
        Timber.i("==位置来了==" + event.toString());
        if (event.getType().equals("location")) {
            address = event.getAddress();
            latLong = String.format(Locale.getDefault(),"%.6f", event.getLongitude()) + "," + String.format(Locale.getDefault(),"%.6f", event.getLatitude());
            etPositionInfo.setText(latLong);
            tvLatLong.setText(address);
            LocationUtils.getInstance().stopLocalService();
        }
    }


    private boolean checkIsBluetoothConnected() {
        if (!MCloudApp.isIsBluetoothDeviceConnected()) {
            ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
            return false;
        }

        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
