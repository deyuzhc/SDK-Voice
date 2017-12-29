
package com.baidu.android.voice;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CommandsAdapter extends ArrayAdapter<JSONObject> {
    public static final int ITEM_VIEW_TYPE_TEXT = 0;

    public static final int ITEM_VIEW_TYPE_HTML = 1;

    private LayoutInflater mInflater;

    public CommandsAdapter(Context context) {
        super(context, 0);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View temp = convertView;
        if (temp == null) {
            temp = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        TextView text = (TextView) temp.findViewById(android.R.id.text1);
        if (getItemViewType(position) == ITEM_VIEW_TYPE_HTML) {
            text.setText(R.string.view_html);
        } else {
            text.setText(getItem(position).toString());
        }
        return temp;
    }

    @Override
    public int getItemViewType(int position) {
        JSONObject data = getItem(position);
        int type = IGNORE_ITEM_VIEW_TYPE;
        if ("search".equals(data.opt("commandtype"))) {
            type = ITEM_VIEW_TYPE_HTML;
        } else {
            type = ITEM_VIEW_TYPE_TEXT;
        }
        return type;
    }

    public void setData(JSONArray data) {
        clear();
        int size = data == null ? 0 : data.length();
        for (int i = 0; i < size; i++) {
            Log.i("=======================", data.optJSONObject(i).toString());
            add(data.optJSONObject(i));
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }
}
