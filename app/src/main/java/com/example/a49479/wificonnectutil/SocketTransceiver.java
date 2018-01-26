package com.example.a49479.wificonnectutil;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by zsq on 2017/3/19.
 */
public abstract class SocketTransceiver implements Runnable {

    protected Socket socket;
    protected InetAddress addr;
    protected DataInputStream in;
    protected DataOutputStream out;
    private boolean runFlag;

    /**
     * 实例化
     *
     * @param socket
     *            已经建立连接的socket
     */
    public SocketTransceiver(Socket socket) {
        this.socket = socket;
        this.addr = socket.getInetAddress();
    }

    /**
     * 获取连接到的Socket地址
     *
     * @return InetAddress对象
     */
    public InetAddress getInetAddress() {
        return addr;
    }

    /**
     * 开启Socket收发
     * <p>
     * 如果开启失败，会断开连接并回调{@code onDisconnect()}
     */
    public void start() {
        runFlag = true;
        new Thread(this).start();
    }

    /**
     * 断开连接(主动)
     * <p>
     * 连接断开后，会回调{@code onDisconnect()}
     */
    public void stop() {
        runFlag = false;
        try {
            if(socket !=null)
                socket.shutdownInput();
            if(in!=null)
                in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送字符串
     *
     * @param s
     *            字符串
     * @return 发送成功返回true
     */
    public boolean send(String s) {
        if (out != null) {
            try {
                out.writeUTF(s);
                out.flush();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean send(byte[] b) {
        if (out != null) {
            try {
                out.write(b);
                out.flush();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 监听Socket接收的数据(新线程中运行)
     */
    @Override
    public void run() {
        try {
            in = new DataInputStream(this.socket.getInputStream());
            out = new DataOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            runFlag = false;
        }
        while (runFlag) {
            try {
                int readBytes=0;
                byte[] b=new byte[32];//1024可改成任何需要的值
                int len=b.length;

                while (readBytes < len) {
                    int read = in.read(b, readBytes, len - readBytes);
                    //判断是不是读到了数据流的末尾 ，防止出现死循环。
                    if (read == -1) {
                        break;
                    }
                    readBytes += read;
                    this.onReceive(addr, b);
                }


/*                byte[] b = new byte[32];
                if(in.read() != -1)
                in.read(b);
                //Log.d("111", "A:" + in.available());
                *//*try {
                    Log.d("111", "A:" + in.read(b));
                }catch (java.net.SocketTimeoutException e){
                    Log.d("111", "SocketTimeoutException:" + DingTextUtils.convertToHexString(b));
                }*//*
                //byte[] b = readBytes(in);
                this.onReceive(addr, b);*/
                //Log.d("111", "B:" + DingTextUtils.convertToHexString(b));
            } catch (IOException e) {
                // 连接被断开(被动)
                runFlag = false;
            }
        }
        // 断开连接
        try {
            in.close();
            out.close();
            socket.close();
            in = null;
            out = null;
            socket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.onDisconnect(addr);
    }

    public static byte[] readBytes(InputStream in) throws IOException {
        Log.d("111", "A:" + in.available());
        byte[] result = new byte[32];
        if(in.available() == 0)
            return result;
        byte[] temp = new byte[in.available()];

        int size = 0;
        while ((size = in.read(temp)) != -1) {
            byte[] readBytes = new byte[size];
            System.arraycopy(temp, 0, readBytes, 0, size);
            result = mergeArray(result,readBytes);
        }
        return result;
    }

    /***
     * 合并字节数组
     * @param a
     * @return
     */
    public static byte[] mergeArray(byte[]... a) {
        // 合并完之后数组的总长度
        int index = 0;
        int sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum = sum + a[i].length;
        }
        byte[] result = new byte[sum];
        for (int i = 0; i < a.length; i++) {
            int lengthOne = a[i].length;
            if(lengthOne==0){
                continue;
            }
            // 拷贝数组
            System.arraycopy(a[i], 0, result, index, lengthOne);
            index = index + lengthOne;
        }
        return result;
    }

    /**
     * 接收到数据
     * <p>
     * 注意：此回调是在新线程中执行的
     *
     * @param addr
     *            连接到的Socket地址
     * @param s
     *            收到的字符串
     */
    public abstract void onReceive(InetAddress addr, String s);

    public abstract void onReceive(InetAddress addr, byte[] b);

    /**
     * 连接断开
     * <p>
     * 注意：此回调是在新线程中执行的
     *
     * @param addr
     *            连接到的Socket地址
     */
    public abstract void onDisconnect(InetAddress addr);
}
