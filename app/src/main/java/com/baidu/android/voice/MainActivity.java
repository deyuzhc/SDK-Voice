
package com.baidu.android.voice;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements OnClickListener {
    private static final String log = "MainActivity";

    private ViewPager pager;
    private ArrayList<Fragment> container;
    private RadioButton left, middle, right;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private TextView textView;
    private static final int FILE_SELECT_CODE = 0;

    /**/

    @Override
    public void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_container);

        left = (RadioButton) findViewById(R.id.left);
        middle = (RadioButton) findViewById(R.id.middle);
        right = (RadioButton) findViewById(R.id.right);

        textView = (TextView) findViewById(R.id.title);


        //LayoutInflater lf = getLayoutInflater().from(MainActivity.this);
        /*View main = lf.inflate(R.layout.activity_voice, null);
        View set = lf.inflate(R.layout.activity_set, null);*/

        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();

        Fragment voice = new Fragment_voice();
        Fragment mean = new Fragment_mean();
        Fragment set = new Fragment_set();
        container = new ArrayList<>();

        container.add(voice);
        container.add(mean);
        container.add(set);

        pager = (ViewPager) findViewById(R.id.viewpager);
        pager.setAdapter(mPageAdapter);
        pager.setOnPageChangeListener(mPageChangeListener);

        /*Button file = (Button) findViewById(R.id.file);
        file.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //openFile("sdcard/Download/0.jpg");
                fileChooser();
            }
        });*/


        /*setContentView(R.layout.activity_voice);
        mResult = (EditText) findViewById(R.id.recognition_text);

        mRecognitionListener = new DialogRecognitionListener() {

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> rs = results != null ? results.getStringArrayList(RESULTS_RECOGNITION) : null;
                if (rs != null && rs.size() > 0) {
                    mResult.setText(rs.get(0));
                }
            }
        };*/
    }




    /*void openFile(String filepath) {
        File file = new File(filepath);
        if (!file.exists()) {
            Toast.makeText(getApplicationContext(), "非", Toast.LENGTH_LONG).show();
        } else {
            Intent intent = getIntent(filepath);
            startActivity(intent);
        }
    }

    Intent getIntent(String param) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "image/*");
        return intent;
    }*/

    void fileChooser() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "选择一个文件"), FILE_SELECT_CODE);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "无文件资源管理器!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    //Log.i(log, "uri ###############" + uri.toString());
                    String path = getPath(getApplicationContext(), uri);
                    Log.i(log, "path ###############" + path);
                }
                break;
        }
    }

    String getPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {

            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    FragmentPagerAdapter mPageAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
        @Override
        public Fragment getItem(int position) {
            return container.get(position);
        }

        @Override
        public int getCount() {
            return container.size();
        }
    };

    ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 0:
                    left.setChecked(true);

                    /*Fragment fragment = new Fragment_voice();
                    transaction.add(R.id.fragment_container, fragment);
                    transaction.commit();*/
                    textView.setText("语音识别");
                    break;
                case 1:
                    middle.setChecked(true);
                    textView.setText("语义识别");
                    break;
                case 2:
                    right.setChecked(true);
                    textView.setText("设置");
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    protected void onDestroy() {
        /*if (mDialog != null) {
            mDialog.dismiss();
        }*/
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        /*switch (v.getId()) {

        }*/

    }
}
