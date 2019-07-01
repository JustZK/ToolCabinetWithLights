package com.zk.cabinet.activity;

import android.databinding.DataBindingUtil;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.zk.cabinet.R;
import com.zk.cabinet.adapter.CabinetSetAdapter;
import com.zk.cabinet.bean.Cabinet;
import com.zk.cabinet.databinding.ActivityAccessBinding;
import com.zk.cabinet.db.CabinetService;
import com.zk.cabinet.view.TimeOffAppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AccessOutActivity extends TimeOffAppCompatActivity {
    private ActivityAccessBinding binding;
    private List<Cabinet> list;
    private CabinetSetAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_access);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_access);
        setSupportActionBar(binding.accessToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        inti();
    }

    protected void countDownTimerOnTick(long millisUntilFinished){
        binding.accessCountdownTv.setText(String.valueOf(millisUntilFinished));
    }

    private void inti(){
        list = CabinetService.getInstance().loadAll();
        if (list == null) list = new ArrayList<>();
        GridLayoutManager manager = new GridLayoutManager(this, 9, LinearLayoutManager.HORIZONTAL, false);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                Cabinet bean = list.get(position);
                return bean.getProportion();
            }
        });

        binding.accessRv.addItemDecoration(new SpaceItemDecoration());
        binding.accessRv.setLayoutManager(manager);
        binding.accessRv.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new CabinetSetAdapter(list);
        binding.accessRv.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(mOnItemClickListener);
    }

    private CabinetSetAdapter.OnItemClickListener mOnItemClickListener = new CabinetSetAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            if(list.get(position).getCellNumber() > 0){
                if (list.get(position).getAntennaNumber() != null) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("CellNumber", list.get(position).getCellNumber());
                    bundle.putInt("OperationType", 0);
                    bundle.putBoolean("ImmediatelyOpen", false);
                    IntentActivity(AccessingOutActivity.class, bundle);
                }else showToast("该格子未配置，请先配置！");
            }
        }
    };

    public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

            int position = parent.getChildAdapterPosition(view);

            if ((list.get(position).getCellNumber() < 0 ||
                    (list.get(position).getCellNumber() % 10 == 8) ||
                    (list.get(position).getCellNumber() % 10 == 9) ||
                    (list.get(position).getCellNumber() % 10 == 0)) &&
                    (position < (list.size() - 3))){
                outRect.set(5, 0, 30, 0);
            } else {
                outRect.set(5, 0, 5, 0);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
