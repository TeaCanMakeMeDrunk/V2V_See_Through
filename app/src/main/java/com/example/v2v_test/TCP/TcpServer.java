package com.example.v2v_test.TCP;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class TcpServer extends Thread {

    private ServerSocket mServerSocket;
    private Socket receiver;

    private Socket sender;

    private DataOutputStream mOutStream;

    private DataInputStream inputStream;

    @Override
    public void run() {
        try {
            while (true) {
                if (mServerSocket == null && receiver == null) {
                    mServerSocket = new ServerSocket(4040);
//                    System.out.println(" mServerSocket.accept(),waiting...");
                    receiver = mServerSocket.accept();
//                    System.out.println("transporting...");
                }
                inputStream = new DataInputStream(receiver.getInputStream());

                int headSize = 244;
                byte[] head = new byte[headSize];

                int len = 0;
                String temp = null;
                while (len < headSize) {
                    len += inputStream.read(head, len, headSize - len);
//                    System.out.println("len:" + len);
                    temp = new String(head, 0, 4);
                    if (temp.startsWith("END!") || len < 0) {
                        break;
                    }
                }
                if (temp.startsWith("END!") || len < 0) {
                    if (inputStream != null) {
                        inputStream.close();
                        inputStream = null;
                    }
                    if (mOutStream != null) {
                        mOutStream.flush();
                        mOutStream.close();
                        mOutStream = null;
                    }
                    if (sender != null) {
                        sender.close();
                        sender = null;
                    }
                    if (receiver != null) {
                        receiver.close();
                        receiver = null;
                    }
                    if (mServerSocket != null) {
                        mServerSocket.close();
                        mServerSocket = null;
                    }
//                    System.out.println("close success!");
                    break;
                }
                ByteArrayInputStream byteInt = new ByteArrayInputStream(head);
                ObjectInputStream objInt = new ObjectInputStream(byteInt);
                @SuppressWarnings("unchecked")
                Map<String, Double> headMap = (Map<String, Double>) objInt.readObject();
                objInt.close();
                byteInt.close();
                Double totalSize = headMap.get("totalSize");
                byte[] picData = new byte[totalSize.intValue() - headSize];

                len = 0;
                while (len < totalSize.intValue() - headSize) {
                    len += inputStream.read(picData, len, totalSize.intValue() - headSize - len);
                }


                byte[] total = new byte[totalSize.intValue()];
                System.arraycopy(head, 0, total, 0, head.length);
                System.arraycopy(picData, 0, total, head.length, picData.length);

                if (sender == null) {
                    sender = new Socket("192.168.123.111", 4040);
                    mOutStream = new DataOutputStream(sender.getOutputStream());
                }
                mOutStream.write(total);
            }
            startService();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        startService();
    }

    private static void startService() {
        TcpServer tcpServer = new TcpServer();
        tcpServer.start();
    }
}

