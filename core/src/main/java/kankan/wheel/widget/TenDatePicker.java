package kankan.wheel.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.shmedo.core.R;

import java.util.Calendar;

import kankan.wheel.widget.adapters.NumericWheelAdapter;

/**
 * @author Jesley
 */
public class TenDatePicker extends PopupWindow
{

    private Activity mContext;

    private View mMainView;

    private ViewFlipper viewfipper;

    private Button btn_submit, btn_cancel;

    private TextView textViewTitle;

    private String age;

    private DateNumericAdapter monthAdapter, dayAdapter, yearAdapter;

    private WheelView year, month, day;

    private int mCurYear = 80, mCurMonth = 5, mCurDay = 14;

    private String[] dateType;

    private Calendar pickedCalendar = null;


    public TenDatePicker(Activity context, String date)
    {
        super(context);
        this.mContext = context;
        this.age = date;//"2016-09-07";

        initView();
    }

    public TenDatePicker(Activity context, String date, String title)
    {
        super(context);
        mContext = context;
        this.age = date;//"2016-09-07";

        initView();

        if(!TextUtils.isEmpty(title))
        {
            textViewTitle.setText(title);
        }
    }


    private void initView()
    {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMainView = inflater.inflate(R.layout.date_picker_layout, null);
        viewfipper = new ViewFlipper(mContext);
        viewfipper.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        year = (WheelView) mMainView.findViewById(R.id.year);
        year.setVisibleItems(3);

        month = (WheelView) mMainView.findViewById(R.id.month);
        month.setVisibleItems(3);

        day = (WheelView) mMainView.findViewById(R.id.day);
        day.setVisibleItems(3);

        btn_submit = (Button) mMainView.findViewById(R.id.submit);
        btn_cancel = (Button) mMainView.findViewById(R.id.cancel);
        textViewTitle = (TextView) mMainView.findViewById(R.id.tv_title);

        Calendar calendar = Calendar.getInstance();
        int curYear = calendar.get(Calendar.YEAR);
        if (age != null && age.contains("-"))
        {
            String str[] = age.split("-");
            mCurYear = 100 - (curYear - Integer.parseInt(str[0]));
            mCurMonth = Integer.parseInt(str[1]) - 1;
            mCurDay = Integer.parseInt(str[2]) - 1;
        }
        dateType = mContext.getResources().getStringArray(R.array.date);

        // year
        yearAdapter = new DateNumericAdapter(mContext, curYear - 100, curYear + 100);
        yearAdapter.setDataType(dateType[0]);
        year.setViewAdapter(yearAdapter);
        year.setCurrentItem(mCurYear);
        year.addChangingListener(listener);

        //month
        monthAdapter = new DateNumericAdapter(mContext, 1, 12);
        monthAdapter.setDataType(dateType[1]);
        month.setViewAdapter(monthAdapter);
        month.setCurrentItem(mCurMonth);
        month.addChangingListener(listener);

        //day
        dayAdapter = new DateNumericAdapter(mContext, 1, 31);
        dayAdapter.setDataType(dateType[2]);
        day.setViewAdapter(dayAdapter);
        day.setCurrentItem(mCurDay);
        day.addChangingListener(listener);

        updateDays(year, month, day);

        viewfipper.addView(mMainView);
        viewfipper.setFlipInterval(6000000);
        this.setContentView(viewfipper);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x00000000);
        this.setBackgroundDrawable(dw);
        this.update();

        backgroundAlpha(0.5f);//0.0-1.0
        this.setOnDismissListener(new OnDismissListener()
        {
            @Override
            public void onDismiss()
            {
                backgroundAlpha(1f);
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                backgroundAlpha(1f);
                TenDatePicker.this.dismiss();
            }
        });

    }


    /**
     * 设置添加屏幕的背景透明度
     * <p/>
     *
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha)
    {
        WindowManager.LayoutParams lp = mContext.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        if (bgAlpha == 1)
        {
            mContext.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//不移除该Flag的话,可能出现黑屏的bug
        }
        else
        {
            mContext.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug
        }
        mContext.getWindow().setAttributes(lp);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y)
    {
        super.showAtLocation(parent, gravity, x, y);
        viewfipper.startFlipping();
    }


    private OnWheelChangedListener listener = new OnWheelChangedListener()
    {
        public void onChanged(WheelView wheel, int oldValue, int newValue)
        {
            updateDays(year, month, day);
        }
    };

    private void updateDays(WheelView year, WheelView month, WheelView day)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + year.getCurrentItem() - 100);
        calendar.set(Calendar.MONTH, month.getCurrentItem() + 1);
        calendar.set(Calendar.DAY_OF_MONTH, 0);

        int maxDays = calendar.get(Calendar.DAY_OF_MONTH);
        dayAdapter = new DateNumericAdapter(mContext, 1, maxDays);
        dayAdapter.setDataType(dateType[2]);
        day.setViewAdapter(dayAdapter);

        int curDay = Math.min(maxDays, day.getCurrentItem() + 1);
        day.setCurrentItem(curDay - 1, true);
        calendar.set(Calendar.DAY_OF_MONTH, curDay);

        int years = calendar.get(Calendar.YEAR);
        age = years + "-" + (month.getCurrentItem() + 1) + "-" + (day.getCurrentItem() + 1);

        pickedCalendar = calendar;
    }

    /**
     * Adapter for numeric wheels. Highlights the current value.
     */
    private class DateNumericAdapter extends NumericWheelAdapter
    {
        private String dataType = "";

        /**
         * Constructor
         */
        public DateNumericAdapter(Context context, int minValue, int maxValue)
        {
            super(context, minValue, maxValue);
            setTextSize(16);
        }

        @Override
        public CharSequence getItemText(int index)
        {
            return super.getItemText(index) + dataType;
        }

        public String getDataType()
        {
            return dataType;
        }

        public void setDataType(String dataType)
        {
            this.dataType = dataType;
        }

        @Override
        protected void configureTextView(TextView view)
        {
            super.configureTextView(view);
            view.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
        }

        @Override
        public View getItem(int index, View convertView, ViewGroup parent)
        {
            if (index >= 0 && index < getItemsCount())
            {
                if (convertView == null)
                {
                    convertView = new WheelTextView(context);
                    ((WheelTextView) convertView).setDefaultColor(Color.parseColor("#FFCCCCCC"));//LTGRAY
                    ((WheelTextView) convertView).setCurrentColor(Color.parseColor("#3f83f5"));
                }
                WheelTextView textView = (WheelTextView) convertView;
                CharSequence text = getItemText(index);
                if (text == null)
                {
                    text = "";
                }
                textView.setText(text);
                configureTextView(textView);

                return convertView;
            }
            return null;
        }
    }

    public Calendar getPickedCalendar()
    {
        return pickedCalendar;
    }

    public void setLeftListener(OnClickListener leftListener)
    {
        btn_cancel.setOnClickListener(leftListener);
    }

    public void setRightListener(OnClickListener rightListener)
    {
        btn_submit.setOnClickListener(rightListener);
    }
}
