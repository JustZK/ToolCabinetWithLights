package com.zk.cabinet.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.zk.cabinet.R;
import com.zk.cabinet.adapter.ToolsAdapter;
import com.zk.cabinet.bean.Cabinet;
import com.zk.cabinet.bean.DoorSendInfo;
import com.zk.cabinet.bean.InventoryInfo;
import com.zk.cabinet.bean.NettySendInfo;
import com.zk.cabinet.bean.Tools;
import com.zk.cabinet.callback.DoorListener;
import com.zk.cabinet.callback.InventoryListener;
import com.zk.cabinet.databinding.ActivityAccessingBinding;
import com.zk.cabinet.db.CabinetService;
import com.zk.cabinet.db.ToolsService;
import com.zk.cabinet.netty.server.NettyServerParsingLibrary;
import com.zk.cabinet.serial.door.DoorSerialOperation;
import com.zk.cabinet.util.LogUtil;
import com.zk.cabinet.util.SharedPreferencesUtil;
import com.zk.cabinet.view.CustomProgressDialog;
import com.zk.cabinet.view.FullScreenAlertDialog;
import com.zk.cabinet.view.TimeOffAppCompatActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AccessingActivity extends TimeOffAppCompatActivity implements View.OnClickListener {
    private int operationType = 0;

    private String userTemp;

    private final int OPEN_BOX_RESULT = 0x00;
    private final int CHECK_BOX_DOOR_STATE = 0x01;
    private final int INVENTORY_18K6C = 0x02;

    private ActivityAccessingBinding binding;
    private int cellNumber;
    private Cabinet cabinet;
    private List<Integer> antennaNumberList;
    private int antennaNumberPosition = 0;

    private int openDooring = 0; // 0 关闭 1 开启

    private List<Tools> toolsList;
    private ToolsAdapter mAdapter;
    private List<InventoryInfo> inventoryList;

    private CustomProgressDialog progressDialog;

    private FullScreenAlertDialog accessingFullDialog;
    private View accessingDialog;
    private ListView dialog_accessing_lv;
    private ToolsAdapter accessingAdapter;
    private TextView dialog_accessing_result_tv;
    private List<Tools> accessingList;
    private Button dialog_accessing_sure;
    private TextView dialog_accessing_reopen_error_tv;

    private MHandler mHandler;

    private void handleMessage(Message msg) {
        switch (msg.what) {
            case OPEN_BOX_RESULT:
                showToast("锁孔板收到开门指令");
                LogUtil.getInstance().LogPrint("锁孔板收到开门指令");
                break;
            case CHECK_BOX_DOOR_STATE:
                if ((Integer) msg.obj == cabinet.getTargetAddress()) {
                    ArrayList<Integer> boxStateList = msg.getData().getIntegerArrayList("BoxStateList");
                    if (openDooring == 0) {
                        if (boxStateList.contains(cabinet.getLockNumber())) {
                            openDooring = 1;
                            timerCancel();
                            showToast(cabinet.getBoxName() + "已经开启.");
                            LogUtil.getInstance().LogPrint(cabinet.getBoxName() + "已经开启.");
                        }
                    } else if (openDooring == 1) {
                        if (!boxStateList.contains(cabinet.getLockNumber())) {
                            DoorSerialOperation.getInstance().startCheckBoxDoorState(-1);
                            openDooring = 0;
                            LogUtil.getInstance().LogPrint(cabinet.getBoxName() + "已经关闭，准备盘点.");
                            antennaNumberPosition = 0;
                            NettyServerParsingLibrary.getInstance().send(new NettySendInfo(cabinet.getReaderDeviceID(), 0, antennaNumberList.get(antennaNumberPosition), 0));
                            showToast(cabinet.getBoxName() + "已经关闭，准备盘点.");
                            ProgressDialogShow("正在第1次盘点，请稍后......");
                        }
                    }

                } else {
                    showToast("有其他柜门被开启，请检查！");
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
                        NettyServerParsingLibrary.getInstance().send(new NettySendInfo(cabinet.getReaderDeviceID(), 0, antennaNumberList.get(antennaNumberPosition), 0));


                    } else {
                        ProgressDialogDismiss();
                        int saveNumber = 0, takeNumber = 0;
                        for (InventoryInfo inventoryInfo : inventoryList) {
                            Tools toolsTemp = ToolsService.getInstance().queryEq(inventoryInfo.getEPC());
                            if (toolsTemp != null) {
                                if (toolsTemp.getToolState() != 0) {
                                    saveNumber++;
                                    toolsTemp.setCellNumber(cellNumber);
                                    toolsTemp.setToolState(0);
                                    toolsTemp.setBorrower(userTemp);
                                    accessingList.add(toolsTemp);
                                }
                            }
//                            客户需求不要没有录入的数据
//                            else {
//                                saveNumber++;
//                                Tools toolsNew = new Tools();
//                                toolsNew.setToolState(0);
//                                toolsNew.setEpc(inventoryInfo.getEPC());
//                                toolsNew.setCellNumber(cellNumber);
//                                toolsNew.setBorrower(userTemp);
//                                accessingList.add(toolsNew);
//                            }
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
                                tools.setToolState(1);
                                takeNumber++;
                                tools.setBorrower(userTemp);
                                accessingList.add(tools);
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

                        if (operationType == 0) {
                            if (saveNumber > 0) {
                                accessingDialog.findViewById(R.id.dialog_accessing_sure).setEnabled(false);
                                dialog_accessing_reopen_error_tv.setText("本次操作只允许取出！");
                            } else {
                                dialog_accessing_reopen_error_tv.setVisibility(View.GONE);
                            }
                        } else if (operationType == 1) {
                            if (takeNumber > 0) {
                                accessingDialog.findViewById(R.id.dialog_accessing_sure).setEnabled(false);
                                dialog_accessing_reopen_error_tv.setText("本次操作只允许存入！");
                            } else {
                                dialog_accessing_reopen_error_tv.setVisibility(View.GONE);
                            }
                        }

                    }
                } else {
                    accessClear();

                    accessingFullDialog.dismiss();

                    showToast("读卡器离线，本次存取操作无效！");
                }
                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_accessing);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_accessing);
        binding.setOnClickListener(this);

        operationType = getIntent().getExtras().getInt("OperationType");
        if (operationType == 0){
            binding.accessingToolbar.setTitle("取出操作");
        }else {
            binding.accessingToolbar.setTitle("存入操作");
        }

        setSupportActionBar(binding.accessingToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mHandler = new MHandler(this);
        cellNumber = getIntent().getExtras().getInt("CellNumber");

        cabinet = CabinetService.getInstance().queryEq(cellNumber);



        userTemp = spUtil.getString(SharedPreferencesUtil.Key.UserTemp, "");

        init();
    }

    private void init() {
        DoorSerialOperation.getInstance().onDoorListener(doorListener);
//        DoorSerialOperation.getInstance().startCheckBoxDoorState(cabinet.getTargetAddress());

        accessingList = new ArrayList<>();

        antennaNumberList = new ArrayList<>();
        LogUtil.getInstance().LogPrint(cabinet.getAntennaNumber());
        String[] antennaNumberStr = cabinet.getAntennaNumber().split(",");
        for (String s : antennaNumberStr) {
            antennaNumberList.add(Integer.valueOf(s));
            LogUtil.getInstance().LogPrint(s);
        }

        binding.accessingBoxNameTv.setText(cabinet.getBoxName());

        toolsList = ToolsService.getInstance().queryEq(cabinet.getCellNumber(), 0);
        mAdapter = new ToolsAdapter(this, toolsList);
        binding.accessingLv.setAdapter(mAdapter);

        binding.accessingToolNumberTv.setText("本柜共有：" + (toolsList == null ? 0 : toolsList.size()) + "件工具");

        NettyServerParsingLibrary.getInstance().processor.onInventoryListener(inventoryListener);

        if (getIntent().getExtras().getBoolean("ImmediatelyOpen")){
            LogUtil.getInstance().LogPrint("点击开启" + cabinet.getBoxName());
            DoorSerialOperation.getInstance().send(new DoorSendInfo(cabinet.getTargetAddress(), cabinet.getSourceAddress(), cabinet.getLockNumber()));
            DoorSerialOperation.getInstance().startCheckBoxDoorState(cabinet.getTargetAddress());
        }
    }

    protected void countDownTimerOnTick(long millisUntilFinished) {
        binding.accessingCountdownTv.setText(String.valueOf(millisUntilFinished));
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.accessing_open_btn:
                LogUtil.getInstance().LogPrint("点击开启" + cabinet.getBoxName());
                DoorSerialOperation.getInstance().send(new DoorSendInfo(cabinet.getTargetAddress(), cabinet.getSourceAddress(), cabinet.getLockNumber()));
                DoorSerialOperation.getInstance().startCheckBoxDoorState(cabinet.getTargetAddress());
                break;
            case R.id.dialog_accessing_reopen:
                accessClear();

                accessingFullDialog.dismiss();
                DoorSerialOperation.getInstance().send(new DoorSendInfo(cabinet.getTargetAddress(), cabinet.getSourceAddress(), cabinet.getLockNumber()));
                DoorSerialOperation.getInstance().startCheckBoxDoorState(cabinet.getTargetAddress());
                break;
            case R.id.dialog_accessing_sure:
                ToolsService.getInstance().insertOrUpdate(accessingList);

                accessClear();

                accessingFullDialog.dismiss();
                timerStart();

                toolsList = ToolsService.getInstance().queryEq(cabinet.getCellNumber(), 0);
                mAdapter.setList(toolsList);
                mAdapter.notifyDataSetChanged();

                binding.accessingToolNumberTv.setText("本柜共有：" + (toolsList == null ? 0 : toolsList.size()) + "件工具");


                break;
        }
    }

    private static class MHandler extends Handler {
        private final WeakReference<AccessingActivity> accessingActivityWeakReference;

        MHandler(AccessingActivity accessingActivity) {
            super();
            accessingActivityWeakReference = new WeakReference<>(accessingActivity);
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
        DoorSerialOperation.getInstance().onDoorListener(null);
        DoorSerialOperation.getInstance().startCheckBoxDoorState(-1);
        NettyServerParsingLibrary.getInstance().processor.onInventoryListener(null);
        super.onDestroy();
    }

    private void accessClear() {
        openDooring = 0;
        antennaNumberPosition = 0;
        accessingList.clear();
        inventoryList.clear();
    }

}
