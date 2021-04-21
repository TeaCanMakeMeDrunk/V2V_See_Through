package com.example.v2v_test.TcpServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Observable;

public class ClientThread extends Observable implements Runnable {
    private int count;
    private Socket s;
    private List<Socket> li;

    public ClientThread(Socket s, int count, List<Socket> li) {
        this.s = s;
        this.count = count;
        this.li = li;
    }

    @Override
    public void run() {
        try {
            DataInputStream dataInput = new DataInputStream(s.getInputStream());

            byte[] data = new byte[4];
            while (-1 != (dataInput.read(data))) {
//                String temp = new String(data, 0, 4);
//                if (temp.startsWith("END!")) {
//                    li.remove(s);
//                    s.close();
//                    s = null;
//                    break;
//                }
                if (!li.isEmpty()) {
                    for (Socket _s : li) {
//                        if (!_s.equals(s)) {
                        DataOutputStream dataOutput = new DataOutputStream(_s.getOutputStream());
                        dataOutput.write(data);
                        dataOutput.flush();
//                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
