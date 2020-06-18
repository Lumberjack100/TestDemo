package com.shmedo.mcloudapp.maps.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.CommonAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.MultiItemTypeAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 常规搜索 Poi 点位
 */
public class SearchPoiFragment extends BaseSearchPoiDialogFragment implements TextWatcher, Inputtips.InputtipsListener, MultiItemTypeAdapter.OnItemClickListener {
    private static final String ARG_PARAM1 = "param1";

    @BindView(R.id.et_search_tip)
    EditText mEtSearchTip;

    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    @BindView(R.id.iv_clear_text)
    ImageView mIvClearText;

    @BindView(R.id.rv_search_result)
    RecyclerView mRecyclerView;

    private CommonAdapter adapter;
    private List<Tip> poiResultList = new ArrayList<>();

    private String mCity;


    public static SearchPoiFragment newInstance(String city) {
        SearchPoiFragment fragment = new SearchPoiFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, city);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCity = getArguments().getString(ARG_PARAM1);
            String ss="";
        }
    }

    @Override
    protected int initContentView() {
        return R.layout.fragment_search_poi;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        initView();
        initAdapter();
        return rootView;
    }

    private void initView() {
        mEtSearchTip.addTextChangedListener(this);

    }

    private void initAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CommonAdapter<Tip>(getContext(), R.layout.search_tip_recycle_item, poiResultList) {
            @Override
            protected void convert(ViewHolder holder, final Tip tip, final int position) {
                holder.setText(R.id.tv_search_title, tip.getName());
                holder.setText(R.id.tv_search_loc, tip.getAddress());
                holder.setOnClickListener(R.id.iv_route, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ToastUtils.show("点击了路线");
                    }
                });
            }
        };
        adapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
        ToastUtils.show("点击了条目");

    }

    @Override
    public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
        return false;
    }

    @OnClick({R.id.iv_search_left, R.id.iv_clear_text})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_search_left:
                dismiss();
                break;

            case R.id.iv_clear_text:
                mEtSearchTip.setText("");
                mIvClearText.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s == null || TextUtils.isEmpty(s.toString())) {
            mProgressBar.setVisibility(View.GONE);
            mIvClearText.setVisibility(View.GONE);
            return;
        }

        String content = s.toString();
        if (!TextUtils.isEmpty(content) && !TextUtils.isEmpty(mCity)) {
            // 调用高德地图搜索提示api
            InputtipsQuery inputtipsQuery = new InputtipsQuery(content, mCity);
            inputtipsQuery.setCityLimit(true);
            Inputtips inputTips = new Inputtips(getContext(), inputtipsQuery);
            inputTips.setInputtipsListener(this);
            inputTips.requestInputtipsAsyn();
            mProgressBar.setVisibility(View.VISIBLE);
            mIvClearText.setVisibility(View.GONE);
        }
    }

    /**
     * 高德地图搜索提示回调
     */
    @Override
    public void onGetInputtips(List<Tip> list, int i) {
        mProgressBar.setVisibility(View.GONE);
        mIvClearText.setVisibility(View.VISIBLE);
        if (list == null || list.size() == 0) {
            return;
        }
        poiResultList.clear();
        poiResultList.addAll(list);
        // 刷新RecycleView
        adapter.notifyDataSetChanged();
    }
}
