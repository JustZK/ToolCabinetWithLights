package com.zk.cabinet.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.zk.cabinet.R;
import com.zk.cabinet.adapter.ToolsAdapter;
import com.zk.cabinet.bean.Cabinet;
import com.zk.cabinet.bean.Tools;
import com.zk.cabinet.databinding.ActivityAccessDepositBinding;
import com.zk.cabinet.db.CabinetService;
import com.zk.cabinet.db.ToolsService;
import com.zk.cabinet.util.SharedPreferencesUtil;
import com.zk.cabinet.view.TimeOffAppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AccessDepositActivity extends TimeOffAppCompatActivity {
    private ActivityAccessDepositBinding binding;

    private String userTemp;

    private List<Tools> list;
    private ToolsAdapter mAdapter;

    private AlertDialog.Builder openBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_access_deposit);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_access_deposit);
        setSupportActionBar(binding.accessDepositToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        init();
    }

    protected void countDownTimerOnTick(long millisUntilFinished){
        binding.accessDepositCountdownTv.setText(String.valueOf(millisUntilFinished));
    }

    private void init(){
        userTemp = spUtil.getString(SharedPreferencesUtil.Key.UserTemp, "");

        list = ToolsService.getInstance().queryOr(userTemp, 1);
        if (list == null) list = new ArrayList<>();
        mAdapter = new ToolsAdapter(this, list);
        binding.accessDepositQueryLv.setAdapter(mAdapter);
        binding.accessDepositQueryLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (openBuilder == null) {
                    openBuilder = new AlertDialog.Builder(AccessDepositActivity.this);
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
                        bundle.putInt("OperationType", 2);
                        bundle.putBoolean("ImmediatelyOpen", true);
                        IntentActivity(AccessingDepositActivity.class, bundle);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        list = ToolsService.getInstance().queryOr(userTemp, 1);
        if (list == null) list = new ArrayList<>();
        mAdapter.setList(list);
        mAdapter.notifyDataSetChanged();

        super.onActivityResult(requestCode, resultCode, data);
    }
}
