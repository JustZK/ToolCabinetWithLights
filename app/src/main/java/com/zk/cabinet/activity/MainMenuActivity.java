package com.zk.cabinet.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.zk.cabinet.R;
import com.zk.cabinet.adapter.ImageTextListAdapter;
import com.zk.cabinet.bean.ImageTextListInfo;
import com.zk.cabinet.databinding.ActivityMainMenuBinding;
import com.zk.cabinet.view.TimeOffAppCompatActivity;

import java.util.ArrayList;
import java.util.Objects;

public class MainMenuActivity extends TimeOffAppCompatActivity {
    private ActivityMainMenuBinding mainMenuBinding;

    private ArrayList<ImageTextListInfo> list;
    private ImageTextListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainMenuBinding = DataBindingUtil.setContentView(this, R.layout.activity_main_menu);
        setSupportActionBar(mainMenuBinding.mainMenuToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        init();
    }

    private void init(){
        list = new ArrayList<>();
        if (getIntent().getIntExtra("UserType", 1) == 1) {
            list.add(new ImageTextListInfo(R.drawable.cabinet, getString(R.string.borrow_by_cabinet)));
            list.add(new ImageTextListInfo(R.drawable.tools, getString(R.string.borrow_by_tool)));
            list.add(new ImageTextListInfo(R.drawable.one_click_return, getString(R.string.one_click_return)));
        } else {
            list.add(new ImageTextListInfo(R.drawable.goods, getString(R.string.tool_management)));
            list.add(new ImageTextListInfo(R.drawable.personnel_management, getString(R.string.personnel_management)));
            list.add(new ImageTextListInfo(R.drawable.system_settings, getString(R.string.system_settings)));
        }

        adapter = new ImageTextListAdapter(MainMenuActivity.this, list);
        mainMenuBinding.mainMenuGv.setAdapter(adapter);
        mainMenuBinding.mainMenuGv.setOnItemClickListener(mOnItemClickListener);
    }

    protected void countDownTimerOnTick(long millisUntilFinished){
        mainMenuBinding.mainMenuCountdownTv.setText(String.valueOf(millisUntilFinished));
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent();
            switch (list.get(i).getTitle()) {
                case R.drawable.cabinet:
                    intent.setClass(MainMenuActivity.this, AccessOutActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                    break;
                case R.drawable.tools:
                    intent.setClass(MainMenuActivity.this, AccessOutByQueryActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                    break;
                case R.drawable.one_click_return:
                    intent.setClass(MainMenuActivity.this, AccessDepositActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                    break;
                case R.drawable.personnel_management:
                    intent.setClass(MainMenuActivity.this, PersonnelManagementActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                    break;
                case R.drawable.goods:
                    intent.setClass(MainMenuActivity.this, ToolsQueryActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                    break;
                case R.drawable.system_settings:
                    intent.setClass(MainMenuActivity.this, SystemSettingsActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                    break;
                default:
                    break;
            }
        }
    };

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
