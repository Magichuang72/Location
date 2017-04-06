package com.friendlyarm.SerialPortDemo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.util.Log;
import android.text.Html;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import com.friendlyarm.AndroidSDK.HardwareControler;
import com.friendlyarm.SerialPortDemo.R;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.content.Intent;

public class SerialPortDataProcessView extends Activity {
    private static final String TAG = "SerialPort  ";
    private static final int WIFI = 4;
    private static final int LINKUI = 5;
    private static final int SOCKET = 6;
    private static final int SENDBLUE = 7;
    private static final int SENDZ = 8;
    private static final int SENDALL = 9;
    private byte deviceName = 0x01;
    private TextView fromTextView = null;
    private TextView fromTextView2 = null;
    private TextView fromTextView3 = null;
    private TextView fromTextView4 = null;
    private WifiManager wifiManager = null;
    private EditText RouterName = null;
    private EditText Ip = null;
    private EditText Port = null;
    private TextView SocketShow = null;
    private Button linkButton = null;
    private Button stopButton = null;
    private Button modeButton = null;
    private Button mode2Button = null;
    private Button mode3Button = null;
    private Button idButton = null;
    private Button id2Button = null;
    private Button reButton = null;
    private Button genButton = null;
    private TextView LinkShow = null;
    WifiAutoConnectManager wac;
    private final int MAXLINES = 200;
    private StringBuilder sb = new StringBuilder(256 * MAXLINES);
    private StringBuilder remote = new StringBuilder(256 * MAXLINES);
    private StringBuilder scanBuilder = new StringBuilder(256 * MAXLINES);
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private static List<BluetoothDevice> mDevices = new ArrayList<>();
    private List<Device_Rssi> Devices_show = new ArrayList<>();
    private List<byte[]> buffList = new ArrayList<>();
    private static final int SERIAL_PORT1 = 1;
    private static final int SERIAL_PORT2 = 2;
    private static final int BLUE = 3;
    private static final String EDNAME1 = "SimpleBLEPeripheral1";
    private static final String EDNAME2 = "SimpleBLEPeripheral2";
    private static final String EDNAME3 = "SimpleBLEPeripheral3";
    private static final String EDNAME4 = "SimpleBLEPeripheral4";
    private String devName1 = "/dev/ttyUSB0";
    private String devName2 = "/dev/ttySAC3";
    Socket socket = null;
    private int speed = 115200;
    private int dataBits = 8;
    private int stopBits = 1;
    private int devfd = -1;
    private int devfd2 = -1;

    private final int BUFSIZE = 512;
    private byte[] buf = new byte[BUFSIZE];
    private Timer timer = new Timer();
    private Timer timer2 = new Timer();
    private Timer timer3 = new Timer();
    private Timer timer4 = new Timer();
    private Timer timer5 = new Timer();
    private Timer timer6 = new Timer();
    private boolean socketstate;
    private Button button = null;
    private ScrollView scrollview = null;
    private ScrollView scrollview2 = null;
    private byte timeZ = 0x01;
    private byte timeL = 0x01;
    private byte timeB = 0x01;
    private byte timeW = 0x01;

    private byte coordNum = 0x00;

    private byte map = 0x02;
    private TextView modeShow = null;
    private TextView idShow = null;


