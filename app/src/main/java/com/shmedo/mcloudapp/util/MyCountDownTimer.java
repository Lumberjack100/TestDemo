package com.shmedo.mcloudapp.util;

import android.os.CountDownTimer;
import android.widget.Button;

import com.shmedo.mcloudapp.R;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util
 * 文件名:   MyCountDownTimer
 * 创建者:   dpc
 * 创建时间:  2019/1/9 15:15
 *
 */
public class MyCountDownTimer extends CountDownTimer {
    private Button btnTime;
    public MyCountDownTimer(Button time,long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
        this.btnTime = time;
    }

    //计时过程
    @Override
    public void onTick(long l) {
        //防止计时过程中重复点击
        btnTime.setClickable(false);
        btnTime.setText(l/1000+"秒");
        btnTime.setBackgroundResource(R.drawable.shape_verify_btn_press);
    }

    //计时完毕的方法
    @Override
    public void onFinish() {
        //重新给Button设置文字
        btnTime.setText("重新获取");
        //设置可点击
        btnTime.setClickable(true);
        btnTime.setBackgroundResource(R.drawable.btn_blue_selector);
    }
}


