package com.example.demo.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.DisplayCutout;
import android.view.View;
import android.view.WindowInsets;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * Created by meiyou.lijiangping
 * 2018年10月24日16:46:57
 */
public class ScreenUtils {

    /**
     * 判断是否是刘海屏
     * @return
     */
    public static boolean isNotchScreen(Context context){

        //TODO 各种品牌的刘海屏检测方案需要补充
        if (isNotchAtXiaomi(context)
                || isNotchAtHuawei(context)
                || isNotchAtOPPO(context)
                || isNotchAtVivo(context)
                || isNotchAtAndroidP(context)){
            return true;
        }

        return false;
    }


    /**
     * Android P 刘海屏判断
     * @param context
     * @return
     */
    private static boolean isNotchAtAndroidP(Context context){
        if (context == null)
            return false;
        if (context instanceof Activity) {
            View decorView = ((Activity)context).getWindow().getDecorView();
            if (decorView != null && android.os.Build.VERSION.SDK_INT >= 28) {
                WindowInsets windowInsets = decorView.getRootWindowInsets();
                if (windowInsets != null) {
                    DisplayCutout displayCutout = windowInsets.getDisplayCutout();
                    return displayCutout != null;
                }
            }
        }
        return false;
    }

    /**
     * 小米手机刘海屏判断方法
     * @return
     */
    private static boolean isNotchAtXiaomi(Context context) {
        int result = 0;
        if ("Xiaomi".equals(Build.MANUFACTURER)){
            try {
                ClassLoader classLoader = context.getClassLoader();
                @SuppressWarnings("rawtypes")
                Class SystemProperties = classLoader.loadClass("android.os.SystemProperties");
                //参数类型
                @SuppressWarnings("rawtypes")
                Class[] paramTypes = new Class[2];
                paramTypes[0] = String.class;
                paramTypes[1] = int.class;
                Method getInt = SystemProperties.getMethod("getInt", paramTypes);
                //参数
                Object[] params = new Object[2];
                params[0] = new String("ro.miui.notch");
                params[1] = new Integer(0);
                result = (Integer) getInt.invoke(SystemProperties, params);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return result == 1;
    }

    /**
     * 华为手机刘海屏判断
     * @return
     */
    private static boolean isNotchAtHuawei(Context context) {
        if (context == null)
            return false;

        boolean ret = false;
        try {
            ClassLoader classLoader = context.getClassLoader();
            Class HwNotchSizeUtil = classLoader.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = HwNotchSizeUtil.getMethod("hasNotchInScreen");
            ret = (boolean) get.invoke(HwNotchSizeUtil);
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (Exception e) {
        } finally {
            return ret;
        }
    }

    private static final int VIVO_NOTCH = 0x00000020;//是否有刘海
    private static final int VIVO_FILLET = 0x00000008;//是否有圆角

    /**
     * Vivo手机刘海屏判断方法
     * @return
     */
    private static boolean isNotchAtVivo(Context context) {
        if (context == null)
            return false;

        boolean ret = false;
        try {
            ClassLoader classLoader = context.getClassLoader();
            Class FtFeature = classLoader.loadClass("android.util.FtFeature");
            Method method = FtFeature.getMethod("isFeatureSupport", int.class);
            ret = (boolean) method.invoke(FtFeature, VIVO_NOTCH);
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (Exception e) {
        } finally {
            return ret;
        }
    }

    /**
     * OPPO手机刘海屏判断方法
     * @return
     */
    private static boolean isNotchAtOPPO(Context context) {
        if (context == null){
            return false;
        }
        return  context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
    }

}