    @Override
    public void onDestroy() {
        timer.cancel();
        if (devfd != -1) {
            HardwareControler.close(devfd);
            devfd = -1;
        }
        if (devfd2 != -1) {
            HardwareControler.close(devfd2);
            devfd2 = -1;
        }
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.serialport_dataprocessview_landscape);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.serialport_dataprocessview);
        }
        initDevice();
        initUI();
        openDevice();
    }

    private void initDevice() {
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        wac = new WifiAutoConnectManager(wifiManager);

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "BLE not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth disabled", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


    }


    private void initUI() {
        String winTitle = "----定位实验终端部分程序" + (deviceName);
        setTitle(winTitle);
        fromTextView = (TextView) findViewById(R.id.fromTextView);
        fromTextView2 = (TextView) findViewById(R.id.fromTextView2);
        fromTextView3 = (TextView) findViewById(R.id.fromTextView3);
        fromTextView4 = (TextView) findViewById(R.id.fromTextView4);
        modeShow = (TextView) findViewById(R.id.modeShow);
        idShow = (TextView) findViewById(R.id.idShow);
        RouterName = (EditText) findViewById(R.id.RouterEdit);
        SocketShow = (TextView) findViewById(R.id.SocketShow);
        linkButton = (Button) findViewById(R.id.LinkButton);
        stopButton = (Button) findViewById(R.id.StopButton);
        modeButton = (Button) findViewById(R.id.mapSelect);
        mode2Button = (Button) findViewById(R.id.mapSelect2);
        mode3Button = (Button) findViewById(R.id.mapSelect3);
        reButton = (Button) findViewById(R.id.reset);
        idButton = (Button) findViewById(R.id.idNum);
        id2Button = (Button) findViewById(R.id.idNum2);
        genButton = (Button) findViewById(R.id.gen);
        scrollview = (ScrollView) findViewById(R.id.scroolView);
        scrollview2 = (ScrollView) findViewById(R.id.scroolView2);
        Ip = (EditText) findViewById(R.id.IpText);
        Port = (EditText) findViewById(R.id.PortText);
        LinkShow = (TextView) findViewById(R.id.LinkShow);
        button = (Button) findViewById(R.id.StartButton);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        socketstate = startSocket();

                    }
                }).start();

            }
        });
        stopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (socket != null)
                        socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        linkButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                wac.connect(RouterName.getText().toString(), "",
                        WifiAutoConnectManager.WifiCipherType.WIFICIPHER_NOPASS);
            }
        });
        modeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                map = 0x00;

            }
        });
        mode2Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                map = 0x01;
            }
        });
        mode3Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                map = 0x02;
            }
        });
        idButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                coordNum++;
            }
        });
        id2Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                coordNum--;
            }
        });
        reButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                coordNum = 0x00;
            }
        });
        genButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] end = InitSend2();
                for (int i = 2; i < end.length - 1; i++) end[i] = 0x01;
                sendMessageToSocket(end);
                map = 0x02;
            }
        });
    }

    private void openDevice() {
        devfd = HardwareControler.openSerialPort(devName1, speed, dataBits, stopBits);
        devfd2 = HardwareControler.openSerialPort(devName2, speed, dataBits, stopBits);
        timer.schedule(blue_task, 0, 500);
        timer3.schedule(SendAll, 0, 1000);
        timer3.schedule(wifi_task, 0, 1000);
        timer4.schedule(LinkUi, 0, 500);


        if (devfd >= 0 && devfd2 >= 0) {
            timer2.schedule(serial_task1, 0, 500);
            timer5.schedule(serial_task2, 0, 500);

        } else {

            if (devfd < 0) {
                fromTextView.append("Fail  to open");
                fromTextView.append(devName1);
            }
            if (devfd2 < 0) {
                fromTextView2.append("Fail  to open");
                fromTextView2.append(devName2);
            }
        }
    }


    private Handler handler = new Handler() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SERIAL_PORT1:
                    /*-----------------串口1数据读取-------------------------*/
                    byte send[] = InitSend();
                    if (HardwareControler.select(devfd, 0, 0) == 1) {
                        int retSize = HardwareControler.read(devfd, buf, BUFSIZE);
                        String str = new String(buf, 0, retSize);
                        remote.append(str);
                        fromTextView.append(StoHexString(remote.toString()) + "\n");
                        scrollview.fullScroll(ScrollView.FOCUS_DOWN);
                        remote.delete(0, remote.length());
                        if (retSize > 0 && buf[0] == 0x0a && buf[1] == 0x0d) {
                            send[2] = 0x5a;
                            send[6] = deviceName;
                            send[7] = buf[2];
                            send[8] = buf[3];
                            boolean add = false;
                            for (int i = 0; i < buffList.size(); i++) {
                                byte[] check = buffList.get(i);
                                if (check[7] == send[7]) {
                                    buffList.remove(i);
                                    buffList.add(send);
                                    add = true;
                                    break;
                                }
                            }
                            if (!add) buffList.add(send);

                        }
                    }
                    break;
                case SERIAL_PORT2:
                    /*----------------------------串口2数据读取------------------------------------*/
                    byte send2[] = InitSend();
                    byte sendL[] = InitSend2();
                    if (HardwareControler.select(devfd2, 0, 0) == 1) {
                        int retSize = HardwareControler.read(devfd2, buf, BUFSIZE);
                        String str = new String(buf, 0, retSize);
                        remote.append(str);
                        fromTextView2.append(StoHexString(remote.toString()) + '\n');
                        scrollview2.fullScroll(ScrollView.FOCUS_DOWN);
                        remote.delete(0, remote.length());

                        if (retSize > 0 && buf[0] == 0x0a && buf[1] == 0x0d) {

                            send2[2] = 0x4c;

                            send2[5] = timeL;

                            send2[7] = buf[2];
                            send2[8] = buf[3];
                            sendL[2] = 0x4c;
                            sendL[5] = timeL;
                            sendL[7] = buf[2];
                            sendL[8] = buf[3];
                            //   sendMessageToSocket(send2);
                            sendMessageToSocket(sendL);
                            Log.d(TAG, "handleMessage: " + "LLLLL");
                        }
                        timeL++;

                    }
                    break;
                case BLUE:
                    BluetoothLeScanner mba = mBluetoothAdapter.getBluetoothLeScanner();
                    mba.startScan(new ScanCallback() {
                        /*----------------------------蓝牙扫描--------------------------*/
                        @Override
                        public void onScanResult(int callbackType, ScanResult result) {
                            super.onScanResult(callbackType, result);
                            BluetoothDevice device = result.getDevice();
                            String addr = device.getAddress();
                            String name = device.getName();
                            int rssi = result.getRssi();
                            Device_Rssi dr = new Device_Rssi(addr, name, rssi);

                            if (EDNAME1.equals(name) || EDNAME2.equals(name) || EDNAME3.equals(name) || EDNAME4.equals(name)) {

                                if (mDevices.contains(device)) {
                                    for (Device_Rssi buff : Devices_show) {
                                        if (buff.getId().equals(device.getAddress())) {
                                            buff.setRssi(result.getRssi());
                                        }
                                    }
                                }
                                if (!mDevices.contains(device)) {
                                    Devices_show.add(dr);
                                    mDevices.add(device);
                                    fromTextView3.setText("");
                                }

                            }
                        }

                        @Override
                        public void onBatchScanResults(List<ScanResult> results) {
                            super.onBatchScanResults(results);

                        }

                        @Override
                        public void onScanFailed(int errorCode) {
                            super.onScanFailed(errorCode);

                        }
                    });
                    Log.i("Num of finding devices", String.valueOf(mDevices.size()));
                    break;
                case WIFI:
                    if (scanBuilder != null) {
                        scanBuilder = new StringBuilder();
                    }
                    boolean found = false;
                    wifiManager.startScan();
                    List<android.net.wifi.ScanResult> scanResults = wifiManager.getScanResults();
                    byte[] sendW = InitSend2();
                    for (android.net.wifi.ScanResult scanResult : scanResults) {
                        if (scanResult.SSID.startsWith("TEST")) {
                            byte[] send4 = InitSend();
                            found = true;
                            Log.d(TAG, "handleMessage: " + found);
                            send4[2] = 0x55;
                            sendW[2] = 0x55;
                            send4[5] = timeW;
                            sendW[5] = timeW;
                            send4[6] = deviceName;
                            sendW[6] = deviceName;
                            send4[8] = (byte) scanResult.level;
                            switch (scanResult.SSID) {
                                case "TEST1":
                                    send4[7] = 0x31;
                                    sendW[7] = (byte) scanResult.level;
                                    break;
                                case "TEST2":
                                    send4[7] = 0x32;
                                    sendW[8] = (byte) scanResult.level;
                                    break;
                                case "TEST3":
                                    send4[7] = 0x33;
                                    sendW[9] = (byte) scanResult.level;
                                    break;
                                case "TEST4":
                                    send4[7] = 0x34;
                                    sendW[10] = (byte) scanResult.level;
                                    break;
                            }
                            send4[5] = timeW;


                            //   sendMessageToSocket(send4);
                            scanBuilder.append(scanResult.SSID + "     level=" + scanResult.level + "\n");
                            fromTextView4.setText("");
                        }
                    }
                    sendW[5] = timeW;
                    if (found) {
                        sendMessageToSocket(sendW);
                        Log.d(TAG, "handleMessage: " + "    wwwww   ");
                    }
                    fromTextView4.setText(scanBuilder.toString());
                    timeW++;

                    break;
                case SENDALL:
                    byte[] sendZ = InitSend2();
