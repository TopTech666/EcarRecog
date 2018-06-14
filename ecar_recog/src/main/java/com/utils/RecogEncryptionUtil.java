package com.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 3DES加密工具类
 */
public class RecogEncryptionUtil {

    public static String MYRECOG_LOC = "110001 110010 110011";  //位置
    public static String MYRECOG_SCOPE = "110010 110101 110101"; //范围
    public static String MYRECOG_VALUE = "110001 110010 111000"; //值

    // 将二进制字符串转换成Unicode字符串
    public static String binaryStrToStr(String binStr) {
        String[] tempStr = strToStrArray(binStr);
        char[] tempChar = new char[tempStr.length];
        for (int i = 0; i < tempStr.length; i++) {
            tempChar[i] = binaryStrToChar(tempStr[i]);
        }
        return String.valueOf(tempChar);
    }


    public static int getLocation(String code) {
        return Integer.valueOf(binaryStrToStr(code));
    }


    // 将初始二进制字符串转换成字符串数组，以空格相隔
    private static String[] strToStrArray(String str) {
        return str.split(" ");
    }

    // 将二进制字符串转换为char
    private static char binaryStrToChar(String binStr) {
        int[] temp = binaryStrToIntArray(binStr);
        int sum = 0;
        for (int i = 0; i < temp.length; i++) {
            sum += temp[temp.length - 1 - i] << i;
        }
        return (char) sum;
    }

    // 将二进制字符串转换成int数组
    private static int[] binaryStrToIntArray(String binStr) {
        char[] temp = binStr.toCharArray();
        int[] result = new int[temp.length];
        for (int i = 0; i < temp.length; i++) {
            result[i] = temp[i] - 48;
        }
        return result;
    }

    public static void setPoint(byte[] data, int location) {
        int mloc = RecogEncryptionUtil.getLocation(RecogEncryptionUtil.MYRECOG_LOC);
        int value = RecogEncryptionUtil.getLocation(RecogEncryptionUtil.MYRECOG_VALUE);
        data[mloc] = (byte) location;
        data[location] = (byte) value;
    }

    public static String getSHA(String val) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("SHA-1");
        md5.update((val.concat("ecaray")).getBytes());
        byte[] m = md5.digest();// 加密
        return getString(m);
    }

    private static String getString(byte[] b) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            sb.append(b[i]);
        }
        return sb.toString();
    }
}
