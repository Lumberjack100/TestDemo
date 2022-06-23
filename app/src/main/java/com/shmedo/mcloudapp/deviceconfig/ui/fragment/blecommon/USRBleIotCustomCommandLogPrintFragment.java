package com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.entity.IotLogOutputEntity;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
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

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/8/21 <br/>
 * 描述：   自定义蓝牙指令交互输出并保存日志文件
 */
public class USRBleIotCustomCommandLogPrintFragment extends BaseUSRBleIotCommunicateFragment {
    private static final String TAG = "BleIotCustomCommandLogPrintFragment";

    @BindView(R.id.logPrintEnableSBtn)
    SwitchButton logSwitchButton;

    @BindView(R.id.et_custom_command)
    ClearEditText mEtcommand;

    @BindView(R.id.recyclerView_log)
    RecyclerView mRecyclerView;

    @BindView(R.id.fab_clear_log)
    ImageView mIvClearLog;

    @BindView(R.id.btn_send)
    Button btnSend;

    private CommonAdapter cmdAdapter;

    private List<CmdLogInfo> logDataList = new ArrayList<>();

    private String snNumber;


    public static USRBleIotCustomCommandLogPrintFragment newInstance() {
        return new USRBleIotCustomCommandLogPrintFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_iot_custom_command_log_print_fragment;
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
        snNumber = MCloudApp.getCurDeviceToken();
        Log4a.i(TAG, String.format("====开始调试设备：%s", snNumber));
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
                    float xStart = -mIvClearLog.getWidth();
                    float xEnd = ConvertUtils.dp2px(10);
                    ObjectAnimator heightAnimator = ObjectAnimator
                            .ofFloat(mIvClearLog, "x", xStart, xEnd)
                            .setDuration(2000);
                    heightAnimator.start();
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    float xStart = mIvClearLog.getX();
                    float xEnd = -mIvClearLog.getWidth();
                    ObjectAnimator heightAnimator = ObjectAnimator
                            .ofFloat(mIvClearLog, "x", xStart, xEnd)
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

    @OnClick({R.id.fab_clear_log, R.id.btn_send})
    public void onViewClicked(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.fab_clear_log) {
            logDataList.clear();
            cmdAdapter.notifyDataSetChanged();
        } else if (id == R.id.btn_send) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
                return;
            }
            //发送指令
            String command = mEtcommand.getText().toString().trim();
            if (command.startsWith("##")) {
                command = String.format("$cmd=md_raw&content=%s", command);
            }
            if (!command.startsWith("$cmd")) {
                ToastUtils.show("指令格式不正确，请重新输入");
                return;
            }
            sendCommand(command);
//            btnSend.setEnabled(false);
            CmdLogInfo cmdLogInfo = new CmdLogInfo(TimeUtils.getNowString(new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())), command);
            logDataList.add(cmdLogInfo);
            cmdAdapter.notifyDataSetChanged();
            mRecyclerView.scrollToPosition(cmdAdapter.getItemCount() - 1);
            Log4a.i(TAG, String.format("发送指令==%s", command));
        }
    }

    private void setLogOutputMode(boolean isOpen) {
        IotLogOutputEntity entity = new IotLogOutputEntity();
        entity.setLevel(isOpen ? "info" : "off");
        entity.setType("bt");
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.SET_LOG_OUTPUT_MODE_LEVEL, entity);
        sendCommand(command);
        CmdLogInfo cmdLogInfo = new CmdLogInfo(TimeUtils.getNowString(new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())), command);
        logDataList.add(cmdLogInfo);
        cmdAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(cmdAdapter.getItemCount() - 1);
    }

    @Override
    protected void parseResponseMessage(String cmdStr) {
        if (!isActive) {
            return;
        }
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        btnSend.setEnabled(true);
        Log4a.i(TAG, cmdStr);
        Log4a.flush();

        CmdLogInfo cmdLogInfo = new CmdLogInfo(TimeUtils.getNowString(new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())), cmdStr);
        logDataList.add(cmdLogInfo);
        cmdAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(cmdAdapter.getItemCount() - 1);
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
}