package com.zk.cabinet.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.zk.cabinet.R;
import com.zk.cabinet.bean.User;
import com.zk.cabinet.callback.CardListener;
import com.zk.cabinet.callback.FingerprintVerifyListener;
import com.zk.cabinet.constant.SelfComm;
import com.zk.cabinet.databinding.ActivityMainBinding;
import com.zk.cabinet.databinding.DilaogLoginBinding;
import com.zk.cabinet.network.NetworkRequest;
import com.zk.cabinet.serial.card.CardSerialOperation;
import com.zk.cabinet.service.BusinessService;
import com.zk.cabinet.util.FingerprintParsingLibrary;
import com.zk.cabinet.util.SharedPreferencesUtil.Key;
import com.zk.cabinet.util.SharedPreferencesUtil.Record;
import com.zk.cabinet.util.SoundPoolUtil;
import com.zk.cabinet.util.TimeOpera;
import com.zk.cabinet.view.TimeOffAppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MainActivity extends TimeOffAppCompatActivity implements View.OnClickListener,
        View.OnLongClickListener {
    private final static int CARD = 0x00;
    private final static int LOGIN_SUCCESS = 0x01;
    private final static int LOGIN_ERROR = 0x02;
    private final static int FINGER_LOGIN_SUCCESS = 0x03;
    private final static int FINGER_LOGIN_ERROR = 0x04;
    private ActivityMainBinding mainBinding;
    private AlertDialog dialogLogin;

    private boolean mEntireInventory = false;
    private ProgressDialog mEntireInventoryDialog;
    private Intent businessServiceIntent;
    private ServiceConnection businessServiceConnection;
    private Messenger businessMessenger;

    private MHandler mHandler;

    private DilaogLoginBinding dilaogLoginBinding;
    private ProgressDialog progressDialog;
    private boolean isLanding = false;

    private void handleMessage(Message msg) {
        switch (msg.what) {
            case CARD:
                if (!isLanding) {
                    isLanding = true;
                    progressDialog.setMessage("已经识别到磁卡，正在联网校对，请稍后......");
                    progressDialog.show();
                    Login(1, null, msg.obj.toString());
                }
                break;
            case LOGIN_SUCCESS:
                progressDialog.dismiss();
                isLanding = false;

                Bundle bundleLogin = msg.getData();
                ArrayList<Record> records = new ArrayList<>();
                records.add(new Record(Key.UserIDTemp, bundleLogin.getString("UserID")));
                records.add(new Record(Key.CodeTemp, bundleLogin.getString("Code")));
                records.add(new Record(Key.NameTemp, bundleLogin.getString("Name")));
                records.add(new Record(Key.MobilePhoneTemp, bundleLogin.getString("MobilePhone")));
                records.add(new Record(Key.CardIDTemp, bundleLogin.getString("CardID")));
                records.add(new Record(Key.UnitNumber, bundleLogin.getString("MechanismCoding")));
                spUtil.applyValue(records);

                FingerprintParsingLibrary.getInstance().setFingerprintVerify(false);
                Intent intent = new Intent();
                intent.putExtra("UserType", 1);
                intent.setClass(MainActivity.this, MainMenuActivity.class);
                startActivityForResult(intent, REQUEST_CODE);

                break;
            case LOGIN_ERROR:
                progressDialog.dismiss();
                isLanding = false;
                showToast(msg.obj.toString());
                break;
            case FINGER_LOGIN_SUCCESS:
                if (!mEntireInventory) {
                    FingerprintParsingLibrary.getInstance().setFingerprintVerify(false);
                    ArrayList<Record> records1 = new ArrayList<>();
                    User user = (User) msg.obj;
                    records1.add(new Record(Key.UserIDTemp, user.getUserID()));
                    records1.add(new Record(Key.CodeTemp, user.getCode()));
                    records1.add(new Record(Key.NameTemp, user.getUserName()));
                    records1.add(new Record(Key.MobilePhoneTemp, user.getMobilePhone()));
                    records1.add(new Record(Key.CardIDTemp, user.getCardID()));
                    records1.add(new Record(Key.UnitNumber, user.getMechanismCoding()));
                    spUtil.applyValue(records1);

                    Intent intent1 = new Intent();
                    intent1.putExtra("UserType", 1);
                    intent1.setClass(MainActivity.this, MainMenuActivity.class);
                    startActivityForResult(intent1, REQUEST_CODE);
                }
                break;
            case FINGER_LOGIN_ERROR:
                showToast(msg.obj.toString());
                break;
            case SelfComm.BUSINESS_ENTIRE_INVENTORY:
                mEntireInventory = (boolean) msg.obj;
                if (mEntireInventory){
                    if (mEntireInventoryDialog == null) {
                        mEntireInventoryDialog = new ProgressDialog(this);
                        mEntireInventoryDialog.setMessage("正在整柜盘点，暂时无法操作...");
                    }
                    mEntireInventoryDialog.show();
                } else {
                    if (mEntireInventoryDialog != null && mEntireInventoryDialog.isShowing()) {
                        mEntireInventoryDialog.dismiss();
                    }
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mainBinding.setOnClickListener(this);
        mHandler = new MHandler(this);

        isFinish = false;//倒计时关闭

        init();

//        JKSDolphinUtil.getInstance().hideNavigation();
    }

    private void init() {

        progressDialog = new ProgressDialog(this);

        CardSerialOperation.getInstance().onCardListener(cardListener);

        FingerprintParsingLibrary.getInstance().init(this);
        FingerprintParsingLibrary.getInstance().onFingerprintVerifyListener(fingerprintVerifyListener);
        FingerprintParsingLibrary.getInstance().setFingerprintVerify(true);

        SoundPoolUtil.getInstance().init(this);

        businessServiceIntent = new Intent(this, BusinessService.class);
        businessServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                businessMessenger = new Messenger(service);
                Message msg = Message.obtain();
                msg.what = SelfComm.BUSINESS_SERVICE_CONNECT;
                msg.replyTo = new Messenger(mHandler);
                try {
                    businessMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }

    private CardListener cardListener = new CardListener() {
        @Override
        public void cardInfo(String cardCode) {
            Message msg = Message.obtain();
            msg.what = CARD;
            msg.obj = cardCode;
            mHandler.sendMessage(msg);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.guide_login_rl:
                showLoginDialog();
//                NettyServerParsingLibrary.getInstance().send(new NettySendInfo(202375248, 0, 0,0));
//                DoorSerialOperation.getInstance().send(new DoorSendInfo(1, 0xff, 8));
                break;
            case R.id.dialog_other_login_dismiss_btn:
                dismissLoginDialog();
                dilaogLoginBinding.dialogOtherLoginAccountEdt.setText(null);
                dilaogLoginBinding.dialogOtherLoginPwdEdt.setText(null);

//                DoorSerialOperation.getInstance().send(new DoorSendInfo(1, 0xff, p));
//                p++;


//                NettyServerParsingLibrary.getInstance().send(new NettySendInfo(201457737, 0, 0,0));
                break;
            case R.id.dialog_other_login_sure_btn:

                String user = dilaogLoginBinding.dialogOtherLoginAccountEdt.getText().toString().trim();
                String pwd = dilaogLoginBinding.dialogOtherLoginPwdEdt.getText().toString().trim();
                if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pwd)) {
                    progressDialog.setMessage("正在联网校对，请稍后......");
                    progressDialog.show();

                    dilaogLoginBinding.dialogOtherLoginAccountEdt.setText(null);
                    dilaogLoginBinding.dialogOtherLoginPwdEdt.setText(null);
                    dismissLoginDialog();

                    Login(0, user, pwd);
//                    User loginUser = UserService.getInstance().queryByUserID(user);
//                    if (loginUser != null){
//                        if (loginUser.getPassword().equals(pwd)){
//
//                            spUtil.applyValue(new SharedPreferencesUtil.Record(Key.UserTemp, loginUser.getUserID()));
//
//                            dilaogLoginBinding.dialogOtherLoginAccountEdt.setText(null);
//                            dilaogLoginBinding.dialogOtherLoginPwdEdt.setText(null);
//                            dismissLoginDialog();
//                            Intent intent = new Intent();
//                            intent.putExtra("UserType", 1);
//                            intent.setClass(MainActivity.this, MainMenuActivity.class);
//                            startActivityForResult(intent, REQUEST_CODE);
//                        } else {
//                            showToast("密码错误！");
//                        }
//                    } else {
//                        showToast("用户不存在！");
//                    }
                } else {
                    showToast("请填写完整！");
                }

                break;
        }
    }

    private void showLoginDialog() {
        if (dialogLogin == null) {
            dialogLogin = new AlertDialog.Builder(MainActivity.this).create();
            dilaogLoginBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dilaog_login, null, false);
            dilaogLoginBinding.setOnClickListener(this);
            dilaogLoginBinding.setOnLongClickListener(this);
            dialogLogin.setView(dilaogLoginBinding.getRoot());
            dialogLogin.setCancelable(false);
        }
        dialogLogin.show();
    }

    private void dismissLoginDialog() {
        if (dialogLogin != null && dialogLogin.isShowing()) dialogLogin.dismiss();
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_other_login_sure_btn:

                String user = dilaogLoginBinding.dialogOtherLoginAccountEdt.getText().toString().trim();
                String pwd = dilaogLoginBinding.dialogOtherLoginPwdEdt.getText().toString().trim();
                if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pwd)) {
                    if (spUtil.getString(Key.Root, "").equals(user) &&
                            spUtil.getString(Key.RootPwd, "").equals(pwd)) {

                        dilaogLoginBinding.dialogOtherLoginAccountEdt.setText(null);
                        dilaogLoginBinding.dialogOtherLoginPwdEdt.setText(null);
                        dismissLoginDialog();

                        FingerprintParsingLibrary.getInstance().setFingerprintVerify(false);
                        Intent intent = new Intent();
                        intent.putExtra("UserType", 0);
                        intent.setClass(MainActivity.this, MainMenuActivity.class);
                        startActivityForResult(intent, REQUEST_CODE);
                    } else {
                        showToast("账户或密码错误！");
                    }
                } else {
                    showToast("请填写完整！");
                }

                break;
        }


        return true;
    }

    @Override
    protected void onDestroy() {
        FingerprintParsingLibrary.getInstance().close();
        SoundPoolUtil.getInstance().shutDown();
        super.onDestroy();
    }

    private static class MHandler extends Handler {
        private final WeakReference<MainActivity> mainActivityWeakReference;

        MHandler(MainActivity mainActivity) {
            super();
            mainActivityWeakReference = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mainActivityWeakReference.get() != null) {
                mainActivityWeakReference.get().handleMessage(msg);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        FingerprintParsingLibrary.getInstance().setFingerprintVerify(true);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private FingerprintVerifyListener fingerprintVerifyListener = new FingerprintVerifyListener() {
        @Override
        public void fingerprintVerify(boolean result, User user) {
            if (result) {
                Message msg = Message.obtain();
                msg.what = FINGER_LOGIN_SUCCESS;
                msg.obj = user;
                mHandler.sendMessage(msg);
            } else {
                Message msg = Message.obtain();
                msg.what = FINGER_LOGIN_ERROR;
                msg.obj = "该指纹不存在。";
                mHandler.sendMessage(msg);
            }
        }
    };

    /**
     * @param loginBy 0: 密码登陆 1：刷卡登陆 3：指纹登陆 4：二维码登陆
     * @param user
     * @param pwd
     */
    private void Login(int loginBy, String user, String pwd) {
        isLanding = true;
        String url = "";
        final JSONObject jsonObject = new JSONObject();
        Long time = TimeOpera.getNowTime();
        try {
            if (loginBy == 0) {
                url = NetworkRequest.getInstance().urlLoginByPwd;

            } else if (loginBy == 1) {
                url = NetworkRequest.getInstance().urlLoginByPwd;

            }
            jsonObject.put("account", user);
            jsonObject.put("password", pwd);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,
                jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonResult) {
                try {
                    JSONObject jsonObject = jsonResult.getJSONObject("Data");
                    if (jsonResult.getInt("Result") == 200 && jsonObject.getBoolean("Result")) {
                        Bundle bundle = new Bundle();
                        bundle.putString("UserID", jsonObject.getString("UserID"));
                        bundle.putString("Code", jsonObject.getString("Code"));
                        bundle.putString("Name", jsonObject.getString("Name"));
                        bundle.putString("MobilePhone", jsonObject.getString("MobilePhone"));
                        bundle.putString("CardID", jsonObject.getString("CardID"));
                        bundle.putString("MechanismCoding", jsonObject.getString("MechanismCoding"));
                        Message msg = Message.obtain();
                        msg.what = LOGIN_SUCCESS;
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                    } else {
                        Message msg = Message.obtain();
                        msg.what = LOGIN_ERROR;
                        msg.obj = "账户或密码不存在。";
                        mHandler.sendMessage(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Message msg = Message.obtain();
                    msg.what = LOGIN_ERROR;
                    msg.obj = "数据解析失败。";
                    mHandler.sendMessage(msg);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Message msg = Message.obtain();
                msg.what = LOGIN_ERROR;
                msg.obj = error.toString();
                mHandler.sendMessage(msg);
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        NetworkRequest.getInstance().add(jsonObjectRequest);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK ){
//            showToast("禁止使用回退建！");
//            return false;
//        }
//        return super.onKeyDown(keyCode, event);
//    }



}
