package com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.Inclinometer_debug_box.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.at.ATCommand;
import com.shmedo.configlibrary.at.WHBLE102CommandType;
import com.shmedo.core.util.DeviceInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.usb_serial.ATCommandItem;
import com.shmedo.mcloudapp.util.TextUtil;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/19 <br/>
 * 描述：     工作模式配置
 */
public class ConfigWorkModeDialogFragment extends BaseDebugBoxDialogFragment {
    private int USB_SERIAL_WORK_MODE = 0x10001;//

    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.tv_work_mode)
    TextView mTvWorkMode;

    @BindView(R.id.btn_query_data)
    Button mBtnQuery;

    @BindView(R.id.btn_save)
    Button mBtnSave;

    private String workMode;

    public static ConfigWorkModeDialogFragment newInstance() {
        return new ConfigWorkModeDialogFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.config_work_mode_dialog_fragment;
    }

    @Override
    protected void setWindowStyle(int gravity) {
        super.setWindowStyle(Gravity.CENTER);
        Dialog mDialog = getDialog();
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = (int) (DeviceInfo.getScreenWidth() * 0.9f);
        wlp.height = (int) (DeviceInfo.getScreenHeight() * 0.6f);
        window.setAttributes(wlp);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvTitle.setText("模式配置");
        workMode = "存贮/运输状态";
        mTvWorkMode.setText(workMode);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isConnected()) {
            mBtnQuery.setEnabled(false);
            mBtnSave.setEnabled(false);
            exitCommand();
        }
    }

    /**
     * 退出命令行模式
     */
    private void exitCommand() {
        atCommandItems.clear();

        String command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.ENTM.toString() + ATCommand.NEWLINE_CRLF;
        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.ENTM, command);
        atCommandItems.add(atCommandItem);

        sendCommandFromCmdList(USB_SERIAL_WORK_MODE, WRITE_TIME_OUT_500_MILLIS);
    }

    /**
     * 查询工作模式
     * 发送 01 03 04 04 00 01 C4 FB
     */
    private void queryWorkMode() {
        atCommandItems.clear();

        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.QUERY_WORK_MODE, "010304040001C4FB");
        atCommandItems.add(atCommandItem);

        sendHexCommandFromCmdList(USB_SERIAL_WORK_MODE, WRITE_TIME_OUT_500_MILLIS);
    }

    /**
     * 设置工作模式
     * 发送 01 03 00 64 00 03 44 14
     */
    private void setWorkMode() {
        atCommandItems.clear();

        String command = "";
        if (workMode.equals("工作状态")) {
            command = "01 10 08 0D 00 01 02 5A 5A 94 16";
        } else {
            command = "01 10 08 0D 00 01 02 00 00 2E 8D";
        }
        command = command.replace(" ", "").trim();
        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.SET_WORK_MODE, command);
        atCommandItems.add(atCommandItem);

        sendHexCommandFromCmdList(USB_SERIAL_WORK_MODE, WRITE_TIME_OUT_500_MILLIS);
    }

    @OnClick({R.id.iv_close, R.id.ll_work_mode, R.id.btn_query_data, R.id.btn_save})
    public void onClick(View view) {
//        if (isDoubleClick(view)) {
//            return;
//        }
        int id = view.getId();
        if (id == R.id.iv_close) {
            dismiss();

        } else if (id == R.id.ll_work_mode) {
            showWorkModeDialog();

        } else if (id == R.id.btn_query_data) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.usb_config_disconnect_warn));
                return;
            }
            stopProgressAll();
            mBtnQuery.setEnabled(false);
            mBtnSave.setEnabled(false);
            queryWorkMode();
        } else if (id == R.id.btn_save) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.usb_config_disconnect_warn));
                return;
            }
            stopProgressAll();
            mBtnQuery.setEnabled(false);
            mBtnSave.setEnabled(false);
            setWorkMode();
        }
    }

    /**
     * 选择状态
     */
    private void showWorkModeDialog() {
        final String[] modes = new String[]{"存贮/运输状态", "工作状态"};
        int pos = Arrays.asList(modes).indexOf(workMode);
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title("选择状态")
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .items(modes)
                .itemsCallbackSingleChoice(pos, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        workMode = modes[which];
                        mTvWorkMode.setText(workMode);
                        return true;
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    @Override
    protected void parseResponseMessage(byte[] data) {
        try {
            resultByteBuf.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        if (atCommandItems.size() == 0)
            return;

        ATCommandItem commandItem = atCommandItems.getFirst();
        atCommandItems.removeFirst();//移除已经发送完的指令
        if (msg.what == USB_SERIAL_WORK_MODE) {
            switch (commandItem.getCommandType()) {
                case ENTM: {//退出命令模式
                    String cmdStr = resultByteBuf.toString();
                    resultByteBuf.reset();
                    Timber.e("接收串口数据: %s", cmdStr);
                    if ((cmdStr.contains("ENTM:OK") && cmdStr.contains(ATCommand.OK_FLAG)) || TextUtils.isEmpty(cmdStr)) {
                        queryWorkMode();
                    }else if (cmdStr.toUpperCase().contains("ERR")) {
                        cmdStr = filterControlCharacter(commandItem.getCommand());
                        Timber.e("%s  出错", cmdStr);
                        ToastUtils.show(cmdStr + "  出错");
                    }
                }
                break;

                case QUERY_WORK_MODE: {//
                    mBtnQuery.setEnabled(true);
                    mBtnSave.setEnabled(true);
                    String hexData = TextUtil.toHexString(resultByteBuf.toByteArray());
                    resultByteBuf.reset();
                    if (TextUtils.isEmpty(hexData)) {
                        return;
                    }
                    Timber.e("接收16进制串口数据: %s", hexData);
                    hexData = hexData.replace(" ", "").toUpperCase().trim();

                    if (hexData.equals("0103025A5A02DF")) {//工作
                        workMode = "工作状态";
                        mTvWorkMode.setText(workMode);
                    } else if (hexData.equals("0103020000B844")) {//存贮
                        workMode = "存贮/运输状态";
                        mTvWorkMode.setText(workMode);
                    }
                }
                break;

                case SET_WORK_MODE: {//
                    mBtnQuery.setEnabled(true);
                    mBtnSave.setEnabled(true);
                    String hexData = TextUtil.toHexString(resultByteBuf.toByteArray());
                    resultByteBuf.reset();
                    if (TextUtils.isEmpty(hexData)) {
                        ToastUtils.show("保存失败");
                        return;
                    }
                    Timber.e("接收16进制串口数据: %s", hexData);
                    hexData = hexData.replace(" ", "").toUpperCase().trim();

                    if (hexData.equals("0110080D0001926A")) {//工作
                        ToastUtils.show("保存成功");
                    }
                }
                break;
            }
        }
    }
}
