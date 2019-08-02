package com.zk.cabinet.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.zk.cabinet.R;
import com.zk.cabinet.adapter.ImageTextListAdapter;
import com.zk.cabinet.bean.ImageTextListInfo;
import com.zk.cabinet.bean.Tools;
import com.zk.cabinet.bean.User;
import com.zk.cabinet.databinding.ActivityMainMenuBinding;
import com.zk.cabinet.db.UserService;
import com.zk.cabinet.network.NetworkRequest;
import com.zk.cabinet.util.FingerprintParsingLibrary;
import com.zk.cabinet.util.LogUtil;
import com.zk.cabinet.util.SharedPreferencesUtil;
import com.zk.cabinet.view.TimeOffAppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainMenuActivity extends TimeOffAppCompatActivity {
    private final static int GET_USER_LIST_SUCCESS = 0x00;
    private final static int GET_USER_LIST_ERROR = 0x01;
    private ActivityMainMenuBinding mainMenuBinding;

    private String mechanismCoding;
    private ProgressDialog progressDialog;

    private ArrayList<ImageTextListInfo> list;
    private ImageTextListAdapter adapter;

    private MHandler mHandler;
    private void handleMessage(Message msg) {
        switch (msg.what) {
            case GET_USER_LIST_SUCCESS:
                progressDialog.dismiss();
                List<User> users = (List<User>) msg.obj;
                UserService.getInstance().insertOrUpdate(users);
                FingerprintParsingLibrary.getInstance().upUserList();
                showToast("人员同步成功");
                break;
            case GET_USER_LIST_ERROR:
                progressDialog.dismiss();
                showToast(msg.obj.toString());
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainMenuBinding = DataBindingUtil.setContentView(this, R.layout.activity_main_menu);
        setSupportActionBar(mainMenuBinding.mainMenuToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mHandler = new MHandler(this);
        progressDialog = new ProgressDialog(this);
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
            list.add(new ImageTextListInfo(R.drawable.personnel_management, getString(R.string.personnel_management)));
            list.add(new ImageTextListInfo(R.drawable.system_settings, getString(R.string.system_settings)));

        }
        list.add(new ImageTextListInfo(R.drawable.synchronize, "人员同步"));
        adapter = new ImageTextListAdapter(MainMenuActivity.this, list);
        mainMenuBinding.mainMenuGv.setAdapter(adapter);
        mainMenuBinding.mainMenuGv.setOnItemClickListener(mOnItemClickListener);

        mechanismCoding = spUtil.getString(SharedPreferencesUtil.Key.UnitNumber, "");
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
                case R.drawable.personnel_management:
                    intent.setClass(MainMenuActivity.this, PersonnelManagementActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                    break;
                case R.drawable.synchronize:
                    progressDialog.setMessage("正在联网获取用户信息......");
                    progressDialog.show();
                    getUserList();
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

    private static class MHandler extends Handler {
        private final WeakReference<MainMenuActivity> mainMenuActivityWeakReference;

        MHandler(MainMenuActivity mainMenuActivity) {
            super();
            mainMenuActivityWeakReference = new WeakReference<>(mainMenuActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mainMenuActivityWeakReference.get() != null) {
                mainMenuActivityWeakReference.get().handleMessage(msg);
            }
        }
    }

    private void getUserList() {
        String url = "";
        JSONObject jsonObject = new JSONObject();
        url = NetworkRequest.getInstance().urlUserList;
        try {
            jsonObject.put("MechanismCoding", mechanismCoding);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        LogUtil.getInstance().d("" + jsonObject);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,
                jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonResult) {
                try {
                    LogUtil.getInstance().d("--  " + jsonResult);
                    JSONArray data = jsonResult.getJSONArray("Data");
                    if (jsonResult.getInt("Result") == 200 && data.length() > 0) {
                        List<User> userList = new ArrayList<>();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject jsonObject = data.getJSONObject(i);
                            User user = new User();
//                            user.setUserID(jsonObject.getString("ID"));
                            user.setUserName(jsonObject.getString("Name"));

                            user.setCode(jsonObject.getString("Code"));
                            user.setMobilePhone(jsonObject.getString("MobilePhone"));
//                            user.setPassword(jsonObject.getString("Password"));
                            user.setFingerPrint(jsonObject.getString("FPI").getBytes());
                            user.setFingerPrintTime(jsonObject.getString("FPITime"));
                            user.setMechanismCoding(mechanismCoding);
                            user.setMechanismName(jsonObject.getString("MechanismName"));
                            userList.add(user);
                        }
                        Message msg = Message.obtain();
                        msg.what = GET_USER_LIST_SUCCESS;
                        msg.obj = userList;
                        mHandler.sendMessage(msg);
                    } else {
                        List<User> userList = new ArrayList<>();
                        Message msg = Message.obtain();
                        msg.what = GET_USER_LIST_SUCCESS;
                        msg.obj = userList;
                        mHandler.sendMessage(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Message msg = Message.obtain();
                    msg.what = GET_USER_LIST_ERROR;
                    msg.obj = "数据解析失败。";
                    mHandler.sendMessage(msg);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Message msg = Message.obtain();
                msg.what = GET_USER_LIST_ERROR;
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

}
