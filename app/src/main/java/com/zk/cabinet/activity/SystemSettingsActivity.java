package com.zk.cabinet.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.zk.cabinet.R;
import com.zk.cabinet.databinding.ActivitySystemSettingsBinding;
import com.zk.cabinet.db.CabinetService;
import com.zk.cabinet.util.RegularExpressionUtil;
import com.zk.cabinet.util.SharedPreferencesUtil.Key;
import com.zk.cabinet.util.SharedPreferencesUtil.Record;
import com.zk.cabinet.util.SoundPoolUtil;
import com.zk.cabinet.view.TimeOffAppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SystemSettingsActivity extends TimeOffAppCompatActivity implements View.OnClickListener {
    private ActivitySystemSettingsBinding binding;

    private String mDeviceId, mPlatformServiceIp;
    private int mPlatformServicePort;

    private View mDialogView;
    private AutoCompleteTextView dialog_universal_tv;

    private String mTemp;

    private EditText dialog_ip_port_set_ip_edt, dialog_ip_port_set_port_edt;
    private TextView dialog_reader_settings_tv;

    private String[] mCountdownItems, mNumberBoxesItems, mAlarmTimeItems;
    private int[] mIntCountdownItems, mIntAlarmTimeItems;
    private int mCountdownItemSelected, mNumberBoxesItemSelected, mAlarmTimeItemSelected;

    private List<String> mReaderService;

    private Boolean beepSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_system_settings);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_system_settings);
        binding.setOnClickListener(this);
        setSupportActionBar(binding.systemSettingsToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        init();
    }

    private void init() {

        mDeviceId = spUtil.getString(Key.DeviceId, getResources().getString(R.string.null_prompt));
        mPlatformServiceIp = spUtil.getString(Key.PlatformServiceIp,
                getResources().getString(R.string.null_prompt));
        mPlatformServicePort = spUtil.getInt(Key.PlatformServicePort, -1);

        mIntAlarmTimeItems = new int[]{0, 1, 2, 5, 10, 15};
        mAlarmTimeItems = getResources().getStringArray(R.array.dialog_not_closed_door_alarm_time_array);
        for (int i = 0; i < mIntAlarmTimeItems.length; i++) {
            if (mAlarmTimeItemSelected == mIntAlarmTimeItems[i]) {
                mAlarmTimeItemSelected = i;
                break;
            }
        }

        mCountdownItems = getResources().getStringArray(R.array.dialog_countdown_array);
        mIntCountdownItems = new int[]{15, 30, 1 * 60, 2 * 60, 5 * 60, 10 * 60};
        for (int i = 0; i < mIntCountdownItems.length; i++) {
            if (mIntCountdownItems[i] == mCountdown) {
                mCountdownItemSelected = i;
                break;
            }
        }

        mReaderService = new ArrayList<>();
        if (!spUtil.getString(Key.ReaderServiceIpPort, "").equals("")) {
            String[] ReaderServiceStr = spUtil.getString(Key.ReaderServiceIpPort, "").split(",");
            for (String s : ReaderServiceStr) {
                mReaderService.add(s);
            }
        }

        mNumberBoxesItemSelected = spUtil.getInt(Key.NumberOfBoxes, 10);
        mNumberBoxesItems = getResources().getStringArray(R.array.dialog_number_of_boxes_array);

        binding.systemSettingDeviceIdSb.setCaptionText(mDeviceId);
        binding.systemSettingUnitNumberSb.setCaptionText(
                spUtil.getString(Key.UnitNumber, getResources().getString(R.string.null_prompt)));
        binding.systemSettingUnitAddressSb.setCaptionText(
                spUtil.getString(Key.UnitAddress, getResources().getString(R.string.null_prompt)));
        binding.systemSettingPlatformServiceIpSb.setCaptionText(mPlatformServiceIp.equals(
                getResources().getString(R.string.null_prompt)) ?
                getResources().getString(R.string.null_prompt) :
                mPlatformServiceIp + ":" + mPlatformServicePort);
        binding.systemSettingReaderDeviceIdSb.setCaptionText(spUtil.getString(Key.ReaderServiceIpPort,
                getResources().getString(R.string.null_prompt)));

        binding.systemSettingAppVersionSb.setCaptionText(getAppVersionName(getApplicationContext()));

        binding.systemSettingNumberOfBoxesSb.setCaptionText(mNumberBoxesItems[mNumberBoxesItemSelected]);

        binding.systemSettingNotClosedDoorAlarmTimeSb.setCaptionText("箱门未关" + mAlarmTimeItems[mAlarmTimeItemSelected] + "报警");
        if (mCountdown < 60) {
            binding.systemSettingCountdownSb.setCaptionText("无人操作" + mCountdown + "秒后自动返回主界面");
        } else {
            binding.systemSettingCountdownSb.setCaptionText("无人操作" + mIntCountdownItems[mCountdownItemSelected] / 60 + "分钟后自动返回主界面");
        }

        beepSound = spUtil.getBoolean(Key.BeepSound, true);
        binding.systemSettingSoundSwitchSb.setChecked(beepSound);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.system_setting_device_id_sb:
                timerCancel();
                mDialogView = getLayoutInflater().inflate(R.layout.dialog_universal_edt, null);
                dialog_universal_tv = mDialogView.findViewById(R.id.dialog_universal_tv);
                dialog_universal_tv.setText(mDeviceId.equals(getResources().getString(R.string.null_prompt)) ?
                        getResources().getString(R.string.air) : mDeviceId);
                dialog_universal_tv.setInputType(InputType.TYPE_CLASS_NUMBER);
                dialog_universal_tv.setSelection(mDeviceId.equals(getResources().getString(R.string.null_prompt)) ?
                        0 : mDeviceId.length());
                new AlertDialog.Builder(SystemSettingsActivity.this)
                        .setView(mDialogView)
                        .setTitle(getString(R.string.device_id))
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                timerStart();
                            }
                        })
                        .setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                timerStart();
                                String deviceId = dialog_universal_tv.getText().toString().trim();
                                if (!TextUtils.isEmpty(deviceId) &&
                                        RegularExpressionUtil.isNumber(deviceId)) {
                                    mDeviceId = deviceId;
                                    spUtil.applyValue(new Record(Key.DeviceId, mDeviceId));
                                    binding.systemSettingDeviceIdSb.setCaptionText(mDeviceId);
                                } else {
                                    showToast(getText(R.string.misconfiguration));
                                }
                            }
                        })
                        .setCancelable(false)
                        .show();
                break;
            case R.id.system_setting_unit_number_sb:
                timerCancel();
                mTemp = spUtil.getString(Key.UnitNumber, getResources().getString(R.string.air));
                mDialogView = getLayoutInflater().inflate(R.layout.dialog_universal_edt, null);
                dialog_universal_tv = mDialogView.findViewById(R.id.dialog_universal_tv);
                dialog_universal_tv.setText(mTemp);
                dialog_universal_tv.setInputType(InputType.TYPE_CLASS_NUMBER);
                dialog_universal_tv.setSelection(mTemp.length());
                new AlertDialog.Builder(SystemSettingsActivity.this)
                        .setView(mDialogView)
                        .setTitle(getString(R.string.unit_number))
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                timerStart();
                            }
                        })
                        .setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                timerStart();
                                mTemp = dialog_universal_tv.getText().toString();
                                if (!TextUtils.isEmpty(mTemp) && mTemp.length() < 20) {
                                    spUtil.applyValue(new Record(Key.UnitNumber, mTemp));

                                    binding.systemSettingUnitNumberSb.setCaptionText(mTemp);
                                } else {
                                    showToast(getText(R.string.misconfiguration));
                                }
                            }
                        })
                        .setCancelable(false)
                        .show();
                break;
            case R.id.system_setting_unit_address_sb:
                timerCancel();
                mTemp = spUtil.getString(Key.UnitAddress, getResources().getString(R.string.air));
                mDialogView = getLayoutInflater().inflate(R.layout.dialog_universal_edt_1, null);
                dialog_universal_tv = mDialogView.findViewById(R.id.dialog_universal_tv);
                dialog_universal_tv.setText(mTemp);
