package com.mindray.yoursteps.view.impl;

/**
 * Created by DuXuan on 2017/5/1.
 */


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class DecisionTree {

    //	InfoGainRatio infoGainRatio = new InfoGainRatio();
    public static int station = 0;
    // public static String[][] splitV = {{""}};
    public static ArrayList<String> list = new ArrayList<String>();

    public TreeNode createDecisionTree(List<String> attribute, List<ArrayList<String>> dataset) {
        TreeNode tree = new TreeNode();

        // check if it is pure

        // boolean a = DataSetUtil.isPure(DataSetUtil.getTarget(dataset));
        if (DataSetUtil.isPure(DataSetUtil.getTarget(dataset))) {
            tree.setLeaf(true); // pure leaf
            tree.setTargetValue(DataSetUtil.getTarget(dataset).get(0));
            System.out.println("DataSetUtil.getTarget(dataset).get(0):" + DataSetUtil.getTarget(dataset).get(0));
            // print the status,never has a child.
            return tree; // same station will be set as leaf.
        } // that is interesting.
        // choose the best attribute
        // int bestAttr = getBestAttribute(attribute, dataset); // function
        // was
        double[] receive = new double[3];

        receive = allMin(dataset, attribute);

        int bestAttr = (int) receive[2];// get bestAttribute
        // given later.
        // create a decision tree
        tree.setAttribute(attribute.get(bestAttr)); // bestAttr will be tree
        // Attribute. mean,
        // variance...
        System.out.println("attribute.get(bestAttr):" + bestAttr);
        tree.setLeaf(false); // is not a leaf
        List<String> attrValueList = DataSetUtil.getAttributeValueOfUnique(receive, dataset); // replace
        // bestAttr
        // 我得再修改此部分的内容
        // attrValueList:
        // >1.5
        // <=1.5
        List<String> subAttribute = new ArrayList<String>();
        subAttribute.addAll(attribute); //
        // subAttribute.remove(bestAttr); // replace bestAttr 这边我注释掉了
        System.out.println("subAttribute" + subAttribute);
        for (String attrValue : attrValueList) {
//			System.out.println("+++++++++++++++" + attrValue);
//			System.out.println("----------------" + attrValue);
            // 更新数据集dataSet
            List<ArrayList<String>> subDataSet = DataSetUtil.getSubDataSetByAttribute(dataset, bestAttr, attrValue);
            // 递归构建子树
            TreeNode childTree = createDecisionTree(subAttribute, subDataSet); // remove
            // the
            // bestAttr?
            tree.addAttributeValue(attrValue);
            tree.addChild(childTree); // 构建递归子树，在这里分为两个child.
        }
        System.out.println("attrValueList::::" + attrValueList);
        return tree;
    }

    /**
     * compare infoGainRatio 选出最优属性
     *
     * @param
     * @param dataset
     * @return bestAttr
     */
    // public int getBestAttribute(List<String> attribute,
    // List<ArrayList<String>> dataset) {
    // // calculate the gainRatio of each attribute, choose the max
    // int bestAttr = 0;
    // double maxGainRatio = 0;
    //
    // for (int i = 0; i < attribute.size(); i++) {
    // double thisGainRatio = infoGainRatio.getGainRatio(i, dataset);
    // if (thisGainRatio > maxGainRatio) {
    // maxGainRatio = thisGainRatio;
    // bestAttr = i;
    // }
    // }
    // System.out.println("The best attribute is \"" + attribute.get(bestAttr) +
    // "\"");
    // return bestAttr;
    // } // should be changed

    // sort by stri attribute
    public static void sort1(List<ArrayList<String>> dataset, int atri) {
        // String temp = new String();
        for (int i = 0; i < dataset.size(); i++) {
            for (int j = i + 1; j < dataset.size(); j++) {
                if (Double.valueOf(dataset.get(i).get(atri)) > Double.valueOf(dataset.get(j).get(atri))) {
                    @SuppressWarnings("unchecked")
                    ArrayList<String> num2 = (ArrayList<String>) dataset.get(i).clone();
                    dataset.set(i, dataset.get(j));
                    dataset.set(j, num2);
                }
            }
        }
        System.out.println("根据" + atri + "排序后的：" + dataset);
    }

    // 分裂点的位置

    public static List<Double> split1(List<ArrayList<String>> dataset, int atri) {
        int len = dataset.get(0).size(); // length
        List<Double> tzz = new ArrayList<Double>(); // store split point.
        for (int i = 1; i < dataset.size(); i++) {
            if (!dataset.get(i - 1).get(len - 1).equals(dataset.get(i).get(len - 1))) {
                tzz.add((Double.parseDouble(dataset.get(i - 1).get(atri))
                        + Double.parseDouble(dataset.get(i).get(atri))) / 2);
            }
        }
        return tzz;
    }

    public static double[] split2(List<ArrayList<String>> dataset, int atri, double tzz) {
        double[] st1 = new double[2];
        for (int i = 0; i < dataset.size(); i++) {
            if (Double.parseDouble(dataset.get(i).get(atri)) < tzz) {
                st1[0] += 1;

            } else {
                st1[1] += 1;
            }
        }
        return st1;
    }

    // 1个分裂点的信息增益值
    public static double[] split3(List<ArrayList<String>> dataset, int atri, double tzz) {
        double[] st2 = new double[2];
        st2 = split2(dataset, atri, tzz);
        double[] str1 = new double[4];
        // System.out.println(st2[1]);
        str1 = prob(dataset, 0, st2[0]); // not include st2[0];
        double[] str2 = new double[4];
        str2 = prob(dataset, st2[0], st2[0] + st2[1]);
        double x1 = 0;
        double x2 = 0;
        for (int i = 0; i < 4; i++) {
            x1 += (-str1[i] * Math.log(str1[i]) / Math.log(2));
            x2 += (-str2[i] * Math.log(str2[i]) / Math.log(2));
        }
        double[] sum = new double[3];
        sum[0] = st2[0] / (st2[0] + st2[1]) * x1 + st2[1] / (st2[0] + st2[1]) * x2;
        sum[1] = tzz;
        sum[2] = atri;
        System.out.println("sum:" + sum[0] + " ????????" + sum[1] + " " + sum[2]);
        return sum;
    }

    // 求最小的信息增益与分类点的值

    public static double[] minValue(List<ArrayList<String>> dataset, int atri, List<Double> tzz) {
        double[][] sum1 = new double[tzz.size()][3];

        for (int i = 0; i < tzz.size(); i++) {
            sum1[i] = split3(dataset, atri, tzz.get(i));
        }
        double[] min = {Double.MAX_VALUE, 0, 0};
        for (int i = 0; i < sum1.length; i++) {
            if (min[0] > sum1[i][0]) {
                min = sum1[i];
            }
        }
        System.out.println("min:" + min[0] + " " + min[1] + " " + min[2]);
        return min;
    }

    /**
     * compare infoGainRatio 选出最优属性3 {gainValue(we want minimum),
     * splitValue(sum/2) , feature{mean ,var
     *
     * @param attribute
     * @param dataset
     * @return temporary
     */
    // 选出最优属性 {gainValue(we want minimum), splitValue(sum/2) , feature{mean
    // ,variance
    // ...}}
    public static double[] allMin(List<ArrayList<String>> dataset, List<String> attribute) {
        int len = dataset.get(0).size() - 1;
        double[][] allmin = new double[len][3];
        double[] temp = {Double.MAX_VALUE, 0, 0};
        for (int i = 0; i < len; i++) {
            sort1(dataset, i);
            allmin[i] = minValue(dataset, i, split1(dataset, i));
            // System.out.println("split1(dataset, i)"+split1(dataset, i));
        }
        for (int i = 0; i < len; i++) {
            if (temp[0] > allmin[i][0]) {
                temp = allmin[i];
            }
        }
        System.out.println((int) temp[2]);
        System.out.println(attribute);
        System.out.println("The best attribute is " + attribute.get((int) temp[2]) + temp[0] + "!!!!!!!!!!!!!!!!!");
        return temp;
    }

    // 求各个状态的概率
    public static double[] prob(List<ArrayList<String>> dataset, double start, double end) {
        if (start > end) {
            double[] x = {1, 1, 1, 1};
            return x;
        }
        double[] probility1 = new double[4]; // 4 状态 or 6?
        int len = dataset.get(0).size();
        // System.out.println("len"+len);
        double a = end - start;

        for (int i = (int) start; i < (int) end; i++) {
            // System.out.println("I"+i);
            if (dataset.get(i).get(len - 1).equals("1")) {
                probility1[0] += 1 / (a);
            }
            if (dataset.get(i).get(len - 1).equals("2")) {
                probility1[1] += 1 / (a);
            }
            if (dataset.get(i).get(len - 1).equals("3")) {
                probility1[2] += 1 / (a);
            }
            if (dataset.get(i).get(len - 1).equals("4")) {
                probility1[3] += 1 / (a);
            }
            // if(subdataset.get(i).get(len-1).equals("4")){
            // probility1[0]+= 1/(subdataset.size());
            // }
            // if(subdataset.get(i).get(len-1).equals("4")){
            // probility1[0]+= 1/(subdataset.size());
            // }

        }
        for (int i = 0; i < probility1.length; i++) {
            if (probility1[i] == 0) {
                probility1[i] = 1;
            }
        } // prevent from negative

        return probility1;
    }

    public static void main(String args[]) {
        // eg 1
        String attr = "Mean Var CSVM Points";
        String[] set = CalibrationActivity.set;

        //测试数据
//        set[0] = "10.6085 5.7470 0.9803 28 1";
//        set[1] = "10.8130 12.3204 1.1946 28.667 1";
//        set[2] = "10.0873 10.6999 0.7870 29.5 1";
//        set[3] = "10.5063 10.9859 0.9803 29.667 1";
//        set[4] = "10.6270 7.6146 0.9560 28.6667 1";
//        set[5] = "10.5998 12.7507 1.1606 28.6667 1";
//        set[6] = "10.5441 11.0925 1.1177 27 1";
//        set[7] = "10.6023 11.3307 1.0596 29.6667 1";
//        set[8] = "10.5660 10.0049 1.0389 28.6 1";
//        set[9] = "10.6416 11.4286 1.1638 29 1";
//        set[10] = "10.6122 11.0677 1.1407 27.3 1";
//        set[11] = "10.6986 11.2628 1.1182 28 1";
//        set[12] = "10.4196 10.3618 1.0591 29.7 1";
//
//        set[13] = "20.3479 32.5504 1.9154 39 2";
//        set[14] = "10.2323 28.4069 1.8382 35.5 2";
//        set[15] = "10.7455 28.4069 1.8382 30.667 2";
//        set[16] = "10.1051 27.4119 2.2173 30 2";
//        set[17] = "10.7799 21.6391 1.8945 30 2";
//        set[18] = "10.8486 22.1199 1.9721 29 2";
//        set[19] = "10.4915 17.8499 1.7111 30 2";
//        set[20] = "10.1746 20.2816 1.8701 30 2";
//        set[21] = "10.8206 16.9738 1.7319 30.3 2";
//        set[22] = "10.2780 23.75 1.5472 34.5 2";
//        set[23] = "10.6633 25.2095 2.1551 31.6667 2";
//        set[24] = "10.7251 15.0332 1.5043 31 2";
//        set[25] = "10.5555 22.2645 1.9937 31.667 2";
//
//        set[26] = "26.7047 38.1135 2.7494 18.5 3";
//        set[27] = "11.9901 37.4412 3.5233 17.5 3";
//        set[28] = "11.6179 33.5767 3.3701 17.5 3";
//        set[29] = "11.6486 23.4906 2.4 17.2 3";
//        set[30] = "12.14 33.03 2.7 18.6 3";
//        set[31] = "12.24 31.2017 2.67 17.6 3";
//        set[32] = "11.7068 27.6698 2.54 28.6 3";
//        set[33] = "11.6 26 2.57 18 3";
//        set[34] = "11.17 22 2.1765 18 3";
//        set[35] = "11.05 27.82 2.205 19.75 3";
//        set[36] = "11.9 30 2.72 19 3";
//        set[37] = "11.74 36 2.95 20.4 3";
//        set[38] = "12.2 34.05 2.771 20.2 3";
//
//        set[39] = "32.2139 56.5 4.24 16.7 4";
//        set[40] = "31.89 56.45 5.117 14.43 4";
//        set[41] = "29.21 69.77 5.266 15.14 4";
//        set[42] = "28.744 64.9159 4.757 15.17 4";
//        set[43] = "30.01 56.55 5.45 14.856 4";
//        set[44] = "28.98 60.27 4.83 15.3 4";
//        set[45] = "29.7 52.94 4.99 15.4 4";
//        set[46] = "29.83 56.694 4.72 15.5 4";
//        set[47] = "26.5 69.98 4.93 15.83 4";
//        set[48] = "26.64 81 5 16 4";
//        set[49] = "25.74 79 5 16.3 4";
//        set[50] = "24.41 82.5 4.32 20.2 4";
//        set[51] = "25 80 4.26 22.4 4";

        // String[] set = new String[5];
        // set[0] = "2 2 2 2 2";
        // set[1] = "1 1 1 1 1";
        // set[2] = "2 3 3 2 3";
        // set[3] = "4 4 4 4 4";
        // set[4] = "1 1 1 2 1";

        List<ArrayList<String>> dataset = new ArrayList<ArrayList<String>>();
        List<String> attribute = Arrays.asList(attr.split(" ")); // 数组转化为list
        for (int i = 0; i < set.length; i++) {
            String[] s = set[i].split(" ");
            ArrayList<String> list = new ArrayList<String>();
            for (int j = 0; j < s.length; j++) {
                list.add((s[j]));
            }
            dataset.add(list); // 原始数据获取
        }

        DecisionTree dt = new DecisionTree();
        long a = System.currentTimeMillis();

        System.out.println("attribute:" + attribute);
        TreeNode tree = dt.createDecisionTree(attribute, dataset);
        tree.print("");
        long b = System.currentTimeMillis();
        System.out.println(b - a);
        // System.out.println(TreeNode.str1);
        System.out.println(TreeNode.str);
        for (int i = 0; i < TreeNode.str.size(); i++) {
            System.out.println(TreeNode.str.get(i));
            String str1 = TreeNode.str.get(i).get(0);
            // System.out.println("--------------------------");
            System.out.println(str1);
            String[] str2 = str1.split("\\,");
            System.out.println(str2[0]);
            String[] str3 = str2[0].split("\\s+");
            String[] str4 = str2[1].split("\\s+");
            String str5 = "";
            for (int j = 1; j < str3.length; j += 2) {
                str5 += str3[j] + " ";
            }
            System.out.println(str5);
            list.add(str5 + str4[str4.length - 1]);
        }
        System.out.println(list);
        System.out.println(list.size());
        double[] input = {12.25, 10.10, 3.2, 15.6};
        station = Integer.parseInt(recognition(list, input));
        System.out.println("status:" + station);

        //// for(int i=0;i<TreeNode.str.size();i++){
        //// System.out.println("size:"+ splitV[i].length);
        //// splitV[i] = TreeNode.str.get(i).get(0).split("\\s+");
        //// System.out.println("size2:"+ splitV[i].length);
        //// for(int j=1;j<splitV[i].length-2;j+=2){
        //// System.out.print(splitV[i][j]+" "); //
        ////// System.out.print(split[split.length-1]); //运动状态
        //// }
        //// System.out.println(splitV[i][splitV[i].length-1]);
        // }
    }

    public static String recognition(ArrayList<String> list, double[] eigenValues) {
//		double[] eigenValues = new double[4];
        String s = "";
        System.out.println("list1111:" + list);
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }

        for (int i = 0; i < list.size(); i++) {
            String[] a1 = list.get(i).split("\\s+");
            boolean status = true;
            for (int j = 0; j < a1.length; j++) {
                String[] a2 = a1[j].split("\\>");
                String[] a3 = a1[j].split("\\<=");
                if (a2.length == 2) {
                    if (a2[0].equals("Mean")) {
                        if (Double.parseDouble(a2[1]) > eigenValues[0]) {
                            System.out.println("不满足1");
                            System.out.println(a2[1] + "<" + eigenValues[0]);
                            status = false;
                            break;
                        }
                    }
                    if (a2[0].equals("Var")) {
                        if (Double.parseDouble(a2[1]) > eigenValues[1]) {
                            System.out.println("不满足2");
                            status = false;
                            break;
                        }
                    }
                    if (a2[0].equals("CSVM")) {
                        if (Double.parseDouble(a2[1]) > eigenValues[2]) {
                            System.out.println("不满足3");
                            status = false;
                            break;
                        }
                    }
                    if (a2[0].equals("Points")) {
                        if (Double.parseDouble(a2[1]) > eigenValues[3]) {
                            System.out.println("不满足4");
                            status = false;
                            break;
                        }
                    }
                    System.out.println(a2[0]);
                } // 大于的情况
                if (a3.length == 2) {
                    System.out.println(a3[1]);
                    if (a3[0].equals("Mean")) {
                        if (Double.parseDouble(a3[1]) <= eigenValues[0]) {
                            System.out.println("不满足5");
                            status = false;
                            break;
                        }
                    }
                    if (a3[0].equals("Var")) {
                        if (Double.parseDouble(a3[1]) <= eigenValues[1]) {
                            System.out.println("不满足6");
                            status = false;
                            break;
                        }
                    }


                    if (a3[0].equals("CSVM")) {
                        if (Double.parseDouble(a3[1]) <= eigenValues[2]) {
                            System.out.println("不满足7");
                            status = false;
                            break;
                        }
                    }
                    if (a3[0].equals("Points")) {
                        if (Double.parseDouble(a3[1]) <= eigenValues[3]) {
                            System.out.println("不满足8");
                            status = false;
                            break;
                        }
                    }
                    System.out.println(a3[0]);
                }
                System.out.println(a1[a1.length - 1]);
            }
            if (status) {
                s = a1[a1.length - 1];
                break;
            }
        }
        return s;

    }
}
