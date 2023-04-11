package com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.DebouncingUtils;
import com.blankj.utilcode.util.StringUtils;
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
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.tcpcommon.BaseTcpIotCommunicateFragment;

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

    @BindView(R.id.tv_baud)
    TextView mTvBaud;

    private int typePos;
    private int baudPos;

    private String typeOld;//
    private String type;//
    private String baudOld;//
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        queryParamInfo();
    }


    /**
     * 获取配置参数
     */
    private void queryParamInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.E40_MD_GET_DB_GUART);
        sendCommand(command);
    }

    @OnClick({R.id.ll_type, R.id.ll_baud, R.id.btn_confirm})
    public void onClick(View view) {
        if(!DebouncingUtils.isValid(view, 1000)) {
            return;
        }
        if (!tcpViewModel.getConnectStatus()) {
            ToastUtils.show(StringUtils.getString(R.string.tcp_config_disconnect_warn));
            return;
        }
        int id = view.getId();
        if (id == R.id.ll_type) {
            showTypeDialog();

        } else if (id == R.id.ll_baud) {
            showBaudDialog();

        } else if (id == R.id.btn_confirm) {
            processSave();
        }
    }

    /**
     * 选择输出的数据格式
     */
    private void showTypeDialog() {
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"CMD", "NMEA", "RAW_OUT"},
                        null, typePos,
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
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    /**
     * 选择波特率
     */
    private void showBaudDialog() {
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"9600", "115200"},
                        null, baudPos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                baudPos = position;
                                baud = text;
                                mTvBaud.setText(text);
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
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
        baudOld = e40SerialPortInfo.getBaud().trim();
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

        if (baudOld.equals("9600")) {
            baudPos = 0;
        } else if (baudOld.equals("115200")) {
            baudPos = 1;
        }
        mTvBaud.setText(baudOld);
    }

    private void doAfterSetting() {
        ToastUtils.show("保存成功");
        if (e40SerialPortInfo != null) {
            e40SerialPortInfo.setType(type);
            e40SerialPortInfo.setBaud(baud);
        }
        typeOld = type;
        baudOld = baud;
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
        if (baudOld != null && baud != null && !baudOld.equals(baud)) {
            return true;
        }
        return false;
    }
}
