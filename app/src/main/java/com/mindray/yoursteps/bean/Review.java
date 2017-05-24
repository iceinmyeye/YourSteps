package com.mindray.yoursteps.bean;

/**
 * Created by 董小京 on 2017/5/24.
 */

public class Review {

    private String stepDate;

    private String stepReviewNum;

    private String stepTargetNum;

    public Review(String stepDate, String stepReviewNum, String stepTargetNum) {
        this.stepDate = stepDate;
        this.stepReviewNum = stepReviewNum;
        this.stepTargetNum = stepTargetNum;
    }

    public String getStepDate() {
        return stepDate;
    }

    public String getStepReviewNum() {
        return stepReviewNum;
    }

    public String getStepTargetNum() {
        return stepTargetNum;
    }
}
