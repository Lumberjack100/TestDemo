package com.shmedo.mcloudapp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.CommonAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.MultiItemTypeAdapter;
import com.shmedo.mcloudapp.adapter.recyclerviewbaseadapter.ViewHolder;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.entity.SystemDataInfo;
import com.shmedo.mcloudapp.entity.SystemDataInfoDao;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.KeyBordUtils;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.views.ClearEditText;
import com.shmedo.mcloudapp.views.EmptyDataView;
import com.shmedo.mcloudapp.views.recycleviewitemdivider.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class SearchProjectActivity extends BaseActivity implements MultiItemTypeAdapter.OnItemClickListener, TextWatcher, TextView.OnEditorActionListener {

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.et_search)
    ClearEditText mEtSearch;

    @BindView(R.id.recycler_project)
    RecyclerView mRecyclerProject;

    @BindView(R.id.empty_data)
    EmptyDataView mEmptyData;

    private CommonAdapter adapter;

    private List<SystemDataInfo> projectList = new ArrayList<>();

    private DaoManager manager = DaoManager.getInstance();


    /**
     * 说明：启动Activity
     * <p>
     * 注意：这里使用到了Intent的Flag属性singleTop。singleTop模式下，在同一个task中，如果存在该Activity的实例，
     * 并且该Activity实例位于栈顶(即，该Activity位于前端)，则调用startActivity()时，不再创建该Activity的示例；
     * 而仅仅只是调用Activity的onNewIntent()。否则的话，则新建该Activity的实例，并将其置于栈顶。
     * </p>
     */
    public static void startActivity(Context context) {
        Intent intent = new Intent(context, SearchProjectActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_search_project;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("项目查询");
        mEtSearch.setHint("请输入工程项目名称关键字");
        initView();
        initAdapter();
    }

    private void initView() {
        //搜索框获取焦点，弹出软键盘
        KeyBordUtils.popSoftKeyboard(mEtSearch, true);
        mEtSearch.addTextChangedListener(this);
        mEtSearch.setOnEditorActionListener(this);
    }

    private void initAdapter() {
        mRecyclerProject.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerProject.addItemDecoration(new DividerItemDecoration());
        adapter = new CommonAdapter<SystemDataInfo>(this, R.layout.item_project, projectList) {
            @Override
            protected void convert(ViewHolder holder, final SystemDataInfo systemDataInfo, final int position) {
                holder.setText(R.id.tv_projectName, systemDataInfo.getProjName());
                holder.setText(R.id.tv_companyName, "惠山区洛社镇XX社区");
                holder.setText(R.id.tv_createTime, "2018.11.16 09:38");
            }
        };
        adapter.setOnItemClickListener(this);
        mRecyclerProject.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
        DeviceManageDetailActivity.startActivity(SearchProjectActivity.this, projectList.get(position));
    }

    @Override
    public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
        return false;
    }


    @OnClick({R.id.tv_cancel})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancel:
                SearchProjectActivity.this.finish();
                break;
        }
    }


    /**
     * 搜索处理逻辑<br/>
     * 查询本地数据库中匹配搜索关键字的当前用户的项目
     */
    private void searchProcess(String queryText) {
        List<SystemDataInfo> resultList = manager.getDaoSession().getSystemDataInfoDao().queryBuilder()
                .where(SystemDataInfoDao.Properties.ProjName.like("%" + queryText + "%"), SystemDataInfoDao.Properties.Account.isNotNull(), SystemDataInfoDao.Properties.Account.eq(MCloudApp.getAccount()))
                .list();

        if (resultList != null && resultList.size() > 0) {
            projectList.clear();
            projectList.addAll(resultList);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {
        if (!TextUtils.isEmpty(text)) {
            searchProcess(text.toString().trim());

        } else {
            KeyBordUtils.popSoftKeyboard(mEtSearch, true);
            projectList.clear();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            // 当按了搜索之后关闭软键盘
            KeyBordUtils.hideSoftKeyboard(mEtSearch);

            String text = mEtSearch.getText().toString();
            if (TextUtils.isEmpty(text)) {
                mEtSearch.clearFocus();
                return true;
            }

            searchProcess(text.trim());
            return true;
        }
        return false;
    }
}
