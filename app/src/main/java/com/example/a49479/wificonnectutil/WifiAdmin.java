package com.example.a49479.wificonnectutil;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by zsq on 2017/3/17.
 */
public class WifiAdmin {
    private final static String TAG = "WifiAdmin";
    // 定义WifiManager对象
    private WifiManager mWifiManager;
    // 定义WifiInfo对象
    private WifiInfo mWifiInfo;
    // 扫描出的网络连接列表
    private List<ScanResult> mWifiList;
    // 网络连接列表
    private List<WifiConfiguration> mWifiConfiguration;
    // 定义一个WifiLock
    WifiManager.WifiLock mWifiLock;
    private Context context;

    static final int SECURITY_NONE = 0;
    static final int SECURITY_WEP = 1;
    static final int SECURITY_PSK = 2;
    static final int SECURITY_EAP = 3;

    private WifiConnectListenner listenner;

    public interface WifiConnectListenner {
        void connectFail();

        void connectSuc();
    }

    public enum WifiCipherType {
        WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
    }

    public void setListenner(WifiConnectListenner listener) {
        this.listenner = listener;
    }

    // 构造器
    public WifiAdmin(Context context) {
        this.context = context;
        // 取得WifiManager对象
        mWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        // 取得WifiInfo对象
        mWifiInfo = mWifiManager.getConnectionInfo();
    }

    public boolean isWifiOpen() {
        return mWifiManager.isWifiEnabled();
    }

    // 打开WIFI
    public void openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    // 关闭WIFI
    public void closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    // 检查当前WIFI状态
    public int checkState() {
        return mWifiManager.getWifiState();
    }

    // 锁定WifiLock
    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    // 解锁WifiLock
    public void releaseWifiLock() {
        // 判断时候锁定
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    // 创建一个WifiLock
    public void creatWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("Test");
    }

    // 得到配置好的网络
    public List<WifiConfiguration> getConfiguration() {
        startScan();
        return mWifiConfiguration;
    }

