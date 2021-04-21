package com.example.v2v_test.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.v2v_test.R;
import com.example.v2v_test.utils.MyUtils;

import static com.example.v2v_test.utils.MyUtils.setupItem;


public class HorizontalPagerAdapter extends PagerAdapter {

    private final MyUtils.LibraryObject[] LIBRARIES = new MyUtils.LibraryObject[]{
            new MyUtils.LibraryObject(
                    R.drawable.ic_development,
                    "Time synchronize"//对应id 0

            ),
            new MyUtils.LibraryObject(
                    R.drawable.ic_strategy,
                    "DSRC Send" //对应id 1
            ),
            new MyUtils.LibraryObject(
                    R.drawable.ic_qa,
                    "DSRC Receive"//对应id 2
            ),
            new MyUtils.LibraryObject(
                    R.drawable.ic_qa,
                    "4G Receive"//对应id 3
            ),
            new MyUtils.LibraryObject(
                    R.drawable.ic_design,
                    "4G Send"//对应id 4
            ),
            new MyUtils.LibraryObject(
                    R.drawable.ic_design,
                    "Location Send"//对应id 5
            ),
            new MyUtils.LibraryObject(
                    R.drawable.ic_qa,
                    "Location Receive"//对应id 6
            )
    };

    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private boolean mIsTwoWay;

    public HorizontalPagerAdapter(final Context context, final boolean isTwoWay) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mIsTwoWay = isTwoWay;
    }

    @Override
    public int getCount() {
        return mIsTwoWay ? 6 : LIBRARIES.length;
    }

    @Override
    public int getItemPosition(final Object object) {
        return POSITION_NONE;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        final View view;
        if (mIsTwoWay) {
            view = mLayoutInflater.inflate(R.layout.two_way_item, container, false);
        } else {
            view = mLayoutInflater.inflate(R.layout.item, container, false);
            setupItem(view, LIBRARIES[position], position);
        }

        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(final View view, final Object object) {
        return view.equals(object);
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        container.removeView((View) object);
    }
}
