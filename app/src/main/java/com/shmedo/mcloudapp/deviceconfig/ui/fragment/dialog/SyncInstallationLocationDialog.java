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

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.util.DeviceInfo;
import com.shmedo.core.util.JZLocationConverter;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.LocationViewModel;
import com.shmedo.mcloudapp.entity.SyncPositionBean;
import com.shmedo.mcloudapp.maps.util.MapErrorUtil;
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

    @BindView(R.id.tv_address)
    TextView mTvAddress;

    private Activity activity;

    private LocationViewModel locationViewModel;

    private DialogFragmentClickListener mListener;

    private String installLocation;
    private LatLng latLng;

    public SyncInstallationLocationDialog(Activity activity) {
        this.activity = activity;
    }

    public SyncInstallationLocationDialog(Activity activity, String installLocation) {
        this.activity = activity;
        this.installLocation = installLocation;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_sync_install_location_dialog;
    }

    @Override
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
        locationViewModel.getSyncPositionBean().observeInFragment(this, new Observer<SyncPositionBean>() {
            @Override
            public void onChanged(SyncPositionBean syncPositionBean) {
                String address = syncPositionBean.getAddress();
                mTvAddress.setText(address);
                try {
                    //将高德坐标(即GCJ-02火星坐标)转换为WGS-84世界标准地理坐标
                    JZLocationConverter.LatLng latLng = new JZLocationConverter.LatLng(syncPositionBean.getLatitude(), syncPositionBean.getLongitude());
                    latLng = JZLocationConverter.gcj02ToWgs84(latLng);

                    String position = String.format(Locale.getDefault(), "%.8f", latLng.longitude) + "," + String.format(Locale.getDefault(), "%.8f", latLng.latitude);
                    mEtLatLong.setText(position);

                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                    latLng = null;
                }
            }
        });
        initView();
    }

    private void initView() {
        mTvTitle.setText("同步安装位置");
        if (!TextUtils.isEmpty(installLocation)) {
            mEtLatLong.setText(installLocation);
            String[] strs = installLocation.split(",");
            try {
                double longitude = Double.parseDouble(strs[0]);
                double latitude = Double.parseDouble(strs[1]);
                latLng = new LatLng(latitude, longitude);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                latLng = null;
            }

        }
        if (latLng != null) {
            mEtLatLong.setText(latLng.longitude + "," + latLng.latitude);
            processSearchAddressByLatLng();
        }
    }

    private void processSearchAddressByLatLng() {
        LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
        GeocodeSearch geocoderSearch = new GeocodeSearch(getActivity());
        geocoderSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult result, int errorCode) {
                if (errorCode != AMapException.CODE_AMAP_SUCCESS) {
                    ToastUtils.show(MapErrorUtil.getErrorMsg(errorCode));
                    return;
                }

                if (result != null && result.getRegeocodeAddress() != null && result.getRegeocodeAddress().getFormatAddress() != null) {
                    String address = result.getRegeocodeAddress().getFormatAddress();
                    mTvAddress.setText(address);
                }
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

            }
        });
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 100, GeocodeSearch.GPS);
        geocoderSearch.getFromLocationAsyn(query);
    }

    @OnClick({R.id.iv_close, R.id.iv_locate, R.id.tv_cancel, R.id.tv_confirm})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.iv_close) {
            KeyBordUtils.hideSoftKeyboard(mEtLatLong);
            dismiss();

        } else if (id == R.id.iv_locate) {
            LocationUtils.getInstance().getPositionPermission(activity);

        } else if (id == R.id.tv_cancel) {
            KeyBordUtils.hideSoftKeyboard(mEtLatLong);
            dismiss();

        } else if (id == R.id.tv_confirm) {
            KeyBordUtils.hideSoftKeyboard(mEtLatLong);

            String result = mEtLatLong.getText().toString().trim();
            if (TextUtils.isEmpty(result)) {
                ToastUtils.show("经纬度不能为空");
                return;
            }

            if (mListener == null) {
                return;
            }

            if (mListener.onPositiveClick(view, result)) {
                dismiss();
            }
        }
    }

    public void setDialogFragmentClickListener(DialogFragmentClickListener listener) {
        mListener = listener;
    }
}
