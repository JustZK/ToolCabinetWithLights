package com.zk.cabinet.activity;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.zk.cabinet.R;
import com.zk.cabinet.adapter.UserAdapter;
import com.zk.cabinet.bean.User;
import com.zk.cabinet.databinding.ActivityPersonnelManagementBinding;
import com.zk.cabinet.db.UserService;
import com.zk.cabinet.view.TimeOffAppCompatActivity;

import java.util.List;
import java.util.Objects;

public class PersonnelManagementActivity extends TimeOffAppCompatActivity implements View.OnClickListener {
    private ActivityPersonnelManagementBinding binding;

    private List<User> list;
    private UserAdapter mAdapter;

    private View mDialogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_personnel_management);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_personnel_management);
        binding.setOnClickListener(this);
        setSupportActionBar(binding.personalManagementToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        init();
    }

    private void init(){
        list = UserService.getInstance().loadAll();
        mAdapter = new UserAdapter(this, list);
        binding.personalManagementQueryLv.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.personal_management_add_fab:
                mDialogView = getLayoutInflater().inflate(R.layout.dialog_add_user, null);
                new AlertDialog.Builder(PersonnelManagementActivity.this)
                        .setView(mDialogView)
                        .setTitle(getString(R.string.user_add))
                        .setNegativeButton(getString(R.string.cancel), null)
                        .setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String userName = ((EditText) mDialogView.
                                        findViewById(R.id.dialog_add_user_name_edt)).
                                        getText().toString().trim();
                                String userId = ((EditText) mDialogView.
                                        findViewById(R.id.dialog_add_user_id_edt)).
                                        getText().toString().trim();
                                String userPwd = ((EditText) mDialogView.
                                        findViewById(R.id.dialog_add_user_pwd_edt)).
                                        getText().toString().trim();
                                String userPwdAgain = ((EditText) mDialogView.
                                        findViewById(R.id.dialog_add_user_pwd_again_edt)).
                                        getText().toString().trim();

                                if (!TextUtils.isEmpty(userName) &&
                                        !TextUtils.isEmpty(userId) &&
                                        !TextUtils.isEmpty(userPwd) &&
                                        !TextUtils.isEmpty(userPwdAgain)) {
                                    if (userPwd.equals(userPwdAgain)) {
                                        if (UserService.getInstance().queryByUserID(userId) == null){
                                            User user = new User();
                                            user.setUserName(userName);
                                            user.setUserID(userId);
                                            user.setPassword(userPwd);
                                            UserService.getInstance().insert(user);
                                            list = UserService.getInstance().loadAll();
                                            mAdapter.setList(list);
                                            mAdapter.notifyDataSetChanged();
                                        } else {
                                            showToast("该用户编号已存在！");
                                        }
                                    } else {
                                        showToast("两次密码不一样！");
                                    }
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

    protected void countDownTimerOnTick(long millisUntilFinished){
        binding.personalManagementCountdownTv.setText(String.valueOf(millisUntilFinished));
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
