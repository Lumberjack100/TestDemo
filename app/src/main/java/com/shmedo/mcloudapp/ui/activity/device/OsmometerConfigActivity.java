package com.shmedo.mcloudapp.ui.activity.device;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.model.QueryOsmometerParameterInfo;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.interfaces.Extras;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import com.shmedo.mcloudapp.util.AdvanceSetDialogUtils;
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

    private String cmdOsmometerAddress;//渗压计地址

    private String cmdDepthTriggerValue;//深度触发值-水位报警值

    private String cmdDepthCorrection;//深度修正值

    private String cmdOsmometerLength;//渗压计绳长

    private String cmdNozzelHeight;//管口高程


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
        setView();
        parseIntent();
    }

    private void setView(){
        mToolbarTitle.setText("配置渗压计");
        AdvanceSetDialogUtils.modifyHintText("随手一记，好记性不如烂笔头", mEtNote);

        mEtOsmometerAddress.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtOsmometerAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        mEtWaterAlarmValue.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtWaterAlarmValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        mEtWaterRevised.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtWaterRevised.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        mEtOsmometerCord.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtOsmometerCord.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        mEtNozzelHeight.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtNozzelHeight.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
    }

    private void parseIntent() {
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


    @OnClick({R.id.back, R.id.iv_osmometer_address, R.id.iv_water_alarm_value, R.id.iv_water_revised,
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
        String osmometerAddress = mEtOsmometerAddress.getText().toString().trim();

        String depthTriggerValue = mEtWaterAlarmValue.getText().toString().trim();

        String depthCorrection = mEtWaterRevised.getText().toString().trim();

        String osmometerLength = mEtOsmometerCord.getText().toString().trim();

        String nozzelHeight = mEtNozzelHeight.getText().toString().trim();

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

        cmdOsmometerAddress = "##402" + osmometerAddress + "\r\n";
        cmdDepthTriggerValue = "##403" + depthTriggerValue + ",1" + "\r\n";
        cmdDepthCorrection = "##404" + depthCorrection + "," + nozzelHeight + "\r\n";
        cmdOsmometerLength = "##405" + osmometerLength + "\r\n";
        cmdNozzelHeight = "##406" + nozzelHeight + "\r\n";

        startProgressRunnable("正在发送配置指令...", 10000);
        sendCommonCommandImmediately(cmdOsmometerAddress);
        Timber.d("发送设置数字渗压计地址指令===" + cmdOsmometerAddress);
    }

    /**
     * 设置显示数据
     */
    private void setResultData(String cmdStr) {
        if (cmdStr.startsWith("$$402") && cmdStr.endsWith("\r\n")) {
            if (cmdStr.startsWith("$$402e") || cmdStr.startsWith("$$402ce")) {
                ToastUtils.show("数字渗压计地址配置错误!");
                stopProgressRunnable();
                return;
            }
            sendCommonCommandImmediately(cmdDepthTriggerValue);
            Timber.d("发送设置数字渗压计深度触发值，温度触发值指令===" + cmdDepthTriggerValue);
            return;
        }

        if (cmdStr.startsWith("$$403") && cmdStr.endsWith("\r\n")) {
            if (cmdStr.startsWith("$$403e") || cmdStr.startsWith("$$403ce")) {
                ToastUtils.show("数字渗压计深度触发值配置错误!");
                stopProgressRunnable();
                return;
            }
            sendCommonCommandImmediately(cmdDepthCorrection);
            Timber.d("发送设置数字渗压计深度修正值，温度修正值指令===" + cmdDepthCorrection);
            return;
        }

        if (cmdStr.startsWith("$$404") && cmdStr.endsWith("\r\n")) {
            if (cmdStr.startsWith("$$404e") || cmdStr.startsWith("$$404ce")) {
                ToastUtils.show("数字渗压计深度修正值配置错误!");
                stopProgressRunnable();
                return;
            }
            sendCommonCommandImmediately(cmdOsmometerLength);
            Timber.d("发送数字渗压计绳长指令===" + cmdOsmometerLength);
            return;
        }

        if (cmdStr.startsWith("$$405") && cmdStr.endsWith("\r\n")) {
            if (cmdStr.startsWith("$$405e") || cmdStr.startsWith("$$405ce")) {
                ToastUtils.show("数字渗压计绳长配置错误!");
                stopProgressRunnable();
                return;
            }
            sendCommonCommandImmediately(cmdNozzelHeight);
            Timber.d("发送数字渗压计安装高程指令===" + cmdNozzelHeight);
            return;
        }

        if (cmdStr.startsWith("$$406") && cmdStr.endsWith("\r\n")) {
            if (cmdStr.startsWith("$$406e") || cmdStr.startsWith("$$406ce")) {
                ToastUtils.show("数字渗压计安装高程配置错误!");
                stopProgressRunnable();
                return;
            }
            stopProgressRunnable();
            ToastUtils.show("设置完成");
            hander.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 2000);
            return;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (!TextUtils.isEmpty(messageEvent) && messageEvent.startsWith("$$")) {
            setResultData(messageEvent);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}