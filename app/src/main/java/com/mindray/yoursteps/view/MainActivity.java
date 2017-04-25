package com.mindray.yoursteps.view;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
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
import com.mindray.yoursteps.data.StepService;
import com.mindray.yoursteps.view.impl.SettingsActivity;

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

    // 定义ServiceConnection对象
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
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

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        initUI();
        setupService();
    }

    // 启动UI
    private void initUI() {
        // TODO Auto-generated method stub
        textStep = (TextView) findViewById(R.id.textView_step);
        delayHandler = new Handler(this);
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
        // TODO Auto-generated method stub
        switch (msg.what) {
            case MSG_FROM_SERVER:
                Log.d(TAG, "text=" + msg.getData().getInt("step"));
                textStep.setText(msg.getData().getInt("step") + "");//显示记步数
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

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unbindService(conn);//解除服务的绑定
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
                startActivity(settingsIntent);
                break;
            case R.id.action_reset:
                break;
            case R.id.action_quit:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    // End of Menu
}
