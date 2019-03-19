package com.shmedo.mcloudapp.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseFragment;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.fragment
 * 文件名:   DeviceFragment
 * 创建者:   dpc
 * 创建时间:  2019/3/13 09:14
 * 描述：    TODO
 */
public class DeviceFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.sub_content) FrameLayout mSubContent;
    @BindView(R.id.tv_status) TextView mTvStatus;
    @BindView(R.id.tv_setguid) TextView mTvSetguid;
    @BindView(R.id.tv_highsetting) TextView mTvHighsetting;
    @BindView(R.id.tv_query_data) TextView mTvQueryData;
    @BindView(R.id.tv_device_details) TextView mTvDeviceDetails;

    RunStatusFragment runStatusFragment;
    SetGuideFragment setGuideFragment;
    AdvanceSetFragment advanceSetFragment;
    QueryDataFragment queryDataFragment;
    DeviceDetailsFragment deviceDetailsFragment;

    @Override protected int initContentView() {
        return R.layout.fragment_device;
    }


    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       View view = super.onCreateView(inflater, container, savedInstanceState);
       initData();
       return view;
    }


    private void initData() {
        mTvStatus.setOnClickListener(this);
        mTvSetguid.setOnClickListener(this);
        mTvHighsetting.setOnClickListener(this);
        mTvQueryData.setOnClickListener(this);
        mTvDeviceDetails.setOnClickListener(this);
        setDefaultFragment();
    }
    /**
     * set the default Fragment
     */
    private void setDefaultFragment() {
        switchFrgment(0);
        //set the defalut tab state
        setTabState(mTvStatus, R.drawable.yxzt, getColor(R.color.colorPrimary));
    }
    @Override public void onClick(View v) {
        resetTabState();//reset the tab state
        switch (v.getId()) {
            case R.id.tv_status:
                setTabState(mTvStatus, R.drawable.yxzt, getColor(R.color.colorPrimary));
                switchFrgment(0);
                break;
            case R.id.tv_setguid:
                setTabState(mTvSetguid, R.drawable.szxd, getColor(R.color.colorPrimary));
                switchFrgment(1);
                break;
            case R.id.tv_highsetting:
                setTabState(mTvHighsetting, R.drawable.gjpz, getColor(R.color.colorPrimary));
                switchFrgment(2);
                break;
            case R.id.tv_query_data:
                setTabState(mTvQueryData, R.drawable.xtgj, getColor(R.color.colorPrimary));
                switchFrgment(3);
                break;
            case R.id.tv_device_details:
                setTabState(mTvDeviceDetails, R.drawable.xtgj, getColor(R.color.colorPrimary));
                switchFrgment(4);
            break;
            default:
        }
    }
    /**
     * switch the fragment accordting to id
     * @param i id
     */
    private void switchFrgment(int i) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        switch (i) {
            case 0:
                runStatusFragment = new RunStatusFragment();
                transaction.replace(R.id.sub_content, runStatusFragment);
                break;
            case 1:
                setGuideFragment = new SetGuideFragment();
                transaction.replace(R.id.sub_content, setGuideFragment);
                break;
            case 2:
                advanceSetFragment = new AdvanceSetFragment();
                transaction.replace(R.id.sub_content, advanceSetFragment);
                break;
            case 3:
                queryDataFragment = new QueryDataFragment();
                transaction.replace(R.id.sub_content, queryDataFragment);
                break;
            case 4:
                deviceDetailsFragment = new DeviceDetailsFragment();
                transaction.replace(R.id.sub_content, deviceDetailsFragment);
            break;
            default:
        }
        transaction.commit();
    }
    /**
     * set the tab state of bottom navigation bar
     *
     * @param textView the text to be shown
     * @param image    the image
     * @param color    the text color
     */
    private void setTabState(TextView textView, int image, int color) {
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, image, 0, 0);//Call requires API level 17
        textView.setTextColor(color);
    }
    /**
     * revert the image color and text color to black
     */
    private void resetTabState() {
        setTabState(mTvStatus, R.drawable.yxzt_wxz, getColor(R.color.font_main_79));
        setTabState(mTvSetguid, R.drawable.szxd_wxz, getColor(R.color.font_main_79));
        setTabState(mTvHighsetting, R.drawable.gjpz_wxz, getColor(R.color.font_main_79));
        setTabState(mTvQueryData, R.drawable.xtgj_wxz, getColor(R.color.font_main_79));
        setTabState(mTvDeviceDetails, R.drawable.xtgj_wxz, getColor(R.color.font_main_79));

    }
    /**
     * @param i the color id
     * @return color
     */
    private int getColor(int i) {
        return ContextCompat.getColor(getActivity(), i);
    }
}
