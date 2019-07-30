package com.zk.cabinet.activity;

import android.app.ProgressDialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.zk.cabinet.R;
import com.zk.cabinet.adapter.ToolsAdapter;
import com.zk.cabinet.bean.Cabinet;
import com.zk.cabinet.bean.DoorSendInfo;
import com.zk.cabinet.bean.InventoryInfo;
import com.zk.cabinet.bean.LightSendInfo;
import com.zk.cabinet.bean.NettySendInfo;
import com.zk.cabinet.bean.Tools;
import com.zk.cabinet.callback.DoorListener;
import com.zk.cabinet.callback.InventoryListener;
import com.zk.cabinet.callback.LightListener;
import com.zk.cabinet.databinding.ActivityAccessingDepositBinding;
import com.zk.cabinet.db.CabinetService;
import com.zk.cabinet.db.ToolsService;
import com.zk.cabinet.netty.server.NettyServerParsingLibrary;
import com.zk.cabinet.network.NetworkRequest;
import com.zk.cabinet.serial.door.DoorSerialOperation;
import com.zk.cabinet.serial.light.LightSerialOperation;
import com.zk.cabinet.util.LogUtil;
import com.zk.cabinet.util.MediaPlayerUtil;
import com.zk.cabinet.util.SharedPreferencesUtil;
import com.zk.cabinet.util.SharedPreferencesUtil.Key;
import com.zk.cabinet.util.TimeOpera;
import com.zk.cabinet.view.CustomProgressDialog;
import com.zk.cabinet.view.FullScreenAlertDialog;
import com.zk.cabinet.view.TimeOffAppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AccessingDepositActivity extends TimeOffAppCompatActivity implements View.OnClickListener {
    private final int OPEN_BOX_RESULT = 0x00;
    private final int CHECK_BOX_DOOR_STATE = 0x01;
    private final int INVENTORY_18K6C = 0x02;
    private final int OPEN_LIGHT_RESULT = 0x03;
    private final int CHECK_LIGHT_STATE = 0x04;
    private final static int GET_TOOLS_IN_BOX_LIST_SUCCESS = 0x05;
    private final static int GET_TOOLS_IN_BOX_LIST_ERROR = 0x06;
    private final static int UP_UP_OUT_BOUND_LIST_SUCCESS = 0x07;
    private final static int UP_UP_OUT_BOUND_LIST_ERROR = 0x08;
    private final static int GET_TOOLS_IN_BOX_LIST_SUCCESS_TWO = 0x09;

    private int propertyInvolved;
    private ActivityAccessingDepositBinding binding;

    private int cellNumber; //格子编号
    private Cabinet cabinet; //格子
    private List<Integer> antennaNumberList; //分支器列表
    private int antennaNumberPosition = 0;
    private int openDooring = 0; // 0 关闭 1 开启
    private boolean inventorying = false; // false 关闭 true 正在盘点
    private String userTemp; //用户ID

    private List<Tools> toolsList; //
    private ToolsAdapter mAdapter;

    private List<InventoryInfo> inventoryList; //盘点的epc信息

    private CustomProgressDialog progressDialog;//进度条
    private FullScreenAlertDialog accessingFullDialog;//全屏显示
    private View accessingDialog;
    private ListView dialog_accessing_lv;
    private ToolsAdapter accessingAdapter;
    private TextView dialog_accessing_result_tv;
    private List<Tools> accessingList;
    private Button dialog_accessing_sure;
    private TextView dialog_accessing_reopen_error_tv;

    private ArrayList<Integer> lightNumbers, needOpenLightNumbers;//需要开灯的列表

    //    private String EPC;
    private List<Tools> depositList;

    private MHandler mHandler;

    private ProgressDialog progress;

    private String userIDTemp, unitNumber, deviceId;

    private void handleMessage(Message msg) {
        switch (msg.what) {
            case OPEN_BOX_RESULT:
                showToast("锁控收到开门指令");
                break;
            case CHECK_BOX_DOOR_STATE:
                if ((Integer) msg.obj == cabinet.getTargetAddress()) {
                    ArrayList<Integer> boxStateList = msg.getData().getIntegerArrayList("BoxStateList");
                    if (openDooring == 0) {
                        if (boxStateList.contains(cabinet.getLockNumber())) {
                            openDooring = 1;
                            timerCancel();
                            showToast(cabinet.getBoxName() + "已经开启.");
                        }
                    } else if (openDooring == 1) {
                        if (!boxStateList.contains(cabinet.getLockNumber())) {
                            timerStart();
//                            if (!inventorying) {
//                                inventorying = true;
                            DoorSerialOperation.getInstance().startCheckBoxDoorState(-1);
                            LightSerialOperation.getInstance().startCheckLightState(-1);
                            needOpenLightNumbers.clear();//关灯
                            LightSerialOperation.getInstance().send(new LightSendInfo(cabinet.getTargetAddressForLight(),
                                    cabinet.getSourceAddress(), needOpenLightNumbers));

                            openDooring = 0;
                            antennaNumberPosition = 0;
//                                NettyServerParsingLibrary.getInstance().send(new NettySendInfo(
//                                        cabinet.getReaderDeviceID(), 0,
//                                        antennaNumberList.get(antennaNumberPosition), 0));
//                                ProgressDialogShow("正在第1次盘点，请稍后......");
                            showToast(cabinet.getBoxName() + "已经关闭，请点击返回键返回上一层.");

//                            } else {
//                                showToast(cabinet.getBoxName() + "已经关闭.");
//                            }
                        }
                    }

                } else if (!inventorying) {
                    showToast("有其他柜门被开启，请检查！");
                } else {
                    showToast("正在盘点，请等待盘点结束");
                }
                break;
            case INVENTORY_18K6C:
                if (msg.arg1 == 0) {
                    if (antennaNumberPosition == 0) {
                        inventoryList = (List<InventoryInfo>) msg.obj;
                        if (inventoryList == null) inventoryList = new ArrayList<>();
                    } else {
                        List<InventoryInfo> temp = (List<InventoryInfo>) msg.obj;
                        if (temp != null) {
                            for (InventoryInfo inventoryInfoTemp : temp) {
                                boolean isExist = false;
                                for (InventoryInfo inventoryInfo : inventoryList) {
                                    if (inventoryInfo.getEPC().equalsIgnoreCase(inventoryInfoTemp.getEPC())) {
                                        isExist = true;
                                        break;
                                    }
                                }
                                if (!isExist) inventoryList.add(inventoryInfoTemp);
                            }
                        }
                    }
                    antennaNumberPosition++;
                    if (antennaNumberPosition < antennaNumberList.size()) {
                        ProgressDialogShow("正在第" + (antennaNumberPosition + 1) + "次盘点，请稍后......");
                        NettyServerParsingLibrary.getInstance().send(new NettySendInfo(
                                cabinet.getReaderDeviceID(), 0,
                                antennaNumberList.get(antennaNumberPosition), 0));


                    } else { // 盘点结束
                        ProgressDialogDismiss();
                        int saveNumber = 0, takeNumber = 0;
                        for (InventoryInfo inventoryInfo : inventoryList) {
                            boolean isSave = false;
                            for (Tools tools : depositList) {
                                if (inventoryInfo.getEPC().equalsIgnoreCase(tools.getEpc())) {
                                    Tools toolsTemp = tools;
                                    saveNumber++; // 有工具存入

                                    toolsTemp.setState(1);
                                    toolsTemp.setOperateTime(TimeOpera.getStringDateShort());

                                    accessingList.add(toolsTemp);
                                    isSave = true;
                                    break;
                                }
                            }
                            if (isSave) break;
                        }
                        for (Tools tools : toolsList) {
                            boolean isExist = false;
                            for (InventoryInfo inventoryInfo : inventoryList) {
                                if (tools.getEpc().equalsIgnoreCase(inventoryInfo.getEPC())) {
                                    isExist = true;
                                    break;
                                }
                            }
                            if (!isExist) {
                                takeNumber++;
                            }
                        }

                        String messageStr = "";
                        if (saveNumber != 0 && takeNumber != 0) {
                            messageStr = "本次操作取出：" + takeNumber + "件，存入：" + saveNumber + "件";
                        } else if (saveNumber != 0 && takeNumber == 0) {
                            messageStr = "本次操作存入：" + saveNumber + "件";
                        } else if (saveNumber == 0 && takeNumber != 0) {
                            messageStr = "本次操作取出：" + takeNumber + "件";
                        } else if (saveNumber == 0 && takeNumber == 0) {
                            messageStr = "本次未发生存取操作";
                        }

                        if (accessingDialog == null) {
                            accessingDialog = getLayoutInflater().inflate(R.layout.dialog_accessing, null);
                            accessingFullDialog = new FullScreenAlertDialog(this);
                            accessingFullDialog.show();
                            accessingFullDialog.setCancelable(false);
                            accessingFullDialog.setContentView(accessingDialog);
                            dialog_accessing_lv = accessingDialog.findViewById(R.id.dialog_accessing_lv);
                            accessingAdapter = new ToolsAdapter(this, accessingList);
                            accessingDialog.findViewById(R.id.dialog_accessing_abnormal_completion_operation).setOnClickListener(this);
                            accessingDialog.findViewById(R.id.dialog_accessing_reopen).setOnClickListener(this);
                            dialog_accessing_sure = accessingDialog.findViewById(R.id.dialog_accessing_sure);
                            dialog_accessing_sure.setOnClickListener(this);
                            dialog_accessing_result_tv = accessingDialog.findViewById(R.id.dialog_accessing_result_tv);
                            dialog_accessing_reopen_error_tv = accessingDialog.findViewById(R.id.dialog_accessing_reopen_error_tv);
                            dialog_accessing_lv.setAdapter(accessingAdapter);
                        } else {
                            accessingFullDialog.show();
                            accessingAdapter.notifyDataSetChanged();
                        }
                        dialog_accessing_sure.setEnabled(true);
                        dialog_accessing_result_tv.setText(messageStr);

                        if (takeNumber > 0) {
                            accessingDialog.findViewById(R.id.dialog_accessing_sure).setEnabled(false);
                            accessingDialog.findViewById(R.id.dialog_accessing_sure).setVisibility(View.INVISIBLE);
                            dialog_accessing_reopen_error_tv.setText("本次操作只允许存入！");
                            MediaPlayerUtil.getInstance().reportNumber(0,0, 5);
                        } else {
                            if (accessingList.size() == 1) {
                                boolean isOK = false;
                                for (Tools tools : depositList) {
                                    if (tools.getEpc().equalsIgnoreCase(accessingList.get(0).getEpc())) {
                                        isOK = true;
                                        MediaPlayerUtil.getInstance().reportNumber(takeNumber,saveNumber, 0);
                                        dialog_accessing_reopen_error_tv.setVisibility(View.GONE);
                                        accessingDialog.findViewById(R.id.dialog_accessing_sure).setEnabled(true);
                                        accessingDialog.findViewById(R.id.dialog_accessing_sure).setVisibility(View.VISIBLE);
                                        break;
                                    }
                                }
                                if (!isOK) {
                                    accessingDialog.findViewById(R.id.dialog_accessing_sure).setEnabled(false);
                                    accessingDialog.findViewById(R.id.dialog_accessing_sure).setVisibility(View.INVISIBLE);
                                    dialog_accessing_reopen_error_tv.setText("您存入的工具和您准备存入的工具不符！");
                                    MediaPlayerUtil.getInstance().reportNumber(0,0, 6);
                                }
                            } else if (accessingList.size() > 1) {
                                accessingDialog.findViewById(R.id.dialog_accessing_sure).setEnabled(false);
                                accessingDialog.findViewById(R.id.dialog_accessing_sure).setVisibility(View.INVISIBLE);
                                dialog_accessing_reopen_error_tv.setText("您存入了多个工具！");
                                MediaPlayerUtil.getInstance().reportNumber(0,0, 7);
                            } else if (accessingList.size() == 0) {
                                dialog_accessing_reopen_error_tv.setVisibility(View.GONE);
                                accessingDialog.findViewById(R.id.dialog_accessing_sure).setEnabled(true);
                                accessingDialog.findViewById(R.id.dialog_accessing_sure).setVisibility(View.VISIBLE);
                                MediaPlayerUtil.getInstance().reportNumber(takeNumber,saveNumber, 0);
                            }
                        }
                    }
                } else {
                    accessClear();
                    showToast("读卡器离线，本次存取操作无效！");
                    MediaPlayerUtil.getInstance().reportNumber(0,0, 1);
                }
                break;
            case OPEN_LIGHT_RESULT:
                showToast("灯控收到指令");
                break;
            case CHECK_LIGHT_STATE:
                if (!inventorying && openDooring == 1) {
                    needOpenLightNumbers = msg.getData().getIntegerArrayList("LightStateList");
                    LogUtil.getInstance().d("lightNumbers.s " + lightNumbers.size());
                    LogUtil.getInstance().d("needOpenLightNumbers.s " + needOpenLightNumbers.size());
                    needOpenLightNumbers.removeAll(lightNumbers);
                    if (needOpenLightNumbers.size() == 1) {
                        inventorying = true;

                        LightSerialOperation.getInstance().send(new LightSendInfo(cabinet.getTargetAddressForLight(),
                                cabinet.getSourceAddress(), needOpenLightNumbers));

                        antennaNumberPosition = 0;
                        NettyServerParsingLibrary.getInstance().send(new NettySendInfo(
                                cabinet.getReaderDeviceID(), 0,
                                antennaNumberList.get(antennaNumberPosition), 0));
                        showToast(needOpenLightNumbers.get(0) + "号位已经插入，准备盘点.");
                        ProgressDialogShow("正在第1次盘点，请稍后......");

                    } else if (needOpenLightNumbers.size() > 1) {
                        MediaPlayerUtil.getInstance().reportNumber(0,0, 4);
                        showToast("本次操作只允许归还一件工具");
                    }
                }
                break;
            case GET_TOOLS_IN_BOX_LIST_SUCCESS:
                progress.dismiss();
                toolsList = (List<Tools>) msg.obj;

                for (Tools tools : toolsList) {
                    lightNumbers.add(tools.getToolLightNumber());
                }
                LogUtil.getInstance().d("lightNumbers.s " + lightNumbers.size());
                mAdapter.setList(toolsList);
                mAdapter.notifyDataSetChanged();
                binding.accessingDepositToolNumberTv.setText("本柜共有：" + toolsList.size() + "件工具");
                binding.accessingDepositOpenBtn.setVisibility(View.INVISIBLE);
                DoorSerialOperation.getInstance().send(new DoorSendInfo(cabinet.getTargetAddress(),
                        cabinet.getSourceAddress(), cabinet.getLockNumber()));
                DoorSerialOperation.getInstance().startCheckBoxDoorState(cabinet.getTargetAddress());

                LightSerialOperation.getInstance().startCheckLightState(cabinet.getTargetAddressForLight());
                accessClear();
                break;
            case GET_TOOLS_IN_BOX_LIST_SUCCESS_TWO:
                progress.dismiss();
                toolsList = (List<Tools>) msg.obj;
                mAdapter.setList(toolsList);
                mAdapter.notifyDataSetChanged();
                binding.accessingDepositToolNumberTv.setText("本柜共有：" + toolsList.size() + "件工具");
                lightNumbers.clear();
                for (Tools tools : toolsList) {
                    lightNumbers.add(tools.getToolLightNumber());
                }
                accessClear();
                break;
            case GET_TOOLS_IN_BOX_LIST_ERROR:
                binding.accessingDepositToolNumberTv.setText("获取异常");
                progress.dismiss();
                showToast(msg.obj.toString());
                break;
            case UP_UP_OUT_BOUND_LIST_SUCCESS:

                binding.accessingDepositToolNumberTv.setText("正在联网获取格子信息");
                progress.setMessage("正在联网获取格子数据，请稍后......");
                getToolsInBoxList(GET_TOOLS_IN_BOX_LIST_SUCCESS_TWO);
                break;
            case UP_UP_OUT_BOUND_LIST_ERROR:
                toolsList.clear();
                mAdapter.notifyDataSetChanged();
                binding.accessingDepositToolNumberTv.setText("获取异常");
                progress.dismiss();
                showToast(msg.obj.toString());
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accessing_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_accessing_deposit);
        binding.setOnClickListener(this);
        setSupportActionBar(binding.accessingDepositToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mHandler = new MHandler(this);

        init();
    }

    private void init() {

        userIDTemp = spUtil.getString(Key.UserIDTemp, "");
        unitNumber = spUtil.getString(Key.UnitNumber, "");
        deviceId = spUtil.getString(Key.DeviceId, "");

        propertyInvolved = getIntent().getIntExtra("PropertyInvolved", 1);
        userTemp = spUtil.getString(SharedPreferencesUtil.Key.UserIDTemp, "");
        cellNumber = getIntent().getExtras().getInt("CellNumber");
        cabinet = CabinetService.getInstance().queryEq(cellNumber);
//        EPC = getIntent().getExtras().getString("EPC");
        depositList = ToolsService.getInstance().getDepositTools(cellNumber);

        lightNumbers = new ArrayList<>();
        accessingList = new ArrayList<>();
        antennaNumberList = new ArrayList<>();//分支器list
        String[] antennaNumberStr = cabinet.getAntennaNumber().split(",");
        for (String s : antennaNumberStr) {
            antennaNumberList.add(Integer.valueOf(s));
        }

        DoorSerialOperation.getInstance().onDoorListener(doorListener);
        LightSerialOperation.getInstance().onLightListener(lightListener);
        NettyServerParsingLibrary.getInstance().processor.onInventoryListener(inventoryListener);

        binding.accessingDepositBoxNameTv.setText(cabinet.getBoxName());
//        toolsList = ToolsService.getInstance().queryEq(cabinet.getCellNumber(), 0);
//        if (toolsList == null)
        toolsList = new ArrayList<>();
        mAdapter = new ToolsAdapter(this, toolsList);
        binding.accessingDepositLv.setAdapter(mAdapter);
        binding.accessingDepositToolNumberTv.setText("本柜共有：" + toolsList.size() + "件工具");

//        for (Tools tools : toolsList) {
//            lightNumbers.add(tools.getToolLightNumber());
//        }
//
//        if (getIntent().getExtras().getBoolean("ImmediatelyOpen")) { //进来就开门
//            binding.accessingDepositOpenBtn.setVisibility(View.INVISIBLE);
//            DoorSerialOperation.getInstance().send(new DoorSendInfo(cabinet.getTargetAddress(),
//                    cabinet.getSourceAddress(), cabinet.getLockNumber()));
//            DoorSerialOperation.getInstance().startCheckBoxDoorState(cabinet.getTargetAddress());
//
//            LightSerialOperation.getInstance().startCheckLightState(cabinet.getTargetAddressForLight());
//        }

        getToolsInBoxList(GET_TOOLS_IN_BOX_LIST_SUCCESS);
        progress = new ProgressDialog(this);
        progress.setMessage("正在联网获取格子数据，请稍后......");
        progress.show();


    }

    protected void countDownTimerOnTick(long millisUntilFinished) {
        binding.accessingDepositCountdownTv.setText(String.valueOf(millisUntilFinished));
    }

    private InventoryListener inventoryListener = new InventoryListener() {
        @Override
        public void inventoryList(int result, List<InventoryInfo> inventoryInfoList) {
            Message msg = Message.obtain();
            msg.what = INVENTORY_18K6C;
            msg.obj = inventoryInfoList;
            msg.arg1 = result;
            mHandler.sendMessage(msg);
        }
    };

    private DoorListener doorListener = new DoorListener() {
        @Override
        public void openBoxResult(boolean openBoxResult) {
            Message msg = Message.obtain();
            msg.what = OPEN_BOX_RESULT;
            msg.obj = openBoxResult;
            mHandler.sendMessage(msg);
        }

        @Override
        public void checkBoxDoorState(int targetAddress, ArrayList<Integer> boxStateList) {
            Message msg = Message.obtain();
            msg.what = CHECK_BOX_DOOR_STATE;
            msg.obj = targetAddress;
            Bundle b = new Bundle();
            b.putIntegerArrayList("BoxStateList", boxStateList);
            msg.setData(b);
            mHandler.sendMessage(msg);
        }
    };


    private LightListener lightListener = new LightListener() {
        @Override
        public void openLightResult(boolean openBoxResult) {
            Message msg = Message.obtain();
            msg.what = OPEN_LIGHT_RESULT;
            msg.obj = openBoxResult;
            mHandler.sendMessage(msg);
        }

        @Override
        public void checkLightState(int targetAddress, ArrayList<Integer> lightStateList) {
            Message msg = Message.obtain();
            msg.what = CHECK_LIGHT_STATE;
            msg.obj = targetAddress;
            Bundle b = new Bundle();
            b.putIntegerArrayList("LightStateList", lightStateList);
            msg.setData(b);
            mHandler.sendMessage(msg);
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.accessing_deposit_open_btn:
                for (Tools tools : toolsList) {
                    if (tools.isSelected()) {
                        lightNumbers.add(tools.getToolLightNumber());
                    }
                }

                DoorSerialOperation.getInstance().send(new DoorSendInfo(cabinet.getTargetAddress(),
                        cabinet.getSourceAddress(), cabinet.getLockNumber()));
                DoorSerialOperation.getInstance().startCheckBoxDoorState(cabinet.getTargetAddress());

                LightSerialOperation.getInstance().startCheckLightState(cabinet.getTargetAddressForLight());
                LightSerialOperation.getInstance().send(new LightSendInfo(cabinet.getTargetAddressForLight(),
                        cabinet.getSourceAddress(), lightNumbers));
                break;
            case R.id.dialog_accessing_reopen:
                accessClear();

                DoorSerialOperation.getInstance().send(new DoorSendInfo(cabinet.getTargetAddress(),
                        cabinet.getSourceAddress(), cabinet.getLockNumber()));
                DoorSerialOperation.getInstance().startCheckBoxDoorState(cabinet.getTargetAddress());
                LightSerialOperation.getInstance().startCheckLightState(cabinet.getTargetAddressForLight());
                break;
            case R.id.dialog_accessing_sure:
//                ToolsService.getInstance().insertOrUpdate(accessingList);

//                DoorSerialOperation.getInstance().startCheckBoxDoorState(-1);
//                LightSerialOperation.getInstance().startCheckLightState(-1);

//                accessClear();
                timerStart();

                if (accessingFullDialog != null && accessingFullDialog.isShowing())
                    accessingFullDialog.dismiss();

                progress.setMessage("正在提交出库数据，请稍后......");
                progress.show();
                if (accessingList.size() > 0)
                    getUpOutBoundList(accessingList);


//                toolsList = ToolsService.getInstance().queryEq(cabinet.getCellNumber(), 0);
//                if (toolsList == null) toolsList = new ArrayList<>();
//                mAdapter.setList(toolsList);
//                mAdapter.notifyDataSetChanged();
//                binding.accessingDepositToolNumberTv.setText("本柜共有：" + toolsList.size() + "件工具");
                break;
        }
    }

    private static class MHandler extends Handler {
        private final WeakReference<AccessingDepositActivity> accessingActivityWeakReference;

        MHandler(AccessingDepositActivity accessingDepositActivity) {
            super();
            accessingActivityWeakReference = new WeakReference<>(accessingDepositActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (accessingActivityWeakReference.get() != null) {
                accessingActivityWeakReference.get().handleMessage(msg);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (openDooring == 0) {
                    finish();
                } else {
                    showToast("请先关闭柜门！");
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //转圈圈提示开始
    private void ProgressDialogShow(String msg) {
        if (progressDialog == null)
            progressDialog = new CustomProgressDialog(this);
        progressDialog.setMessage(msg);
        if (!progressDialog.isShowing()) {
            isDialogShow = true;
            progressDialog.show();
        }
    }

    //转圈圈提示结束
    private void ProgressDialogDismiss() {
        if (progressDialog != null && progressDialog.isShowing()) {
            isDialogShow = false;
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        for (Tools tools : toolsList) {
            tools.setSelected(false);
        }
        DoorSerialOperation.getInstance().onDoorListener(null);
        DoorSerialOperation.getInstance().startCheckBoxDoorState(-1);
        LightSerialOperation.getInstance().startCheckLightState(-1);
        LightSerialOperation.getInstance().onLightListener(null);
        NettyServerParsingLibrary.getInstance().processor.onInventoryListener(null);
        lightNumbers.clear();
        LightSerialOperation.getInstance().send(new LightSendInfo(cabinet.getTargetAddressForLight(),
                cabinet.getSourceAddress(), lightNumbers));
        super.onDestroy();
    }

    private void accessClear() {
        inventorying = false; //是否在盘点
        openDooring = 0; //门状态
        antennaNumberPosition = 0;
        if (accessingList != null) accessingList.clear();
        if (inventoryList != null) inventoryList.clear();
        if (accessingFullDialog != null && accessingFullDialog.isShowing())
            accessingFullDialog.dismiss();
        ProgressDialogDismiss();
    }

    private void getToolsInBoxList(final int what) {
        String url = "";
        JSONObject jsonObject = new JSONObject();
        url = NetworkRequest.getInstance().urlToolsInBoxList;
        try {
            jsonObject.put("CabinetID", spUtil.getString(Key.DeviceId, ""));
            jsonObject.put("CountErNumber", cabinet.getCellNumber());
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
                        msg.what = what;
                        msg.obj = toolsList;
                        mHandler.sendMessage(msg);
                    } else {
                        List<Tools> toolsList = new ArrayList<>();
                        Message msg = Message.obtain();
                        msg.what = what;
                        msg.obj = toolsList;
                        mHandler.sendMessage(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Message msg = Message.obtain();
                    msg.what = GET_TOOLS_IN_BOX_LIST_ERROR;
                    msg.obj = "数据解析失败。";
                    mHandler.sendMessage(msg);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Message msg = Message.obtain();
                msg.what = GET_TOOLS_IN_BOX_LIST_ERROR;
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


    private void getUpOutBoundList(List<Tools> upAccessingList) {
        String url = "";
        JSONObject jsonObject = new JSONObject();
        url = NetworkRequest.getInstance().urlUpOutBoundList;
        try {
            jsonObject.put("CreatorID", userIDTemp);
            jsonObject.put("Creator ", spUtil.getString(Key.NameTemp, ""));
            jsonObject.put("CabinetID", deviceId);
            jsonObject.put("TypeState", 0);
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < upAccessingList.size(); i++) {
                JSONObject dataJsonObject = new JSONObject();
                dataJsonObject.put("CaseNumber", upAccessingList.get(i).getCaseNumber());
                dataJsonObject.put("PropertyInVolved", propertyInvolved);
                dataJsonObject.put("PropertyInVolvedName", upAccessingList.get(i).getPropertyInvolvedName());
                dataJsonObject.put("PropertyNumber", upAccessingList.get(i).getPropertyNumber());
                dataJsonObject.put("MechanismCoding", upAccessingList.get(i).getMechanismCoding());
                dataJsonObject.put("MechanismName", upAccessingList.get(i).getMechanismName());
                dataJsonObject.put("EPC", upAccessingList.get(i).getEpc());
                dataJsonObject.put("CountErNumber", upAccessingList.get(i).getCellNumber());
                dataJsonObject.put("Light", upAccessingList.get(i).getToolLightNumber());
                dataJsonObject.put("State", upAccessingList.get(i).getState());
                dataJsonObject.put("OperateTime", upAccessingList.get(i).getOperateTime());
                dataJsonObject.put("NameParty", upAccessingList.get(i).getNameParty());
                dataJsonObject.put("CabinetID", deviceId);
                jsonArray.put(dataJsonObject);
                LogUtil.getInstance().d("出库上报写入：" + jsonArray);

            }
            jsonObject.put("toolInStoreBacks", jsonArray);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        LogUtil.getInstance().d("出库上报：" + jsonObject);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,
                jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonResult) {

                Message msg = Message.obtain();
                msg.what = UP_UP_OUT_BOUND_LIST_SUCCESS;
                msg.obj = toolsList;
                mHandler.sendMessage(msg);

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Message msg = Message.obtain();
                msg.what = UP_UP_OUT_BOUND_LIST_ERROR;
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