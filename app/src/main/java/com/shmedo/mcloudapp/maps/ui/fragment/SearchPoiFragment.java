package com.shmedo.mcloudapp.maps.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.maps.adapter.PoiSearchAdapter;
import com.shmedo.mcloudapp.maps.model.PoiPageInfo;
import com.shmedo.mcloudapp.maps.ui.activity.SearchPoiActivity;
import com.shmedo.mcloudapp.maps.util.MapErrorUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 常规搜索 Poi 点位
 */
public class SearchPoiFragment extends BaseFragment implements TextWatcher, PoiSearch.OnPoiSearchListener {
    private static final int PAGE_SIZE = 15;

    @BindView(R.id.et_search_tip)
    ClearEditText mEtSearchTip;

    @BindView(R.id.rv_search_result)
    RecyclerView mRecyclerView;

    private PoiSearchAdapter poiSearchAdapter;

    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;// POI搜索
    private PoiResult poiResult; // poi返回的结果

    private String keyWord;// 要输入的poi搜索关键字
    private PoiPageInfo poiPageInfo = new PoiPageInfo();

    private String cityName;
    private String poiTitle;

    private SearchPoiActivity activity;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search_poi;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (SearchPoiActivity)getActivity();
        initAdapter();
        initListener();
        parseIntent();
    }

    private void parseIntent() {
        Intent intent = activity.getIntent();
        if (intent != null) {
            cityName = intent.getStringExtra(SearchPoiActivity.CITY_NAME);
            poiTitle = intent.getStringExtra(SearchPoiActivity.POI_TITLE);
            if (!TextUtils.isEmpty(poiTitle)) {
                mEtSearchTip.setText(poiTitle);
            }
        }
    }

    private void initListener() {
        mEtSearchTip.requestFocus();
        mEtSearchTip.addTextChangedListener(this);
        mEtSearchTip.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (TextUtils.isEmpty(textView.getText())) {
                        ToastUtils.show("请输入搜索内容");
                    } else {
                        doSearchQuery();
                    }
                    return true;
                }
                return false;
            }
        });

    }

    private void initAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        poiSearchAdapter = new PoiSearchAdapter();
        poiSearchAdapter.setAnimationEnable(true);
        mRecyclerView.setAdapter(poiSearchAdapter);
        poiSearchAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                PoiItem poiItem = poiSearchAdapter.getItem(position);
                Intent intent = activity.getIntent();
                intent.putExtra(AppContants.Extras.POIITEM_INFO, poiItem);
                activity.setResult(Activity.RESULT_OK, intent);
                activity.finish();
            }
        });
        poiSearchAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                ToastUtils.show("点击了路线");
            }
        });

        poiSearchAdapter.getLoadMoreModule().setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadMore();
            }
        });
        poiSearchAdapter.getLoadMoreModule().setEnableLoadMore(true);
        //        poiSearchAdapter.getLoadMoreModule().setAutoLoadMore(true);
//        //当自动加载开启，同时数据不满一屏时，是否继续执行自动加载更多(默认为true)
//        poiSearchAdapter.getLoadMoreModule().setEnableLoadMoreIfNotFullPage(false);
    }



    /**
     * 加载更多
     */
    private void loadMore() {
        if (query != null && poiSearch != null && poiResult != null) {
            if (poiResult.getPageCount() - 1 > poiPageInfo.getPage()) {
                // page加一
                poiPageInfo.nextPage();
                query.setPageNum(poiPageInfo.getPage());// 设置查后一页
                poiSearch.searchPOIAsyn();
            } else {
//                ToastUtils.show(R.string.no_result);
            }
        }
    }

    @OnClick({R.id.tv_cancel_search})
    public void onClick(View view) {
        if (view.getId() == R.id.tv_cancel_search) {
            activity.finish();
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
            resetData();
            return;
        }

        keyWord = s.toString();
        if (!TextUtils.isEmpty(keyWord)) {
            resetData();
            doSearchQuery();
        }
    }


    /**
     * 开始进行poi搜索
     */
    private void doSearchQuery() {
        // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query = new PoiSearch.Query(keyWord, "", cityName);
        // 设置每页最多返回多少条poiitem
        query.setPageSize(PAGE_SIZE);
        query.setPageNum(poiPageInfo.getPage());

        poiSearch = new PoiSearch(activity, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    /**
     * POI信息查询回调方法
     */
    @Override
    public void onPoiSearched(PoiResult result, int errorCode) {
        if (errorCode != AMapException.CODE_AMAP_SUCCESS) {
            ToastUtils.show(MapErrorUtil.getErrorMsg(errorCode));
            return;
        }

        if (result == null || result.getQuery() == null) {
            ToastUtils.show(R.string.no_result);
            return;
        }

        if (!result.getQuery().equals(query)) {// 是否是同一个搜索
            return;
        }

        poiResult = result;
        // 取得搜索到的poiitems有多少页
        List<PoiItem> poiItems = poiResult.getPois();
        // 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
        List<SuggestionCity> suggestionCities = poiResult.getSearchSuggestionCitys();
        if (poiItems != null && poiItems.size() > 0) {
            if (poiPageInfo.isFirstPage()) {
                //如果是加载的第一页数据，用setNew
                poiSearchAdapter.setNewInstance(poiItems);
            } else {
                //不是第一页，则用add
                poiSearchAdapter.addData(poiItems);
            }

            if (poiItems.size() < PAGE_SIZE) {
                //如果不够一页,显示没有更多数据布局
                poiSearchAdapter.getLoadMoreModule().loadMoreEnd();
            } else {
                poiSearchAdapter.getLoadMoreModule().loadMoreComplete();
            }

        }
//        else if (suggestionCities != null && suggestionCities.size() > 0) {
//            showSuggestCity(suggestionCities);
//        }
        else {
            ToastUtils.show(R.string.no_result);
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    private void resetData() {
        // 需要重置页数
        poiPageInfo.reset();
        // 刷新RecycleView
        poiSearchAdapter.setNewInstance(new ArrayList<>());
    }

    /**
     * poi没有搜索到数据，返回一些推荐城市的信息
     */
    private void showSuggestCity(List<SuggestionCity> cities) {
        StringBuilder infomation = new StringBuilder("推荐城市\n");
        for (int i = 0; i < cities.size(); i++) {
            infomation.append("城市名称:")
                    .append(cities.get(i).getCityName())
                    .append("城市区号:")
                    .append(cities.get(i).getCityCode())
                    .append("城市编码:")
                    .append(cities.get(i).getAdCode())
                    .append("\n");
        }
        ToastUtils.show(infomation.toString());
    }


}
