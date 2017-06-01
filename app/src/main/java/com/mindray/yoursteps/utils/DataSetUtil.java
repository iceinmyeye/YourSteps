package com.mindray.yoursteps.utils;

/**
 * Created by DuXuan on 2017/5/1.
 */


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataSetUtil {

    /**
     * 获取数据集中的结果列 we can use it.
     *
     * @param dataset
     * @return
     */

    public static List<String> getTarget(List<ArrayList<String>> dataset) {
        List<String> target = new ArrayList<String>();
        int targetId = dataset.get(0).size() - 1; // 第0个的长度
        // System.out.println("targetId:"+dataset);
        for (List<String> element : dataset) {
            target.add(element.get(targetId));
        }
//		System.out.println("targetId:" + target);
        return target;
    }

    /**
     * 获取属性值 有好多
     *
     * @param attrId
     * @param dataset
     * @return
     */
    public static List<String> getAttributeValue(int attrId, List<ArrayList<String>> dataset) {
        List<String> attrValue = new ArrayList<String>();

        for (List<String> element : dataset) {
            attrValue.add(element.get(attrId));
        }
//		System.out.println("attrValue1:" + attrValue + attrId);
        return attrValue;
    }

    /**
     * 获取属性值，唯一值
     *
     * @param atr
     * @param dataset
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<String> getAttributeValueOfUnique(int atr, List<ArrayList<String>> dataset) {
        Set attrSet = new HashSet();
        List<String> attrValue = new ArrayList<String>();
        for (List<String> element : dataset) {
            attrSet.add(element.get((int) (atr)));
        }

        Iterator iterator = attrSet.iterator();
        while (iterator.hasNext()) {
            attrValue.add((String) iterator.next());
        }
//		System.out.println("@@@attrValue2:" + attrValue);
        return attrValue;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<String> getAttributeValueOfUnique(double[] rec, List<ArrayList<String>> dataset) {
        Set attrSet = new HashSet();
        List<String> attrValue = new ArrayList<String>();
        attrValue.add(">" + rec[1]);
        attrValue.add("<=" + rec[1]);
//		System.out.println("@@@attrValue2:" + attrValue);
        return attrValue;
    }

    /**
     * for test <br/>
     * 输出数据集
     *
     * @param attribute
     * @param dataset
     */
    public static void printDataset(List<String> attribute, List<ArrayList<String>> dataset) {
        System.out.println("123:" + attribute);
        for (List<String> element : dataset) {
            System.out.println(element);
        }
    }

    /**
     * 数据集纯度检测 we need to change this 10%的误差
     */
    public static boolean isPure(List<String> data) {
        String result = data.get(0);
        double len = data.size();
        double b = 0;
        for (int i = 1; i < data.size(); i++) {
            if (!data.get(i).equals(result))
                b += 1;

        }
        if (b / len > 0.05) {  //纯度检测
            return false;
        }
        return true;
    }

    /**
     * 对一列进行概率统计
     *
     * @param list
     * @return
     */
    public static Map<String, Double> getProbability(List<String> list) {
        double unitProb = 1.00 / list.size();
        Map<String, Double> probability = new HashMap<String, Double>();
        for (String key : list) {
            if (probability.containsKey(key)) {
                probability.put(key, unitProb + probability.get(key));
            } else {
                probability.put(key, unitProb);
            }
        }
        System.out.println("probability" + probability);
        return probability;
    }

    /**
     * 根据属性值，分离出结果列target
     *
     * @param attrValue
     * @param attrValueList
     * @param targetList
     * @return
     */
    public static List<String> getTargetByAttribute(String attrValue, List<String> attrValueList,
                                                    List<String> targetList) {
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < attrValueList.size(); i++) {
            if (attrValueList.get(i).equals(attrValue))
                result.add(targetList.get(i));
        }
        return result;
    }

    /**
     * 拿出指定属性值对应的子数据集
     *
     * @param dataset
     * @param attrId
     * @param attrValue
     * @return
     */
    public static List<ArrayList<String>> getSubDataSetByAttribute(List<ArrayList<String>> dataset, int attrId,
                                                                   String attrValue) {// 那一列就是最佳属性， 及其分裂点>1.5
        List<ArrayList<String>> subDataset = new ArrayList<ArrayList<String>>();
        String subAttr = new String();
        if (attrValue.charAt(1) == '=') {
            subAttr = attrValue.substring(2);
            System.out.println("==============");
        } else {
            subAttr = attrValue.substring(1);
        }

        for (ArrayList<String> list : dataset) {
            switch (attrValue.charAt(0)) {
                case '>': {
                    if (Double.parseDouble(list.get(attrId)) > Double.parseDouble(subAttr)) {
                        subDataset.add(list);
                        System.out.println("attrValue:" + attrValue);
                    }
                    break;
                }
                case '<': {
                    if (Double.parseDouble(list.get(attrId)) <= Double.parseDouble(subAttr)) {
                        subDataset.add(list);
                        System.out.println("attrValue:" + attrValue);
                    }
                    break;
                }
                default:
                    break;

            }
            if (list.get(attrId).equals(attrValue)) {
                ArrayList<String> cutList = new ArrayList<String>();
                cutList.addAll(list);
                cutList.remove(attrId);
                subDataset.add(cutList);
                System.out.println("attrValue:" + attrValue);
            }
        }
        System.out.println("subDataset：datasetutil" + subDataset + attrId);

        return subDataset;
    }

    public static String getString(List<ArrayList<String>> dataset, int i, int j) {
        return dataset.get(i).get(j);
    }

}
