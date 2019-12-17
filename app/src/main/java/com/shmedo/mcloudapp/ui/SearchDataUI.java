package com.shmedo.mcloudapp.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import androidx.annotation.NonNull;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.util.DensityUtil;

import butterknife.BindView;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui
 * 创建者:   gonghe
 * 创建时间:  2019-12-17
 * 描述：    TODO
 */
public class SearchDataUI implements View.OnClickListener {

    @BindView(R.id.fab_bluetooth)
    FloatingActionButton mFabBluetooth;

    @BindView(R.id.fab_wifi)
    FloatingActionButton mFabWfi;

    @BindView(R.id.fab_config)
    FloatingActionButton mFabConfig;

    @BindView(R.id.fab_expand)
    FloatingActionButton mFabExpand;

    @BindView(R.id.fab_location)
    FloatingActionButton mFabLocation;

    @BindView(R.id.fab_refresh)
    FloatingActionButton mFabRefresh;

    private Activity mainActivity;

    private View rootView;

    private BottomSheetBehavior mBottomSheetBehavior;

    private boolean isArrowTop = true;

    private boolean isScrollUp = true;

    private Animation mExpandAnimation;

    private Animation mFoldResetAnimation;


    public SearchDataUI(Activity context, View root) {
        this.mainActivity = context;
        this.rootView = root;

        findViews();
        getAndroiodScreenProperty();
    }

    @SuppressLint("NewApi")
    public void getAndroiodScreenProperty() {
        WindowManager wm = (WindowManager) mainActivity.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;// 屏幕宽度（像素）
        int height = dm.heightPixels; // 屏幕高度（像素）
        float density = dm.density;//屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = dm.densityDpi;//屏幕密度dpi（120 / 160 / 240）
        //屏幕宽度算法:屏幕宽度（像素）/屏幕密度
        int screenWidth = (int) (width / density);//屏幕宽度(dp)
        int screenHeight = (int) (height / density);//屏幕高度(dp)
        Log.d("DisplayMetrics", "density=" + density + ";densityDpi=" + densityDpi + ";screenWidth=" + screenWidth + "======" + screenHeight);
    }


    private void findViews() {
        if (rootView == null) {
            return;
        }

        View bottomView = mainActivity.findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomView);
        int height = DensityUtil.Dp2Px(mainActivity, 120);
        mBottomSheetBehavior.setPeekHeight(height);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);


        mFabBluetooth = mainActivity.findViewById(R.id.fab_bluetooth);
        mFabWfi = mainActivity.findViewById(R.id.fab_wifi);
        mFabConfig = mainActivity.findViewById(R.id.fab_config);
        mFabExpand = mainActivity.findViewById(R.id.fab_expand);
        mFabLocation = mainActivity.findViewById(R.id.fab_location);
        mFabRefresh = mainActivity.findViewById(R.id.fab_refresh);

        mFabBluetooth.setOnClickListener(this);
        mFabWfi.setOnClickListener(this);
        mFabConfig.setOnClickListener(this);
        mFabExpand.setOnClickListener(this);
        mFabLocation.setOnClickListener(this);
        mFabRefresh.setOnClickListener(this);

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
                    mFabExpand.setVisibility(View.VISIBLE);
                    mFabExpand.setImageResource(R.drawable.ic_arrow_top_primary);
                    mFabExpand.clearAnimation();
//                    int height = DensityUtil.Dp2Px(mainActivity, 120);
//                    mBottomSheetBehavior.setPeekHeight(height);
                    break;

                case BottomSheetBehavior.STATE_DRAGGING:
                    if (isScrollUp) {
                        mFabBluetooth.setVisibility(View.GONE);
                        mFabWfi.setVisibility(View.GONE);
                        mFabConfig.setVisibility(View.GONE);
                        mFabExpand.setVisibility(View.GONE);
                    } else {
                        mFabExpand.setVisibility(View.VISIBLE);
                        int height = DensityUtil.Dp2Px(mainActivity, 120);
                        mBottomSheetBehavior.setPeekHeight(height);
                    }
                    break;

                case BottomSheetBehavior.STATE_EXPANDED:
                    isScrollUp = false;
                    break;

                case BottomSheetBehavior.STATE_HIDDEN:
                    Timber.d("STATE_HIDDEN");
                    break;

                case BottomSheetBehavior.STATE_SETTLING:
                    Timber.d("STATE_SETTLING");
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

                break;

            case R.id.fab_wifi:

                break;

            case R.id.fab_config:

                break;

            case R.id.fab_expand:
                mFabExpand.startAnimation(isArrowTop ? mExpandAnimation : mFoldResetAnimation);
                mFabBluetooth.setVisibility(isArrowTop ? View.VISIBLE : View.GONE);
                mFabWfi.setVisibility(isArrowTop ? View.VISIBLE : View.GONE);
                mFabConfig.setVisibility(isArrowTop ? View.VISIBLE : View.GONE);
                if (isArrowTop) {
                    int height = DensityUtil.Dp2Px(mainActivity, 330);
                    mBottomSheetBehavior.setPeekHeight(height);
                } else {
                    int height = DensityUtil.Dp2Px(mainActivity, 120);
                    mBottomSheetBehavior.setPeekHeight(height);
                }

                isArrowTop = !isArrowTop;
                break;

            case R.id.fab_location:

                break;

            case R.id.fab_refresh:

                break;

        }
    }
}
