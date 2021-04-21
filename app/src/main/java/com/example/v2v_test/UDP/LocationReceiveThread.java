package com.example.v2v_test.UDP;

import android.os.Bundle;
import android.os.Message;

import com.example.v2v_test.LocationReceiveActivity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Map;

//接受消息 弄成一个常驻线程 APP启动线程则启动 监听接受消息
public class LocationReceiveThread extends Thread {
    //本机端口
    private int SERVER_PORT;
    //Handler传递的数据
    private Message msg;

    public LocationReceiveThread(int Server_Port) {
        SERVER_PORT = Server_Port;
    }

    @Override
    public void run() {
        try {
            while (LocationReceiveActivity.keepRunning) {
                DatagramSocket socket = new DatagramSocket(SERVER_PORT);
                socket.setBroadcast(true);//
                int headSize = 199;
                byte[] head = new byte[headSize];
                DatagramPacket packet;
                Double latitude;
                Double longitude;
                //不要地图就是CameraReceiveV2VActivity，要就是CameraReceiveActivity
                packet = new DatagramPacket(head, headSize);
                socket.receive(packet);// 此方法在接收到数据报之前会一直阻塞
                //接收图片数据
                ByteArrayInputStream byteInt = new ByteArrayInputStream(head);
                ObjectInputStream objInt = new ObjectInputStream(byteInt);
                Map<String, Double> headMap = (Map<String, Double>) objInt.readObject();
                latitude = headMap.get("latitude");
                longitude = headMap.get("longitude");
                //关闭资源
                objInt.close();
                byteInt.close();
                msg = new Message();
                msg.what = 1;
                Bundle bundle = new Bundle();
                if (latitude != null && longitude != null) {
                    bundle.putDouble("latitude", latitude);
                    bundle.putDouble("longitude", longitude);
                }
                msg.setData(bundle);
                LocationReceiveActivity.handler.sendMessage(msg);
                socket.close();
            }
//            this.start();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}




