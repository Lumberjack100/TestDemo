package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.ColorUtils;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.util.DateUtil;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelTextView;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyDatePicker extends BaseDialogFragment {
    private static final int YEAR_MAX = 218;

    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.year)
    WheelView year;

    @BindView(R.id.month)
    WheelView month;

    @BindView(R.id.day)
    WheelView day;

    private DateNumericAdapter monthAdapter, dayAdapter, yearAdapter;

    private int mCurYear = 0, mCurMonth = 0, mCurDay = 0;

    private String[] dateType;

    private String age;
    private String title;

    private Calendar pickedCalendar = null;

    private OnPositiveClickListener mListener;


    public MyDatePicker(String date, String title) {
        this.age = TextUtils.isEmpty(date) ? DateUtil.getNowDateYYYYMMDDString() : date;
        this.title = title;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_date_picker;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    private void initView() {
        if (!TextUtils.isEmpty(title)) {
            mTvTitle.setText(title);
        }

        year.setVisibleItems(5);
        month.setVisibleItems(5);
        day.setVisibleItems(5);

        Calendar calendar = Calendar.getInstance();
        int curYear = calendar.get(Calendar.YEAR);
        if (age != null && age.contains("-")) {
            String str[] = age.split("-");
            mCurYear = YEAR_MAX - (curYear - Integer.parseInt(str[0]));
            mCurMonth = Integer.parseInt(str[1]) - 1;
            mCurDay = Integer.parseInt(str[2]) - 1;
        }
        dateType = mActivity.getResources().getStringArray(R.array.date);

        // year
        yearAdapter = new MyDatePicker.DateNumericAdapter(mActivity, curYear - YEAR_MAX, curYear + 100);
        yearAdapter.setDataType(dateType[0]);
        year.setViewAdapter(yearAdapter);
        year.setCurrentItem(mCurYear);
        year.addChangingListener(listener);

        //month
        monthAdapter = new DateNumericAdapter(mActivity, 1, 12);
        monthAdapter.setDataType(dateType[1]);
        month.setViewAdapter(monthAdapter);
        month.setCurrentItem(mCurMonth);
        month.addChangingListener(listener);

        //day
        dayAdapter = new DateNumericAdapter(mActivity, 1, 31);
        dayAdapter.setDataType(dateType[2]);
        day.setViewAdapter(dayAdapter);
        day.setCurrentItem(mCurDay);
        day.addChangingListener(listener);

        updateDays(year, month, day);

    }

    private OnWheelChangedListener listener = new OnWheelChangedListener() {
        public void onChanged(WheelView wheel, int oldValue, int newValue) {
            updateDays(year, month, day);
        }
    };


    private void updateDays(WheelView year, WheelView month, WheelView day) {
        Calendar calendar = Calendar.getInstance();

        //不能超过当前系统日期年份
        if (year.getCurrentItem() > YEAR_MAX) {
            year.setCurrentItem(YEAR_MAX);
        }

        //不能超过当前系统日期日期的月份
        int cur_month = calendar.get(Calendar.MONTH);
        if (year.getCurrentItem() == YEAR_MAX && month.getCurrentItem() > cur_month) {
            month.setCurrentItem(cur_month);
        }

        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + year.getCurrentItem() - YEAR_MAX);
        calendar.set(Calendar.MONTH, month.getCurrentItem() + 1);
        calendar.set(Calendar.DAY_OF_MONTH, 0);

        int maxDays = calendar.get(Calendar.DAY_OF_MONTH);
        dayAdapter = new DateNumericAdapter(mActivity, 1, maxDays);
        dayAdapter.setDataType(dateType[2]);
        day.setViewAdapter(dayAdapter);

        int cur_day = Integer.parseInt(DateUtil.getNowStrDate("dd"));
        if (year.getCurrentItem() == YEAR_MAX && month.getCurrentItem() == cur_month && (day.getCurrentItem() + 1) > cur_day) {

        } else {
            cur_day = Math.min(maxDays, day.getCurrentItem() + 1);
        }
        day.setCurrentItem(cur_day - 1, true);
        calendar.set(Calendar.DAY_OF_MONTH, cur_day);

        String ddd = calendar.get(Calendar.YEAR) + "-" + (month.getCurrentItem() + 1) + "-" + (day.getCurrentItem() + 1);
        Date date = calendar.getTime();

        pickedCalendar = calendar;
    }


    @OnClick({R.id.tv_cancel, R.id.tv_confirm})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                dismiss();
                break;

            case R.id.tv_confirm:
                doPositiveClick(view);
                break;
        }
    }

    private void doPositiveClick(View view) {
        if (mListener == null) {
            return;
        }

        String dateStr = DateUtil.DateToStrFormat(pickedCalendar.getTime(), "yyyy-MM-dd");
        mListener.onPositiveClick(dateStr);
        dismiss();
    }

    public interface OnPositiveClickListener {
        void onPositiveClick(String date);
    }

    public void setOnPositiveClickListener(OnPositiveClickListener listener) {
        mListener = listener;
    }


    /**
     * Adapter for numeric wheels. Highlights the current value.
     */
    private class DateNumericAdapter extends NumericWheelAdapter {
        private String dataType = "";

        /**
         * Constructor
         */
        public DateNumericAdapter(Context context, int minValue, int maxValue) {
            super(context, minValue, maxValue);
            setTextSize(16);
        }

        @Override
        public CharSequence getItemText(int index) {
            return super.getItemText(index) + dataType;
        }

        public String getDataType() {
            return dataType;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }

        @Override
        protected void configureTextView(TextView view) {
            super.configureTextView(view);
            view.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
        }

        @Override
        public View getItem(int index, View convertView, ViewGroup parent) {
            if (index >= 0 && index < getItemsCount()) {
                if (convertView == null) {
                    convertView = new WheelTextView(context);
                    ((WheelTextView) convertView).setDefaultColor(ColorUtils.getColor(R.color.text_color_cccccc));//LTGRAY
                    ((WheelTextView) convertView).setCurrentColor(ColorUtils.getColor(R.color.title_text_color));

                    ((WheelTextView) convertView).setDefaultSize(15);
                    ((WheelTextView) convertView).setCurrentSize(18);
                }

                WheelTextView textView = (WheelTextView) convertView;
                CharSequence text = getItemText(index);
                if (text == null) {
                    text = "";
                }
                textView.setText(text);
                configureTextView(textView);

                return convertView;
            }
            return null;
        }
    }
}
