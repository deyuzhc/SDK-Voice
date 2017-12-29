package com.baidu.android.voice;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.baidu.voicerecognition.android.ui.BaiduASRDigitalDialog;
import com.baidu.voicerecognition.android.ui.DialogRecognitionListener;

import java.util.ArrayList;

/**
 * Created by deyuz on 2017/4/7.
 */
public class Fragment_voice extends Fragment {

    private String log = "----->Fragment_voice";

    private EditText mResult = null;

    private BaiduASRDigitalDialog mDialog = null;

    private DialogRecognitionListener mRecognitionListener;

    private int mCurrentTheme = Config.DIALOG_THEME;

    private Button start;

    private SQLiteDatabase db;  //数据库对象
    private ListView listView;  //列表


    private LinearLayout swipeContainer;

    ArrayList<SwipeButton> swipeButtonArrayList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_voice, container, false);

        mResult = (EditText) view.findViewById(R.id.recognition_text);

        swipeContainer = (LinearLayout) view.findViewById(R.id.itemcontainer);

        swipeButtonArrayList = new ArrayList<>();

        //打开或者创建数据库, 这里是创建数据库
        db = SQLiteDatabase.openOrCreateDatabase(getActivity().getFilesDir().toString() + "/words.db", null);
        System.out.println(getActivity().getFilesDir().toString() + "/news.db");

        //初始化组件
        listView = (ListView) view.findViewById(R.id.lv_words);
        listView.setOnCreateContextMenuListener(getActivity());

        try{


            Cursor cursor = db.rawQuery("select * from words_table order by _id", null);
            inflateListView(cursor);

            swipeContainer.removeAllViews();
            swipeButtonArrayList.clear();



            if(cursor.moveToLast()) {
                SwipeButton swipeButton = new SwipeButton(getContext());
                swipeButton.setText(cursor.getString(1));
                swipeButtonArrayList.add(swipeButton);

                swipeContainer.addView(swipeButton);
                while (cursor.moveToPrevious()) {
                    SwipeButton swipeBut = new SwipeButton(getContext());
                    swipeBut.setText(cursor.getString(1));
                    swipeButtonArrayList.add(swipeBut);

                    swipeContainer.addView(swipeBut);
                }
            }
            //db.execSQL("delete from words_table");
        }catch(SQLiteException exception){
                swipeButtonArrayList.clear();
                swipeContainer.removeAllViews();
                db.execSQL("create table words_table (" +
                        "_id integer primary key autoincrement, " +
                        "item_content varchar(50)) ");


                Cursor cursor = db.rawQuery("select * from words_table order by _id", null);
                inflateListView(cursor);

                //添加到swipebutton
                if(cursor.moveToLast()) {
                    SwipeButton swipeButton = new SwipeButton(getContext());
                    swipeButton.setText(cursor.getString(1));
                    swipeButtonArrayList.add(swipeButton);

                    swipeContainer.addView(swipeButton);
                    while (cursor.moveToPrevious()) {
                        SwipeButton swipeBut = new SwipeButton(getContext());
                        swipeBut.setText(cursor.getString(1));
                        swipeButtonArrayList.add(swipeBut);

                        swipeContainer.addView(swipeBut);
                    }
                }
                //db.execSQL("delete from words_table");

        }


        mResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(log, "#####################");
            }
        });

        //waveView.setA(0);

        start = (Button) view.findViewById(R.id.start_diolog);
        start.setOnClickListener(mOnClick);

        mRecognitionListener = new DialogRecognitionListener() {

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> rs = results != null ? results.getStringArrayList(RESULTS_RECOGNITION) : null;
                if (rs != null && rs.size() > 0) {
                    String recv = rs.get(0);

                    mResult.setText(recv);

                    // 用对话框显示结果
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    final EditText editText = new EditText(getContext());
                    builder.setPositiveButton("保留", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 添加新表项
                            String msg = editText.getText().toString();
                            //加入数据库
                            //insertNews(msg);



                            try{

                                insertData(db, msg);
                                Cursor cursor = db.rawQuery("select * from words_table order by _id", null);
                                inflateListView(cursor);

                                swipeContainer.removeAllViews();
                                swipeButtonArrayList.clear();


                                cursor.moveToLast();
                                SwipeButton swipeButton = new SwipeButton(getContext());
                                swipeButton.setText(cursor.getString(1));
                                swipeButtonArrayList.add(swipeButton);

                                swipeContainer.addView(swipeButton);
                                while(cursor.moveToPrevious()){
                                    SwipeButton swipeBut = new SwipeButton(getContext());
                                    swipeBut.setText(cursor.getString(1));
                                    swipeButtonArrayList.add(swipeBut);

                                    swipeContainer.addView(swipeBut);
                                }
                                //db.execSQL("delete from words_table");
                            }catch(SQLiteException exception){
                                swipeButtonArrayList.clear();
                                swipeContainer.removeAllViews();
                                db.execSQL("create table words_table (" +
                                        "_id integer primary key autoincrement, " +
                                        "item_content varchar(50)) " );

                                insertData(db, msg);
                                Cursor cursor = db.rawQuery("select * from words_table order by _id", null);
                                inflateListView(cursor);

                                //添加到swipebutton
                                cursor.moveToLast();
                                SwipeButton swipeButton = new SwipeButton(getContext());
                                swipeButton.setText(cursor.getString(1));
                                swipeButtonArrayList.add(swipeButton);

                                swipeContainer.addView(swipeButton);
                                while(cursor.moveToPrevious()){
                                    SwipeButton swipeBut = new SwipeButton(getContext());
                                    swipeBut.setText(cursor.getString(1));
                                    swipeButtonArrayList.add(swipeBut);

                                    swipeContainer.addView(swipeBut);
                                }
                                //db.execSQL("delete from words_table");
                            }

                        }
                    });

                    builder.setNegativeButton("丢弃", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.create();
                    builder.setTitle("识别结果");

                    editText.setText(recv);
                    builder.setView(editText);
                    builder.show();
                }

            }


        };

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    View.OnClickListener mOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.start_diolog:
                    mResult.setText(null);

                    mCurrentTheme = Config.DIALOG_THEME;
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                    Bundle params = new Bundle();
                    params.putString(BaiduASRDigitalDialog.PARAM_API_KEY, Constants.API_KEY);
                    params.putString(BaiduASRDigitalDialog.PARAM_SECRET_KEY, Constants.SECRET_KEY);
                    params.putInt(BaiduASRDigitalDialog.PARAM_DIALOG_THEME, Config.DIALOG_THEME);

                    mDialog = new BaiduASRDigitalDialog(getActivity(), params);
                    mDialog.setDialogRecognitionListener(mRecognitionListener);

                    mDialog.getParams().putInt(BaiduASRDigitalDialog.PARAM_PROP, Config.CURRENT_PROP);
                    mDialog.getParams().putString(BaiduASRDigitalDialog.PARAM_LANGUAGE, Config.getCurrentLanguage());
                    Log.e("DEBUG", "Config.PLAY_START_SOUND = " + Config.PLAY_START_SOUND);
                    mDialog.getParams().putBoolean(BaiduASRDigitalDialog.PARAM_START_TONE_ENABLE, Config.PLAY_START_SOUND);
                    mDialog.getParams().putBoolean(BaiduASRDigitalDialog.PARAM_END_TONE_ENABLE, Config.PLAY_END_SOUND);
                    mDialog.getParams().putBoolean(BaiduASRDigitalDialog.PARAM_TIPS_TONE_ENABLE, Config.DIALOG_TIPS_SOUND);
                    mDialog.show();
                    break;
               /* case R.id.more:
                    activity_set = new Intent(getActivity(), DemoListActivity.class);
                    startActivity(activity_set);
                    break;*/
                default:
                    break;
            }
        }
    };

    /*
      * 插入数据到数据库中的触发点击事件
      * 如果数据库存在就能正常访问数据库, 如果不存在访问数据库的时候就会出现 SQLiteException 异常
      * 正常访问 : 获取输入的新闻标题 和 新闻内容, 将标题 和 内容插入到数据库, 重新获取Cursor, 使用Cursor刷新ListView内容
      * 异常访问 : 如果访问出现了SQLiteException异常, 说明数据库不存在, 这时就需要先创建数据库
      */
    public void insertNews(String content) {


        try{
            insertData(db, content);
            Cursor cursor = db.rawQuery("select * from news_table order by _id desc", null);
            inflateListView(cursor);
        }catch(SQLiteException exception){
            db.execSQL("create table words_table (" +
                    "_id integer primary key autoincrement, " +
                    "item_content varchar(50)), " );

            insertData(db, content);
            Cursor cursor = db.rawQuery("select * from words_table order by _id desc", null);
            inflateListView(cursor);
        }

    }

    /*
    * 向数据库中插入数据
    * 参数介绍 :
    * -- 参数① : SQL语句, 在这个语句中使用 ? 作为占位符, 占位符中的内容在后面的字符串中按照顺序进行替换
    * -- 参数② : 替换参数①中占位符中的内容
    */
    private void insertData(SQLiteDatabase db,  String content) {
        db.execSQL("insert into words_table values(null, ?)", new String[]{content});
    }

    /*
    * 刷新数据库列表显示
    * 1. 关联SimpleCursorAdapter与数据库表, 获取数据库表中的最新数据
    * 2. 将最新的SimpleCursorAdapter设置给ListView
    */
    private void inflateListView(Cursor cursor) {
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(
                getContext(),

                R.layout.listview,
                cursor,
                new String[]{ "item_content"},
                new int[]{R.id.content});


        listView.setAdapter(cursorAdapter);
    }




}
