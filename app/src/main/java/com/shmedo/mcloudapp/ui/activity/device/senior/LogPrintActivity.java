package com.shmedo.mcloudapp.ui.activity.device.senior;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
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
import com.shmedo.mcloudapp.util.LogToSDUtil;
import com.shmedo.mcloudapp.views.ClearEditText;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device.senior
 * 创建者:   dpc
 * 创建时间:  2019-12-18
 * 描述：     体脂输出页面
 */
public class LogPrintActivity extends BaseDeviceConnectActivity {
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;

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

    @BindView(R.id.viewEmpty)
    View viewEmpty;

    @BindView(R.id.recycler_log_print)
    RecyclerView recyclerLogPrint;

    @BindView(R.id.tv_view_log_directory)
    TextView tvViewLogDirectory;

    @BindView(R.id.view2)
    View view2;

    @BindView(R.id.fab_start_pause)
    FloatingActionButton fabStartPause;

    private ArrayAdapter<String> debugModeAdapter;
    private int debugModeCheck = 0;

    private CommonAdapter adapter;

    private List<String> logList = new ArrayList<>();

    private boolean isStart = false;

    private String snNumber;
    private MaterialDialog mMaterialDialog;
    private MaterialDialog.Builder mBuilder;

    public static void startActivity(Context context, String snNumber) {
        Intent intent = new Intent(context, LogPrintActivity.class);
        intent.putExtra("snNumber", snNumber);
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
        snNumber = getIntent().getStringExtra("snNumber");
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

                LogOutputEntity logOutputEntity;
                String cmd;
                if (isChecked) {
                    logOutputEntity = new LogOutputEntity(LogOutputStatus.OPEN.toInt());
                    cmd = CommandManager.getInstance().getCommand(CommandType.LOG_OUTPUT_STATUS, logOutputEntity);
                    sendCommonCommandImmediately(cmd);
                    setSwitchViewState(true, logSwitchButton, "已开启");
                    isStart = false;
                    ToastUtils.show("开始日志输出");
                    fabStartPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
                } else {
                    logOutputEntity = new LogOutputEntity(LogOutputStatus.CLOSE.toInt());
                    cmd = CommandManager.getInstance().getCommand(CommandType.LOG_OUTPUT_STATUS, logOutputEntity);
                    sendCommonCommandImmediately(cmd);
                    setSwitchViewState(false, logSwitchButton, "已关闭");
                    ToastUtils.show("关闭日志输出");
                    isStart = true;
                    fabStartPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_start));
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
        viewEmpty.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(200);
                new XPopup.Builder(LogPrintActivity.this)
                        //.maxWidth(600)
                        .asCenterList("", new String[]{"清空日志"},
                                new OnSelectListener() {
                                    @Override
                                    public void onSelect(int position, String text) {
                                        if (position == 0) {
                                            logList.clear();
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
                //发送指令
                String sendCode = ceSendCode.getText().toString().trim();
                String result = sendCode + "\r\n";
                if (sendCode.startsWith("##")) {
                    sendCommonCommandImmediately(result);
                    logList.add(sendCode);
                    adapter.notifyDataSetChanged();
                } else {
                    ToastUtils.show("指令格式不正确，请重新输入");
                }
                break;

            case R.id.tv_view_log_directory:
                File filesPath = Environment.getExternalStorageDirectory().getAbsoluteFile();
                File file = LogFileUtil.createLogFile(filesPath, snNumber);
                if (file.exists()) {
                    showLogResultDialog(file.getAbsolutePath());
                } else {
                    ToastUtils.show("暂未生成日志");
                }
//                Uri logUir = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider",file.getParentFile());
//                //打开日志目录
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setDataAndType(logUir, "*.txt");
////                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                startActivity(intent);
//                try {
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
                break;

            case R.id.fab_start_pause:
                if (isStart) {
                    isStart = false;
                    ToastUtils.show("日志已开始输出");
                    fabStartPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
                } else {
                    ToastUtils.show("日志已暂停输出");
                    isStart = true;
                    fabStartPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_start));
                }
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
        //输出内容
        @SuppressLint("SimpleDateFormat")
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String content = time + "  " + messageEvent;
        Timber.i("====日志内容" + content);
        LogToSDUtil.saveLogToSD(content, snNumber);

        if (isStart) { //
            Timber.i("=====暂停了");

        } else {
            Timber.i("=====开始了");
            String result = messageEvent.replace("\r\n", "");
            logList.add(result);
            adapter.notifyDataSetChanged();
            recyclerLogPrint.scrollToPosition(adapter.getItemCount() - 1);

        }
    }

    @Override
    public void onBackPressed() {
        finish();
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
}
