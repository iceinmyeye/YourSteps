package com.mindray.yoursteps.view.impl;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.mindray.yoursteps.R;
import com.mindray.yoursteps.bean.Review;
import com.mindray.yoursteps.bean.StepTarget;
import com.mindray.yoursteps.config.Constant;
import com.mindray.yoursteps.utils.StepDateUtils;
import com.mindray.yoursteps.utils.DbUtils;
import com.mindray.yoursteps.view.MainActivity;
import com.mindray.yoursteps.view.adapter.ReviewAdapter;

import java.util.ArrayList;
import java.util.List;

public class ReviewActivity extends AppCompatActivity {

    private int exist;

    private List<Review> reviewList = new ArrayList<Review>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReviewActivity.this.finish();
            }
        });

        initReview();
        ReviewAdapter adapter = new ReviewAdapter(ReviewActivity.this, R.layout.review_item, reviewList);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);
    }

    private void initReview() {

        for (int i = 1; i < 30; i++) {

            List<StepTarget> listTarget = DbUtils.getQueryByWhere(StepTarget.class, "date", new String[]{StepDateUtils.getSomeDate(i)});

            if (listTarget.size() == 0 || listTarget.isEmpty()) {

                exist++;

            } else if (listTarget.size() == 1) {

                String reviewDate = listTarget.get(0).getDate();
                String reviewStep = MainActivity.reviewSeven[i-1];
                String reviewTarget = listTarget.get(0).getTarget();
                Review review = new Review(reviewDate, reviewStep, reviewTarget);
                reviewList.add(review);
            }
        }

        // exist的值大于6，即7次循环判断结果均为无历史数据
        if (exist > 28) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(ReviewActivity.this);
            dialog.setMessage(this.getResources().getString(R.string.info_alert));
            dialog.setCancelable(false);
            dialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ReviewActivity.this.finish();
                }
            });
            dialog.show();
        }
    }

//    @Override
//    public void onBackPressed() {
////        DbUtils.closeDb();
//        ReviewActivity.this.finish();
//    }

    @Override
    protected void onDestroy() {
//        DbUtils.closeDb();
        super.onDestroy();
    }
}
