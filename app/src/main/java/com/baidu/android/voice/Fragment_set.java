package com.baidu.android.voice;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.baidu.voicerecognition.android.ui.BaiduASRDigitalDialog;

/**
 * Created by deyuz on 2017/4/7.
 */
public class Fragment_set extends Fragment implements CompoundButton.OnCheckedChangeListener {

    private String log = "----->Fragment_set";

    private Spinner propTypeSpinner;

    private Spinner dialogThemeSpinner;

    private Spinner languageSpinner;

    private CheckBox startSoundCheckBox;

    private CheckBox endSoundCheckBox;

    private CheckBox dialogTipsCheckBox;

    private CheckBox showVolCheckBox;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_set, container, false);

        startSoundCheckBox = (CheckBox) view.findViewById(R.id.cb_play_start_sound);
        startSoundCheckBox.setChecked(Config.PLAY_START_SOUND);
        startSoundCheckBox.setOnCheckedChangeListener(this);
        endSoundCheckBox = (CheckBox) view.findViewById(R.id.cb_play_end_sound);
        endSoundCheckBox.setChecked(Config.PLAY_END_SOUND);
        endSoundCheckBox.setOnCheckedChangeListener(this);
        dialogTipsCheckBox = (CheckBox) view.findViewById(R.id.cb_dialog_tips_sound);
        dialogTipsCheckBox.setChecked(Config.DIALOG_TIPS_SOUND);
        dialogTipsCheckBox.setOnCheckedChangeListener(this);
        showVolCheckBox = (CheckBox) view.findViewById(R.id.cb_show_vol);
        showVolCheckBox.setChecked(Config.SHOW_VOL);
        showVolCheckBox.setOnCheckedChangeListener(this);
        propTypeSpinner = (Spinner) view.findViewById(R.id.propType);
        propTypeSpinner.setSelection(Config.getCurrentPropIndex());
        // 垂直领域类型
        propTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Config.setCurrentPropIndex(position);
                TextView tv = (TextView) view;
                //tv.setTextColor(getResources().getColor(R.color.black));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });
        languageSpinner = (Spinner) view.findViewById(R.id.languages);
        languageSpinner.setSelection(Config.getCurrentLanguageIndex());
        // 识别语言
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Config.setCurrentLanguageIndex(position);
                TextView tv = (TextView) view;
                //tv.setTextColor(getResources().getColor(R.color.black));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });

        int selection = 0;
        switch (Config.DIALOG_THEME) {
            case BaiduASRDigitalDialog.THEME_BLUE_DEEPBG:
                selection = 0;
                break;
            case BaiduASRDigitalDialog.THEME_BLUE_LIGHTBG:
                selection = 1;
                break;
            case BaiduASRDigitalDialog.THEME_GREEN_DEEPBG:
                selection = 2;
                break;
            case BaiduASRDigitalDialog.THEME_GREEN_LIGHTBG:
                selection = 3;
                break;
            case BaiduASRDigitalDialog.THEME_ORANGE_DEEPBG:
                selection = 4;
                break;
            case BaiduASRDigitalDialog.THEME_ORANGE_LIGHTBG:
                selection = 5;
                break;
            case BaiduASRDigitalDialog.THEME_RED_DEEPBG:
                selection = 6;
                break;
            case BaiduASRDigitalDialog.THEME_RED_LIGHTBG:
                selection = 7;
                break;

            default:
                break;
        }
        dialogThemeSpinner = (Spinner) view.findViewById(R.id.dialogTheme);
        dialogThemeSpinner.setSelection(selection);
        dialogThemeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                int result = BaiduASRDigitalDialog.THEME_BLUE_DEEPBG;
                switch (position) {
                    case 0:
                        result = BaiduASRDigitalDialog.THEME_BLUE_DEEPBG;
                        break;
                    case 1:
                        result = BaiduASRDigitalDialog.THEME_BLUE_LIGHTBG;
                        break;
                    case 2:
                        result = BaiduASRDigitalDialog.THEME_GREEN_DEEPBG;
                        break;
                    case 3:
                        result = BaiduASRDigitalDialog.THEME_GREEN_LIGHTBG;
                        break;
                    case 4:
                        result = BaiduASRDigitalDialog.THEME_ORANGE_DEEPBG;
                        break;
                    case 5:
                        result = BaiduASRDigitalDialog.THEME_ORANGE_LIGHTBG;
                        break;
                    case 6:
                        result = BaiduASRDigitalDialog.THEME_RED_DEEPBG;
                        break;
                    case 7:
                        result = BaiduASRDigitalDialog.THEME_RED_LIGHTBG;
                        break;

                    default:
                        break;
                }
                Config.DIALOG_THEME = result;

                TextView tv = (TextView) view;
                //tv.setTextColor(getResources().getColor(R.color.black));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });

        return view;

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == showVolCheckBox) {
            Config.SHOW_VOL = isChecked;
        }
        if (buttonView == startSoundCheckBox) {
            Config.PLAY_START_SOUND = isChecked;
        }
        if (buttonView == endSoundCheckBox) {
            Config.PLAY_END_SOUND = isChecked;
        }
        if (buttonView == dialogTipsCheckBox) {
            Config.DIALOG_TIPS_SOUND = isChecked;
        }
    }
}
