package com.shmedo.mcloudapp.projects.ui.activity;

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

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.entity.ProjectDetailInfoDao;
import com.shmedo.mcloudapp.entity.ProjectSearchKeyWordDao;
import com.shmedo.mcloudapp.projects.adapter.ProjectSearchKeyWordsAdapter;
import com.shmedo.mcloudapp.projects.adapter.ProjectSimpleItemAdapter;
import com.shmedo.mcloudapp.projects.model.ProjectDetailInfo;
import com.shmedo.mcloudapp.projects.model.ProjectItem;
import com.shmedo.mcloudapp.projects.model.ProjectSearchKeyWord;
import com.shmedo.mcloudapp.projects.view.ProjectExpiredGuideDialog;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.KeyBordUtils;
import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class ProjectSearchActivity extends BaseActivity implements TextWatcher {
    @BindView(R.id.et_keywords)
    ClearEditText mEtKeyWords;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    @BindView(R.id.recyclerview_history)
    RecyclerView mRecyclerViewHistory;

    @BindView(R.id.scrollView_history)
    NestedScrollView scrollViewHistory;

    private ProjectSimpleItemAdapter searchResultAdapter;
    private ProjectSearchKeyWordsAdapter keyWordsAdapter;
    private List<ProjectItem> projectItems = new ArrayList<>();
    private List<ProjectSearchKeyWord> historyKeyWordList = new ArrayList<>();

    private int userId;
    private int companyID;
    private String keyWords;// 要输入的poi搜索关键字


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ProjectSearchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_project_search;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        initUserData();
        initView();
        initHistoryAdater();
        initResultAdapter();
        loadHistoryKeyWordsData();
        setHistoryKeyWordsVisibility(true);
    }

    private void initUserData() {
        UserInfo userInfo = MCloudApp.getCurrentUserInfo();
        if (userInfo != null && userInfo.getUser() != null) {
            UserInfo.UserBean user = userInfo.getUser();
            userId = user.getId();
        }
        if (userInfo.getDepartments() != null && userInfo.getDepartments().size() > 0) {
            companyID = userInfo.getDepartments().get(0).getCompanyID();
        }
    }

    private void initView() {
        mEtKeyWords.requestFocus();
        mEtKeyWords.addTextChangedListener(this);
        mEtKeyWords.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (TextUtils.isEmpty(textView.getText())) {
                        ToastUtils.show("请输入搜索内容");
                    } else {
                        // 当按了搜索之后关闭软键盘
                        KeyBordUtils.hideSoftKeyboard(mEtKeyWords);
                        doSearchQuery(true);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void initHistoryAdater() {
        mRecyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        keyWordsAdapter = new ProjectSearchKeyWordsAdapter(historyKeyWordList);
        keyWordsAdapter.setAnimationEnable(true);
        keyWordsAdapter.setAnimationFirstOnly(false);
        keyWordsAdapter.setOnItemClickListener(new com.chad.library.adapter.base.listener.OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                ProjectSearchKeyWord searchKeyWord = historyKeyWordList.get(position);
                mEtKeyWords.setText(searchKeyWord.getKeyWord());
                mEtKeyWords.setSelection(searchKeyWord.getKeyWord().length());
                // 当按了搜索之后关闭软键盘
                KeyBordUtils.hideSoftKeyboard(mEtKeyWords);
            }
        });
        mRecyclerViewHistory.setAdapter(keyWordsAdapter);
    }

    private void initResultAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        DefaultItemDecoration mItemDecoration = new DefaultItemDecoration(ContextCompat.getColor(this, R.color.transparent), 0, DensityUtil.Dp2Px(this, 14));
        mRecyclerView.addItemDecoration(mItemDecoration);
        searchResultAdapter = new ProjectSimpleItemAdapter(R.layout.item_project_info_normal, projectItems);
        searchResultAdapter.setAnimationEnable(true);
        searchResultAdapter.setAnimationFirstOnly(false);
        searchResultAdapter.setOnItemClickListener(new com.chad.library.adapter.base.listener.OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                ProjectItem projectItem = projectItems.get(position);
                if (!(projectItem.getObject() instanceof ProjectDetailInfo)) {
                    return;
                }

                ProjectDetailInfo detailInfo = (ProjectDetailInfo) projectItem.getObject();
                //已过期的项目，针对非米度公司的用户进行限制操作
                if (detailInfo.isOutOfDate() && companyID != 1) {
                    ProjectExpiredGuideDialog customPopup = new ProjectExpiredGuideDialog(ProjectSearchActivity.this, detailInfo.getRegisterTime());
                    new XPopup.Builder(ProjectSearchActivity.this)
                            .asCustom(customPopup)
                            .show();
                } else {
                    DevicesInProjectActivity.startActivity(ProjectSearchActivity.this, detailInfo.getProjectID(), detailInfo.getProjectName());
                }
            }
        });
        mRecyclerView.setAdapter(searchResultAdapter);
    }

    @OnClick({R.id.tv_search, R.id.iv_clear_history_keywords})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_search:
                if (TextUtils.isEmpty(mEtKeyWords.getText().toString().trim())) {
                    ToastUtils.show("请输入搜索内容");
                    return;
                }
                // 当按了搜索之后关闭软键盘
                KeyBordUtils.hideSoftKeyboard(mEtKeyWords);
                doSearchQuery(true);
                break;

            case R.id.iv_clear_history_keywords:
                historyKeyWordList.clear();
                keyWordsAdapter.notifyDataSetChanged();
                DaoManager.getInstance().getDaoSession().getProjectSearchKeyWordDao().deleteAll();
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
            setHistoryKeyWordsVisibility(true);
            return;
        }

        keyWords = s.toString();
        if (!TextUtils.isEmpty(keyWords)) {
            doSearchQuery(false);
        }
    }

    /**
     * 开始进行搜索
     */
    private void doSearchQuery(boolean isDoSaveKeyWordAction) {
        List<ProjectDetailInfo> resultList = DaoManager.getInstance().getDaoSession().getProjectDetailInfoDao().queryBuilder()
                .where(ProjectDetailInfoDao.Properties.UserId.eq(userId), ProjectDetailInfoDao.Properties.ProjectName.like("%" + keyWords + "%"))
                .list();

        projectItems.clear();
        if (resultList != null && resultList.size() > 0) {
            for (ProjectDetailInfo projectDetailInfo : resultList) {
                projectItems.add(new ProjectItem(projectDetailInfo));
            }

            if (isDoSaveKeyWordAction) {
                updateHistoryKeyWordsData();
            }
        } else {
            searchResultAdapter.setEmptyView(R.layout.empty_view);
            searchResultAdapter.notifyDataSetChanged();
        }


        setHistoryKeyWordsVisibility(false);
    }

    private void updateHistoryKeyWordsData() {
        for (ProjectSearchKeyWord searchKeyWord : historyKeyWordList) {
            if (searchKeyWord.getKeyWord().equals(keyWords)) {
                return;//已经保存过这个关键字
            }
        }
        ProjectSearchKeyWord searchKeyWord = new ProjectSearchKeyWord(userId, keyWords);
        historyKeyWordList.add(searchKeyWord);
        //更新到本地数据库
        DaoManager.getInstance().getDaoSession().getProjectSearchKeyWordDao().insertOrReplaceInTx(searchKeyWord);
    }

    private void loadHistoryKeyWordsData() {
        List<ProjectSearchKeyWord> resultList = DaoManager.getInstance().getDaoSession().getProjectSearchKeyWordDao().queryBuilder()
                .where(ProjectSearchKeyWordDao.Properties.UserId.eq(userId))
                .list();
        if (resultList != null && resultList.size() > 0) {
            historyKeyWordList.clear();
            historyKeyWordList.addAll(resultList);
        }
        keyWordsAdapter.notifyDataSetChanged();
    }

    private void setHistoryKeyWordsVisibility(boolean isShow) {
        if (isShow) {
            mRecyclerView.setVisibility(View.GONE);
            scrollViewHistory.setVisibility(View.VISIBLE);
            keyWordsAdapter.notifyDataSetChanged();
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            scrollViewHistory.setVisibility(View.GONE);
            searchResultAdapter.notifyDataSetChanged();
        }
    }
}
