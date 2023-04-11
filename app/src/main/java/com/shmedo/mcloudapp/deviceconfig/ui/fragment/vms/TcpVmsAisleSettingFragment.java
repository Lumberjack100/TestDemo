package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.DebouncingUtils;
import com.blankj.utilcode.util.StringUtils;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.vms.VmsAisleNumberEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.VmsAisleNumber;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.vms.VmsAisleInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.view.vms.VmsAisleSettingView;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;


/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20 <br/>
 * 描述：     Vms 网关通道控制参数配置
 */
public class TcpVmsAisleSettingFragment extends BaseVmsTcpCommunicateFragment {
    @BindView(R.id.vmsAisleSettingView)
    VmsAisleSettingView vmsAisleSettingView;

    @BindView(R.id.maskLayerLayout)
    ViewGroup maskLayerLayout;

    private static final String VMS_AISLE_NUMBER = "vms_aisle_number";
    private VmsAisleNumber vmsAisleNumber;

    public static TcpVmsAisleSettingFragment newInstance(VmsAisleNumber vmsAisleNumber) {
        TcpVmsAisleSettingFragment fragment = new TcpVmsAisleSettingFragment();
        Bundle args = new Bundle();
        args.putSerializable(VMS_AISLE_NUMBER, vmsAisleNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vmsAisleNumber = (VmsAisleNumber) getArguments().getSerializable(VMS_AISLE_NUMBER);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.vms_aisle_setting_fragment;
    }

   @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vmsAisleSettingView.vmsAisleNumber = vmsAisleNumber;
        queryVmsAisleInfo();
    }

    /**
     * 获取网关不同通道下的控制参数
     */
    private void queryVmsAisleInfo() {
        VmsAisleNumberEntity vmsAisleNumberEntity = new VmsAisleNumberEntity(vmsAisleNumber.toInt());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_GET_GATEWAY_PARAM, vmsAisleNumberEntity);
        sendCommand(command);
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View view) {
        if(!DebouncingUtils.isValid(view, 1000)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.btn_confirm) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!tcpViewModel.getConnectStatus()) {
                ToastUtils.show(StringUtils.getString(R.string.tcp_config_disconnect_warn));
                return;
            }
            if (!vmsAisleSettingView.checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            String command = vmsAisleSettingView.getSetCommand();
            if (!TextUtils.isEmpty(command)) {
                showProgressDialog("处理中...");
                sendCommand(command);
            }
        }
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case VMS_MD_GET_GATEWAY_PARAM: {//获取网关通道的控制参数
//                stopProgressRunnable();
                IOTCommandResult<VmsAisleInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询网关通道的控制参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(commandResult.getMessage().contains("unsupported") ? "设备版本不支持!" : errMsg);
                    maskLayerLayout.setVisibility(commandResult.getMessage().contains("unsupported") ? View.VISIBLE : View.GONE);
                    return;
                }
                vmsAisleSettingView.vmsAisleInfo = commandResult.getResult();
                vmsAisleSettingView.initParamConfigInfo();
            }
            break;

            case VMS_MD_SET_GATEWAY_PARAM: {//设置网关通道的控制参数
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置参数失败!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show("设置成功");
            }
            break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        if (tcpViewModel.getConnectStatus()) {
            if (vmsAisleSettingView.checkValueIsChange()) {
                warnNotYetSettingBeforeLeavePage();
                return true;
            } else {
                return false;
            }
        }

        return false;
    }
}