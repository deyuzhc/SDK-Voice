package com.baidu.android.voice;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.voicerecognition.android.VoiceRecognitionClient;
import com.baidu.voicerecognition.android.VoiceRecognitionConfig;
import com.baidu.voicerecognition.android.ui.BaiduASRDigitalDialog;
import com.baidu.voicerecognition.android.ui.DialogRecognitionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by deyuz on 2017/4/7.
 */
public class Fragment_mean extends Fragment {
    private static final String TAG = "Fragment_mean";

    private static final int RECOGNITION_DIALOG = 1;

    private DialogRecognitionListener mRecognitionListener;

    private ControlPanelFragment mControlPanel;

    private VoiceRecognitionClient mASREngine;

    /**
     * 正在识别中
     */
    private boolean isRecognition = false;

    /**
     * 音量更新间隔
     */
    private static final int POWER_UPDATE_INTERVAL = 100;

    /**
     * 识别回调接口
     */
    private MyVoiceRecogListener mListener = new MyVoiceRecogListener();

    /**
     * 主线程Handler
     */
    private Handler mHandler;

    private CommandsAdapter mCommandsAdapter;

    private ListFragment mCommandsFragment;

    private LinearLayout voiceContainer;

    BaiduASRDigitalDialog mDialog;

    private WaveView waveView;

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

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_mean, container, false);


        waveView = (WaveView) view.findViewById(R.id.view);

        voiceContainer = (LinearLayout) view.findViewById(R.id.VoiceContainer);

        //*******************************
        /*for (int i = 0; i < 100; i++) {
            SwipeButton bt = new SwipeButton(getContext());
            bt.setText(1 + i + "");
            voiceContainer.addView(bt);
        }*/
        //*******************************

        mASREngine = VoiceRecognitionClient.getInstance(getActivity());
        mASREngine.setTokenApis(Constants.API_KEY, Constants.SECRET_KEY);
        mHandler = new Handler();

        mControlPanel = (ControlPanelFragment) (getChildFragmentManager().findFragmentById(R.id.control_panel));

        mControlPanel.setWaveView(waveView);

        mControlPanel.setOnEventListener(new ControlPanelFragment.OnEventListener() {

            @Override
            public boolean onStopListening() {

                waveView.setVisibility(View.GONE);

                mASREngine.speakFinish();
                return true;
            }

            @Override
            public boolean onStartListening() {

                //waveView.setVisibility(View.VISIBLE);

                VoiceRecognitionConfig config = new VoiceRecognitionConfig();
                int prop = Config.CURRENT_PROP;
                // 输入法暂不支持语义解析
                if (prop == VoiceRecognitionConfig.PROP_INPUT) {
                    prop = VoiceRecognitionConfig.PROP_SEARCH;
                }
                config.setProp(prop);
                config.setLanguage(Config.getCurrentLanguage());
                config.enableNLU();
                config.enableVoicePower(Config.SHOW_VOL); // 音量反馈。
                if (Config.PLAY_START_SOUND) {
                    config.enableBeginSoundEffect(R.raw.bdspeech_recognition_start); // 设置识别开始提示音
                }
                if (Config.PLAY_END_SOUND) {
                    config.enableEndSoundEffect(R.raw.bdspeech_speech_end); // 设置识别结束提示音
                }
                config.setSampleRate(VoiceRecognitionConfig.SAMPLE_RATE_8K); // 设置采样率
                // 下面发起识别
                int code = mASREngine.startVoiceRecognition(mListener, config);
                if (code != VoiceRecognitionClient.START_WORK_RESULT_WORKING) {
                    Toast.makeText(getActivity(), getString(R.string.error_start, code),
                            Toast.LENGTH_LONG).show();
                }

                return code == VoiceRecognitionClient.START_WORK_RESULT_WORKING;
            }

            @Override
            public boolean onCancel() {
                mASREngine.stopVoiceRecognition();
                return true;
            }
        });
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mDialog != null) {
            mDialog.dismiss();
        }
        VoiceRecognitionClient.releaseInstance(); // 释放识别库
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onClick(View v) {
        /*switch (v.getId()) {
            case R.id.start_diolog:
                Bundle params = new Bundle();
                params.putInt(BaiduASRDigitalDialog.PARAM_DIALOG_THEME, Config.DIALOG_THEME);
                mDialog = new BaiduASRDigitalDialog(getActivity(), params);
                mRecognitionListener = new DialogRecognitionListener() {

                    @Override
                    public void onResults(Bundle results) {
                        ArrayList<String> rs = results != null ? results
                                .getStringArrayList(RESULTS_RECOGNITION) : null;
                        if (rs != null && rs.size() > 0) {
                            showResourceViewer(rs.get(0));
                        }

                    }
                };
                mDialog.setDialogRecognitionListener(mRecognitionListener);
                int prop = Config.CURRENT_PROP;
                // 输入法暂不支持语义解析
                if (prop == VoiceRecognitionConfig.PROP_INPUT) {
                    prop = VoiceRecognitionConfig.PROP_SEARCH;
                }
                mDialog.getParams().putString(BaiduASRDigitalDialog.PARAM_API_KEY, Constants.API_KEY);
                mDialog.getParams().putString(BaiduASRDigitalDialog.PARAM_SECRET_KEY,
                        Constants.SECRET_KEY);
                mDialog.getParams().putInt(BaiduASRDigitalDialog.PARAM_PROP, prop);
                mDialog.getParams().putBoolean(BaiduASRDigitalDialog.PARAM_NLU_ENABLE, true);
                mDialog.getParams().putString(BaiduASRDigitalDialog.PARAM_LANGUAGE,
                        Config.getCurrentLanguage());
                mDialog.getParams().putBoolean(BaiduASRDigitalDialog.PARAM_START_TONE_ENABLE, Config.PLAY_START_SOUND);
                mDialog.getParams().putBoolean(BaiduASRDigitalDialog.PARAM_END_TONE_ENABLE, Config.PLAY_END_SOUND);
                mDialog.getParams().putBoolean(BaiduASRDigitalDialog.PARAM_TIPS_TONE_ENABLE, Config.DIALOG_TIPS_SOUND);
                mDialog.show();
                Log.i(TAG, "#########################");
                break;
            default:
                break;
        }*/

    }

    /**
     * 将语义结果中的资源单独展示
     *
     * @param result
     */
    private void showResourceViewer(String result) {

        Log.i(TAG, "############" + result);
        String recv = "语音识别结果：\n";
        JSONArray results = null;
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject temp_json = new JSONObject(result);

                String temp_str = temp_json.optString("json_res");
                recv += temp_json.optString("item");

                recv += "\n语义分析结果：\n";

                if (!TextUtils.isEmpty(temp_str)) {
                    temp_json = new JSONObject(temp_str);
                    if (temp_json != null) {
                        // 获取语义结果
                        results = temp_json.optJSONArray("results");
                        recv += results;
                        JSONArray commands = temp_json.optJSONArray("commandlist");
                        // 如果语义结果为空获取资源结果
                        if (results == null || results.length() == 0) {
                            results = commands;

                        } else if (commands != null && commands.length() > 0) {
                            for (int i = 0; i < commands.length(); i++) {
                                results.put(commands.opt(i));
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                Log.w(TAG, e);
            }
        }

        // 用对话框显示结果
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final EditText editText = new EditText(getContext());
        /*builder.setPositiveButton("保留", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.setNegativeButton("丢弃", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });*/
        builder.create();
        builder.setTitle("识别结果");

        editText.setText(recv);
        builder.setView(editText);
        builder.show();
        //showListFragment(results);
        //Log.i(TAG, "!!!!!!!!!!!!!" + results.toString());


        Log.i(TAG, "++++++++++++++++++" + recv);
    }

    private void showListFragment(JSONArray data) {
        if (mCommandsAdapter == null) {
            mCommandsAdapter = new CommandsAdapter(getActivity());
            mCommandsFragment = new ListFragment();
            mCommandsFragment.setListAdapter(mCommandsAdapter);
        } else {
            mCommandsAdapter.clear();
        }
        mCommandsAdapter.setData(data);
        mCommandsAdapter.notifyDataSetChanged();
        getChildFragmentManager().popBackStackImmediate();
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, mCommandsFragment);
        ft.commit();
    }


    /**
     * 重写用于处理语音识别回调的监听器
     */
    class MyVoiceRecogListener implements VoiceRecognitionClient.VoiceClientStatusChangeListener {

        @Override
        public void onClientStatusChange(int status, Object obj) {

            switch (status) {
                // 语音识别实际开始，这是真正开始识别的时间点，需在界面提示用户说话。
                case VoiceRecognitionClient.CLIENT_STATUS_START_RECORDING:
                    isRecognition = true;
                    mHandler.removeCallbacks(mUpdateVolume);
                    mHandler.postDelayed(mUpdateVolume, POWER_UPDATE_INTERVAL);
                    mControlPanel.statusChange(ControlPanelFragment.STATUS_RECORDING_START);
                    //waveView.setVisibility(View.VISIBLE);
                    break;
                case VoiceRecognitionClient.CLIENT_STATUS_SPEECH_START: // 检测到语音起点
                    mControlPanel.statusChange(ControlPanelFragment.STATUS_SPEECH_START);
                    //waveView.setVisibility(View.VISIBLE);
                    break;
                // 已经检测到语音终点，等待网络返回
                case VoiceRecognitionClient.CLIENT_STATUS_SPEECH_END:
                    mControlPanel.statusChange(ControlPanelFragment.STATUS_SPEECH_END);
                    waveView.setVisibility(View.GONE);
                    break;
                // 语音识别完成，显示obj中的结果
                case VoiceRecognitionClient.CLIENT_STATUS_FINISH:
                    waveView.setVisibility(View.GONE);
                    mControlPanel.statusChange(ControlPanelFragment.STATUS_FINISH);
                    isRecognition = false;
                    if (obj != null && obj instanceof List) {
                        List results = (List) obj;
                        if (results.size() > 0) {
                            String recv = results.get(0).toString();

                            showResourceViewer(recv);
                        }
                    }
                    break;
                // 用户取消
                case VoiceRecognitionClient.CLIENT_STATUS_USER_CANCELED:
                    waveView.setVisibility(View.GONE);
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
            mControlPanel.statusChange(ControlPanelFragment.STATUS_FINISH);
        }

        @Override
        public void onNetworkStatusChange(int status, Object obj) {
            // 这里不做任何操作不影响简单识别
        }
    }
}
