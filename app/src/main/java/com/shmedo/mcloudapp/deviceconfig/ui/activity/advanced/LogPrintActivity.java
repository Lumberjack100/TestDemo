package com.shmedo.mcloudapp.deviceconfig.ui.activity.advanced;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shmedo.core.MCloudApp;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.core.cmd.CommandManager;
import com.shmedo.core.cmd.entity.LogOutputEntity;
import com.shmedo.core.cmd.entity.WorkModeEntity;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.enums.LogOutputStatus;
import com.shmedo.core.enums.WorkModel;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.CommonAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.CommonViewHolder;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseDeviceConnectActivity;
import com.shmedo.mcloudapp.util.FileProviderUtils;
import com.shmedo.core.util.LogFileUtil;
import com.shmedo.mcloudapp.common.view.ClearEditText;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import gdut.bsx.share2.Share2;
import gdut.bsx.share2.ShareContentType;
import me.pqpo.librarylog4a.Log4a;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.deviceconfig.ui.activity.senior
 * 创建者:   dpc
 * 创建时间:  2019-12-18
 * 描述：     体脂输出页面
 */
public class LogPrintActivity extends BaseDeviceConnectActivity {
    private static final int FILE_SELECT_CODE = 100;

    private static final int REQUEST_SHARE_FILE_CODE = 120;

    private static final String TAG = "LogPrintActivity";

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;

    @BindView(R.id.spinner_debug)
    Spinner spinnerDebug;

    @BindView(R.id.log_switchButton)
    SwitchButton logSwitchButton;

    @BindView(R.id.ce_send_code)
    ClearEditText ceSendCode;

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

    private Uri shareFileUrl = null;


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
            protected void convert(CommonViewHolder holder, String string, int position) {
                holder.setText(R.id.tv_log, string);
            }
        };
        recyclerLogPrint.setAdapter(adapter);
    }

    private void setSwitchViewState(boolean isOpen, TextView textView, String content) {
        textView.setText(content);
        textView.setTextColor(isOpen ? getResources().getColor(R.color.colorPrimary) : getResources().getColor(R.color.gray_807B7B));
    }

    @OnClick({R.id.btn_send, R.id.tv_view_log_directory, R.id.fab_clear_log, R.id.fab_start_pause})
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
                shareFile();
                break;

            case R.id.fab_clear_log:
                logDataList.clear();
                adapter.notifyDataSetChanged();
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
        String content = messageEvent.replace("\r\n", "");
        Timber.i("====日志内容%s", content);
        Log4a.i(TAG, content);
        Log4a.flush();

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

    private void shareFile() {
        String path = LogFileUtil.getLogPath();
        File file = new File(path);
        if (!file.exists()) {
            ToastUtils.show("日志文件不存在");
            return;
        }

        Uri contentUri = FileProviderUtils.uriFromFile(this, file);

        new Share2.Builder(this)
                .setContentType(ShareContentType.FILE)
                .setShareFileUri(contentUri)
                .setTitle("分享文件")
                .setOnActivityResult(300)
                .build()
                .shareBySystem();
    }

   /* private void openFileChooser() {
        File file = getExternalFilesDir("logs");
        if (null == file || !file.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileProvider", file);
            intent.setDataAndType(contentUri, "text/plain");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "text/plain");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, FILE_SELECT_CODE);
    }*/

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("requestCode=" + requestCode + " resultCode=" + resultCode);
        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK) {
            shareFileUrl = data.getData();
            if (shareFileUrl == null) {
                Toast.makeText(this, "Please choose a file to share.", Toast.LENGTH_SHORT).show();
                return;
            }

            new Share2.Builder(this)
                    .setContentType(ShareContentType.FILE)
                    .setShareFileUri(shareFileUrl)
                    .setTitle("Share File")
                    .setOnActivityResult(REQUEST_SHARE_FILE_CODE)
                    .build()
                    .shareBySystem();
        }
    }*/


    @Override
    public void onBackPressed() {
        String content = String.format("====结束调试设备：%s\r\n", snNumber);
        Log4a.i(TAG, content);
        Log4a.flush();
        finish();
    }
}
