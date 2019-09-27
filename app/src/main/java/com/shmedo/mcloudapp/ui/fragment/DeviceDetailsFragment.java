package com.shmedo.mcloudapp.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseFragment;
import com.shmedo.mcloudapp.entity.BasicInfoResult;
import com.shmedo.mcloudapp.entity.ConfigParameterResult;
import com.shmedo.mcloudapp.entity.DetailInfoResult;
import com.shmedo.mcloudapp.entity.DeviceDetailInfo;
import com.shmedo.mcloudapp.entity.DeviceSensorResult;
import com.shmedo.mcloudapp.model.BaseObserver;
import com.shmedo.mcloudapp.model.MDRetrofit;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.views.LoadingDialog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.fragment
 * 文件名:   DeviceDetailsFragment
 * 创建者:   dpc
 * 创建时间:  2019/3/12 17:41
 * 描述：   设备详情
 */
public class DeviceDetailsFragment extends BaseFragment {

    @BindView(R.id.name)
    TextView mName;
    @BindView(R.id.device_type)
    TextView mDeviceType;
    @BindView(R.id.function_model)
    TextView mFunctionModel;
    @BindView(R.id.authorization_code)
    TextView mAuthorizationCode;
    @BindView(R.id.device_number)
    TextView mDeviceNumber;
    @BindView(R.id.production_date)
    TextView mProductionDate;
    @BindView(R.id.the_project)
    TextView mTheProject;
    @BindView(R.id.cpu_id)
    TextView mCpuId;
    @BindView(R.id.firmware_version)
    TextView mFirmwareVersion;
    @BindView(R.id.hardware_version)
    TextView mHardwareVersion;
    @BindView(R.id.operator_one)
    TextView mOperatorOne;
    @BindView(R.id.sim_number_one)
    TextView mSimNumberOne;
    @BindView(R.id.port_one)
    TextView mPortOne;
    @BindView(R.id.signal_strength_one)
    TextView mSignalStrengthOne;
    @BindView(R.id.data_amount_one)
    TextView mDataAmountOne;
    @BindView(R.id.operator_two)
    TextView mOperatorTwo;
    @BindView(R.id.sim_number_two)
    TextView mSimNumberTwo;
    @BindView(R.id.port_two)
    TextView mPortTwo;
    @BindView(R.id.signal_strength_two)
    TextView mSignalStrengthTwo;
    @BindView(R.id.data_amount_two)
    TextView mDataAmountTwo;
    @BindView(R.id.device_status)
    TextView mDeviceStatus;
    @BindView(R.id.rain_function)
    TextView mRainFunction;
    @BindView(R.id.osmometer_function)
    TextView mOsmometerFunction;
    @BindView(R.id.debug_model)
    TextView mDebugModel;
    @BindView(R.id.data_interval)
    TextView mDataInterval;
    @BindView(R.id.data_communication)
    TextView mDataCommunication;
    @BindView(R.id.sensor_type)
    TextView mSensorType;
    @BindView(R.id.external_voltage)
    TextView mExternalVoltage;
    @BindView(R.id.register_deadline)
    TextView mRegisterDeadline;
    @BindView(R.id.internal_temperature)
    TextView mInternalTemperature;
    @BindView(R.id.device_position)
    TextView mDevicePosition;
    @BindView(R.id.data_center)
    TextView mDataCenter;

    private Unbinder unbinder;

    private LoadingDialog mLoadingDialog;

    @Override
    protected int initContentView() {
        return R.layout.fragment_device_details;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        initView();
        initData(65);
        return view;
    }


    private void initView() {
        mLoadingDialog = new LoadingDialog(getActivity());
    }


    private void initData(int deviceId) {
        mLoadingDialog.showNoCancelDialog("正在加载。。");

        String json = GsonFactory.getGson().toJson(deviceId);
        RequestBody body = RequestBody.create(CommonVariable.JSON_TYPE, json);
        MDRetrofit.getInstance().createService().GetDeviceDetailInfo(CommonVariable.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<DeviceDetailInfo>() {

                    @Override
                    public void Success(DeviceDetailInfo deviceDetailInfo, String message) {
                        mLoadingDialog.dismiss();
                        setDeviceDetail(deviceDetailInfo);
                    }


                    @Override
                    public void Failure(String message) {
                        mLoadingDialog.dismiss();
                        Timber.w("服务器连接失败--" + message);
                        ToastUtil.showShortToast("服务器连接失败");
                    }
                });
    }


    private void setDeviceDetail(DeviceDetailInfo deviceDetailInfo) {
        BasicInfoResult basicInfoResult = deviceDetailInfo.getBasicInfo();
        List<DeviceSensorResult> sensorResultList = deviceDetailInfo.getSensors();
        DetailInfoResult detailInfoResult = deviceDetailInfo.getDetailInfo();
        ConfigParameterResult configParameterResult = deviceDetailInfo.getConfigParameter();

        mName.setText(basicInfoResult.getDeviceName());
        mDeviceType.setText(basicInfoResult.getDeviceTypeName());
        mFunctionModel.setText("");
        mAuthorizationCode.setText(basicInfoResult.getSecurityNO());
        mDeviceNumber.setText("");
        mProductionDate.setText(detailInfoResult.getProductionTime());
        mTheProject.setText(basicInfoResult.getProjectName());
        mCpuId.setText(basicInfoResult.getCpuID());
        mFirmwareVersion.setText(detailInfoResult.getSoftwareVersion());
        mHardwareVersion.setText(detailInfoResult.getHardwareVersion());

        mOperatorOne.setText(detailInfoResult.getSim1NO());
        mSimNumberOne.setText(detailInfoResult.getSim1Vendor());
        mPortOne.setText("");
        mSignalStrengthOne.setText(detailInfoResult.getSim1State());
        mDataAmountOne.setText(detailInfoResult.getSim1Data() + "");

        mOperatorTwo.setText(detailInfoResult.getSim2NO());
        mSimNumberTwo.setText(detailInfoResult.getSim2Vendor());
        mPortTwo.setText("");
        mSignalStrengthTwo.setText(detailInfoResult.getSim2State());
        mDataAmountTwo.setText(detailInfoResult.getSim2Data() + "");

        mDeviceStatus.setText(basicInfoResult.getDeviceStatus());
        mRainFunction.setText("");
        mOsmometerFunction.setText("");
        mDebugModel.setText("");
        mDataInterval.setText("");
        mDataInterval.setText(configParameterResult.getDataUploadInterval() + "");
        mDataCommunication.setText("");
        //mSensorType.setText(sensorResultList.get(0).getSensorType()+"");
        mExternalVoltage.setText("");
        mRegisterDeadline.setText("");
        mInternalTemperature.setText("");
        mDevicePosition.setText("");
        mDataCenter.setText("");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }
}
