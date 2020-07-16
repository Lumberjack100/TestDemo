package com.shmedo.mcloudapp.maps.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.interfaces.Extras;
import com.shmedo.mcloudapp.maps.model.PageInfo;
import com.shmedo.mcloudapp.maps.util.MapErrorUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class PoiSearchActivity extends BaseActivity implements TextWatcher, PoiSearch.OnPoiSearchListener {
    private static final String ARG_PARAM1 = "param1";

    @BindView(R.id.et_search_tip)
    EditText mEtSearchTip;

    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    @BindView(R.id.iv_clear_text)
    ImageView mIvClearText;

    @BindView(R.id.rv_search_result)
    RecyclerView mRecyclerView;

    private PoiSearchAdapter poiSearchAdapter;

    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;// POI搜索
    private PoiResult poiResult; // poi返回的结果

    private static final int PAGE_SIZE = 10;

    private String keyWord;// 要输入的poi搜索关键字
    private String cityName;

    private PageInfo pageInfo = new PageInfo();


    public static void startActivityForResult(Activity activity, String cityName, int requestCode) {
        Intent intent = new Intent(activity, PoiSearchActivity.class);
        intent.putExtra(ARG_PARAM1, cityName);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_poi_search;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent();
        initView();
        initAdapter();
        initLoadMore();
    }


    private void parseIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            cityName = intent.getStringExtra(ARG_PARAM1);
        }
    }

    private void initView() {
        mEtSearchTip.requestFocus();
        mEtSearchTip.addTextChangedListener(this);
        mEtSearchTip.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if ("".equals(keyWord)) {
                        ToastUtils.show("请输入搜索关键字");
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
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        poiSearchAdapter = new PoiSearchAdapter();
        poiSearchAdapter.setAnimationEnable(true);
        mRecyclerView.setAdapter(poiSearchAdapter);
        poiSearchAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                PoiItem poiItem = poiSearchAdapter.getItem(position);
                Intent intent = getIntent();
                intent.putExtra(Extras.POIITEM_INFO, poiItem);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    /**
     * 初始化加载更多
     */
    private void initLoadMore() {
        poiSearchAdapter.getLoadMoreModule().setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadMore();
            }
        });
        poiSearchAdapter.getLoadMoreModule().setEnableLoadMore(true);
        poiSearchAdapter.getLoadMoreModule().setAutoLoadMore(true);
        //当自动加载开启，同时数据不满一屏时，是否继续执行自动加载更多(默认为true)
        poiSearchAdapter.getLoadMoreModule().setEnableLoadMoreIfNotFullPage(false);
    }

    /**
     * 加载更多
     */
    private void loadMore() {
        if (query != null && poiSearch != null && poiResult != null) {
            if (poiResult.getPageCount() - 1 > pageInfo.getPage()) {
                // page加一
                pageInfo.nextPage();
                query.setPageNum(pageInfo.getPage());// 设置查后一页
                poiSearch.searchPOIAsyn();
            } else {
                ToastUtils.show(R.string.no_result);
            }
        }
    }

    @OnClick({R.id.iv_search_left, R.id.iv_clear_text})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_search_left:
                finish();
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
            // 需要重置页数
            pageInfo.reset();
            // 刷新RecycleView
            poiSearchAdapter.setNewInstance(new ArrayList<>());
            // 刷新RecycleView
            return;
        }

        keyWord = s.toString();
        if (!TextUtils.isEmpty(keyWord) && !TextUtils.isEmpty(cityName)) {
            // 需要重置页数
            pageInfo.reset();
            doSearchQuery();
            mProgressBar.setVisibility(View.VISIBLE);
            mIvClearText.setVisibility(View.GONE);
        }
    }


    /**
     * 开始进行poi搜索
     */
    protected void doSearchQuery() {
        // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query = new PoiSearch.Query(keyWord, "", cityName);
        // 设置每页最多返回多少条poiitem
        query.setPageSize(PAGE_SIZE);
        // 设置查第一页
        query.setPageNum(pageInfo.getPage());

        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    /**
     * POI信息查询回调方法
     */
    @Override
    public void onPoiSearched(PoiResult result, int errorCode) {
        mProgressBar.setVisibility(View.GONE);
        mIvClearText.setVisibility(View.VISIBLE);

        if (errorCode != AMapException.CODE_AMAP_SUCCESS) {
            ToastUtils.show(MapErrorUtil.getErrorMsg(errorCode));
            return;
        }

        if (result == null || result.getQuery() == null) {
            ToastUtils.show(R.string.no_result);
            return;
        }

        if (result.getQuery().equals(query)) {// 是否是同一条
            poiResult = result;
            // 取得搜索到的poiitems有多少页
            List<PoiItem> poiItems = poiResult.getPois();
            // 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
            List<SuggestionCity> suggestionCities = poiResult.getSearchSuggestionCitys();
            if (poiItems != null && poiItems.size() > 0) {
                if (pageInfo.isFirstPage()) {
                    //如果是加载的第一页数据，用setNew
                    poiSearchAdapter.setNewInstance(poiItems);
                } else {
                    //不是第一页，则用add
                    poiSearchAdapter.addData(poiItems);
                }

                if (poiItems.size() < PAGE_SIZE) {
                    //如果不够一页,显示没有更多数据布局
                    poiSearchAdapter.getLoadMoreModule().loadMoreEnd();
                    ToastUtils.show("no more data");
                } else {
                    poiSearchAdapter.getLoadMoreModule().loadMoreComplete();
                }

            } else if (suggestionCities != null && suggestionCities.size() > 0) {
                showSuggestCity(suggestionCities);
            } else {
                ToastUtils.show(R.string.no_result);
            }
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    /**
     * poi没有搜索到数据，返回一些推荐城市的信息
     */
    private void showSuggestCity(List<SuggestionCity> cities) {
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        ToastUtils.show(infomation);
    }

    public class PoiSearchAdapter extends BaseQuickAdapter<PoiItem, BaseViewHolder> implements LoadMoreModule {
        public PoiSearchAdapter() {
            super(R.layout.poi_search_adapter_item);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, @Nullable PoiItem poiItem) {
            holder.setText(R.id.tv_search_title, poiItem.getTitle());
            holder.setText(R.id.tv_search_loc, poiItem.getSnippet());
            holder.getView(R.id.iv_route).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToastUtils.show("点击了路线");
                }
            });
        }
    }
}
