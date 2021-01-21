package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.core.util.GsonFactory;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DevcieCurrentState;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/2 <br/>
 * 描述：     TODO
 */
public class QueryCurrentStateDialog extends BaseDispatchCmdDialog {
    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.contentView)
    View contentView;

    @BindView(R.id.tv_ext_power_volt)
    TextView mTvExtPowerVolt;

    @BindView(R.id.tv_firmware_version)
    TextView mTvFirmwareVersion;

    @BindView(R.id.tv_online_state)
    TextView mTvOnlineState;

    @BindView(R.id.tv_4g_signal)
    TextView mTv4gSignal;

    @BindView(R.id.tv_confirm)
    TextView mTvConfirm;

    private OnSeeDetailClickListener mListener;

    private DevcieCurrentState devcieCurrentState;


    public QueryCurrentStateDialog(String title, List<String> msgIDList) {
        this.title = title;
        this.msgIDList.clear();
        this.msgIDList.addAll(msgIDList);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_query_current_state_dialog;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        showResponseLoadingView();
        startRunnable(2000);
    }

    private void initView() {
        mTvTitle.setText(title);
        contentView.setVisibility(View.GONE);
    }

    @OnClick({R.id.iv_close, R.id.tv_confirm})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                stopRunnable();
                dismiss();
                break;

            case R.id.tv_confirm:
                stopRunnable();
                dismiss();
                if (mTvConfirm.getText().toString().equals("查看详情")) {
                    if (mListener != null) {
                        mListener.onSeeDetailClick(devcieCurrentState);
                    }
                }
                break;

            default:
                break;

        }
    }


    public void setOnSeeDetailClickListener(OnSeeDetailClickListener listener) {
        mListener = listener;
    }

    public interface OnSeeDetailClickListener {
        void onSeeDetailClick(DevcieCurrentState devcieCurrentState);
    }

    @Override
    protected void onQueryCmdResponseResultSuccess(QueryCmdResult queryCmdResult) {
        contentView.setVisibility(View.VISIBLE);

        IOTCommandResult<String> commandResult = IOTParseManager.getInstance().parse(queryCmdResult.getResponseContent());
        if (!commandResult.isSuccess()) {
            return;
        }
        String content = commandResult.getResult();
        content = content.replace("\\", "");
        content = content.replace("000_1:", "");
        try {
            devcieCurrentState = GsonFactory.getGson().fromJson(content, DevcieCurrentState.class);
            if (devcieCurrentState != null) {
                mTvExtPowerVolt.setText(String.format("%s V", devcieCurrentState.getExt_power_volt()));
                mTvFirmwareVersion.setText(TextUtils.isEmpty(devcieCurrentState.getSw_version()) ? "--" : devcieCurrentState.getSw_version());
                mTvOnlineState.setText(devcieCurrentState.isOn_4g() ? "在线" : "离线");
                if (devcieCurrentState.isOn_4g()) {
                    mTvOnlineState.setTextColor(GlobalUtil.getColor(R.color.text_color_3AD094));

                } else {
                    mTvOnlineState.setTextColor(GlobalUtil.getColor(R.color.red));
                }
                mTv4gSignal.setText(String.valueOf(devcieCurrentState.get_$4g_signal()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        mTvConfirm.setText("查看详情");
        mTvConfirm.setTextColor(GlobalUtil.getColor(R.color.blue_52B4F8));
    }

    @Override
    protected void onQueryCmdResponseResultTimeOut(QueryCmdResult queryCmdResult) {
        mTvConfirm.setText("好的");
        mTvConfirm.setTextColor(GlobalUtil.getColor(R.color.sub_title_text_color));
    }
}
