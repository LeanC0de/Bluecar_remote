package  com.example.h.baidu_vocal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.asrwakeup3.core.mini.ActivityMiniRecog;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManagerFactory;

import java.util.Objects;


@SuppressWarnings("ALL")
public class MainActivity extends ActivityMiniRecog implements EventListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int BT_ENABLE_TRUE = 123456;
    public static final int BT_ENABLE_FALSE = 654321;
    public static BluetoothUtils bluetoothUtils;

    private boolean isBTAvailable;
    // Layout Views


    // Member object for the chat services
    //private Resources resources;
    private SoundPool sp;//声明一个SoundPool
    private int music;//定义一个整型用load（）；来设置suondID
    private AudioManager mAudioManager;
    private float streamVolumeCurrent, streamVolumeMax, volume;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothUtils = new BluetoothUtils();
        isBTAvailable = bluetoothUtils.isBlueToothAvailable();
        if (!isBTAvailable) {
            Toast.makeText(this, "蓝牙是不可用的", Toast.LENGTH_LONG).show();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持亮屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏


        sp = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);//第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
        music = sp.load(this, R.raw.about, 1); //把你的声音素材放到res/raw里，第2个参数即为资源文件，第3个为音乐的优先级
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);//让音量键固定为媒体音量控制


        initView();
        initPermission();
        // 基于sdk集成1.1 初始化EventManager对象
        asr = EventManagerFactory.create(this, "asr");
        // 基于sdk集成1.3 注册自己的输出事件类
        asr.registerListener(this); //  EventListener 中 onEvent方法
        if (enableOffline) {
            loadOfflineEngine(); // 测试离线命令词请开启, 测试 ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH 参数时开启
        }
        final Button help = findViewById(R.id.button_help);
        help.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HelpActivity.class)));

        final Button yuyin = findViewById(R.id.button_yuyin);
        yuyin.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                start();
                Log.v("读取数据", "");
                yuyin.setText("正在识别");
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                stop();
                yuyin.setText("按下说话");
            }
            return false;
        });
        handler_vocal = new Handler() {
            @SuppressLint("SetTextI18n")//不能直接使用文本给TextView.setText()，具体参照HardcodedText的说明。
            // 代码中可以使用@SuppressLint("SetTextI18n") 禁用lint的这项检查。
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) { //开机校验更新回传
                    Log.v("handler_vocal", msg.obj.toString());
                    Log.v("识别结果", msg.obj.toString());
                    Toast.makeText(MainActivity.this, "识别结果：" + msg.obj.toString(), Toast.LENGTH_LONG).show();
                    //这里就是得到的数据结果。msg.obj.toString() 字符串型  然后你可以干你的指令控制了


                    if (msg.obj.toString().equals("前进。")) {
                        bluetoothUtils.write("8");
                    } else if (msg.obj.toString().equals("后退。")) {
                        bluetoothUtils.write("2");
                    } else if (msg.obj.toString().equals("左转。")) {
                        bluetoothUtils.write("4");
                    } else if (msg.obj.toString().equals("右转。")) {
                        bluetoothUtils.write("6");
                    } else if (msg.obj.toString().equals("停止。")) {
                        bluetoothUtils.write("5");
                    } else if (msg.obj.toString().equals("左前。")) {
                        bluetoothUtils.write("7");
                    } else if (msg.obj.toString().equals("右前。")) {
                        bluetoothUtils.write("9");
                    } else if (msg.obj.toString().equals("右后。")) {
                        bluetoothUtils.write("3");
                    } else if (msg.obj.toString().equals("左后。")) {
                        bluetoothUtils.write("1");
                    }
                }
            }
        };

        final Button garviey = findViewById(R.id.button_grivaty);
        garviey.setOnClickListener(v -> {
            bluetoothUtils.write("G");
            startActivity(new Intent(MainActivity.this, GravityActivity.class));
        });

        findViewById(R.id.bt_bluetooth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!isBTAvailable) {
                    Toast.makeText(MainActivity.this, "蓝牙是不可用的", Toast.LENGTH_LONG).show();
                } else {
                    String[] items = new String[]{"打开蓝牙", "连接蓝牙", "断开蓝牙"};
                    AlertDialog dialog;
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this).setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i) {
                                case 0:   //打开蓝牙
                                    bluetoothUtils.openBlueTooth(MainActivity.this);
                                    break; //可选
                                case 1:  //连接蓝牙
                                    if (!isBTAvailable) {
                                        Toast.makeText(MainActivity.this, "蓝牙是不可用的", Toast.LENGTH_LONG)
                                                .show();
                                    } else if (!bluetoothUtils.getBluetoothAdapter().isEnabled()) {
                                        Toast.makeText(MainActivity.this, "未打开蓝牙", Toast.LENGTH_SHORT)
                                                .show();
                                    } else {
                                        Intent serverIntent = new Intent(MainActivity.this,
                                                BluetoothDeviceListActivity.class);   //跳转到蓝牙扫描连接页面
                                        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                                    }
                                    break; //可选
                                case 2:// 断开连接
                                    if (!bluetoothUtils.isConnected()) {
                                        Toast.makeText(MainActivity.this, "无连接", Toast.LENGTH_SHORT)
                                                .show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "已断开连接", Toast.LENGTH_SHORT)
                                                .show();
                                        bluetoothUtils.cancelConnect();
                                    }
                                    break;
                                default: //可选
                                    //语句
                            }
                        }
                    });
                    dialog = builder.create();
                    dialog.show();
                }
            }
        });


        final TextView tv_mode = findViewById(R.id.textView_Mode);

        final Button bt_mode = findViewById(R.id.Button_Mode);
        bt_mode.setOnClickListener(v ->

        {
            String SEND_BYTE;
            if (tv_mode.getText().equals("遥控模式")) {
                SEND_BYTE = "A";
                tv_mode.setText("自动模式");
            } else {
                SEND_BYTE = "M";
                tv_mode.setText("遥控模式");
            }
            bluetoothUtils.write(SEND_BYTE);
        });

        final Button bt_up = findViewById(R.id.buttonup);
        bt_up.setOnTouchListener((v, event) ->
        {
            int ea = event.getAction();
            switch (ea) {
                case MotionEvent.ACTION_DOWN:
                    bluetoothUtils.write("8");
                    streamVolumeCurrent = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    streamVolumeMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    volume = streamVolumeCurrent / streamVolumeMax;
                    sp.play(music, volume, volume, 0, 0, 1);
                    bt_up.setBackgroundResource(R.drawable.design_point1);//设置背景
                    break;
                case MotionEvent.ACTION_UP:
                    bt_up.setBackgroundResource(R.drawable.design_point);//设置背景
                    bluetoothUtils.write("5");
                    break;
            }

            return false;
        });

        final Button bt_upleft = findViewById(R.id.buttonupleft);
        bt_upleft.setOnTouchListener((v, event) ->
        {
            int ea = event.getAction();
            switch (ea) {
                case MotionEvent.ACTION_DOWN:
                    bluetoothUtils.write("7");
                    streamVolumeCurrent = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    streamVolumeMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    volume = streamVolumeCurrent / streamVolumeMax;
                    sp.play(music, volume, volume, 0, 0, 1);
                    bt_upleft.setBackgroundResource(R.drawable.design_point1);//设置背景
                    break;
                case MotionEvent.ACTION_UP:
                    bt_upleft.setBackgroundResource(R.drawable.design_point);//设置背景
                    bluetoothUtils.write("5");
                    break;
            }

            return false;
        });

        final Button bt_upright = findViewById(R.id.buttonupright);
        bt_upright.setOnTouchListener((v, event) ->
        {
            int ea = event.getAction();
            switch (ea) {
                case MotionEvent.ACTION_DOWN:
                    bluetoothUtils.write("9");
                    streamVolumeCurrent = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    streamVolumeMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    volume = streamVolumeCurrent / streamVolumeMax;
                    sp.play(music, volume, volume, 0, 0, 1);
                    bt_upright.setBackgroundResource(R.drawable.design_point1);//设置背景
                    break;
                case MotionEvent.ACTION_UP:
                    bt_upright.setBackgroundResource(R.drawable.design_point);//设置背景
                    bluetoothUtils.write("5");
                    break;
            }

            return false;
        });

        final Button bt_downright = findViewById(R.id.buttondownright);
        bt_downright.setOnTouchListener((v, event) ->
        {
            int ea = event.getAction();
            switch (ea) {
                case MotionEvent.ACTION_DOWN:
                    bluetoothUtils.write("3");
                    streamVolumeCurrent = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    streamVolumeMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    volume = streamVolumeCurrent / streamVolumeMax;
                    sp.play(music, volume, volume, 0, 0, 1);
                    bt_downright.setBackgroundResource(R.drawable.design_point1);//设置背景
                    break;
                case MotionEvent.ACTION_UP:
                    bt_downright.setBackgroundResource(R.drawable.design_point);//设置背景
                    bluetoothUtils.write("5");
                    break;
            }

            return false;
        });

        final Button bt_downleft = findViewById(R.id.buttondownleft);
        bt_downleft.setOnTouchListener((v, event) ->
        {
            int ea = event.getAction();
            switch (ea) {
                case MotionEvent.ACTION_DOWN:
                    bluetoothUtils.write("1");
                    streamVolumeCurrent = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    streamVolumeMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    volume = streamVolumeCurrent / streamVolumeMax;
                    sp.play(music, volume, volume, 0, 0, 1);
                    bt_downleft.setBackgroundResource(R.drawable.design_point1);//设置背景
                    break;
                case MotionEvent.ACTION_UP:
                    bt_downleft.setBackgroundResource(R.drawable.design_point);//设置背景
                    bluetoothUtils.write("5");
                    break;
            }

            return false;
        });

        final Button bt_down = findViewById(R.id.buttondown);
        bt_down.setOnTouchListener((v, event) ->

        {
            int ea = event.getAction();
            switch (ea) {
                case MotionEvent.ACTION_DOWN:
                    bluetoothUtils.write("2");
                    streamVolumeCurrent = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    streamVolumeMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    volume = streamVolumeCurrent / streamVolumeMax;
                    sp.play(music, volume, volume, 0, 0, 1);
                    bt_down.setBackgroundResource(R.drawable.design_point1);//设置背景
                    break;
                case MotionEvent.ACTION_UP:
                    bt_down.setBackgroundResource(R.drawable.design_point);//设置背景
                    bluetoothUtils.write("5");
                    break;
            }

            return false;
        });

        final Button bt_left = findViewById(R.id.buttonleft);
        bt_left.setOnTouchListener((v, event) ->

        {
            int ea = event.getAction();
            switch (ea) {
                case MotionEvent.ACTION_DOWN:
                    bluetoothUtils.write("4");
                    streamVolumeCurrent = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    streamVolumeMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    volume = streamVolumeCurrent / streamVolumeMax;
                    sp.play(music, volume, volume, 0, 0, 1);
                    bt_left.setBackgroundResource(R.drawable.design_point1);//设置背景
                    break;
                case MotionEvent.ACTION_UP:
                    bt_left.setBackgroundResource(R.drawable.design_point);//设置背景
                    bluetoothUtils.write("5");
                    break;
            }

            return false;
        });

        final Button bt_right = findViewById(R.id.buttonright);
        bt_right.setOnTouchListener((v, event) ->

        {
            int ea = event.getAction();
            switch (ea) {
                case MotionEvent.ACTION_DOWN:
                    bluetoothUtils.write("6");
                    streamVolumeCurrent = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    streamVolumeMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    volume = streamVolumeCurrent / streamVolumeMax;
                    sp.play(music, volume, volume, 0, 0, 1);
                    bt_right.setBackgroundResource(R.drawable.design_point1);//设置背景
                    break;
                case MotionEvent.ACTION_UP:
                    bt_right.setBackgroundResource(R.drawable.design_point);//设置背景
                    bluetoothUtils.write("5");
                    break;
            }

            return false;
        });
    }


    //获取蓝牙设备名，并进行蓝牙连接

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CONNECT_DEVICE) {
            // 当DeviceListActivity返回与设备连接的消息
            if (resultCode == Activity.RESULT_OK) {
                // 得到链接设备的MAC
                String address = Objects.requireNonNull(data.getExtras()).getString(
                        BluetoothDeviceListActivity.EXTRA_DEVICE_ADDRESS, "");
                // 得到BluetoothDevice对象
                if (!TextUtils.isEmpty(address)) {
                    BluetoothDevice device = bluetoothUtils.getBluetoothAdapter().getRemoteDevice(address);
                    boolean conSt = bluetoothUtils.connectThread(device);
                    if (conSt) {
                        Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.i(TAG, "onBackPressed() : finish()");
        finish();
    }

    private void reset() {
//        setBtEnabled(true);
//        showResult.setText("");
    }


    @Override
    protected void onPause() {
        Log.d(TAG, "onPause() ");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop() ");
        super.onStop();
        reset();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void setBtEnabled(boolean isEnabled) {

    }

}



