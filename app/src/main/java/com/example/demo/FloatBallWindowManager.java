package com.example.demo;

import android.app.Application;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Environment;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.example.demo.utils.DeviceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * create at：2019/11/28 on 11:30 AM
 * des:全局悬浮球管理类
 * author:hucanhui
 */
public class FloatBallWindowManager {
    /**
     * 默认位置
     */
    private int deaultY;
    private int mFloatY;
    private static volatile FloatBallWindowManager mInstance;
    private Context context;
    private WindowManager wm;
    private Map<View, WindowManager.LayoutParams> viewLayoutParamsMap = new HashMap<>();
    private Map<View, Integer> edgeLeft = new HashMap<>();
    private Map<View, Integer> edgeRight = new HashMap<>();
    private Map<View, Integer> viewHeightMap = new HashMap<>();
    private int statusHeight;
    private int screenWidth;
    private int screenHeight;

    private FloatBallWindowManager(Context context) {
        this.context = context;
        //通过像素密度来设置按钮的大小
        statusHeight = getStatusHeight(context);
        screenHeight = DeviceUtils.getScreenRealHeight(context);
        wm = (WindowManager) context.getSystemService(Application.WINDOW_SERVICE);
        screenWidth = DeviceUtils.getScreenWidth(context);
        deaultY = screenHeight - DeviceUtils.dip2px(context, 200);
        screenHeight = screenHeight - DeviceUtils.dip2px(context, 140);
        mFloatY = deaultY;
    }


