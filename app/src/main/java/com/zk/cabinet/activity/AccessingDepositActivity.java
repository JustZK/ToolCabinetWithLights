package com.zk.cabinet.activity;

import android.databinding.DataBindingUtil;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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
import com.zk.cabinet.databinding.ActivityAccessingOutBinding;
import com.zk.cabinet.db.CabinetService;
import com.zk.cabinet.db.ToolsService;
import com.zk.cabinet.netty.server.NettyServerParsingLibrary;
import com.zk.cabinet.serial.door.DoorSerialOperation;
import com.zk.cabinet.serial.light.LightSerialOperation;
import com.zk.cabinet.util.SharedPreferencesUtil;
import com.zk.cabinet.view.CustomProgressDialog;
import com.zk.cabinet.view.FullScreenAlertDialog;
import com.zk.cabinet.view.TimeOffAppCompatActivity;

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

    private String EPC;

    private MHandler mHandler;

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
                            if (!inventorying) {
//                                inventorying = true;
                                DoorSerialOperation.getInstance().startCheckBoxDoorState(-1);
                                LightSerialOperation.getInstance().startCheckLightState(-1);
                                needOpenLightNumbers.clear();//关灯
                                LightSerialOperation.getInstance().send(new LightSendInfo(cabinet.getTargetAddress(),
                                        cabinet.getSourceAddress(), needOpenLightNumbers));

                                openDooring = 0;
                                antennaNumberPosition = 0;
//                                NettyServerParsingLibrary.getInstance().send(new NettySendInfo(
//                                        cabinet.getReaderDeviceID(), 0,
//                                        antennaNumberList.get(antennaNumberPosition), 0));
//                                ProgressDialogShow("正在第1次盘点，请稍后......");
                                showToast(cabinet.getBoxName() + "已经关闭，请点击返回键返回上一层.");

                            } else {
                                showToast(cabinet.getBoxName() + "已经关闭，当前正在盘点中");
                            }
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
                            Tools toolsTemp = ToolsService.getInstance().queryEq(inventoryInfo.getEPC());
                            if (toolsTemp != null) {
                                if (toolsTemp.getToolState() != 0) {
                                    saveNumber++; // 有工具存入

                                    toolsTemp.setCellNumber(cellNumber);
                                    toolsTemp.setToolState(0);
                                    toolsTemp.setBorrower(userTemp);
                                    toolsTemp.setToolLightNumber(needOpenLightNumbers.get(0));
                                    toolsTemp.setSelected(false);
                                    accessingList.add(toolsTemp);
                                }
                            }
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
                            dialog_accessing_reopen_error_tv.setText("本次操作只允许存入！");
                        } else {
                            if (toolsList.size() == 1) {
                                if (EPC.equalsIgnoreCase(toolsList.get(0).getEpc())) {
                                    dialog_accessing_reopen_error_tv.setVisibility(View.GONE);
                                    accessingDialog.findViewById(R.id.dialog_accessing_sure).setEnabled(true);
                                } else {
                                    accessingDialog.findViewById(R.id.dialog_accessing_sure).setEnabled(false);
                                    dialog_accessing_reopen_error_tv.setText("您存入的工具和您准备存入的工具不符！");
                                }
                            } else {
                                accessingDialog.findViewById(R.id.dialog_accessing_sure).setEnabled(false);
                                dialog_accessing_reopen_error_tv.setText("您存入了多个工具！");
                            }
                        }
                    }
                } else {
                    accessClear();
                    showToast("读卡器离线，本次存取操作无效！");
                }
                break;
            case OPEN_LIGHT_RESULT:
                showToast("灯控收到开灯指令");
                break;
            case CHECK_LIGHT_STATE:
                if (!inventorying && openDooring == 1) {
                    needOpenLightNumbers = msg.getData().getIntegerArrayList("LightStateList");
                    needOpenLightNumbers.removeAll(lightNumbers);
                    if (needOpenLightNumbers.size() == 1) {
                        inventorying = true;

                        LightSerialOperation.getInstance().send(new LightSendInfo(cabinet.getTargetAddress(),
                                cabinet.getSourceAddress(), needOpenLightNumbers));

                        antennaNumberPosition = 0;
                        NettyServerParsingLibrary.getInstance().send(new NettySendInfo(
                                cabinet.getReaderDeviceID(), 0,
                                antennaNumberList.get(antennaNumberPosition), 0));
                        showToast(needOpenLightNumbers.get(0) + "号位已经插入，准备盘点.");
                        ProgressDialogShow("正在第1次盘点，请稍后......");

                    } else if (needOpenLightNumbers.size() > 1) {
                        showToast("本次操作只允许归还一件工具");
                    }
                }
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
        userTemp = spUtil.getString(SharedPreferencesUtil.Key.UserTemp, "");
        cellNumber = getIntent().getExtras().getInt("CellNumber");
        cabinet = CabinetService.getInstance().queryEq(cellNumber);
        EPC = getIntent().getExtras().getString("EPC");

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
        toolsList = ToolsService.getInstance().queryEq(cabinet.getCellNumber(), 0);
        if (toolsList == null) toolsList = new ArrayList<>();
        mAdapter = new ToolsAdapter(this, toolsList);
        binding.accessingDepositLv.setAdapter(mAdapter);
        binding.accessingDepositToolNumberTv.setText("本柜共有：" + toolsList.size() + "件工具");

        for (Tools tools : toolsList) {
            lightNumbers.add(tools.getToolLightNumber());
        }

        if (getIntent().getExtras().getBoolean("ImmediatelyOpen")) { //进来就开门
            binding.accessingDepositOpenBtn.setVisibility(View.INVISIBLE);
            DoorSerialOperation.getInstance().send(new DoorSendInfo(cabinet.getTargetAddress(),
                    cabinet.getSourceAddress(), cabinet.getLockNumber()));
            DoorSerialOperation.getInstance().startCheckBoxDoorState(cabinet.getTargetAddress());

            LightSerialOperation.getInstance().startCheckLightState(cabinet.getTargetAddressForLight());
        }


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
            case R.id.accessing_open_btn:
                for (Tools tools : toolsList) {
                    if (tools.getSelected()) {
                        lightNumbers.add(tools.getToolLightNumber());
                    }
                }

                DoorSerialOperation.getInstance().send(new DoorSendInfo(cabinet.getTargetAddress(),
                        cabinet.getSourceAddress(), cabinet.getLockNumber()));
                DoorSerialOperation.getInstance().startCheckBoxDoorState(cabinet.getTargetAddress());

                LightSerialOperation.getInstance().startCheckLightState(cabinet.getTargetAddressForLight());
                LightSerialOperation.getInstance().send(new LightSendInfo(cabinet.getTargetAddress(),
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
                ToolsService.getInstance().insertOrUpdate(accessingList);

                accessClear();
                timerStart();

                toolsList = ToolsService.getInstance().queryEq(cabinet.getCellNumber(), 0);
                if (toolsList == null) toolsList = new ArrayList<>();
                mAdapter.setList(toolsList);
                mAdapter.notifyDataSetChanged();
                binding.accessingDepositToolNumberTv.setText("本柜共有：" + toolsList.size() + "件工具");
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
        LightSerialOperation.getInstance().send(new LightSendInfo(cabinet.getTargetAddress(),
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

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (toolsList.get(position).getSelected()) toolsList.get(position).setSelected(false);
            else toolsList.get(position).setSelected(true);
            mAdapter.notifyDataSetChanged();
        }
    };
}