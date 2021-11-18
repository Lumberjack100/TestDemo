package com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.UriUtils;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.littlegreens.netty.client.NettyTcpClient;
import com.littlegreens.netty.client.listener.NettyClientListener;
import com.littlegreens.netty.client.status.ConnectState;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.ServerNumberEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.DataCenterInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.CommonViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import gdut.bsx.share2.Share2;
import gdut.bsx.share2.ShareContentType;
import me.pqpo.librarylog4a.Log4a;
import me.pqpo.librarylog4a.appender.Appender;
import me.pqpo.librarylog4a.appender.FileAppender;
import me.pqpo.librarylog4a.formatter.Formatter;
import me.pqpo.librarylog4a.logger.AppenderLogger;
import timber.log.Timber;

public class NetE40SocketDataDebugFragment extends BaseNetIotCommunicateFragment implements NettyClientListener<String> {
    private static final String TAG = "SocketTestActivity";

    private final int maxPacketLong = 1024 * 60;//设置一次发送数据的最大长度 60K
    //自定义心跳包指令
    private final String heartBeat = "I'm HeartBeatData";
    private NettyTcpClient mNettyTcpClient;

    @BindView(R.id.tv_data_center)
    TextView mTvDataCenter;

    @BindView(R.id.tv_connect_title)
    TextView mTvConnectTitle;

    @BindView(R.id.centerEnableSBtn)
    SwitchButton mSbEnable;

    @BindView(R.id.recyclerView_log)
    RecyclerView mRecyclerView;

    @BindView(R.id.pause_log)
    Button mBtnPause;

    private CommonAdapter mReceAdapter;

    private List<String> logDataList = new ArrayList<>();

    private String deviceSn;

    private boolean isPause = false;

    private Handler myHandler = new Handler();

    List<String> dataCenterNameList = Arrays.asList("数据中心1", "数据中心2", "数据中心3", "数据中心4");
    List<DataCenterInfo> dataCenterInfoList = new ArrayList<>();
    private int dataCenterPos = 0;
    private String ip;
    private int port = 0;

    public static NetE40SocketDataDebugFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetE40SocketDataDebugFragment fragment = new NetE40SocketDataDebugFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.e40_socket_data_debug_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        deviceSn = projectDeviceInfo.getToken();
        dataCenterPos = 0;
        mTvDataCenter.setText(dataCenterNameList.get(0));
        initAdapter();
        setSwitchViewListener();
        initLog4a();