    public static FloatBallWindowManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (FloatBallWindowManager.class) {
                if (mInstance == null) {
                    mInstance = new FloatBallWindowManager(context);
                }
            }
        }
        return mInstance;
    }

    public void addFloatBall(View view, int wmParamsX, int viewWidth, int viewHeight, View.OnClickListener onClickListener) {
        try {
            if (viewLayoutParamsMap.containsKey(view)){
                return;
            }
            edgeLeft.put(view, wmParamsX);
            edgeRight.put(view, DeviceUtils.getScreenWidth(context) - wmParamsX - viewWidth);
            int y = deaultY;

            List<WindowManager.LayoutParams> list = new ArrayList<>();
            for (Map.Entry<View, WindowManager.LayoutParams> entry : viewLayoutParamsMap.entrySet()) {
                if (entry.getValue().x < screenWidth/2){
                    list.add(entry.getValue());
                }
            }
            Collections.sort(list, new Comparator<WindowManager.LayoutParams>() {
                @Override
                public int compare(WindowManager.LayoutParams params, WindowManager.LayoutParams t1) {
                    return t1.y - params.y;
                }
            });
            if (list.size() == 1 && (list.get(0).y + list.get(0).height < y + DeviceUtils.dip2px(context, 8) ||
                    list.get(0).y > y + viewHeight + DeviceUtils.dip2px(context, 8))) {

            }else {
                for (int i = 0; i < list.size(); i++) {
                    if (i == list.size() - 1) {
                        y = list.get(i).y - DeviceUtils.dip2px(context, 8) - viewHeight;
                    } else if ((list.get(i).y - (list.get(i + 1).y + list.get(i + 1).height)) >= DeviceUtils.dip2px(context, 16) + viewHeight) {
                        y = list.get(i).y - DeviceUtils.dip2px(context, 8) - viewHeight;
                        break;
                    }
                }
            }

            viewHeightMap.put(view, viewHeight);
            WindowManager.LayoutParams layoutParams = getViewParams(wmParamsX, y, viewWidth, viewHeight);
            viewLayoutParamsMap.put(view, layoutParams);
            view.setOnTouchListener(new BallOnTouchListener(layoutParams, onClickListener));
            wm.addView(view, layoutParams);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void removeBall(View view){
        try {
            if (viewLayoutParamsMap.containsKey(view)){
                wm.removeViewImmediate(view);
                viewHeightMap.remove(view);
                viewLayoutParamsMap.remove(view);
                edgeLeft.remove(view);
                edgeRight.remove(view);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void autoAnimTranslate(View view, final int offsetX) {
        try {
            WindowManager.LayoutParams layoutParams = viewLayoutParamsMap.get(view);
            if (layoutParams == null || view == null) {
                return;
            }
            layoutParams.x = offsetX + layoutParams.x;
            int left = edgeLeft.get(view);
            int right = edgeRight.get(view);
            if (layoutParams.x <= left) {
                layoutParams.x = left;
            }
            if (layoutParams.x >= right) {
                layoutParams.x = right;
            }
            wm.updateViewLayout(view, layoutParams);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取参数
     *
     * @return
     */
    private WindowManager.LayoutParams getViewParams(int wmParamsX, int wmParamsY, int viewWidth, int viewHeight) {
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        //设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        //设置悬浮窗口长宽数据 --用RelativeLayout注意赋具体值
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        } else if (isMIUI() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        } else if (Build.MODEL != null && Build.MODEL.equalsIgnoreCase("Vivo X7")) {
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        } else {
            wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        }
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        wmParams.x = wmParamsX;
        wmParams.y = wmParamsY;
        wmParams.width = viewWidth;
        wmParams.height = viewHeight;
        return wmParams;
    }

    /**
     * 检查手机是否是miui
     *
     * @return
     * @ref http://dev.xiaomi.com/doc/p=254/index.html
     */
    public static boolean isMIUI() {
        String device = Build.MANUFACTURER;
        System.out.println("Build.MANUFACTURER = " + device);
        if (device.equals("Xiaomi")) {
            System.out.println("this is a xiaomi device");
            Properties prop = new Properties();
            try {
                prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return prop.getProperty("ro.miui.ui.version.code", null) != null
                    || prop.getProperty("ro.miui.ui.version.name", null) != null
                    || prop.getProperty("ro.miui.internal.storage", null) != null;
        } else {
            return false;
        }
    }


    /**
     * 获得状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusHeight(Context context) {
        int statusHeight = -1;
        try {
            Class clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    private class BallOnTouchListener implements View.OnTouchListener {

        private float x;
        private float y;
        private long mCurrentTime;
        private long mLastTime;
        private float mStartX;
        private float mStartY;
        private float mLastX;
        private float mLastY;
        private WindowManager.LayoutParams params;
        private View.OnClickListener onClickListener;
        private float mTouchStartX;
        private float mTouchStartY;

        public BallOnTouchListener(WindowManager.LayoutParams params, View.OnClickListener onClickListener) {
            this.params = params;
            this.onClickListener = onClickListener;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            //获取相对屏幕的坐标，即以屏幕左上角为原点
            x = motionEvent.getRawX();
            //statusHeight是系统状态栏的高度
            y = motionEvent.getRawY() - statusHeight;
            try {
                switch (motionEvent.getAction()) {
                    //捕获手指触摸按下动作
                    case MotionEvent.ACTION_DOWN:
                        //获取相对View的坐标，即以此View左上角为原点
                        //获取相对View的坐标，即以此View左上角为原点
                        mTouchStartX = motionEvent.getX();
                        mTouchStartY = motionEvent.getY();
                        mStartX = motionEvent.getRawX();
                        mStartY = motionEvent.getRawY();
                        mLastTime = System.currentTimeMillis();
                        break;

                    //捕获手指触摸移动动作
                    case MotionEvent.ACTION_MOVE:
                        updateViewPosition(view, params, x, y, mTouchStartX, mTouchStartY);
                        break;

                    //捕获手指触摸离开动作
                    case MotionEvent.ACTION_UP:
                        mLastX = motionEvent.getRawX();
                        mLastY = motionEvent.getRawY();

                        // 抬起手指时让floatView紧贴屏幕左右边缘
                        updateViewPosition(view, params, x, y, mTouchStartX, mTouchStartY);
                        if (params != null) {
                            if (params.x <= (screenWidth / 2)) {
                                autoAnimTranslate(view, -params.x);
                            } else {
                                autoAnimTranslate(view, screenWidth - params.x);
                            }
                        }
                        mCurrentTime = System.currentTimeMillis();
                        if (mCurrentTime - mLastTime < 800) {
                            if (Math.abs(mStartX - mLastX) < 10.0 && Math.abs(mStartY - mLastY) < 10.0) {
                                if (onClickListener != null) {
                                    onClickListener.onClick(view);
                                }
                            }
                        }
                        break;

                    default:
                        break;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return true;
        }
    }

    /**
     * 更新浮动窗口位置参数
     */
    private void updateViewPosition(View view, WindowManager.LayoutParams params, float x, float y, float mTouchStartX, float mTouchStartY) {
        if (params == null || view == null) {
            return;
        }
        try {
            //更新浮动窗口位置参数
            params.x = (int) (x - mTouchStartX);
            params.y = (int) (y - mTouchStartY);
            if (params.y >= screenHeight) {
                params.y = screenHeight;
            }
            int left = edgeLeft.get(view);
            int right = edgeRight.get(view);
            if (params.x <= left) {
                params.x = left;
            }
            if (params.x >= right) {
                params.x = right;
            }
            //刷新显示
            wm.updateViewLayout(view, params);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 通过标记找到悬浮球位置
     * @param tag
     * @return
     */
    public float[] getBallXYByViewTag(String tag){
        float[] xy = new float[2];
        if (tag != null){
            for (Map.Entry<View, WindowManager.LayoutParams> entry : viewLayoutParamsMap.entrySet()) {
                View view = entry.getKey();
                if (view.getTag() != null && view.getTag().equals(tag)){
                    WindowManager.LayoutParams layoutParams = entry.getValue();
                    xy[0] = layoutParams.x;
                    xy[1] = layoutParams.y;
                    break;
                }
            }
        }
        return xy;
    }
}
