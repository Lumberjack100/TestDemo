package com.shmedo.mcloudapp.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.views.ClearEditText;

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


    @Override protected int initContentView() {
        return R.layout.activity_input_devicesn;
    }


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @OnClick(R.id.add_device)
    public void onViewClicked() {
        String snNumber = mInputSn.getText()!=null?mInputSn.getText().toString().trim():"";
        if (StringUtil.isNullOrEmpty(snNumber)){
            mInputSn.setError("请输入设备SN号");
            return;
        }
        ToastUtil.showSToast(snNumber);
        Intent intent = new Intent(InputDeviceSNActivity.this,DeviceActivity.class);
        intent.putExtra("inputDevice",snNumber);
        startActivity(intent);
    }
}
