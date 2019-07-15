package com.zk.cabinet.activity;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.zk.cabinet.R;
import com.zk.cabinet.adapter.ToolsAdapter;
import com.zk.cabinet.bean.Cabinet;
import com.zk.cabinet.bean.Tools;
import com.zk.cabinet.databinding.ActivityToolsQueryBinding;
import com.zk.cabinet.db.CabinetService;
import com.zk.cabinet.db.ToolsService;
import com.zk.cabinet.util.RegularExpressionUtil;
import com.zk.cabinet.view.TimeOffAppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ToolsQueryActivity extends TimeOffAppCompatActivity implements View.OnClickListener {
    private ActivityToolsQueryBinding binding;

    private List<Tools> list;
    private ToolsAdapter mAdapter;

    private View mDialogView;

    private List<Cabinet> cabinetList;
    private String[] boxName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_tools_query);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tools_query);
        binding.setOnClickListener(this);
        setSupportActionBar(binding.toolToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        init();
    }

    protected void countDownTimerOnTick(long millisUntilFinished) {
        binding.toolCountdownTv.setText(String.valueOf(millisUntilFinished));
    }

    private void init() {
        list = ToolsService.getInstance().loadAll();
        mAdapter = new ToolsAdapter(this, list);
        binding.toolsQueryLv.setAdapter(mAdapter);

        cabinetList = CabinetService.getInstance().loadAll();
        ArrayList<String> boxNameList = new ArrayList<>();
        for (int i = 0; i < cabinetList.size(); i++) {
            if (cabinetList.get(i).getCellNumber() > 0) {
                boxNameList.add(cabinetList.get(i).getBoxName());
            }
        }
        boxName = new String[boxNameList.size()];
        for (int i = 0; i < boxNameList.size(); i++) {
            boxName[i] = boxNameList.get(i);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tools_add_fab:
                mDialogView = getLayoutInflater().inflate(R.layout.dialog_add_tool, null);
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                        R.layout.support_simple_spinner_dropdown_item, boxName);
                ((Spinner) mDialogView.findViewById(R.id.dialog_add_box_name_sp)).setAdapter(spinnerAdapter);
                new AlertDialog.Builder(ToolsQueryActivity.this)
                        .setView(mDialogView)
                        .setTitle(getString(R.string.tools_add))
                        .setNegativeButton(getString(R.string.cancel), null)
                        .setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String epc = ((EditText) mDialogView.
                                        findViewById(R.id.dialog_add_tools_epc_edt)).
                                        getText().toString().trim();
                                String name = ((EditText) mDialogView.
                                        findViewById(R.id.dialog_add_tools_name_edt)).
                                        getText().toString().trim();
                                String toolLightNumber = ((EditText) mDialogView.
                                        findViewById(R.id.dialog_add_tools_light_edt)).
                                        getText().toString().trim();
                                if (!TextUtils.isEmpty(epc) &&
                                        !TextUtils.isEmpty(name) &&
                                        !TextUtils.isEmpty(toolLightNumber) &&
                                        RegularExpressionUtil.isNumber(toolLightNumber)) {

                                    Tools tools = new Tools();

                                    String boxName = ((Spinner) mDialogView.findViewById(R.id.dialog_add_box_name_sp)).getSelectedItem().toString();
                                    for (int p = 3; p < cabinetList.size(); p++) {
                                        if (boxName.equals(cabinetList.get(p).getBoxName())) {
                                            tools.setCellNumber(cabinetList.get(p).getCellNumber());
                                        }
                                    }
                                    tools.setEpc(epc);
                                    tools.setToolName(name);
                                    tools.setToolState(0);
                                    tools.setToolLightNumber(Integer.parseInt(toolLightNumber));
                                    ToolsService.getInstance().insertOrUpdate(tools);
                                    list = ToolsService.getInstance().loadAll();
                                    mAdapter.setList(list);
                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    showToast(getText(R.string.fill_complete));
                                }
                            }
                        })
                        .setCancelable(false)
                        .show();
                break;
        }
    }
}
