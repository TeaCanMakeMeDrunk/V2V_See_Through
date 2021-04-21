package com.example.v2v_test.TcpServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TcpServerBroadcast {

    public static void main(String[] args) {
        int count = 0;

        List<Socket> li = Collections.synchronizedList(new ArrayList<Socket>());

        while (true) {
            System.out.println("Server is waiting connection...");
            try (ServerSocket ss = new ServerSocket(4040)) {
                for (Socket tmpS : li) {
//                    System.out.println("isConnected:"+tmpS.getOutputStream()+  "  isOutputShutdown "+tmpS.isInputShutdown());
                    if(!tmpS.isConnected()){
                        li.remove(tmpS);
                        tmpS.close();
                    }
                }
                Socket s = ss.accept();

                count++;
                li.add(s);

                new Thread(new ClientThread(s, count, li)).start();
                System.out.println("Connect success!");


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

