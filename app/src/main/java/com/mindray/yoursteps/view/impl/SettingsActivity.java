package com.mindray.yoursteps.view.impl;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mindray.yoursteps.R;
import com.mindray.yoursteps.bean.StepData;
import com.mindray.yoursteps.bean.StepTarget;
import com.mindray.yoursteps.config.Constant;
import com.mindray.yoursteps.utils.DbUtils;
import com.mindray.yoursteps.utils.DateUtils;

import java.util.List;

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

    private int target; //目标步数要存储到数据库
    private int magnitude;
    private int calorie;

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
        onFillSettings(target, magnitude, calorie);
        setListener();
    }

    private void bindView() {
        rlStepTarget = (RelativeLayout) findViewById(R.id.rl_step_target);
        rlStepMagnitude = (RelativeLayout) findViewById(R.id.rl_step_magnitude);
        rlStepCalorie = (RelativeLayout) findViewById(R.id.rl_step_calorie);

        txtStepTarget = (TextView) findViewById(R.id.txt_step_target);
        txtStepMagnitude = (TextView) findViewById(R.id.txt_step_magnitude);
        txtStepCalorie = (TextView) findViewById(R.id.txt_step_calorie);

        btnSave = (Button) findViewById(R.id.btn_save);
    }

    // 设置监听事件
    private void setListener() {

        // 目标步数
        final NumberPicker npStepTarget = new NumberPicker(this);
        npStepTarget.setMaxValue(50000);
        npStepTarget.setMinValue(100);
        target = Integer.parseInt(txtStepTarget.getText().toString());
        npStepTarget.setValue(target);
        npStepTarget.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                target = newVal;
                txtStepTarget.setText(String.valueOf(target));
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
        magnitude = Integer.parseInt(txtStepMagnitude.getText().toString());
        npStepMagnitude.setValue(magnitude);
        npStepMagnitude.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                magnitude = newVal;
                txtStepMagnitude.setText(String.valueOf(magnitude));
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
        calorie = Integer.parseInt(txtStepCalorie.getText().toString());
        npStepCalorie.setValue(calorie);
        npStepCalorie.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                calorie = newVal;
                txtStepCalorie.setText(String.valueOf(calorie));
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
                SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE).edit();

                String strTarget = txtStepTarget.getText().toString();
                intent.putExtra("target", strTarget);
                target = Integer.parseInt(strTarget);
                editor.putInt("target", target);

                String strMagnitude = txtStepMagnitude.getText().toString();
                intent.putExtra("magnitude", strMagnitude);
                magnitude = Integer.parseInt(strMagnitude);
                editor.putInt("magnitude", magnitude);

                String strCalorie = txtStepCalorie.getText().toString();
                intent.putExtra("calorie", strCalorie);
                calorie = Integer.parseInt(strCalorie);
                editor.putInt("calorie", calorie);

                editor.commit();

                saveTarget2DataBase();

                setResult(RESULT_OK, intent);
                SettingsActivity.this.finish();
            }
        });
    }

    // 为设置中各项内容赋初值
    private void onFillSettings(int t, int m, int c) {

        SharedPreferences pref = getSharedPreferences("settings", MODE_PRIVATE);
        t = pref.getInt("target", 5000);
        m = pref.getInt("magnitude", 30);
        c = pref.getInt("calorie", 220);

        txtStepTarget.setText(String.valueOf(t));
        txtStepMagnitude.setText(String.valueOf(m));
        txtStepCalorie.setText(String.valueOf(c));
    }

    // 将设置的目标步数存储到数据库中
    private void saveTarget2DataBase() {

        List<StepTarget> list = DbUtils.getQueryByWhere(StepTarget.class, "date", new String[]{DateUtils.getTodayDate()});

        if (list.size() == 0 || list.isEmpty()) {
            StepTarget stepTarget = new StepTarget(DateUtils.getTodayDate(), String.valueOf(target));
            DbUtils.insert(stepTarget);
        } else if (list.size() == 1) {
            StepTarget data = list.get(0);
            data.setTarget(String.valueOf(target));
            DbUtils.update(data);
        }

        List<StepTarget> list1 = DbUtils.getQueryByWhere(StepTarget.class, "date", new String[]{DateUtils.getTodayDate()});
        System.out.println("lite-orm target " + list1.get(0).getTarget());
    }
}
