package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.entity.LogOutputEntity;
import com.shmedo.configlibrary.ble.cmd.entity.WorkModeEntity;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.LogOutputStatus;
import com.shmedo.configlibrary.ble.enums.WorkModel;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.model.CmdLogInfo;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.CommonViewHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import me.pqpo.librarylog4a.Log4a;
import timber.log.Timber;

/**
 * 自定义蓝牙指令交互输出并保存日志文件
 */
public class BleDasCustomCommandLogPrintFragment extends BaseBleCommunicateFragment {
    private static final String TAG = "BleCustomCommandLogPrintFragment";

    @BindView(R.id.tv_debug_mode)
    TextView mTvDebugMode;

    @BindView(R.id.logPrintEnableSBtn)
    SwitchButton logSwitchButton;

    @BindView(R.id.et_custom_command)
    ClearEditText mEtcommand;

    @BindView(R.id.recyclerView_log)
    RecyclerView mRecyclerView;

    @BindView(R.id.ll_fab_action)
    View fabActionView;

    @BindView(R.id.fab_clear_log)
    ImageView mIvClearLog;

    @BindView(R.id.fab_start_pause)
    ImageView fabStartPause;

    private CommonAdapter cmdAdapter;

    private List<CmdLogInfo> logDataList = new ArrayList<>();

    private boolean isPause = false;

    private String snNumber;

    private int debugModePos = 0;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_das_custom_command_log_print;
    }

   @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setView();
        initAdapter();
        setSwitchViewListener();
        setOnScrollListener();
    }

    private void setView() {
        isPause = false;
        fabStartPause.setImageResource(R.drawable.sl_stop_monitor);
        snNumber = MCloudApp.getCurDeviceToken();
        Log4a.i(TAG, String.format("====开始调试设备：%s", snNumber));
        mTvDebugMode.setText("关闭");
        bleViewModel.updateLogOutputMode(true);
    }

    private void initAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        cmdAdapter = new CommonAdapter<CmdLogInfo>(mActivity, R.layout.item_cmd_log_print, logDataList) {
            @Override
            protected void convert(CommonViewHolder holder, CmdLogInfo cmdLogInfo, int position) {
                holder.setText(R.id.tv_log_time, cmdLogInfo.getLogTime());
                holder.setText(R.id.tv_log_content, cmdLogInfo.getLogContent());
            }
        };
        mRecyclerView.setAdapter(cmdAdapter);
        mRecyclerView.setHasFixedSize(true);
    }

    /**
     * switch按钮事件
     */
    private void setSwitchViewListener() {
        //日志输出开关
        logSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    logSwitchButton.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                if (isChecked) {
                    setLogOutputMode(true);
                } else {
                    setLogOutputMode(false);
                }
            }
        });
    }

    private void setOnScrollListener() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    float xStart = -fabActionView.getWidth();
                    float xEnd = ConvertUtils.dp2px(10);
                    ObjectAnimator heightAnimator = ObjectAnimator
                            .ofFloat(fabActionView, "x", xStart, xEnd)
                            .setDuration(2000);
                    heightAnimator.start();
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    float xStart = fabActionView.getX();
                    float xEnd = -fabActionView.getWidth();
                    ObjectAnimator heightAnimator = ObjectAnimator
                            .ofFloat(fabActionView, "x", xStart, xEnd)
                            .setDuration(500);
                    heightAnimator.start();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    @OnClick({R.id.debugModeLayout, R.id.fab_clear_log, R.id.fab_start_pause, R.id.btn_send})
    public void onViewClicked(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.debugModeLayout) {
            showDebugModeDialog();

        } else if (id == R.id.fab_clear_log) {
            logDataList.clear();
            cmdAdapter.notifyDataSetChanged();

        } else if (id == R.id.fab_start_pause) {
            if (isPause) {
                isPause = false;
                ToastUtils.show("日志输出开始");
                fabStartPause.setImageResource(R.drawable.sl_stop_monitor);
            } else {
                ToastUtils.show("日志输出暂停");
                isPause = true;
                fabStartPause.setImageResource(R.drawable.sl_start_monitor);
            }
        } else if (id == R.id.btn_send) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            //发送指令
            String command = mEtcommand.getText().toString().trim();
            if (!command.startsWith("##")) {
                ToastUtils.show("指令格式不正确，请重新输入");
                return;
            }
            sendCommand(command + "\r\n");
            CmdLogInfo cmdLogInfo = new CmdLogInfo(TimeUtils.getNowString(new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())), command);
            logDataList.add(cmdLogInfo);
            cmdAdapter.notifyItemInserted(logDataList.size() - 1);
            mRecyclerView.scrollToPosition(cmdAdapter.getItemCount() - 1);
            Log4a.i(TAG, String.format("发送指令==%s", command));
        }
    }

    private void showDebugModeDialog() {
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"关闭", "DEBUG", "INFO"},
                        null, debugModePos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                debugModePos = position;
                                mTvDebugMode.setText(text);
                                WorkModel workModel;
                                switch (text) {
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

                                setWorkMode(workModel);
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    private void setLogOutputMode(boolean isOpen) {
        LogOutputEntity logOutputEntity = new LogOutputEntity(isOpen ? LogOutputStatus.OPEN.toInt() : LogOutputStatus.CLOSE.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.LOG_OUTPUT_STATUS, logOutputEntity);
        sendCommand(command);

        CmdLogInfo cmdLogInfo = new CmdLogInfo(TimeUtils.getNowString(new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())), command.replace("\r\n", ""));
        logDataList.add(cmdLogInfo);
        cmdAdapter.notifyItemInserted(logDataList.size() - 1);
        mRecyclerView.scrollToPosition(cmdAdapter.getItemCount() - 1);
        Log4a.i(TAG, String.format("设置日志输出模式指令==%s", command.replace("\r\n", "")));
    }

    private void setWorkMode(WorkModel workMode) {
        WorkModeEntity workModeEntity = new WorkModeEntity(workMode.toInt());
        String command = CommandManager.getInstance().getCommand(CommandType.WORK_MODE, workModeEntity);
        sendCommand(command);

        CmdLogInfo cmdLogInfo = new CmdLogInfo(TimeUtils.getNowString(new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())), command.replace("\r\n", ""));
        logDataList.add(cmdLogInfo);
        cmdAdapter.notifyItemInserted(logDataList.size() - 1);
        mRecyclerView.scrollToPosition(cmdAdapter.getItemCount() - 1);
        Log4a.i(TAG, String.format("设置调试模式指令==%s", command.replace("\r\n", "")));
    }

    @Override
    protected void parseResponseMessage(String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        Log4a.i(TAG, cmdStr);
        Log4a.flush();

        if (isPause) {
            Timber.i("=====屏幕打印暂停了");

        } else {
            CmdLogInfo cmdLogInfo = new CmdLogInfo(TimeUtils.getNowString(new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())), cmdStr);
            logDataList.add(cmdLogInfo);
            cmdAdapter.notifyItemInserted(logDataList.size() - 1);
            mRecyclerView.scrollToPosition(cmdAdapter.getItemCount() - 1);
        }
    }

    @Override
    public void onDestroy() {
        bleViewModel.updateLogOutputMode(false);
        setLogOutputMode(false);

        String content = String.format("====结束调试设备：%s\r\n", snNumber);
        Log4a.i(TAG, content);
        Log4a.flush();
        super.onDestroy();
    }

//    @Override
//    public boolean onBackPressed() {
//        String content = String.format("====结束调试设备：%s\r\n", snNumber);
//        Log4a.i(TAG, content);
//        Log4a.flush();
//        mActivity.finish();
//
//        return true;
//    }

}
