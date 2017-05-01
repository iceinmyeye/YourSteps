package com.mindray.yoursteps.data;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.util.Log;

/**
 * Created by 董小京 on 2017/4/24.
 */

public class StepService extends Service implements SensorEventListener {
    private final static String TAG = "SetupService";

    private SensorManager sensorManager;
    private StepCount2 stepCount2;
    private BroadcastReceiver mBroadcastReceiver;
    private final static int MSG = 0;
    private final static int MSG_SERVER = 1;

    // 设备电源锁
    private PowerManager.WakeLock mWakeLock;

    //计步器传感器类型 0-counter 1-detector
    private static int stepSensor = -1;

    private Messenger messenger = new Messenger(new MessengerHandler());

    private static class MessengerHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case MSG:
                    try {
                        Messenger messenger = msg.replyTo;
                        Message replyMsg = Message.obtain(null, MSG_SERVER);
                        Bundle bundle = new Bundle();
                        //bundle.putInt("step", StepCount.CURRENT_STEP);
                        bundle.putString("step", StepCount2.CURRENT_STEP + " " + StepCount2.getStationvalue());
                        Log.v("step", String.valueOf(StepCount2.CURRENT_STEP));
                        System.out.println("xxxx");
                        System.out.println("test_11"+String.valueOf(StepCount2.stationvalue));
                        replyMsg.setData(bundle);
                        Log.d(TAG, replyMsg + "");
                        messenger.send(replyMsg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return messenger.getBinder();
    }

    // onCreat()方法，在服务创建的时候调用
    @Override
    public void onCreate() {
        super.onCreate();
        //这里开启了一个线程，因为后台服务也是在主线程中进行，这样可以安全点，防止主线程阻塞
        new Thread(new Runnable() {

            @Override
            public void run() {
                startStepCount();
            }
        }).start();

    }

    // onStartCommand()方法，在每次服务启动的时候调用。服务一旦启动，就会立刻执行其中的动作
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void startStepCount() {
        if (sensorManager != null && stepCount2 != null) {
            sensorManager.unregisterListener(stepCount2);
            sensorManager = null;
            stepCount2 = null;
        }

        //获取传感器管理器的实例
        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        addBasePedoListener();
        acquireWakeLock(StepService.this);
    }


    private void addBasePedoListener() {
        stepCount2 = new StepCount2(this);
        // 获得传感器的类型，这里获得的类型是加速度传感器
        // 此方法用来注册，只有注册过才会生效，参数：SensorEventListener的实例，Sensor的实例，更新速率
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // sensorManager.unregisterListener(stepDetector);
        sensorManager.registerListener(stepCount2, sensor, SensorManager.SENSOR_DELAY_GAME);
        stepCount2.setOnSensorChangeListener(new StepCount2.OnSensorChangeListener() {

            @Override
            public void onChange() {
                // updateNotification("今日步数：" + StepDcretor.CURRENT_STEP + " 步");
            }
        });
    }

//    private void addCountStepListener() {
//        //android 两种计步方式 detector启动后，确认了，才启动counter.
//        Sensor detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);//计步传感器
//        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);//步行检测传感器
//        if (countSensor != null) {
//            stepSensor = 0;
//            Log.v("base", "countSensor");
//            //第一个参数是Listener，第二个参数是所得传感器类型，第三个参数值获取传感器信息的频率
//            sensorManager.registerListener(StepService.this, countSensor, SensorManager.SENSOR_DELAY_UI);
//        } else if (detectorSensor != null) {
//            stepSensor = 1;
//            Log.v("base", "detector");
//            sensorManager.registerListener(StepService.this, detectorSensor, SensorManager.SENSOR_DELAY_UI);
//        } else {
//            Log.v("xf", "Count sensor not available!");
//            addBasePedoListener();
//        }
//    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (stepSensor == 0) {
            StepCount2.CURRENT_STEP = (int) event.values[0];
        } else if (stepSensor == 1) {
            StepCount2.CURRENT_STEP++;
        }
        //updateNotification("今日步数：" + StepDcretor.CURRENT_STEP + " 步");
    }

    // onDestroy()方法，在服务销毁的时候调用
    @Override
    public void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);//注销广播
        releaseWakeLock();
        Intent intent = new Intent(this, StepService.class);
        startService(intent);//重新启动StepService 服务
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    /**
     * 申请设备电源锁
     * @param context
     */
    private void acquireWakeLock(Context context)
    {
        if (null == mWakeLock)
        {
            PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, TAG);
            if (null != mWakeLock)
            {
                mWakeLock.acquire();
            }
        }
    }

    /**
     * 释放设备电源锁
     */
    private void releaseWakeLock()
    {
        if (null != mWakeLock)
        {
            mWakeLock.release();
            mWakeLock = null;
        }
    }
    // 服务启动时申请设备电源锁，服务退出时释放设备电源锁
}
