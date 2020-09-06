package kankan.wheel.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;

import static androidx.annotation.Dimension.SP;

/**
 * Created by Jesley on 2017/1/12.
 */

public class WheelTextView extends androidx.appcompat.widget.AppCompatTextView implements IWheelSubView {
    @ColorInt
    private int currentColor = Color.BLACK;

    @ColorInt
    private int defaultColor = Color.LTGRAY;

    @Dimension(unit = SP)
    private int currentSize = 16;

    @Dimension(unit = SP)
    private int defaultSize = 16;


    public WheelTextView(Context context) {
        super(context);
    }

    public WheelTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WheelTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onDrawBefor(boolean isCurrent) {
        if (isCurrent) {
            setTextColor(currentColor);
            setTextSize(currentSize);
        } else {
            setTextColor(defaultColor);
            setTextSize(defaultSize);
        }
    }

    public int getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(int currentColor) {
        this.currentColor = currentColor;
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    public void setDefaultColor(int defaultColor) {
        this.defaultColor = defaultColor;
    }

    public void setCurrentSize(int currentSize) {
        this.currentSize = currentSize;
    }

    public void setDefaultSize(int defaultSize) {
        this.defaultSize = defaultSize;
    }
}
