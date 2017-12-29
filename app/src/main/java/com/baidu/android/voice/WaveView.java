package com.baidu.android.voice;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by deyuz on 2017/4/6.
 */
public class WaveView extends View {

    int color = Color.parseColor("#1E88E5");
    int precise;
    int N;
    float A;
    int width;
    float speed;

    Paint mPaint;

    float index = (float) Math.PI;
    float uwidth = 0.1f;

    public WaveView(Context context) {
        super(context);
    }

    public WaveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.WaveView, 0, 0);
        N = t.getInteger(R.styleable.WaveView_N, 2);
        A = t.getInteger(R.styleable.WaveView_A, 100);
        width = t.getInteger(R.styleable.WaveView_width, 2);
        color = t.getColor(R.styleable.WaveView_color, color);
        precise = t.getInteger(R.styleable.WaveView_precise, 20);
        speed = t.getFloat(R.styleable.WaveView_speed, 1.0f);
        t.recycle();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(color);
        mPaint.setStrokeWidth(width);
    }

    void setA(float value) {
        A = value;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        index -= uwidth * speed;

        float sw = this.getMeasuredWidth();


        float uw = (float) sw / precise;


        float rela=1.0f;

        for (int i = 0; i < precise; i++) {
            canvas.drawLine(uw * i, A * (float) Math.sin(2 * N * Math.PI * i / precise + index) + rela * A, uw * (i + 1), A * (float) Math.sin(2 * N * Math.PI * (i + 1) / precise + index) + rela * A, mPaint);
        }

        if (index <= -(float) Math.PI) {
            index = (float) Math.PI;
        }
        invalidate();
    }
}
