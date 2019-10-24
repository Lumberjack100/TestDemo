package com.shmedo.mcloudapp.ui.activity.device;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.ui.fragment.DeviceFragment;
import com.shmedo.mcloudapp.util.ToastUtil;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device.config
 * 文件名:   RainConfigActivity
 * 创建者:   dpc
 * 创建时间:  2019/2/22 14:33
 * 描述：   配置雨量计
 */
public class RainConfigActivity extends BaseActivity {
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.sp_rain)
    Spinner mSpRain;

    @BindView(R.id.btn_confirm_complete)
    Button mBtnConfirmComplete;

    private String rainResult;

    private ArrayAdapter<String> dataAdapter;

    @Override
    protected int initContentView() {
        return R.layout.activity_rain_config;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        initView();
        initData();
    }


    private void initView() {
        mToolbarTitle.setText("配置雨量计");

        String[] debugData = getResources().getStringArray(R.array.rain);
        dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, debugData);
        mSpRain.setAdapter(dataAdapter);
        mSpRain.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String result = mSpRain.getSelectedItem().toString().replace("mm", "");

                DecimalFormat df = new DecimalFormat("0");
                rainResult = df.format(Double.valueOf(result) * 100);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    private void initData() {
        if (DeviceFragment.setRianAccuryParameter != null) {
            String result = Double.valueOf(DeviceFragment.setRianAccuryParameter.getRainAccury()) / 100 + "mm";
            SpinnerAdapter spinnerAdapter = mSpRain.getAdapter();
            int count = spinnerAdapter.getCount();
            for (int i = 0; i < count; i++) {
                if (result.equals(spinnerAdapter.getItem(i).toString())) {
                    mSpRain.setSelection(i);
                    break;
                }
            }
        }
    }


    @OnClick(R.id.btn_confirm_complete)
    public void onViewClicked() {
        if (rainResult == null) {
            ToastUtil.showShortToast("未获取到选中的值");
            return;
        }

        if (DeviceFragment.isConnected) {
            //恢复出厂设置
            com.shmedo.mcloudapp.bluetooth.Message msg = new com.shmedo.mcloudapp.bluetooth.Message("##121",
                    "##121" + rainResult + "\r\n", true);
            if (DeviceFragment.mdBluetoothManager != null) {
                DeviceFragment.mdBluetoothManager.writeMessage(msg);
            }
            finish();

        } else {
            ToastUtil.showShortToast("蓝牙未连接");
        }
    }
}
