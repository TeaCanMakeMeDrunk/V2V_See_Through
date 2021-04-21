package com.example.v2v_test;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.v2v_test.TCP.ReceivePicThread;
import com.example.v2v_test.utils.DemoBase;
import com.example.v2v_test.utils.MyMarkerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TCPReceiveV2VActivity extends DemoBase {
    //无地图定位的接收，有时延分析
    boolean flag = true;
    ReceivePicThread receivePicThread;
    private ImageView photo;
    private TextView FPS;
    private TextView picSize;
    public static Handler handler;        // 用于修改主界面UI

    private Map<Integer, Integer> delayData = new HashMap<>();
    private List<Integer> totalSize = new ArrayList<>();

    private LineChart chart;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTitle("4G Video Receive");
        setContentView(R.layout.activity_tcp_v2v_receive);

        photo = findViewById(R.id.photo);
        //FPS=findViewById(R.id.FPS);
        picSize = findViewById(R.id.distance);

        chart = findViewById(R.id.chart1);

        keepRunning = true;
        //从缓冲队列出取出数据并处理
        receivePicThread = new ReceivePicThread(Integer.parseInt(MainActivity.PORT));
        //关闭 accept 之前的资源
        if (ReceivePicThread.mServerSocket != null) {
            try {
                ReceivePicThread.mServerSocket.close();
                ReceivePicThread.mServerSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!receivePicThread.isAlive()) {
            receivePicThread.start();
        }

        // 绑定线程更改UI
        handler = new Handler(new Handler.Callback() {
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 1://图片
                        Bitmap bitmap = BitmapFactory.decodeByteArray((byte[]) msg.obj, 0, ((byte[]) msg.obj).length);
                        //对图片进行压缩处理
                        //Matrix matrix = new Matrix();
                        //matrix.setRotate(0);
                        Bundle data = msg.getData();
                        TCPReceiveV2VActivity.this.picSize.setText("totalSize：" + (int) data.get("totalSize") / 1024 + "KB");
                        //先显示图片，再画时延表
                        photo.setImageBitmap(bitmap);
                        Integer delayTime = (Integer) data.get("FPS");
                        Integer index = (Integer) data.get("index");
                        if (delayTime > 500 || delayTime < 0) {
                            delayTime = 0;
                        }
                        totalSize.add((int) data.get("totalSize"));
                        delayData.put(index, delayTime);
                        setData(delayData);
//                        Log.i("delay", String.valueOf(index));
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    //初始化时延曲线图数据
    private void setData(Map<Integer, Integer> delayData) {
        //先配置属性,再赋值
        chart.setDrawGridBackground(false);
        chart.getDescription().setEnabled(false);

        // enable touch gestures
        chart.setTouchEnabled(true);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
        mv.setChartView(chart); // For bounds control
        chart.setMarker(mv); // Set the marker to the chart

        XAxis xl = chart.getXAxis();
        xl.setAvoidFirstLastClipping(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setAxisMinimum(0f);

        YAxis leftAxis = chart.getAxisLeft();
        //保证Y轴从0开始，不然会上移一点
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setAxisMinimum(0f);
        rightAxis.setEnabled(false);

        //属性配置结束，开始赋值
        ArrayList<Entry> entries = new ArrayList<>();

        for (Map.Entry<Integer, Integer> auto : delayData.entrySet()) {
            entries.add(new Entry(auto.getKey(), auto.getValue()));
        }

        // sort by x-value
        Collections.sort(entries, new EntryXComparator());

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(entries, "delay time");

//        set1.setLineWidth(1.5f);
        set1.setCircleRadius(4f);

        // create a data object with the data sets
        LineData data = new LineData(set1);
        // set data
        chart.setData(data);

        //重新刷新setData（x,y） x 数量，y范围
        //绘制时间
        chart.animateX(20);
        // redraw
        chart.invalidate();

    }

    public static boolean keepRunning = true;

    @Override
    public void onBackPressed() {
        //接收视频线程停止
        keepRunning = false;
        // 释放Camera资源
        this.finish();
    }

    //创建菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_data, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionSave: {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    //保存delay data
                    writeCsvFile(delayData, totalSize);
                    saveToGallery();
                } else {
                    requestStoragePermission(chart);
                }
                break;
            }
        }
        return true;
    }

    @Override
    protected void saveToGallery() {
        saveToGallery(chart, "delayTime");
    }

    private void writeCsvFile(Map<Integer, Integer> delayData, List<Integer> totalSize) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            //保存到根目录下
            File file = new File(Environment.getExternalStorageDirectory(), "delay_data" + dateFormat.format(System.currentTimeMillis()) + ".csv");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            // 添加头部名称
            bw.write("id" + "," + "delay_time" + "," + "totalSize");
            bw.newLine();
            int i = 0;
            for (Map.Entry<Integer, Integer> auto : delayData.entrySet()) {
                bw.write(auto.getKey() + "," + auto.getValue() + "," + totalSize.get(i++));
                bw.newLine();
            }
            bw.close();
            delayData.clear();
            delayData = null;
            totalSize.clear();
            totalSize = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
