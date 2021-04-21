package com.example.v2v_test.TCP;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.v2v_test.TCPSendV2VActivity;
import com.example.v2v_test.TimeSynActivity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Send extends Thread {
    // public static int HEAD_MAX_SIZE = 300;
    //public static int TOTAL_MAX_SIZE = 1500;
    public static int DATA_MAX_SIZE = 1100;

    private static Send send;

    //單例模式
    public static Send getInstance() {
        if (send == null)
            send = new Send();
        return send;
    }

    /**
     * 发送图片，与文本有区别
     *
     * @param ip
     * @param port
     * @param bitmap 单帧图片
     * @throws IOException
     * @throws InterruptedException
     */

    public static Socket mSocket;
    public static DataOutputStream mOutStream;

    public void sendPicture(String ip, String port, Bitmap bitmap) throws IOException, InterruptedException {
        ByteArrayOutputStream dataBaos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, dataBaos);//图片质量百分60,不然数据包太大
        Map<String, Double> headMap = new HashMap<>();
        byte[] data = dataBaos.toByteArray();
        byte[] total;
        //加上每个包包头大小,244是包头大小
        headMap.put("totalSize", (double) data.length + 244);
        headMap.put("index", Double.valueOf(TCPSendV2VActivity.index));
        headMap.put("sendTime", (double) System.currentTimeMillis() + TimeSynActivity.sendDifferenceVal);
        Log.i("index", String.valueOf(TCPSendV2VActivity.index));
        if (mSocket == null) {
            mSocket = new Socket(ip, Integer.parseInt(port));
            //获取输出流、输入流
            mOutStream = new DataOutputStream(mSocket.getOutputStream());
        }
        headMap.put("offset", (double) 0);//覆盖原来的值
        ByteArrayOutputStream subDataBaos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(subDataBaos);
        oos.writeObject(headMap);//把头部输入到baos，total总长度应不超过1500B
        subDataBaos.write(data);
        total = subDataBaos.toByteArray();

        Log.i("total", String.valueOf(total.length));
        oos.flush();
        oos.close();
        subDataBaos.flush();
        subDataBaos.close();
        mOutStream.write(total);
        mOutStream.flush();
//        String endMark = "END!";
//        mOutStream.writeUTF(endMark);
//        Log.i("endMark", String.valueOf(endMark.getBytes().length));
        //        mOutStream.close();
        //        receiver.close();
        sleep(5);//线程停止一会 不然END！标记不能最后一个发送
    }
}

