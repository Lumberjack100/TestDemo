package com.shmedo.mcloudapp.ui.activity.device.senior;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.CommonAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.ViewHolder;
import com.shmedo.mcloudapp.ui.activity.device.BaseDeviceConnectActivity;
import com.shmedo.mcloudapp.views.ClearEditText;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device.senior
 * 创建者:   dpc
 * 创建时间:  2019-12-18
 * 描述：     体脂输出页面
 */
public class LogPrintActivity extends BaseDeviceConnectActivity {


    @BindView(R.id.spinner_debug)
    Spinner spinnerDebug;

    @BindView(R.id.ll_debug)
    LinearLayout llDebug;

    @BindView(R.id.tv_config_name)
    TextView tvConfigName;

    @BindView(R.id.tv_log_print)
    TextView tvLogPrint;

    @BindView(R.id.log_switchButton)
    SwitchButton logSwitchButton;

    @BindView(R.id.rl_log)
    RelativeLayout rlLog;

    @BindView(R.id.ce_send_code)
    ClearEditText ceSendCode;

    @BindView(R.id.btn_send)
    Button btnSend;

    @BindView(R.id.ll_send)
    LinearLayout llSend;

    @BindView(R.id.recycler_log_print)
    RecyclerView recyclerLogPrint;

    @BindView(R.id.tv_view_log_directory)
    TextView tvViewLogDirectory;

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;

    private ArrayAdapter<String> debugModeAdapter;
    private int debugModeCheck = 0;

    private CommonAdapter adapter;

    private List<String> logList = new ArrayList<>();


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, LogPrintActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_log_print;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        initView();
        initAdapter();
    }

    private void initView() {
        toolbarTitle.setText("指令日志输出");
        //调试模式
        String[] debugData = getResources().getStringArray(R.array.das_debug);
        debugModeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, debugData);
        debugModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDebug.setAdapter(debugModeAdapter);
        spinnerDebug.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (++debugModeCheck > 1) {
                    String status = parent.getSelectedItem().toString();
                    String smdStr;
                    switch (status) {
                        case "关闭":
                            smdStr = "##0061\r\n";
                            break;

                        case "DEBUG":
                            smdStr = "##0062\r\n";
                            break;

                        case "INFO":
                            smdStr = "##0063\r\n";
                            break;

                        default:
                            smdStr = "##0061\r\n";
                            break;
                    }
                    sendCommonCommand(smdStr);
                    Timber.d("设置调试模式指令==" + smdStr);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //日志输出开关
        logSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    logSwitchButton.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }

                if (isChecked) {
                    //发送激活DAS命令
                    sendCommonCommand("##2261\r\n");
                    setSwitchViewState(true, logSwitchButton, "已开启");
                } else {
                    sendCommonCommand("##2260\r\n");
                    setSwitchViewState(true, logSwitchButton, "已关闭");
                }
            }
        });


    }

    private void initAdapter() {
        recyclerLogPrint.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommonAdapter<String>(this, R.layout.item_log_print, logList) {
            @Override
            protected void convert(ViewHolder holder, String string, int position) {
                holder.setText(R.id.tv_log, string);
            }
        };

        recyclerLogPrint.setAdapter(adapter);
    }

    private void setSwitchViewState(boolean isOpen, TextView textView, String content) {
        textView.setText(content);
        textView.setTextColor(isOpen ? getResources().getColor(R.color.colorPrimary) : getResources().getColor(R.color.gray_807B7B));
    }

    @OnClick({R.id.btn_send, R.id.tv_view_log_directory})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                //发送指令
                String sendCode = ceSendCode.getText().toString().trim();
                String result = sendCode+"\r\n";
                if (null != sendCode && sendCode.startsWith("##")) {
                    logList.add(sendCode);
                    adapter.notifyDataSetChanged();
                }
                break;
            case R.id.tv_view_log_directory:
                //打开日志目录
                ToastUtils.show("功能开发中...");
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (!TextUtils.isEmpty(messageEvent)) {
            setResultData(messageEvent);
        }
    }

    private void setResultData(String messageEvent) {
        String result = messageEvent.replace("\r\n","");
        logList.add(result);
        adapter.notifyDataSetChanged();
    }
}
