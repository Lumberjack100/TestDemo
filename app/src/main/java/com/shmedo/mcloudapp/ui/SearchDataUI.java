package com.shmedo.mcloudapp.ui;

import android.Manifest;
import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import androidx.annotation.NonNull;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.ui.activity.MainActivity;
import com.shmedo.mcloudapp.ui.activity.ScanActivity;
import com.shmedo.mcloudapp.ui.activity.WifiConnectionActivity;
import com.shmedo.mcloudapp.util.DensityUtil;
import com.shmedo.mcloudapp.util.XPermissionUtils;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui
 * 创建者:   gonghe
 * 创建时间:  2019-12-17
 * 描述：    TODO
 */
public class SearchDataUI implements View.OnClickListener {

    private FloatingActionButton mFabBluetooth;

    private FloatingActionButton mFabWfi;

    private FloatingActionButton mFabConfig;

    private FloatingActionButton mFabExpand;

    private FloatingActionButton mFabLocation;

    private FloatingActionButton mFabRefresh;

    private View scanView;

    private MainActivity mainActivity;

    private BottomSheetBehavior mBottomSheetBehavior;

    private boolean isArrowTop = true;

    private boolean isScrollUp = true;

    private Animation mExpandAnimation;

    private Animation mFoldResetAnimation;


    public SearchDataUI(Activity activity) {
        if (activity instanceof MainActivity) {
            mainActivity = (MainActivity) activity;
        }
        findViews();
    }

    private void findViews() {
        View bottomView = mainActivity.findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomView);
        int height = DensityUtil.Dp2Px(mainActivity, 80);
        mBottomSheetBehavior.setPeekHeight(height);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);

        mFabBluetooth = mainActivity.findViewById(R.id.fab_bluetooth);
        mFabWfi = mainActivity.findViewById(R.id.fab_wifi);
        mFabConfig = mainActivity.findViewById(R.id.fab_config);
        mFabExpand = mainActivity.findViewById(R.id.fab_expand);
        mFabLocation = mainActivity.findViewById(R.id.fab_location);
        mFabRefresh = mainActivity.findViewById(R.id.fab_refresh);

        scanView = mainActivity.findViewById(R.id.RL_scan);

        mFabBluetooth.setOnClickListener(this);
        mFabWfi.setOnClickListener(this);
        mFabConfig.setOnClickListener(this);
        mFabExpand.setOnClickListener(this);
        mFabLocation.setOnClickListener(this);
        mFabRefresh.setOnClickListener(this);
        scanView.setOnClickListener(this);

        initAnimation();
    }

    private void initAnimation() {
        mExpandAnimation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mExpandAnimation.setDuration(300);
        mExpandAnimation.setFillAfter(true);

        mFoldResetAnimation = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mFoldResetAnimation.setDuration(300);
        mFoldResetAnimation.setFillAfter(true);
    }

    private BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View view, int newState) {
            // Check Logs to see how bottom sheets behaves
            switch (newState) {
                case BottomSheetBehavior.STATE_COLLAPSED:
                    isScrollUp = true;
                    isArrowTop = true;
                    break;

                case BottomSheetBehavior.STATE_DRAGGING:
                    if (isScrollUp) {
                        mFabBluetooth.setVisibility(View.GONE);
                        mFabWfi.setVisibility(View.GONE);
                        mFabConfig.setVisibility(View.GONE);
                        mFabExpand.setVisibility(View.GONE);
                    } else {
                        mFabExpand.setVisibility(View.VISIBLE);
                        mFabExpand.setImageResource(R.drawable.ic_arrow_top_primary);
                        mFabExpand.clearAnimation();
                    }
                    break;

                case BottomSheetBehavior.STATE_EXPANDED:
                    isScrollUp = false;
                    break;
            }
        }

        @Override
        public void onSlide(@NonNull View view, float v) {

        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_bluetooth:
                mainActivity.startDiscoveryDevice();
                break;

            case R.id.fab_wifi:
                WifiConnectionActivity.startActivity(mainActivity);
                break;

            case R.id.fab_config:
                mainActivity.processConfigListener();
                break;

            case R.id.fab_expand:
                mFabExpand.startAnimation(isArrowTop ? mExpandAnimation : mFoldResetAnimation);
                mFabBluetooth.setVisibility(isArrowTop ? View.VISIBLE : View.GONE);
                mFabWfi.setVisibility(isArrowTop ? View.VISIBLE : View.GONE);
                mFabConfig.setVisibility(isArrowTop ? View.VISIBLE : View.GONE);
                isArrowTop = !isArrowTop;
                break;

            case R.id.fab_location:
                mainActivity.processLocationListener();
                break;

            case R.id.fab_refresh:
                mainActivity.processRefreshListener();
                break;

            case R.id.RL_scan:
                doScanButtonClick();
                break;
        }
    }

    private void doScanButtonClick() {
        XPermissionUtils.requestPermissionsResult(mainActivity, 200, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        ScanActivity.startActivityForResult(mainActivity, MainActivity.REQUEST_CODE_SCAN);
                    }

                    @Override
                    public void onPermissionDenied() {
                        XPermissionUtils.showRefusePermissionDialog(mainActivity,
                                mainActivity.getResources().getString(R.string.permission_request_camera_external_storage));
                    }
                });
    }
}
