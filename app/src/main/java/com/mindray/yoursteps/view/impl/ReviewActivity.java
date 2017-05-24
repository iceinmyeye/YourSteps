package com.mindray.yoursteps.view.impl;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.mindray.yoursteps.R;
import com.mindray.yoursteps.bean.Review;
import com.mindray.yoursteps.view.adapter.ReviewAdapter;

import java.util.ArrayList;
import java.util.List;

public class ReviewActivity extends AppCompatActivity {

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
    }
}
