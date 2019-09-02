package com.shmedo.mcloudapp.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.StyleRes;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.SensorAndCount;
import com.shmedo.mcloudapp.util.StringUtil;
import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.views
 * 文件名:   DeviceSensorDialog
 * 创建者:   dpc
 * 创建时间:  2019/8/16 13:43
 * 描述：    传感器详情dialog
 */
public class DeviceSensorDialog extends AlertDialog   {

    private Context mContext;
    private List<SensorAndCount> data;

    private int width;
    private int height;
    public DeviceSensorDialog(Context mContext,@StyleRes int themeResId,List<SensorAndCount> data) {
        super(mContext,themeResId);
        this.mContext = mContext;
        this.data = data;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(mContext).inflate(R.layout.device_sensor_dialog, null);
        this.setCancelable(true);
        this.setTitle("传感器");
        displaySize();
        setContentView(view,new ViewGroup.LayoutParams(width, height));
        initView(view);

    }

    private void displaySize() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        this.width = (int) (width * 0.68);
        this.height = (int) (height * 0.22);
    }
    private void initView(View contentView) {
        LinearLayout llSensorDialog02 = contentView.findViewById(R.id.ll_sensor_dialog_02);
        ImageView imgSensor02 = contentView.findViewById(R.id.img_sensor_02);
        TextView tvSensor02 = contentView.findViewById(R.id.tv_sensor_count_02);

        LinearLayout llSensorDialog04 = contentView.findViewById(R.id.ll_sensor_dialog_04);
        ImageView imgSensor04 = contentView.findViewById(R.id.img_sensor_04);
        TextView tvSensor04 = contentView.findViewById(R.id.tv_sensor_count_04);

        LinearLayout llSensorDialog06 = contentView.findViewById(R.id.ll_sensor_dialog_06);
        ImageView imgSensor06 = contentView.findViewById(R.id.img_sensor_06);
        TextView tvSensor06 = contentView.findViewById(R.id.tv_sensor_count_06);

        LinearLayout llSensorDialog08 = contentView.findViewById(R.id.ll_sensor_dialog_08);
        ImageView imgSensor08 = contentView.findViewById(R.id.img_sensor_08);
        TextView tvSensor08 = contentView.findViewById(R.id.tv_sensor_count_08);

        LinearLayout llSensorDialog12 = contentView.findViewById(R.id.ll_sensor_dialog_12);
        ImageView imgSensor12 = contentView.findViewById(R.id.img_sensor_12);
        TextView tvSensor12 = contentView.findViewById(R.id.tv_sensor_count_12);

        LinearLayout llSensorDialog15 = contentView.findViewById(R.id.ll_sensor_dialog_15);
        ImageView imgSensor15 = contentView.findViewById(R.id.img_sensor_15);
        TextView tvSensor15 = contentView.findViewById(R.id.tv_sensor_count_15);

        LinearLayout llSensorDialog50 = contentView.findViewById(R.id.ll_sensor_dialog_50);
        ImageView imgSensor50 = contentView.findViewById(R.id.img_sensor_50);
        TextView tvSensor50 = contentView.findViewById(R.id.tv_sensor_count_50);

        LinearLayout llSensorDialog51 = contentView.findViewById(R.id.ll_sensor_dialog_51);
        ImageView imgSensor51 = contentView.findViewById(R.id.img_sensor_51);
        TextView tvSensor51 = contentView.findViewById(R.id.tv_sensor_count_51);

        LinearLayout llSensorDialog53 = contentView.findViewById(R.id.ll_sensor_dialog_53);
        ImageView imgSensor53 = contentView.findViewById(R.id.img_sensor_53);
        TextView tvSensor53 = contentView.findViewById(R.id.tv_sensor_count_53);

        //btnConfirm = contentView.findViewById(R.id.btn_confirm);

        if (data != null){
            for (int i = 0; i < data.size(); i++) {
                String sensorType = StringUtil.formatStringTwo(String.valueOf(data.get(i).getSensorType()));
                switch (sensorType){
                    case "02":
                        llSensorDialog02.setVisibility(View.VISIBLE);
                        imgSensor02.setImageResource(R.drawable.medo_icon_sensortype_2);
                        tvSensor02.setText(data.get(i).getSensorType()+"个");
                        break;
                    case "04":
                        llSensorDialog04.setVisibility(View.VISIBLE);
                        imgSensor04.setImageResource(R.drawable.medo_icon_sensortype_3);
                        tvSensor04.setText(data.get(i).getSensorType()+"个");
                        break;
                    case "06":
                        llSensorDialog06.setVisibility(View.VISIBLE);
                        imgSensor06.setImageResource(R.drawable.medo_icon_sensortype_4);
                        tvSensor06.setText(data.get(i).getSensorType()+"个");
                        break;
                    case "08":
                        llSensorDialog08.setVisibility(View.VISIBLE);
                        imgSensor08.setImageResource(R.drawable.medo_icon_sensortype_5);
                        tvSensor08.setText(data.get(i).getSensorType()+"个");
                        break;
                    case "12":
                        llSensorDialog12.setVisibility(View.VISIBLE);
                        imgSensor12.setImageResource(R.drawable.medo_icon_sensortype_6);
                        tvSensor12.setText(data.get(i).getSensorType()+"个");
                        break;
                    case "15":
                        llSensorDialog15.setVisibility(View.VISIBLE);
                        imgSensor15.setImageResource(R.drawable.medo_icon_sensortype_7);
                        tvSensor15.setText(data.get(i).getSensorType()+"个");
                        break;
                    case "50":
                        llSensorDialog50.setVisibility(View.VISIBLE);
                        imgSensor50.setImageResource(R.drawable.medo_icon_sensortype_8);
                        tvSensor50.setText(data.get(i).getSensorType()+"个");
                        break;
                    case "51":
                        llSensorDialog51.setVisibility(View.VISIBLE);
                        imgSensor51.setImageResource(R.drawable.medo_icon_sensortype_9);
                        tvSensor51.setText(data.get(i).getSensorType()+"个");
                        break;
                    case "53":
                        llSensorDialog53.setVisibility(View.VISIBLE);
                        imgSensor53.setImageResource(R.drawable.medo_icon_sensortype_13);
                        tvSensor53.setText(data.get(i).getSensorType()+"个");
                        break;
                }
            }
        }
    }


}
