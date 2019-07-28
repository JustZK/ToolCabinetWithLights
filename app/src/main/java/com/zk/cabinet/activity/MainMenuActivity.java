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
//            list.add(new ImageTextListInfo(R.drawable.cabinet, getString(R.string.borrow_by_cabinet)));
            list.add(new ImageTextListInfo(R.drawable.document_storage, "证件入库"));
            list.add(new ImageTextListInfo(R.drawable.document_delivery, "证件出库"));
            list.add(new ImageTextListInfo(R.drawable.blood_storage,"血样/尿样入库"));
            list.add(new ImageTextListInfo(R.drawable.blood_delivery, "血样/尿样出库"));
            list.add(new ImageTextListInfo(R.drawable.goods_storage, "收缴物品入库"));
            list.add(new ImageTextListInfo(R.drawable.goods_delivery, "收缴物品出库"));

        } else {
//            list.add(new ImageTextListInfo(R.drawable.goods, getString(R.string.tool_management)));
//            list.add(new ImageTextListInfo(R.drawable.personnel_management, getString(R.string.personnel_management)));
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
                case R.drawable.document_delivery:
                    intent.setClass(MainMenuActivity.this, AccessOutByQueryActivity.class);
                    intent.putExtra("PropertyInvolved", 1);
                    startActivityForResult(intent, REQUEST_CODE);
                    break;
                case R.drawable.document_storage:
                    intent.setClass(MainMenuActivity.this, AccessDepositActivity.class);
                    intent.putExtra("PropertyInvolved", 1);
                    startActivityForResult(intent, REQUEST_CODE);
                    break;
                case R.drawable.blood_delivery:
                    intent.setClass(MainMenuActivity.this, AccessOutByQueryActivity.class);
                    intent.putExtra("PropertyInvolved", 2);
                    startActivityForResult(intent, REQUEST_CODE);
                    break;
                case R.drawable.blood_storage:
                    intent.setClass(MainMenuActivity.this, AccessDepositActivity.class);
                    intent.putExtra("PropertyInvolved", 2);
                    startActivityForResult(intent, REQUEST_CODE);
                    break;
                case R.drawable.goods_delivery:
                    intent.setClass(MainMenuActivity.this, AccessOutByQueryActivity.class);
                    intent.putExtra("PropertyInvolved", 3);
                    startActivityForResult(intent, REQUEST_CODE);
                    break;
                case R.drawable.goods_storage:
                    intent.setClass(MainMenuActivity.this, AccessDepositActivity.class);
                    intent.putExtra("PropertyInvolved", 3);
                    startActivityForResult(intent, REQUEST_CODE);
                    break;
                case R.drawable.one_click_return:
                    intent.setClass(MainMenuActivity.this, AccessDepositActivity.class);
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
