package com.example.v2v_test.UDP;

import android.graphics.Bitmap;

import com.example.v2v_test.TimeSynActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class Send extends Thread {
    // public static int HEAD_MAX_SIZE = 300;
    //public static int TOTAL_MAX_SIZE = 1500;
    public static int DATA_MAX_SIZE = 1100;

    private Double latitude;
    private Double longitude;

    public Double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * 发送图片，与文本有区别
     *
     * @param ip
     * @param PORT
     * @param bitmap 单帧图片
     * @throws IOException
     * @throws InterruptedException
     */
    static DatagramPacket packet;
    static DatagramSocket socket;

    public void sendPicture(String ip, String port, Bitmap bitmap, Double index) throws IOException {
        InetAddress address = InetAddress.getByName(ip);
        //Bitmap bitmap = BitmapFactory.decodeFile(picturePath);//想要传输图片地址来解析图片  函数重载, Bitmap bitmap变为String picturePath
        ByteArrayOutputStream dataBaos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 18, dataBaos);//图片质量百分60,不然数据包太大
        Map<String, Double> headMap = new HashMap<>();
        //byte[] head=new byte[100];
        byte[] data = dataBaos.toByteArray();
        //Log.i("sendDataSize", String.valueOf(data.length));
        byte[] total;//byte[] total=new byte[1500];
        int count = data.length / DATA_MAX_SIZE;//一次发1100B图片数据
        if (data.length % DATA_MAX_SIZE != 0) {
            //加上每个包包头大小,244是包头大小
            headMap.put("totalSize", (double) data.length + 244 * (count + 1));
        } else {
            headMap.put("totalSize", (double) data.length + 244 * count);
        }
        headMap.put("index", index);
        headMap.put("sendTime", (double) System.currentTimeMillis() + TimeSynActivity.sendDifferenceVal);
//        Log.i("sendSize", String.valueOf(data.length));
        //创建DatagramSocket对象
        socket = new DatagramSocket();
        //不需要地图定位，故取消
        //headMap.put("latitude", getLatitude());
        //headMap.put("longitude", getLongitude());
        for (int offset = 0; offset < count; offset++) {
            //一次最多1500字节
            //0-1499     偏移量0
            //1500-2999  偏移量1
            //3000-3100
            headMap.put("offset", (double) offset);//覆盖原来的值
            //先组装头,再组装数据
            ByteArrayOutputStream subDataBaos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(subDataBaos);
            oos.writeObject(headMap);//把头部输入到baos，total总长度应不超过1500B
            int headSize = subDataBaos.toByteArray().length;
//            Log.i("headSize", String.valueOf(headSize));
            subDataBaos.write(data, offset * DATA_MAX_SIZE, DATA_MAX_SIZE);
            //int totalSize = subDataBaos.toByteArray().length;
            //Log.i("dataSize", String.valueOf(totalSize-headSize));
            total = subDataBaos.toByteArray();
            oos.flush();
            oos.close();
            subDataBaos.flush();
            subDataBaos.close();
            // Log.i("sendSize", String.valueOf(total.length));
            packet = new DatagramPacket(total, 0, total.length, address, Integer.parseInt(port));
            //向服务器端发送数据报
            socket.send(packet);
            //Log.i("sendPacketSize", String.valueOf(packet.getLength()));
        }
        if (data.length % DATA_MAX_SIZE != 0) {//图片还有多出的没发完，再发一次
            headMap.put("offset", (double) count);
            ByteArrayOutputStream subDataBaos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(subDataBaos);
            oos.writeObject(headMap);//把头部输入到baos，total总长度应不超过1500B
            //int headSize=subDataBaos.toByteArray().length;
            //Log.i("headSize", String.valueOf(headSize));
            subDataBaos.write(data, count * DATA_MAX_SIZE, data.length - count * DATA_MAX_SIZE);//只传输1400B图片数据
            //int totalSize=subDataBaos.toByteArray().length;
            //Log.i("dataSize", String.valueOf(totalSize-headSize));
            total = subDataBaos.toByteArray();
            // 关闭资源
            oos.flush();
            oos.close();
            subDataBaos.close();
            subDataBaos.flush();
            packet = new DatagramPacket(total, 0, total.length, address, Integer.parseInt(port));
            socket.send(packet);
            //Log.i("sendPacketSize", String.valueOf(packet.getLength()));
        }
//        sleep(5);//线程停止一会 不然END！标记不能最后一个发送
        String endMark = "END!";
        packet = new DatagramPacket(endMark.getBytes(), endMark.getBytes().length, address, Integer.parseInt(port));
        socket.send(packet);
        socket.close();
        dataBaos.flush();
        dataBaos.close();
//        sleep(5);//线程停止一会 不然END！标记不能最后一个发送
    }
}