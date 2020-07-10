package com.shmedo.mcloudapp.ui;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dragon.core.util.DensityUtil;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.CommonAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.ViewHolder;
import com.shmedo.mcloudapp.entity.QueryCloudDataInfo;
import com.shmedo.mcloudapp.entity.parameter.QueryCloudDataParameter;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.network.api.ServiceAddressType;
import com.shmedo.mcloudapp.ui.activity.MainActivity;
import com.shmedo.mcloudapp.ui.activity.WifiConnectionActivity;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.util.TimeUtil;
import com.shmedo.mcloudapp.util.permission.PermissionHelper;
import com.shmedo.mcloudapp.views.TimePickerDialog;
import com.shmedo.mcloudapp.views.recycleviewitemdivider.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui
 * 创建者:   gonghe
 * 创建时间:  2019-12-17
 * 描述：    TODO
 */
public class SearchDataUI implements View.OnClickListener {

    private NestedScrollView nestedScrollView;

    private FloatingActionButton mFabBluetooth;

    private FloatingActionButton mFabWfi;

    private FloatingActionButton mFabConfig;

    private FloatingActionButton mFabExpand;

    private FloatingActionButton mFabLocation;

    private FloatingActionButton mFabRefresh;

    private View scanView;

    private EditText mEtSN;

    private TextView startTime;

    private TextView endTime;

    private Button btnQueryDevice;

    private ImageView imgArrow;

    private RecyclerView queryRecycleView;

    private Spinner spItemCount;

    private ArrayAdapter<String> itemCountAdapter;
    private String itemCount;
    private String snNubmer;

    private TimePickerDialog timeDialog;

    private LinearLayoutManager linearLayoutManager;

    private CommonAdapter adapter;
    private List<QueryCloudDataInfo> queryCloudDataInfoList = new ArrayList<>();

    private MainActivity mainActivity;

    private BottomSheetBehavior mBottomSheetBehavior;

    private boolean isExpandActionButton = true;

    private boolean isScrollUp = true;

    private Animation mExpandAnimation;

    private Animation mFoldResetAnimation;


    public SearchDataUI(Activity activity) {
        if (activity instanceof MainActivity) {
            mainActivity = (MainActivity) activity;
        }
        initHeadView();
        initContentView();
        initAdapter();
    }

    private void initHeadView() {
        nestedScrollView = mainActivity.findViewById(R.id.bottom_sheet);

        mBottomSheetBehavior = BottomSheetBehavior.from(nestedScrollView);
        int height = DensityUtil.Dp2Px(mainActivity, 85);
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
        mExpandAnimation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mExpandAnimation.setDuration(350);
        mExpandAnimation.setFillAfter(true);

        mFoldResetAnimation = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mFoldResetAnimation.setDuration(350);
        mFoldResetAnimation.setFillAfter(true);
    }

