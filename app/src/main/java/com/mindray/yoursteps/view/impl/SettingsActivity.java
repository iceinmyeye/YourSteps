package com.mindray.yoursteps.view.impl;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mindray.yoursteps.R;

public class SettingsActivity extends AppCompatActivity {

    // 布局
    private RelativeLayout rlStepTarget;
    private RelativeLayout rlStepMagnitude;
    private RelativeLayout rlStepCalorie;

    // 文本框与按钮
    private TextView txtStepTarget;
    private TextView txtStepMagnitude;
    private TextView txtStepCalorie;
    private Button btnSave;

    // 对话框
    private Dialog dlStepTarget;
    private Dialog dlStepMagnitude;
    private Dialog dlStepCalorie;

    private int target = 1000;
    private int magnitude = 30;
    private int calorie = 220;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsActivity.this.finish();
            }
        });

        bindView();
        setListener();
    }

//    private void  initStepInfo(int target, int magnitude, int calorie) {
//        this.target = target;
//        this.magnitude = magnitude;
//        this.calorie = calorie;
//
//        txtStepTarget.setText(target);
//        txtStepMagnitude.setText(magnitude);
//        txtStepCalorie.setText(calorie);
//    }

    private void bindView() {
        rlStepTarget = (RelativeLayout) findViewById(R.id.rl_step_target);
        rlStepMagnitude = (RelativeLayout) findViewById(R.id.rl_step_magnitude);
        rlStepCalorie = (RelativeLayout) findViewById(R.id.rl_step_calorie);

        txtStepTarget = (TextView) findViewById(R.id.txt_step_target);
        //txtStepTarget.setText("1000");
        txtStepMagnitude = (TextView) findViewById(R.id.txt_step_magnitude);
        //txtStepMagnitude.setText("30");
        txtStepCalorie = (TextView) findViewById(R.id.txt_step_calorie);
        //txtStepCalorie.setText("2200");

        btnSave = (Button) findViewById(R.id.btn_save);
    }

    // 设置监听事件
    private void setListener() {

        // 目标步数
        final NumberPicker npStepTarget = new NumberPicker(this);
        npStepTarget.setMaxValue(10000);
        npStepTarget.setMinValue(100);
        npStepTarget.setValue(target);
        txtStepTarget.setText(target + "");
        npStepTarget.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                target = newVal;
                txtStepTarget.setText(target + "");
            }
        });
        dlStepTarget = new AlertDialog.Builder(SettingsActivity.this)
                .setTitle(R.string.settings_target)
                .setView(npStepTarget)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        rlStepTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlStepTarget.show();
            }
        });

        // 单步幅度
        final NumberPicker npStepMagnitude = new NumberPicker(this);
        npStepMagnitude.setMaxValue(100);
        npStepMagnitude.setMinValue(10);
        npStepMagnitude.setValue(magnitude);
        txtStepMagnitude.setText(magnitude + "");
        npStepMagnitude.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                magnitude = newVal;
                txtStepMagnitude.setText(magnitude + "");
            }
        });
        dlStepMagnitude = new AlertDialog.Builder(SettingsActivity.this)
                .setTitle(R.string.settings_magnitude)
                .setView(npStepMagnitude)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        rlStepMagnitude.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlStepMagnitude.show();
            }
        });

        // 单步消耗卡路里
        final NumberPicker npStepCalorie = new NumberPicker(this);
        npStepCalorie.setMaxValue(300);
        npStepCalorie.setMinValue(50);
        npStepCalorie.setValue(calorie);
        txtStepCalorie.setText(calorie + "");
        npStepCalorie.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                calorie = newVal;
                txtStepCalorie.setText(calorie + "");
            }
        });
        dlStepCalorie = new AlertDialog.Builder(SettingsActivity.this)
                .setTitle(R.string.settings_calorie)
                .setView(npStepCalorie)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        rlStepCalorie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlStepCalorie.show();
            }
        });

        // 保存按钮功能
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
//                intent.putExtra("target", txtStepTarget.getText().toString());
//                intent.putExtra("magnitude", txtStepMagnitude.getText().toString());
//                intent.putExtra("calorie", txtStepCalorie.getText().toString());
                intent.putExtra("test", "I'm a test");
                setResult(RESULT_OK, intent);
                SettingsActivity.this.finish();
            }
        });
    }
}
