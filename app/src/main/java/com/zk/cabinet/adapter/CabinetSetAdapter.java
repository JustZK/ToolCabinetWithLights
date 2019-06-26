package com.zk.cabinet.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zk.cabinet.R;
import com.zk.cabinet.bean.Cabinet;
import com.zk.cabinet.util.LogUtil;

import java.util.List;

/**
 * Created by ZK on 2017/12/11.
 */

public class CabinetSetAdapter extends RecyclerView.Adapter<CabinetSetAdapter.ViewHolder> {
    public static final String TAG = "AccessListAdapter";

    private List<Cabinet> list;

    public CabinetSetAdapter(List<Cabinet> list) {
        this.list = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View arg0) {
            super(arg0);
        }

        TextView adapter_cabinet_set_name_tv;
        TextView adapter_cabinet_set_file_number_tv;
        TextView adapter_cabinet_set_tv;
        LinearLayout adapter_cabinet_set_ll;
        TextView adapter_cabinet_set_main_screen;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.adapter_cabinet_set, viewGroup,false);
        ViewHolder viewHolder = new ViewHolder(view);
        LogUtil.getInstance().d("-----------",""+viewGroup.getWidth());

        viewHolder.adapter_cabinet_set_name_tv = view.findViewById(R.id.adapter_cabinet_set_name_tv);
        viewHolder.adapter_cabinet_set_file_number_tv = view.findViewById(R.id.adapter_cabinet_set_file_number_tv);
        viewHolder.adapter_cabinet_set_tv = view.findViewById(R.id.adapter_cabinet_set_tv);
        viewHolder.adapter_cabinet_set_ll = view.findViewById(R.id.adapter_cabinet_set_ll);
        viewHolder.adapter_cabinet_set_main_screen = view.findViewById(R.id.adapter_cabinet_set_main_screen);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
//        viewHolder.adapter_access_item_ll.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
        Cabinet cabinet = list.get(position);
        if (cabinet.getCellNumber() >= 0) {
            viewHolder.adapter_cabinet_set_main_screen.setVisibility(View.GONE);
            viewHolder.adapter_cabinet_set_ll.setVisibility(View.VISIBLE);
            viewHolder.adapter_cabinet_set_ll.setBackgroundResource(R.drawable.border_line);

            viewHolder.adapter_cabinet_set_name_tv.setText(cabinet.getBoxName());
            if (cabinet.getAntennaNumber() != null && !cabinet.getAntennaNumber().equals("") ){
                viewHolder.adapter_cabinet_set_tv.setText("");
            } else {
                viewHolder.adapter_cabinet_set_tv.setText("未设置");
            }
        } else {
            viewHolder.adapter_cabinet_set_main_screen.setVisibility(View.VISIBLE);
            viewHolder.adapter_cabinet_set_ll.setVisibility(View.GONE);
            if (cabinet.getCellNumber() == -2){
                viewHolder.adapter_cabinet_set_main_screen.setText("主\n屏\n幕");
            }else {
                viewHolder.adapter_cabinet_set_main_screen.setText("钥\n匙\n格");
            }
        }


        //如果设置了回调，则设置点击事件
        if (mOnItemClickListener != null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int positionTemp = viewHolder.getAdapterPosition();
                    LogUtil.getInstance().d(TAG, "viewHolder.getAdapterPosition() = " + viewHolder.getAdapterPosition(), false);
                    if (positionTemp != -1) {
                        // 当ViewHolder处于FLAG_REMOVED 等状态时会返回NO_POSITION-1
                        mOnItemClickListener.onItemClick(positionTemp);
                    }
                }
            });

        }

    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }


    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }


}
