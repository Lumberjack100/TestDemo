package com.shmedo.mcloudapp.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.DeviceBasicInfoResult;
import com.shmedo.mcloudapp.entity.DeviceTypeEnum;
import com.shmedo.mcloudapp.entity.event.MapDeviceEvent;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.views.ClearEditText;
import com.shmedo.mcloudapp.views.LoadingDialog;

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

    @Override
    protected int initContentView() {
        return R.layout.activity_input_devicesn;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
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
            ToastUtil.showShortToast("请选择设备类型");
            return;
        }

        scanResult(snNumber, deviceType);
    }


    //MEDO,189150L,DAS
    private void scanResult(String snNumber, String deviceType) {
        if (snNumber.length() != 7) {
            LoadingDialog.showScanResultDialog(this, "请输入正确的设备SN号");
            return;
        }

        if (!DeviceTypeEnum.value(deviceType)) {
            LoadingDialog.showScanResultDialog(this, "此设备类型暂时不支持");
            return;
        }

        String deviceInfo = "";
        if (deviceType.equals("DAS")) {
            deviceInfo = "MEDO," + snNumber + "," + deviceType;
            ConfigDASActivity.startActivity(InputDeviceSNActivity.this, deviceInfo);

        } else if (deviceType.equals("E60")) {
            deviceInfo = "MEDO," + snNumber + "," + deviceType;

            DeviceBasicInfoResult deviceBasicInfoResult = new DeviceBasicInfoResult();
            deviceBasicInfoResult.setDeviceToken(snNumber);
            deviceBasicInfoResult.setDeviceTypeName(deviceType);

            ConfigE60Activity.startActivity(InputDeviceSNActivity.this, deviceBasicInfoResult);

        } else if (deviceType.equals("PVS")) {
            ToastUtil.showShortToast("pvs==" + snNumber + "==" + deviceType);
        }


        if (!TextUtils.isEmpty(deviceInfo) && deviceInfo.split(",").length > 0) {

            //将设备信息传递到MainActivity中
            MapDeviceEvent event = new MapDeviceEvent();
            event.setType("mapDevice");
            event.setDeviceName(deviceInfo);
            EventBus.getDefault().post(event);
        }
    }
}
