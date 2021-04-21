package com.example.v2v_test.TCP;

import android.os.Bundle;
import android.os.Message;

import com.example.v2v_test.TCPReceiveV2VActivity;
import com.example.v2v_test.TimeSynActivity;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

//接受消息 弄成一个常驻线程 APP启动线程则启动 监听接受消息
public class ReceivePicThreadNoServer extends Thread {
    //本机端口
    private int SERVER_PORT;
    //Handler传递的数据
    private Message msg;


    public static ServerSocket mServerSocket;
    public static Socket mSocket;
    public static DataInputStream inputStream;


    public ReceivePicThreadNoServer(int Server_Port) {
        SERVER_PORT = Server_Port;
    }

    @Override
    public void run() {
        try {
            while (TCPReceiveV2VActivity.keepRunning) {
//                Log.i("RESTART", "----------------------------");
                //开启服务、指定端口号
                if (mServerSocket == null) {
                    mServerSocket = new ServerSocket(SERVER_PORT);
                }
                if (mSocket == null) {
                    //等待客户端的连接，Accept会阻塞，直到建立连接，
                    System.out.println(" mServerSocket.accept(),waiting...");
                    //所以需要放在子线程中运行。
                    mSocket = mServerSocket.accept();
                    System.out.println("after accept");
                }
                inputStream = new DataInputStream(mSocket.getInputStream());

                int headSize = 244;
                byte[] head = new byte[headSize];
                int len = 0;
                //接收图片,防止粘包情况
                while (len < headSize) {
                    len += inputStream.read(head, len, headSize - len);
                    if (len < 0) break; //发送方 已关闭连接
//                    Log.i("len", String.valueOf(len));
                }
                if (len < 0) {
                    //发送方 已关闭连接
                    //不接收信息了
//                    Log.i("len", "len<0");
                    //释放资源
                    if (inputStream != null) {
                        inputStream.close();
                        inputStream = null;
                    }
                    if (mSocket != null) {
                        mSocket.close();
                        mSocket = null;
                    }
                    if (mServerSocket != null) {
                        mServerSocket.close();
                        mServerSocket = null;
                    }
                    break;
                } else {
                    ByteArrayInputStream byteInt = new ByteArrayInputStream(head);
                    ObjectInputStream objInt = new ObjectInputStream(byteInt);
                    Map<String, Double> headMap = (Map<String, Double>) objInt.readObject();
//                    Log.i("headMap", String.valueOf(headMap));
                    objInt.close();
                    byteInt.close();
                    Double totalSize = headMap.get("totalSize");
                    Double sendTime = headMap.get("sendTime");
                    Double index = headMap.get("index");
                    byte[] picData = new byte[totalSize.intValue() - headSize];
                    len = 0;
                    //接收图片,防止粘包情况
                    while (len < totalSize.intValue() - headSize) {
                        len += inputStream.read(picData, len, totalSize.intValue() - headSize - len);
//                        Log.i("len", String.valueOf(len));

                    }

                    Double receiveTime = (double) System.currentTimeMillis() + TimeSynActivity.receiveDifferenceVal;

                    //发送相关信到View，显示图片
                    if (picData.length != 0) {
                        if (picData.length != 0) {
                            msg = new Message();
                            msg.what = 1;
                            msg.obj = picData;
                            Bundle bundle = new Bundle();
                            bundle.putInt("FPS", (int) (receiveTime - sendTime));//FPS应该是1s多少帧图片，这里有点误
                            bundle.putInt("index", index.intValue());
                            bundle.putInt("totalSize", totalSize.intValue());
                            msg.setData(bundle);
                            TCPReceiveV2VActivity.handler.sendMessage(msg);
                        }
                    }
                    //            mInStream.close();
                    //            receiver.close();
                    //            mServerSocket.close();
                }
            }
            //接收方主动选择关闭链路
            //发送结束包
//            DataOutputStream mOutStream = new DataOutputStream(mSocket.getOutputStream());
//            mOutStream.write("END!".getBytes());
//            Log.i("END!", "END!");
//            mOutStream.flush();
//            mOutStream.close();
//            //等待server接收余下的数据在释放链路
//            sleep(1000);
            //释放资源
            if (inputStream != null) {
                inputStream.close();
                inputStream = null;

            }
            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }
            if (mServerSocket != null) {
                mServerSocket.close();
                mServerSocket = null;
            }
            TCPReceiveV2VActivity.keepRunning = false;
            this.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}




