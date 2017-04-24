package com.mindray.yoursteps.utils;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 董小京 on 2017/4/24.
 * 用来实现一次性销毁所有活动
 */

public class ActivityCollector {
    public static List<Activity> activities = new ArrayList<Activity>();

    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public static void finishAll() {
        for (Activity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }
}
