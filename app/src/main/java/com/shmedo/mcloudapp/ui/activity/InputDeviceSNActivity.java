package com.shmedo.mcloudapp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.shmedo.core.MCloudApp;
import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.ConfigADMEActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.ConfigDASActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.ConfigE60Activity;
import com.shmedo.mcloudapp.entity.DeviceBasicInfoResult;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceTypeEnum;
import com.shmedo.core.event.MapDeviceEvent;
import com.shmedo.mcloudapp.common.view.ClearEditText;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   InputDeviceSNActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/18 10:03
 * 描述：    手动输入设备sn号
 */
public class InputDeviceSNActivity extends BaseActivity {
    @BindView(R.id.rg_device)
    RadioGroup mRgDevice;

    @BindView(R.id.input_sn)
    ClearEditText mInputSn;

    @BindView(R.id.add_device)
    Button mAddDevice;

    private String deviceType;


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, InputDeviceSNActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_input_devicesn;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        int checkedId = mRgDevice.getCheckedRadioButtonId();
        RadioButton chose = findViewById(checkedId);
        deviceType = chose.getText().toString();
    }


    private void initData() {
        mRgDevice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int id = radioGroup.getCheckedRadioButtonId();
                RadioButton chose = findViewById(id);
                deviceType = chose.getText().toString();

            }
        });
    }


    @OnClick(R.id.add_device)
    public void onViewClicked() {
        String snNumber = mInputSn.getText() != null ? mInputSn.getText().toString().trim() : "";
        if (TextUtils.isEmpty(snNumber)) {
            mInputSn.setError("请输入设备SN号");
            return;

        } else if (TextUtils.isEmpty(deviceType)) {
            ToastUtils.show("请选择设备类型");
            return;
        }

        scanResult(snNumber, deviceType);
    }


    //MEDO,189150L,DAS
    private void scanResult(String snNumber, String deviceType) {
        if (snNumber.length() != 7) {
            showTipDialog("请输入正确的设备SN号");
            return;
        }

        if (!DeviceTypeEnum.value(deviceType)) {
            showTipDialog("此设备类型暂时不支持");
            return;
        }

        MCloudApp.setCurDeviceToken(snNumber);
        MCloudApp.setCurDeviceMacAddr(null);

        String deviceInfo = "MEDO," + snNumber + "," + deviceType;
        if (deviceType.equals("DAS")) {
            ConfigDASActivity.startActivity(InputDeviceSNActivity.this, deviceInfo);

        } else if (deviceType.equals("ADME")) {
            ConfigADMEActivity.startActivity(InputDeviceSNActivity.this, deviceInfo);

        } else if (deviceType.equals("E60")) {
            DeviceBasicInfoResult deviceBasicInfoResult = new DeviceBasicInfoResult();
            deviceBasicInfoResult.setDeviceToken(snNumber);
            deviceBasicInfoResult.setDeviceTypeName(deviceType);
            ConfigE60Activity.startActivity(InputDeviceSNActivity.this, deviceBasicInfoResult);

        } else if (deviceType.equals("PVS")) {
            ToastUtils.show("pvs==" + snNumber + "==" + deviceType);
        }

        if (!TextUtils.isEmpty(deviceInfo) && deviceInfo.split(",").length > 0) {
            //将设备信息传递到MainActivity中
            MapDeviceEvent event = new MapDeviceEvent();
            event.setType("mapDevice");
            event.setDeviceName(deviceInfo);
            EventBus.getDefault().post(event);
        }

        InputDeviceSNActivity.this.finish();
    }
}
