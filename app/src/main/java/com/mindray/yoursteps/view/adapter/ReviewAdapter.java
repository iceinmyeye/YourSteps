package com.mindray.yoursteps.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mindray.yoursteps.R;
import com.mindray.yoursteps.bean.Review;

import java.util.List;

/**
 * Created by 董小京 on 2017/5/24.
 */

public class ReviewAdapter extends ArrayAdapter<Review> {

    private int resourceId;

    public ReviewAdapter(Context context, int textViewResourceId, List<Review> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Review review = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        TextView txtReviewDate = (TextView) view.findViewById(R.id.textView_review_date);
        TextView txtReviewSteps = (TextView) view.findViewById(R.id.textView_review_steps);
        ProgressBar pgReview = (ProgressBar) view.findViewById(R.id.progressBar_ReviewSteps);

        txtReviewDate.setText(review.getStepDate());
        txtReviewSteps.setText(review.getStepReviewNum());
        pgReview.setMax(Integer.parseInt(review.getStepTargetNum()));
        pgReview.setProgress(Integer.parseInt(review.getStepReviewNum()));

        return view;
    }
}
