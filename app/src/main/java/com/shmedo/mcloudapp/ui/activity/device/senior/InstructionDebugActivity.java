package com.shmedo.mcloudapp.ui.activity.device.senior;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.bluetooth.MdBluetoothManager;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.TimeUtil;
import com.shmedo.mcloudapp.util.ToastUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Random;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import jp.bassaer.chatmessageview.models.Message;
import jp.bassaer.chatmessageview.views.ChatView;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device.config
 * 文件名:   InstructionDebugActivity
 * 创建者:   dpc
 * 创建时间:  2019/2/19 13:59
 * 描述：    指令交互调试模式
 */
public class InstructionDebugActivity extends BaseActivity {

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.send_ascii_id)
    RadioButton mSendAsciiId;

    @BindView(R.id.send_hex_id)
    RadioButton mSendHexId;

    @BindView(R.id.send_rg)
    RadioGroup mSendRg;

    @BindView(R.id.receive_ascii_id)
    RadioButton mReceiveAsciiId;

    @BindView(R.id.receive_hex_id)
    RadioButton mReceiveHexId;

    @BindView(R.id.receive_rg)
    RadioGroup mReceiveRg;

    @BindView(R.id.setSysTimeBT)
    Button mSetSysTimeBT;

    @BindView(R.id.getDataBt)
    Button mGetDataBt;

    @BindView(R.id.chat_view)
    ChatView mChatView;

    private MdBluetoothManager mdBluetoothManager;


    //0 ASCII，1 HEX
    public static int send_model = 0;

    public static int receive_model = 0;

    final String myName = "发送";

    final String yourName = "接收";


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, InstructionDebugActivity.class);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_instruction_debug;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        initView();
        initData();
    }


    private void initView() {
        mToolbarTitle.setText("指令交互调试模式");
        mdBluetoothManager = MdBluetoothManager.getInstance();
    }


    private void initData() {
        //Set UI options
        mChatView.setRightBubbleColor(ContextCompat.getColor(this, R.color.orange_FF7502));
        mChatView.setLeftBubbleColor(Color.WHITE);
        mChatView.setBackgroundColor(ContextCompat.getColor(this, R.color.gray200));
        mChatView.setSendButtonColor(ContextCompat.getColor(this, R.color.cyan900));
        mChatView.setSendIcon(R.drawable.ic_action_send);
        mChatView.setRightMessageTextColor(Color.WHITE);
        mChatView.setLeftMessageTextColor(Color.BLACK);
        mChatView.setUsernameTextColor(Color.BLACK);
        mChatView.setSendTimeTextColor(Color.BLACK);
        mChatView.setDateSeparatorColor(Color.BLACK);
        mChatView.setInputTextHint("new message...");

        mChatView.setOnClickSendButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtil.showShortToast("设备已断开连接，暂无法进行指令调试");
                    return;
                }

                sendMessage(mChatView.getInputText().trim());
                sendCommand(mChatView.getInputText().trim() + "\r\n");
                //Reset edit text
                mChatView.setInputText("");
            }
        });

        mSendRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.send_ascii_id:
                        mSendAsciiId.setChecked(true);
                        mSendHexId.setChecked(false);
                        send_model = 0;
                        break;

                    case R.id.send_hex_id:
                        mSendAsciiId.setChecked(false);
                        mSendHexId.setChecked(true);
                        send_model = 1;
                        break;
                }
            }
        });
        mReceiveRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.receive_ascii_id:
                        mReceiveAsciiId.setChecked(true);
                        mReceiveHexId.setChecked(false);
                        receive_model = 0;
                        break;

                    case R.id.receive_hex_id:
                        mReceiveAsciiId.setChecked(false);
                        mReceiveHexId.setChecked(true);
                        receive_model = 1;
                        break;
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String msg) {
        Timber.i("onMessageEvent(), current msg is " + msg);
        //Receive message
        if (receive_model == 1) {
            msg = StringUtil.convertStringToHex(msg);
        }
        final Bitmap yourIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_das);
        final Message receivedMessage = new Message.Builder()
                .setUserName(yourName)
                .setUserIcon(yourIcon)
                .setRightMessage(false)
                .setMessageText(msg)
                .build();

        // This is a demo bot
        // Return within 3 seconds
        int sendDelay = (new Random().nextInt(4) + 1) * 1000;

        MCloudApp.getMainHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mChatView.receive(receivedMessage);
            }
        }, sendDelay);
    }


    @OnClick({R.id.setSysTimeBT, R.id.getDataBt})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.setSysTimeBT:
                setSysTime();
                break;

            case R.id.getDataBt:
                getData();
                break;
        }
    }

    /**
     * 系统授时
     */
    private void setSysTime() {
        if (!MCloudApp.isIsBluetoothDeviceConnected()) {
            ToastUtil.showShortToast("设备已断开连接，无法获取授时");
            return;
        }

        String sysTime = getSysTime();
        sendMessage("##010" + sysTime);
        sendCommand("##010" + sysTime + "\r\n");
        //Reset edit text
        mChatView.setInputText("");
    }

    /**
     * 获取数据
     */
    public void getData() {
        if (!MCloudApp.isIsBluetoothDeviceConnected()) {
            ToastUtil.showShortToast("设备已断开连接，无法获取数据");
            return;
        }

        sendMessage("##110");
        sendCommand("##110\r\n");
        //Reset edit text
        mChatView.setInputText("");
    }

    private void sendMessage(String messageText){
        final Bitmap myIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.logo);
        Message message = new Message.Builder()
                .setUserName(myName)
                .setUserIcon(myIcon)
                .setRightMessage(true)
                .build();

        if (send_model == 0) {
            message.setMessageText(messageText);
        } else {
            message.setMessageText(StringUtil.convertStringToHex(messageText));
        }
        //Set to chat view
        mChatView.send(message);
    }


    private void sendCommand(String cmdStr) {
        com.shmedo.mcloudapp.bluetooth.Message msg =
                new com.shmedo.mcloudapp.bluetooth.Message(UUID.randomUUID().toString(), cmdStr, true);
        if (mdBluetoothManager != null) {
            mdBluetoothManager.writeMessage(msg);
            Timber.d("发送指令===" + cmdStr);
        }
    }


    /**
     * 例如：设置本地时间为2015/5/26 10:22:1
     * 设置举例：##010150526102201
     * 返回信息：$$010150526102201
     *
     * @return
     */
    public String getSysTime() {
        return TimeUtil.getSysTimeStr();
    }
}
