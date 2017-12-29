
package com.baidu.android.voice;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 语音控制面板
 *
 * @author yangliang02
 */
public class ControlPanelFragment extends Fragment {

    private String log = "----->ControlPanelFragment";

    private static final int STATUS_IDLE = 1;

    public static final int STATUS_RECORDING_START = STATUS_IDLE << 1;

    public static final int STATUS_SPEECH_START = STATUS_IDLE << 2;

    public static final int STATUS_SPEECH_END = STATUS_IDLE << 3;

    public static final int STATUS_FINISH = STATUS_IDLE << 4;

    private int mStatus = STATUS_IDLE;

    private WaveView waveView;

    private View mStatusPanel;

    private TextView mStatusView;

    private ProgressBar mVolumeBar;

    /**
     * 取消按钮
     */
    private Button mCancelButton = null;

    /**
     * 完成和重试按钮
     */
    private Button mFinishButton = null;

    private OnEventListener mEventListener;

    private View.OnClickListener mClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mEventListener != null) {
                switch (v.getId()) {
                    case R.id.start:
                        switch (mStatus) {
                            case STATUS_IDLE:
                                // 启动中
                                mStatusView.setVisibility(View.VISIBLE);
                                mStatusView.setText(R.string.starting);
                                setWaveViewVisibility(false);
                                if (mEventListener.onStartListening()) {
                                    mStatusPanel.setVisibility(View.VISIBLE);
                                    mFinishButton.setEnabled(false);
                                }
                                break;
                            case STATUS_SPEECH_START:
                            case STATUS_RECORDING_START:
                                if (mEventListener.onStopListening()) {
                                    setWaveViewVisibility(false);
                                    mFinishButton.setEnabled(false);
                                }
                                break;
                            default:
                                break;
                        }
                        break;
                    case R.id.cancelButton:
                        if (mEventListener.onCancel()) {
                            mCancelButton.setEnabled(false);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    };

    public void setWaveView(WaveView waveView) {
        this.waveView = waveView;
    }

    private void setWaveViewVisibility(boolean visibility) {
        if (visibility) {
            waveView.setVisibility(View.VISIBLE);
        } else {
            waveView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.control_panel_fragment, container, true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View root = getView();
        mStatusPanel = root.findViewById(R.id.status_panel);
        mStatusView = (TextView) root.findViewById(R.id.statusTextView);
        mVolumeBar = (ProgressBar) root.findViewById(R.id.volumeProgressbar);
        mCancelButton = (Button) root.findViewById(R.id.cancelButton);
        mCancelButton.setOnClickListener(mClickListener);
        mFinishButton = (Button) root.findViewById(R.id.start);
        mFinishButton.setOnClickListener(mClickListener);
    }

    public void statusChange(int status) {
        switch (status) {
            case STATUS_RECORDING_START:
                mFinishButton.setEnabled(true);
                //mFinishButton.setText(R.string.tofinish);
                //mFinishButton.setBackgroundResource(R.drawable.pressed);
                mCancelButton.setEnabled(true);
                // 显示提示信息
                // 已启动
                mStatusView.setText(R.string.speaking);
                setWaveViewVisibility(true);
                //mStatusView.setVisibility(View.INVISIBLE);
                // 显示音量的进度条
                mVolumeBar.setVisibility(View.GONE);
                mFinishButton.setBackgroundResource(R.drawable.pressed);
                mStatus = STATUS_RECORDING_START;
                break;
            case STATUS_SPEECH_START:
                mStatusView.setText(R.string.speaking);
                //mFinishButton.setBackgroundResource(R.drawable.trash);
                //mFinishButton.setBackgroundResource(R.drawable.pressed);
                mStatus = STATUS_SPEECH_START;
                break;
            case STATUS_SPEECH_END:
                mStatusView.setText(R.string.in_recog);
                setWaveViewVisibility(false);
                mFinishButton.setEnabled(false);
                mVolumeBar.setVisibility(View.INVISIBLE);
                mFinishButton.setBackgroundResource(R.drawable.normal);
                mStatus = STATUS_SPEECH_END;
                break;
            case STATUS_FINISH:
                setWaveViewVisibility(false);
                mStatusView.setText(null);
                mCancelButton.setEnabled(false);
                mFinishButton.setEnabled(true);
                //mFinishButton.setText(R.string.start);
                mStatusPanel.setVisibility(View.GONE);
                mStatus = STATUS_IDLE;
                mFinishButton.setBackgroundResource(R.drawable.normal);
                break;
            default:
                break;
        }
    }

    public void setOnEventListener(OnEventListener listener) {
        this.mEventListener = listener;
    }

    public void volumeChange(int power) {
        mVolumeBar.setProgress(power);
    }

    /**
     * 面板按钮事件监听
     *
     * @author yangliang02
     */
    public interface OnEventListener {
        /**
         * 开始倾听按钮事件回调
         *
         * @return true成功启动。
         */
        public boolean onStartListening();

        /**
         * 结束倾听按钮事件回调
         *
         * @return true 成功结束
         */
        public boolean onStopListening();

        /**
         * 取消按钮事件
         *
         * @return true 成功取消
         */
        public boolean onCancel();
    }
}
