package com.example.a49479.wificonnectutil;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";

    private String hostIP;
    private int port;
    private WifiAdmin mWifiAdmin;
    private WifiAutoConnectManager wifiautoconnect;
    private String mUserSSID;
    private WifiConfiguration mUserWifiConfiguration;
    private String mCurSSID;
    private String mCurPwd;

    private Handler mEventHandler;
    private HandlerThread mHandlerThread;

    TextView tv_choose_wifi;
    ListView lv;
    Button btn_connect;
    Button btn_scan;
    EditText et_pwd;


    CommonAdapter adapter ;
    List<ScanResult> scanResults ;
    ScanResult mChoose;

    private Handler mUIHandler ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWifiAdmin = new WifiAdmin(this);

        mUIHandler = new Handler();

        mHandlerThread = new HandlerThread("event handler thread", THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        mEventHandler = new Handler(mHandlerThread.getLooper());

        lv = (ListView)findViewById(R.id.list);
        btn_scan = (Button)findViewById(R.id.btn_scan);
        btn_connect= (Button)findViewById(R.id.btn_connect);
        tv_choose_wifi =(TextView)findViewById(R.id.tv_choose_wifi);
        et_pwd =(EditText) findViewById(R.id.et_pwd);

        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWifiAdmin.startScan();
                Toast.makeText(MainActivity.this,"正在扫描wifi",Toast.LENGTH_LONG).show();
                mUIHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getScanList();
                    }
                },8000);

            }
        });

        btn_connect .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectAp();
            }
        });
    }

    private void getScanList(){
        scanResults = (ArrayList<ScanResult>) mWifiAdmin.getWifiList_2_4G();
        adapter = new CommonAdapter<ScanResult>(MainActivity.this,scanResults,R.layout.layout_item) {
            @Override
            public void convert(ViewHolder helper, ScanResult item, View convertView, int position) throws ParseException {
                helper.setText(R.id.tv_item, item.SSID);
            }
        };
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                tv_choose_wifi.setText("选中：" + scanResults.get(i).SSID);
                mChoose = scanResults.get(i);
            }
        });

    }

    /**
     * 连接猫眼热点
     */
    private void connectAp() {
        mUserSSID = mWifiAdmin.getSSID();
        mUserWifiConfiguration = mWifiAdmin.getCuurentConfiguration();


        mWifiAdmin.startScan();
        ScanResult scanResult = mChoose;
        if (scanResult == null) {
            Toast.makeText(MainActivity.this, "未找到设备",Toast.LENGTH_LONG).show();
            return;
        }



        //startCountTime();

        wifiautoconnect = new WifiAutoConnectManager(this);
        wifiautoconnect.setListenner(new WifiAutoConnectManager.WifiConnectListenner() {
            @Override
            public void connectSuc() {
                Log.i(TAG, mWifiAdmin.getSSID()+ " isWifiConnected:" + mWifiAdmin.isWifiConnected(MainActivity.this) + ", isNetworkAvailable:" + mWifiAdmin.isNetworkAvailable(MainActivity.this));
                while (!mWifiAdmin.isWifiConnected(MainActivity.this)) {
                    try {

                        // 为了避免程序一直while循环，让它睡个100毫秒检测……
                        Thread.sleep(1000);
                        System.out.println("wifi已经打开");
                    } catch (InterruptedException ie) {
                    }
                }
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tv_choose_wifi.setText(tv_choose_wifi.getText().toString()+"  连接成功");
                    }
                });
//                mEventHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        connect();
//                    }
//                }, 10 * 1000);

            }

            @Override
            public void connectFail() {
                Log.e(TAG, "wifiautoconnect setListenner connectFail");
            }

        });

        int wifi_security_type = mWifiAdmin.getWifiSecretType(scanResult);
        if(wifi_security_type==4) {
            wifiautoconnect.connect(scanResult, "12345678", wifi_security_type);
        }
        else{
            wifiautoconnect.connect(scanResult, et_pwd.getText().toString(), wifi_security_type);
        }
    }

    private void connect() {
        Log.e(TAG, "connect()------------" + client.isConnected());
        if (client.isConnected()) {
            // 断开连接
            client.disconnect();
        } else {
            try {
                hostIP = mWifiAdmin.getApAddress();
                port = Integer.parseInt("22270");
                /*String hostIP = "192.168.1.101";
                int port = Integer.parseInt("8080");*/
                client.connect(hostIP, port);
            } catch (NumberFormatException e) {
//                Toast.makeText(this, "端口错误", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }


    /**
     * 用来与猫眼传递数据的tcp客户端
     */
    private TcpClient client = new TcpClient() {

        @Override
        public void onConnect(SocketTransceiver transceiver) {
        }

        @Override
        public void onDisconnect(SocketTransceiver transceiver) {
        }

        @Override
        public void onConnectFailed() {

        }

        @Override
        public void onReceive(SocketTransceiver transceiver, final String s) {

        }

        @Override
        public void onReceive(SocketTransceiver transceiver, final byte[] b) {

        }
    };

    private ScanResult getCatCameraAp() {
        ArrayList<ScanResult> scanResults = (ArrayList<ScanResult>) mWifiAdmin.getWifiList_2_4G();
        for (ScanResult scanResult : scanResults) {
            if (scanResult.SSID.length() > 8 && scanResult.SSID.substring(0, 8).equalsIgnoreCase("Loock-C1")) {
                return scanResult;
            }
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestAllPermission();
    }

    public void requestAllPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                /*|| ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED*/) {
            requestAllPerm();
        } else {
            Log.i("saveUserIcon", "2");
        }
    }

    private void requestAllPerm() {
        boolean isFirstAsk = (boolean) SPUtil.getInstance(this).get("f", true);
        if (isFirstAsk) {
            SPUtil.getInstance(this).put("f", false);

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    1000);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    /*|| ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)*/
                    ) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        1000);
            } else {

            }
        }
    }
}
