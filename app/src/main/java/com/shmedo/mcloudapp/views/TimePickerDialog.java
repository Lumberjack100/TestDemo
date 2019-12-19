package com.shmedo.mcloudapp.views;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import com.shmedo.mcloudapp.R;
import top.defaults.view.DateTimePickerView;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import static top.defaults.view.DateTimePickerView.TYPE_YEAR_MONTH_DAY_HOUR_MINUTE;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.views
 * 文件名:   TimePickerDialog
 * 创建者:   dpc
 * 创建时间:  2019/2/12 17:33
 *
 */
public class TimePickerDialog extends BaseDialog {
    private DateTimePickerView mPickerView;

    public TimePickerDialog(Context context) {
        super(R.layout.dilog_timepicker, context);
    }

    @Override
    protected void updataView(ViewHelp helper) {
        mPickerView = helper.getView(R.id.datePickerView);
        mPickerView.setType(TYPE_YEAR_MONTH_DAY_HOUR_MINUTE);

        // 注意：月份是从0开始计数的
        Calendar date = Calendar.getInstance();
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int dayOfMonth = date.get(Calendar.DAY_OF_MONTH);
        int time = date.get(Calendar.HOUR_OF_DAY);
        int min = date.get(Calendar.MINUTE);
        int second = date.get(Calendar.SECOND);
        mPickerView.setSelectedDate(date);
        mPickerView.setStartDate(new GregorianCalendar(2008, 10, 1, 12, 30,10));
        mPickerView.setEndDate(new GregorianCalendar(year, month, dayOfMonth, time, min,second));
        helper.addOnClickLisinter(R.id.datePickerCancel);
        helper.addOnClickLisinter(R.id.datePickerOk);
    }

    public void setTimeLisinter(final TextView textView) {
        this.setOnChildClickLisinter(new OnChildClickLisinter() {
            @Override
            public void onChildChildClick(Dialog dialog, View view) {
                switch (view.getId()) {
                    case R.id.datePickerOk:
                        textView.setText(getDateString(mPickerView.getSelectedDate()));
                        dismiss();
                        break;
                    case R.id.datePickerCancel:
                        dismiss();
                        break;
                }
            }
        });
    }

    @NonNull
    private String getDateString(Calendar date) {
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int dayOfMonth = date.get(Calendar.DAY_OF_MONTH);
        int time = date.get(Calendar.HOUR_OF_DAY);
        int min = date.get(Calendar.MINUTE);
        int second = date.get(Calendar.SECOND);
        return String.format(Locale.getDefault(), "%d-%02d-%02d %02d:%02d:%02d", year, month + 1, dayOfMonth,time,min,second);

    }
}