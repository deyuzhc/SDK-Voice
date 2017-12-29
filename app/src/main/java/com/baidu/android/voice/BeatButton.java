package com.baidu.android.voice;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import java.util.LinkedHashMap;

/**
 * Created by deyuz on 2017/4/9.
 */
public class BeatButton extends LinearLayout {

    public BeatButton(Context context) {
        super(context);
    }

    public BeatButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.api_demo_activity, this);

    }
}
