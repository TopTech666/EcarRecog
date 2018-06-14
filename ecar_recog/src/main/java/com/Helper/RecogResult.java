package com.Helper;
//权限回调接口
public interface RecogResult {
    void recogSuccess(String carPlate, byte[] picData);  //识别成功

    void recogFail();   //识别失败

    void permitionSuccess();  //授权成功

    void permitionFail();   //授权失败
}