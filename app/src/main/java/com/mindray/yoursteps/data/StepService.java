package com.mindray.yoursteps.data;

import android.app.Service;
import android.content.BroadcastReceiver;
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
import android.util.Log;

/**
 * Created by 董小京 on 2017/4/24.
 */

public class StepService extends Service implements SensorEventListener {
    private final static String TAG="SetupService";

    private SensorManager sensorManager;
    private StepCount stepCount;
    private BroadcastReceiver mBroadcastReceiver;
    private final static int MSG=0;
    private final static int MSG_SERVER=1;

    //计步器传感器类型 0-counter 1-detector
    private static int stepSensor = -1;

    private Messenger messenger = new Messenger(new MessenerHandler());
    private static class MessenerHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch(msg.what){
                case MSG:
                    try{
                        Messenger messenger = msg.replyTo;
                        Message replyMsg = Message.obtain(null, MSG_SERVER);
                        Bundle bundle = new Bundle();
                        bundle.putInt("step", StepCount.CURRENT_SETP);
                        replyMsg.setData(bundle);
                        Log.d(TAG, replyMsg+"");
                        messenger.send(replyMsg);
                    }catch(Exception e){
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

    @Override
    @Deprecated
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        return START_STICKY;
    }

    private void startStepDetector(){
        if(sensorManager != null && stepCount !=null){
            sensorManager.unregisterListener(stepCount);
            sensorManager = null;
            stepCount = null;
        }
        //getLock(this);
        //获取传感器管理器的实例
        sensorManager = (SensorManager)this.getSystemService(SENSOR_SERVICE);
        //android 4.4以后可以使用计步传感器  在根据API不同版本分别执行addCountStepListener和addBasePedoListener两个方法
        int VERSION_CODES = android.os.Build.VERSION.SDK_INT;
        Log.d(TAG, VERSION_CODES+"");
        if(VERSION_CODES>19){//sdk版本
            addCountStepListener();
        }else{
            addBasePedoListener();
        }
    }


    private void addBasePedoListener() {
        // TODO Auto-generated method stub
        stepCount = new StepCount(this);
        // 获得传感器的类型，这里获得的类型是加速度传感器
        // 此方法用来注册，只有注册过才会生效，参数：SensorEventListener的实例，Sensor的实例，更新速率
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // sensorManager.unregisterListener(stepDetector);
        sensorManager.registerListener(stepCount, sensor, SensorManager.SENSOR_DELAY_UI);
        stepCount.setOnSensorChangeListener(new StepCount.OnSensorChangeListener() {

            @Override
            public void onChange() {
                // updateNotification("今日步数：" + StepDcretor.CURRENT_SETP + " 步");
            }
        });
    }

    private void addCountStepListener() {
        // TODO Auto-generated method stub
        //android 两种计步方式 detector启动后，确认了，才启动counter.
        Sensor detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);//计步传感器
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);//步行检测传感器
        if (countSensor != null) {
            stepSensor = 0;
            Log.v("base", "countSensor");
            //第一个参数是Listener，第二个参数是所得传感器类型，第三个参数值获取传感器信息的频率
            sensorManager.registerListener(StepService.this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else if (detectorSensor != null) {
            stepSensor = 1;
            Log.v("base", "detector");
            sensorManager.registerListener(StepService.this, detectorSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Log.v("xf", "Count sensor not available!");
            addBasePedoListener();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        if(stepSensor ==0){
            StepCount.CURRENT_SETP = (int)event.values[0];
        }else if(stepSensor ==1){
            StepCount.CURRENT_SETP++;
        }
        //updateNotification("今日步数：" + StepDcretor.CURRENT_SETP + " 步");
    }


    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        //这里开启了一个线程，因为后台服务也是在主线程中进行，这样可以安全点，防止主线程阻塞
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                startStepDetector();
            }
        }).start();

    }
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        unregisterReceiver(mBroadcastReceiver);//注销广播
        Intent intent = new Intent(this,StepService.class);
        startService(intent);//重新启动StepService 服务
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub
        return super.onUnbind(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub

    }
}
