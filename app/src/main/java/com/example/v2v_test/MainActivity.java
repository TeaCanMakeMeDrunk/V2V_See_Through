package com.example.v2v_test;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.v2v_test.adapters.MainPagerAdapter;
import com.gigamole.navigationtabstrip.NavigationTabStrip;
import com.github.florent37.expectanim.ExpectAnim;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.github.florent37.expectanim.core.Expectations.aboveOf;
import static com.github.florent37.expectanim.core.Expectations.atItsOriginalPosition;
import static com.github.florent37.expectanim.core.Expectations.bottomOfParent;
import static com.github.florent37.expectanim.core.Expectations.invisible;
import static com.github.florent37.expectanim.core.Expectations.leftOfParent;
import static com.github.florent37.expectanim.core.Expectations.outOfScreen;
import static com.github.florent37.expectanim.core.Expectations.rightOfParent;
import static com.github.florent37.expectanim.core.Expectations.sameCenterVerticalAs;
import static com.github.florent37.expectanim.core.Expectations.toHaveBackgroundAlpha;
import static com.github.florent37.expectanim.core.Expectations.toHaveTextColor;
import static com.github.florent37.expectanim.core.Expectations.toRightOf;
import static com.github.florent37.expectanim.core.Expectations.visible;
import static com.github.florent37.expectanim.core.Expectations.width;

public class MainActivity extends AppCompatActivity {

    //通过id绑定UI特效按钮
    @BindView(R.id.ip)
    View ip;
    @BindView(R.id.textView)
    View textView;
    @BindView(R.id.port)
    View port;
    @BindView(R.id.result)
    View result;
    @BindView(R.id.follow)
    View follow;
    @BindView(R.id.ping)
    View ping;
    @BindView(R.id.bottomLayout)
    View bottomLayout;
    @BindView(R.id.content)
    View content;

    private ExpectAnim expectAnimMove;

    //获取组件的输入值
    private EditText ipEditText;
    private EditText portEditText;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expectanim_activity_sample);
        ButterKnife.bind(this);

        //加载各种组件
        ipEditText = findViewById(R.id.ip);
        portEditText = findViewById(R.id.port);
        resultTextView = findViewById(R.id.result);

        //功能界面
        final ViewPager viewPager = findViewById(R.id.vp_main);
        viewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(2);
        final NavigationTabStrip navigationTabStrip = findViewById(R.id.nts);
        //导航菜单
        navigationTabStrip.setTitles("Basic function");
        navigationTabStrip.setViewPager(viewPager);

        //UI特效
        new ExpectAnim()
                .expect(bottomLayout)
                .toBe(
                        outOfScreen(Gravity.BOTTOM)
                )
                .expect(content)
                .toBe(
                        outOfScreen(Gravity.BOTTOM),
                        invisible()
                )
                .toAnimation()
                .setNow();

        this.expectAnimMove = new ExpectAnim()

                .expect(textView)
                .toBe(
                        bottomOfParent().withMarginDp(36),
                        leftOfParent().withMarginDp(16),
                        width(40).toDp().keepRatio()
                )

                .expect(ip)
                .toBe(
                        toRightOf(textView).withMarginDp(16),
                        sameCenterVerticalAs(textView),
                        toHaveTextColor(Color.WHITE)
                )

                .expect(port)
                .toBe(
                        toRightOf(ip).withMarginDp(5),
                        sameCenterVerticalAs(ip),
                        toHaveTextColor(Color.WHITE)
                )

                .expect(result)
                .toBe(
                        toRightOf(port).withMarginDp(5),
                        sameCenterVerticalAs(port),
                        toHaveTextColor(Color.WHITE)
                )

                .expect(follow)
                .toBe(
                        rightOfParent().withMarginDp(4),
                        bottomOfParent().withMarginDp(12),
                        toHaveBackgroundAlpha(0f)
                )

                .expect(ping)
                .toBe(
                        aboveOf(follow).withMarginDp(4),
                        rightOfParent().withMarginDp(4),
                        toHaveBackgroundAlpha(0f)
                )

                .expect(bottomLayout)
                .toBe(
                        atItsOriginalPosition()
                )

                .expect(content)
                .toBe(
                        atItsOriginalPosition(),
                        visible()
                )

                .toAnimation()
                .setDuration(1500);
    }

    public static String IP;//ip地址
    public static String PORT;//端口号

    /**
     * PING按钮 点击事件
     *
     * @param
     * @return
     */
    @OnClick(R.id.ping)
    public void onMoveClicked() {
        IP = ipEditText.getText().toString();
        PORT = portEditText.getText().toString();
        //ping  IP地址
        if (isConnectedServer(IP)) {
            resultTextView.setText("success");
            //UI特效
            expectAnimMove.start();
        } else {
            resultTextView.setText("failure");
        }
    }

    /**
     * ping IP地址 是否连通
     *
     * @param ip
     * @return 成功true 失败false
     */
    public boolean isConnectedServer(String ip) {
        try {
            Process process = Runtime.getRuntime().exec("/system/bin/ping -c 1 " + ip);
            int value = process.waitFor();
            if (value == 0) {
                Log.i("result", "与 " + ip + " 连接畅通.");
                return true;
            } else {
                Log.i("result", "与 " + ip + " 连接不畅通.");
                return false;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    @OnClick(R.id.follow)
    public void onResetClicked() {
        expectAnimMove.reset();
    }


    /**
     * 点击跳转各种不同的功能界面
     */
    public void onclick(View view) {
        Intent intent;
        switch (view.getId()) {//点击不同按钮显示不同功能  功能查看移步HorizontalPagerAdapter
            case 0://时间同步
                intent = new Intent(this, TimeSynActivity.class);
                startActivity(intent);
                break;
            case 1://UDP 发
                CameraSendV2VActivity.index = 0;
                intent = new Intent(this, CameraSendV2VActivity.class);
                startActivity(intent);
                break;
            case 2://UDP 接
                intent = new Intent(this, CameraReceiveV2VActivity.class);
                startActivity(intent);
                break;
            case 3://TCP 接
                intent = new Intent(this, TCPReceiveV2VActivity.class);
                startActivity(intent);
                break;
            case 4://TCP 发
                TCPSendV2VActivity.index = 0;
                intent = new Intent(this, TCPSendV2VActivity.class);
                startActivity(intent);
                break;
            case 5://根据经纬度测距离
                intent = new Intent(this, LocationSendActivity.class);
                startActivity(intent);
                break;
            case 6://根据经纬度测距离
                intent = new Intent(this, LocationReceiveActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

}