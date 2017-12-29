
package com.baidu.android.voice;

import com.baidu.voicerecognition.android.ui.BaiduASRDigitalDialog;
import com.baidu.voicerecognition.android.ui.DialogRecognitionListener;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * 识别对话框提示语Demo。通过 {@link BaiduASRDigitalDialog#getParams()}获取到识别参数,调用
 * Bundle.putStringArray({@link BaiduASRDigitalDialog#PARAM_TIPS}, String[])
 * 方法设置提示语列表。设置提示语后，在识别对话框左上角会有“？”按钮，点击后显示提示语列表。设置提示语，还可以通过
 * {@link Bundle#putBoolean(String, boolean)}方法设置以下参数，打开相关功能
 * <table border="1">
 * <tr>
 * <th>参数名</th>
 * <th>参数类型</th>
 * <th>默认值</th>
 * <th>描述</th>
 * </tr>
 * <tr>
 * <td>{@link BaiduASRDigitalDialog#PARAM_SHOW_TIPS_ON_START}</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>对话框显示时不启动识别，展示提示语列表</td>
 * </tr>
 * <tr>
 * <td>{@link BaiduASRDigitalDialog#PARAM_SHOW_TIP}</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>识别启动3秒后未检测到语音，在音效动画下方展示一条提示语</td>
 * </tr>
 * <tr>
 * <td>{@link BaiduASRDigitalDialog#PARAM_SHOW_HELP_ON_SILENT}</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>由于未检测到语音而异常结束时，替换取消按钮为帮助按钮，用户点击后展示提示语列表</td>
 * </tr>
 * </table>
 * 
 * @author yangliang02
 */
public class DialogTipsDemoActivity extends PreferenceActivity {
    private static final String SHOW_TIPS_ONSTART = "dialog_show_tips_onstart";

    private static final String SHOW_TIP_ONSILENT = "dialog_show_tip_onsilent";

    private static final String SHOW_HELP_ONSILENT = "dialog_show_help_onsilent";

    private static final int RECOGNITION_DIALOG = 1;

    private static final String INTENT_ACTION_START = "baidu.voicedemo.intent.action.START";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(getApplication(), R.xml.dialog_tips, false);
        addPreferencesFromResource(R.xml.dialog_tips);
        startRecognition(getIntent());
    }

    private void startRecognition(Intent intent) {
        if (INTENT_ACTION_START.equals(intent.getAction())) {
            showDialog(RECOGNITION_DIALOG);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        startRecognition(intent);
    }

    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {
        if (id == RECOGNITION_DIALOG) {
            Bundle params = new Bundle();
            params.putString(BaiduASRDigitalDialog.PARAM_API_KEY, Constants.API_KEY);
            params.putString(BaiduASRDigitalDialog.PARAM_SECRET_KEY, Constants.SECRET_KEY);
            params.putInt(BaiduASRDigitalDialog.PARAM_DIALOG_THEME, Config.DIALOG_THEME);
            BaiduASRDigitalDialog mDialog = new BaiduASRDigitalDialog(this, params);
            mDialog.setDialogRecognitionListener(new DialogRecognitionListener() {

                @Override
                public void onResults(Bundle arg0) {
                    // TODO Auto-generated method stub

                }
            });
            return mDialog;
        }
        return super.onCreateDialog(id);
    }

    @Override
    @Deprecated
    protected void onPrepareDialog(int id, Dialog dialog) {
        if (id == RECOGNITION_DIALOG) {
            BaiduASRDigitalDialog mDialog = (BaiduASRDigitalDialog) dialog;
            // 设置提示语列表。设置提示语后，在识别对话框左上角会有“？”按钮，点击后显示提示语列表。
            mDialog.getParams().putStringArray(BaiduASRDigitalDialog.PARAM_TIPS,
                    getResources().getStringArray(R.array.command_tips));
            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(getApplication());
            // 设置为True，对话框显示时不启动识别，展示提示语列表
            mDialog.getParams().putBoolean(BaiduASRDigitalDialog.PARAM_SHOW_TIPS_ON_START,
                    preferences.getBoolean(SHOW_TIPS_ONSTART, false));
            // 设置为True，识别启动3秒后未检测到语音，在音效动画下方展示一条提示语
            mDialog.getParams().putBoolean(BaiduASRDigitalDialog.PARAM_SHOW_TIP,
                    preferences.getBoolean(SHOW_TIP_ONSILENT, false));
            // 设置为True，未检测到语音而异常结束时，替换取消按钮为帮助按钮，用户点击后展示提示语列表。
            mDialog.getParams().putBoolean(BaiduASRDigitalDialog.PARAM_SHOW_HELP_ON_SILENT,
                    preferences.getBoolean(SHOW_HELP_ONSILENT, false));
        }
        super.onPrepareDialog(id, dialog);
    }
}
