package com.example.v2v_test.UDP;

import android.os.Bundle;
import android.os.Message;

import com.example.v2v_test.CameraReceiveV2VActivity;
import com.example.v2v_test.TimeSynActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Map;

//接受消息 弄成一个常驻线程 APP启动线程则启动 监听接受消息
public class ReceivePicThread extends Thread {
    private int MAX_SIZE = 1344;
    //本机端口
    private int SERVER_PORT;
    //Handler传递的数据
    private Message msg;

    public ReceivePicThread(int Server_Port) {
        SERVER_PORT = Server_Port;
    }

    @Override
    public void run() {
        try {
            ByteArrayOutputStream resultPic = new ByteArrayOutputStream();//类似于List 动态获取总数据包
            DatagramSocket socket = new DatagramSocket(SERVER_PORT);
            socket.setBroadcast(true);//
            byte[] total = new byte[MAX_SIZE];// 创建字节数组，指定接收的数据包的大小
            //不要经度纬度就是220 ，要就是271
            int headSize = 244;
            byte[] head = new byte[headSize];
            byte[] data = new byte[1100];
            DatagramPacket packet;
            Double dataOffset;
            int offset = 0;
            Double index = (double) 0;
            Double totalSize = (double) 0;
            Double sendTime = (double) 0;
            Double receiveTime = (double) 0;
            //Double latitude = Double.valueOf(0);
            //Double longitude = Double.valueOf(0);
            boolean flag = true;
            //不要地图就是CameraReceiveV2VActivity，要就是CameraReceiveActivity
            while (CameraReceiveV2VActivity.keepRunning) {//一直监听
                //packet=new DatagramPacket(data, data.length);
                //  packet=new DatagramPacket()
                packet = new DatagramPacket(total, MAX_SIZE);
                socket.receive(packet);// 此方法在接收到数据报之前会一直阻塞
                String temp = new String(packet.getData(), 0, packet.getData().length);
                if (temp.startsWith("END!")) {
                    receiveTime = (double) System.currentTimeMillis() + TimeSynActivity.receiveDifferenceVal;
                    break;
                }
                //接收图片数据
                System.arraycopy(packet.getData(), headSize, data, 0, 1100);
                //根据头数据的分片偏移量,有序组装图片的数据
                System.arraycopy(total, 0, head, 0, headSize);
                ByteArrayInputStream byteInt = new ByteArrayInputStream(head);
                ObjectInputStream objInt = new ObjectInputStream(byteInt);
                Map<String, Double> headMap = (Map<String, Double>) objInt.readObject();
                dataOffset = headMap.get("offset");//分片偏移量标记
                while (flag) {
                    totalSize = headMap.get("totalSize");
                    sendTime = headMap.get("sendTime");
                    index = headMap.get("index");
                    //latitude = headMap.get("latitude");
                    //longitude = headMap.get("longitude");
                    flag = false;
                }
                //Log.i("offset:", String.valueOf(dataOffset));
                //按照偏移量有序接收图片数据
                if (dataOffset == offset) {
                    offset++;
                    resultPic.write(data);
                }
                //关闭资源
                objInt.close();
                byteInt.close();
            }
            byte[] byteResultPic = resultPic.toByteArray();//转换为字节
            //Log.i("totalSize", String.valueOf(totalSize + dataOffset * 128));
            //Log.i("ReceiveSize", String.valueOf(resultPic.size()));
            if (byteResultPic.length != 0) {
                if (byteResultPic.length != 0) {
                    msg = new Message();
                    msg.what = 1;
                    msg.obj = byteResultPic;
                    Bundle bundle = new Bundle();
                    bundle.putInt("FPS", (int) (receiveTime - sendTime));
                    bundle.putInt("index", index.intValue());
                    bundle.putInt("totalSize", totalSize.intValue());
                    //bundle.putDouble("latitude",latitude);
                    //bundle.putDouble("longitude",longitude);
                    //此次实验不需要地图定位，故取消
//                Log.i("latitude:",latitude.toString());
//                Log.i("longitude:",longitude.toString());
                    msg.setData(bundle);
                    //CameraReceiveActivity.handler.sendMessage(msg);
                    CameraReceiveV2VActivity.handler.sendMessage(msg);
                }
            }
            resultPic.flush();
            resultPic.close();
            socket.close();
            this.start();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}




