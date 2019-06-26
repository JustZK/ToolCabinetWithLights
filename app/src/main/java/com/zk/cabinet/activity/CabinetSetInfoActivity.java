package com.zk.cabinet.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.zk.cabinet.R;
import com.zk.cabinet.bean.Cabinet;
import com.zk.cabinet.databinding.ActivityCabinetSetInfoBinding;
import com.zk.cabinet.db.CabinetService;
import com.zk.cabinet.util.LogUtil;
import com.zk.cabinet.util.SharedPreferencesUtil;
import com.zk.cabinet.util.SharedPreferencesUtil.Key;

import java.util.Objects;

public class CabinetSetInfoActivity extends AppCompatActivity {
    private ActivityCabinetSetInfoBinding binding;
    private Cabinet cabinet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_cabinet_set_info);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cabinet_set_info);
        setSupportActionBar(binding.cabinetSetInfoToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        init();
    }

    private void init() {
        int cellNumber = getIntent().getIntExtra("CellNumber", 1);
        cabinet = CabinetService.getInstance().queryEq(cellNumber);

        String[] readerServiceIpPorts = SharedPreferencesUtil.getInstance().getString(Key.ReaderServiceIpPort, "").split(",");
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, readerServiceIpPorts);
        binding.cabinetSetInfoReaderIpSpinner.setAdapter(spinnerAdapter);

        binding.cabinetSetInfoCellNumberTv.setText(String.valueOf(cellNumber));
        binding.cabinetSetInfoBoxNameEdt.setText(cabinet.getBoxName() == null ? "" : cabinet.getBoxName());
        binding.cabinetSetInfoTargetAddressEdt.setText(String.valueOf(cabinet.getTargetAddress()));
        binding.cabinetSetInfoTargetAddressForLightEdt.setText(String.valueOf(cabinet.getTargetAddressForLight()));
        binding.cabinetSetInfoLockNumberEdt.setText(String.valueOf(cabinet.getLockNumber()));
        binding.cabinetSetInfoAntennaNumberEdt.setText(cabinet.getAntennaNumber() == null ? "" : cabinet.getAntennaNumber());
        for (int i = 0; i < readerServiceIpPorts.length; i++) {
            if (readerServiceIpPorts[i].equals(String.valueOf(cabinet.getReaderDeviceID()))) {
                binding.cabinetSetInfoReaderIpSpinner.setSelection(i);
                break;
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                String boxName = binding.cabinetSetInfoBoxNameEdt.getText().toString().trim();
                String targetAddressStr = binding.cabinetSetInfoTargetAddressEdt.getText().toString().trim();
                String targetAddressForLightStr = binding.cabinetSetInfoTargetAddressForLightEdt.getText().toString().trim();
                String lockNumberStr = binding.cabinetSetInfoLockNumberEdt.getText().toString().trim();
                String readerIp = binding.cabinetSetInfoReaderIpSpinner.getSelectedItem().toString();
                String antennaNumber = binding.cabinetSetInfoAntennaNumberEdt.getText().toString().trim();

                if (!TextUtils.isEmpty(boxName) &&
                        !TextUtils.isEmpty(targetAddressStr) &&
                        !TextUtils.isEmpty(targetAddressForLightStr) &&
                        !TextUtils.isEmpty(lockNumberStr) &&
                        !TextUtils.isEmpty(readerIp) &&
                        !TextUtils.isEmpty(antennaNumber) &&
                        Integer.parseInt(lockNumberStr) > 0 &&
                        Integer.parseInt(lockNumberStr) < 17) {
                    cabinet.setBoxName(boxName);
                    cabinet.setTargetAddress(Integer.parseInt(targetAddressStr));
                    cabinet.setTargetAddressForLight(Integer.parseInt(targetAddressForLightStr));
                    cabinet.setLockNumber(Integer.parseInt(lockNumberStr));
                    cabinet.setReaderDeviceID(Integer.parseInt(readerIp));
                    cabinet.setAntennaNumber(antennaNumber);
                    CabinetService.getInstance().update(cabinet);
                    finish();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.misconfiguration), Toast.LENGTH_SHORT).show();
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        LogUtil.getInstance().d("setResult(RESULT_OK)");
        this.setResult(RESULT_OK);
        super.finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ){
            Toast.makeText(this, "禁止使用回退建！", Toast.LENGTH_SHORT).show();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
