package com.shmedo.mcloudapp.projects.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/23 <br/>
 * 描述：     TODO
 */
public class HeaderSearchView extends FrameLayout implements TextView.OnEditorActionListener {
    @BindView(R.id.place_holder_search_layout)
    View holderSearchLayout;

    @BindView(R.id.input_search_layout)
    View inputSearchLayout;

    @BindView(R.id.tv_search_hint)
    TextView tvSearchHint;

    @BindView(R.id.et_search)
    ClearEditText etSearch;

    private OnSearchClickListener mListener;


    public HeaderSearchView(Context context) {
        this(context, null);
    }

    public HeaderSearchView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeaderSearchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.header_search_view, this, true);
        ButterKnife.bind(this);
        etSearch.setOnEditorActionListener(this);
        setSearchView(true);
    }

    private void setSearchView(boolean isHolderSearchView) {
        if (isHolderSearchView) {
            holderSearchLayout.setVisibility(View.VISIBLE);
            inputSearchLayout.setVisibility(View.GONE);
            etSearch.setText("");
            KeyBordUtils.hideSoftKeyboard(etSearch);
        } else {
            holderSearchLayout.setVisibility(View.GONE);
            inputSearchLayout.setVisibility(View.VISIBLE);
            KeyBordUtils.showSoftKeyboard(etSearch);
        }

        if (mListener != null) {
            mListener.onSearchViewSwitch(isHolderSearchView);
        }
    }

    public void setSearchHint(String text) {
        tvSearchHint.setHint(text);
        etSearch.setHint(text);
    }

    @OnClick({R.id.place_holder_search_layout, R.id.tv_cancel})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.place_holder_search_layout:
                setSearchView(false);
                break;

            case R.id.tv_cancel:
                setSearchView(true);
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            String text = etSearch.getText().toString();
            if (TextUtils.isEmpty(text)) {
                return true;
            }

            // 当按了搜索之后关闭软键盘
            etSearch.clearFocus();
            KeyBordUtils.hideSoftKeyboard(etSearch);
            if (mListener != null)
                mListener.onSearch(text.trim());

            return true;
        }
        return false;
    }

    public void setOnSearchClickListener(OnSearchClickListener listener) {
        this.mListener = listener;
    }

    public interface OnSearchClickListener {
        void onSearch(String keyWord);

        void onSearchViewSwitch(boolean isHolderView);
    }
}
