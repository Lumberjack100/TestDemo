package com.shmedo.mcloudapp.ui.activity.device;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.das.common.QueryOsmometerParameterInfo;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.model.Extras;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import com.shmedo.mcloudapp.ui.fragment.AdvanceSetFragment;
import com.shmedo.mcloudapp.util.UserConfig;
import com.shmedo.mcloudapp.util.bleutil.BlueResultParserUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device.config
 * 文件名:   OsmometerConfigActivity
 * 创建者:   dpc
 * 创建时间:  2019/2/22 14:44
 * 描述：   渗压计功能配置
 */
public class OsmometerConfigActivity extends BaseDeviceConnectActivity {
    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.iv_osmometer_address)
    ImageView mIvOsmometerAddress;

    @BindView(R.id.et_osmometer_address)
    EditText mEtOsmometerAddress;

    @BindView(R.id.iv_water_alarm_value)
    ImageView mIvWaterAlarmValue;

    @BindView(R.id.et_water_alarm_value)
    EditText mEtWaterAlarmValue;

    @BindView(R.id.iv_water_revised)
    ImageView mIvWaterRevised;

    @BindView(R.id.et_water_revised)
    EditText mEtWaterRevised;

    @BindView(R.id.iv_osmometer_cord)
    ImageView mIvOsmometerCord;

    @BindView(R.id.et_osmometer_cord)
    EditText mEtOsmometerCord;

    @BindView(R.id.iv_nozzel_height)
    ImageView mIvNozzelHeight;

    @BindView(R.id.et_nozzel_height)
    EditText mEtNozzelHeight;

    @BindView(R.id.et_note)
    EditText mEtNote;

    @BindView(R.id.btn_confirm_complete)
    Button mBtnConfirmComplete;


    private String osmometerAddress;//渗压计地址
    private String depthTriggerValue;//深度触发值-水位报警值
    private String depthCorrection;//深度修正值
    private String osmometerLength;//渗压计绳长
    private String nozzelHeight;//管口高程

    private UserConfig uc;

    public static void startActivity(Context context, String osmometerParameterInfo) {
        Intent intent = new Intent(context, OsmometerConfigActivity.class);
        intent.putExtra(Extras.PARAM_CONFIG_INFO, osmometerParameterInfo);
        context.startActivity(intent);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_osmometer_config;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent();
    }

    private void parseIntent() {
        mToolbarTitle.setText("配置渗压计");
        AdvanceSetFragment.modifyHintText("随手一记，好记性不如烂笔头", mEtNote);

        uc = UserConfig.getConfig(this, CommonVariable.OSMOMETER_NOTE);
        mEtNote.setText(uc.readString(CommonVariable.OSMOMETER_NOTE));

        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey(Extras.PARAM_CONFIG_INFO)) {
            String configInfo = intent.getStringExtra(Extras.PARAM_CONFIG_INFO);
            if (!TextUtils.isEmpty(configInfo)) {
                QueryOsmometerParameterInfo queryOsmometerParameterInfo = BlueResultParserUtil.getQueryOsmometerParameterInfo(configInfo);
                if (queryOsmometerParameterInfo != null) {
                    mEtOsmometerAddress.setText(queryOsmometerParameterInfo.getOsmometerAddress());
                    mEtWaterAlarmValue.setText(String.valueOf(queryOsmometerParameterInfo.getDepthTrigger()));
                    mEtWaterRevised.setText(String.valueOf(queryOsmometerParameterInfo.getDepthCorrect()));
                    mEtOsmometerCord.setText(String.valueOf(queryOsmometerParameterInfo.getCordLenght()));
                    //TODO  安装高程
                    mEtNozzelHeight.setText("");
                }
            }
        }

    }


    @OnClick({R.id.back,R.id.iv_osmometer_address, R.id.iv_water_alarm_value, R.id.iv_water_revised,
            R.id.iv_osmometer_cord, R.id.iv_nozzel_height, R.id.btn_confirm_complete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                break;

            case R.id.iv_osmometer_address:
                showTipDialog(getResources().getString(R.string.osmometer_address));
                break;

            case R.id.iv_water_alarm_value:
                showTipDialog(getResources().getString(R.string.water_alarm_value));
                break;

            case R.id.iv_water_revised:
                showTipDialog(getResources().getString(R.string.water_revised));
                break;

            case R.id.iv_osmometer_cord:
                showTipDialog(getResources().getString(R.string.osmometer_cord));
                break;

            case R.id.iv_nozzel_height:
//                showTipDialog(getResources().getString(R.string.nozzel_height));
                showDialog();
                break;

            case R.id.btn_confirm_complete:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    return;
                }
                sendOsmometerConfig();
                break;
        }
    }

    private void showDialog() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this);
        mBuilder.title("温馨提示：")
                .customView(R.layout.dialog_test, false)
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false)
                .positiveText("确定");
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }


    private void sendOsmometerConfig() {
        osmometerAddress = mEtOsmometerAddress.getText().toString().trim();

        depthTriggerValue = mEtWaterAlarmValue.getText().toString().trim();

        depthCorrection = mEtWaterRevised.getText().toString().trim();

        osmometerLength = mEtOsmometerCord.getText().toString().trim();

        nozzelHeight = mEtNozzelHeight.getText().toString().trim();

        String note = mEtNote.getText().toString().trim();
        uc.writeString(CommonVariable.OSMOMETER_NOTE, note);


        if (TextUtils.isEmpty(osmometerAddress) || Integer.parseInt(osmometerAddress) <= 0 || Integer.parseInt(osmometerAddress) > 255) {
            ToastUtils.show("请输入正确的渗压计地址");
            return;
        }

        if (TextUtils.isEmpty(depthTriggerValue)) {
            ToastUtils.show("水位报警值不能为空");
            return;
        }

        if (TextUtils.isEmpty(depthCorrection)) {
            ToastUtils.show("水深修正值不能为空");
            return;
        }

        if (TextUtils.isEmpty(osmometerLength)) {
            ToastUtils.show("渗压计绳长不能为空");
            return;
        }

        if (TextUtils.isEmpty(nozzelHeight)) {
            ToastUtils.show("管口高程值不能为空");
            return;
        }

        String cmdOsmometerAddress = "##402" + osmometerAddress + "\r\n";
        String cmdDepthTriggerValue = "##403" + depthTriggerValue + ",0" + "\r\n";
        String cmdDepthCorrection = "##404" + depthCorrection + "," + nozzelHeight + "\r\n";
        String cmdOsmometerLength = "##405" + osmometerLength + "\r\n";
        String cmdNozzelHeight = "##405" + nozzelHeight + "\r\n";

        sendCommonCommand(cmdOsmometerAddress);
        Timber.d("发送设置数字渗压计地址指令===" + cmdOsmometerAddress);

        sendCommonCommand(cmdDepthTriggerValue);
        Timber.d("发送设置数字渗压计深度触发值，温度触发值指令===" + cmdDepthTriggerValue);

        sendCommonCommand(cmdDepthCorrection);
        Timber.d("发送设置数字渗压计深度修正值，温度修正值指令===" + cmdDepthCorrection);

        sendCommonCommand(cmdOsmometerLength);
        Timber.d("发送数字渗压计绳长指令===" + cmdOsmometerLength);

        sendCommonCommand(cmdNozzelHeight);
        Timber.d("发送数字渗压计安装高程指令===" + cmdNozzelHeight);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (!TextUtils.isEmpty(messageEvent) && messageEvent.startsWith("$$")) {
            Timber.d(messageEvent);
        }
    }

    @Override
    public void onBackPressed() {
        if (MCloudApp.isIsBluetoothDeviceConnected()) {
            if (isConfigChange) {
                isExitMode = true;
                showSaveDialog(getResources().getString(R.string.disconnect_bluetooth_device_save_param_warn));
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

}