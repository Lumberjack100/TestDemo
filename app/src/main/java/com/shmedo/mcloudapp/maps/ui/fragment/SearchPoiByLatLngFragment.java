package com.shmedo.mcloudapp.maps.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.maps.ui.activity.SearchPoiActivity;
import com.shmedo.mcloudapp.maps.util.CoordinateFormatUtils;
import com.shmedo.mcloudapp.maps.util.MapErrorUtil;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 按照经纬度搜索 Poi 点位
 */
public class SearchPoiByLatLngFragment extends BaseFragment implements GeocodeSearch.OnGeocodeSearchListener {

    @BindView(R.id.tv_left_tab)
    TextView tvLeftTab;

    @BindView(R.id.tv_right_tab)
    TextView tvRightTab;

    @BindView(R.id.ll_tab_left_content)
    ViewGroup tabLeftContent;

    @BindView(R.id.ll_tab_rightt_content)
    ViewGroup tabRightContent;

    @BindView(R.id.longitudeET)
    ClearEditText longitudeET;

    @BindView(R.id.latitudeET)
    ClearEditText latitudeET;

    @BindView(R.id.lngDegreeET)
    ClearEditText lngDegreeET;

    @BindView(R.id.lngMinuteET)
    ClearEditText lngMinuteET;

    @BindView(R.id.lngSecondET)
    ClearEditText lngSecondET;

    @BindView(R.id.latDegreeET)
    ClearEditText latDegreeET;

    @BindView(R.id.latMinuteET)
    ClearEditText latMinuteET;

    @BindView(R.id.latSecondET)
    ClearEditText latSecondET;

    @BindView(R.id.spinner_coord)
    Spinner spinnerCoord;

    private static final int DEGREE = 0;//度
    private static final int DEGREE_MINUTE_SECOND = 1;//度分秒

    private int mCurrentMode = DEGREE;//当前定位状态

    private SearchPoiActivity activity;

    private LatLng latLng;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    protected int initContentView() {
        return R.layout.fragment_search_poi_by_lat_lng;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (SearchPoiActivity) getActivity();
        initView();
    }

    private void initView() {
        mCurrentMode = DEGREE;
        switchTab();
        setSpinnerAdapter();
        setListener();
    }

    private void switchTab() {
        if (mCurrentMode == DEGREE) {
            tvLeftTab.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
            tvLeftTab.setBackgroundResource(R.drawable.bg_left_corner_4dp_white);

            tvRightTab.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
            tvRightTab.setBackground(null);
            tabLeftContent.setVisibility(View.VISIBLE);
            tabRightContent.setVisibility(View.GONE);
        } else {
            tvLeftTab.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
            tvLeftTab.setBackground(null);

            tvRightTab.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
            tvRightTab.setBackgroundResource(R.drawable.bg_right_corner_4dp_white);
            tabLeftContent.setVisibility(View.GONE);
            tabRightContent.setVisibility(View.VISIBLE);
        }
    }

    private void setSpinnerAdapter() {
        String[] itemCoords = getResources().getStringArray(R.array.coordinate_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, itemCoords);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCoord.setAdapter(adapter);
    }

    private void setListener() {
        longitudeET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || TextUtils.isEmpty(s.toString())) {
                    return;
                }

