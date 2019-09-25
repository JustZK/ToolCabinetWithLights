package com.zk.cabinet.activity;

import android.app.ProgressDialog;
import android.databinding.DataBindingUtil;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.zk.cabinet.R;
import com.zk.cabinet.adapter.ToolsAdapter;
import com.zk.cabinet.bean.Tools;
import com.zk.cabinet.databinding.ActivityListInLibraryBinding;
import com.zk.cabinet.db.ToolsService;
import com.zk.cabinet.network.NetworkRequest;
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

public class ListInLibraryActivity extends TimeOffAppCompatActivity {
    private final static int GET_IN_BOUND_LIST_SUCCESS = 0x00;
    private final static int GET_IN_BOUND_LIST_ERROR = 0x01;
    private ActivityListInLibraryBinding mBinding;

    private List<Tools> list;
    private ToolsAdapter mAdapter;
    private ProgressDialog progressDialog;
    private MHandler mHandler;
    private void handleMessage(Message msg) {
        switch (msg.what) {
            case GET_IN_BOUND_LIST_SUCCESS:
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                list = (List<Tools>) msg.obj;
                mAdapter.setList(list);
                mAdapter.notifyDataSetChanged();
                break;
            case GET_IN_BOUND_LIST_ERROR:
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                list.clear();
                mAdapter.setList(list);
                mAdapter.notifyDataSetChanged();
                showToast(msg.obj.toString());
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_list_in_library);
        setSupportActionBar(mBinding.listInLibraryToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mHandler = new MHandler(this);
        list = new ArrayList<>();
        mAdapter = new ToolsAdapter(this, list);
        mBinding.listInLibraryLv.setAdapter(mAdapter);
        getInBoundList();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在联网获取在库清单，请稍后......");
        progressDialog.show();
    }

    protected void countDownTimerOnTick(long millisUntilFinished) {
        mBinding.listInLibraryCountdownTv.setText(String.valueOf(millisUntilFinished));
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

    private static class MHandler extends Handler {
        private final WeakReference<ListInLibraryActivity> listInLibraryActivityWeakReference;

        MHandler(ListInLibraryActivity listInLibraryActivity) {
            super();
            listInLibraryActivityWeakReference = new WeakReference<>(listInLibraryActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (listInLibraryActivityWeakReference.get() != null) {
                listInLibraryActivityWeakReference.get().handleMessage(msg);
            }
        }
    }

    private void getInBoundList() {
        String url = "";
        JSONObject jsonObject = new JSONObject();
        url = NetworkRequest.getInstance().urlInBoundList;
        try {
            jsonObject.put("CreatorID", spUtil.getString(SharedPreferencesUtil.Key.DeviceId, ""));
            jsonObject.put("MechanismCoding", spUtil.getString(SharedPreferencesUtil.Key.UnitNumber, ""));
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        LogUtil.getInstance().d("----" + jsonObject);
        LogUtil.getInstance().d("----" + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,
                jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonResult) {
                try {
                    LogUtil.getInstance().d("--  " + jsonResult);
                    JSONArray data = jsonResult.getJSONArray("Data");
                    if (jsonResult.getInt("Result") == 200 && data.length() > 0) {
                        List<Tools> toolsList = new ArrayList<>();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject jsonObject = data.getJSONObject(i);
                            Tools tools = new Tools();
                            tools.setCaseNumber(jsonObject.getString("CaseNumber"));
                            tools.setPropertyInvolved("1");
                            tools.setPropertyInvolvedName(jsonObject.getString("PropertyInVolvedName"));
                            tools.setPropertyNumber(jsonObject.getString("PropertyNumber"));
                            tools.setMechanismCoding(jsonObject.getString("MechanismCoding"));
                            tools.setMechanismName(jsonObject.getString("MechanismName"));
                            tools.setEpc(jsonObject.getString("EPC"));
                            tools.setCellNumber(jsonObject.getInt("CountErNumber"));
                            tools.setToolLightNumber(jsonObject.getInt("Light"));
                            tools.setState(jsonObject.getInt("State"));
                            tools.setNameParty(jsonObject.getString("NameParty"));
                            tools.setOperateTime(jsonObject.getString("OperateTime"));
                            toolsList.add(tools);
                        }
                        Message msg = Message.obtain();
                        msg.what = GET_IN_BOUND_LIST_SUCCESS;
                        msg.obj = toolsList;
                        mHandler.sendMessage(msg);
                    } else {
                        Message msg = Message.obtain();
                        msg.what = GET_IN_BOUND_LIST_ERROR;
                        msg.obj = "无入库数据。";
                        mHandler.sendMessage(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Message msg = Message.obtain();
                    msg.what = GET_IN_BOUND_LIST_ERROR;
                    msg.obj = "数据解析失败。";
                    mHandler.sendMessage(msg);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Message msg = Message.obtain();
                msg.what = GET_IN_BOUND_LIST_ERROR;
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
