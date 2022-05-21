package com.shmedo.mcloudapp.util;

import android.os.CountDownTimer;
import android.widget.TextView;

import com.blankj.utilcode.util.ColorUtils;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util
 * 文件名:   MyCountDownTimer
 * 创建者:   dpc
 * 创建时间:  2019/1/9 15:15
 */
public class MyCountDownTimer extends CountDownTimer {
    private TextView btnTime;
    private String orginalText;
    private int orginalColorId = -1;
    private int timerColorId = -1;


    public MyCountDownTimer(TextView time, long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
        this.btnTime = time;
        orginalText = btnTime.getText().toString();
    }

    //计时过程
    @Override
    public void onTick(long l) {
        //防止计时过程中重复点击
        btnTime.setClickable(false);
        btnTime.setText("剩余" + l / 1000 + "秒");
//        btnTime.setBackgroundResource(R.drawable.shape_verify_btn_press);
        if (timerColorId != -1) {
            btnTime.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(timerColorId));
        }
    }

    //计时完毕的方法
    @Override
    public void onFinish() {
        //重新给Button设置文字
        btnTime.setText(orginalText);
        //设置可点击
        btnTime.setClickable(true);
//        btnTime.setBackgroundResource(R.drawable.btn_blue_selector);
        if (orginalColorId != -1) {
            btnTime.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(orginalColorId));
        }
    }

    public void setTextColor(int orginalColorId, int timerColorId) {
        this.orginalColorId = orginalColorId;
        this.timerColorId = timerColorId;

    }
}


