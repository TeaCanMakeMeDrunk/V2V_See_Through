package com.example.v2v_test.UDP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class LocationSend extends Thread {
    private Double latitude;
    private Double longitude;
    private float speed;

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void sendLocation(String ip, String port) throws IOException {
        InetAddress address = InetAddress.getByName(ip);
        Map<String, Double> headMap = new HashMap<>();
        DatagramSocket socket = new DatagramSocket();
        //不需要地图定位，故取消
        headMap.put("latitude", getLatitude());
        headMap.put("longitude", getLongitude());
//        Log.i("latitude", String.valueOf(getLatitude()));
//        Log.i("longitude", String.valueOf(getLongitude()));
        //先组装头,再组装数据
        ByteArrayOutputStream subDataBaos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(subDataBaos);
        oos.writeObject(headMap);//把头部输入到baos，total总长度应不超过1500B
        int headSize = subDataBaos.toByteArray().length;
//        Log.i("headSize", String.valueOf(headSize));
        //int totalSize = subDataBaos.toByteArray().length;
        //Log.i("dataSize", String.valueOf(totalSize-headSize));
        byte[] total = subDataBaos.toByteArray();
        oos.flush();
        oos.close();
        subDataBaos.flush();
        subDataBaos.close();
        // Log.i("sendSize", String.valueOf(total.length));
        DatagramPacket packet = new DatagramPacket(total, 0, total.length, address, Integer.parseInt(port));
        //向服务器端发送数据报
        socket.send(packet);
//        String endMark = "END!";
//        packet = new DatagramPacket(endMark.getBytes(), endMark.getBytes().length, address, Integer.parseInt(port));
//        socket.send(packet);
        socket.close();
    }
}