        queryDataCenterInfo(1);
    }

    private void initAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mReceAdapter = new CommonAdapter<String>(mActivity, R.layout.item_log_print, logDataList) {
            @Override
            protected void convert(CommonViewHolder holder, String msg, int position) {
                holder.setText(R.id.tv_log, msg);
            }
        };
        mRecyclerView.setAdapter(mReceAdapter);
    }

    private void setSwitchViewListener() {
        mSbEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    disconnect();
                    setNmeaTimeInfo(false);
                } else {
                    connect();//连接服务器
                    setNmeaTimeInfo(true);
                }
            }
        });
    }

    private void initLog4a() {
        Log4a.release();
        AppenderLogger logger = new AppenderLogger.Builder().create();
        Log4a.setLogger(logger);
        logger.addAppender(createLog4aFileAppender());
    }

    private Appender createLog4aFileAppender() {
        File log = getLogDir(mActivity);
        File cacheFile = new File(log, deviceSn + ".logCache");
        File logFile = new File(log, deviceSn + ".txt");
        cacheFile.delete();
        logFile.delete();
        FileAppender.Builder fileBuild = new FileAppender.Builder(mActivity)
                .setLogFilePath(logFile.getAbsolutePath())
                .setBufferSize(1024 * 400)
                .setFormatter(new Formatter() {
                    @Override
                    public String format(int logLevel, String tag, String msg) {
                        return msg;
                    }
                })
                .setBufferFilePath(cacheFile.getAbsolutePath());
        return fileBuild.create();
    }

    private static File getLogDir(Context context) {
        File log = context.getExternalFilesDir("logs");
        if (log == null) {
            log = new File(context.getFilesDir(), "logs");
        }
        if (!log.exists()) {
            log.mkdir();
        }
        return log;
    }

    private void initTcpClient(String host, int port) {
        mNettyTcpClient = new NettyTcpClient.Builder()
                .setHost(host)    //设置服务端地址
                .setTcpPort(port) //设置服务端端口号
                .setMaxReconnectTimes(3)    //设置最大重连次数
                .setReconnectIntervalTime(5)    //设置重连间隔时间。单位：秒
                .setSendheartBeat(false) //设置是否发送心跳
                .setHeartBeatInterval(30)    //设置心跳间隔时间。单位：秒
                .setHeartBeatData(heartBeat) //设置心跳数据，可以是String类型，也可以是byte[]，以后设置的为准
                .setIndex(0)    //设置客户端标识.(因为可能存在多个tcp连接)
//                .setPacketSeparator("&&")//用特殊字符，作为分隔符，解决粘包问题，默认是用换行符作为分隔符
                .setMaxPacketLong(maxPacketLong)//设置一次发送数据的最大长度，默认是1024
                .build();

        mNettyTcpClient.setListener(this); //设置TCP监听
    }

    /**
     * 当接收到系统消息
     *
     * @param msg   消息
     * @param index tcp 客户端的标识，因为一个应用程序可能有很多个长链接
     */
    @Override
    public void onMessageResponseClient(String msg, int index) {
        Timber.d("onMessageResponseClient data length: %s bytes", msg.getBytes().length);
        //跳过心跳包数据的分发处理
        if (msg.contains(IOTCommandType.HEART_BEAT.toString()))
            return;

        if (!checkDataValid(msg)) {
            Timber.d("checkDataValid 校验失败");
            return;
        }

        if (myHandler == null)
            return;

        msg += "\r\n";
        Log4a.i(TAG, msg);
        if (!isPause) {
            logDataList.add(msg);
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    mReceAdapter.notifyDataSetChanged();
                    mRecyclerView.scrollToPosition(mReceAdapter.getItemCount() - 1);
                }
            });
        }
    }

    /**
     * 连接状态改变事件
     *
     * @param statusCode 状态变化
     * @param index      tcp 客户端的标识，因为一个应用程序可能有很多个长链接
     */
    @Override
    public void onClientStatusConnectChanged(int statusCode, int index) {
        if (myHandler == null)
            return;

        myHandler.post(new Runnable() {
            @Override
            public void run() {
                if (statusCode == ConnectState.STATUS_CONNECT_SUCCESS) {
                    ToastUtils.show("连接成功!");
                    Timber.d("STATUS_CONNECT_SUCCESS:");
                } else {
                    ToastUtils.show("连接断开!");
                    Timber.e("onServiceStatusConnectChanged:%s", statusCode);
                    mSbEnable.setCheckedImmediatelyNoEvent(false);
                }
            }
        });
    }

    private void connect() {
        if (TextUtils.isEmpty(ip)) {
            ToastUtils.show("未获取到IP地址!");
            return;
        }
        initTcpClient(ip, port);
        if (!mNettyTcpClient.getConnectStatus()) {
            mNettyTcpClient.connect();//连接服务器
        } else {
            mNettyTcpClient.disconnect();
        }
    }

    private void disconnect() {
        if (mNettyTcpClient != null && mNettyTcpClient.getConnectStatus()) {
            mNettyTcpClient.disconnect();
        }
    }

    private boolean getConnectStatus() {
        return mNettyTcpClient != null && mNettyTcpClient.getConnectStatus();
    }

    @OnClick({R.id.dataCenterLayout, R.id.pause_log, R.id.share_log})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.dataCenterLayout) {
            showDataCenterNameDialog();
        } else if (id == R.id.pause_log) {
            if (mBtnPause.getText().toString().equals("暂停")) {
                isPause = true;
                mBtnPause.setText("继续");
            } else {
                isPause = false;
                mBtnPause.setText("暂停");
            }
        } else if (id == R.id.share_log) {
            Log4a.release();
            shareFile();
        }
    }

    private void shareFile() {
        File log = getLogDir(mActivity);
        File logFile = new File(log, deviceSn + ".txt");
        if (!logFile.exists()) {
            ToastUtils.show("日志文件不存在");
            return;
        }
        Uri contentUri = UriUtils.file2Uri(logFile);
        new Share2.Builder(mActivity)
                .setContentType(ShareContentType.FILE)
                .setShareFileUri(contentUri)
                .setTitle("分享文件")
                .setOnActivityResult(300)
                .build()
                .shareBySystem();
    }

    private void showDataCenterNameDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", (String[]) dataCenterNameList.toArray(),
                        null, dataCenterPos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                dataCenterPos = position;
                                mTvDataCenter.setText(text);
                                DataCenterInfo dataCenterInfo = dataCenterInfoList.get(position);
                                if (TextUtils.isEmpty(dataCenterInfo.getAddr()) || dataCenterInfo.getPort().equals("0")) {
                                    mSbEnable.setEnabled(false);
                                    mTvConnectTitle.setTextColor(ContextCompat.getColor(mActivity, R.color.sub_title_text_color));
                                } else {
                                    mSbEnable.setEnabled(true);
                                    mTvConnectTitle.setTextColor(ContextCompat.getColor(mActivity, R.color.title_text_color));
                                    ip = dataCenterInfo.getAddr();
                                    port = Integer.parseInt(dataCenterInfo.getPort());
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    /**
     * 获取设备的数据中心参数
     */
    private void queryDataCenterInfo(int number) {
        ServerNumberEntity serverNumberEntity = new ServerNumberEntity(number);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_GET_DATA_CENTER, serverNumberEntity);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 获取配置参数
     */
    private void queryNmeaTimeInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.E40_MD_GET_NMEA_TIME);
