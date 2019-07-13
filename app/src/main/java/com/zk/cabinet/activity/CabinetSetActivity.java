package com.zk.cabinet.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.zk.cabinet.R;
import com.zk.cabinet.adapter.CabinetSetAdapter;
import com.zk.cabinet.bean.Cabinet;
import com.zk.cabinet.databinding.ActivityCabinetSetBinding;
import com.zk.cabinet.db.CabinetService;
import com.zk.cabinet.util.LogUtil;
import com.zk.cabinet.util.SharedPreferencesUtil;
import com.zk.cabinet.view.TimeOffAppCompatActivity;

import java.util.List;
import java.util.Objects;

public class CabinetSetActivity extends TimeOffAppCompatActivity {
    private final int REQUEST_CODE = 0xFF;
    private ActivityCabinetSetBinding binding;

    private CabinetSetAdapter mAdapter;
    private List<Cabinet> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_cabinet_set);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cabinet_set);
        setSupportActionBar(binding.cabinetSetToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        inti();
    }

    private void inti(){
        list = CabinetService.getInstance().loadAll();
        GridLayoutManager manager;

        if (spUtil.getInt(SharedPreferencesUtil.Key.NumberOfBoxes, 10) != 10 ) {
            manager = new GridLayoutManager(this, 9, LinearLayoutManager.HORIZONTAL, false);
            manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    Cabinet bean = list.get(position);
                    return bean.getProportion();
                }
            });
        } else {
            manager = new GridLayoutManager(this, 2, LinearLayoutManager.HORIZONTAL, false);
            manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    Cabinet bean = list.get(position);
                    return bean.getProportion();
                }
            });
        }

        binding.cabinetSetRv.addItemDecoration(new SpaceItemDecoration());
        binding.cabinetSetRv.setLayoutManager(manager);
        binding.cabinetSetRv.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new CabinetSetAdapter(list);
        binding.cabinetSetRv.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(mOnItemClickListener);
    }

    private CabinetSetAdapter.OnItemClickListener mOnItemClickListener = new CabinetSetAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            if(list.get(position).getCellNumber() > 0){
                Intent intent = new Intent(CabinetSetActivity.this, CabinetSetInfoActivity.class);
                intent.putExtra("CellNumber", list.get(position).getCellNumber());
                startActivityForResult(intent, REQUEST_CODE);
            }
        }
    };

    public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

            int position = parent.getChildAdapterPosition(view);

            if (spUtil.getInt(SharedPreferencesUtil.Key.NumberOfBoxes, 10) != 10 ) {

                if ((list.get(position).getCellNumber() < 0 ||
                        (list.get(position).getCellNumber() % 10 == 8) ||
                        (list.get(position).getCellNumber() % 10 == 9) ||
                        (list.get(position).getCellNumber() % 10 == 0)) &&
                        (position < (list.size() - 3))) {
                    outRect.set(5, 0, 30, 0);
                } else {
                    outRect.set(5, 0, 5, 0);
                }
            } else {
                if (position == 0) {
                    outRect.set(5, 0, 30, 0);
                } else {
                    outRect.set(5, 0, 5, 0);
                }
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                boolean isComplete = true;
                for (Cabinet cabinet : list){
                    if (cabinet.getCellNumber() > 0 && cabinet.getAntennaNumber() == null){
                        isComplete = false;
                        break;
                    }
                }
                if (isComplete) finish();
                else Toast.makeText(this, "请先配置完所有的柜体！", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.getInstance().d("onActivityResult");
        list.clear();
        list.addAll(CabinetService.getInstance().loadAll());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ){
            Toast.makeText(this, "禁止使用回退建！", Toast.LENGTH_SHORT).show();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
