package com.shmedo.mcloudapp.maps.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shmedo.mcloudapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 带图标控件
 *
 * @version 1.2
 */
public abstract class BaseIconView extends RelativeLayout implements IconViewInterface {
    @BindView(R.id.iv_icon)
     ImageView mIconView;

    @BindView(R.id.tv_desc)
    TextView mTextView;

    public BaseIconView(Context context) {
        this(context, null);
    }

    public BaseIconView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //init Views
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_base_icon, this, true);
        ButterKnife.bind(this);
        initFromAttributes(context, attrs, defStyleAttr);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        boolean isCreateBg = createBackground();
        boolean isCreateIcon = createIcon();
        //未设置背景图片和icon图片，才读取自定义属性
        if (!(isCreateBg && isCreateIcon)) {
            //自定义属性
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BaseIconView);
            if (null == getBackground()) {
                //未设置background属性，则解析自定义background属性值
                int bgResId = ta.getResourceId(R.styleable.BaseIconView_biv_background, 0);
                if (bgResId != 0) {
                    setBackgroundResource(bgResId);
                }
            }
            //控件中心图片资源属性
            int iconResId = ta.getResourceId(R.styleable.BaseIconView_biv_icon, 0);
            if (iconResId != 0 && null != mIconView) {
                mIconView.setImageResource(iconResId);
            }
            ta.recycle();
        }
    }

    public void setText(String text){
        if (mTextView != null) {
            mTextView.setText(text);
        }
    }

    /**
     * 设置icon背景
     *
     * @param resId
     */
    public void setIconResource(int resId) {
        if (mIconView != null) {
            mIconView.setImageResource(resId);
        }
    }


    /**
     * 设置icon背景
     *
     * @param drawable
     */
    public void setIconResource(Drawable drawable) {
        if (mIconView != null) {
            mIconView.setImageDrawable(drawable);
        }
    }

    public void setIconViewSelected(boolean pressed) {
        if (mIconView != null) {
            mIconView.setSelected(pressed);
        }
    }

    /**
     * 获取icon所属View selected状态
     *
     * @return
     */
    public boolean isIconViewSelected() {
        if (mIconView != null) {
            return mIconView.isSelected();
        }
        return false;
    }

    public View getIconView() {
        return mIconView;
    }

}
