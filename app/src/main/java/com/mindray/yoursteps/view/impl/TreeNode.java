package com.mindray.yoursteps.view.impl;

/**
 * Created by DuXuan on 2017/5/1.
 */


import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    public String attribute; // 属性
    public List<String> attributeValue; // 属性对应的值
    public List<TreeNode> child; // 子节点
    // for leaf node
    public boolean isLeaf; // 叶子
    public String targetValue; // status
    public static List<ArrayList<String>> str = new ArrayList<ArrayList<String>>();
    public ArrayList<String> str1 = new ArrayList<String>();
    public static String s = "";
    public static String s2 = "";

    TreeNode() {
        attributeValue = new ArrayList<String>();
        child = new ArrayList<TreeNode>();
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public List<String> getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(List<String> attributeValue) {
        this.attributeValue = attributeValue;
    }

    public void addAttributeValue(String attributeValue) {
        this.attributeValue.add(attributeValue);
    }

    public List<TreeNode> getChild() {
        return child;
    }

    public void setChild(List<TreeNode> child) {
        this.child = child;
    }

    public void addChild(TreeNode child) {
        this.child.add(child);
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    public String getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
    }

    int b = 0;

    public void print(String depth) {
        int a = 0;

        if (!this.isLeaf) {
            System.out.println(depth + this.attribute);
            // str.add(this.attribute);
            depth += "\t";
            for (int i = 0; i < this.attributeValue.size(); i++) {
                System.out.println(depth + "---(" + this.attributeValue.get(i) + ")---");

                // System.out.println("~attributeValue:"+attributeValue);
                // str.add(this.attribute);
                if (str1.contains("if " + this.attribute + this.attributeValue.get(1 - i))) {

                    // System.out.println(str.indexOf("if " + this.attribute +
                    // this.attributeValue.get(1 - i)));
                    // str.add(str.get(index))
                }
                // str1.add("if " + this.attribute +
                // this.attributeValue.get(i));
                s = s + "if " + this.attribute + this.attributeValue.get(i) + " ";
                // str.add(b,"if "+this.attribute+this.attributeValue.get(i));
                this.child.get(i).print(depth + "\t"); // child's tree iterate

            }
        } else {

            // String temp = str.subList(fromIndex, toIndex)
            System.out.println(depth + "[" + this.targetValue + "]");
            // System.out.println("s: " + s);
            String[] s3 = s.split("\\s+");
            if (s3[1].split("\\<=").length >= 2 && // ????????????还需再讨论   =2 || >=2
                    // 是否是只有两个的需要扩展！！！！！！！！！！！！！！！！！！！！
                    (s.length() < str.get(str.size() - 1).get(0).length())) {//
                String[] s4 = s3[1].split("\\<=");
                String s5 = s4[0] + ">" + s4[1];
                // System.out.println(s5);
                if (str.get(str.size() - 1).get(0).contains(s5)) {
                    // System.out.println("+++++-----++++");
                    String s7 = "if " + s5;
                    String[] s6 = str.get(str.size() - 1).get(0).split(s7);
                    // System.out.println("s6:"+s6[0]);
                    s = s6[0] + s;
                }
            }
            str1.add(s + "," + "result is " + this.targetValue);
            str.add(str1);
            a = str.size();
            s = "";
        }

    }

}