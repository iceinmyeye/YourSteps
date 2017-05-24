package com.mindray.yoursteps.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by 董小京 on 2017/5/24.
 */

public class DateUtils {

    // 获取当天日期信息，格式为yyyy-MM-dd
    public static String getTodayDate() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    // 获取距今n天之前的日期，日期格式为yyyy-MM-dd
    public static String getSomeDate(int n) {
        long before = n * (24 * 60 * 60 * 1000);
        Date date = new Date(System.currentTimeMillis() - before);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }
}
