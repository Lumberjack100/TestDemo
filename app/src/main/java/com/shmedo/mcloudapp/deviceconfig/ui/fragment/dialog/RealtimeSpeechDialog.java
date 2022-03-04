package com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.StringUtils;
import com.google.gson.Gson;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.callback.WeakHandler;
import com.shmedo.mcloudapp.deviceconfig.model.VoiceBean;
import com.shmedo.mcloudapp.deviceconfig.view.SoundWaveView;

import java.util.ArrayList;
import java.util.Random;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/3/2 <br/>
 * 描述：     TODO
 */
public class RealtimeSpeechDialog extends BaseDialogFragment {
    @BindView(R.id.tv_msg)
    TextView mTvMsg;

    @BindView(R.id.iv_microphone)
    ImageView mIvMicrophone;

    @BindView(R.id.soundWaveView)
    SoundWaveView soundWaveView;

    private SpeechDialogListener speechDialogListener;

    private Toast mToast;

    // 语音听写对象
    private SpeechRecognizer speechRecognizer;

    private String resultType = "plain";

    private StringBuffer buffer = new StringBuffer();

    private final DefaultHandler mDefaultHandler = new DefaultHandler(this);

    private static final class DefaultHandler extends WeakHandler<RealtimeSpeechDialog> {
        private DefaultHandler(RealtimeSpeechDialog fragment) {
            super(fragment);
        }

        @Override
        protected void handleMessage(Message msg, RealtimeSpeechDialog fragment) {
            fragment.dismiss();
        }
    }

    public static RealtimeSpeechDialog newInstance() {
        RealtimeSpeechDialog fragment = new RealtimeSpeechDialog();
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.realtime_speech_dialog_fragment;
    }

    @Override
    protected void setWindowStyle(int gravity) {
        super.setWindowStyle(gravity);
        this.setCancelable(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initSpeech(getContext());
        buffer.setLength(0);
        startSpeechListening();
    }

    /**
     * 科大讯飞语音听写设置参数
     *
     * @Create by: wjw
     * @Create time: 2020/5/6 9:36
     */
    public void initSpeech(Context context) {
        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        speechRecognizer = SpeechRecognizer.createRecognizer(context, null);
        // 清空参数
        speechRecognizer.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        speechRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置返回结果格式
        speechRecognizer.setParameter(SpeechConstant.RESULT_TYPE, resultType);
        speechRecognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        speechRecognizer.setParameter(SpeechConstant.ACCENT, "mandarin");
        // 设置语音前端点:静音超时时间，单位ms，即用户多长时间不说话则当做超时处理
        //取值范围{1000～10000}
        speechRecognizer.setParameter(SpeechConstant.VAD_BOS, "4000");
        //设置语音后端点:后端点静音检测时间，单位ms，即用户停止说话多长时间内即认为不再输入，
        //自动停止录音，范围{0~10000}
        speechRecognizer.setParameter(SpeechConstant.VAD_EOS, "2000");
        //设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        speechRecognizer.setParameter(SpeechConstant.ASR_PTT, "0");
    }

    private void startSpeechListening() {
        //不显示听写对话框
        int retCode = speechRecognizer.startListening(mRecognizerListener);
        if (retCode != ErrorCode.SUCCESS) {
            showTip("听写失败,错误码：" + retCode + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
        }
    }

    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            Timber.d("RecognizerListener call onBeginOfSpeech");
            updateMsg(StringUtils.getString(R.string.listen_to_speech), false);
            mIvMicrophone.setVisibility(View.GONE);
            soundWaveView.setVisibility(View.VISIBLE);
            soundWaveView.reset();
            Random random = new Random();
            int volume = random.nextInt(10);
            Timber.d("volume value %s", volume);
            soundWaveView.setSoundVolume(volume);
        }

        @Override
        public void onError(SpeechError error) {
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            Timber.d("RecognizerListener call onError %s", error.getPlainDescription(true));
            updateMsg(error.getPlainDescription(true), true);
            speechRecognizer.stopListening();
            mIvMicrophone.setVisibility(View.VISIBLE);
            soundWaveView.setVisibility(View.GONE);
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            Timber.d("RecognizerListener call onEndOfSpeech 结束说话");
        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean isLast) {
            if (resultType.equals("json")) {
                String result = parseVoice(recognizerResult.getResultString());
                buffer.append(result);
            } else if (resultType.equals("plain")) {
                buffer.append(recognizerResult.getResultString());
            }
            if (isLast) {
                Timber.d("RecognizerListener call onResult isLast");
                updateMsg(buffer.toString(), false);
                speechRecognizer.stopListening();
                if (speechDialogListener != null) {
                    speechDialogListener.onResult(buffer.toString(), true);
                }
                mDefaultHandler.sendEmptyMessageDelayed(0, 300);
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
//            Timber.d("RecognizerListener call onVolumeChanged 当前正在说话，音量大小 = " + volume + " 返回音频数据 = " + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    /**
     * 解析语音json
     */
    public String parseVoice(String resultString) {
        Gson gson = new Gson();
        VoiceBean voiceBean = gson.fromJson(resultString, VoiceBean.class);

        StringBuffer sb = new StringBuffer();
        ArrayList<VoiceBean.WSBean> ws = voiceBean.ws;
        for (VoiceBean.WSBean wsBean : ws) {
            String word = wsBean.cw.get(0).w;
            sb.append(word);
        }
        return sb.toString();
    }

    private void showTip(final String str) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(MCloudApp.getContext(), str, Toast.LENGTH_SHORT);
        mToast.show();
    }

    @OnClick({R.id.iv_close, R.id.iv_microphone})
    public void onClick(View view) {
        int id = view.getId();
        if (isDoubleClick(view)) {
            return;
        }
        if (id == R.id.iv_close) {
            speechRecognizer.cancel();
            if (mTvMsg.getText().toString().equals(StringUtils.getString(R.string.listen_to_speech))) {
                updateMsg("取消语音识别", false);
            }
            mDefaultHandler.sendEmptyMessageDelayed(0, 200);
        } else if (id == R.id.iv_microphone) {
            startSpeechListening();
        }
    }

    public void updateMsg(String msg, boolean isError) {
        mTvMsg.setText(msg);
        mTvMsg.setTextColor(isError ? ColorUtils.getColor(R.color.speech_orange) : ColorUtils.getColor(R.color.title_text_color));
    }

    public void setSpeechDialogClickListener(SpeechDialogListener speechDialogListener) {
        this.speechDialogListener = speechDialogListener;
    }

    public interface SpeechDialogListener {
        void onResult(String result, boolean isLast);
    }
}
