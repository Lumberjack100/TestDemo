package com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.Inclinometer_debug_box;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.at.ATCommand;
import com.shmedo.configlibrary.at.WHBLE102CommandType;
import com.shmedo.core.usbserial.livedata.state.USBConnectionState;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.BaseUSBSerialCommunicateFragment;
import com.shmedo.mcloudapp.util.TextUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wandersnail.widget.textview.RoundButton;
import cn.wandersnail.widget.textview.RoundTextView;

public class InclinometerDebugBoxLoggerFragment extends BaseUSBSerialCommunicateFragment {

    @BindView(R.id.controlLines)
    ViewGroup controlLinesView;

    @BindView(R.id.receive_text)
    TextView mTvReceiveText;//

    @BindView(R.id.btnFirst)
    RoundButton btnFirst;

    @BindView(R.id.btnSecond)
    RoundButton btnSecond;

    @BindView(R.id.ivExpand)
    ImageView ivExpand;

//    @BindView(R.id.etAsciiValue)
//    ClearEditText mEtAsciiValue;//

    @BindView(R.id.etHexValue)
    ClearEditText mEtHexValue;//

    @BindView(R.id.btnSend)
    RoundButton btnSend;

    @BindView(R.id.chkHex)
    CheckBox chkHex;

    @BindView(R.id.tvTailNewLine)
    RoundTextView tvTailNewLine;

    @BindView(R.id.bottomRootView)
    ViewGroup bottomRootView;

    @BindView(R.id.layoutSettings)
    ViewGroup layoutSettingsView;

    private TextUtil.HexWatcher hexWatcher;

    private boolean hexEnabled = false;
    private boolean controlLinesEnabled = true;
    private boolean pendingNewline = false;
    private String newline = ATCommand.NEWLINE_CRLF;

    private int layoutSettingsHeight;


