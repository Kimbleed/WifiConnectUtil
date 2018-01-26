package com.example.a49479.wificonnectutil;


import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by zsq on 2017/3/19.
 */
public abstract class TcpClient implements Runnable {

    private int port;
    private String hostIP;
    private boolean connect = false;
    private SocketTransceiver transceiver;

    /**
     * 建立连接
     * <p>
     * 连接的建立将在新线程中进行
     * <p>
     * 连接建立成功，回调{@code onConnect()}
     * <p>
     * 连接建立失败，回调{@code onConnectFailed()}
     *
     * @param hostIP
     *            服务器主机IP
     * @param port
     *            端口
     */
    public void connect(String hostIP, int port) {
        this.hostIP = hostIP;
        this.port = port;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(hostIP, port);
            //socket.setSoTimeout(500);
            transceiver = new SocketTransceiver(socket) {

                @Override
                public void onReceive(InetAddress addr, String s) {
                    TcpClient.this.onReceive(this, s);
                }

                @Override
                public void onReceive(InetAddress addr, byte[] b) {
                    TcpClient.this.onReceive(this, b);
                }

                @Override
                public void onDisconnect(InetAddress addr) {
                    connect = false;
                    TcpClient.this.onDisconnect(this);
                }
            };
            transceiver.start();
            connect = true;
            this.onConnect(transceiver);
        } catch (Exception e) {
            e.printStackTrace();
            this.onConnectFailed();
        }
    }

    /**
     * 断开连接
     * <p>
     * 连接断开，回调{@code onDisconnect()}
     */
    public void disconnect() {
        if (transceiver != null) {
            transceiver.stop();
            transceiver = null;
        }
    }

    /**
     * 判断是否连接
     *
     * @return 当前处于连接状态，则返回true
     */
    public boolean isConnected() {
        return connect;
    }

    /**
     * 获取Socket收发器
     *
     * @return 未连接则返回null
     */
    public SocketTransceiver getTransceiver() {
        return isConnected() ? transceiver : null;
    }

    /**
     * 连接建立
     *
     * @param transceiver
     *            SocketTransceiver对象
     */
    public abstract void onConnect(SocketTransceiver transceiver);

    /**
     * 连接建立失败
     */
    public abstract void onConnectFailed();

    /**
     * 接收到数据
     * <p>
     * 注意：此回调是在新线程中执行的
     *
     * @param transceiver
     *            SocketTransceiver对象
     * @param s
     *            字符串
     */
    public abstract void onReceive(SocketTransceiver transceiver, String s);

    public abstract void onReceive(SocketTransceiver transceiver, byte[] b);

    /**
     * 连接断开
     * <p>
     * 注意：此回调是在新线程中执行的
     *
     * @param transceiver
     *            SocketTransceiver对象
     */
    public abstract void onDisconnect(SocketTransceiver transceiver);

    public static byte[] constructAPHead(int length, int cmdType, int result){
        byte[] resultByte = new byte[0];
        byte[] head = new byte[]{(byte) 0XAA,  0x55, (byte) 0XAA, 0X55};
        resultByte = BytesUtilsBE.mergeArray(resultByte, head);

        String cmdHexStr = BytesUtilsBE.algorismToHEXString(cmdType);
        byte[] cmdHexByte = BytesUtilsBE.hexStringToBytes(cmdHexStr, 4);
        resultByte = BytesUtilsBE.mergeArray(resultByte, cmdHexByte);

        String lengthHexStr = BytesUtilsBE.algorismToHEXString(length);
        byte[] lengthHexByte = BytesUtilsBE.hexStringToBytes(lengthHexStr, 4);
        resultByte = BytesUtilsBE.mergeArray(resultByte, lengthHexByte);

        String rehHexStr = BytesUtilsBE.algorismToHEXString(result);
        byte[] reHexByte = BytesUtilsBE.hexStringToBytes(rehHexStr, 4);
        resultByte = BytesUtilsBE.mergeArray(resultByte, reHexByte);

        return resultByte;
    }

    public byte[] constructInfo(int length, int type, String SSID, String pwd, String registID){
        byte[] resultByte = new byte[0];

        String typeHexStr = BytesUtilsBE.algorismToHEXString(type);
        byte[] typeHexByte = BytesUtilsBE.hexStringToBytes(typeHexStr, 4);
        resultByte = BytesUtilsBE.mergeArray(resultByte, typeHexByte);

        String ssidHex = BytesUtilsBE.str2HexStr(SSID);
        final byte[] ssidHexByte = BytesUtilsBE.hexStringToBytes(ssidHex, 32);
        resultByte = BytesUtilsBE.mergeArray(resultByte, ssidHexByte);

        String pwdHex = BytesUtilsBE.str2HexStr(pwd);
        final byte[] pwdHexByte = BytesUtilsBE.hexStringToBytes(pwdHex, 64);
        resultByte = BytesUtilsBE.mergeArray(resultByte, pwdHexByte);

        String registIDHex = BytesUtilsBE.str2HexStr(registID);
        final byte[] registIDHexByte = BytesUtilsBE.hexStringToBytes(registIDHex, 64);
        resultByte = BytesUtilsBE.mergeArray(resultByte, registIDHexByte);

        return  resultByte;
    }

    public static byte[] constructHead(int length){
        byte[] resultByte = new byte[0];
        byte[] head = new byte[]{(byte) 0XAA,  0x55, (byte) 0XAA, 0X55};
        resultByte = BytesUtilsBE.mergeArray(resultByte, head);

        String cmdHexStr = BytesUtilsBE.algorismToHEXString(40);
        byte[] cmdHexByte = BytesUtilsBE.hexStringToBytes(cmdHexStr, 2);
        resultByte = BytesUtilsBE.mergeArray(resultByte, cmdHexByte);

        String reseved_w1HexStr = BytesUtilsBE.algorismToHEXString(0);
        byte[] reseved_w1HexByte = BytesUtilsBE.hexStringToBytes(reseved_w1HexStr, 2);
        resultByte = BytesUtilsBE.mergeArray(resultByte, reseved_w1HexByte);

        String reseved_d2HexStr = BytesUtilsBE.algorismToHEXString(0);
        byte[] reseved_d2HexByte = BytesUtilsBE.hexStringToBytes(reseved_d2HexStr, 4);
        resultByte = BytesUtilsBE.mergeArray(resultByte, reseved_d2HexByte);

        String lengthHexStr = BytesUtilsBE.algorismToHEXString(length);
        byte[] lengthHexByte = BytesUtilsBE.hexStringToBytes(lengthHexStr, 4);
        resultByte = BytesUtilsBE.mergeArray(resultByte, lengthHexByte);

        return resultByte;
    }

    public static byte[] constructOTAInfo(String needOTAJsonStr){
        byte[] resultByte = new byte[0];

        //String needOTAHex = BytesUtilsBE.str2HexStr(needOTAJsonStr);
        //final byte[] needOTAHexByte = BytesUtilsBE.hexStringToBytes(needOTAHex, needOTAJsonStr.getBytes().length);
        byte[] needOTAHexByte = needOTAJsonStr.getBytes();
        resultByte = BytesUtilsBE.mergeArray(resultByte, needOTAHexByte);

        return  resultByte;
    }
}
