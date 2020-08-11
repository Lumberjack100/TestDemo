package com.shmedo.mcloudapp.common.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.CommonAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.CommonViewHolder;
import com.shmedo.mcloudapp.entity.SensorAndCount;
import com.shmedo.mcloudapp.util.ImageUtil;

import java.util.List;

import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.views
 * 文件名:   DeviceSensorDialog
 * 创建者:   dpc
 * 创建时间:  2019/8/16 13:43
 * 描述：    传感器详情dialog
 */
public class DeviceSensorDialog extends AlertDialog {

    private static final int MAX_HEIGHT = 1000;

    private Context mContext;
    private List<SensorAndCount> dataList;
    private View rootView;
    private RecyclerView mRecyclerView;
    private CommonAdapter adapter;


    public DeviceSensorDialog(Context mContext, @StyleRes int themeResId, List<SensorAndCount> data) {
        super(mContext, themeResId);
        this.mContext = mContext;
        this.dataList = data;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = LayoutInflater.from(mContext).inflate(R.layout.device_sensor_dialog, null);
        this.setCancelable(true);

        setContentView(rootView);
        initView();
        displaySize();
    }

    private void displaySize() {
        //这种设置宽高的方式也是好使的！！！-- show 前调用，show 后调用都可以！！！
        rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                       int oldRight, int oldBottom) {
                int height = v.getHeight();     //此处的view 和v 其实是同一个控件
                int contentHeight = rootView.getHeight();

                Timber.d("高度:" + height + " / " + " / " + contentHeight);

                if (contentHeight > MAX_HEIGHT) {
                    //注意：这里的 LayoutParams 必须是 FrameLayout的！！
                    rootView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                            MAX_HEIGHT));
                }
            }
        });
    }


    private void initView() {

        mRecyclerView = rootView.findViewById(R.id.recycler_sensor);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
//        mRecyclerView.addItemDecoration(new RecycleViewDivider(LinearLayoutManager.VERTICAL, DensityUtil.Dp2Px(mContext, 0.2f), mContext.getResources().getColor(R.color.color_split_line_d9d9d9)));
        adapter = new CommonAdapter<SensorAndCount>(mContext, R.layout.item_sensor_type, dataList) {
            @Override
            protected void convert(CommonViewHolder holder, SensorAndCount bean, final int position) {
                holder.setImageResource(R.id.img_sensor, ImageUtil.getSensorResourceID(bean.getSensorType()));
                holder.setText(R.id.tv_sensor_count, bean.getSensorCount() + "个");
            }
        };

        mRecyclerView.setAdapter(adapter);
    }

}
