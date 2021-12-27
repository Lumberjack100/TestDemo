package com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.e40.E40EthernetEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.e40.E40EthernetInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/1/21 <br/>
 * 描述：    E40网络模式有线网络配置页面
 */
public class NetE40EthernetFragment extends BaseNetIotCommunicateFragment {

    @BindView(R.id.tv_dhcp)
    TextView mTvDhcp;

    @BindView(R.id.et_ip)
    ClearEditText mEtIpAddress;

    @BindView(R.id.et_gateway)
    ClearEditText mEtGateWayAddress;

    @BindView(R.id.et_dns)
    ClearEditText mEtDnsAddress;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    private int dhcpPos;

    private String dhcpOld;//
    private String dhcp;//
    private String ip;//
    private String gateway;//
    private String dns;//

    private E40EthernetInfo e40EthernetInfo;


    public static NetE40EthernetFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetE40EthernetFragment fragment = new NetE40EthernetFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.net_e40_ethernet_fragment;
    }

   @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setView();
        queryParamInfo();
    }

    private void setView() {
        mEtIpAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        mEtGateWayAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        mEtDnsAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
    }

    /**
     * 获取配置参数
     */
    private void queryParamInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.E40_MD_GET_ETHERNET);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    @OnClick({R.id.ll_dhcp, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.ll_dhcp) {
            showDhcpDialog();

        } else if (id == R.id.btn_confirm) {
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            processSave();
        }
    }

    /**
     * 选择DHCP
     */
    private void showDhcpDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"手动", "自动"},
                        null, dhcpPos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                dhcpPos = position;
                                mTvDhcp.setText(text);
                                if (position == 0) {
                                    dhcp = "0";
                                    onDhcpChanged(false);
                                } else {
                                    dhcp = "1";
                                    onDhcpChanged(true);
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    private boolean checkValueIsValid() {
        ip = mEtIpAddress.getText().toString().trim();
        gateway = mEtGateWayAddress.getText().toString().trim();
        dns = mEtDnsAddress.getText().toString().trim();
        if (TextUtils.isEmpty(ip)) {
            ToastUtils.show("请输入ip地址!");
            mEtIpAddress.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(gateway)) {
            ToastUtils.show("请输入网关!");
            mEtGateWayAddress.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(dns)) {
            ToastUtils.show("请输入DNS!");
            mEtDnsAddress.requestFocus();
            return false;
        }
        return true;
    }

    private void processSave() {
        E40EthernetEntity entity = new E40EthernetEntity();
        entity.setDhcp(dhcp);
        entity.setIp(ip);
        entity.setGateway(gateway);
        entity.setDns(dns);

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.E40_MD_SET_ETHERNET, entity);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            dismissWaitDialog();
            showDispatchFailedDialog(cmdStr);
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponse();
        }
    }

    /**
     * 指令下发失败弹框
     */
    private void showDispatchFailedDialog(String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case E40_MD_GET_ETHERNET:
                ToastUtils.show("下发指令失败");
                break;

            case E40_MD_SET_ETHERNET:
                ToastUtils.show("下发指令失败");
                break;

            default:
                break;
        }
    }

    /**
     * 查询指令响应结果出错
     *
     * @param errMsg
     */
    @Override
    protected void onQueryCmdResponseResultError(String errMsg) {
        super.onQueryCmdResponseResultError(errMsg);
        ToastUtils.show("指令响应错误");
    }

    /**
     * 查询指令响应结果超时
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultTimeOut(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultTimeOut(queryCmdResult);
        ToastUtils.show("指令响应超时");
    }

    /**
     * 查询指令响应结果成功
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultSuccess(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultSuccess(queryCmdResult);
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(queryCmdResult.getCmdEngName());
        switch (type) {
            case E40_MD_GET_ETHERNET: {
                IOTCommandResult<E40EthernetInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询有线网络参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                e40EthernetInfo = commandResult.getResult();
                initParamInfo();
            }
            break;

            case E40_MD_SET_ETHERNET: {
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

    private void doAfterSetting() {
        ToastUtils.show("保存成功");
        if (e40EthernetInfo != null) {
            e40EthernetInfo.setDhcp(dhcp);
            e40EthernetInfo.setIp(ip);
            e40EthernetInfo.setGateway(gateway);
            e40EthernetInfo.setDns(dns);
        }
        dhcpOld = dhcp;
    }

    private void initParamInfo() {
        if (e40EthernetInfo == null) {
            Timber.e("E40EthernetInfo 为空!");
            e40EthernetInfo = new E40EthernetInfo();
            return;
        }
        dhcpOld = e40EthernetInfo.getDhcp().trim();
        dhcp = e40EthernetInfo.getDhcp().trim();
        ip = e40EthernetInfo.getIp().trim();
        gateway = e40EthernetInfo.getGateway().trim();
        dns = e40EthernetInfo.getDns().trim();

        if (dhcpOld.equals("0")) {
            dhcpPos = 0;
            mTvDhcp.setText("手动");
            onDhcpChanged(false);
        } else {
            dhcpPos = 1;
            mTvDhcp.setText("自动");
            onDhcpChanged(true);
        }
        mEtIpAddress.setText(ip);
        mEtGateWayAddress.setText(gateway);
        mEtDnsAddress.setText(dns);
    }

    private void onDhcpChanged(boolean isAuto) {
        if (isAuto) {
            mEtIpAddress.setHint("");
            mEtGateWayAddress.setHint("");
            mEtDnsAddress.setHint("");

            mEtIpAddress.setEnabled(false);
            mEtGateWayAddress.setEnabled(false);
            mEtDnsAddress.setEnabled(false);

        } else {
            mEtIpAddress.setHint("请输入");
            mEtGateWayAddress.setHint("请输入");
            mEtDnsAddress.setHint("请输入");

            mEtIpAddress.setEnabled(true);
            mEtGateWayAddress.setEnabled(true);
            mEtDnsAddress.setEnabled(true);
        }
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
        if (dhcpOld != null && dhcp != null && !dhcpOld.equals(dhcp)) {
            return true;
        }
        if (ip != null && !ip.equals(mEtIpAddress.getText().toString().trim())) {
            return true;
        }
        if (gateway != null && !gateway.equals(mEtGateWayAddress.getText().toString().trim())) {
            return true;
        }
        if (dns != null && !dns.equals(mEtDnsAddress.getText().toString().trim())) {
            return true;
        }
        return false;
    }
}