package com.shmedo.mcloudapp.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.util.DensityUtil;

/**
 * 项目名：  das-config-app
 * 包名：    com.example.medoDas.views
 * 文件名:   MyMenu
 * 创建者:   dpc
 * 创建时间:  2018/1/25 15:01
 * 描述：    TODO
 */

public class MyMenu extends Dialog implements View.OnClickListener{

    private Context context;
    private Window window = null;
    private Button item1Btn;
    private Button item2Btn;
    private Button cancel;
    private TextView title;
    private String titleStr;
    private String item1;
    private String item2;
    private MenuOnClickListener clickListener;

    public MyMenu(Context context, String title, String item1, String item2) {
        super(context);
        this.context = context;
        this.titleStr = title;
        this.item1 = item1;
        this.item2 = item2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("111","MyMenu ------------onCreate()");
        setFullScreen();
        setContentView(R.layout.ac_menu);
        windowDeploy(0, 0);
        //		setCancelable(false);
        initView();
    }

    private void initView() {
        title = (TextView) findViewById(R.id.title);
        item1Btn = (Button) findViewById(R.id.item_1);
        item2Btn = (Button) findViewById(R.id.item_2);
        cancel = (Button) findViewById(R.id.cancel);
        title.setText(titleStr);
        item1Btn.setText(item1);
        item2Btn.setText(item2);
        item1Btn.setOnClickListener(this);
        item2Btn.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }

    public void windowDeploy(int x, int y) {
        window = getWindow();
        window.setWindowAnimations(R.style.dialogWindowAnim);
        window.setBackgroundDrawableResource(R.color.vifrification);
        WindowManager.LayoutParams wl = window.getAttributes();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        wl.width = (int) (width * 0.95);
        wl.height = DensityUtil.Dp2Px(context, 201f);
        //        wl.alpha = 0.8f;
        wl.gravity = Gravity.BOTTOM;
        wl.dimAmount = 0.3f;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setAttributes(wl);
        //      window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        MyMenu.this.dismiss();
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.item_1:
                if(clickListener != null)clickListener.onItem1Click();
                break;
            case R.id.item_2:
                if(clickListener != null)clickListener.onItem2Click();
                break;
            case R.id.cancel:
                this.cancel();
                break;
        }
    }

    public void setOnMenuClickListener(MenuOnClickListener listener){
        this.clickListener = listener;
    }

    public interface MenuOnClickListener{
        void onItem1Click();
        void onItem2Click();
    }


}