    // 指定配置好的网络进行连接
    public void connectConfiguration(int index) {
        // 索引大于配置好的网络索引返回
        if (index > mWifiConfiguration.size()) {
            return;
        }
        // 连接配置好的指定ID的网络
        mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId,
                true);
    }

    public void connectConfiguration(WifiConfiguration mWifiConfiguration) {
        if (mWifiConfiguration == null)
            return;
        mWifiManager.enableNetwork(mWifiConfiguration.networkId,
                true);
    }

    public WifiConfiguration getCuurentConfiguration() {
        startScan();
        if (mWifiConfiguration != null) {
            for (WifiConfiguration wifiConfiguration : mWifiConfiguration) {
                if (wifiConfiguration.SSID != null && wifiConfiguration.SSID.equalsIgnoreCase(getSSID())) {
                    return wifiConfiguration;
                }
            }
        }
        return null;
    }

    public void startScan() {
        mWifiManager.startScan();
        // 得到扫描结果
        mWifiList = mWifiManager.getScanResults();
        // 得到配置好的网络连接
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();
    }

    // 得到网络列表
    public List<ScanResult> getWifiList() {
        return mWifiList;
    }

    public List<ScanResult> getWifiList_2_4G() {
        List<ScanResult> ret = new ArrayList<ScanResult>();
        for (ScanResult wifi : mWifiList) {
            if (wifi.frequency < 3000) {
                ret.add(wifi);
            }
        }
        return ret;
    }

    public List<ScanResult> getWifiList_5G() {
        List<ScanResult> ret = new ArrayList<ScanResult>();
        for (ScanResult wifi : mWifiList) {
            if (wifi.frequency > 3000) {
                ret.add(wifi);
            }
        }
        return ret;
    }

    public List<ScanResult> getWifiListAllType() {
        List<ScanResult> retList = new ArrayList<ScanResult>();
        List<ScanResult> ret2_4G = getWifiList_2_4G();
        for (ScanResult result2_4G : ret2_4G) {
            boolean isExist = false;
            for (ScanResult result : retList) {
                if (result2_4G.SSID.equals(result.SSID))
                    isExist = true;
            }
            if (!isExist) {
                retList.add(result2_4G);
            }
        }
        List<ScanResult> ret5G = getWifiList_5G();
        for (ScanResult result5G : ret5G) {
            boolean isExist = false;
            for (ScanResult result : retList) {
                if (result5G.SSID.equals(result.SSID))
                    isExist = true;
            }
            if (!isExist) {
                retList.add(result5G);
            }
        }
        return retList;
    }

    // 查看扫描结果
    public StringBuilder lookUpScan() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mWifiList.size(); i++) {
            stringBuilder
                    .append("Index_" + new Integer(i + 1).toString() + ":");
            // 将ScanResult信息转换成一个字符串包
            // 其中把包括：BSSID、SSID、capabilities、frequency、level
            stringBuilder.append((mWifiList.get(i)).toString());
            stringBuilder.append("/n");
        }
        return stringBuilder;
    }

    // 得到MAC地址
    public String getMacAddress() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    // 得到接入点的BSSID
    public String getBSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }

    public String getSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID();
    }

    // 得到IP地址
    public int getIPAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    // 得到连接的ID
    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    // 得到WifiInfo的所有信息包
    public String getWifiInfo() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
    }

    // 添加一个网络并连接
    public boolean addNetwork(WifiConfiguration wcg) {
        int wcgID = mWifiManager.addNetwork(wcg);
        /*boolean b =  mWifiManager.enableNetwork(wcgID, true);
        Log.e(TAG,"wcgID:" + wcgID + "b:" + b);
        return b;*/


        Method connectMethod = connectWifiByReflectMethod(wcgID);
        if (connectMethod == null) {
            Log.i(TAG,
                    " connect wifi by enableNetwork method, Add by jiangping.li");
            // 通用API
            boolean b = mWifiManager.enableNetwork(wcgID, true);
            return b;
        } else {
            return true;
        }
    }

    public boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            Log.e(TAG,"mWiFiNetworkInfo:" + mWiFiNetworkInfo);
            if (mWiFiNetworkInfo != null) {
                Log.e(TAG, "isWifiConnected:" + mWiFiNetworkInfo.isConnected() + ", isAvailable:" + mWiFiNetworkInfo.isAvailable());
//                if(mWiFiNetworkInfo.isConnected())
//                    return mWiFiNetworkInfo.isConnected();
//                if(mWiFiNetworkInfo.isAvailable())
//                    return mWiFiNetworkInfo.isAvailable();
                return mWiFiNetworkInfo.isConnected();
            }
        }
        return false;
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // 断开指定ID的网络
    public void disconnectWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }


    public WifiConfiguration CreateWifiInfo(String SSID, String Password, int Type, boolean ssidSymbol) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.priority = Integer.MAX_VALUE;
        if (ssidSymbol) {
            config.SSID = "\"" + SSID + "\"";
        } else {
            config.SSID = SSID;
        }

        WifiConfiguration tempConfig = this.IsExsits(SSID);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        // 分为三种情况：1没有密码2用wep加密3用wpa加密
        if (Type == 1) // WIFICIPHER_NOPASS
        {
//            config.wepKeys[0] = "\"" + "\"";
//            config.wepTxKeyIndex = 0;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        if (Type == 2) // WIFICIPHER_WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + Password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 3) // WIFICIPHER_WPA
        {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    private WifiConfiguration createWifiInfo(String SSID, String Password, WifiCipherType Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        if (Type == WifiCipherType.WIFICIPHER_NOPASS) {//modify 20161219�������ε����лᵼ�·���netIdΪ-1
//            config.wepKeys[0] = "";
            config.hiddenSSID = true;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//            config.wepTxKeyIndex = 0;

            System.out.println("��������");
        } else if (Type == WifiCipherType.WIFICIPHER_WEP) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (Type == WifiCipherType.WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            // �˴���Ҫ�޸ķ������Զ�����
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        } else {
            return null;
        }
        return config;
    }

    private WifiConfiguration IsExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    public String getApAddress() {
        DhcpInfo dhcpinfo = mWifiManager.getDhcpInfo();
        String serverAddress = intToIp(dhcpinfo.serverAddress);
        Log.e(TAG,"getApAddress:" + serverAddress);
        return serverAddress;
    }

    private String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                + (0xFF & paramInt >> 24);
    }

    public int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
//            Toast.makeText(context,"加密方式:SECURITY_PSK", Toast.LENGTH_LONG).show();
            Log.i(TAG,"加密方式:SECURITY_PSK");
            return SECURITY_PSK;
        } else if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) || config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
