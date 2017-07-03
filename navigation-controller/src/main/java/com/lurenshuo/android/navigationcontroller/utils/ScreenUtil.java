package com.lurenshuo.android.navigationcontroller.utils;

import android.content.Context;
import android.util.DisplayMetrics;


/**
 * Created by Li dajun
 * Date: 2016-03-11
 * Time: 14:22
 */
public class ScreenUtil {

    /**
     * 获取屏幕尺寸
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }
}
