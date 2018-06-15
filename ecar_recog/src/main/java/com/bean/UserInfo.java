package com.bean;


import cn.bmob.v3.BmobObject;

/*************************************
 功能：
 *************************************/

public class UserInfo extends BmobObject {
    public String  userName;
    public String  passWord;
    public String  parkId;
    public String  dean;
    public String  app;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getParkId() {
        return parkId;
    }

    public void setParkId(String parkId) {
        this.parkId = parkId;
    }

    public String getDean() {
        return dean;
    }

    public void setDean(String dean) {
        this.dean = dean;
    }
}
