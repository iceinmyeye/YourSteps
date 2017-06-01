package com.mindray.yoursteps.bean;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

import java.util.ArrayList;

/**
 * Created by 董小京 on 2017/6/1.
 */

@Table("tree")   //标记为表名  使用了第三方框架

public class TreeData {

    //@*** 加入的jar包里的功能，方便对于数据库的管理
    //指定自增，每个对象需要一个主键
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    private int id;

    @Column("day")
    private String day;
    @Column("tree")
    private ArrayList tree;

    public TreeData(String day, ArrayList tree) {
        this.day = day;
        this.tree = tree;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public ArrayList getTree() {
        return tree;
    }

    public void setTree(ArrayList tree) {
        this.tree = tree;
    }
}
