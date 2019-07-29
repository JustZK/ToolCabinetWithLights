package com.zk.cabinet.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
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
import com.zk.cabinet.bean.Cabinet;
import com.zk.cabinet.bean.Tools;
import com.zk.cabinet.databinding.ActivityAccessDepositBinding;
import com.zk.cabinet.db.CabinetService;
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

public class AccessDepositActivity extends TimeOffAppCompatActivity {
    private final static int GET_IN_BOUND_LIST_SUCCESS = 0x00;
    private final static int GET_IN_BOUND_LIST_ERROR = 0x01;
    private ActivityAccessDepositBinding binding;
    private int propertyInvolved;

    private String userTemp, unitNumber;

    private List<Tools> list;
    private ToolsAdapter mAdapter;

    private AlertDialog.Builder openBuilder;

    private ProgressDialog progressDialog;
    private MHandler mHandler;

    private void handleMessage(Message msg) {
        switch (msg.what) {
            case GET_IN_BOUND_LIST_SUCCESS:
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                list = (List<Tools>) msg.obj;
                ToolsService.getInstance().insert(list);
                mAdapter.setList(list);
                mAdapter.notifyDataSetChanged();
                break;
            case GET_IN_BOUND_LIST_ERROR:
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                list.clear();
                ToolsService.getInstance().insert(list);
                mAdapter.setList(list);
                mAdapter.notifyDataSetChanged();
                showToast(msg.obj.toString());
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_access_deposit);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_access_deposit);
        setSupportActionBar(binding.accessDepositToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        propertyInvolved = getIntent().getIntExtra("PropertyInvolved", 1);
        init();
    }

    protected void countDownTimerOnTick(long millisUntilFinished) {
        binding.accessDepositCountdownTv.setText(String.valueOf(millisUntilFinished));
    }

    private void init() {

        ToolsService.getInstance().deleteByState(1);

        mHandler = new MHandler(this);
        userTemp = spUtil.getString(Key.UserIDTemp, "");
        unitNumber = spUtil.getString(Key.UnitNumber, "");

//        list = ToolsService.getInstance().queryOr(userTemp, 1);
//        if (list == null)

        list = new ArrayList<>();
        mAdapter = new ToolsAdapter(this, list);
        binding.accessDepositQueryLv.setAdapter(mAdapter);
        binding.accessDepositQueryLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (openBuilder == null) {
                    openBuilder = new AlertDialog.Builder(AccessDepositActivity.this);
                }
                final Cabinet cabinetTemp = CabinetService.getInstance().queryEq(list.get(position).getCellNumber());
                openBuilder.setTitle(getString(R.string.title_open_cabinet_where_the_file_is_located));
                openBuilder.setMessage(String.format(getResources().getString(R.string.open_cabinet_where_the_file_is_located),
                        list.get(position).getPropertyInvolvedName(), cabinetTemp.getBoxName()));
                openBuilder.setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("CellNumber", cabinetTemp.getCellNumber());
                        bundle.putInt("OperationType", 2);
                        bundle.putString("EPC", list.get(position).getEpc());
                        bundle.putBoolean("ImmediatelyOpen", true);

                        bundle.putString("CaseNumber", list.get(position).getCaseNumber());
                        bundle.putString("PropertyInVolvedName", list.get(position).getPropertyInvolvedName());
                        bundle.putString("PropertyNumber", list.get(position).getPropertyNumber());
                        bundle.putString("MechanismCoding", list.get(position).getMechanismCoding());
                        bundle.putString("MechanismName", list.get(position).getMechanismName());

                        bundle.putInt("PropertyInvolved", propertyInvolved);
                        IntentActivity(AccessingDepositActivity.class, bundle);
                    }
                });
                openBuilder.setNegativeButton(getString(R.string.cancel), null);
                openBuilder.show();
            }
        });

        getInBoundList();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在联网获取入库清单，请稍后......");
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        list = ToolsService.getInstance().queryOr(userTemp, 1);
//        if (list == null) list = new ArrayList<>();
//        mAdapter.setList(list);
//        mAdapter.notifyDataSetChanged();
        if (resultCode != RESULT_CODE) {
            getInBoundList();
            if (progressDialog == null)
                progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在联网获取入库清单，请稍后......");
            progressDialog.show();
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private static class MHandler extends Handler {
        private final WeakReference<AccessDepositActivity> accessDepositActivityWeakReference;

        MHandler(AccessDepositActivity accessDepositActivity) {
            super();
            accessDepositActivityWeakReference = new WeakReference<>(accessDepositActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (accessDepositActivityWeakReference.get() != null) {
                accessDepositActivityWeakReference.get().handleMessage(msg);
            }
        }
    }

    private void getInBoundList() {
        String url = "";
        JSONObject jsonObject = new JSONObject();
        url = NetworkRequest.getInstance().urlInBoundList;
        try {
            jsonObject.put("CreatorID", userTemp);
            jsonObject.put("MechanismCoding", unitNumber);
            jsonObject.put("PropertyInVolved", propertyInvolved);
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