    public static InclinometerDebugBoxLoggerFragment newInstance() {
        return new InclinometerDebugBoxLoggerFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.inclinometer_debug_box_logger_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observerConnectionState();
        mTvReceiveText.setTextColor(getResources().getColor(R.color.orange_FF7502)); // set as default color to reduce number of spans
        mTvReceiveText.setMovementMethod(ScrollingMovementMethod.getInstance());

        hexWatcher = new TextUtil.HexWatcher(mEtHexValue);
        hexWatcher.enable(hexEnabled);
        mEtHexValue.addTextChangedListener(hexWatcher);
        mEtHexValue.setHint(hexEnabled ? getResources().getString(R.string.hex_characters) : "");

        chkHex.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                hexEnabled = isChecked;
                mEtHexValue.setText("");
                hexWatcher.enable(hexEnabled);
                mEtHexValue.setHint(hexEnabled ? "HEX mode" : "");
            }
        });

        layoutSettingsView.measure(0, 0);
        layoutSettingsHeight = layoutSettingsView.getMeasuredHeight();
    }

    /**
     * 观察连接状态变化
     */
    private void observerConnectionState() {
        usbSerialViewModel.getConnectionState().observe(getViewLifecycleOwner(), new Observer<USBConnectionState>() {
            @Override
            public void onChanged(USBConnectionState usbConnectionState) {
                switch (usbConnectionState.getState()) {
                    case READY://The initialization is complete, and the device is ready to use.
                        onConnectionStateChanged(true);
                        break;

                    case DISCONNECTED://The device disconnected or failed to connect.
                        onConnectionStateChanged(false);
                        break;
                }
            }
        });
    }

    /**
     * USB连接/断开回调，更新页面头部信息
     *
     * @param isConnected
     */
    private void onConnectionStateChanged(boolean isConnected) {
        setBottomViewState(bottomRootView, isConnected);
    }

    private void setBottomViewState(ViewGroup paramViewGroup, boolean paramBoolean) {
        int count = paramViewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View childView = paramViewGroup.getChildAt(i);
            if (childView.getId() != R.id.ivExpand) {
                childView.setEnabled(paramBoolean);
                if (childView instanceof ViewGroup)
                    setBottomViewState((ViewGroup) childView, paramBoolean);
            }
        }
    }

    @OnClick({R.id.btnFirst, R.id.btnSecond, R.id.btnThird, R.id.btnFourth, R.id.ivExpand, R.id.tvTailNewLine, R.id.btnSend})
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnFirst) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.usb_config_disconnect_warn));
                return;
            }
            enterCommand();

        } else if (id == R.id.btnSecond) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.usb_config_disconnect_warn));
                return;
            }
            queryLink();

        } else if (id == R.id.btnThird) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.usb_config_disconnect_warn));
                return;
            }
            exitCommand();

        } else if (id == R.id.btnFourth) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.usb_config_disconnect_warn));
                return;
            }
            send("01 03 04 08 00 01 04 F8");

        } else if (id == R.id.ivExpand) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layoutSettingsView.getLayoutParams();
            if (layoutParams.height == 0) {
                layoutParams.height = layoutSettingsHeight;
                layoutSettingsView.setLayoutParams(layoutParams);
                ivExpand.setRotation(0f);
            } else {
                layoutParams.height = 0;
                layoutSettingsView.setLayoutParams(layoutParams);
                ivExpand.setRotation(180f);
            }
        } else if (id == R.id.tvTailNewLine) {
            String[] newlineNames = getResources().getStringArray(R.array.newline_names);
            String[] newlineValues = getResources().getStringArray(R.array.newline_values);
            int pos = Arrays.asList(newlineValues).indexOf(newline);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Newline");
            builder.setSingleChoiceItems(newlineNames, pos, (dialog, item1) -> {
                newline = newlineValues[item1];
                tvTailNewLine.setText(newlineNames[item1]);
                dialog.dismiss();
            });
            builder.create().show();

        } else if (id == R.id.btnSend) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.usb_config_disconnect_warn));
                return;
            }
            send(mEtHexValue.getText().toString());
        }
    }

    /**
     * 进入命令模式
     */
    private void enterCommand() {
        String cmd = WHBLE102CommandType.ENTER_COMMAND.toString();
        SpannableStringBuilder spn = new SpannableStringBuilder(cmd + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.testspeed_result_bg)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvReceiveText.append(spn);
        usbSerialViewModel.sendData(cmd.getBytes());
    }

    /**
     * 查询蓝牙测斜仪设备连接状态
     */
    private void queryLink() {
        String cmd = ATCommand.COMMAND_HEADER + WHBLE102CommandType.LINK.toString() + ATCommand.QUERY_FLAG;
        byte[] data = (cmd + newline).getBytes();

        SpannableStringBuilder spn = new SpannableStringBuilder(cmd + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.testspeed_result_bg)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvReceiveText.append(spn);
        usbSerialViewModel.sendData(data);
    }

    /**
     * 退出命令行模式
     */
    private void exitCommand() {
        String cmd = ATCommand.COMMAND_HEADER + WHBLE102CommandType.ENTM.toString();
        byte[] data = (cmd + newline).getBytes();

        SpannableStringBuilder spn = new SpannableStringBuilder(cmd + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.testspeed_result_bg)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvReceiveText.append(spn);
        usbSerialViewModel.sendData(data);
    }

    private void send(String str) {
        String msg;
        byte[] data;
        if (hexEnabled) {
            StringBuilder sb = new StringBuilder();
            TextUtil.toHexString(sb, TextUtil.fromHexString(str));
            TextUtil.toHexString(sb, newline.getBytes());
            msg = sb.toString();
            data = TextUtil.fromHexString(msg);
        } else {
            msg = str;
            data = (msg + newline).getBytes();
        }
        SpannableStringBuilder spn = new SpannableStringBuilder(msg + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.testspeed_result_bg)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvReceiveText.append(spn);
        usbSerialViewModel.sendData(data);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_inclinometer_debug_box, menu);
        menu.findItem(R.id.controlLines).setChecked(controlLinesEnabled);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clear) {
            mTvReceiveText.setText("");
            return true;
        } else if (id == R.id.controlLines) {
            controlLinesEnabled = !controlLinesEnabled;
            item.setChecked(controlLinesEnabled);
            controlLinesView.setVisibility(controlLinesEnabled ? View.VISIBLE : View.GONE);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void parseResponseMessage(byte[] data) {
        if (hexEnabled) {
            mTvReceiveText.append(TextUtil.toHexString(data) + '\n');
        } else {
            String msg = new String(data);
            if (newline.equals(TextUtil.newline_crlf) && msg.length() > 0) {
                // don't show CR as ^M if directly before LF
                msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf);
                // special handling if CR and LF come in separate fragments
                if (pendingNewline && msg.charAt(0) == '\n') {
                    Editable edt = mTvReceiveText.getEditableText();
                    if (edt != null && edt.length() > 1)
                        edt.replace(edt.length() - 2, edt.length(), "");
                }
                pendingNewline = msg.charAt(msg.length() - 1) == '\r';
            }
            mTvReceiveText.append(TextUtil.toCaretString(msg, newline.length() != 0));
        }
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {

    }

}