    private BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View view, int newState) {
            // Check Logs to see how bottom sheets behaves
            switch (newState) {
                case BottomSheetBehavior.STATE_COLLAPSED:
                    isScrollUp = true;
                    isExpandActionButton = true;
                    mFabExpand.setVisibility(View.VISIBLE);
                    mFabExpand.clearAnimation();
                    break;

                case BottomSheetBehavior.STATE_DRAGGING:
                    if (isScrollUp) {
                        mFabBluetooth.setVisibility(View.GONE);
                        mFabWfi.setVisibility(View.GONE);
                        mFabConfig.setVisibility(View.GONE);
                        mFabExpand.setVisibility(View.GONE);
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


    private void initContentView() {
        mEtSN = mainActivity.findViewById(R.id.et_device_sn);
        startTime = mainActivity.findViewById(R.id.start_time);
        endTime = mainActivity.findViewById(R.id.end_time);
        btnQueryDevice = mainActivity.findViewById(R.id.btn_query_device);
        imgArrow = mainActivity.findViewById(R.id.img_arrow);
        queryRecycleView = mainActivity.findViewById(R.id.query_recycle_view);
        spItemCount = mainActivity.findViewById(R.id.sp_item_count);

        String[] cmData = mainActivity.getResources().getStringArray(R.array.item_count);
        itemCountAdapter = new ArrayAdapter<>(mainActivity, R.layout.spinner_item, cmData);
        itemCountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spItemCount.setAdapter(itemCountAdapter);
        spItemCount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                itemCount = parent.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //默认设置3天前的时间
        startTime.setText(TimeUtil.getDateBefore(3));
        endTime.setText(TimeUtil.getCurrentTime());

        startTime.setOnClickListener(this);
        endTime.setOnClickListener(this);
        btnQueryDevice.setOnClickListener(this);
        imgArrow.setOnClickListener(this);
    }

    private void initAdapter() {
        linearLayoutManager = new LinearLayoutManager(mainActivity);
        linearLayoutManager.setStackFromEnd(false);
        linearLayoutManager.setReverseLayout(false);
        queryRecycleView.setLayoutManager(linearLayoutManager);
        queryRecycleView.addItemDecoration(new DividerItemDecoration());
        adapter = new CommonAdapter<QueryCloudDataInfo>(mainActivity, R.layout.item_query_cloud_data, queryCloudDataInfoList) {
            @Override
            protected void convert(ViewHolder holder, QueryCloudDataInfo info, int position) {
                holder.setText(R.id.tv_data_time, info.getTimeStr());
                holder.setText(R.id.tv_data_content, info.getContent());
            }
        };

        queryRecycleView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_bluetooth:

                break;

            case R.id.fab_wifi:
                WifiConnectionActivity.startActivity(mainActivity);
                break;

            case R.id.fab_config:
                mainActivity.processConfigListener();
                break;

            case R.id.fab_expand:
                mFabExpand.startAnimation(isExpandActionButton ? mExpandAnimation : mFoldResetAnimation);
                mFabBluetooth.setVisibility(isExpandActionButton ? View.VISIBLE : View.GONE);
                mFabWfi.setVisibility(isExpandActionButton ? View.VISIBLE : View.GONE);
                mFabConfig.setVisibility(isExpandActionButton ? View.VISIBLE : View.GONE);
                isExpandActionButton = !isExpandActionButton;
                break;

            case R.id.fab_location:
                mainActivity.processLocationListener();
                break;

            case R.id.fab_refresh:
                mainActivity.processRefreshListener();
                break;

            case R.id.RL_scan:
                PermissionHelper.requestScanPermissions(mainActivity);
                break;

            case R.id.start_time:
                timeDialog = null;
                timeDialog = new TimePickerDialog(mainActivity);
                timeDialog.setTimeLisinter(startTime);
                timeDialog.build();
                break;

            case R.id.end_time:
                timeDialog = null;
                timeDialog = new TimePickerDialog(mainActivity);
                timeDialog.setTimeLisinter(endTime);
                timeDialog.build();
                break;

            case R.id.btn_query_device:
                queryCloudData();
                break;

            case R.id.img_arrow:
                if (linearLayoutManager.getReverseLayout()) {
                    imgArrow.startAnimation(mExpandAnimation);
                    linearLayoutManager.setReverseLayout(false);
                    linearLayoutManager.setReverseLayout(false);
                    queryRecycleView.setLayoutManager(linearLayoutManager);
                    queryRecycleView.scrollToPosition(0);
                    adapter.notifyDataSetChanged();
                } else {
                    imgArrow.startAnimation(mFoldResetAnimation);
                    linearLayoutManager.setReverseLayout(true);
                    linearLayoutManager.setReverseLayout(true);
                    queryRecycleView.setLayoutManager(linearLayoutManager);
                    queryRecycleView.scrollToPosition(queryCloudDataInfoList.size() - 1);
                    adapter.notifyDataSetChanged();
                }
                break;
        }
    }


    private void queryCloudData() {
        snNubmer = mEtSN.getText().toString().trim();
        String begin = startTime.getText().toString();
        String end = endTime.getText().toString();
        if (TextUtils.isEmpty(snNubmer)) {
            ToastUtils.show("请输入设备编号");
            return;
        }
        if (snNubmer.length() != 7) {
            ToastUtils.show("请输入正确的设备编号");
            return;
        }
        QueryCloudDataParameter paramter = new QueryCloudDataParameter();
        paramter.setSn(snNubmer);
        paramter.setBegin(begin);
        paramter.setEnd(end);
        paramter.setNumber(itemCount);
        String json = GsonFactory.getGson().toJson(paramter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);

        MDRetrofit.getInstance().createService(ServiceAddressType.HTTPS_NO_API_VERSION)
                .QueryCloudData(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<QueryCloudDataInfo>>() {
                    @Override
                    public void Success(List<QueryCloudDataInfo> queryCloudDataInfos, String message) {
                        Timber.i(message + "===queryCloudDataInfos==" + queryCloudDataInfos.size());
                        ToastUtils.show("查询成功");
                        queryCloudDataInfoList.clear();
                        if (queryCloudDataInfos.size() != 0) {
                            queryCloudDataInfoList.addAll(queryCloudDataInfos);
                        } else {
                            ToastUtils.show("暂无数据！");
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void Failure(String message) {
                        ToastUtils.show(message);
                    }
                });
    }

}
