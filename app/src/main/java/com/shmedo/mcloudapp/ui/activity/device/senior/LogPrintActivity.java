package com.shmedo.mcloudapp.ui.activity.device.senior;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.core.cmd.CommandManager;
import com.shmedo.core.cmd.entity.LogOutputEntity;
import com.shmedo.core.cmd.entity.WorkModeEntity;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.enums.LogOutputStatus;
import com.shmedo.core.enums.WorkModel;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.CommonAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.ViewHolder;
import com.shmedo.mcloudapp.ui.activity.device.BaseDeviceConnectActivity;
import com.shmedo.mcloudapp.util.LogFileUtil;
import com.shmedo.mcloudapp.views.ClearEditText;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import me.pqpo.librarylog4a.Log4a;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device.senior
 * 创建者:   dpc
 * 创建时间:  2019-12-18
 * 描述：     体脂输出页面
 */
public class LogPrintActivity extends BaseDeviceConnectActivity {
    private static final String TAG = "LogPrintActivity";

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;

    @BindView(R.id.spinner_debug)
    Spinner spinnerDebug;

    @BindView(R.id.log_switchButton)
    SwitchButton logSwitchButton;

    @BindView(R.id.ce_send_code)
    ClearEditText ceSendCode;

    @BindView(R.id.viewEmpty)
    View viewEmpty;

    @BindView(R.id.recycler_log_print)
    RecyclerView recyclerLogPrint;

    @BindView(R.id.tv_view_log_directory)
    TextView tvViewLogDirectory;

    @BindView(R.id.fab_start_pause)
    FloatingActionButton fabStartPause;

    private ArrayAdapter<String> debugModeAdapter;
    private int debugModeCheck = 0;

    private CommonAdapter adapter;

    private List<String> logDataList = new ArrayList<>();

    private boolean isPause = false;

    private String snNumber;
    private MaterialDialog mMaterialDialog;
    private MaterialDialog.Builder mBuilder;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, LogPrintActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
        snNumber = MCloudApp.getCurDeviceToken();
        String content = String.format("====开始调试设备：%s", snNumber);
        Log4a.i(TAG, content);

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
                    WorkModel workModel;
                    switch (status) {
                        case "关闭":
                            workModel = WorkModel.WORK;
                            break;

                        case "DEBUG":
                            workModel = WorkModel.DEBUG;
                            break;

                        case "INFO":
                            workModel = WorkModel.INFO;
                            break;

                        default:
                            workModel = WorkModel.WORK;
                            break;
                    }
                    WorkModeEntity workModeEntity = new WorkModeEntity(workModel.toInt());
                    String command = CommandManager.getInstance().getCommand(CommandType.WORK_MODE, workModeEntity);
                    sendCommonCommandImmediately(command);
                    Timber.d("设置调试模式指令==%s", command);
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
                    switchLogOutputMode(true);
                    setSwitchViewState(true, logSwitchButton, "已开启");
                    ToastUtils.show("开始日志输出");
                    isPause = false;
                    fabStartPause.setImageResource(R.drawable.ic_pause);
                } else {
                    switchLogOutputMode(false);
                    setSwitchViewState(false, logSwitchButton, "已关闭");
                    ToastUtils.show("关闭日志输出");
                    isPause = true;
                    fabStartPause.setImageResource(R.drawable.ic_start);
                }
            }
        });
    }

    private void initAdapter() {
        recyclerLogPrint.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommonAdapter<String>(this, R.layout.item_log_print, logDataList) {
            @Override
            protected void convert(ViewHolder holder, String string, int position) {
                holder.setText(R.id.tv_log, string);
            }
        };
        recyclerLogPrint.setAdapter(adapter);
        viewEmpty.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (logDataList.size() == 0)
                    return false;

                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(200);
                new XPopup.Builder(LogPrintActivity.this)
                        .asCenterList("", new String[]{"清空日志"},
                                new OnSelectListener() {
                                    @Override
                                    public void onSelect(int position, String text) {
                                        if (position == 0) {
                                            logDataList.clear();
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                })
                        .show();
                return false;
            }
        });
    }

    private void setSwitchViewState(boolean isOpen, TextView textView, String content) {
        textView.setText(content);
        textView.setTextColor(isOpen ? getResources().getColor(R.color.colorPrimary) : getResources().getColor(R.color.gray_807B7B));
    }

    @OnClick({R.id.btn_send, R.id.tv_view_log_directory, R.id.fab_start_pause})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    return;
                }
                //发送指令
                String sendCode = ceSendCode.getText().toString().trim();
                String result = sendCode + "\r\n";
                if (!sendCode.startsWith("##")) {
                    ToastUtils.show("指令格式不正确，请重新输入");
                    return;
                }
                sendCommonCommandImmediately(result);
                logDataList.add(sendCode);
                adapter.notifyDataSetChanged();
                break;

            case R.id.tv_view_log_directory:
//                File filesPath = Environment.getExternalStorageDirectory().getAbsoluteFile();
//                File file = LogFileUtil.createLogFile(filesPath, snNumber);
//                if (file.exists()) {
//                    showLogResultDialog(file.getAbsolutePath());
//                } else {
//                    ToastUtils.show("暂未生成日志");
//                }
                showLogResultDialog(LogFileUtil.getLogPath());
                break;

            case R.id.fab_start_pause:
                if (isPause) {
                    isPause = false;
                    ToastUtils.show("日志已开始输出");
                    fabStartPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
                } else {
                    ToastUtils.show("日志已暂停输出");
                    isPause = true;
                    fabStartPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_start));
                }
                break;
        }
    }

    private void switchLogOutputMode(boolean isOpen) {
        LogOutputEntity logOutputEntity = new LogOutputEntity(isOpen ? LogOutputStatus.OPEN.toInt() : LogOutputStatus.CLOSE.toInt());
        String cmd = CommandManager.getInstance().getCommand(CommandType.LOG_OUTPUT_STATUS, logOutputEntity);
        sendCommonCommandImmediately(cmd);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (!TextUtils.isEmpty(messageEvent)) {
            setResultData(messageEvent);
        }
    }

    private void setResultData(String messageEvent) {
        String content = messageEvent.replace("\r\n","");
        Timber.i("====日志内容%s", content);
        Log4a.i(TAG, content);
        Log4a.flush();
//        Log4a.release();
//        LogToSDUtil.saveLogToSD(content, snNumber);

        if (isPause) { //
            Timber.i("=====暂停了");

        } else {
            Timber.i("=====开始了");
            String result = messageEvent.replace("\r\n", "");
            logDataList.add(result);
            adapter.notifyDataSetChanged();
            recyclerLogPrint.scrollToPosition(adapter.getItemCount() - 1);
        }
    }

    private void showLogResultDialog(String content) {
        mBuilder = new MaterialDialog.Builder(this);
        mBuilder.title("日志目录地址：").content(content).contentColor(Color.parseColor("#000000")).canceledOnTouchOutside(false).positiveText("确定").onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        });
        mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    @Override
    public void onBackPressed() {
        String content = String.format("====结束调试设备：%s\r\n", snNumber);
        Log4a.i(TAG, content);
        Log4a.flush();
        finish();
    }
}
