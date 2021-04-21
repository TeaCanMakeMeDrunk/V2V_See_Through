package com.example.v2v_test;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.example.v2v_test.UDP.LocationSend;

import java.io.IOException;

public class LocationSendActivity extends AppCompatActivity {
    //有地图定位
    private TextView senderSpeed;
    private TextView longitudeV;
    private TextView latitudeV;
    //声明AMapLocationClient类对象
    private AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
    private AMapLocationClientOption mLocationOption = null;

    LocationSend send = new LocationSend();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_send);
        setTitle("Location Send");
        senderSpeed=findViewById(R.id.senderSpeed);
        longitudeV = findViewById(R.id.longitude);
        latitudeV = findViewById(R.id.latitude);

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
    }

    //声明定位回调监听器
    AMapLocationListener mLocationListener = new AMapLocationListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
//                LatLng latLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                    send.setLatitude(amapLocation.getLatitude());
                    send.setLongitude(amapLocation.getLongitude());
                    senderSpeed.setText("速度"+amapLocation.getSpeed()+" m/s");
                    longitudeV.setText("经度"+ amapLocation.getLongitude());
                    latitudeV.setText("纬度"+amapLocation.getLatitude());
                    send.setSpeed(amapLocation.getSpeed());
//                    Log.i("address",amapLocation.getAddress()+"---"+amapLocation.getLatitude()+"+"+amapLocation.getLongitude()+"+speed"+amapLocation.getSpeed());
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

    @Override
    public void onBackPressed() {
        //接收视频线程停止
        deactivate();
        // 释放Camera资源
        this.finish();
    }

    /**
     * 点击事件
     */
    public void takePictureOnclick(View view) throws IOException {
        send.sendLocation(MainActivity.IP, MainActivity.PORT);
    }
}
