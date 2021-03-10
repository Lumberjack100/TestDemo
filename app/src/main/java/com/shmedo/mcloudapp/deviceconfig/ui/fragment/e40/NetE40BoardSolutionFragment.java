package com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.e40.E40BoardSolutionEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.e40.E40BoardSolutionInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/1/21 <br/>
 * 描述：    E40网络模式板卡解算配置页面
 */
public class NetE40BoardSolutionFragment extends BaseNetIotCommunicateFragment {
    @BindView(R.id.et_init_time)
    ClearEditText mEtInitTime;

    @BindView(R.id.et_solution_time)
    ClearEditText mEtSolutionTime;

    @BindView(R.id.tv_smooth_level)
    TextView mTvSmoothLevel;

    @BindView(R.id.tv_reinit)
    TextView mTvReinit;

    @BindView(R.id.tv_rtk_dynamic_mode)
    TextView mTvRtkDynamicMode;

    @BindView(R.id.et_deformation_correction_value)
    ClearEditText mEtDeformationCorrectionValue;

    @BindView(R.id.et_correction_level)
    ClearEditText mEtCorrectionLevel;

    @BindView(R.id.maskLayerLayout)
    ViewGroup maskLayerLayout;

    private int smoothLevelPos;
    private int reInitPos;
    private int rtkDynamicModePos;

    private String initTime;//初始化时间
    private String solutionTime;//解算时间
    private String smoothLevelOld;//平滑等级
    private String smoothLevel;
    private String reinitOld;// 重新初始化
    private String reInit;
    private String rtkDynamicModeOld;//
    private String rtkDynamicMode;
    private String correctionValue;
    private String correctionLevel;

    private DecimalFormat decimalFormat = new DecimalFormat();

    private E40BoardSolutionInfo e40BoardSolutionInfo;


