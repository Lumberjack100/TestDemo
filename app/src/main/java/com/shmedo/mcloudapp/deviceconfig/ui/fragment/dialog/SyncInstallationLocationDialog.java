package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.util.DeviceInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.LocationViewModel;
import com.shmedo.mcloudapp.entity.SyncPositionBean;
import com.shmedo.mcloudapp.util.KeyBordUtils;
import com.shmedo.mcloudapp.util.LocationUtils;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/16/20 <br/>
 * 描述：     TODO
 */
public class SyncInstallationLocationDialog extends BaseDialogFragment {
    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.et_latitude_longitude)
    ClearEditText mEtLatLong;

    @BindView(R.id.et_location)
    ClearEditText mEtLocation;

    private Activity activity;

    private LocationViewModel locationViewModel;

    private DialogFragmentClickListener mListener;


    public SyncInstallationLocationDialog(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_sync_install_location_dialog;
    }

    protected void setWindowStyle(int gravity) {
        super.setWindowStyle(Gravity.CENTER);
        Dialog mDialog = getDialog();
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = (int) (DeviceInfo.getScreenWidth() * 0.8f);
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        locationViewModel = getApplicationScopeViewModel(LocationViewModel.class);
        initView();
        locationViewModel.getSyncPositionBean().observeInFragment(this, new Observer<SyncPositionBean>() {
            @Override
            public void onChanged(SyncPositionBean syncPositionBean) {
                String address = syncPositionBean.getAddress();
                String latLong = String.format(Locale.getDefault(), "%.6f", syncPositionBean.getLongitude()) + "," + String.format(Locale.getDefault(), "%.6f", syncPositionBean.getLatitude());
                mEtLatLong.setText(latLong);
                mEtLocation.setText(address);
                LocationUtils.getInstance().stopLocalService();
            }
        });
    }

    private void initView() {
        mTvTitle.setText("同步安装位置");
    }


    @OnClick({R.id.iv_close, R.id.iv_locate, R.id.tv_cancel, R.id.tv_confirm})
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_close) {
            KeyBordUtils.hideSoftKeyboard(mEtLatLong);
            LocationUtils.getInstance().stopLocalService();
            dismiss();

        } else if (id == R.id.iv_locate) {
            LocationUtils.getInstance().getPositionPermission(activity);

        } else if (id == R.id.tv_cancel) {
            KeyBordUtils.hideSoftKeyboard(mEtLatLong);
            LocationUtils.getInstance().stopLocalService();
            dismiss();

        } else if (id == R.id.tv_confirm) {
            String result = mEtLatLong.getText().toString().trim();
            if (TextUtils.isEmpty(result)) {
                ToastUtils.show("位置信息不能为空");
                return;
            }
//            try {
//                InstallLocationEntity installLocationEntity = new InstallLocationEntity(1);
//                String command = CommandManager.getInstance().getCommand(CommandType.INSTALL_LOCATION, installLocationEntity);
//                activity.sendCommonCommandImmediately(command);
//                Timber.i("查询安装位置：%s", command);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            ToastUtils.show("位置信息同步成功");
//            KeyBordUtils.hideSoftKeyboard(mEtLatLong);
//            LocationUtils.getInstance().stopLocalService();
//            dismiss();
        }
    }

    public void setDialogFragmentClickListener(DialogFragmentClickListener listener) {
        mListener = listener;
    }
}
