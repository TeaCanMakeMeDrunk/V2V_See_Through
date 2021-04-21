package com.example.v2v_test.utils;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.v2v_test.R;

public class MyUtils {

    public static void setupItem(final View view, final LibraryObject libraryObject,int position) {
        final Button button =  view.findViewById(R.id.button_item);
        button.setId(position);//不同按钮显示不同功能，得设置ID区分
        button.setText(libraryObject.getTitle());

        final ImageView img = view.findViewById(R.id.img_item);
        img.setId(position);
        img.setImageResource(libraryObject.getRes());
    }

    public static class LibraryObject {

        private String mTitle;
        private int mRes;

        public LibraryObject(final int res, final String title) {
            mRes = res;
            mTitle = title;
        }

        public String getTitle() {
            return mTitle;
        }

        public void setTitle(final String title) {
            mTitle = title;
        }

        public int getRes() {
            return mRes;
        }

        public void setRes(final int res) {
            mRes = res;
        }
    }
}
