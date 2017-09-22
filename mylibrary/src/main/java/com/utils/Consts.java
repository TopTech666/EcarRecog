package com.utils;

public class Consts {
    public static final String SPATH = "/.dir/key/key.txt";//加密文件路径
    /**
     * 全局参数
     */
    static public String IMAGGE_DIR = "";
    static public boolean IS_CHECK_PERMITION = true;   //是否开启验证    ture 开启

    static public boolean IS_WENTONG = false;

    public static byte[] orgdata;

    public static int orgw;
    public static int orgh;

    public static float speed;//识别速度
    public static String platenum;//车牌号
    public static int recogingDegger;  //识别次数

    static public String PIC_PATH = ""; //识别图片保存路径(savePic后调用)


    public static final String SP_PERMITION = "recog_permition";  //permition文件
    public static final String IS_GETEDPERMITION = "recog_permition";  //


}