//        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    private void setNmeaTimeInfo(boolean isOpen) {
        String command = isOpen ? "$cmd=md_setnmeatime&gga=1" : "$cmd=md_setnmeatime&gga=0";
//        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            dismissWaitDialog();
            showDispatchFailedDialog(cmdStr);
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponse();
        }
    }

    /**
     * 指令下发失败弹框
     */
    private void showDispatchFailedDialog(String cmdStr) {
        ToastUtils.show("下发指令失败");
    }

    /**
     * 查询指令响应结果出错
     *
     * @param errMsg
     */
    @Override
    protected void onQueryCmdResponseResultError(String errMsg) {
        super.onQueryCmdResponseResultError(errMsg);
        ToastUtils.show("指令响应错误");
    }

    /**
     * 查询指令响应结果超时
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultTimeOut(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultTimeOut(queryCmdResult);
        ToastUtils.show("指令响应超时");
    }

    /**
     * 查询指令响应结果成功
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultSuccess(QueryCmdResult queryCmdResult) {
//        super.onQueryCmdResponseResultSuccess(queryCmdResult);
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(queryCmdResult.getCmdEngName());
        switch (type) {
            case MD_GET_DATA_CENTER: {//获取设备的数据中心参数
                IOTCommandResult<DataCenterInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "查询数据中心参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                DataCenterInfo dataCenterInfo = commandResult.getResult();
                dataCenterInfoList.add(dataCenterInfo);

                if (dataCenterInfo.getCenterid().equals("1")) {
                    if (TextUtils.isEmpty(dataCenterInfo.getAddr()) || dataCenterInfo.getPort().equals("0")) {
                        mSbEnable.setEnabled(false);
                        mTvConnectTitle.setTextColor(ContextCompat.getColor(mActivity, R.color.sub_title_text_color));
                    } else {
                        mSbEnable.setEnabled(true);
                        mTvConnectTitle.setTextColor(ContextCompat.getColor(mActivity, R.color.title_text_color));

                        ip = dataCenterInfo.getAddr();
                        port = Integer.parseInt(dataCenterInfo.getPort());
                    }
                    queryDataCenterInfo(2);
                } else if (dataCenterInfo.getCenterid().equals("2")) {
                    queryDataCenterInfo(3);
                } else if (dataCenterInfo.getCenterid().equals("3")) {
                    queryDataCenterInfo(4);
                } else if (dataCenterInfo.getCenterid().equals("4")) {
                    dismissWaitDialog();
                }
            }
            break;

            case E40_MD_SET_NMEA_TIME: {
                dismissWaitDialog();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置NMEA参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
            }
            break;

            default:
                break;
        }
    }

    /**
     * 按照nmea 0183协议校验数据是否有效
     *
     * @param msg
     * @return
     */
    private boolean checkDataValid(String msg) {
        if (!msg.startsWith("$"))
            return false;

        int xor = msg.charAt(1);
        //通过序号索引取得每一个字符
        for (int i = 2; i < msg.length(); i++) {
            if (msg.charAt(i) == '*') {
                break;
            } else {
                xor ^= msg.charAt(i);
            }
        }
        int result = xor % 65536;
        String str = Integer.toString(result, 16);
        String checkStr = msg.substring(msg.lastIndexOf("*") + 1, msg.lastIndexOf("*") + 3);
        if (str.toUpperCase().equals(checkStr))
            return true;
        else
            return false;
    }

    @Override
    public void onDestroy() {
        myHandler = null;
        Log4a.flush();
        disconnect();
        super.onDestroy();
    }
}