    public static NetE40BoardSolutionFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetE40BoardSolutionFragment fragment = new NetE40BoardSolutionFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.net_e40_board_solution_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setView();
        queryParamInfo();
    }

    private void setView() {
        mEtInitTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtSolutionTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtDeformationCorrectionValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtCorrectionLevel.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
    }

    /**
     * 获取配置参数
     */
    private void queryParamInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.E40_MD_GET_BOARDSOLUTION);
        showProgressDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    @OnClick({R.id.ll_smooth_level, R.id.ll_reinit, R.id.ll_rtk_dynamic_mode, R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.ll_smooth_level) {
            showSmoothLevelDialog();

        } else if (id == R.id.ll_reinit) {
            showReinitDialog();

        } else if (id == R.id.ll_rtk_dynamic_mode) {
            showRtkDynamicModeDialog();

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
     * 选择平滑等级
     */
    private void showSmoothLevelDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"标准", "突变", "缓变"},
                        null, smoothLevelPos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                smoothLevelPos = position;
                                mTvSmoothLevel.setText(text);
                                if (position == 0) {
                                    smoothLevel = "0";
                                } else if (position == 1) {
                                    smoothLevel = "1";
                                } else {
                                    smoothLevel = "2";
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    /**
     * 是否重新初始化
     */
    private void showReinitDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"是", "否"},
                        null, reInitPos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                reInitPos = position;
                                mTvReinit.setText(text);
                                if (position == 0) {
                                    reInit = "0";
                                } else {
                                    reInit = "1";
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    /**
     * 选择 RTK Dynamic mode
     */
    private void showRtkDynamicModeDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"Static", "Dynamic"},
                        null, rtkDynamicModePos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                rtkDynamicModePos = position;
                                mTvRtkDynamicMode.setText(text);
                                if (position == 0) {
                                    rtkDynamicMode = "0";
                                } else {
                                    rtkDynamicMode = "1";
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    private boolean checkValueIsValid() {
        initTime = mEtInitTime.getText().toString().trim();
        solutionTime = mEtSolutionTime.getText().toString().trim();
        correctionValue = mEtDeformationCorrectionValue.getText().toString().trim();
        correctionLevel = mEtCorrectionLevel.getText().toString().trim();

        if (TextUtils.isEmpty(initTime)) {
            ToastUtils.show("请输入初始时间!");
            mEtInitTime.requestFocus();
            return false;
        }
        try {
            int port = Integer.parseInt(initTime);
            if (port < 0 || port > 65535) {
                ToastUtils.show("请输入正确的初始时间!");
                mEtInitTime.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的初始时间!");
            mEtInitTime.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(solutionTime)) {
            ToastUtils.show("请输入解算时间!");
            mEtSolutionTime.requestFocus();
            return false;
        }
        try {
            int port = Integer.parseInt(solutionTime);
            if (port < 0 || port > 65535) {
                ToastUtils.show("请输入正确的解算时间!");
                mEtSolutionTime.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的解算时间!");
            mEtSolutionTime.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(correctionValue)) {
            ToastUtils.show("请输入形变修正值!");
            mEtDeformationCorrectionValue.requestFocus();
            return false;
        }
        try {
            double port = Double.parseDouble(correctionValue);
//            if (port < 0 || port > 65535) {
//                ToastUtils.show("请输入正确的形变修正值!");
//                mEtDeformationCorrectionValue.requestFocus();
//                return false;
//            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的形变修正值!");
            mEtDeformationCorrectionValue.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(correctionLevel)) {
            ToastUtils.show("请输入电层修正等级!");
            mEtCorrectionLevel.requestFocus();
            return false;
        }
        try {
            int port = Integer.parseInt(correctionLevel);
            if (port < 0 || port > 1000) {
                ToastUtils.show("请输入正确的电层修正等级!");
                mEtCorrectionLevel.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的电层修正等级!");
            mEtCorrectionLevel.requestFocus();
            return false;
        }
        return true;
    }

    private void processSave() {
        try {
            E40BoardSolutionEntity entity = new E40BoardSolutionEntity();
            entity.setInittime(initTime);
            entity.setCalcgap(solutionTime);
            entity.setSmoothlevel(smoothLevel);
            entity.setReinit(reInit);
            entity.setRtkdynamicmode(rtkDynamicMode);
            decimalFormat.applyPattern("#.##");
            entity.setCorrval(decimalFormat.format(Double.parseDouble(correctionValue)));

            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.E40_MD_SET_BOARDSOLUTION, entity);
            showProgressDialog("处理中...");
            doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
            case E40_MD_GET_BOARDSOLUTION:
                ToastUtils.show("下发指令失败");
                break;

            case E40_MD_SET_BOARDSOLUTION:
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
            case E40_MD_GET_BOARDSOLUTION: {
                IOTCommandResult<E40BoardSolutionInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询板卡解算参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                e40BoardSolutionInfo = commandResult.getResult();
                initParamInfo();
            }
            break;

            case E40_MD_SET_BOARDSOLUTION: {
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置板卡解算参数出错!", cmdResult.getReason());
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
        if (e40BoardSolutionInfo != null) {
            e40BoardSolutionInfo.setInittime(initTime);
            e40BoardSolutionInfo.setCalcgap(solutionTime);
            e40BoardSolutionInfo.setSmoothlevel(smoothLevel);
            e40BoardSolutionInfo.setReinit(reInit);
            e40BoardSolutionInfo.setRtkdynamicmode(rtkDynamicMode);
            e40BoardSolutionInfo.setCorrval(correctionValue);
        }
        smoothLevelOld = smoothLevel;
        reinitOld = reInit;
        rtkDynamicModeOld = rtkDynamicMode;
    }

    private void initParamInfo() {
        if (e40BoardSolutionInfo == null) {
            Timber.e("E40BoardSolutionInfo 为空!");
            return;
        }
        initTime = e40BoardSolutionInfo.getInittime().trim();
        solutionTime = e40BoardSolutionInfo.getCalcgap().trim();
        smoothLevelOld = e40BoardSolutionInfo.getSmoothlevel().trim();
        smoothLevel = e40BoardSolutionInfo.getSmoothlevel().trim();
        reinitOld = e40BoardSolutionInfo.getReinit().trim();
        reInit = e40BoardSolutionInfo.getReinit().trim();
        rtkDynamicModeOld = e40BoardSolutionInfo.getRtkdynamicmode().trim();
        rtkDynamicMode = e40BoardSolutionInfo.getRtkdynamicmode().trim();
        correctionValue = e40BoardSolutionInfo.getCorrval().trim();
        correctionLevel = "";

        try {
            mEtInitTime.setText(initTime);
            mEtSolutionTime.setText(solutionTime);
            if (smoothLevelOld.equals("0")) {
                smoothLevelPos = 0;
                mTvSmoothLevel.setText("标准");
            } else if (smoothLevelOld.equals("1")) {
                smoothLevelPos = 1;
                mTvSmoothLevel.setText("突变");
            } else {
                smoothLevelPos = 2;
                mTvSmoothLevel.setText("缓变");
            }

            if (reinitOld.equals("0")) {
                reInitPos = 0;
                mTvReinit.setText("是");
            } else {
                reInitPos = 1;
                mTvReinit.setText("否");
            }

            if (rtkDynamicModeOld.equals("0")) {
                rtkDynamicModePos = 0;
                mTvRtkDynamicMode.setText("Static");
            } else {
                rtkDynamicModePos = 1;
                mTvRtkDynamicMode.setText("Dynamic");
            }

            decimalFormat.applyPattern("#.##");
            correctionValue = decimalFormat.format(Double.parseDouble(correctionValue));
            mEtDeformationCorrectionValue.setText(correctionValue);
            mEtCorrectionLevel.setText(correctionLevel);
        } catch (Exception ex) {
            ex.printStackTrace();
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
        if (initTime != null && !initTime.equals(mEtInitTime.getText().toString().trim())) {
            return true;
        }
        if (solutionTime != null && !solutionTime.equals(mEtSolutionTime.getText().toString().trim())) {
            return true;
        }
        if (smoothLevelOld != null && smoothLevel != null && !smoothLevelOld.equals(smoothLevel)) {
            return true;
        }
        if (reinitOld != null && reInit != null && !reinitOld.equals(reInit)) {
            return true;
        }
        if (rtkDynamicModeOld != null && rtkDynamicMode != null && !rtkDynamicModeOld.equals(rtkDynamicMode)) {
            return true;
        }
        if (correctionValue != null && !correctionValue.equals(mEtDeformationCorrectionValue.getText().toString().trim())) {
            return true;
        }
        if (correctionLevel != null && !correctionLevel.equals(mEtCorrectionLevel.getText().toString().trim())) {
            return true;
        }
        return false;
    }
}