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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class CalibrationActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;

    // 0-慢走；1-快走；2-慢跑；3-快跑
    private static int calibrationTag = 0;
    private StringBuilder sb0 = new StringBuilder();
    private StringBuilder sb1 = new StringBuilder();
    private StringBuilder sb2 = new StringBuilder();
    private StringBuilder sb3 = new StringBuilder();

    private TextView txtCalibration;

    private TimeCount calibrationTime = new TimeCount(241000, 60000);

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
    public void onSensorChanged(SensorEvent event) {
        float x =  event.values[0];
        float y =  event.values[1];
        float z =  event.values[2];
        switch (calibrationTag) {
            case 0:
                sb0.append(x+" "+y+" "+z+" ");
                break;
            case 1:
                sb1.append(x+" "+y+" "+z+" ");
                break;
            case 2:
                sb2.append(x+" "+y+" "+z+" ");
                break;
            case 3:
                sb3.append(x+" "+y+" "+z+" ");
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        String str0 = sb0.toString();
        save(str0, "walkSlowly");

        String str1 = sb1.toString();
        save(str1, "walkQuickly");

        String str2 = sb2.toString();
        save(str2, "runSlowly");

        String str3 = sb3.toString();
        save(str3, "runQuickly");
    }

    public void save(String input, String fileName){
        FileOutputStream out =null;
        BufferedWriter writer = null;
        try{
            out = openFileOutput(fileName, Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(input);
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            try{
                if(writer !=null){
                    writer.close();
                }
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            switch (calibrationTag) {
                case 0:
                    walkSlowly();
                    break;
                case 1:
                    walkQuickly();
                    break;
                case 2:
                    runSlowly();
                    break;
                case 3:
                    runQuickly();
                    break;
                default:
                    Toast.makeText(CalibrationActivity.this,
                            getResources().getString(R.string.calibration_finished), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFinish() {

            CalibrationActivity.this.finish();
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
