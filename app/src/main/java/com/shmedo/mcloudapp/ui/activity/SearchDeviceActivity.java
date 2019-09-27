package com.shmedo.mcloudapp.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.DeviceStatusAdapter;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.StatusInfoResult;
import com.shmedo.mcloudapp.entity.StatusInfoResultDao;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.KeyBordUtils;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.views.ClearEditText;
import com.shmedo.mcloudapp.views.DividerItemDecoration;

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
public class SearchDeviceActivity extends BaseActivity {

    @BindView(R.id.ce_search) ClearEditText mCeSearch;
    @BindView(R.id.btn_search) Button mBtnSearch;
    @BindView(R.id.recycler_device) RecyclerView mRecyclerDevice;
    private List<StatusInfoResult> statusInfoList = new ArrayList<>();
    private DeviceStatusAdapter deviceStatusAdapter;
    private DaoManager manager = DaoManager.getInstance();

    @Override protected int initContentView() {
        return R.layout.activity_search_device;
    }


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }


    private void initView() {
        manager.init(this);
        deviceStatusAdapter = new DeviceStatusAdapter(this, statusInfoList);
        mRecyclerDevice.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerDevice.addItemDecoration(new DividerItemDecoration());
        mRecyclerDevice.setAdapter(deviceStatusAdapter);
    }


    @OnClick(R.id.btn_search) public void onViewClicked() {
        String snName = mCeSearch.getText().toString();
        if (StringUtil.isEmpty(snName)){
            ToastUtil.showShortToast("设备的SN号不能为空");
            return;
        }
        KeyBordUtils.hideSoftKeyboard(mBtnSearch);
        List<StatusInfoResult> list =  fuzzyQueryDevice(snName);
        statusInfoList.clear();
        statusInfoList.addAll(list);
        deviceStatusAdapter.notifyDataSetChanged();
    }

    /**
     * 通过设备名字进行模糊查询
     */
    private List<StatusInfoResult> fuzzyQueryDevice(String name) {
        List<StatusInfoResult> list = manager.getDaoSession()
            .getStatusInfoResultDao()
            .queryBuilder()
            .where(StatusInfoResultDao.Properties.DeviceToken.like("%" + name + "%"))
            .list();
        return list;
    }
}
