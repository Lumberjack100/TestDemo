package com.shmedo.mcloudapp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shmedo.core.utils.StringUtil;
import com.dragon.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.CommonAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.MultiItemTypeAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.ViewHolder;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.DeviceBasicInfoResult;
import com.shmedo.mcloudapp.entity.SensorAndCount;
import com.shmedo.mcloudapp.entity.StatusInfoResult;
import com.shmedo.mcloudapp.entity.StatusInfoResultDao;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.ImageUtil;
import com.shmedo.mcloudapp.util.KeyBordUtils;
import com.shmedo.mcloudapp.views.ClearEditText;
import com.shmedo.mcloudapp.views.DeviceSensorDialog;
import com.shmedo.mcloudapp.views.recycleviewitemdivider.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   SearchDeviceActivity
 * 创建者:   dpc
 * 创建时间:  2019/8/20 18:08
 * 描述：    搜索设备
 */
public class SearchDeviceActivity extends BaseActivity implements MultiItemTypeAdapter.OnItemClickListener, TextWatcher, TextView.OnEditorActionListener {

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.et_search)
    ClearEditText mEtSearch;

    @BindView(R.id.recycler_device)
    RecyclerView mRecyclerDevice;

    private CommonAdapter adapter;

    private List<StatusInfoResult> statusInfoList = new ArrayList<>();

    private DaoManager manager = DaoManager.getInstance();


    /**
     * 说明：启动Activity
     * <p>
     * 注意：这里使用到了Intent的Flag属性singleTop。singleTop模式下，在同一个task中，如果存在该Activity的实例，
     * 并且该Activity实例位于栈顶(即，该Activity位于前端)，则调用startActivity()时，不再创建该Activity的示例；
     * 而仅仅只是调用Activity的onNewIntent()。否则的话，则新建该Activity的实例，并将其置于栈顶。
     * </p>
     */
    public static void startActivity(Context context) {
        Intent intent = new Intent(context, SearchDeviceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_search_device;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("设备查询");
        mEtSearch.setHint("请输入设备SN号");
        initView();
        initAdapter();
    }


    private void initView() {
        //搜索框获取焦点，弹出软键盘
        KeyBordUtils.popSoftKeyboard(mEtSearch, true);
        mEtSearch.addTextChangedListener(this);
        mEtSearch.setOnEditorActionListener(this);
    }

    private void initAdapter() {
        mRecyclerDevice.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerDevice.addItemDecoration(new DividerItemDecoration());
        adapter = new CommonAdapter<StatusInfoResult>(this, R.layout.item_all_device, statusInfoList) {
            @Override
            protected void convert(ViewHolder holder, final StatusInfoResult statusInfoResult, final int position) {
                holder.setText(R.id.tv_deviceToken, statusInfoResult.getDeviceToken() + "\n" + (statusInfoResult.getLocal() ? "本地设备" : "云端设备"));
                holder.setText(R.id.tv_electricity, statusInfoResult.getVoltage() + "V");
                holder.setText(R.id.tv_gprs, StringUtil.getByteSize(statusInfoResult.getGprs()));
                holder.setText(R.id.tv_signal, statusInfoResult.getSignal() + "");
                holder.setTag(R.id.ll_SensorType, position);

                final List<SensorAndCount> sensorAndCountList = statusInfoResult.getSensorInfo();
                if (sensorAndCountList == null || sensorAndCountList.size() == 0) {
                    holder.setVisibleOrGone(R.id.tv_sensor_type_1, false);
                    holder.setVisibleOrGone(R.id.tv_sensor_type_2, false);
                    holder.setVisibleOrGone(R.id.tv_sensor_more, false);
                    return;
                }

                if (sensorAndCountList.size() > 1) {
                    holder.setVisibleOrGone(R.id.tv_sensor_type_1, true);
                    holder.setVisibleOrGone(R.id.tv_sensor_type_2, true);
                    holder.setVisibleOrGone(R.id.tv_sensor_more, sensorAndCountList.size() > 2);

                    holder.setCompoundDrawablesWithIntrinsicBounds(R.id.tv_sensor_type_1, ImageUtil.getSensorResourceID(sensorAndCountList.get(0).getSensorType()), 0, 0, 0);
                    holder.setText(R.id.tv_sensor_type_1, "x" + sensorAndCountList.get(0).getSensorCount());
                    holder.setCompoundDrawablesWithIntrinsicBounds(R.id.tv_sensor_type_2, ImageUtil.getSensorResourceID(sensorAndCountList.get(1).getSensorType()), 0, 0, 0);
                    holder.setText(R.id.tv_sensor_type_2, "x" + sensorAndCountList.get(1).getSensorCount());

                } else {
                    holder.setVisibleOrGone(R.id.tv_sensor_type_1, true);
                    holder.setVisibleOrGone(R.id.tv_sensor_type_2, false);
                    holder.setVisibleOrGone(R.id.tv_sensor_more, false);

                    holder.setCompoundDrawablesWithIntrinsicBounds(R.id.tv_sensor_type_1, ImageUtil.getSensorResourceID(sensorAndCountList.get(0).getSensorType()), 0, 0, 0);
                    holder.setText(R.id.tv_sensor_type_1, "x" + sensorAndCountList.get(0).getSensorCount());
                }

                holder.setOnClickListener(R.id.tv_sensor_more, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (sensorAndCountList == null || sensorAndCountList.size() < 3)
                            return;

                        DeviceSensorDialog deviceSensorDialog = new DeviceSensorDialog(SearchDeviceActivity.this, R.style.dialog_center_full, sensorAndCountList);
                        if (!deviceSensorDialog.isShowing()) {
                            deviceSensorDialog.show();
                        }
                    }
                });

            }
        };
        adapter.setOnItemClickListener(this);
        mRecyclerDevice.setAdapter(adapter);
    }


    @Override
    public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
        StatusInfoResult statusInfoResult = statusInfoList.get(position);
        MCloudApp.setCurDeviceToken(statusInfoResult.getDeviceName());
        MCloudApp.setCurDeviceMacAddr(null);

        String deviceInfo = "MEDO," + statusInfoResult.getDeviceName() + "," + statusInfoResult.getDeviceTypeName();
        if (!TextUtils.isEmpty(statusInfoResult.getDeviceTypeName()) && statusInfoResult.getDeviceTypeName().toUpperCase().contains("ADME")) {
            ConfigADMEActivity.startActivity(SearchDeviceActivity.this, deviceInfo);
            SearchDeviceActivity.this.finish();
        }

        if (!TextUtils.isEmpty(statusInfoResult.getDeviceTypeName()) && statusInfoResult.getDeviceTypeName().toUpperCase().contains("DAS")) {
            ConfigDASActivity.startActivity(SearchDeviceActivity.this, deviceInfo);
            SearchDeviceActivity.this.finish();
        }

        if (!TextUtils.isEmpty(statusInfoResult.getDeviceName()) && statusInfoResult.getDeviceName().toUpperCase().contains("E60")) {
            DeviceBasicInfoResult deviceBasicInfoResult = new DeviceBasicInfoResult();
            deviceBasicInfoResult.setDeviceToken(statusInfoResult.getDeviceToken());
            deviceBasicInfoResult.setDeviceName(statusInfoResult.getDeviceName());
            ConfigE60Activity.startActivity(SearchDeviceActivity.this, deviceBasicInfoResult);
            SearchDeviceActivity.this.finish();
        }
    }

    @Override
    public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
        return false;
    }


    @OnClick({R.id.tv_cancel})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancel:
                SearchDeviceActivity.this.finish();
                break;
        }
    }


    /**
     * 搜索处理逻辑
     * <br/>
     * 查询本地数据库中匹配搜索关键字的当前用户的设备
     */
    private void searchProcess(String queryText) {
        List<StatusInfoResult> resultList = manager.getDaoSession()
                .getStatusInfoResultDao()
                .queryBuilder()
                .where(StatusInfoResultDao.Properties.DeviceToken.like("%" + queryText + "%"),
                        StatusInfoResultDao.Properties.Account.isNotNull(),
                        StatusInfoResultDao.Properties.Account.eq(MCloudApp.getAccount()))
                .list();

        if (resultList != null && resultList.size() > 0) {
            statusInfoList.clear();
            statusInfoList.addAll(resultList);
            adapter.notifyDataSetChanged();
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {
        if (!TextUtils.isEmpty(text)) {
            searchProcess(text.toString().trim());

        } else {
            KeyBordUtils.popSoftKeyboard(mEtSearch, true);
            statusInfoList.clear();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            // 当按了搜索之后关闭软键盘
            KeyBordUtils.hideSoftKeyboard(mEtSearch);

            String text = mEtSearch.getText().toString();
            if (TextUtils.isEmpty(text)) {
                mEtSearch.clearFocus();
                return true;
            }

            searchProcess(text.trim());
            return true;
        }
        return false;
    }
}
