package com.shmedo.mcloudapp.deviceconfig.viewmodels;

import androidx.lifecycle.ViewModel;

import com.blankj.utilcode.util.ThreadUtils;
import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.shmedo.mcloudapp.util.CacheUtil;

import java.util.ArrayList;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/2/16 <br/>
 * 描述：     TODO
 */
public class SearchViewModel extends ViewModel {
    //搜索历史词数据
    private UnPeekLiveData<ArrayList<String>> historyData = new UnPeekLiveData<>();

    public ProtectedUnPeekLiveData<ArrayList<String>> getHistoryData() {
        return historyData;
    }

    public void setHistoryData(ArrayList<String> dataList) {
        this.historyData.setValue(dataList);
    }

    public void requestHistoryData() {
        ThreadUtils.getSinglePool().execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> list = CacheUtil.INSTANCE.getSearchHistoryData();
                historyData.postValue(list);
            }
        });
    }

}
