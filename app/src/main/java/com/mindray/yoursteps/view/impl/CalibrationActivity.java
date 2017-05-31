package com.mindray.yoursteps.view.impl;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.mindray.yoursteps.R;
import com.mindray.yoursteps.utils.CountDownTimer;
import com.mindray.yoursteps.utils.VibrateUtil;

import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class CalibrationActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;

    // 0-慢走；1-快走；2-慢跑；3-快跑
    private static int calibrationTag = 0;
    private StringBuilder sb0 = new StringBuilder();
    private StringBuilder sb1 = new StringBuilder();
    private StringBuilder sb2 = new StringBuilder();
    private StringBuilder sb3 = new StringBuilder();

    private int i1 = 1;
    private int i2 = 1;
    private int i3 = 1;
    private int i4 = 1;
    private double[] store1 = new double[2000];
    private double[] store2 = new double[2000];
    private double[] store3 = new double[2000];
    private double[] store4 = new double[2000];
    private List listMean1 = new ArrayList();
    private List listMean2 = new ArrayList();
    private List listMean3 = new ArrayList();
    private List listMean4 = new ArrayList();
    private List listVar1 = new ArrayList();
    private List listVar2 = new ArrayList();
    private List listVar3 = new ArrayList();
    private List listVar4 = new ArrayList();

    private TextView txtCalibration;
    private TextView txtCountDownTime;

    private TimeCount calibrationTime = new TimeCount(241000, 60000);
    private OneMinuteCount minuteCount = new OneMinuteCount(60000, 1000);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        initParam();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        calibrationTime.start();
    }

    private void initParam() {
        txtCalibration = (TextView) findViewById(R.id.textView_calibration);
        txtCountDownTime = (TextView) findViewById(R.id.textView_calibrationCount);

        txtCalibration.setText(getResources().getString(R.string.calibration_preparation));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onBackPressed() {
        calibrationTag = 0;
        calibrationTime.cancel();
        this.finish();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        switch (calibrationTag) {
            case 1:
                if (i1 > 300 && i1 < 2301) {
                    store1[i1 - 301] = Math.pow(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2), 1 / 2);
                }
                i1++;
                sb0.append(x + " " + y + " " + z + " ");
                break;
            case 2:
                if (i2 > 300 && i2 < 2301) {
                    store2[i2 - 301] = Math.pow(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2), 1 / 2);
                }
                i2++;
                sb1.append(x + " " + y + " " + z + " ");
                break;
            case 3:
                if (i3 > 300 && i3 < 2301) {
                    store3[i3 - 301] = Math.pow(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2), 1 / 2);
                }
                i3++;
                sb2.append(x + " " + y + " " + z + " ");
                break;
            case 4:
                if (i4 > 300 && i4 < 2301) {
                    store4[i4 - 301] = Math.pow(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2), 1 / 2);
                }
                i4++;
                sb3.append(x + " " + y + " " + z + " ");
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        double[][] store11 = separateArray(store1);
        double[][] store22 = separateArray(store2);
        double[][] store33 = separateArray(store3);
        double[][] store44 = separateArray(store4);

        for (int i = 0; i < 20; i++) {

            double[] temp1 = new double[100];
            double[] temp2 = new double[100];
            double[] temp3 = new double[100];
            double[] temp4 = new double[100];

            for (int j = 0; j < 100; j++) {
                temp1[j] = store11[i][j];
                temp2[j] = store22[i][j];
                temp3[j] = store33[i][j];
                temp4[j] = store44[i][j];
            }

            listMean1.add(calculateMean(temp1));
            listMean2.add(calculateMean(temp2));
            listMean3.add(calculateMean(temp3));
            listMean4.add(calculateMean(temp4));

            listVar1.add(calculateVariance(temp1));
            listVar2.add(calculateVariance(temp2));
            listVar3.add(calculateVariance(temp3));
            listVar4.add(calculateVariance(temp4));
        }

        String str0 = sb0.toString();
        save(str0, "walkSlowly");

        String str1 = sb1.toString();
        save(str1, "walkQuickly");

        String str2 = sb2.toString();
        save(str2, "runSlowly");

        String str3 = sb3.toString();
        save(str3, "runQuickly");
    }

    public void save(String input, String fileName) {
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            out = openFileOutput(fileName, Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(input);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 拆分数组：将2000拆分为20*100
    private double[][] separateArray(double[] doubles) {
        double[][] d2 = new double[20][100];
        for (int j = 0; j < 20; j++) {
            for (int i = 0; i < doubles.length; i++) {
                if (i % 100 == 0) {
                    for (int k = 0; k < 100; k++) {
                        d2[j][k] = doubles[i];
                    }
                }
            }
        }
        return d2;
    }

    // 计算均值的函数
    private double calculateMean(double[] doubles) {
        double sum = 0;
        for (double d : doubles) {
            sum += d;
        }
        return sum / doubles.length;
    }

    // 计算方差的函数
    private double calculateVariance(double[] doubles) {
        double sum = 0;
        double mean = calculateMean(doubles);
        for (double d : doubles) {
            sum += Math.pow(d - mean, 2);
        }
        return sum / doubles.length;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private class TimeCount extends CountDownTimer {
        TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            switch (calibrationTag) {
                case 0:
                    walkSlowly();
                    minuteCount.start();
                    break;
                case 1:
                    walkQuickly();
                    VibrateUtil.Vibrate(CalibrationActivity.this, 500);
                    minuteCount.start();
                    break;
                case 2:
                    runSlowly();
                    VibrateUtil.Vibrate(CalibrationActivity.this, 500);
                    minuteCount.start();
                    break;
                case 3:
                    runQuickly();
                    VibrateUtil.Vibrate(CalibrationActivity.this, 500);
                    minuteCount.start();
                    break;
                default:
                    break;

            }
        }

        @Override
        public void onFinish() {

            VibrateUtil.Vibrate(CalibrationActivity.this, 500);
            Toast.makeText(CalibrationActivity.this,
                    getResources().getString(R.string.calibration_finished), Toast.LENGTH_LONG).show();
            calibrationTag = 0;
            calibrationTime.cancel();
            CalibrationActivity.this.finish();
        }
    }

    private class OneMinuteCount extends CountDownTimer {
        OneMinuteCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            txtCountDownTime.setText(String.valueOf(millisUntilFinished / 1000));
        }

        @Override
        public void onFinish() {
            minuteCount.cancel();
        }
    }

    private void walkSlowly() {
        txtCalibration.setText(getResources().getString(R.string.calibration_walkSlowly));
        calibrationTag++;
    }

    private void walkQuickly() {
        txtCalibration.setText(getResources().getString(R.string.calibration_walkQuickly));
        Toast.makeText(CalibrationActivity.this,
                getResources().getString(R.string.calibration_interval1), Toast.LENGTH_SHORT).show();
        calibrationTag++;
    }

    private void runSlowly() {
        txtCalibration.setText(getResources().getString(R.string.calibration_runSlowly));
        Toast.makeText(CalibrationActivity.this,
                getResources().getString(R.string.calibration_interval2), Toast.LENGTH_SHORT).show();
        calibrationTag++;
    }

    private void runQuickly() {
        txtCalibration.setText(getResources().getString(R.string.calibration_runQuickly));
        Toast.makeText(CalibrationActivity.this,
                getResources().getString(R.string.calibration_interval3), Toast.LENGTH_SHORT).show();
        calibrationTag++;
    }
}
