package com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.littlegreens.netty.client.NettyTcpClient;
import com.littlegreens.netty.client.listener.NettyClientListener;
import com.littlegreens.netty.client.status.ConnectState;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.e40.E40NmeaTimeInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.util.FileProviderUtils;
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

    @BindView(R.id.centerEnableSBtn)
    SwitchButton mSbCenterEnable;

    @BindView(R.id.recyclerView_log)
    RecyclerView mRecyclerView;

    @BindView(R.id.et_ipAddress)
    EditText mEtIpAddress;

    @BindView(R.id.et_ipPort)
    EditText mEtIpPort;

    @BindView(R.id.pause_log)
    Button mBtnPause;

    @BindView(R.id.connect)
    Button mBtnConnect;

    private CommonAdapter mReceAdapter;

    private List<String> logDataList = new ArrayList<>();

    private String deviceSn;

    private E40NmeaTimeInfo nmeaTimeInfo;

    private boolean isPause = false;

    private Handler myHandler = new Handler();

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
        initAdapter();
        setSwitchViewListener();
        initLog4a();
        initTcpClient("114.215.177.133", 7683);//"192.168.31.53", 1088
        connect();
        queryNmeaTimeInfo();
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
        mSbCenterEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    setNmeaTimeInfo(false);
                } else {
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

        if (!checkDataValid(msg))
            return;

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
                    mBtnConnect.setText("断开");
                } else {
                    ToastUtils.show("连接断开!");
                    Timber.e("onServiceStatusConnectChanged:%s", statusCode);
                    mBtnConnect.setText("连接");
                }
            }
        });
    }

    private void connect() {
        if (mNettyTcpClient == null) {
            setupTcpConnect();
            return;
        }
        if (!mNettyTcpClient.getConnectStatus()) {
            mNettyTcpClient.connect();//连接服务器
        } else {
            mNettyTcpClient.disconnect();
        }
    }

    private void disconnect() {
        if (mNettyTcpClient != null)
            mNettyTcpClient.disconnect();
    }

    private boolean getConnectStatus() {
        return mNettyTcpClient != null && mNettyTcpClient.getConnectStatus();
    }

    @OnClick({R.id.connect, R.id.pause_log, R.id.share_log})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.connect) {
            if (!getConnectStatus()) {
                connect();//连接服务器
            } else {
                disconnect();
            }

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

    /**
     * 建立 Tcp 通讯连接
     */
    private void setupTcpConnect() {
        if (TextUtils.isEmpty(mEtIpAddress.getText())) {
            ToastUtils.show("请输入IP地址!");
            mEtIpAddress.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(mEtIpPort.getText())) {
            ToastUtils.show("请输入IP端口号!");
            mEtIpPort.requestFocus();
            return;
        }
        int port = Integer.parseInt(mEtIpPort.getText().toString());
        initTcpClient(mEtIpAddress.getText().toString(), port);
        connect();
    }

    private void shareFile() {
        File log = getLogDir(mActivity);
        File logFile = new File(log, deviceSn + ".txt");
        if (!logFile.exists()) {
            ToastUtils.show("日志文件不存在");
            return;
        }
        Uri contentUri = FileProviderUtils.uriFromFile(mActivity, logFile);

        new Share2.Builder(mActivity)
                .setContentType(ShareContentType.FILE)
                .setShareFileUri(contentUri)
                .setTitle("分享文件")
                .setOnActivityResult(300)
                .build()
                .shareBySystem();
    }

    /**
     * 获取配置参数
     */
    private void queryNmeaTimeInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.E40_MD_GET_NMEA_TIME);
//        showProgressDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    private void setNmeaTimeInfo(boolean isOpen) {
        String command = isOpen ? "$cmd=md_setnmeatime&gga=1" : "$cmd=md_setnmeatime&gga=0";
        showProgressDialog("处理中...");
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
            dismissProgressDialog();
            showDispatchFailedDialog(cmdStr);
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponseRunnable(0);
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
        super.onQueryCmdResponseResultSuccess(queryCmdResult);
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(queryCmdResult.getCmdEngName());
        switch (type) {
            case E40_MD_GET_NMEA_TIME: {
                IOTCommandResult<E40NmeaTimeInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询NMEA参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                nmeaTimeInfo = commandResult.getResult();
                initParamInfo();
            }
            break;

            case E40_MD_SET_NMEA_TIME: {
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

    private void initParamInfo() {
        if (nmeaTimeInfo == null) {
            Timber.e("E40NmeaTimeInfo 为空!");
            nmeaTimeInfo = new E40NmeaTimeInfo();
            return;
        }

        if (nmeaTimeInfo.getGga().equals("1")) {
            mSbCenterEnable.setCheckedImmediatelyNoEvent(true);
        } else {
            mSbCenterEnable.setCheckedImmediatelyNoEvent(false);
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
        myHandler.removeCallbacksAndMessages(null);
        Log4a.flush();
        disconnect();
        super.onDestroy();
    }
}