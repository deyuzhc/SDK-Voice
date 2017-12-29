package com.baidu.android.voice;

/**
 * Created by Acer on 2017/4/8.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class SwipeButton extends LinearLayout {

    private Scroller mScroller;
    private RelativeLayout swipeContainer;
    private OnSlideListener mOnSlideListener;
    private int mHolderWidth = 120;
    private static int TAN = 2;
    private int mLastX = 0;
    private int mLastY = 0;
    private String log = "SwipeButton";

    private TextView textView;
    private RelativeLayout swipeLayout;
    private boolean checked = false;
    private boolean state = false;

    private SQLiteDatabase db;  //数据库对象


    public interface OnSlideListener {
        public static final int SLIDE_STATUS_OFF = 0;
        public static final int SLIDE_STATUS_START_SCROLL = 1;
        public static final int SLIDE_STATUS_ON = 2;

        public void onSlide(View view, int status);
    }

    public SwipeButton(Context context) {
        this(context, null);
    }

    public SwipeButton(final Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.swipebutton, this);


        swipeLayout = (RelativeLayout) findViewById(R.id.swipe_container);

        new BitmapWorkerTask(swipeLayout).execute(getResources());


        textView = (TextView) findViewById(R.id.Voice_textView);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                final EditText editText = new EditText(getContext());
                editText.setMinHeight(50);
                builder.setView(editText);
                editText.setText(getText());
                editText.requestFocus();
                InputMethodManager m = (InputMethodManager)editText.getContext().getSystemService(context.INPUT_METHOD_SERVICE) ;
                m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                builder.setTitle("编辑");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String str = editText.getText().toString();

                        setText(str);
                    }
                });

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.create();

                builder.show();
            }
        });

        textView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //checkBox.setVisibility(VISIBLE);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        swipeContainer.setVisibility(GONE);
                        db = SQLiteDatabase.openOrCreateDatabase(getContext().getFilesDir().toString() + "/words.db", null);
                        String text = getText();
                        db.delete("words_table", "item_content=?", new String[]{text});
                        state = true;
                    }
                });

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.create();
                builder.setTitle("提示");
                builder.setMessage("确定删除此项?");
                builder.show();


                return false;
            }
        });

        mScroller = new Scroller(context);
        swipeContainer = (RelativeLayout) findViewById(R.id.swipe_container);
        mHolderWidth = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mHolderWidth, getResources().getDisplayMetrics()));
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public String getText(){
        return textView.getText().toString();
    }
    public boolean isDeleted() {
        return state;
    }

    private AlertDialog.Builder dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create();
        return builder;
    }


    public static int calculateInSampleSize(BitmapFactory.Options options, float reqWidth, float reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / reqHeight);
            } else {
                inSampleSize = Math.round((float) width / reqWidth);
            }
        }
        return inSampleSize;
    }

    class BitmapWorkerTask extends AsyncTask<Resources, Void, Bitmap> {
        private final WeakReference<RelativeLayout> textViewReference;
        private String data = null;

        public BitmapWorkerTask(RelativeLayout textView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            textViewReference = new WeakReference<RelativeLayout>(textView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Resources... params) {
            //return decodeSampledBitmapFromFile(100, 100);
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            //BitmapFactory.decodeFile(filename, options);

            // Calculate inSampleSize
            //options.inSampleSize = calculateInSampleSize(options, 0.00001f, 0.00001f);
            options.inSampleSize = 2;

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeResource(params[0], R.drawable.item, options);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (textViewReference != null && bitmap != null) {
                final RelativeLayout textView = textViewReference.get();
                if (textView != null) {
                    textView.setBackground(new BitmapDrawable(bitmap));
                }
            }
        }
    }


    public void addView(View view) {
        swipeContainer.addView(view);
    }

    public void setOnSlideListener(OnSlideListener onSlideListener) {
        mOnSlideListener = onSlideListener;
    }

    public void shrink() {
        if (getScrollX() != 0) {
            this.smoothScrollTo(0, 0);
        }
    }

    private void smoothScrollTo(int destX, int destY) {
        int scrollX = getScrollX();
        int delta = destX - scrollX;
        mScroller.startScroll(scrollX, 0, delta, 0, Math.abs(delta) * 3);
        invalidate();
    }


    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }


    public void onRequireTouchEvnet(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        int scrollX = getScrollX();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                if (mOnSlideListener != null) {
                    mOnSlideListener.onSlide(this, OnSlideListener.SLIDE_STATUS_START_SCROLL);
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int deltaX = x - mLastX;
                int deltaY = y = mLastY;
                if (Math.abs(deltaX) < Math.abs(deltaY) * TAN) {
                    break;
                }
                int newScrollX = scrollX - deltaX;
                if (deltaX != 0) {
                    if (newScrollX < 0) {
                        newScrollX = 0;
                    } else if (newScrollX > mHolderWidth) {
                        newScrollX = mHolderWidth;
                    }
                    this.scrollTo(newScrollX, 0);
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                int newScrollX = 0;
                if (scrollX - mHolderWidth * 0.75f > 0) {
                    newScrollX = mHolderWidth;
                }
                this.smoothScrollTo(newScrollX, 0);
                if (mOnSlideListener != null) {
                    mOnSlideListener.onSlide(this, newScrollX == 0 ? OnSlideListener.SLIDE_STATUS_OFF : OnSlideListener.SLIDE_STATUS_ON);
                }
                break;
            }
            default: {
                break;
            }
        }
        mLastX = 0;
        mLastY = 0;
    }

}
