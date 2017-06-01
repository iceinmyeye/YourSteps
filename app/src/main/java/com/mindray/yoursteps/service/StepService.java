package com.mindray.yoursteps.service;

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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.mindray.yoursteps.R;
import com.mindray.yoursteps.bean.StepData;
import com.mindray.yoursteps.config.Constant;
import com.mindray.yoursteps.utils.CountDownTimer;
import com.mindray.yoursteps.utils.DbUtils;
import com.mindray.yoursteps.utils.StepDateUtils;
import com.mindray.yoursteps.view.MainActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by 董小京 on 2017/4/24.
 */

public class StepService extends Service implements SensorEventListener {
    private final static String TAG = "SetupService";

    // 定义存储历史数据需要传递给Review的变量
    private static String[] reviewStepData;
    public static ArrayList treeEight;

    // 定义今日步数的变量
    private static int TODAY_STEPS;
    private static int STEPS_COPY;

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
                        bundle.putInt("key_steps", StepCount2.CURRENT_STEPS);
                        bundle.putInt("key_station", StepCount2.getDecisionTreeStation());
                        bundle.putInt("key_today_steps", STEPS_COPY);
                        bundle.putStringArray("key_last_seven", reviewStepData);
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

        //在创建方法中有判断，如果数据库已经创建了不会二次创建的
        //! 加上这句，依然存在同样的问题，周期性调用save()函数，每日步数没有保存，而当前步数被清空
        DbUtils.createDb(this, Constant.DB_NAME);

        initBroadcastReceiver();

        //这里开启了一个线程，因为后台服务也是在主线程中进行，这样可以安全点，防止主线程阻塞
        new Thread(new Runnable() {

            @Override
            public void run() {
                startStepCount();
            }
        }).start();

        startTimeCount();
    }

    // onStartCommand()方法，在每次服务启动的时候调用。服务一旦启动，就会立刻执行其中的动作
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 获取决策树
        treeEight = intent.getStringArrayListExtra("DecisionTree");
        System.out.println("DecisionTree " + treeEight);

        initTodayData();
        updateNotification("当前步数：" + StepCount2.CURRENT_STEPS + " 步");
        return START_STICKY;
    }

    /**
     * 初始化当天的日期
     */
    private void initTodayData() {

        getLastSevenData();

        CURRENTDATE = StepDateUtils.getTodayDate();

        //在创建方法中有判断，如果数据库已经创建了不会二次创建的
        DbUtils.createDb(this, Constant.DB_NAME);

        //获取当天的数据
        List<StepData> list = DbUtils.getQueryByWhere(StepData.class, "today", new String[]{CURRENTDATE});
        if (list.size() == 0 || list.isEmpty()) {
            //如果获取当天数据为空，则步数为0
            TODAY_STEPS = 0;
        } else if (list.size() == 1) {
            TODAY_STEPS = Integer.parseInt(list.get(0).getStep());
        } else {
            Log.e(TAG, "出错了！");
        }
        STEPS_COPY = TODAY_STEPS;
    }

    // 获取过去7天中的数据，这些在Review中需要使用
    private void getLastSevenData() {

        reviewStepData = new String[30];

        for (int i = 0; i < 30; i++) {
            List<StepData> listStep = DbUtils.getQueryByWhere(StepData.class, "today", new String[]{StepDateUtils.getSomeDate(i+1)});
            if (listStep.size() == 1) {
                reviewStepData[i] = listStep.get(0).getStep();
            } else {
                reviewStepData[i] = "0";
            }
        }
    }

    /**
     * 初始化广播
     */
    private void initBroadcastReceiver() {
        //定义意图过滤器
        final IntentFilter filter = new IntentFilter();
        //屏幕灭屏广播
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        //日期修改
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        //关闭广播
        filter.addAction(Intent.ACTION_SHUTDOWN);
        //屏幕高亮广播
        filter.addAction(Intent.ACTION_SCREEN_ON);
        //屏幕解锁广播
        filter.addAction(Intent.ACTION_USER_PRESENT);
        //当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
        //example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
        //所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        mBatInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    Log.v(TAG, "screen on");
                } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    Log.v(TAG, "screen off");
                    save();
                    //改为60秒一存储
                    storeDuration = 60000;
                } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                    Log.v(TAG, "screen unlock");
                    save();
                    //改为30秒一存储
                    storeDuration = 30000;
                } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                    Log.v(TAG, "receive Intent.ACTION_CLOSE_SYSTEM_DIALOGS  出现系统对话框");
                    //保存一次
                    save();
                } else if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
                    Log.v(TAG, "receive ACTION_SHUTDOWN");
                    save();
                } else if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction())) {
                    Log.v(TAG, "receive ACTION_TIME_CHANGED");
                    initTodayData();
                }
            }
        };
        registerReceiver(mBatInfoReceiver, filter);
    }

    /**
     * 更新通知(显示通知栏信息)
     *
     * @param content
     */
    private void updateNotification(String content) {
        builder = new NotificationCompat.Builder(this);
        builder.setPriority(Notification.PRIORITY_MIN);

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

    private void startStepCount() {
        if (sensorManager != null && stepCount2 != null) {
            sensorManager.unregisterListener(stepCount2);
            sensorManager = null;
            stepCount2 = null;
        }

        getLock(this);

        //获取传感器管理器的实例
        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        addStepCountListener();
    }


    private void addStepCountListener() {
        stepCount2 = new StepCount2(this);
        // 获得传感器的类型，这里获得的类型是加速度传感器
        // 此方法用来注册，只有注册过才会生效，参数：SensorEventListener的实例，Sensor的实例，更新速率
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(stepCount2, sensor, SensorManager.SENSOR_DELAY_GAME);
        stepCount2.setOnSensorChangeListener(new StepCount2.OnSensorChangeListener() {

            @Override
            public void onChange() {
                updateNotification("当前步数：" + StepCount2.CURRENT_STEPS + " 步");
            }
        });
    }

    // 当使用步数传感器或手机内置步数检测器时才调用该函数，
    // 所以使用加速度传感器，该函数未曾调用过
    @Override
    public void onSensorChanged(SensorEvent event) {
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    private class TimeCount extends CountDownTimer {
        TimeCount(long millisInFuture, long countDownInterval) {
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

    /**
     * 保存数据
     */
    private void save() {
        STEPS_COPY = StepCount2.CURRENT_STEPS + TODAY_STEPS;

        List<StepData> list = DbUtils.getQueryByWhere(StepData.class, "today", new String[]{CURRENTDATE});

        if (list.size() == 0 || list.isEmpty()) {
            StepData saveData = new StepData(CURRENTDATE, String.valueOf(STEPS_COPY));
            DbUtils.insert(saveData);
        } else if (list.size() == 1) {
            //修改数据
            StepData data = list.get(0);
            data.setStep(String.valueOf(STEPS_COPY));
            DbUtils.update(data);
        }
    }

    // onDestroy()方法，在服务销毁的时候调用
    @Override
    public void onDestroy() {
        //取消前台进程
        save(); //程序结束前调用save函数，保证当前的CURRENTSTEP加入到今日步数中并存储
        stopForeground(true);
        DbUtils.closeDb();
        unregisterReceiver(mBatInfoReceiver);//注销广播
        Intent intent = new Intent(this, StepService.class);
        startService(intent); //重新启动StepService 服务
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
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
            } else {
                mWakeLock.acquire(300000);
            }
        }

        return (mWakeLock);
    }
}