//                    dialog_universal_tv.setInputType(InputType.TYPE_NULL);
                dialog_universal_tv.setSelection(mTemp.length());
                new AlertDialog.Builder(SystemSettingsActivity.this)
                        .setView(mDialogView)
                        .setTitle(getString(R.string.unit_address))
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                timerStart();
                            }
                        })
                        .setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                timerStart();
                                mTemp = dialog_universal_tv.getText().toString();

                                spUtil.applyValue(new Record(Key.UnitAddress, mTemp));

                                binding.systemSettingUnitAddressSb.setCaptionText(mTemp);
                            }
                        })
                        .setCancelable(false)
                        .show();
                break;
            case R.id.system_setting_number_of_boxes_sb:
                if (mReaderService.size() > 0) {
                    new AlertDialog.Builder(SystemSettingsActivity.this)
                            .setTitle(getString(R.string.number_of_boxes))
                            .setSingleChoiceItems(mNumberBoxesItems, mNumberBoxesItemSelected, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (mNumberBoxesItemSelected != i) {
                                        mNumberBoxesItemSelected = i;
                                        binding.systemSettingNumberOfBoxesSb.setCaptionText(mNumberBoxesItems[mNumberBoxesItemSelected]);

                                        spUtil.applyValue(new Record(Key.NumberOfBoxes, mNumberBoxesItemSelected));

                                        CabinetService.getInstance().deleteAll();
                                        if (mNumberBoxesItemSelected != 10) {
                                            CabinetService.getInstance().buildMain();
                                            for (int j = 0; j < (i + 1); j++) {
                                                CabinetService.getInstance().buildDeputy(j);
                                            }
                                        } else {
                                            CabinetService.getInstance().buildTest();
                                        }
                                    }
                                    dialogInterface.dismiss();

                                    startActivity(new Intent(SystemSettingsActivity.this, CabinetSetActivity.class));
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel), null)
                            .show();
                } else {
                    showToast(getText(R.string.please_configure_reader_service_first));
                }
                break;
            case R.id.system_setting_not_closed_door_alarm_time_sb:
                new AlertDialog.Builder(SystemSettingsActivity.this)
                        .setTitle(getString(R.string.not_closed_door_alarm_time))
                        .setSingleChoiceItems(mAlarmTimeItems, mAlarmTimeItemSelected, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mAlarmTimeItemSelected = i;
                                spUtil.applyValue(new Record(Key.NotClosedDoorAlarmTime, mIntAlarmTimeItems[mAlarmTimeItemSelected]));
                                binding.systemSettingNotClosedDoorAlarmTimeSb.setCaptionText("箱门未关" + mAlarmTimeItems[mAlarmTimeItemSelected] + "报警");
                                dialogInterface.dismiss();

                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show();

                break;
            case R.id.system_setting_countdown_sb:
                new AlertDialog.Builder(SystemSettingsActivity.this)
                        .setTitle(getString(R.string.countdown))
                        .setSingleChoiceItems(mCountdownItems, mCountdownItemSelected, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mCountdownItemSelected = i;
                                mCountdown = mIntCountdownItems[i];
                                spUtil.applyValue(new Record(Key.Countdown, mCountdown));
                                if (i < 2) {
                                    binding.systemSettingCountdownSb.setCaptionText("无人操作" + mCountdown + "秒后自动返回主界面");
                                } else {
                                    binding.systemSettingCountdownSb.setCaptionText("无人操作" + mCountdown / 60 + "分钟后自动返回主界面");
                                }
                                initTime();
                                timerStart();

                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show();
                break;
            case R.id.system_setting_platform_service_ip_sb:
                timerCancel();
                View viewPlatformServiceIP = getLayoutInflater().inflate(R.layout.dialog_ip_port_settings, null);
                dialog_ip_port_set_ip_edt = viewPlatformServiceIP.findViewById(R.id.dialog_ip_port_set_ip_edt);
                dialog_ip_port_set_port_edt = viewPlatformServiceIP.findViewById(R.id.dialog_ip_port_set_port_edt);
                dialog_ip_port_set_ip_edt.setText(mPlatformServiceIp.equals(getResources().getString(R.string.null_prompt)) ?
                        getResources().getString(R.string.air) : mPlatformServiceIp);
                dialog_ip_port_set_port_edt.setText(mPlatformServicePort == -1 ?
                        getResources().getString(R.string.air) : String.valueOf(mPlatformServicePort));
                new AlertDialog.Builder(SystemSettingsActivity.this)
                        .setView(viewPlatformServiceIP)
                        .setTitle(getString(R.string.platform_service_ip))
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                timerStart();
                            }
                        })
                        .setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                timerStart();
                                String platformServiceIPStr = dialog_ip_port_set_ip_edt.getText().toString();
                                String platformServicePortStr = dialog_ip_port_set_port_edt.getText().toString();

                                if (!TextUtils.isEmpty(platformServiceIPStr) &&
                                        !TextUtils.isEmpty(platformServicePortStr) &&
                                        RegularExpressionUtil.isIp(platformServiceIPStr) &&
                                        RegularExpressionUtil.isNumber(platformServicePortStr) &&
                                        platformServicePortStr.length() < 6) {
                                    int platformServicePortInt = Integer.parseInt(platformServicePortStr);
                                    if (!platformServiceIPStr.equals(mPlatformServiceIp) ||
                                            platformServicePortInt != mPlatformServicePort) {
                                        mPlatformServiceIp = platformServiceIPStr;
                                        mPlatformServicePort = platformServicePortInt;
                                        spUtil.applyValue(new Record(Key.PlatformServiceIp, mPlatformServiceIp));
                                        spUtil.applyValue(new Record(Key.PlatformServicePort, mPlatformServicePort));
                                        binding.systemSettingPlatformServiceIpSb.setCaptionText(mPlatformServiceIp + ":" + mPlatformServicePort);

                                    }
                                } else
                                    showToast(getText(R.string.misconfiguration));
                            }
                        })
                        .setCancelable(false)
                        .show();
                break;
            case R.id.system_setting_reader_device_id_sb:
                timerCancel();
                View viewReaderServiceIP = getLayoutInflater().inflate(R.layout.dialog_reader_settings, null);
                dialog_ip_port_set_ip_edt = viewReaderServiceIP.findViewById(R.id.dialog_reader_settings_ip_edt);
                dialog_reader_settings_tv = viewReaderServiceIP.findViewById(R.id.dialog_reader_settings_tv);
                dialog_reader_settings_tv.setText("");
                for (String s : mReaderService) {
                    dialog_reader_settings_tv.append(s + "\n");
                }
                viewReaderServiceIP.findViewById(R.id.dialog_reader_settings_delete_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String readerIPStr = dialog_ip_port_set_ip_edt.getText().toString();
                        if (mReaderService != null &&
                                !TextUtils.isEmpty(readerIPStr)) {
                            if (mReaderService.contains(readerIPStr)) {
                                mReaderService.remove(readerIPStr);
                                dialog_reader_settings_tv.setText("");
                                for (String s : mReaderService) {
                                    dialog_reader_settings_tv.append(s + "\n");
                                }
                            } else {
                                showToast(getText(R.string.misconfiguration));
                            }
                        } else {
                            showToast(getText(R.string.misconfiguration));
                        }
                    }
                });
                viewReaderServiceIP.findViewById(R.id.dialog_reader_settings_add_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String readerIPStr = dialog_ip_port_set_ip_edt.getText().toString();
                        if (mReaderService != null &&
                                !TextUtils.isEmpty(readerIPStr)) {
                            if (!mReaderService.contains(readerIPStr)) {
                                mReaderService.add(readerIPStr);
                                dialog_reader_settings_tv.setText("");
                                for (String s : mReaderService) {
                                    dialog_reader_settings_tv.append(s + "\n");
                                }
                            } else {
                                showToast(getText(R.string.misconfiguration));
                            }
                        } else {
                            showToast(getText(R.string.misconfiguration));
                        }
                    }
                });
                new AlertDialog.Builder(SystemSettingsActivity.this)
                        .setView(viewReaderServiceIP)
                        .setTitle(getString(R.string.reader_device_id))
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                timerStart();
                            }
                        })
                        .setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                timerStart();
                                StringBuffer temp = new StringBuffer();
                                for (int k = 0; k < mReaderService.size(); k++) {
                                    if (k == 0) {
                                        temp.append(mReaderService.get(k));
                                    } else {
                                        temp.append("," + mReaderService.get(k));
                                    }

                                }
                                spUtil.applyValue(new Record(Key.ReaderServiceIpPort, temp.toString()));

                                binding.systemSettingReaderDeviceIdSb.setCaptionText(temp.toString());
                            }
                        })
                        .setCancelable(false)
                        .show();
                break;
            case R.id.system_setting_app_version_sb:
                break;
            case R.id.system_setting_display_sb:
                startActivity(new Intent(Settings.ACTION_SETTINGS));
                break;
            case R.id.system_setting_file_manager_sb:
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName cn = new ComponentName("com.softwinner.TvdFileManager",
                        "com.softwinner.TvdFileManager.MainUI");
                intent.setComponent(cn);
                startActivity(intent);
                break;
            case R.id.system_setting_sound_switch_sb:
                if (beepSound){
                    beepSound = false;
                } else {
                    beepSound = true;
                }
                SoundPoolUtil.getInstance().setOpen(beepSound);
                binding.systemSettingSoundSwitchSb.setChecked(beepSound);
                spUtil.applyValue(new Record( Key.BeepSound, beepSound));
                break;
        }
    }

    @Override
    protected void countDownTimerOnTick(long millisUntilFinished) {
        binding.systemSettingsCountdownTv.setText(String.valueOf(millisUntilFinished));
    }

    private String getAppVersionName(Context context) {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName + "  -  " + pi.versionCode;
//            versioncode = pi.versionCode;
//            if (versionName == null || versionName.length() <= 0) {
//                return "";
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
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
}
