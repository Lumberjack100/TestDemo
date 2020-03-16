package com.shmedo.mcloudapp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.CommonAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.ViewHolder;
import com.shmedo.mcloudapp.base.BaseFragment;
import com.shmedo.mcloudapp.entity.QueryCloudDataInfo;
import com.shmedo.mcloudapp.entity.parameter.QueryCloudDataParameter;
import com.shmedo.mcloudapp.model.BaseObserver;
import com.shmedo.mcloudapp.inter.Extras;
import com.shmedo.mcloudapp.model.MDRetrofit;
import com.shmedo.mcloudapp.model.common.CommonVariable;
import com.shmedo.mcloudapp.ui.activity.ConfigDASActivity;
import com.shmedo.mcloudapp.util.ApiName;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.util.TimeUtil;
import com.shmedo.mcloudapp.views.TimePickerDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.fragment
 * 文件名:   QueryDataFragment
 * 创建者:   dpc
 * 创建时间:  2019/3/12 17:39
 * 描述：    查询数据
 */
public class QueryDeviceDataFragment extends BaseFragment {


    @BindView(R.id.tv_device_number)
    TextView tvDeviceNumber;

    @BindView(R.id.start_time)
    TextView startTime;

    @BindView(R.id.end_time)
    TextView endTime;

    @BindView(R.id.img_arrow)
    ImageView imgArrow;

    @BindView(R.id.query_recycle_view)
    RecyclerView queryRecycleView;

    @BindView(R.id.sp_item_count)
    Spinner spItemCount;

    private Animation mExpandAnimation;

    private Animation mFoldResetAnimation;

    private ArrayAdapter<String> itemCountAdapter;
    private String itemCount;
    private String snNubmer;

    private TimePickerDialog timeDialog;

    private LinearLayoutManager linearLayoutManager;

    private CommonAdapter adapter;
    private List<QueryCloudDataInfo> queryCloudDataInfoList = new ArrayList<>();

    private ConfigDASActivity configDASActivity;


    @Override
    protected int initContentView() {
        return R.layout.fragment_query_device_data;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, view);
        getIntentData();
        initView();
        initAdapter();
        initAnimation();
        return view;
    }

    private void getIntentData() {
        Intent intent = getActivity().getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey(Extras.CUR_DEVICE_NAME)) {
            String deviceInfo = intent.getStringExtra(Extras.CUR_DEVICE_NAME);
            String[] scanData = deviceInfo.split(",");

            tvDeviceNumber.setText("设备号：" + scanData[1]);//设备编号
            snNubmer = scanData[1];
            Timber.i("设备号====" + scanData[1]);
        }
        configDASActivity = (ConfigDASActivity) getActivity();
    }

    private void initView() {
        String[] cmData = getResources().getStringArray(R.array.item_count);
        itemCountAdapter = new ArrayAdapter<>(configDASActivity, R.layout.spinner_item, cmData);
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
    }

    private void initAdapter() {
        linearLayoutManager = new LinearLayoutManager(configDASActivity);
        linearLayoutManager.setStackFromEnd(false);
        linearLayoutManager.setReverseLayout(false);
        queryRecycleView.setLayoutManager(linearLayoutManager);
        adapter = new CommonAdapter<QueryCloudDataInfo>(getActivity(), R.layout.item_query_cloud_data, queryCloudDataInfoList) {
            @Override
            protected void convert(ViewHolder holder, QueryCloudDataInfo info, int position) {
                holder.setText(R.id.tv_data_time, info.getTimeStr());
                holder.setText(R.id.tv_data_content, info.getContent());
            }
        };

        queryRecycleView.setAdapter(adapter);
    }

    private void initAnimation() {
        mExpandAnimation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mExpandAnimation.setDuration(350);
        mExpandAnimation.setFillAfter(true);

        mFoldResetAnimation = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mFoldResetAnimation.setDuration(350);
        mFoldResetAnimation.setFillAfter(true);
    }


    @OnClick({R.id.start_time, R.id.end_time, R.id.btn_telemetry, R.id.btn_query_device, R.id.img_arrow})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.start_time:
                timeDialog = null;
                timeDialog = new TimePickerDialog(configDASActivity);
                timeDialog.setTimeLisinter(startTime);
                timeDialog.build();
                break;

            case R.id.end_time:
                timeDialog = null;
                timeDialog = new TimePickerDialog(configDASActivity);
                timeDialog.setTimeLisinter(endTime);
                timeDialog.build();
                break;

            case R.id.btn_telemetry:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    return;
                }
                //发送遥测指令
                configDASActivity.sendCommonCommand("##110\r\n");
                break;

            case R.id.btn_query_device:
//                Timber.i("sn=====：" + snNubmer);
//                Timber.i("开始时间：" + startTime.getText().toString());
//                Timber.i("结束时间：" + endTime.getText().toString());
//                Timber.i("显示条数：" + itemCount);
                queryCloudData(snNubmer, startTime.getText().toString(), endTime.getText().toString(), itemCount);
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


    /**
     * 设置显示数据
     */
    private void setResultData(String cmdStr) {
        //遥测应答指令处理
        if (cmdStr.startsWith("$$110") && cmdStr.endsWith("\r\n")) {
            ToastUtils.show("遥测指令已发送");
            return;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (TextUtils.isEmpty(messageEvent) || !messageEvent.startsWith("$$")) {
            return;
        }

        setResultData(messageEvent);
    }


    private void queryCloudData(String sn, String begin, String end, String number) {
        QueryCloudDataParameter paramter = new QueryCloudDataParameter();
        paramter.setSn(sn);
        paramter.setBegin(begin);
        paramter.setEnd(end);
        paramter.setNumber(number);
        String json = GsonFactory.getGson().toJson(paramter);
        RequestBody body = RequestBody.create(CommonVariable.JSON_TYPE, json);
        MDRetrofit.getInstance().createService(ApiName.HTTP)
                .QueryCloudData(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<QueryCloudDataInfo>>() {
                    @Override
                    public void Success(List<QueryCloudDataInfo> queryCloudDataInfos, String message) {
                        Timber.i(message + "===queryCloudDataInfos==" + queryCloudDataInfos.size());
                        ToastUtils.show("查询成功");
                        if (queryCloudDataInfos.size() != 0) {
                            queryCloudDataInfoList.clear();
                            queryCloudDataInfoList.addAll(queryCloudDataInfos);
                            adapter.notifyDataSetChanged();
                        } else {
                            ToastUtils.show("暂无数据！");
                        }
                    }

                    @Override
                    public void Failure(String message) {
                        ToastUtils.show(message);
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            Timber.i("重新加载了");
        }
    }
}
