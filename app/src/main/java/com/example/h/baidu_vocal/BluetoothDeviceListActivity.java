package com.example.h.baidu_vocal;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class BluetoothDeviceListActivity extends Activity {

    public static String EXTRA_DEVICE_ADDRESS = "deviceAddress";
    private ArrayAdapter<String> listAdapter;
    private BluetoothAdapter bluetoothAdapter;

    /**
     * 广播接收者
     * 接收发现蓝牙设备和蓝牙扫描设备结束的广播
     */
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();//获取蓝牙设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {  //发现设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                assert device != null;
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {  //如果设备未绑定
                    listAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {  //扫描设备结束
                if (listAdapter.getCount() == 0) {  //没有设备
                    Toast.makeText(BluetoothDeviceListActivity.this, "没有设备",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        //注册广播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(bluetoothReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(bluetoothReceiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        listAdapter = new ArrayAdapter<>(this, R.layout.device_name);
        ListView lv_device = findViewById(R.id.listView);
        Button bt_find = findViewById(R.id.bt_find);  //扫描设备
        lv_device.setAdapter(listAdapter);

        printDevice();

        bt_find.setOnClickListener(arg0 -> {
            bluetoothAdapter.startDiscovery();   //开始扫描
        });

        //选择连接设备
        lv_device.setOnItemClickListener((arg0, v, arg2, arg3) -> {
            String info = ((TextView) v).getText().toString();
            if (info.equals("没有已配对设备")) {
                Toast.makeText(getApplicationContext(), "没有已配对设备", Toast.LENGTH_LONG)
                        .show();
            } else {
                String address = info.substring(info.length() - 17);   //获取蓝牙设备地址

                Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);   //将地址装入EXTRA_DEVICE_ADDRESS
                setResult(Activity.RESULT_OK, intent); //将地址传送回MainActivity
                finish();
            }
        });

    }

    /**
     * 打印已配对设备
     */
    public void printDevice() {
        //打印出已配对的设备
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                listAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            listAdapter.add("没有已配对设备");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
        this.unregisterReceiver(bluetoothReceiver);
    }
}
