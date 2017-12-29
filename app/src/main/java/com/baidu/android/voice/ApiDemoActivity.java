
package com.baidu.android.voice;

import com.baidu.voicerecognition.android.Candidate;
import com.baidu.voicerecognition.android.DataUploader;
import com.baidu.voicerecognition.android.VoiceRecognitionClient;
import com.baidu.voicerecognition.android.VoiceRecognitionClient.VoiceClientStatusChangeListener;
import com.baidu.voicerecognition.android.VoiceRecognitionConfig;

import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.widget.EditText;

import java.util.List;

/**
 * 使用底层API方式识别Demo，开发者可以定义UI交互
 * 
 * @author yangliang02
 */
public class ApiDemoActivity extends FragmentActivity {
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
                mASREngine.speakFinish();
                return true;
            }

            @Override
            public boolean onStartListening() {
                mResult.setText(null);
                VoiceRecognitionConfig config = new VoiceRecognitionConfig();
                config.setProp(Config.CURRENT_PROP);
                config.setLanguage(Config.getCurrentLanguage());
                config.enableVoicePower(Config.SHOW_VOL); // 音量反馈。
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
                    mResult.setText(getString(R.string.error_start, code));
                } 

                return code == VoiceRecognitionClient.START_WORK_RESULT_WORKING;
            }

            @Override
            public boolean onCancel() {
                mASREngine.stopVoiceRecognition();
                return true;
            }
        });
    };

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
                    mControlPanel.statusChange(ControlPanelFragment.STATUS_SPEECH_END);
                    break;
                // 语音识别完成，显示obj中的结果
                case VoiceRecognitionClient.CLIENT_STATUS_FINISH:
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
            mResult.setText(getString(R.string.error_occur, Integer.toHexString(errorCode)));
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
    
    /**
     * 上传通讯录
     * */
    private void uploadContacts(){
    	DataUploader dataUploader = new DataUploader(ApiDemoActivity.this);
    	dataUploader.setApiKey(Constants.API_KEY, Constants.SECRET_KEY);
    	
    	String jsonString = "[{\"name\":\"兆维\", \"frequency\":1}, {\"name\":\"林新汝\", \"frequency\":2}]";
    	try{
    		dataUploader.uploadContactsData(jsonString.getBytes("utf-8"));
    	}catch (Exception e){
    		e.printStackTrace();
    	}
    }
}