                String value = s.toString();
                if (Double.parseDouble(value) > 180) {
                    s.delete(value.length() - 1, value.length());
                    ToastUtils.show("经度值应该小于等于180度!");
                }
            }
        });

        latitudeET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || TextUtils.isEmpty(s.toString())) {
                    return;
                }

                String value = s.toString();
                if (Double.parseDouble(value) > 90) {
                    s.delete(value.length() - 1, value.length());
                    ToastUtils.show("纬度值应该小于等于90度!");
                }
            }
        });

        lngDegreeET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || TextUtils.isEmpty(s.toString())) {
                    return;
                }

                String value = s.toString();
                if (Double.parseDouble(value) >= 180) {
                    s.delete(value.length() - 1, value.length());
                    ToastUtils.show("经度值应该小于180度!");
                }
            }
        });

        lngMinuteET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || TextUtils.isEmpty(s.toString())) {
                    return;
                }

                String value = s.toString();
                if (Double.parseDouble(value) >= 60) {
                    s.delete(value.length() - 1, value.length());
                    ToastUtils.show("分数值应该小于60分!");
                }
            }
        });

        lngSecondET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || TextUtils.isEmpty(s.toString())) {
                    return;
                }

                String value = s.toString();
                if (Double.parseDouble(value) >= 60) {
                    s.delete(value.length() - 1, value.length());
                    ToastUtils.show("秒数值应该小于60秒!");
                }
            }
        });

        latDegreeET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || TextUtils.isEmpty(s.toString())) {
                    return;
                }

                String value = s.toString();
                if (Double.parseDouble(value) >= 90) {
                    s.delete(value.length() - 1, value.length());
                    ToastUtils.show("纬度值应该小于90度!");
                }
            }
        });

        latMinuteET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || TextUtils.isEmpty(s.toString())) {
                    return;
                }

                String value = s.toString();
                if (Double.parseDouble(value) >= 60) {
                    s.delete(value.length() - 1, value.length());
                    ToastUtils.show("分数值应该小于60分!");
                }
            }
        });

        latSecondET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || TextUtils.isEmpty(s.toString())) {
                    return;
                }

                String value = s.toString();
                if (Double.parseDouble(value) >= 60) {
                    s.delete(value.length() - 1, value.length());
                    ToastUtils.show("秒数值应该小于60秒!");
                }
            }
        });
    }


    @OnClick({R.id.back, R.id.tv_left_tab, R.id.tv_right_tab, R.id.btn_confirm})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                activity.finish();
                break;

            case R.id.tv_left_tab:
                mCurrentMode = DEGREE;
                switchTab();
                break;

            case R.id.tv_right_tab:
                mCurrentMode = DEGREE_MINUTE_SECOND;
                switchTab();
                break;

            case R.id.btn_confirm:
                preProcessSearchText();
                break;
        }
    }

    private void preProcessSearchText() {
        double longitude = 0;
        double latitude = 0;

        if (mCurrentMode == DEGREE) {
            String longitudeStr = longitudeET.getText().toString().trim();
            String latitudeStr = latitudeET.getText().toString().trim();

            if (TextUtils.isEmpty(longitudeStr)) {
                ToastUtils.show("请输入经度值!");
                return;
            }

            if (TextUtils.isEmpty(latitudeStr)) {
                ToastUtils.show("请输入纬度值!");
                return;
            }

            try {
                longitude = Double.parseDouble(longitudeStr);
                latitude = Double.parseDouble(latitudeStr);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }

        } else {
            String lngDegreeStr = lngDegreeET.getText().toString().trim();
            String lngMinuteStr = lngMinuteET.getText().toString().trim();
            String lngSecondStr = lngSecondET.getText().toString().trim();

            String latDegreeStr = latDegreeET.getText().toString().trim();
            String latMinuteStr = latMinuteET.getText().toString().trim();
            String latSecondStr = latSecondET.getText().toString().trim();


            if (TextUtils.isEmpty(lngDegreeStr)) {
                ToastUtils.show("请输入经度度数值!");
                return;
            }
            if (TextUtils.isEmpty(lngMinuteStr)) {
                ToastUtils.show("请输入经度分数值!");
                return;
            }
            if (TextUtils.isEmpty(lngSecondStr)) {
                ToastUtils.show("请输入经度秒数值!");
                return;
            }

            if (TextUtils.isEmpty(latDegreeStr)) {
                ToastUtils.show("请输入纬度度数值!");
                return;
            }
            if (TextUtils.isEmpty(latMinuteStr)) {
                ToastUtils.show("请输入纬度分数值!");
                return;
            }
            if (TextUtils.isEmpty(latSecondStr)) {
                ToastUtils.show("请输入纬度秒数值!");
                return;
            }

            String longitudeStr = lngDegreeStr + "°" + lngMinuteStr + "′" + lngSecondStr + "″";
            String latitudeStr = latDegreeStr + "°" + latMinuteStr + "′" + latSecondStr + "″";

            longitudeStr = CoordinateFormatUtils.DmsTurnDD(longitudeStr);
            latitudeStr = CoordinateFormatUtils.DmsTurnDD(latitudeStr);

            try {
                longitude = Double.parseDouble(longitudeStr);
                latitude = Double.parseDouble(latitudeStr);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        }

        latLng = new LatLng(latitude, longitude);
        //坐标系转换
        if (spinnerCoord.getSelectedItem().toString().contains("火星坐标")) {

        } else if (spinnerCoord.getSelectedItem().toString().contains("GPS坐标")) {
            latLng = convert(latLng, CoordinateConverter.CoordType.GPS);
        } else if (spinnerCoord.getSelectedItem().toString().contains("百度坐标")) {
            latLng = convert(latLng, CoordinateConverter.CoordType.BAIDU);
        }

        processSearchText();
    }

    /**
     * 根据类型 转换 坐标
     */
    private LatLng convert(LatLng sourceLatLng, CoordinateConverter.CoordType coord) {
        CoordinateConverter converter = new CoordinateConverter(getActivity());
        // CoordType.GPS 待转换坐标类型
        converter.from(coord);
        // sourceLatLng待转换坐标点
        converter.coord(sourceLatLng);
        // 执行转换操作
        LatLng desLatLng = converter.convert();
        return desLatLng;
    }

    private void processSearchText() {
        LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
        GeocodeSearch geocoderSearch = new GeocodeSearch(getActivity());
        geocoderSearch.setOnGeocodeSearchListener(this);
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 100, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int errorCode) {
        if (errorCode != AMapException.CODE_AMAP_SUCCESS) {
            ToastUtils.show(MapErrorUtil.getErrorMsg(errorCode));
            return;
        }

        if (result != null && result.getRegeocodeAddress() != null && result.getRegeocodeAddress().getFormatAddress() != null) {
            String addressName = result.getRegeocodeAddress().getFormatAddress();

            Intent intent = activity.getIntent();
            intent.putExtra(AppContants.Extras.POI_LATLNG, latLng);
            intent.putExtra(AppContants.Extras.POI_TITLE, addressName);
            activity.setResult(Activity.RESULT_OK, intent);
            activity.finish();
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int errorCode) {

    }
}
