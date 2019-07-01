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
import com.zk.cabinet.bean.Tools;

import java.util.List;

/**
 * Created by ZK on 2017/12/13.
 */

public class ToolsAdapter extends BaseAdapter {
    private List<Tools> list;
    private Context mContext;
    private LayoutInflater inflater;

    public ToolsAdapter(Context mContext, List<Tools> list) {
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
        Tools tools = list.get(i);
        if (view == null) {
            view = inflater.inflate(R.layout.adapter_tools_item, null);
            viewHolder = new ViewHolder(
                    (LinearLayout) view.findViewById(R.id.adapter_tools_item_ll),
                    (TextView) view.findViewById(R.id.adapter_tools_name_tv),
                    (TextView) view.findViewById(R.id.adapter_tools_cell_number_tv),
                    (TextView) view.findViewById(R.id.adapter_tools_epc_tv),
                    (TextView) view.findViewById(R.id.adapter_tools_state_tv),
                    (TextView) view.findViewById(R.id.adapter_tools_light_tv));
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.adapter_tools_name_tv.setText(tools.getToolName() != null ? tools.getToolName() : "---");
        viewHolder.adapter_tools_cell_number_tv.setText(tools.getToolState() == 0 ? String.valueOf(tools.getCellNumber()) : "---");
        viewHolder.adapter_tools_epc_tv.setText(tools.getEpc() != null ? tools.getEpc() : "---");
        viewHolder.adapter_tools_state_tv.setText(tools.getToolState() == 0 ? "在柜" : "离柜");
        viewHolder.adapter_tools_light_tv.setText(String.valueOf(tools.getToolLightNumber()));

        if (tools.getSelected()){
            viewHolder.adapter_tools_item_ll.setBackgroundColor(mContext.getResources().getColor(R.color.md_orange_900));
        }
        if (i % 2 != 0 && !tools.getSelected()) {
            viewHolder.adapter_tools_item_ll.setBackgroundColor(mContext.getResources().getColor(R.color.md_indigo_55));
        } else if (i % 2 == 0 && !tools.getSelected()) {
            viewHolder.adapter_tools_item_ll.setBackgroundColor(mContext.getResources().getColor(R.color.md_grey_200));
        }

        return view;
    }

    private class ViewHolder {
        protected LinearLayout adapter_tools_item_ll;

        protected TextView adapter_tools_name_tv;
        protected TextView adapter_tools_cell_number_tv;

        protected TextView adapter_tools_epc_tv;
        protected TextView adapter_tools_state_tv;

        protected TextView adapter_tools_light_tv;

        public ViewHolder(LinearLayout adapter_tools_item_ll,
                          TextView adapter_tools_name_tv,
                          TextView adapter_tools_cell_number_tv,
                          TextView adapter_tools_epc_tv,
                          TextView adapter_tools_state_tv,
                          TextView adapter_tools_light_tv) {
            this.adapter_tools_item_ll = adapter_tools_item_ll;
            this.adapter_tools_name_tv = adapter_tools_name_tv;
            this.adapter_tools_cell_number_tv = adapter_tools_cell_number_tv;
            this.adapter_tools_epc_tv = adapter_tools_epc_tv;
            this.adapter_tools_state_tv = adapter_tools_state_tv;
            this.adapter_tools_light_tv = adapter_tools_light_tv;
        }
    }

    @Nullable
    @Override
    public CharSequence[] getAutofillOptions() {
        return new CharSequence[0];
    }

    public void setList(List<Tools> list) {
        this.list = list;
    }
}
