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
import com.zk.cabinet.adapter.ToolsAdapter;
import com.zk.cabinet.bean.Tools;
import com.zk.cabinet.databinding.ActivityAccessOutByQueryActivityBinding;
import com.zk.cabinet.db.ToolsService;
import com.zk.cabinet.network.NetworkRequest;
import com.zk.cabinet.util.LogUtil;
import com.zk.cabinet.util.SharedPreferencesUtil.Key;
import com.zk.cabinet.view.TimeOffAppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AccessOutByQueryActivity extends TimeOffAppCompatActivity implements View.OnClickListener {
    private final static int GET_OUT_BOUND_LIST_SUCCESS = 0x00;
    private final static int GET_OUT_BOUND_LIST_ERROR = 0x01;
    private ActivityAccessOutByQueryActivityBinding binding;
    private int propertyInvolved;

    private List<Tools> list;
    private ToolsAdapter mAdapter;

    private ProgressDialog progressDialog;

    private String userIDTemp, unitNumber, deviceId;

    private MHandler mHandler;

    private void handleMessage(Message msg) {
        switch (msg.what) {
            case GET_OUT_BOUND_LIST_SUCCESS:
                ToolsService.getInstance().deleteByState(0);
                progressDialog.dismiss();
                list = (List<Tools>) msg.obj;
                ToolsService.getInstance().insert(list);
                mAdapter.setList(list);
                mAdapter.notifyDataSetChanged();
                break;
            case GET_OUT_BOUND_LIST_ERROR:
                ToolsService.getInstance().deleteByState(0);
                progressDialog.dismiss();
                list.clear();
                mAdapter.notifyDataSetChanged();
                showToast(msg.obj.toString());
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_access_out_by_query_activity);
        binding.setOnClickListener(this);
        setSupportActionBar(binding.accessOutByQueryToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        propertyInvolved = getIntent().getIntExtra("PropertyInvolved", 1);
        init();
    }

    protected void countDownTimerOnTick(long millisUntilFinished) {
        binding.accessOutByQueryCountdownTv.setText(String.valueOf(millisUntilFinished));
    }

    private void init() {

        userIDTemp = spUtil.getString(Key.UserIDTemp, "");
        unitNumber = spUtil.getString(Key.UnitNumber, "");
        deviceId = spUtil.getString(Key.DeviceId, "");

        ToolsService.getInstance().deleteByState(0);

        mHandler = new MHandler(this);
        list = new ArrayList<>();
        mAdapter = new ToolsAdapter(this, list);
        binding.accessOutByQueryQueryLv.setAdapter(mAdapter);
        binding.accessOutByQueryQueryLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (!list.get(position).isSelected()) {
                    boolean isSelect = false;
                    for (Tools tools : list) {
                        if (tools.isSelected()) {
                            isSelect = true;
                            if (tools.getCellNumber() == list.get(position).getCellNumber()) {
                                list.get(position).setSelected(true);
                            } else {
                                showToast("您当前选中的工具与之前选中的工具不在同一个格子。");
                            }
                            break;
                        }
                    }
                    if (!isSelect) list.get(position).setSelected(true);
                } else {
                    list.get(position).setSelected(false);
                }
                mAdapter.notifyDataSetChanged();


                /* 单个存取的逻辑
                Cabinet cabinetTemp = CabinetService.getInstance().queryEq(list.get(position).getCellNumber());

                Bundle bundle = new Bundle();
                bundle.putInt("CellNumber", cabinetTemp.getCellNumber());
                bundle.putInt("OperationType", 1);
                bundle.putString("EPC", list.get(position).getEpc());
                bundle.putBoolean("ImmediatelyOpen", true);
                bundle.putInt("PropertyInvolved", propertyInvolved);
                IntentActivity(AccessingOutActivity.class, bundle);
                */
            }
        });
        getOutBoundList();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在联网获取出库清单，请稍后......");
        progressDialog.show();
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
            case R.id.access_out_btn:
                List<Tools> outList = new ArrayList<>();
                for (Tools tools : list) {
                    tools.setOperating(1);
                    if (tools.isSelected()) outList.add(tools);
                }
                if (outList.size() > 0) {
                    ToolsService.getInstance().update(outList);

                    Bundle bundle = new Bundle();
                    bundle.putInt("CellNumber", outList.get(0).getCellNumber());
                    bundle.putInt("OperationType", 1);
                    bundle.putBoolean("ImmediatelyOpen", true);
                    bundle.putInt("PropertyInvolved", propertyInvolved);
                    IntentActivity(AccessingOutActivity.class, bundle);
                } else showToast("请选中出库的工具");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_CODE) {
            getOutBoundList();
            if (progressDialog == null)
                progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在联网获取入库清单，请稍后......");
            progressDialog.show();
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private static class MHandler extends Handler {
        private final WeakReference<AccessOutByQueryActivity> accessOutByQueryWeakReference;

        MHandler(AccessOutByQueryActivity accessOutByQueryActivity) {
            super();
            accessOutByQueryWeakReference = new WeakReference<>(accessOutByQueryActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (accessOutByQueryWeakReference.get() != null) {
                accessOutByQueryWeakReference.get().handleMessage(msg);
            }
        }
    }

    private void getOutBoundList() {
        String url = "";
        JSONObject jsonObject = new JSONObject();
        url = NetworkRequest.getInstance().urlOutBoundList;
        try {
            jsonObject.put("CreatorID", userIDTemp);
            jsonObject.put("MechanismCoding", unitNumber);
            jsonObject.put("CabinetID", deviceId);
            jsonObject.put("PropertyInVolved", propertyInvolved);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        LogUtil.getInstance().d(jsonObject.toString());
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
                            tools.setPropertyInvolved(String.valueOf(propertyInvolved));
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
                        msg.what = GET_OUT_BOUND_LIST_SUCCESS;
                        msg.obj = toolsList;
                        mHandler.sendMessage(msg);
                    } else {
                        Message msg = Message.obtain();
                        msg.what = GET_OUT_BOUND_LIST_ERROR;
                        msg.obj = "无出库数据。";
                        mHandler.sendMessage(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Message msg = Message.obtain();
                    msg.what = GET_OUT_BOUND_LIST_ERROR;
                    msg.obj = "数据解析失败。";
                    mHandler.sendMessage(msg);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Message msg = Message.obtain();
                msg.what = GET_OUT_BOUND_LIST_ERROR;
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
