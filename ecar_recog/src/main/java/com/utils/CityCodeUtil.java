package com.utils;

import android.content.Context;


import java.util.HashMap;

public class CityCodeUtil {
    private static HashMap<String, Integer> codeMaps;//城市code集合

    private static String[] cityNames = new String[]{
            "京", "津", "沪", "冀", "黑",
            "吉", "辽", "豫", "晋", "陕",
            "宁", "甘", "新", "蒙", "青",
            "藏", "川", "贵", "湘", "鄂",
            "闽", "苏", "浙", "皖", "鲁",
            "桂", "云", "粤", "琼", "渝",
            "赣", "港", "学", "军", "空",
            "海", "北", "沈", "兰", "济",
            "南", "广", "成", "警", "消",
            "边", "通", "森", "金", "使",
            "挂"};
    private static int[] codes = new int[]{
            43454, 62141, 42683, 48572, 55994,
            43708, 51649, 42452, 64189, 49865,
            65220, 51896, 49872, 51651, 57543,
            55474, 43188, 62393, 59087, 62902,
            63171, 54731, 58325, 61133, 46018,
            61625, 50900, 49620, 60871, 58835,
            54200, 56248, 42961, 64702, 54719,
            41914, 45489, 62153, 48320, 50108,
            53188, 58297, 51635, 44990, 64463,
            57265, 43213, 44489, 61629, 47562,
            53945
    };

    static {
        codeMaps = new HashMap<>();
        int length = cityNames.length;
        for (int i = 0; i < length; i++) {
            codeMaps.put(cityNames[i], codes[i]);
        }
    }

    /****************************************
     * 方法描述：   获取城市code
     *
     * @param city 城市  比如：川
     * @return
     ****************************************/
    public static int getCityCode(String city, Context context) {
        if (codeMaps.keySet().contains(city)) {
            return codeMaps.get(city);
        } else {
            return codeMaps.get("粤");
        }
    }

}
