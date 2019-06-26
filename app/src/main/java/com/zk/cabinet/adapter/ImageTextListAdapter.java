package com.zk.cabinet.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zk.cabinet.R;
import com.zk.cabinet.bean.ImageTextListInfo;

import java.util.ArrayList;

/**
 * Created by ZK on 2017/12/6.
 */

public class ImageTextListAdapter extends BaseAdapter {
    private ArrayList<ImageTextListInfo> list;
    private Context mContext;
    private LayoutInflater inflater;

    public ImageTextListAdapter(Context mContext, ArrayList<ImageTextListInfo> list){
        this.mContext = mContext;
        this.list = list;
        this.inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        ImageTextListInfo imageTextListInfo = list.get(i);
        if (view == null) {
            view = inflater.inflate(R.layout.adapter_image_text_item, null);
            viewHolder = new ViewHolder((ImageView) view.findViewById(R.id.adapter_image_text_item_iv),
                    (TextView) view.findViewById(R.id.adapter_image_text_item_tv));
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.adapter_image_text_item_tv.setText(imageTextListInfo.getValue());
        viewHolder.adapter_image_text_item_iv.setBackgroundResource(imageTextListInfo.getTitle());

        return view;
    }

    private class ViewHolder {
        protected ImageView adapter_image_text_item_iv;
        protected TextView adapter_image_text_item_tv;

        public ViewHolder(ImageView adapter_image_text_item_iv, TextView adapter_image_text_item_tv) {
            this.adapter_image_text_item_iv = adapter_image_text_item_iv;
            this.adapter_image_text_item_tv = adapter_image_text_item_tv;
        }
    }

    @Nullable
    @Override
    public CharSequence[] getAutofillOptions() {
        return new CharSequence[0];
    }
}
