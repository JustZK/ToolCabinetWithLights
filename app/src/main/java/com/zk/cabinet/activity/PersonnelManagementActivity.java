package com.zk.cabinet.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;

import com.zk.cabinet.R;
import com.zk.cabinet.adapter.UserAdapter;
import com.zk.cabinet.bean.User;
import com.zk.cabinet.callback.FingerprintListener;
import com.zk.cabinet.databinding.ActivityPersonnelManagementBinding;
import com.zk.cabinet.db.UserService;
import com.zk.cabinet.util.FingerprintParsingLibrary;
import com.zk.cabinet.view.TimeOffAppCompatActivity;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

public class PersonnelManagementActivity extends TimeOffAppCompatActivity implements View.OnClickListener {
    private final static int FINGERPRINT = 0x00;
    private ActivityPersonnelManagementBinding binding;

    private List<User> list;
    private UserAdapter mAdapter;

    private View mDialogView;
    private ProgressDialog fingerDialog;
    private int mPosition;

    private MHandler mHandler;
    private void handleMessage(Message msg) {
        switch (msg.what) {
            case FINGERPRINT:
                if (mPosition != -1) {
                    list.get(mPosition).setFingerPrint((byte[]) msg.obj);
                    UserService.getInstance().update(list.get(mPosition));
                    FingerprintParsingLibrary.getInstance().upUserList();
                    showToast(list.get(mPosition).getUserName() + "您的指纹已录入！");
                    mPosition = -1;
                    if (fingerDialog != null && fingerDialog.isShowing()) fingerDialog.dismiss();
                    mAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_personnel_management);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_personnel_management);
        binding.setOnClickListener(this);
        setSupportActionBar(binding.personalManagementToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mHandler = new MHandler(this);
        init();
    }

    private void init(){
        list = UserService.getInstance().loadAll();
        mAdapter = new UserAdapter(this, list);
        binding.personalManagementQueryLv.setAdapter(mAdapter);
        binding.personalManagementQueryLv.setOnItemClickListener(onItemClickListener);
        FingerprintParsingLibrary.getInstance().onFingerprintListener(fingerprintListener);
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
    protected void onDestroy() {
        FingerprintParsingLibrary.getInstance().onFingerprintListener(null);
        super.onDestroy();
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

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mPosition = position;
            if (fingerDialog == null){
                fingerDialog = new ProgressDialog(PersonnelManagementActivity.this);
                fingerDialog.setTitle("指纹录入");
                fingerDialog.setCancelable(false);
                fingerDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPosition = -1;
                        dialog.dismiss();
                    }
                });
            }
            fingerDialog.setMessage(list.get(position).getUserName() + "请把手指放在指纹传感器上");
            fingerDialog.show();
        }
    };

    private FingerprintListener fingerprintListener = new FingerprintListener() {
        @Override
        public void fingerprint(byte[] fingerprint) {
            if (mPosition != -1) {
                Message msg = Message.obtain();
                msg.what = FINGERPRINT;
                msg.obj = fingerprint;
                mHandler.sendMessage(msg);
            }
        }
    };

    private static class MHandler extends Handler {
        private final WeakReference<PersonnelManagementActivity> personnelManagementActivityWeakReference;

        MHandler(PersonnelManagementActivity personnelManagementActivity) {
            super();
            personnelManagementActivityWeakReference = new WeakReference<>(personnelManagementActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (personnelManagementActivityWeakReference.get() != null) {
                personnelManagementActivityWeakReference.get().handleMessage(msg);
            }
        }
    }
}
