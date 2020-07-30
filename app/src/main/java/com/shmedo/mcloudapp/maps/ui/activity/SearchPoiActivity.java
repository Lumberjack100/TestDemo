package com.shmedo.mcloudapp.maps.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.maps.model.PoiSearchType;
import com.shmedo.mcloudapp.maps.ui.fragment.SearchPoiByLatLngFragment;
import com.shmedo.mcloudapp.maps.ui.fragment.SearchPoiFragment;

public class SearchPoiActivity extends BaseActivity {
    public static final String SEARCH_TYPE = "search_type";
    public static final String CITY_NAME = "city_name";
    public static final String POI_TITLE = "poi_title";

    private PoiSearchType poiSearchType = PoiSearchType.NORMAL_SEARCH;
    private Fragment currentFragment;


    public static void startActivityForResult(Activity activity, PoiSearchType poiSearchType, String cityName, String poiTitle, int requestCode) {
        Intent intent = new Intent(activity, SearchPoiActivity.class);
        intent.putExtra(SEARCH_TYPE, poiSearchType);
        intent.putExtra(CITY_NAME, cityName);
        intent.putExtra(POI_TITLE, poiTitle);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivityForResult(intent, requestCode);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_search_poi;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent();
        loadFragment();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("CurrentFragment", currentFragment.getClass().getName());
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            poiSearchType = (PoiSearchType) intent.getSerializableExtra(SEARCH_TYPE);
        }
    }

    private void loadFragment() {
        if (poiSearchType == PoiSearchType.NORMAL_SEARCH) {
            currentFragment = new SearchPoiFragment();
        } else {
            currentFragment = new SearchPoiByLatLngFragment();
        }

        showFragment();
    }

    private void showFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, currentFragment, currentFragment.getClass().getName());
        transaction.commit();
    }
}
