package com.mindray.yoursteps.view;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.mindray.yoursteps.R;
import com.mindray.yoursteps.service.StepService;
import com.mindray.yoursteps.utils.ActivityCollector;
import com.mindray.yoursteps.view.impl.AboutActivity;
import com.mindray.yoursteps.view.impl.SettingsActivity;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements Callback {

    private static final String TAG = "nsc";
    public static final int MSG_FROM_CLIENT = 0;
    public static final int MSG_FROM_SERVER = 1;//返回服务
    public static final int REQUEST_SERVER = 2;//取消服务
    private long TIME_INTERVAL = 500;

    private TextView textStep;
    private Handler delayHandler;
    private Messenger messenger;
    private Messenger mGetReplyMessenger = new Messenger(new Handler(this));

    private int stepNum; //显示在主界面的步数
    private int stepTarget;
    private float stepMagnitude;
    private float stepConsumption;
    private String status;
    private String distance;
    private String consumption;

    private TextView textViewStatus;
    private TextView textViewDistance;
    private TextView textViewConsumption;

    DecimalFormat df = new DecimalFormat("#.0"); // 数据显示格式

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
                Message msg = Message.obtain(null, MSG_FROM_CLIENT);
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

        ActivityCollector.addActivity(this);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        initUI();
        initParam();
        setupService();
    }

    // 启动UI
    private void initUI() {
        textStep = (TextView) findViewById(R.id.textView_step);
        textViewStatus = (TextView) findViewById(R.id.item_status);
        textViewDistance = (TextView) findViewById(R.id.item_distance);
        textViewConsumption = (TextView) findViewById(R.id.item_consumption);
        delayHandler = new Handler(this);
    }

    // 初始化目标步数、步幅以及单位步卡路里
    private void initParam() {
        SharedPreferences prefMain = getSharedPreferences("settings", MODE_PRIVATE);
        stepTarget = prefMain.getInt("target", 1000);
        stepMagnitude = ((float) prefMain.getInt("magnitude", 30))/100;
        stepConsumption = ((float) prefMain.getInt("calorie", 220))/100;
    }

    // 启动服务
    private void setupService() {
        Intent intent = new Intent(this, StepService.class);
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
            case MSG_FROM_SERVER:
                Log.d(TAG, "text=" + msg.getData().getInt("step"));
                //stepNum = msg.getData().getInt("step");
                String s = msg.getData().getString("step");
                String[] s2 = s.split("\\s");
                stepNum = Integer.parseInt(s2[0]);
                //textStep.setText(s2[0]);//显示步数
                switch (s2[1]) {
                    case "0":
                        textViewStatus.setText("静止");
                        break;
                    case "1":
                        textViewStatus.setText("步行");
                        break;
                    case "2":
                        textViewStatus.setText("快走/慢跑");
                        break;
                    case "3":
                        textViewStatus.setText("快跑");
                        break;
                    case "4":
                        textViewStatus.setText("判断中...");
                        break;
                    default:
                        textViewStatus.setText("error");
                        break;
                }
                distance = df.format(stepNum * stepMagnitude) + " m";
                consumption = df.format(stepNum * stepConsumption) + " C";

                textStep.setText(s2[0]);//显示记步数
                textViewDistance.setText(distance);
                textViewConsumption.setText(consumption);

                //延时500ms发送值为REQUEST_SERVER 消息
                delayHandler.sendEmptyMessageDelayed(REQUEST_SERVER, TIME_INTERVAL);
                break;
            case REQUEST_SERVER:
                try {
                    Message message = Message.obtain(null, MSG_FROM_CLIENT);//发送消息
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
        super.onDestroy();
        unbindService(conn); //解除服务的绑定

        ActivityCollector.removeActivity(this);
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
                // TODO 过去一周的每日记步数回顾
                break;
            case R.id.action_reset:
                Intent intentReset = new Intent(this, StepService.class);
                stopService(intentReset);
                unbindService(conn);
                bindService(intentReset, conn, BIND_AUTO_CREATE);
                startService(intentReset);
                break;
            case R.id.action_quit:
                ActivityCollector.finishAll();
                Intent intentQuit = new Intent(this, StepService.class);
                stopService(intentQuit);
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
