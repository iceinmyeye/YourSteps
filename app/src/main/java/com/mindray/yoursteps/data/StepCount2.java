package com.mindray.yoursteps.data;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.CountDownTimer;
import android.os.Message;
import android.util.Log;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by DuXuan on 2017/4/28.
 */

public class StepCount2 implements SensorEventListener, Serializable {
    //存放原始数据
    float[] orignValues = new float[3];

    //检测波峰或者波谷,true为当前只检测波峰，false为当前只检测波谷
    boolean isPeakOrValley = true;

    //检测5组波峰与波谷的差，保存波谷检测时间与波峰检测时间的差，以及波峰值减波谷值的差
    final int diffNum = 5;

    float[][] diffValue = new float[2][diffNum];

    //此次波峰值
    float valueOfPeak = 0;

    //此次波谷值
    float valueOfValley = 0;

    //波峰时间
    long timeOfPeak = 0;

    //波谷时间
    long timeOfValley = 0;

    //两步之间的时间差
    float frequence = 0;

    //CSVM的均值
    float diff = 0;

    //状态判断，初始为静止状态
    public static int  stationvalue = 0;

    public void setStation(int stationvalue) {
        this.stationvalue = stationvalue;
    }

    public static int getStationvalue() {
        return StepCount2.stationvalue;
    }

    //需要设定快跑的频率和差值
    float fRunFast = 230;//待定
    float dRunFast = 8;//可以调整 5 或者12

    //慢跑的频率与差值
    float fRun = 400;
    float dRun = 5;

    //正常走路的频率与差值
    float fWalk = 600;
    float dWalk = 2;

    // float fStand;
    // float dStand;

    //判断连着上升2次，并且下降2次则为峰值所在，数组长度暂时设置为4，后面可调整为3或者5
    final int judgeNum = 4;
    float[] isPeakOfWave = new float[judgeNum];

    //检测到5个波峰波谷值就进行状态检测
    int isStation = 0;

    //进行静止判断的时间
    long isStandTime = 0;


    private final String TAG = "StepDetector";

    /**
     * 0-准备计时   1-计时中  2-准备为正常计步计时  3-正常计步中
     */
    private int CountTimeState = 0;

    public static int CURRENT_STEP = 0;   //当前步数

    public static int TEMP_STEP = 0;
    private int lastStep = -1;

    //用三个维度算出的平方和开根值
    public static float average = 0;

    private Timer timer;

    // 3秒内不会显示计步，用于屏蔽细微波动
    private long duration = 3000;
    private TimeCount time;

    OnSensorChangeListener onSensorChangeListener;

    public interface OnSensorChangeListener {
        void onChange();
    }

