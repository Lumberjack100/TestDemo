package com.shmedo.mcloudapp.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import butterknife.BindView;
import butterknife.OnClick;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.DeviceTypeEnum;
import com.shmedo.mcloudapp.ui.activity.device.DeviceActivity;
import com.shmedo.mcloudapp.ui.activity.tabdevice.AllDeviceActivity;
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
        scanResult(snNumber);

    }

    private void scanResult(String result) {
        if (result.contains("=")) {
            String results = result.substring(result.indexOf("=") + 1);
            scan(results);
        }else {
            scan(result);
            //showScanResultDialog("请扫码正确的设备二维码");
        }
    }
    //MEDO,189150L,DAS
    private void scan(String results){
        if (results.startsWith("MEDO")) {
            String[] localData = results.split(",");
            if (localData.length != 3) {
                LoadingDialog.showScanResultDialog(this,"请扫码正确的设备二维码");
                return;
            }
            if (StringUtil.isEmpty(localData[0]) || StringUtil.isEmpty(localData[1])
                || StringUtil.isEmpty(localData[2])) {
                LoadingDialog.showScanResultDialog(this,"二维码信息不能为空");
                return;
            }
            if (localData[1].length() != 7) {
                LoadingDialog.showScanResultDialog(this,"设备标识有误,请扫码正确的设备二维码");
                return;
            }
            if (DeviceTypeEnum.value(localData[2])) {
                Intent intent = new Intent(InputDeviceSNActivity.this,DeviceActivity.class);
                intent.putExtra("inputDevice",results);
                startActivity(intent);
                //先根据扫码到的tabName跳转到队应的页面
                //mTabViewPage.setCurrentItem(listTitle.indexOf(localData[2]));
                //SearchEvent event = new SearchEvent();
                //event.setType("scan");
                //event.setSearchName(results);
                //EventBus.getDefault().post(event);
            } else {
                LoadingDialog.showScanResultDialog(this,"此设备类型暂时不支持");
            }
        }
    }
}
