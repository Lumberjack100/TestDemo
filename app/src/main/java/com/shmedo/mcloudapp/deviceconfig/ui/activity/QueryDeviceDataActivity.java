package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.common.view.TimePickerDialog;
import com.shmedo.mcloudapp.deviceconfig.adapter.DeviceReportDataAdapter;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.MyDatePicker;
import com.shmedo.mcloudapp.entity.QueryCloudDataInfo;
import com.shmedo.mcloudapp.entity.parameter.QueryCloudDataParameter;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.network.api.ServiceAddressType;
import com.shmedo.mcloudapp.util.DateUtil;
import com.shmedo.mcloudapp.util.GsonFactory;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;

public class QueryDeviceDataActivity extends BaseActivity {
    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.snET)
    ClearEditText mEtSn;

    @BindView(R.id.tv_start_time)
    TextView mTvStartTime;

    @BindView(R.id.tv_end_time)
    TextView mTvEndTime;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private String startTime;
    private String endTime;
    private String snNubmer;

    private TimePickerDialog timeDialog;


    private DeviceReportDataAdapter adapter;
    private List<QueryCloudDataInfo> queryCloudDataInfoList = new ArrayList<>();


    public static void startActivity(Context context, String sn) {
        Intent intent = new Intent(context, QueryDeviceDataActivity.class);
        intent.putExtra(AppContants.Extras.CUR_DEVICE_SN, sn);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_query_device_data;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("数据查询");
        initDate();
        parseIntent();
        initAdapter();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(AppContants.Extras.CUR_DEVICE_SN)) {
            snNubmer = intent.getStringExtra(AppContants.Extras.CUR_DEVICE_SN);
            if (!TextUtils.isEmpty(snNubmer)) {
                mEtSn.setText(snNubmer);
                startTime = mTvStartTime.getText().toString() + " 00:00:00";
                endTime = mTvEndTime.getText().toString() + " 23:59:59";
                queryCloudData();
            }
        }
    }

    private void initDate() {
        //默认设置3天前的时间
        mTvStartTime.setText(String.format("%s", DateUtil.getBackOrAddDate(new Date(), -3)));
        mTvEndTime.setText(String.format("%s", DateUtil.getNowDateYYYYMMDDString()));
    }

    private void initAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeviceReportDataAdapter(queryCloudDataInfoList);
        adapter.setAnimationEnable(true);
        adapter.setAnimationFirstOnly(false);
        mRecyclerView.setAdapter(adapter);
    }


    @OnClick({R.id.tv_start_time, R.id.tv_end_time, R.id.tv_search})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_start_time: {
//                timeDialog = null;
//                timeDialog = new TimePickerDialog(this);
//                timeDialog.setTimeLisinter(mTvStartTime);
//                timeDialog.build();

                MyDatePicker newFragment = new MyDatePicker(this, mTvStartTime.getText().toString(), "选择开始时间");
                newFragment.setOnPositiveClickListener(new MyDatePicker.OnPositiveClickListener() {
                    @Override
                    public void onPositiveClick(String date) {
                        mTvStartTime.setText(date);
                    }
                });
                newFragment.show(getSupportFragmentManager(), "dialog");
            }
            break;

            case R.id.tv_end_time: {
                MyDatePicker newFragment = new MyDatePicker(this, mTvEndTime.getText().toString(), "选择结束时间");
                newFragment.setOnPositiveClickListener(new MyDatePicker.OnPositiveClickListener() {
                    @Override
                    public void onPositiveClick(String date) {
                        mTvEndTime.setText(date);
                    }
                });
                newFragment.show(getSupportFragmentManager(), "dialog");
            }
            break;

            case R.id.tv_search:
                if (!checkValue()) {
                    return;
                }

                startTime = startTime + " 00:00:00";
                endTime = endTime + " 23:59:59";
                queryCloudData();
                break;
        }
    }

    private boolean checkValue() {
        snNubmer = mEtSn.getText().toString();
        startTime = mTvStartTime.getText().toString();
        endTime = mTvEndTime.getText().toString();

        if (TextUtils.isEmpty(snNubmer)) {
            ToastUtils.show("请输入SN号");
            return false;
        }
        // 当按了搜索之后关闭软键盘
        KeyBordUtils.hideSoftKeyboard(mEtSn);

        if (TextUtils.isEmpty(startTime)) {
            ToastUtils.show("请选择开始时间");
            return false;
        }

        if (TextUtils.isEmpty(endTime)) {
            ToastUtils.show("请选择结束时间");
            return false;
        }

        Date start = DateUtil.stringToDate(startTime, "yyyy-MM-d");
        Date end = DateUtil.stringToDate(endTime, "yyyy-MM-d");
        if (start.after(end)) {
            ToastUtils.show("开始时间必须小于等于结束时间");
            return false;
        }

        return true;
    }


    private void queryCloudData() {
        QueryCloudDataParameter paramter = new QueryCloudDataParameter();
        paramter.setSn(snNubmer);
        paramter.setBegin(startTime);
        paramter.setEnd(endTime);
        paramter.setNumber("30");

        String json = GsonFactory.getGson().toJson(paramter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        MDRetrofit.getInstance().createService(ServiceAddressType.HTTPS_NO_API_VERSION)
                .QueryCloudData(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<QueryCloudDataInfo>>() {
                    @Override
                    public void Success(List<QueryCloudDataInfo> queryCloudDataInfos, String message) {
                        if (queryCloudDataInfos == null || queryCloudDataInfos.size() == 0) {
                            adapter.setEmptyView(R.layout.empty_view);
                            return;
                        }

                        queryCloudDataInfoList.clear();
                        queryCloudDataInfoList.addAll(queryCloudDataInfos);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void Failure(String message) {
                        adapter.setEmptyView(getErrorView());
                    }
                });
    }

    private View getErrorView() {
        View errorView = getLayoutInflater().inflate(R.layout.error_view, mRecyclerView, false);
        errorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryCloudData();
            }
        });
        return errorView;
    }
}
