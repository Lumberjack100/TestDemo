package com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.e40.E40CORSEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.e40.E40CORSInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.model.params.DispatchRawCmdParam;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2/26/21 <br/>
 * 描述：    E40网络模式 CORS 服务配置页面
 */
public class NetE40CorsServiceFragment extends BaseNetIotCommunicateFragment {
    public static final String PRO_DEVICE_INFO = "com.shmedo.mcloudapp.PRO_DEVICE_INFO";

    @BindView(R.id.contentLayout)
    ViewGroup contentLayout;

    @BindView(R.id.enableBtn)
    SwitchButton enableBtn;

    @BindView(R.id.et_target_address)
    ClearEditText mEtTargetAddress;

    @BindView(R.id.et_target_port)
    ClearEditText mEtTargetPort;

    @BindView(R.id.et_target_site_name)
    ClearEditText mEtTargetSiteName;

    @BindView(R.id.et_username)
    ClearEditText mEtUserName;

    @BindView(R.id.et_password)
    ClearEditText mEtPassword;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    private String targetAddress;//目标域名
    private String targetPort;//目标端口号
    private String targetSiteName;//目标站点名
    private String userName;
    private String password;

    private boolean enableButtonOriginalState;//使能开关初始状态，用于判断开关是否打开后没有设置参数就返回
    private boolean isSaveParamOperation = false;//判断当前是保存参数操作，还是关闭使能开关操作

    private ProjectDeviceInfo projectDeviceInfo;


