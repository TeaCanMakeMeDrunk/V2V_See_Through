package com.example.v2v_test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.example.v2v_test.UDP.ReceivePicThread;

public class CameraReceiveActivity extends AppCompatActivity implements AMap.OnMapTouchListener {
    //有地图定位的接收
    boolean flag = true;
    ReceivePicThread receivePicThread;
    private ImageView photo;
    private TextView FPS;
    private TextView picSize;
    public static Handler handler;        // 用于修改主界面UI

    private Double latitude = null;
    private Double longitude = null;

    private AMap aMap;
    private MapView mapView;
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;


    boolean useMoveToLocationWithMapMode = true;

    //自定义定位小蓝点的Marker
    Marker locationMarker;

    //坐标和经纬度转换工具
    Projection projection;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_receive);

        mapView = new MapView(this);
        //获取地图控件引用
        mapView = (MapView) findViewById(R.id.map);

        photo = findViewById(R.id.photo);
        //FPS=findViewById(R.id.FPS);
        picSize = findViewById(R.id.distance);

        keepRunning = true;
        //开启数据包接收监听
        receivePicThread = new ReceivePicThread(Integer.parseInt(MainActivity.PORT));
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
                        //FPS.setText("FPS："+data.get("FPS"));
                        CameraReceiveActivity.this.picSize.setText("picSize" +
                                "+：" + (int) data.get("picSize") / 1024 + "KB");
                        latitude = (double) data.get("latitude");
                        longitude = (double) data.get("longitude");
                        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
                        while (flag) {
                            mapView.onCreate(savedInstanceState);
                            flag = false;
                        }
                        initAMap();
                        photo.setImageBitmap(bitmap);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 初始化地图
     */
    private void initAMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        if (latitude != null && longitude != null) {

            LatLng latLng = new LatLng(latitude, longitude);
//            Log.i("address", "---" + latitude + "+" + longitude);
            //展示自定义定位小蓝点
            if (locationMarker == null) {
                //首次定位
                locationMarker = aMap.addMarker(new MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker))
                        .anchor(0.5f, 0.5f));

                //首次定位,选择移动到地图中心点并修改级别到15级
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            } else {

            //首次定位,选择移动到地图中心点并修改级别到15级
           // aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            if (useMoveToLocationWithMapMode) {
                //二次以后定位，使用sdk中没有的模式，让地图和小蓝点一起移动到中心点（类似导航锁车时的效果）
                startMoveLocationAndMap(latLng);
            } else {
                startChangeLocation(latLng);
            }

            }
        } else {
            String errText = "定位失败,";
            Log.e("AmapErr", errText);
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        useMoveToLocationWithMapMode = true;
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        useMoveToLocationWithMapMode = false;
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (null != mlocationClient) {
            mlocationClient.onDestroy();
        }
    }

    /**
     * 修改自定义定位小蓝点的位置
     *
     * @param latLng
     */
    private void startChangeLocation(LatLng latLng) {

        if (locationMarker != null) {
            LatLng curLatlng = locationMarker.getPosition();
            if (curLatlng == null || !curLatlng.equals(latLng)) {
                locationMarker.setPosition(latLng);
            }
        }
    }

    /**
     * 同时修改自定义定位小蓝点和地图的位置
     *
     * @param latLng
     */
    private void startMoveLocationAndMap(LatLng latLng) {

        //将小蓝点提取到屏幕上
        if (projection == null) {
            projection = aMap.getProjection();
        }
        if (locationMarker != null && projection != null) {
            LatLng markerLocation = locationMarker.getPosition();
            Point screenPosition = aMap.getProjection().toScreenLocation(markerLocation);
            locationMarker.setPositionByPixels(screenPosition.x, screenPosition.y);

        }

        //移动地图，移动结束后，将小蓝点放到放到地图上
        myCancelCallback.setTargetLatlng(latLng);
        //动画移动的时间，最好不要比定位间隔长，如果定位间隔2000ms 动画移动时间最好小于2000ms，可以使用1000ms
        //如果超过了，需要在myCancelCallback中进行处理被打断的情况
        aMap.animateCamera(CameraUpdateFactory.changeLatLng(latLng), 2000, myCancelCallback);

    }


    MyCancelCallback myCancelCallback = new MyCancelCallback();

    @Override
    public void onTouch(MotionEvent motionEvent) {
        //Log.i("amap","onTouch 关闭地图和小蓝点一起移动的模式");
        useMoveToLocationWithMapMode = false;
    }

    /**
     * 监控地图动画移动情况，如果结束或者被打断，都需要执行响应的操作
     */
    class MyCancelCallback implements AMap.CancelableCallback {

        LatLng targetLatlng;

        public void setTargetLatlng(LatLng latlng) {
            this.targetLatlng = latlng;
        }

        @Override
        public void onFinish() {
            if (locationMarker != null && targetLatlng != null) {
                locationMarker.setPosition(targetLatlng);
            }
        }

        @Override
        public void onCancel() {
            if (locationMarker != null && targetLatlng != null) {
                locationMarker.setPosition(targetLatlng);
            }
        }
    }

    public static boolean keepRunning = true;

    @Override
    public void onBackPressed() {
        //接收视频线程停止
        keepRunning = false;
        // 释放Camera资源
        this.finish();
    }

}
