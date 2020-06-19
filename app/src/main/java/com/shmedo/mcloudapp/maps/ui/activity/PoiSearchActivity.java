package com.shmedo.mcloudapp.maps.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.CommonAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.MultiItemTypeAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.ViewHolder;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.maps.util.MapErrorUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class PoiSearchActivity extends BaseActivity implements TextWatcher, PoiSearch.OnPoiSearchListener, MultiItemTypeAdapter.OnItemClickListener {
    @BindView(R.id.et_search_tip)
    EditText mEtSearchTip;

    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    @BindView(R.id.iv_clear_text)
    ImageView mIvClearText;

    @BindView(R.id.rv_search_result)
    RecyclerView mRecyclerView;

    private CommonAdapter adapter;
    private List<PoiItem> poiResultList = new ArrayList<>();


    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;// POI搜索
    private PoiResult poiResult; // poi返回的结果
    private int currentPage = 1;

    private String mKeyWords = "";// 要输入的poi搜索关键字
    private String mCity;


    public static void startActivityForResult(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, PoiSearchActivity.class);
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
//        initStates();
        initView();
        initAdapter();
    }

    /**
     * 沉浸式状态栏
     */
    private void initStates() {
        if (Build.VERSION.SDK_INT > 19 && getApplicationContext().getApplicationInfo().targetSdkVersion > 19) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    private void initView() {
        mEtSearchTip.addTextChangedListener(this);

    }

    private void initAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommonAdapter<PoiItem>(this, R.layout.poi_search_adapter_item, poiResultList) {
            @Override
            protected void convert(ViewHolder holder, final PoiItem poiItem, final int position) {
                holder.setText(R.id.tv_search_title, poiItem.getTitle());
                holder.setText(R.id.tv_search_loc, poiItem.getSnippet());
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
            poiResultList.clear();
            // 刷新RecycleView
            adapter.notifyDataSetChanged();
            return;
        }

        String content = s.toString();
        if (!TextUtils.isEmpty(content) ) {//&& !TextUtils.isEmpty(mCity)
            doSearchQuery(content);
            mProgressBar.setVisibility(View.VISIBLE);
            mIvClearText.setVisibility(View.GONE);
        }
    }

    /**
     * 开始进行poi搜索
     */
    protected void doSearchQuery(String keywords) {
//        showProgressDialog();// 显示进度框
        currentPage = 1;
        // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query = new PoiSearch.Query(keywords, "", "上海");
        // 设置每页最多返回多少条poiitem
        query.setPageSize(20);
        // 设置查第一页
        query.setPageNum(currentPage);

        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }


    /**
     * POI信息查询回调方法
     */
    @Override
    public void onPoiSearched(PoiResult result, int resultCode) {
        mProgressBar.setVisibility(View.GONE);
        mIvClearText.setVisibility(View.VISIBLE);

        if (resultCode == 1000) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

                    if (poiItems != null && poiItems.size() > 0) {
                        poiResultList.clear();
                        poiResultList.addAll(poiItems);
                        // 刷新RecycleView
                        adapter.notifyDataSetChanged();
                    } else if (suggestionCities != null && suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);
                    } else {
                        ToastUtils.show(R.string.no_result);
                    }
                }
            } else {
                ToastUtils.show(R.string.no_result);
            }
        } else {
            ToastUtils.show(MapErrorUtil.getErrorMsg(resultCode));
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
}