    public static NetE40CorsServiceFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetE40CorsServiceFragment fragment = new NetE40CorsServiceFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectDeviceInfo = getArguments().getParcelable(PRO_DEVICE_INFO);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.net_e40_cors_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setView();
        setSwitchViewListener();
        queryParamInfo();
    }

    private void setView() {
        mEtTargetAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        mEtTargetPort.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtTargetSiteName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        mEtUserName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        mEtPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});

    }

    private void setSwitchViewListener() {
        enableBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定要关闭CORS服务？");
                } else {
                    contentLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * 关闭SwitchButton
     */
    private void showCloseSwitchButtonDialog(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title("温馨提示：")
                .content(content)
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        closeServer();//关闭服务器
                        contentLayout.setVisibility(View.GONE);
                        enableButtonOriginalState = enableBtn.isChecked();
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        enableBtn.setCheckedImmediatelyNoEvent(true);
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    /**
     * 获取CORS 服务配置参数
     */
    private void queryParamInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.E40_MD_GET_CORS);
        doCommonDispatchRawCmd(command);
    }

    /**
     * 关闭CORS 服务</br>
     */
    private void closeServer() {
        E40CORSEntity entity = new E40CORSEntity();
        entity.setSw("0");

        isSaveParamOperation = false;
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.E40_MD_SET_CORS, entity);
        doCommonDispatchRawCmd(command);
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.btn_confirm) {
            KeyBordUtils.hideSoftKeyboard(view);
            if (!checkValueIsValid()) {
                Timber.w("参数存在错误!");
                return;
            }
            processSave();
        }
    }

    private boolean checkValueIsValid() {
        targetAddress = mEtTargetAddress.getText().toString().trim();
        targetPort = mEtTargetPort.getText().toString().trim();
        targetSiteName = mEtTargetSiteName.getText().toString().trim();
        userName = mEtUserName.getText().toString().trim();
        password = mEtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(targetAddress)) {
            ToastUtils.show("请输入目标域名!");
            mEtTargetAddress.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(targetPort)) {
            ToastUtils.show("请输入目标端口号!");
            mEtTargetPort.requestFocus();
            return false;
        }
        try {
            int port = Integer.parseInt(targetPort);
            if (port < 0 || port > 65535) {
                ToastUtils.show("请输入正确的目标端口号!");
                mEtTargetPort.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的目标端口号!");
            mEtTargetPort.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(targetSiteName)) {
            ToastUtils.show("请输入目标站点名!");
            mEtTargetSiteName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(userName)) {
            ToastUtils.show("请输入用户名!");
            mEtUserName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            ToastUtils.show("请输入密码");
            mEtPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void processSave() {
        E40CORSEntity entity = new E40CORSEntity();
        entity.setSw("1");
        entity.setAddr(targetAddress);
        entity.setPort(targetPort);
        entity.setSta(targetSiteName);
        entity.setUser(userName);
        entity.setPswd(password);

        enableButtonOriginalState = enableBtn.isChecked();
        isSaveParamOperation = true;

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.E40_MD_SET_CORS, entity);
        doCommonDispatchRawCmd(command);
    }

    /**
     * 调用指令透传接口
     *
     * @param content
     */
    private void doCommonDispatchRawCmd(String content) {
        DispatchRawCmdParam rawCmdParam = new DispatchRawCmdParam();
        rawCmdParam.setContent(content);
        rawCmdParam.setCompanyID(MCloudApp.getCompanyID());
        rawCmdParam.setDeviceIDList(Arrays.asList(projectDeviceInfo.getId()));

        showProgressDialog("处理中...");
        processDispatchRawCmd(rawCmdParam);
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            dismissProgressDialog();
            showDispatchFailedDialog(cmdStr);
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponseRunnable(2000);
        }
    }

    /**
     * 指令下发失败弹框
     */
    private void showDispatchFailedDialog(String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case E40_MD_GET_CORS:
                ToastUtils.show("下发指令失败");
                break;

            case E40_MD_SET_CORS:
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
            case E40_MD_GET_CORS: {
                IOTCommandResult<E40CORSInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询 CORS 服务参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                initParamInfo(commandResult.getResult());
            }
            break;

            case E40_MD_SET_CORS: {
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置 CORS 服务参数出错!", cmdResult.getReason());
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
        if (isSaveParamOperation) {
            ToastUtils.show("保存成功");
        }
    }

    private void initParamInfo(E40CORSInfo e40CORSInfo) {
        if (e40CORSInfo == null) {
            Timber.e("E40CORSInfo 为空!");
            return;
        }
        targetAddress = e40CORSInfo.getAddr().trim();
        targetPort = e40CORSInfo.getPort().trim();
        targetSiteName = e40CORSInfo.getSta().trim();
        userName = e40CORSInfo.getUser().trim();
        password = e40CORSInfo.getPswd().trim();

        if (e40CORSInfo.getSw().trim().equals("1")) {
            enableButtonOriginalState = true;
            enableBtn.setCheckedImmediatelyNoEvent(true);
            contentLayout.setVisibility(View.VISIBLE);

        } else {
            enableButtonOriginalState = false;
            enableBtn.setCheckedImmediatelyNoEvent(false);
            contentLayout.setVisibility(View.GONE);
        }
        mEtTargetAddress.setText(targetAddress);
        mEtTargetPort.setText(targetPort);
        mEtTargetSiteName.setText(targetSiteName);
        mEtUserName.setText(userName);
        mEtPassword.setText(password);
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
        if (!enableBtn.isChecked()) {
            return false;
        }
        if (enableButtonOriginalState != enableBtn.isChecked()) {
            return true;
        }

        if (targetAddress != null && !targetAddress.equals(mEtTargetAddress.getText().toString().trim())) {
            return true;
        }
        if (targetPort != null && !targetPort.equals(mEtTargetPort.getText().toString().trim())) {
            return true;
        }
        if (targetSiteName != null && !targetSiteName.equals(mEtTargetSiteName.getText().toString().trim())) {
            return true;
        }
        if (userName != null && !userName.equals(mEtUserName.getText().toString().trim())) {
            return true;
        }
        if (password != null && !password.equals(mEtPassword.getText().toString().trim())) {
            return true;
        }
        return false;
    }
}