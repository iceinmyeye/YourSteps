package com.mindray.yoursteps.bean;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

/**
 * Created by 董小京 on 2017/5/24.
 */

@Table("target")

public class StepTarget {

    @PrimaryKey(AssignType.AUTO_INCREMENT)
    private int id;

    @Column("date")
    private String date;

    @Column("target")
    private String target;

    public StepTarget(String date, String target) {
        this.date = date;
        this.target = target;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }
}
