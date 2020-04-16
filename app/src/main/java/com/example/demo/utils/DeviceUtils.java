package com.example.demo.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.Field;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Created by linwuhan on 2015/6/26.
 * 获取设备相关信息
 */
@SuppressLint("DefaultLocale")
public class DeviceUtils {


    private static final String TAG ="DeviceUtils" ;
    //private static WebView webview;
    private static String strUA="";

    private static  String mUUID;

    public synchronized static String getUUID(Context context) {
        if(mUUID==null){
            SharedPreferences preference = PreferenceManager
                    .getDefaultSharedPreferences(context);
            String identity = preference.getString("identity_android", null);
            if (identity == null) {
                identity = UUID.randomUUID().toString();
                preference.edit().putString("identity_android", identity).apply();
            }
            mUUID = identity;
        }
        return mUUID;

    }


    //private static int mScreenWidth;
    //private static  int mScreenHeight;
    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public static int getScreenWidth(Context context) {
        try {
            return  context.getResources().getDisplayMetrics().widthPixels;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 480;
    }

    /**
     * 获取屏幕高度
     */
    public static int getScreenHeight(Context context) {
        try {
            //if(mScreenHeight==0){
                return  context.getResources().getDisplayMetrics().heightPixels;
            //}
            //return mScreenHeight;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 800;

    }


    /**
     * 获取真实屏幕高度
     * @param context
     * @return
     */
    private static final int PORTRAIT = 0;
    private static final int LANDSCAPE = 1;
    //private volatile static Point[] mRealSizes = new Point[2];

    public static int getScreenRealHeight(Context context){
        if (ScreenUtils.isNotchScreen(context)){
            return getNotchScreenHeight(context);
        }else{
            return getScreenHeight(context);
        }
    }

    public static int getNotchScreenHeight(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return getScreenHeight(context);
        }

        int orientation = context.getResources().getConfiguration().orientation;
        orientation = orientation == Configuration.ORIENTATION_PORTRAIT ? PORTRAIT : LANDSCAPE;
        Point[] mRealSizes = new Point[2];
        if (mRealSizes[orientation] == null) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager == null) {
                return getScreenHeight(context);
            }
            Display display = windowManager.getDefaultDisplay();
            Point point = new Point();
            display.getRealSize(point);
            mRealSizes[orientation] = point;
        }
        return mRealSizes[orientation].y;
    }


    public static float getDeviceDensity(Context context) {
        try {
            if(mDensity==0){
                mDensity = context.getResources().getDisplayMetrics().density;
            }
            return mDensity;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 480;

    }
    private static int  mDensityDpi;
    public static int getDeviceDensityValue(Context context) {
        //   Display display = context.getWindowManager().getDefaultDisplay();
        //   return display.getHeight();
        try {
            if(mDensityDpi==0)
                 mDensityDpi = context.getResources().getDisplayMetrics().densityDpi;
            return mDensityDpi;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 480;

    }

     private static float  mDensity;
    /**
     * dip到px的转换
     *
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        if(mDensity==0)
            mDensity = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * mDensity + 0.5f);
    }

    private static float  mScaledDensity;
    /**
     * 将px值转换为sp值，保证文字大小不变
     * @param pxValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        if(mScaledDensity==0)
          mScaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / mScaledDensity + 0.5f);
    }

    /**
     * sp -> px
     *
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        if(mScaledDensity==0)
            mScaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * mScaledDensity + 0.5f);
    }

    /**
     * px到dip的转换
     *
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        if(mDensity==0)
            mDensity = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / mDensity + 0.5f);
    }

    /**
     * 隐藏键盘
     *
     * @param activity
     */
    public static void hideKeyboard(Activity activity) {
        try {
            ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    public static void hideKeyboard(Activity activity, View view) {
        try {
            if (view == null){
                view = activity.getCurrentFocus();
            }
            ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public static void setDisableKeyboardOpt(boolean flag){
    }
    /**
     * 显示键盘
     *
     * @param activity
     * @param view
     */
    public static void showkeyboard(Activity activity, View view) {
        try {
            ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(view, 0);
        } catch (Exception e) {
            e.printStackTrace();//
        }
    }


    //是否显示了键盘
    public static boolean isShowkeyboard(Context context) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            return imm.isActive();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;

    }



    private static String imsi;

    private static int phoneType;
    public static int getPhoneType(Context context) {
        try {
            if(phoneType==0){
                TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                phoneType = telephony.getPhoneType();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return phoneType;
    }

    /**
     * 设备名称
     *
     * @return
     */
    private static String phoneModel;
    public static String getPhoneModel() {
        if(phoneModel==null){
            phoneModel  =Build.MODEL;
        }
        return phoneModel;
    }

    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;

    }

    public static String getTimeZoneName() {
        try {
            TimeZone tz = TimeZone.getDefault();
            // String s = "TimeZone   "+tz.getDisplayName(false,
            // TimeZone.SHORT)+" Timezon id :: " +tz.getID();
            // LogUtils.dln(s);

            return tz.getDisplayName(false, TimeZone.SHORT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public static boolean isInstall(Context context, String packagename) {
        boolean isInstall = false;
        try {
            PackageInfo packageInfo;
            try {
                packageInfo = context.getPackageManager().getPackageInfo(
                        packagename, PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                packageInfo = null;
                e.printStackTrace();
            }
            if (packageInfo == null) {
                isInstall = false;
            } else {
                isInstall = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isInstall;
    }

    private static  int mStatusBarHeight;
    /**
     * 获取状态栏高度
     *
     * @param activity
     * @return
     */
    public static int getStatusBarHeight(Activity activity) {
        if(mStatusBarHeight!=0){
            return mStatusBarHeight;
        }
        int statusBarHeight = 0;
        //尝试第一种获取方式
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = activity.getResources().getDimensionPixelSize(resourceId);
            if(statusBarHeight > 0){
                mStatusBarHeight = statusBarHeight;
                return statusBarHeight;
            }
        }
        if(statusBarHeight <= 0){
            //第一种失败时, 尝试第二种获取方式
            Rect rectangle = new Rect();
            Window window = activity.getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
            statusBarHeight = rectangle.top;
            if(statusBarHeight > 0){
                mStatusBarHeight = statusBarHeight;
                return statusBarHeight;
            }
        }
        if(statusBarHeight <= 0 ){
            try {
                Class<?> c = null;
                Object obj = null;
                Field field = null;
                int x = 0;
                c = Class.forName("com.android.internal.R$dimen");
                obj = c.newInstance();
                field = c.getField("status_bar_height");
                x = Integer.parseInt(field.get(obj).toString());
                statusBarHeight = activity.getResources().getDimensionPixelSize(x);
                mStatusBarHeight = statusBarHeight;
                return statusBarHeight;
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        mStatusBarHeight=  DeviceUtils.dip2px(activity, 20);
        return mStatusBarHeight;
    }

}
