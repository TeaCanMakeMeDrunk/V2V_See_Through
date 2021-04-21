package com.example.v2v_test;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.v2v_test.utils.SntpClient;

import java.text.SimpleDateFormat;

public class TimeSynActivity extends AppCompatActivity {

    private TextView ntpTime;
    private TextView differentValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_syn);

        ntpTime = findViewById(R.id.ntpTime);
        differentValue = findViewById(R.id.differentValue);

    }

    //系统同步ntp服务器时间
    private SntpClient sntpClient = new SntpClient();
    private final String NTP_ALIYUN = "ntp1.aliyun.com";
    private final int TIME_OUT = 500;
    public static double currentTime;
    public static double differenceVal;
    public static double sendDifferenceVal;
    public static double receiveDifferenceVal;
    public void senderTimeSyn(View view) {
        Thread t = new Thread() {
            @Override
            public void run() {
                //TODO 发送同步时间数据包
                //同步本地系统时间
                if (sntpClient.requestTime(NTP_ALIYUN, TIME_OUT)) {
                    currentTime = sntpClient.getNtpTime() + SystemClock.elapsedRealtime() - sntpClient.getNtpTimeReference();
                    sendDifferenceVal = currentTime - System.currentTimeMillis();
                    differenceVal = currentTime - System.currentTimeMillis();
                }
            }
        };
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
//        ntpTime.setText(dateFormat.format(currentTime));
        t.start();
        t.interrupt();//中断线程

    }

    public void receiverTimeSyn(View view) {
        Thread t = new Thread() {
            @Override
            public void run() {
                //TODO 接收同步时间数据包
                if (sntpClient.requestTime(NTP_ALIYUN, TIME_OUT)) {
                    currentTime = sntpClient.getNtpTime() + SystemClock.elapsedRealtime() - sntpClient.getNtpTimeReference();
                    receiveDifferenceVal = currentTime - System.currentTimeMillis();
                    differenceVal = currentTime - System.currentTimeMillis();
                }
            }
        };
        t.start();
        t.interrupt();//中断线程
//
    }

    public void showTime(View view) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss:SSS");
        ntpTime.setText(dateFormat.format(currentTime));
        differentValue.setText(String.valueOf(differenceVal));
    }

}