//                    if (buffList != null) {
//                        for (byte[] sendb : buffList) {
//                            Log.d(TAG, "run: " + Thread.currentThread().getName());
//                            sendb[5] = timeZ;
//                           // sendMessageToSocket(sendb);
//                        }
//                        timeZ++;
//                    }
                    Log.d(TAG, "time " + timeB + timeL + timeZ + timeW);
                    if (buffList != null && buffList.size() >= 2) {
                        for (byte[] sendb : buffList) {
                            switch (sendb[7]) {
                                case 0x31:
                                    sendZ[7] = sendb[8];
                                    break;
                                case 0x32:
                                    sendZ[8] = sendb[8];
                                    break;
                                case 0x33:
                                    sendZ[9] = sendb[8];
                                    break;
                                case 0x34:
                                    sendZ[10] = sendb[8];
                                    break;
                            }
                        }
                        sendZ[2] = 0x5a;
                        sendZ[5] = timeZ;
                        sendZ[6] = deviceName;
                        sendMessageToSocket(sendZ);
                        Log.d(TAG, "handleMessage: " + "ZZZZ");
                        timeZ++;
                    }
                    if (Devices_show != null && Devices_show.size() >= 2) {
                        byte send3[] = InitSend();
                        byte[] sendB = InitSend2();
                        for (Device_Rssi sendbuff : Devices_show) {
                            String name = sendbuff.getName();
                            send3[2] = 0x66;
                            sendB[2] = 0x66;
                            byte id = 0x00;
                            byte brssi = (byte) sendbuff.getRssi();
                            switch (name) {
                                case EDNAME1:
                                    id = 0x31;
                                    sendB[7] = brssi;
                                    break;
                                case EDNAME2:
                                    id = 0x32;
                                    sendB[8] = brssi;
                                    break;
                                case EDNAME3:
                                    id = 0x33;
                                    sendB[9] = brssi;
                                    break;
                                case EDNAME4:
                                    id = 0x34;
                                    sendB[10] = brssi;
                                    break;
                            }
                            send3[5] = timeB;
                            sendB[5] = timeB;
                            send3[6] = deviceName;
                            sendB[6] = deviceName;
                            send3[7] = id;
                            send3[8] = brssi;
                            //    sendMessageToSocket(send3);
                        }
                        sendMessageToSocket(sendB);
                        Log.d(TAG, "handleMessage: " + "BBBBBB");
                        timeB++;

                    }
                    break;
                case LINKUI:
                    if (map == 0x00)
                        modeShow.setText("指纹库");
                    else if (map == 0x01)
                        modeShow.setText("测量");
                    else modeShow.setText("暂停");
                    idShow.setText(Byte.toString(coordNum));
                    LinkShow.setText(wifiManager.getConnectionInfo().toString());
                    if (socket != null && socket.isConnected())
                        SocketShow.setText("Success");
                    else
                        SocketShow.setText("Fail");
                    for (Device_Rssi buff : Devices_show) {
                        sb.append(buff.getName() + "    addr=" + buff.getId() + "    rssi=" + String.valueOf(buff.getRssi()) + "\n");
                    }
                    fromTextView3.setText(sb.toString());
                    sb.delete(0, sb.length());
                    break;

            }
            super.handleMessage(msg);
        }
    };

    private TimerTask serial_task1 = new TimerTask() {
        public void run() {
            Message message = new Message();
            message.what = SERIAL_PORT1;
            handler.sendMessage(message);
        }
    };
    private TimerTask serial_task2 = new TimerTask() {
        public void run() {
            Message message2 = new Message();
            message2.what = SERIAL_PORT2;
            handler.sendMessage(message2);
        }
    };
    private TimerTask blue_task = new TimerTask() {
        public void run() {
            Message message3 = new Message();
            message3.what = BLUE;
            handler.sendMessage(message3);
        }
    };
    private TimerTask wifi_task = new TimerTask() {
        @Override
        public void run() {
            Message message4 = new Message();
            message4.what = WIFI;
            handler.sendMessage(message4);
        }
    };
    private TimerTask LinkUi = new TimerTask() {
        @Override
        public void run() {
            Message message5 = new Message();
            message5.what = LINKUI;
            handler.sendMessage(message5);
        }
    };
    private TimerTask SendAll = new TimerTask() {
        @Override
        public void run() {
            Message message6 = new Message();
            message6.what = SENDALL;
            handler.sendMessage(message6);
        }
    };


    private byte[] InitSend() {
        byte send[] = new byte[10];
        send[0] = 0x0a;
        send[1] = 0x0d;

        for (int i = 2; i <= 9; i++) {
            send[i] = 0x00;
        }
        send[3] = map;
        send[4] = coordNum;
        send[6] = deviceName;
        send[9] = 0x0a;
        return send;
    }

    private byte[] InitSend2() {
        byte send[] = new byte[12];
        send[0] = 0x0a;
        send[1] = 0x0d;

        for (int i = 2; i <= 11; i++) {
            send[i] = 0x00;
        }
        send[3] = map;
        send[4] = coordNum;
        send[6] = deviceName;
        send[11] = 0x0a;
        return send;
    }

    public boolean startSocket() {

        try {
            if (socket == null) {
                String IpNum = Ip.getText().toString();
                int PortNum = Integer.parseInt(Port.getText().toString());
                socket = new Socket(IpNum, PortNum);
            }

            return true;
        } catch (IOException e) {

            e.printStackTrace();
            return false;
        }
    }

    public void sendMessageToSocket(final byte buff[]) {

        if (socket != null && buff.length == 12) {
            //发送数据
            try {

                OutputStream writer = socket.getOutputStream();
                Log.d(TAG, "sendMessageToSocket: " + writer);
                writer.write(buff);
                writer.flush();
                Log.d(TAG, "success " + StoHexString(new String(buff, 0, buff.length)));

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "fail " + buff.toString());
            }
        }
    }


    public String StoHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;

    }

}
