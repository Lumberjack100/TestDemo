package com.shmedo.mcloudapp.ui.activity.device.sensor.dialog;

import android.app.Dialog;

/**
 * 项目名：  mCloudAPP
 * 包名：    com.shmedo.mcloudapp.dialog
 * 文件名:   IDialogOpt
 * 创建者:   dpc
 * 创建时间:  2019/6/14 09:47
 * 描述：    TODO
 */
public interface IDialogOpt<T> {
      Dialog getDialog();
      void initData(T t);

      T getData();
}
