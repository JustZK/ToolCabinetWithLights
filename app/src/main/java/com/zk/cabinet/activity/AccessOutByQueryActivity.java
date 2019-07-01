package com.zk.cabinet.activity;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.zk.cabinet.R;
import com.zk.cabinet.adapter.ToolsAdapter;
import com.zk.cabinet.bean.Cabinet;
import com.zk.cabinet.bean.Tools;
import com.zk.cabinet.databinding.ActivityAccessOutByQueryActivityBinding;
import com.zk.cabinet.db.CabinetService;
import com.zk.cabinet.db.ToolsService;
import com.zk.cabinet.view.TimeOffAppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AccessOutByQueryActivity extends TimeOffAppCompatActivity implements View.OnClickListener{
    private ActivityAccessOutByQueryActivityBinding binding;

    private List<Tools> list;
    private ToolsAdapter mAdapter;

    private AlertDialog.Builder openBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_access_out_by_query_activity);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_access_out_by_query_activity);
        binding.setOnClickListener(this);
        setSupportActionBar(binding.accessOutByQueryToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        init();
    }

    protected void countDownTimerOnTick(long millisUntilFinished){
        binding.accessOutByQueryCountdownTv.setText(String.valueOf(millisUntilFinished));
    }

    private void init(){
        list = ToolsService.getInstance().loadAll();
        if (list == null) list = new ArrayList<>();
        mAdapter = new ToolsAdapter(this, list);
        binding.accessOutByQueryQueryLv.setAdapter(mAdapter);
        binding.accessOutByQueryQueryLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (openBuilder == null) {
                    openBuilder = new AlertDialog.Builder(AccessOutByQueryActivity.this);
                }
                final Cabinet cabinetTemp = CabinetService.getInstance().queryEq(list.get(position).getCellNumber());
                openBuilder.setTitle(getString(R.string.title_open_cabinet_where_the_file_is_located));
                openBuilder.setMessage(String.format(getResources().getString(R.string.open_cabinet_where_the_file_is_located),
                        list.get(position).getToolName(), cabinetTemp.getBoxName()));
                openBuilder.setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("CellNumber", cabinetTemp.getCellNumber());
                        bundle.putInt("OperationType", 1);
                        bundle.putString("EPC", list.get(position).getEpc());
                        bundle.putBoolean("ImmediatelyOpen", true);
                        IntentActivity(AccessingOutActivity.class, bundle);
                    }
                });
                openBuilder.setNegativeButton(getString(R.string.cancel), null);
                openBuilder.show();
            }
        });
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.access_out_by_query_btn:
                String search = binding.accessOutByQuerySearchEt.getText().toString().trim();
                if (!TextUtils.isEmpty(search)){
                    list = ToolsService.getInstance().queryOr(search);
                    if (list == null) list = new ArrayList<>();
                    mAdapter.setList(list);
                    mAdapter.notifyDataSetChanged();

                } else {
                    list = ToolsService.getInstance().loadAll();
                    if (list == null) list = new ArrayList<>();
                    mAdapter.setList(list);
                    mAdapter.notifyDataSetChanged();
                }
                break;
        }
    }
}
