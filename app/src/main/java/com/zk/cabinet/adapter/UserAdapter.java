package com.zk.cabinet.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zk.cabinet.R;
import com.zk.cabinet.bean.User;

import java.util.List;

/**
 * Created by ZK on 2017/12/13.
 */

public class UserAdapter extends BaseAdapter {
    private List<User> list;
    private Context mContext;
    private LayoutInflater inflater;

    public UserAdapter(Context mContext, List<User> list) {
        this.mContext = mContext;
        this.list = list;
        this.inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return (list == null ? 0 : list.size());
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
        User user = list.get(i);
        if (view == null) {
            view = inflater.inflate(R.layout.adapter_user_item, null);
            viewHolder = new ViewHolder(
                    (LinearLayout) view.findViewById(R.id.adapter_user_item_ll),
                    (TextView) view.findViewById(R.id.adapter_user_name_tv),
                    (TextView) view.findViewById(R.id.adapter_user_id_tv),
                    (TextView) view.findViewById(R.id.adapter_user_finger_tv));
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.adapter_user_name_tv.setText(user.getUserName());
        viewHolder.adapter_user_id_tv.setText(user.getUserID());
        viewHolder.adapter_user_finger_tv.setText(user.getFingerPrint() == null ? "点击录入指纹" : "******");

        if (i % 2 != 0) {
            viewHolder.adapter_user_item_ll.setBackgroundColor(mContext.getResources().getColor(R.color.md_indigo_55));
        } else {
            viewHolder.adapter_user_item_ll.setBackgroundColor(mContext.getResources().getColor(R.color.md_grey_200));
        }

        return view;
    }

    private class ViewHolder {
        protected LinearLayout adapter_user_item_ll;

        protected TextView adapter_user_name_tv;
        protected TextView adapter_user_id_tv;
        protected TextView adapter_user_finger_tv;

        public ViewHolder(LinearLayout adapter_user_item_ll,
                          TextView adapter_user_name_tv,
                          TextView adapter_user_id_tv,
                          TextView adapter_user_finger_tv) {
            this.adapter_user_item_ll = adapter_user_item_ll;
            this.adapter_user_name_tv = adapter_user_name_tv;
            this.adapter_user_id_tv = adapter_user_id_tv;
            this.adapter_user_finger_tv = adapter_user_finger_tv;
        }
    }

    @Nullable
    @Override
    public CharSequence[] getAutofillOptions() {
        return new CharSequence[0];
    }

    public void setList(List<User> list) {
        this.list = list;
    }
}
