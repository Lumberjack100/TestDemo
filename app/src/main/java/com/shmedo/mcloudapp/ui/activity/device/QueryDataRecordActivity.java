package com.shmedo.mcloudapp.ui.activity.device;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.views.TimePickerDialog;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device
 * 文件名:   QueryDataRecordActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/28 17:01
 * 描述：    查询数据记录
 */
public class QueryDataRecordActivity extends BaseActivity {

    @BindView(R.id.toolbar_title) TextView mToolbarTitle;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.star_time) TextView mStarTime;
    @BindView(R.id.end_time) TextView mEndTime;
    @BindView(R.id.data_size) Spinner mDataSize;
    @BindView(R.id.data_query) Button mDataQuery;
    private TimePickerDialog timeDialog;

    @Override protected int initContentView() {
        return R.layout.activity_query_data_record;
    }


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }


    private void initView() {
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        mToolbarTitle.setText("数据查询");

        String[] dataSize = getResources().getStringArray(R.array.data_size);
        ArrayAdapter<String> spinnerAdapter =
            new ArrayAdapter<>(this, R.layout.spinner_item, dataSize);
        //spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDataSize.setAdapter(spinnerAdapter);
        mDataSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                ToastUtil.showSToast("" + position);
            }


            @Override public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        Date nowTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        mStarTime.setText(sdf.format(nowTime));
        mEndTime.setText(sdf.format(nowTime));
    }


    @OnClick({ R.id.star_time, R.id.end_time ,R.id.data_query })
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.star_time:
                if (timeDialog == null) {
                    timeDialog = new TimePickerDialog(this);
                }
                timeDialog.setTimeLisinter(mStarTime);
                timeDialog.build();
                break;
            case R.id.end_time:
                if (timeDialog == null) {
                    timeDialog = new TimePickerDialog(this);
                }
                timeDialog.setTimeLisinter(mEndTime);
                timeDialog.build();
                break;
            case R.id.data_query:
                try {
                    if (compareToDate()){
                        ToastUtil.showSToast("正在查询。。");
                    }else {
                        ToastUtil.showSToast("开始时间不能大于结束时间");
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
        }
    }


    /**
     * 比较开始时间与结束时间大小
     * @return
     * @throws ParseException
     */
    private boolean compareToDate() throws ParseException {
        String starTime = mStarTime.getText()!=null ? mStarTime.getText().toString().trim():"";
        String endTime = mEndTime.getText()!=null ? mEndTime.getText().toString().trim():"";
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd hh:mm");
        if (!StringUtil.isNullOrEmpty(starTime) && !StringUtil.isNullOrEmpty(endTime)){
            Date starDate = sdf.parse(starTime);
            Date endDate = sdf.parse(endTime);
            if (starDate.before(endDate)){
                return true;
            }else {
                return false;
            }
        }else {
            return false;
        }
    }
}
