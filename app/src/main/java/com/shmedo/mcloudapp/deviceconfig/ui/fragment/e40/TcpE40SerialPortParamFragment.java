package com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.e40.E40SerialPortEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.e40.E40SerialPortInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.tcpcommon.BaseTcpIotCommunicateFragment;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/5/18 <br/>
 * 描述：     E40 TCP模式串口配置页面
 */
public class TcpE40SerialPortParamFragment extends BaseTcpIotCommunicateFragment {
    @BindView(R.id.tv_type)
    TextView mTvType;

    @BindView(R.id.et_baud)
    ClearEditText mEtBaud;

    private int typePos;

    private String typeOld;//
    private String type;//
    private String baud;//

    private E40SerialPortInfo e40SerialPortInfo;

    public static TcpE40SerialPortParamFragment newInstance() {
        TcpE40SerialPortParamFragment fragment = new TcpE40SerialPortParamFragment();
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.e40_serial_port_param_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setView();
        queryParamInfo();
    }

    private void setView() {
        mEtBaud.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
    }

    /**
     * 获取配置参数
     */
    private void queryParamInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.E40_MD_GET_DB_GUART);
        sendCommand(command);
    }

    @OnClick({R.id.ll_type, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        if (!tcpViewModel.getConnectStatus()) {
            ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
            return;
        }
        int id = view.getId();
        if (id == R.id.ll_type) {
            showDhcpDialog();

        } else if (id == R.id.btn_confirm) {
            KeyBordUtils.hideSoftKeyboard(view);
            if (!checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            processSave();
        }
    }

    /**
     * 选择输出的数据格式
     */
    private void showDhcpDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"CMD", "NMEA", "RAW_OUT"},
                        null, typePos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                typePos = position;
                                mTvType.setText(text);
                                switch (text) {
                                    case "CMD":
                                        type = "1";
                                        break;

                                    case "NMEA":
                                        type = "2";
                                        break;

                                    case "RAW_OUT":
                                        type = "5";
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    private boolean checkValueIsValid() {
        baud = mEtBaud.getText().toString().trim();
        if (TextUtils.isEmpty(baud)) {
            ToastUtils.show("请输入波特率!");
            mEtBaud.requestFocus();
            return false;
        }
        return true;
    }

    private void processSave() {
        E40SerialPortEntity entity = new E40SerialPortEntity();
        entity.setType(type);
        entity.setBaud(baud);

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.E40_MD_SET_DB_GUART, entity);
        sendCommand(command);
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case E40_MD_GET_DB_GUART: {
                IOTCommandResult<E40SerialPortInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询有线网络参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                e40SerialPortInfo = commandResult.getResult();
                initParamInfo();
            }
            break;

            case E40_MD_SET_DB_GUART: {
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置有线网络参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                doAfterSetting();
            }
            break;

            default:
                break;
        }
    }
    private void initParamInfo() {
        if (e40SerialPortInfo == null) {
            Timber.e("E40SerialPortInfo 为空!");
            e40SerialPortInfo = new E40SerialPortInfo();
            return;
        }
        typeOld = e40SerialPortInfo.getType().trim();
        type = e40SerialPortInfo.getType().trim();
        baud = e40SerialPortInfo.getBaud().trim();

        if (typeOld.equals("1")) {
            typePos = 0;
            mTvType.setText("CMD");
        } else if (typeOld.equals("2")) {
            typePos = 1;
            mTvType.setText("NMEA");
        } else if (typeOld.equals("5")) {
            typePos = 2;
            mTvType.setText("RAW_OUT");
        }
        mEtBaud.setText(baud);
    }

    private void doAfterSetting() {
        ToastUtils.show("保存成功");
        if (e40SerialPortInfo != null) {
            e40SerialPortInfo.setType(type);
            e40SerialPortInfo.setBaud(baud);
        }
        typeOld = type;
    }

    @Override
    public boolean onBackPressed() {
        if (checkValueIsChange()) {
            warnNotYetSettingBeforeLeavePage();
            return true;
        } else {
            return false;
        }
    }

    private boolean checkValueIsChange() {
        if (typeOld != null && type != null && !typeOld.equals(type)) {
            return true;
        }
        if (baud != null && !baud.equals(mEtBaud.getText().toString().trim())) {
            return true;
        }
        return false;
    }
}
