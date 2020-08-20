package com.shmedo.mcloudapp.projects.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.lxj.xpopup.core.CenterPopupView;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.util.DateUtil;

import java.util.Locale;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/20 <br/>
 * 描述：     过期项目引导提醒框
 */
public class ProjectExpiredGuideDialog extends CenterPopupView {
    private TextView tvExpiredDate;
    private String expiredDate;

    public ProjectExpiredGuideDialog(@NonNull Context context, String date) {
        super(context);
        expiredDate = date;
    }


    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_expired_guide;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        tvExpiredDate = findViewById(R.id.tv_expired_date);

        if (!TextUtils.isEmpty(expiredDate)) {
            try {
                expiredDate = DateUtil.StrToStrFormat(expiredDate, "yyyy-MM-dd HH:mm:ss", "yyyy.MM.dd");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        String msg = TextUtils.isEmpty(expiredDate) ? "抱歉！该项目已过期" : String.format(Locale.getDefault(), "抱歉！该项目已在%s过期", expiredDate);
        tvExpiredDate.setText(msg);

        findViewById(R.id.tv_confirm).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

}