//            Toast.makeText(context,"加密方式:SECURITY_EAP", Toast.LENGTH_LONG).show();
            Log.i(TAG,"加密方式:SECURITY_EAP");
            return SECURITY_EAP;
        } else {
            if (config.wepKeys[0] != null) {
//                Toast.makeText(context,"加密方式:SECURITY_WEP", Toast.LENGTH_LONG).show();
                Log.i(TAG,"加密方式:SECURITY_WEP");
            } else {
//                Toast.makeText(context,"加密方式:SECURITY_NONE", Toast.LENGTH_LONG).show();
                Log.i(TAG,"加密方式:SECURITY_NONE");
            }
            return (config.wepKeys[0] != null ? SECURITY_WEP : SECURITY_NONE);
        }
    }

    public void connect(String ssid, String password, WifiCipherType type) {
        Thread thread = new Thread(new ConnectRunnable(ssid, password, type));
        thread.start();
    }

    class ConnectRunnable implements Runnable {
        private String ssid;

        private String password;

        private WifiCipherType type;

        public ConnectRunnable(String ssid, String password, WifiCipherType type) {
            this.ssid = ssid;
            this.password = password;
            this.type = type;

            System.out.println("ssid: " + ssid + " password: " + password + " type: " + type);
        }

        @Override
        public void run() {
            openWifi();
            while (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
                try {
                    Thread.sleep(100);
                    System.out.println("WIFI_STATE_ENABLING");
                } catch (InterruptedException ie) {
                }
            }

            WifiConfiguration wifiConfig = createWifiInfo(ssid, password, type);
            System.out.println("wifiConfig:" + wifiConfig);

            if (wifiConfig == null) {
                return;
            }
            WifiConfiguration tempConfig = isExsits(ssid);

            if (tempConfig != null) {
                mWifiManager.removeNetwork(tempConfig.networkId);
            }

            int netID = mWifiManager.addNetwork(wifiConfig);

            System.out.println("netID:" + netID);


            if (-1 != netID) {
                Method connectMethod = connectWifiByReflectMethod(netID);
                if (connectMethod == null) {
                    System.out.println("connect wifi by enableNetwork method");
                    boolean b = mWifiManager.enableNetwork(netID, true);
                    if (b) {
                        listenner.connectSuc();
                        Log.e(TAG,"connect success:" + b);
                    } else {
                        listenner.connectFail();
                    }
                } else {
                    listenner.connectSuc();
                }


            } else {
                listenner.connectFail();
            }
        }
    }

    private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        if (existingConfigs != null) {
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                    return existingConfig;
                }
            }
        }
        return null;
    }

    /**
     * 通过反射出不同版本的connect方法来连接Wifi
     *
     * @param netId
     * @return
     * @author jiangping.li
     * @since MT 1.0
     */
    private Method connectWifiByReflectMethod(int netId) {
        Method connectMethod = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

            System.out.println("connectWifiByReflectMethod road 1");
            // ���䷽���� connect(int, listener) , 4.2 <= phone��s android version
            for (Method methodSub : mWifiManager.getClass().getDeclaredMethods()) {
                if ("connect".equalsIgnoreCase(methodSub.getName())) {
                    Class<?>[] types = methodSub.getParameterTypes();
                    if (types != null && types.length > 0) {
                        if ("int".equalsIgnoreCase(types[0].getName())) {
                            connectMethod = methodSub;
                        }
                    }
                }
            }
            if (connectMethod != null) {
                try {
                    connectMethod.invoke(mWifiManager, netId, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("connectWifiByReflectMethod Android "
                            + Build.VERSION.SDK_INT + " error!");

                    return null;
                }
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
            // ���䷽��: connect(Channel c, int networkId, ActionListener listener)
            // ��ʱ������4.1����� , 4.1 == phone��s android version
            System.out.println("connectWifiByReflectMethod road 2");
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            System.out.println("connectWifiByReflectMethod road 3");
            // ���䷽����connectNetwork(int networkId) ,
            // 4.0 <= phone��s android version < 4.1
            for (Method methodSub : mWifiManager.getClass()
                    .getDeclaredMethods()) {
                if ("connectNetwork".equalsIgnoreCase(methodSub.getName())) {
                    Class<?>[] types = methodSub.getParameterTypes();
                    if (types != null && types.length > 0) {
                        if ("int".equalsIgnoreCase(types[0].getName())) {
                            connectMethod = methodSub;
                        }
                    }
                }
            }
            if (connectMethod != null) {
                try {
                    connectMethod.invoke(mWifiManager, netId);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("connectWifiByReflectMethod Android "
                            + Build.VERSION.SDK_INT + " error!");
                    return null;
                }
            }
        } else {
            // < android 4.0
            return null;
        }
        return connectMethod;
    }

    /**
     * 获取wifi加密类型
     *
     * @param sr
     * @return
     */
    public int getWifiSecretType(ScanResult sr) {
        int wifi_security_type = 0;
        String wifi_security = sr.capabilities.toUpperCase(Locale.getDefault());
        if (wifi_security.contains(GlobalParam.WPA_PSK) || wifi_security.contains(GlobalParam.WPA2_PSK)) {
            wifi_security_type = 1;
        } else if (wifi_security.contains(GlobalParam.WPA_EAP) || wifi_security.contains(GlobalParam.WPA2_EAP)
                || wifi_security.contains(GlobalParam.IEEE8021X) || wifi_security.contains(GlobalParam.IBSS)) {
            wifi_security_type = 2;// 802.1x
        } else if (wifi_security.contains(GlobalParam.WEP)) {
            wifi_security_type = 3;
        } else if (wifi_security.contains(GlobalParam.ESS) || wifi_security.contains(GlobalParam.WPS)) {
            wifi_security_type = 4;
        } else {
            ;
        }

        return wifi_security_type;
    }

}
//分为三种情况：1没有密码2用wep加密3用wpa加密




