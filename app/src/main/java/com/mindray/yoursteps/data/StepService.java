package com.mindray.yoursteps.data;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.mindray.yoursteps.R;
import com.mindray.yoursteps.view.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by 董小京 on 2017/4/24.
 */

public class StepService extends Service implements SensorEventListener {
    private final static String TAG = "SetupService";

    private StepsDataBase dbHelp;
    private static final int DB_VERSION = 1;

    //默认为30秒进行一次存储
    private static int storeDuration = 30000;
    private static String CURRENTDATE = "";
    private SensorManager sensorManager;
    private StepCount2 stepCount2;
    private NotificationManager nm;
    private NotificationCompat.Builder builder;
    private BroadcastReceiver mBatInfoReceiver;
    private TimeCount time;

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
            switch (msg.what) {
                case MSG:
                    try {
                        Messenger messenger = msg.replyTo;
                        Message replyMsg = Message.obtain(null, MSG_SERVER);
                        Bundle bundle = new Bundle();
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
        return messenger.getBinder();
    }

    // onCreat()方法，在服务创建的时候调用
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("StepService", "onCreate executed");
        CURRENTDATE = getTodayDate();

        // 创建"YourSteps.db"
        dbHelp = new StepsDataBase(this, "YourSteps.db", null, DB_VERSION);
        dbHelp.getWritableDatabase();

        initBroadcastReceiver();

        //这里开启了一个线程，因为后台服务也是在主线程中进行，这样可以安全点，防止主线程阻塞
        new Thread(new Runnable() {

            @Override
            public void run() {
                startStepCount();
            }
        }).start();

        startTimeCount();
        initTodayData();

        updateNotification("当前步数：" + StepCount2.CURRENT_STEP + " 步");

    }

    private String getTodayDate() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    private void initTodayData() {
//        DbUtils.createDb(this, "basepedo");
//        //获取当天的数据，用于展示
//        List<StepData> list = DbUtils.getQueryByWhere(StepData.class, "today", new String[]{CURRENTDATE});
//        if (list.size() == 0 || list.isEmpty()) {
//            StepCount2.CURRENT_STEP = 0;
//        } else if (list.size() == 1) {
//            StepCount2.CURRENT_STEP = Integer.parseInt(list.get(0).getStep());
//        } else {
//            Log.v("xf", "出错了！");
//        }
    }

    private void initBroadcastReceiver() {
        final IntentFilter filter = new IntentFilter();
        // 屏幕灭屏广播
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        //关机广播
        filter.addAction(Intent.ACTION_SHUTDOWN);
        // 屏幕亮屏广播
        filter.addAction(Intent.ACTION_SCREEN_ON);
        // 屏幕解锁广播
        filter.addAction(Intent.ACTION_USER_PRESENT);
        // 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
        // example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
        // 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        mBatInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                String action = intent.getAction();

                if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    Log.d("xf", "screen on");
                } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    Log.d("xf", "screen off");
                    //改为60秒一存储
                    storeDuration = 60000;
                } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                    Log.d("xf", "screen unlock");
                    save();
                    //改为30秒一存储
                    storeDuration = 30000;
                } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                    Log.i("xf", " receive Intent.ACTION_CLOSE_SYSTEM_DIALOGS");
                    //保存一次
                    save();
                } else if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
                    Log.i("xf", " receive ACTION_SHUTDOWN");
                    save();
                }
            }
        };
        registerReceiver(mBatInfoReceiver, filter);
    }

    /**
     * 更新通知
     */
    private void updateNotification(String content) {
        builder = new NotificationCompat.Builder(this);
        builder.setPriority(Notification.PRIORITY_MIN);

        //Notification.Builder builder = new Notification.Builder(this);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setTicker("YourSteps");
        builder.setContentTitle("YourSteps");
        //设置不可清除
        builder.setOngoing(true);
        builder.setContentText(content);
        Notification notification = builder.build();

        startForeground(0, notification);

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(R.string.app_name, notification);
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

        getLock(this);

        //获取传感器管理器的实例
        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        addBasePedoListener();
    }


    private void addBasePedoListener() {
        stepCount2 = new StepCount2(this);
        // 获得传感器的类型，这里获得的类型是加速度传感器
        // 此方法用来注册，只有注册过才会生效，参数：SensorEventListener的实例，Sensor的实例，更新速率
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(stepCount2, sensor, SensorManager.SENSOR_DELAY_GAME);
        stepCount2.setOnSensorChangeListener(new StepCount2.OnSensorChangeListener() {

            @Override
            public void onChange() {
                updateNotification("当前步数：" + StepCount2.CURRENT_STEP + " 步");
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (stepSensor == 0) {
            StepCount2.CURRENT_STEP = (int) event.values[0];
        } else if (stepSensor == 1) {
            StepCount2.CURRENT_STEP++;
        }
        updateNotification("今日步数：" + StepCount2.CURRENT_STEP + " 步");
    }

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            // 如果计时器正常结束，则开始计步
            time.cancel();
            save(); // 每隔storeDuration时间间隔，save()函数执行一次
            startTimeCount();
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

    }

    private void startTimeCount() {
        time = new TimeCount(storeDuration, 1000);
        time.start();
    }

//    private void save() {
//        int tempStep = StepCount2.CURRENT_STEP;
//
//        List<StepData> list = DbUtils.getQueryByWhere(StepData.class, "today", new String[]{CURRENTDATE});
//        if (list.size() == 0 || list.isEmpty()) {
//            StepData data = new StepData();
//            data.setToday(CURRENTDATE);
//            data.setStep(tempStep + "");
//            DbUtils.insert(data);
//        } else if (list.size() == 1) {
//            StepData data = list.get(0);
//            data.setStep(tempStep + "");
//            DbUtils.update(data);
//        } else {
//        }
//    }
    // 重新改写下save()方法
    private void save() {
        // TODO 数据储存
    }

    // onDestroy()方法，在服务销毁的时候调用
    @Override
    public void onDestroy() {
        //取消前台进程
        stopForeground(true);
//        DbUtils.closeDb();
//        unregisterReceiver(mBatInfoReceiver);//注销广播
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

    synchronized private PowerManager.WakeLock getLock(Context context) {
        if (mWakeLock != null) {
            if (mWakeLock.isHeld())
                mWakeLock.release();
            mWakeLock = null;
        }

        if (mWakeLock == null) {
            PowerManager mgr = (PowerManager) context
                    .getSystemService(Context.POWER_SERVICE);
            mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    StepService.class.getName());
            mWakeLock.setReferenceCounted(true);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            int hour = c.get(Calendar.HOUR_OF_DAY);

            // 判断当前时间，23点至凌晨6点，释放WakeLock的时间设为5000毫秒
            // 该项根据实际需要可以进行修改
            if (hour >= 23 || hour <= 6) {
                mWakeLock.acquire(5000);
            }
        }

        return (mWakeLock);
    }
}
