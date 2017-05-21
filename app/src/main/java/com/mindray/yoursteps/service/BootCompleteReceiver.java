package com.mindray.yoursteps.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 开机完成广播
 * Created by 董小京 on 2017/5/21.
 */

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent){
        Intent i=new Intent(context,StepService.class);
        context.startService(i);
    }
}