    public StepCount2(Context context) {
        super();
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {

    }


    public OnSensorChangeListener getOnSensorChangeListener() {
        return onSensorChangeListener;
    }

    public void setOnSensorChangeListener(
            OnSensorChangeListener onSensorChangeListener) {
        this.onSensorChangeListener = onSensorChangeListener;
    }


    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        synchronized (this) {
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                calc_step(event);
            }
        }
    }

    /**
     * 将加速度传感器三轴平方和开根值作为特征值 传入  DetectorNewStep
     */
    synchronized private void calc_step(SensorEvent event) {
        average = (float) Math.sqrt(Math.pow(event.values[0], 2)
                + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2));
        DetectorNewStep(average);
    }

    /*
     * 检测步子，并开始计步
     * 1.传入sersor中的数据
     * 2.如果检测到了波峰，检测到了波谷，并且符合时间差以及阈值的条件，则判定为1步！
     * 3.
     * */
    public void DetectorNewStep(float values) {
        isStandTime = System.currentTimeMillis();
        if (DetectorPeak(values, isPeakOfWave)) {
            //更新界面
            preStep();
        }
        System.out.println("standtime:"+" "+(isStandTime-timeOfPeak));
      if (isStandTime - timeOfPeak > 2000) {
           stationvalue = 0;        //距离上次波峰值大于了2000，设置为静止
            System.out.println("test_11"+" "+isStandTime +" "+timeOfPeak);
       }
        for (int i = 0; i < judgeNum - 1; i++) {

            isPeakOfWave[i] = isPeakOfWave[i + 1];
        }

        isPeakOfWave[judgeNum - 1] = values;   //判断波峰波谷的矩阵更新

    }

    /**
     * 计时器
     */
    private void preStep() {
        System.out.println("this is a TEST");
        if (CountTimeState == 0) {
            // 开启计时器
            time = new TimeCount(duration, 700);
            time.start();
            CountTimeState = 1;
            Log.v(TAG, "开启计时器");
        } else if (CountTimeState == 1) {
            TEMP_STEP++;
            Log.v(TAG, "计步中 TEMP_STEP:" + TEMP_STEP);
        } else if (CountTimeState == 3) {
            CURRENT_STEP++;
            if (onSensorChangeListener != null) {
                onSensorChangeListener.onChange();
            }
        }
    }

    /*
     * 检测波峰
     * 以下四个条件判断为波峰：
     * 1.之前检测到一个波谷
     * 2.现在的1点为下降趋势，之前的1点为下降的趋势，之前的2点为上升趋势。之前的3点为上升趋势
     * 4.波峰值大于1.2g
     * 记录波谷值
     * 1.观察波形图，可以发现在出现步子的地方，波谷的下一个就是波峰，有比较明显的特征以及差值
     * 2.所以要记录每次的波谷值，为了和下次的波峰做对比  求出特征值波峰减波谷
     * */
    public boolean DetectorPeak(float newValue, float[] oldValue) {
        //lastStatus = isDirectionUp;
        if (isPeakOrValley  && newValue > oldValue[judgeNum - 1] && oldValue[judgeNum - 1] >= oldValue[judgeNum - 2]
                && oldValue[judgeNum - 2] <= oldValue[judgeNum - 3] && oldValue[judgeNum - 3] < oldValue[judgeNum - 4])  {
            //timeOfValley = System.currentTimeMillis();
            isPeakOrValley = !isPeakOrValley; //检测到波谷，下一步检测波峰！
            valueOfValley = oldValue[2];
            timeOfValley = System.currentTimeMillis();
            System.out.println("This is test_3");
        }

        if (!isPeakOrValley && oldValue[judgeNum - 2] > 11.5 && newValue < oldValue[judgeNum - 1] && oldValue[judgeNum - 1] <= oldValue[judgeNum - 2]
                && oldValue[judgeNum - 2] >= oldValue[judgeNum - 3] && oldValue[judgeNum - 3] > oldValue[judgeNum - 4]) {
            timeOfPeak = System.currentTimeMillis();
            //isPeakOrValley = !isPeakOrValley;  //检测波峰
            valueOfPeak = oldValue[2];  // 数组中2为峰值
            System.out.println("This is test_4");
            //判断是否是干扰
            if (valueOfPeak - valueOfValley > (0.1 * 9.8)     //这里可以进行更改判断的阈值0.15手持可以，但是对于放在兜里有点大？
                    && timeOfPeak - timeOfValley > 150 && timeOfPeak - timeOfValley < 2000) {
                System.out.println("This is test_5");
                isPeakOrValley = !isPeakOrValley; //有效的一步，下一个状态检测波谷
                //确认为一个有效的步态
                for (int i = 0; i < diffNum - 1; i++) {
                    diffValue[1][i] = diffValue[1][i + 1];
                    diffValue[0][i] = diffValue[0][i + 1];
                    //System.out.println("test_11"+" "+ diffValue[0][i]);
                }
                System.out.println("This is test_9");
                diffValue[1][diffNum - 1] = valueOfPeak - valueOfValley;

                diffValue[0][diffNum - 1] = timeOfPeak - timeOfValley;  //判断状态用到的5组的时间和幅度差值
                //是否进行状态判断
                isStation += 1;
                //5次进行一次状态检测
                System.out.println("This is test_8");
                if (isStation % 5 == 0 && CURRENT_STEP >= 5) {
                    station(diffValue);

                    System.out.println("This is test_10");
                }

//
                System.out.print("This is test_7");
                timeOfPeak = System.currentTimeMillis();
                return true;  //返回新的一步

            } else {
                isPeakOrValley = !isPeakOrValley;// 判断是否是干扰，如果是干扰，满足else，放弃之前的波谷值。
                System.out.println("This is test_5");
            }
        }

        System.out.println("This is test_2" + " "+(oldValue[0]-oldValue[1]) + isPeakOrValley);

        return false;
    }

    /* 状态的判断{难点}
    *
    */
    public void station(float[][] diffValue) {

        for (int i = 0; i < diffNum; i++) {
            frequence += diffValue[0][i];
        }
        frequence = frequence/5;  //两个动作的时间差  感觉可能在200-750之间
        System.out.println("frequence"+" "+frequence);
        int tempStation = 0;
        for (int i = 0; i < diffNum; i++) {
            diff += diffValue[1][i];
        }
        diff = diff/5;           //两个动作的峰谷值差
        System.out.println("frequence_1"+" "+diff);
        if (frequence < fRunFast && diff > dRunFast) {
            tempStation = 3;
        } else if (frequence > fRunFast && frequence < fRun && diff > dRun) {
            tempStation = 2;
        } else if (frequence < fWalk && diff > dWalk) {
            tempStation = 1;
        } else {
            tempStation = 4; //状态无法判断
        }
        stationvalue = tempStation;
        System.out.print("test_11"+" "+stationvalue);

    }


    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            // 如果计时器正常结束，则开始计步
            time.cancel();
            CURRENT_STEP += TEMP_STEP;  //存的步数加入
            lastStep = -1;
//            CountTimeState = 2;
            Log.v(TAG, "计时正常结束");

            timer = new Timer(true);
            TimerTask task = new TimerTask() {
                public void run() {
                    if (lastStep == CURRENT_STEP) {
                        timer.cancel();
                        CountTimeState = 0;
                        lastStep = -1;
                        TEMP_STEP = 0;
                        Log.v(TAG, "停止计步：" + CURRENT_STEP);
                        System.out.print("tingzhi"+" "+CURRENT_STEP);
                    } else {
                        lastStep = CURRENT_STEP;
                    }
                }
            };
            timer.schedule(task, 0, 3000);
            CountTimeState = 3;//正常记步
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (lastStep == TEMP_STEP) {
                Log.v(TAG, "onTick 计时停止");
                time.cancel();
                CountTimeState = 0;
                lastStep = -1;
                TEMP_STEP = 0;
            } else {
                lastStep = TEMP_STEP;
            }
        }
    }
}
