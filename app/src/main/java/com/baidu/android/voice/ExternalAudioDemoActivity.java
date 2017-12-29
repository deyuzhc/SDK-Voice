
package com.baidu.android.voice;

import com.baidu.voicerecognition.android.Candidate;
import com.baidu.voicerecognition.android.VoiceRecognitionClient;
import com.baidu.voicerecognition.android.VoiceRecognitionClient.VoiceClientStatusChangeListener;
import com.baidu.voicerecognition.android.VoiceRecognitionConfig;

import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 使用外部音频Demo
 * 
 * @author yangliang02
 */
public class ExternalAudioDemoActivity extends FragmentActivity {
    private ControlPanelFragment mControlPanel;

    private VoiceRecognitionClient mASREngine;

    /** 正在识别中 */
    private boolean isRecognition = false;

    /** 音量更新间隔 */
    private static final int POWER_UPDATE_INTERVAL = 100;

    /** 识别回调接口 */
    private MyVoiceRecogListener mListener = new MyVoiceRecogListener();

    /** 主线程Handler */
    private Handler mHandler;

    private AudioFileThread mAudioRecordThread;

    /**
     * 结果展示
     */
    private EditText mResult = null;

    /**
     * 音量更新任务
     */
    private Runnable mUpdateVolume = new Runnable() {
        public void run() {
            if (isRecognition) {
                long vol = mASREngine.getCurrentDBLevelMeter();
                mControlPanel.volumeChange((int) vol);
                mHandler.removeCallbacks(mUpdateVolume);
                mHandler.postDelayed(mUpdateVolume, POWER_UPDATE_INTERVAL);
            }
        }
    };

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.api_demo_activity);
        mResult = (EditText) findViewById(R.id.recognition_text);
        mASREngine = VoiceRecognitionClient.getInstance(this);
        mASREngine.setTokenApis(Constants.API_KEY, Constants.SECRET_KEY);
        mHandler = new Handler();
        mControlPanel = (ControlPanelFragment) (getSupportFragmentManager()
                .findFragmentById(R.id.control_panel));
        mControlPanel.setOnEventListener(new ControlPanelFragment.OnEventListener() {

            @Override
            public boolean onStopListening() {
                stopRecordThread();
                mASREngine.speakFinish();
                return true;
            }

            @Override
            public boolean onStartListening() {
                VoiceRecognitionConfig config = new VoiceRecognitionConfig();
                config.setProp(Config.CURRENT_PROP);
                config.setLanguage(Config.getCurrentLanguage());
                config.enableVoicePower(Config.SHOW_VOL); // 音量反馈。
                config.setUseDefaultAudioSource(false);
                if (Config.PLAY_START_SOUND) {
                    config.enableBeginSoundEffect(R.raw.bdspeech_recognition_start); // 设置识别开始提示音
                }
                if (Config.PLAY_END_SOUND) {
                    config.enableEndSoundEffect(R.raw.bdspeech_speech_end); // 设置识别结束提示音
                }
                config.setSampleRate(VoiceRecognitionConfig.SAMPLE_RATE_8K); // 设置采样率,需要与外部音频一致
                // 下面发起识别
                int code = mASREngine.startVoiceRecognition(mListener, config);
                if (code != VoiceRecognitionClient.START_WORK_RESULT_WORKING) {
                    Toast.makeText(ExternalAudioDemoActivity.this,
                            getString(R.string.error_start, code), Toast.LENGTH_LONG).show();
                } else {
                    mAudioRecordThread = new AudioFileThread();
                    mAudioRecordThread.start();
                }

                return code == VoiceRecognitionClient.START_WORK_RESULT_WORKING;
            }

            @Override
            public boolean onCancel() {
                stopRecordThread();
                mASREngine.stopVoiceRecognition();
                return true;
            }
        });
    };

    private void stopRecordThread() {
        if (mAudioRecordThread != null) {
            mAudioRecordThread.exit();
            mAudioRecordThread = null;
        }
    }

    /**
     * 重写用于处理语音识别回调的监听器
     */
    class MyVoiceRecogListener implements VoiceClientStatusChangeListener {

        @Override
        public void onClientStatusChange(int status, Object obj) {
            switch (status) {
            // 语音识别实际开始，这是真正开始识别的时间点，需在界面提示用户说话。
                case VoiceRecognitionClient.CLIENT_STATUS_START_RECORDING:
                    isRecognition = true;
                    mHandler.removeCallbacks(mUpdateVolume);
                    mHandler.postDelayed(mUpdateVolume, POWER_UPDATE_INTERVAL);
                    mControlPanel.statusChange(ControlPanelFragment.STATUS_RECORDING_START);
                    break;
                case VoiceRecognitionClient.CLIENT_STATUS_SPEECH_START: // 检测到语音起点
                    mControlPanel.statusChange(ControlPanelFragment.STATUS_SPEECH_START);
                    break;
                // 已经检测到语音终点，等待网络返回
                case VoiceRecognitionClient.CLIENT_STATUS_SPEECH_END:
                    stopRecordThread();
                    mControlPanel.statusChange(ControlPanelFragment.STATUS_SPEECH_END);
                    break;
                // 语音识别完成，显示obj中的结果
                case VoiceRecognitionClient.CLIENT_STATUS_FINISH:
                    stopRecordThread();
                    mControlPanel.statusChange(ControlPanelFragment.STATUS_FINISH);
                    isRecognition = false;
                    updateRecognitionResult(obj);
                    break;
                // 处理连续上屏
                case VoiceRecognitionClient.CLIENT_STATUS_UPDATE_RESULTS:
                    updateRecognitionResult(obj);
                    break;
                // 用户取消
                case VoiceRecognitionClient.CLIENT_STATUS_USER_CANCELED:
                    mControlPanel.statusChange(ControlPanelFragment.STATUS_FINISH);
                    isRecognition = false;
                    break;
                default:
                    break;
            }

        }

        @Override
        public void onError(int errorType, int errorCode) {
            isRecognition = false;
            stopRecordThread();
            mControlPanel.statusChange(ControlPanelFragment.STATUS_FINISH);
        }

        @Override
        public void onNetworkStatusChange(int status, Object obj) {
            // 这里不做任何操作不影响简单识别
        }
    }

    /**
     * 将识别结果更新到UI上，搜索模式结果类型为List<String>,输入模式结果类型为List<List<Candidate>>
     * 
     * @param result
     */
    private void updateRecognitionResult(Object result) {
        if (result != null && result instanceof List) {
            List results = (List) result;
            if (results.size() > 0) {
                if (results.get(0) instanceof List) {
                    List<List<Candidate>> sentences = (List<List<Candidate>>) result;
                    StringBuffer sb = new StringBuffer();
                    for (List<Candidate> candidates : sentences) {
                        if (candidates != null && candidates.size() > 0) {
                            sb.append(candidates.get(0).getWord());
                        }
                    }
                    mResult.setText(sb.toString());
                } else {
                    mResult.setText(results.get(0).toString());
                }
            }
        }
    }

    class AudioFileThread extends Thread {
        private final static String TAG = "AudioFileThread";

        private String mFilePath = "8_8.10.39.54.pcm";

        private volatile boolean mStop = false;

        public void exit() {
            mStop = true;
        }

        @Override
        public void run() {
            Log.d(TAG, " audio thread start mFilePath " + mFilePath);

            InputStream in;
            try {
                in = getAssets().open(mFilePath);
            } catch (IOException e) {
                Log.e(TAG, " e is " + e);
                return;
            }

            int length = 1024;
            byte[] buffer = new byte[length];
            while (!mStop) {
                try {
                    int byteread = in.read(buffer);
                    Log.d(TAG, " byteread: " + byteread);
                    if (byteread != -1) {
                        mASREngine.feedAudioBuffer(buffer, 0, byteread);
                    } else {
                        for (int i = 0; i < length; i++) {
                            buffer[i] = 0;
                        }
                        mASREngine.feedAudioBuffer(buffer, 0, length);
                    }
                } catch (IOException e) {
                    Log.e(TAG, " e is " + e);
                }
            }

            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, " e is " + e);
                }
            }

            Log.d(TAG, " audio thread exit");
        }
    }
}
