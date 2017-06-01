package com.mindray.yoursteps.view;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.mindray.yoursteps.R;
import com.mindray.yoursteps.bean.StepTarget;
import com.mindray.yoursteps.bean.TreeData;
import com.mindray.yoursteps.config.Constant;
import com.mindray.yoursteps.service.StepService;
import com.mindray.yoursteps.utils.StepDateUtils;
import com.mindray.yoursteps.utils.DbUtils;
import com.mindray.yoursteps.view.impl.AboutActivity;
import com.mindray.yoursteps.view.impl.CalibrationActivity;
import com.mindray.yoursteps.view.impl.ReviewActivity;
import com.mindray.yoursteps.view.impl.SettingsActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Callback {

    SharedPreferences preferencesMain;

    private static final String TAG = "nsc";
    private long TIME_INTERVAL = 500;

    private TextView textStep;
    private Handler delayHandler;
    private Messenger messenger;
    private Messenger mGetReplyMessenger = new Messenger(new Handler(this));

    private int stepNum; //显示在主界面的步数
    private int stepTodayNum; //当天步数
    private int stepTarget;
    private float stepMagnitude;
    private float stepConsumption;
    private int status;
    private String distance;
    private String consumption;

    public static String[] reviewSeven;
    private ArrayList treeSeven;

    private TextView textViewStatus;
    private TextView textViewTodaySteps;
    private TextView textViewDistance;
    private TextView textViewConsumption;

    DecimalFormat df = new DecimalFormat("#0.0"); // 数据显示格式

    // 定义ServiceConnection对象
    ServiceConnection conn = new ServiceConnection() {

        // 活动与服务接触绑定的时候调用
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }

        // 活动与服务成功绑定的时候调用
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                messenger = new Messenger(service);
                Message msg = Message.obtain(null, Constant.MSG_FROM_CLIENT);
                msg.replyTo = mGetReplyMessenger;//replyTo消息管理器
                Log.d(TAG, "msg =" + msg);
                messenger.send(msg);//发送消息出去
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        initParam();

        // 当首次运行程序时，需要校准
        preferencesMain = getSharedPreferences("launch_count", MODE_PRIVATE);
        int count = preferencesMain.getInt("launch_count", 0);

        if (count == 0) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setMessage(this.getResources().getString(R.string.calibration_option));
            dialog.setCancelable(false);
            dialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), CalibrationActivity.class);
                    startActivity(intent);
                }
            });
            dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.show();

            SharedPreferences.Editor editor = preferencesMain.edit();

            editor.putInt("launch_count", ++count);

            editor.commit();
        }

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        initTargetData();
        setupService();
    }

    // 启动UI
    private void initUI() {
        textStep = (TextView) findViewById(R.id.textView_step);
        textViewStatus = (TextView) findViewById(R.id.item_status);
        textViewTodaySteps = (TextView) findViewById(R.id.item_todaySteps);
        textViewDistance = (TextView) findViewById(R.id.item_distance);
        textViewConsumption = (TextView) findViewById(R.id.item_consumption);
        delayHandler = new Handler(this);
    }

    // 初始化目标步数、步幅以及单位步卡路里
    private void initParam() {
        SharedPreferences prefMain = getSharedPreferences("settings", MODE_PRIVATE);
        stepTarget = prefMain.getInt("target", 5000);
        stepMagnitude = ((float) prefMain.getInt("magnitude", 30)) / 100;
        stepConsumption = ((float) prefMain.getInt("calorie", 160)) / 2;
    }

    // 初始化目标步数数据库
    private void initTargetData() {

        // 在主活动中创建存储目标步数的数据表
        DbUtils.createDb(this, Constant.TARGET_NAME);

        List<StepTarget> list = DbUtils.getQueryByWhere(StepTarget.class, "date", new String[]{StepDateUtils.getTodayDate()});

        if (list.size() == 0 || list.isEmpty()) {
            StepTarget stepTargetData = new StepTarget(StepDateUtils.getTodayDate(), String.valueOf(stepTarget));
            DbUtils.insert(stepTargetData);
        }
    }

    // 启动服务
    private void setupService() {
        Intent intent = new Intent(this, StepService.class);

        List<TreeData> listTree = DbUtils.getQueryByWhere(TreeData.class, "day", new String[]{"decisionTree"});
        if (listTree.size() == 1) {
            treeSeven = listTree.get(0).getTree();
        } else {

            treeSeven = new ArrayList();

            treeSeven.add("Mean>10.949300000000001 Var>45.52675 4");
            treeSeven.add("Mean>10.949300000000001 Var<=45.52675 CSVM>2.04595 3");
            treeSeven.add("Mean>10.949300000000001 Var<=45.52675 CSVM<=2.04595 2");
            treeSeven.add("Mean<=10.949300000000001 Var>13.891950000000001 2");
            treeSeven.add("Mean<=10.949300000000001 Var<=13.891950000000001 1");

        }
        intent.putExtra("DecisionTree", treeSeven);

        //使用这个ServiceConnection，客户端可以绑定到一个service，通过把它传给bindService()
        //第一个bindService()的参数是一个明确指定了要绑定的service的Intent．
        //第二个参数是ServiceConnection对象．
        //第三个参数是一个标志，它表明绑定中的操作．它一般应是BIND_AUTO_CREATE，这样就会在service不存在时创建一个．其它可选的值是BIND_DEBUG_UNBIND和BIND_NOT_FOREGROUND,不想指定时设为0即可．。
        bindService(intent, conn, BIND_AUTO_CREATE);//BIND_AUTO_CREATE =1
        startService(intent);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Constant.MSG_FROM_SERVER:
                stepNum = msg.getData().getInt("key_steps");
                status = msg.getData().getInt("key_station");
                stepTodayNum = msg.getData().getInt("key_today_steps");
                reviewSeven = msg.getData().getStringArray("key_last_seven");
                switch (status) {
                    case 0:
                        textViewStatus.setText("静止");
                        break;
                    case 1:
                        textViewStatus.setText("步行");
                        break;
                    case 2:
                        textViewStatus.setText("快走");
                        break;
                    case 3:
                        textViewStatus.setText("慢跑");
                        break;
                    case 4:
                        textViewStatus.setText("快跑");
                        break;
                    default:
                        textViewStatus.setText("error");
                        break;
                }
                distance = df.format(stepNum * stepMagnitude) + " m";
                consumption = df.format((stepTodayNum * stepConsumption) / 9240) + " g";

                textStep.setText(String.valueOf(stepNum));
                textViewTodaySteps.setText(String.valueOf(stepTodayNum));
                textViewDistance.setText(distance);
                textViewConsumption.setText(consumption);

                //延时500ms发送值为REQUEST_SERVER 消息
                delayHandler.sendEmptyMessageDelayed(Constant.REQUEST_SERVER, TIME_INTERVAL);
                break;
            case Constant.REQUEST_SERVER:
                try {
                    Message message = Message.obtain(null, Constant.MSG_FROM_CLIENT);//发送消息
                    message.replyTo = mGetReplyMessenger;
                    Log.d(TAG, "message=" + message);
                    messenger.send(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        return false;
    }

    // 按返回键，活动后台运行
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        DbUtils.closeDb(); //数据库在主活动中创建，在主活动结束时关闭
        super.onDestroy();
        unbindService(conn); //解除服务的绑定
    }

    // Start of Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(settingsIntent, 11);
                break;
            case R.id.action_review:
                Intent reviewIntent = new Intent(MainActivity.this, ReviewActivity.class);
                startActivity(reviewIntent);
                break;
            case R.id.action_calibration:

                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setMessage(this.getResources().getString(R.string.calibration_option));
                dialog.setCancelable(false);
                dialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent calibrationIntent = new Intent(MainActivity.this, CalibrationActivity.class);
                        startActivity(calibrationIntent);
                    }
                });
                dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dialog.show();

                break;
            case R.id.action_about:
                Intent intentAbout = new Intent(this, AboutActivity.class);
                startActivity(intentAbout);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    // End of Menu

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 11:
                if (resultCode == RESULT_OK) {
                    stepTarget = Integer.parseInt(data.getStringExtra("target"));

                    stepMagnitude = ((float) Integer.parseInt(data.getStringExtra("magnitude"))) / 100;

                    stepConsumption = ((float) Integer.parseInt(data.getStringExtra("calorie"))) / 100;
                }
                break;
            default:
                break;
        }
    }
}
