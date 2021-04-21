package com.example.v2v_test;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.CoordinateConverter;
import com.amap.api.location.DPoint;
import com.example.v2v_test.UDP.LocationReceiveThread;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class LocationReceiveActivity extends AppCompatActivity {
    //有地图定位的接收
    LocationReceiveThread locationReceiveThread;
    private TextView distance;
    private TextView receiveSpeed;
    private TextView longitudeV;
    private TextView latitudeV;
    public static Handler handler;        // 用于修改主界面UI

    //保存速度
    private List<Float> velocityList=new ArrayList<>();

    private Double latitude = null;
    private Double longitude = null;

    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_receive);
        setTitle("Location Receive");

        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();

        //设置为高精度定位模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //获取最近3s内精度最高的一次定位结果：
//        mLocationOption.setOnceLocationLatest(true);
        //是指定位间隔
        mLocationOption.setInterval(1000);
        //设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();

        distance = findViewById(R.id.distance);
        receiveSpeed = findViewById(R.id.receiveSpeed);
        longitudeV = findViewById(R.id.longitude);
        latitudeV = findViewById(R.id.latitude);

        keepRunning = true;
        //开启数据包接收监听
        locationReceiveThread = new LocationReceiveThread(Integer.parseInt(MainActivity.PORT));
        if (!locationReceiveThread.isAlive()) {
            locationReceiveThread.start();
        }
        // 绑定线程更改UI
        handler = new Handler(new Handler.Callback() {
            @SuppressLint("SetTextI18n")
            public boolean handleMessage(Message msg) {

                switch (msg.what) {
                    case 1://图片
                        Bundle data = msg.getData();
                        //获得发送者的经纬度
//                        distance.setText(receiveLatitude.toString());
                        float res = -1;
                        if (data.get("latitude") != null && data.get("longitude") != null) {
                            Double receivedLatitude = (double) data.get("latitude");
                            Double receivedLongitude = (double) data.get("longitude");
                            DPoint startLatlng = new DPoint(latitude, longitude);
                            DPoint endLatlng = new DPoint(receivedLatitude, receivedLongitude);
                            res = CoordinateConverter.calculateLineDistance(startLatlng, endLatlng);
                            Log.i("经度", String.valueOf(longitude));
                            Log.i("纬度", String.valueOf(latitude));
                            Log.i("received经度", String.valueOf(receivedLongitude));
                            Log.i("received纬度", String.valueOf(receivedLatitude));
                        }
                        distance.setText(res + " m");
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    //声明定位回调监听器
    AMapLocationListener mLocationListener = new AMapLocationListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
//                LatLng latLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                    longitude = amapLocation.getLongitude();
                    latitude = amapLocation.getLatitude();
                    receiveSpeed.setText("速度" + amapLocation.getSpeed() + " m/s");
                    //保存速度
                    velocityList.add(amapLocation.getSpeed());
                    longitudeV.setText("经度" + amapLocation.getLongitude());
                    latitudeV.setText("纬度" + amapLocation.getLatitude());
                    Log.i("精度", amapLocation.getAccuracy() + " m");
//                    Log.i("address", amapLocation.getAddress() + "---" + amapLocation.getLatitude() + "+" + amapLocation.getLongitude() + "+speed" + amapLocation.getSpeed());
                } else {
                    String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                    Log.e("AmapErr", errText);
                }
            }
        }
    };

    /**
     * 停止定位
     */
    public void deactivate() {
        mLocationOption = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    public static boolean keepRunning = true;

    @Override
    public void onBackPressed() {
        //接收视频线程停止
        keepRunning = false;
        deactivate();
        // 释放Camera资源
        this.finish();
    }

    //创建菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_velocity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.velocitySave: {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    deactivate();//停止定位
                    //保存速度数据
                    writeCsvFile(velocityList);
                }
                break;
            }
        }
        return true;
    }

    private void writeCsvFile(List<Float> velocityList) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            //保存到根目录下
            File file = new File(Environment.getExternalStorageDirectory(), "velocity_data" + dateFormat.format(System.currentTimeMillis()) + ".csv");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            // 添加头部名称
            bw.write("velocity");
            bw.newLine();
            for (float velocity : velocityList) {
                bw.write(velocity + "m/s");
                bw.newLine();
            }
            bw.close();
            velocityList.clear();
            velocityList = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
