package com.zk.cabinet.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.zk.cabinet.R;
import com.zk.cabinet.callback.CardListener;
import com.zk.cabinet.databinding.ActivityMainBinding;
import com.zk.cabinet.databinding.DilaogLoginBinding;
import com.zk.cabinet.network.NetworkRequest;
import com.zk.cabinet.serial.card.CardSerialOperation;
import com.zk.cabinet.util.SharedPreferencesUtil.Key;
import com.zk.cabinet.util.SharedPreferencesUtil.Record;
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
    private ActivityMainBinding mainBinding;
    private AlertDialog dialogLogin;

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
                records.add(new Record(Key.GenderTemp, bundleLogin.getString("Gender")));
                records.add(new Record(Key.MobilePhoneTemp, bundleLogin.getString("MobilePhone")));
                records.add(new Record(Key.CardIDTemp, bundleLogin.getString("CardID")));
                spUtil.applyValue(records);

                dilaogLoginBinding.dialogOtherLoginAccountEdt.setText(null);
                dilaogLoginBinding.dialogOtherLoginPwdEdt.setText(null);
                dismissLoginDialog();
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
    }

    private void init() {
//        businessServiceIntent = new Intent(this, BusinessService.class);
//        businessServiceConnection = new ServiceConnection() {
//            @Override
//            public void onServiceConnected(ComponentName name, IBinder service) {
//                businessMessenger = new Messenger(service);
////                Messenger messenger = new Messenger(service);
//                Message msg = Message.obtain();
//                msg.what = SelfComm.BUSINESS_SERVICE_CONNECT;
//                msg.replyTo = new Messenger(mHandler);
//                try {
//                    businessMessenger.send(msg);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onServiceDisconnected(ComponentName name) {
//
//            }
//        };

        progressDialog = new ProgressDialog(this);

        CardSerialOperation.getInstance().onCardListener(cardListener);
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

    /**
     * @param loginBy 0: 密码登陆 1：刷卡登陆 3：指纹登陆 4：二维码登陆
     * @param user
     * @param pwd
     */
    private void Login(int loginBy, String user, String pwd) {
        isLanding = true;
        String url = "";
        final JSONObject jsonObject = new JSONObject();
//        JSONObject dataObject = new JSONObject();
        Long time = TimeOpera.getNowTime();
        try {
            if (loginBy == 0) {
                url = NetworkRequest.getInstance().urlLoginByPwd;
//                dataObject.put("code", user);
//                dataObject.put("password", pwd);

            } else if (loginBy == 1) {
                url = NetworkRequest.getInstance().urlLoginByPwd;
//                dataObject.put("cardid", pwd);

            }
            jsonObject.put("account", user);
            jsonObject.put("password", pwd);
//            jsonObject.put("token", EncryptUtil.md5());
//            jsonObject.put("Data", dataObject);
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
                        bundle.putString("Gender", jsonObject.getString("Gender"));
                        bundle.putString("MobilePhone", jsonObject.getString("MobilePhone"));
                        bundle.putString("CardID", jsonObject.getString("CardID"));
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
