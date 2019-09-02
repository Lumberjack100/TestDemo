package com.shmedo.mcloudapp.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import butterknife.BindView;
import butterknife.OnClick;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.DeviceTypeEnum;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.views.ClearEditText;
import com.shmedo.mcloudapp.views.LoadingDialog;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   InputDeviceSNActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/18 10:03
 * 描述：    手动输入设备sn号
 */
public class InputDeviceSNActivity extends BaseActivity {
    @BindView(R.id.input_sn) ClearEditText mInputSn;
    @BindView(R.id.add_device) Button mAddDevice;
    //@BindView(R.id.rb_das) RadioButton mRbDas;
    //@BindView(R.id.rb_e60) RadioButton mRbE60;
    //@BindView(R.id.rb_pvs) RadioButton mRbPvs;
    @BindView(R.id.rg_device) RadioGroup mRgDevice;
    private String deviceType;

    @Override protected int initContentView() {
        return R.layout.activity_input_devicesn;
    }


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }


    private void initData() {
        mRgDevice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int id = radioGroup.getCheckedRadioButtonId();
                RadioButton chose = findViewById(id);
                deviceType = chose.getText().toString();

            }
        });
    }


    @OnClick(R.id.add_device)
    public void onViewClicked() {
        String snNumber = mInputSn.getText() != null ? mInputSn.getText().toString().trim() : "";
        if (StringUtil.isNullOrEmpty(snNumber)) {
            mInputSn.setError("请输入设备SN号");
            return;
        }else if (StringUtil.isNullOrEmpty(deviceType)){
            ToastUtil.showSToast("请选择设备类型");
            return;
        }
        Log.i("adu","设备SN号"+snNumber+"-设备类型--"+deviceType);

        scanResult(snNumber,deviceType);

    }





    //MEDO,189150L,DAS
    private void scanResult(String results,String deviceType) {

        if (StringUtil.isEmpty(results) || StringUtil.isEmpty(deviceType)) {
            LoadingDialog.showScanResultDialog(this, "二维码信息不能为空");
            return;
        }
        if (results.length() != 7) {
            LoadingDialog.showScanResultDialog(this, "设备标识有误,请扫码正确的设备二维码");
            return;
        }
        if (DeviceTypeEnum.value(deviceType)) {
            if (deviceType.equals("DAS")){
                Intent intent = new Intent(InputDeviceSNActivity.this, AllDeviceActivity.class);
                intent.putExtra("inputDevice", results);
                intent.putExtra("deviceType",deviceType);
                startActivity(intent);
            }else if (deviceType.equals("E60")){
                ToastUtil.showSToast("e60=="+results+"=="+deviceType);
            }else if (deviceType.equals("PVS")){
                ToastUtil.showSToast("pvs=="+results+"=="+deviceType);
            }

        } else {
            LoadingDialog.showScanResultDialog(this, "此设备类型暂时不支持");
        }
    }
}
